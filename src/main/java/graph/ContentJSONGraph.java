package graph;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import edu.stanford.nlp.ling.TaggedWord;
import webservice.Constants;

/**
 * 
 */
public class ContentJSONGraph extends ContentGraph {
	JsonArrayBuilder nodes = Json.createArrayBuilder();
	JsonArrayBuilder edges = Json.createArrayBuilder();
	JsonObjectBuilder metaInfo = Json.createObjectBuilder();

	public ContentJSONGraph(List<Object[]> lobj) {
		buildGraph(lobj, null);
	}

	/**
	 * Con
	 * 
	 * @param lobj
	 */
	public ContentJSONGraph(List<Object[]> lobj, Map<String, String> sentencesMap) {
		buildGraphStr(lobj, sentencesMap);
	}

	public ContentJSONGraph(List<Object[]> lobj, Map<String, String> sentencesMap, Set<String> nodeSet,
			List<List<String>> lstConcept) {
		buildGraphV2(lobj, sentencesMap, nodeSet, lstConcept);
	}

	private void buildGraph(List<Object[]> lobj, Map<String, List<TaggedWord>> sentencesMap) {
		Map<String, String> strMap = new HashMap<>();

		for (String key : sentencesMap.keySet()) {
			List<TaggedWord> tmp = sentencesMap.get(key);
			String strVal = tmp.stream().map(o1 -> o1.value()).collect(Collectors.joining(" "));
			strMap.put(key, strVal);
		}

		this.buildGraphStr(lobj, strMap);
	}

	private void buildGraphStr(List<Object[]> lobj, Map<String, String> sentencesMap) {

		int conceptPos = Arrays.asList(Constants.CONCEPT_HEADER).indexOf(conceptName);
		int conceptIndexPos = Arrays.asList(Constants.CONCEPT_HEADER).indexOf(conceptIndex);
		int conceptNodeIDPos = Arrays.asList(Constants.CONCEPT_HEADER).indexOf(conceptNodeId);

		List<String> sentIdNodes = lobj.stream().map(o -> (String) o[fConceptPos.apply(conceptNodeId)])
				.collect(Collectors.toList());

		Set<String> nodeIdSet = new HashSet<>();

		Set<String> sentIdSet = new HashSet<String>(sentIdNodes);
		String title_pre = "<div class=\"graphPopup\"><article>";
		String title_post = "</article></div>";

		for (String o : sentIdSet) {
			String title = o;
			title = "LinkID: " + o;
			if (sentencesMap != null) {
				title = "<p>LinkID: " + o + "</p>";
				title += "<p>DisplayName: " + sentencesMap.get(o) + "</p>";

				this.metaInfo.add(o, Json.createObjectBuilder().add("title", title).add("isSentence", true).build());
			}

			nodes.add(Json.createObjectBuilder().add("id", o).add("label", "").add("group", o)
					.add("title", title_pre + title + title_post).build());

			nodeIdSet.add(title);
		}

		for (Object[] o : lobj) {

			String key = (String) o[conceptPos];
			String word_position = (String) o[conceptIndexPos];
			String docId = (String) o[conceptNodeIDPos];

			if (!nodeIdSet.contains(key)) {
				nodeIdSet.add(key);

				String title = (String) o[conceptIndexPos];
				title = "<p>NodeName: " + key + "</p>";
				title += "<p>ObjectIndex: " + word_position + "</p>";

				if (sentencesMap != null) {
					title += "<p>DislayName: " + sentencesMap.get(docId) + "</p>";

					this.metaInfo.add(key, Json.createObjectBuilder().add("title", title).add("isSentence", false)
							.add("sentId", docId).build());
				}
				title += "<p>LinkID: " + docId + "</p>";

				// create new node if not found
				nodes.add(Json.createObjectBuilder().add("id", key).add("label", key).add("group", docId)
						.add("title", title_pre + title + title_post).build());
			}
		}

		for (Object[] o : lobj) {
			JsonObject edge = Json.createObjectBuilder().add("from", (String) o[conceptPos])
					.add("to", (String) o[fConceptPos.apply(conceptNodeId)]).build();
			this.edges.add(edge);
		}
	}

	/**
	 * version 2 of build Graph with addition info: nodeSet and lstConcept
	 * 
	 * @param connections
	 *            full list of connection
	 * @param sentencesMap
	 *            map sentence id and content
	 * @param nodeSet
	 *            all node can be get to build graph (can be less than full node in
	 *            lstConcept)
	 * @param lstConcept
	 *            full list of concept
	 */
	private void buildGraphV2(List<Object[]> connections, Map<String, String> sentencesMap, Set<String> nodeSet,
			List<List<String>> lstConcept) {

		System.out.println("test node set " + nodeSet.toString());
		System.out.println("test len: " + connections.get(0).length + " -- " + connections.get(1).length);

		List<String> sentIdNodes = nodeSet.stream().filter(o -> null != sentencesMap.get(o))
				.collect(Collectors.toList());

		Set<String> conceptValueNodes = nodeSet.stream().filter(o -> null == sentencesMap.get(o))
				.collect(Collectors.toSet());

		Set<String> nodeIdSet = new HashSet<>();

		Set<String> sentIdSet = new HashSet<String>(sentIdNodes);
		String title_pre = "<div class=\"graphPopup\"><article>";
		String title_post = "</article></div>";

		// sentence node
		for (String o : sentIdSet) {
			String title = o;
			title = "LinkID: " + o;
			if (sentencesMap != null) {
				title = "<p>LinkID: " + o + "</p>";
				title += "<p>DisplayName: " + sentencesMap.get(o) + "</p>";

				this.metaInfo.add(o, Json.createObjectBuilder().add("title", title).add("isSentence", true).build());
			}

			nodes.add(Json.createObjectBuilder().add("id", o).add("label", "").add("group", o)
					.add("title", title_pre + title + title_post).build());

			nodeIdSet.add(title);
		}

		// concept node
		final int nodeNamePos = fConceptPos.apply(Constants.NODE_NAME_HEADER_NAME);
		System.out.println("conceptValueNodes: " + conceptValueNodes.toString());
		System.out.println("lstConcept: " + lstConcept.toString());

		List<List<String>> lstConceptFilter = lstConcept.stream()
				.filter(o -> conceptValueNodes.contains(o.get(nodeNamePos))).collect(Collectors.toList());
		System.out.println("test" + lstConceptFilter.size());
		for (List<String> o : lstConceptFilter) {
			if (!nodeIdSet.contains(o.get(nodeNamePos))) {
				String title = "";
				title = "<p>NodeName: " + o.get(nodeNamePos) + "</p>";
				title += "<p>ObjectIndex: " + o.get(fConceptPos.apply(Constants.OBJECT_INDEX_HEADER_NAME)) + "</p>";

				title += "<p>DislayName: " + o.get(fConceptPos.apply(Constants.DISPLAY_NAME_HEADER_NAME)) + "</p>";
				title += "<p>LinkID: "
						+ Utils.extractSentenceIndex(o.get(fConceptPos.apply(Constants.NODE_ID_HEADER_NAME))) + "</p>";

				BiFunction<String, String, String> fListGetDefault = (pos, defVal) -> {
					try {
						return o.get(fConceptPos.apply(pos)).toLowerCase();
					} catch (Exception e) {
						return defVal;
					}
				};

				String nodeShape = fListGetDefault.apply(Constants.NODE_SHAPE, "");
				String nodeFillColor = fListGetDefault.apply(Constants.NODE_FILL_COLOR, "");
				String nodeOutlineColor = fListGetDefault.apply(Constants.NODE_OUTLINE_COLOR, "");

				// String color =
				// o.get(fConceptPos.apply(Constants.NODE_FILL_COLOR)).toLowerCase();
				// link color

				JsonObject scolor = Json.createObjectBuilder().add("background", nodeFillColor)
						.add("border", nodeOutlineColor).build();

				nodes.add(Json.createObjectBuilder().add("id", o.get(nodeNamePos)).add("label", o.get(nodeNamePos))
						.add("title", title_pre + title + title_post).add("color", scolor).add("shape", nodeShape)
						.build());
				// .add("group", o.get(0)) conflict with color
				// shape must in list

				this.metaInfo.add(o.get(nodeNamePos),
						Json.createObjectBuilder().add("title", title).add("isSentence", true).build());

				nodeIdSet.add(o.get(nodeNamePos));
			}
		}

		int collLinkPos = fCollectionPos.apply(Constants.COLL_LINK_ID);
		int collSourcePos = fCollectionPos.apply(Constants.COLL_SOURCE_OJBECT);
		Set<String> setEdge = new HashSet<>();
		for (Object[] o : connections) {
			String x1 = o[collLinkPos].toString() + ":::" + o[collSourcePos].toString();
			String x2 = o[collSourcePos].toString() + ":::" + o[collLinkPos].toString();

			if (setEdge.contains(x1) || setEdge.contains(x2))
				continue;
			setEdge.add(x1);

			JsonObject edge = Json.createObjectBuilder().add("from", (String) o[collLinkPos])
					.add("to", (String) o[collSourcePos]).build();
			this.edges.add(edge);
		}
	}

	/**
	 * return a JsonObject which content information of nodes and edges and metaInfo
	 */
	public String getJsonVisjsFormat() {
		JsonObject ret = Json.createObjectBuilder().add("nodes", this.nodes.build()).add("edges", this.edges.build())
				.add("metaInfo", this.metaInfo.build()).build();
		return ret.toString();
	}

}
