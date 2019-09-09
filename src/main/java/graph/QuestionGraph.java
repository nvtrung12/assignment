package graph;

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

import org.apache.commons.collections4.map.HashedMap;
import org.apache.log4j.Logger;

import webservice.Constants;

public class QuestionGraph implements IGraph {
	public QuestionGraph(List<List<String>> concepts, List<List<String>> connections, Map<String, String> id2sent,
			Map<String, List<String>> concept2Sents) {
		this.id2sent = id2sent;
		this.concept2Sents = concept2Sents;
		this.buildGraph(concepts, connections);
	}

	private void buildGraph(List<List<String>> concepts, List<List<String>> connections) {
		this.buildNodes(concepts, connections);
		this.buildEdges(concepts, connections);
	}

	private void buildNodes(List<List<String>> concepts, List<List<String>> connections) {
		this.buildConceptNodes(concepts, connections);
		this.buildQuestionNodes(concepts, connections);
	}

	/**
	 * Build concept nodes and add to this.nodes
	 * 
	 * @param concepts
	 * @param connections
	 */
	private void buildConceptNodes(List<List<String>> conceptsP, List<List<String>> connections) {
		final int nodeNamePos = webservice.api.Utils.fConceptPos.apply(Constants.NODE_NAME_HEADER_NAME);
		final int nodeIdPos = webservice.api.Utils.fConceptPos.apply(Constants.NODE_ID_HEADER_NAME);
		Set<String> nodeIdSet = new HashSet<>();

		// a empty concept parent, need improve later
		// ConceptParentCollection x = new ConceptParentCollection();

		// omit sentence node
		List<List<String>> concepts = conceptsP.stream().filter(o -> !("NULL".equals(o.get(nodeNamePos))))
				.collect(Collectors.toList());

		for (List<String> o : concepts) {
			String nodeName = o.get(nodeNamePos);

			if (!nodeIdSet.contains(nodeName)) {
				String title = "";
				title = "<p>NodeName: " + o.get(nodeNamePos) + "</p>";

				// any case that have more than one line that same nodeName?, then merge with |
				title += "<p>ObjectIndex: " + o.get(nodeIdPos) + "</p>";

				// update title get all sentences/question presentation contains this concept
				List<String> tmp = this.concept2Sents.get(nodeName).stream().map(
						sid -> String.format("<p>LinkID: %s</p><p>DisplayName: %s</p>", sid, this.id2sent.get(sid)))
						.collect(Collectors.toList());
				title += String.join("\n", tmp);

				BiFunction<String, String, String> fListGetDefault = (pos, defVal) -> {
					try {
						return o.get(webservice.api.Utils.fConceptPos.apply(pos)).toLowerCase();
					} catch (Exception e) {
						return defVal;
					}
				};

				String nodeShape = fListGetDefault.apply(Constants.NODE_SHAPE, "");
				String nodeFillColor = fListGetDefault.apply(Constants.NODE_FILL_COLOR, "");
				String nodeOutlineColor = fListGetDefault.apply(Constants.NODE_OUTLINE_COLOR, "");

				JsonObject scolor = Json.createObjectBuilder().add("background", nodeFillColor)
						.add("border", nodeOutlineColor).build();

				// color, shape must in list
				this.nodes.add(Json.createObjectBuilder().add("id", nodeName).add("label", nodeName)
						.add("title", titlePre + title + titlePost).add("color", scolor).add("shape", nodeShape)
						.build());

				this.metaInfo.add(nodeName,
						Json.createObjectBuilder().add("title", title).add("isSentence", true).build());

				nodeIdSet.add(nodeName);
			}
		}
	}

	/**
	 * Direct add to this.nodes
	 * 
	 * @param concepts
	 * @param connections
	 */
	private void buildQuestionNodes(List<List<String>> concepts, List<List<String>> connections) {
		// no duplication question id
		List<JsonObject> questionNodes = connections.stream().map(o -> {
			String id = o.get(webservice.api.Utils.fConnectionPos.apply(Constants.COLL_LINK_ID));
			String sourceObj = o.get(webservice.api.Utils.fConnectionPos.apply(Constants.COLL_SOURCE_OJBECT));
			String title = id;
			// more detail about question, pop-up in title
			title = "<p>LinkID: " + id + "</p>";
			title += "<p>DisplayName: " + sourceObj + "</p>";

			JsonObject x = Json.createObjectBuilder().add("id", id).add("label", "").add("group", id)
					.add("title", titlePre + title + titlePost).build();
			return x;
		}).distinct().collect(Collectors.toList());

		// direct add to nodes
		logger.debug("question nodes: " + questionNodes);
		for (JsonObject o : questionNodes)
			this.nodes.add(o);
	}

	private void buildEdges(List<List<String>> concepts, List<List<String>> connections) {
		int collLinkPos = webservice.api.Utils.fConnectionPos.apply(Constants.COLL_LINK_ID);
		final int collSourceObjIdx = webservice.api.Utils.fConnectionPos.apply(Constants.COLL_SOURCE_OBJECT_INDEX);

		final int nodeIdPos = webservice.api.Utils.fConceptPos.apply(Constants.NODE_ID_HEADER_NAME);
		final int nodeNamePos = webservice.api.Utils.fConceptPos.apply(Constants.NODE_NAME_HEADER_NAME);
		final int lPos = webservice.api.Utils.fConnectionPos.apply("OutType");

		Map<String, String> mapConceptId = new HashedMap<>();
		for (List<String> row : concepts) {
			mapConceptId.put(row.get(nodeIdPos), row.get(nodeNamePos));
		}

		Set<String> setEdge = new HashSet<>();
		for (List<String> o : connections) {
			String sentid = o.get(collLinkPos);
			String conceptIdTo = o.get(collSourceObjIdx);
			String conceptTo = mapConceptId.get(conceptIdTo);

			// sentid = mergeSentenceMap.get(sentid); // get root of merge
			String x1 = sentid + ":::" + conceptTo;
			String x2 = conceptTo + ":::" + sentid;

			if (setEdge.contains(x1) || setEdge.contains(x2))
				continue;
			setEdge.add(x1);

			JsonObject edge = Json.createObjectBuilder().add("from", sentid).add("to", conceptTo)
					.add("label", o.get(lPos)).build();
			this.edges.add(edge);
		}
	}

	/**
	 * return a JsonObject which content information of nodes and edges and metaInfo
	 */
	public String getJsonVisjsFormat() {
		return this.getJsonVisjsObj().toString();
	}

	public JsonObject getJsonVisjsObj() {
		JsonObject ret = Json.createObjectBuilder().add("nodes", this.nodes.build()).add("edges", this.edges.build())
				.add("metaInfo", this.metaInfo.build()).build();
		return ret;
	}

	final Logger logger = Logger.getLogger(this.getClass());
	JsonArrayBuilder nodes = Json.createArrayBuilder();
	JsonArrayBuilder edges = Json.createArrayBuilder();
	JsonObjectBuilder metaInfo = Json.createObjectBuilder();
	Map<String, String> id2sent = null;
	Map<String, List<String>> concept2Sents = null;

	String titlePre = "<div class=\"graphPopup\"><article>";
	String titlePost = "</article></div>";

}
