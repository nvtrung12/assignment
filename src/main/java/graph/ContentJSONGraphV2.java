package graph;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.apache.log4j.Logger;

import webservice.Constants;

/**
 * new version with simply parameters
 */
public class ContentJSONGraphV2 extends ContentGraph {

	final Logger logger = Logger.getLogger(this.getClass());
	JsonArrayBuilder nodes = Json.createArrayBuilder();
	JsonArrayBuilder edges = Json.createArrayBuilder();
	JsonObjectBuilder metaInfo = Json.createObjectBuilder();
	Map<String, List<String>> mapSentIdMerge = null;

	/**
	 * build full graph with concepts and connections
	 * 
	 * @param concepts: table, header removed
	 * @param connections: table, header removed
	 */
	public ContentJSONGraphV2(boolean isCall ,List<List<String>> concepts, List<List<String>> connections) {
		final Function<String, Integer> fConceptPos = hname -> Arrays.asList(Constants.CONCEPT_HEADER).indexOf(hname);
		int nodeIdPos = fConceptPos.apply(Constants.NODE_ID_HEADER_NAME);
		int nodeNamePos = fConceptPos.apply(Constants.NODE_NAME_HEADER_NAME);
		int displayNamePos = fConceptPos.apply(Constants.DISPLAY_NAME_HEADER_NAME);

		List<String> nodeList = concepts.stream()
				.map(o -> "NULL".equals(o.get(nodeNamePos)) ? o.get(nodeIdPos) : o.get(nodeNamePos))
				.collect(Collectors.toList());

		Map<String, String> sentMap = concepts.stream().filter(o -> "NULL".equals(o.get(nodeNamePos)))
				.collect(Collectors.toMap(o -> o.get(nodeIdPos), o -> o.get(displayNamePos),
						(dnameOld, dnameNew) -> dnameOld + dnameNew));

		buildGraph(isCall ,concepts, connections, sentMap, new HashSet<>(nodeList));
	}

	/**
	 * build full graph with concepts and connections with merge Sentence
	 * 
	 * @param concepts: table, header removed
	 * @param connections: table, header removed
	 */
	public ContentJSONGraphV2(List<List<String>> concepts, List<List<String>> connections, float threshold) {
		final Function<String, Integer> fConceptPos = hname -> Arrays.asList(Constants.CONCEPT_HEADER).indexOf(hname);
		int nodeIdPos = fConceptPos.apply(Constants.NODE_ID_HEADER_NAME);
		int nodeNamePos = fConceptPos.apply(Constants.NODE_NAME_HEADER_NAME);
		int displayNamePos = fConceptPos.apply(Constants.DISPLAY_NAME_HEADER_NAME);

		List<String> nodeList = concepts.stream()
				.map(o -> "NULL".equals(o.get(nodeNamePos)) ? o.get(nodeIdPos) : o.get(nodeNamePos))
				.collect(Collectors.toList());

		Map<String, String> sentMap = concepts.stream().filter(o -> "NULL".equals(o.get(nodeNamePos)))
				.collect(Collectors.toMap(o -> o.get(nodeIdPos), o -> o.get(displayNamePos)));

		// find the merge map
		Map<String, String> merge = this.findMergeSentence(connections, threshold);

		// build with merge
		mergeAndbuild(concepts, connections, sentMap, new HashSet<>(nodeList), merge);
	}

	/**
	 * Can be limit the node in graph by nodeSet
	 * 
	 * @param concepts: table header removed
	 * @param connections: table header removed
	 * @param sentencesMap: sentence id => sentence content
	 * @param nodeSet: if not full sentence id and concept values than can limit
	 *        graph (not full nodes and edges)
	 */
	public ContentJSONGraphV2(boolean isCall ,List<List<String>> concepts, List<List<String>> connections,
			Map<String, String> sentencesMap, Set<String> nodeSet) {
		buildGraph(isCall ,concepts, connections, sentencesMap, nodeSet);
	}

	/**
	 * build Graph with addition info: nodeSet and lstConcept <br/>
	 * 
	 * TODO concept connect to many sentence => multi LinkID and DislayName (each
	 * sentence)
	 * 
	 * @param connections  full list of connection
	 * @param sentencesMap map sentence id and content
	 * @param nodeSet      all node can be get to build graph (can be less than full
	 *                     node in lstConcept) including sentence node (id) and
	 *                     concept node (value)
	 * @param concepts     full list of concept
	 */
	private void buildGraph(boolean isCall ,List<List<String>> concepts, List<List<String>> connections,
			Map<String, String> sentencesMap, Set<String> nodeSet) {

		int collLinkPos = fCollectionPos.apply(isCall ? Constants.SOURCE_OBJECT_INDEX : Constants.COLL_LINK_ID);
		int collSourcePos = isCall ? fCollectionPos.apply(Constants.SINK_OBJECT_INDEX) :fCollectionPos.apply(Constants.COLL_SOURCE_OJBECT);
		logger.debug("test node set: " + nodeSet.toString());

		buildNodes(isCall, concepts, connections, sentencesMap, nodeSet);

		// keep set edge, if duplication then not re-add
		// build edges
		Set<String> setEdge = new HashSet<>();
		for (List<String> o : connections) {
			String x1 = o.get(collLinkPos) + ":::" + o.get(collSourcePos);
			String x2 = o.get(collSourcePos) + ":::" + o.get(collLinkPos);

			if (setEdge.contains(x1) || setEdge.contains(x2))
				continue;
			setEdge.add(x1);

			JsonObject edge = Json.createObjectBuilder().add("from", o.get(collLinkPos)).add("to", o.get(collSourcePos))
					.build();
			this.edges.add(edge);
		}
		
	}

	/**
	 * build graph with merge sentence
	 * 
	 * @param concepts
	 * @param connections
	 * @param sentencesMap
	 * @param nodeSet
	 * @param mergeSentenceMap
	 */
	private void mergeAndbuild(List<List<String>> concepts, List<List<String>> connections,
			Map<String, String> sentencesMap, Set<String> nodeSet, Map<String, String> mergeSentenceMap) {

		int collLinkPos = fCollectionPos.apply(Constants.COLL_LINK_ID);
		int collSourcePos = fCollectionPos.apply(Constants.COLL_SOURCE_OJBECT);

		logger.debug("test node set: " + nodeSet.toString());
		logger.debug(mergeSentenceMap);
		Set<String> newNodeSet = new HashSet<>(nodeSet);
		// not use sentence node that refer to another
		Set<String> removed = mergeSentenceMap.keySet().stream().filter(o -> !o.equals(mergeSentenceMap.get(o)))
				.collect(Collectors.toSet());
		newNodeSet.removeAll(removed);

		// collect merge sentence node
		this.mapSentIdMerge = new HashMap<>();
		for (String v : mergeSentenceMap.keySet()) {
			String k = mergeSentenceMap.get(v);
			if (this.mapSentIdMerge.get(k) == null)
				this.mapSentIdMerge.put(k, new LinkedList<>());

			this.mapSentIdMerge.get(k).add(v);
		}

		buildNodes(false,concepts, connections, sentencesMap, newNodeSet);

		// keep set edge, if duplication then not re-add
		// build edges
		Set<String> setEdge = new HashSet<>();
		for (List<String> o : connections) {
			String sentid = o.get(collLinkPos);
			sentid = mergeSentenceMap.get(sentid); // get root of merge
			String x1 = sentid + ":::" + o.get(collSourcePos);
			String x2 = o.get(collSourcePos) + ":::" + sentid;

			if (setEdge.contains(x1) || setEdge.contains(x2))
				continue;
			setEdge.add(x1);

			JsonObject edge = Json.createObjectBuilder().add("from", sentid).add("to", o.get(collSourcePos)).build();
			this.edges.add(edge);
		}
	}

	/**
	 * Local build nodes graph
	 * 
	 * @param concepts
	 * @param connections
	 * @param sentencesMap
	 * @param nodeSet
	 */
	private void buildNodes(boolean isCall , List<List<String>> concepts, List<List<String>> connections,
			Map<String, String> sentencesMap, Set<String> nodeSet) {

		int collLinkPos = isCall?fCollectionPos.apply(Constants.SOURCE_OBJECT_INDEX):fCollectionPos.apply(Constants.COLL_LINK_ID);
		int collSourcePos = isCall ? fCollectionPos.apply(Constants.SINK_OBJECT_INDEX) :fCollectionPos.apply(Constants.COLL_SOURCE_OJBECT);
		//int collSourcePos = fCollectionPos.apply(Constants.COLL_SOURCE_OJBECT);

		// separated sentence node and concept node
		List<String> sentIdNodes = nodeSet.stream().filter(o -> null != sentencesMap.get(o))
				.collect(Collectors.toList());

		Set<String> conceptValueNodes = nodeSet.stream().filter(o -> null == sentencesMap.get(o))
				.collect(Collectors.toSet());

		Set<String> nodeIdSet = new HashSet<>();

		// each concept value contains list of sentid that it connect
		// to show multi-linkid
		Map<String, List<String>> concept2Sents = new HashMap<>();
		for (List<String> connection : connections) {
			String conceptVal = connection.get(collSourcePos);
			String sentId = connection.get(collLinkPos);

			if (concept2Sents.get(conceptVal) == null)
				concept2Sents.put(conceptVal, new LinkedList<>());
			concept2Sents.get(conceptVal).add(sentId);
		}

		Set<String> sentIdSet = new HashSet<String>(sentIdNodes);
		String titlePre = "<div class=\"graphPopup\"><article>";
		String titlePost = "</article></div>";

		// sentence node
		for (String o : sentIdSet) {
			String title = o;
			title = "LinkID: " + o;
			if (sentencesMap != null) {
				title = "<p>LinkID: " + o + "</p>";
				title += "<p>DisplayName: " + sentencesMap.get(o) + "</p>";

				if (this.mapSentIdMerge != null) {
					List<String> tmp = this.mapSentIdMerge.get(o).stream().filter(oid -> !o.equals(oid)).map(
							oid -> String.format("<p>LinkID: %s</p><p>DisplayName: %s</p>", oid, sentencesMap.get(oid)))
							.collect(Collectors.toList());
					title += String.join("\n", tmp);
				}

				this.metaInfo.add(o, Json.createObjectBuilder().add("title", title).add("isSentence", true).build());
			}

			nodes.add(Json.createObjectBuilder().add("id", o).add("label", "").add("group", o)
					.add("title", titlePre + title + titlePost).build());

			nodeIdSet.add(title);
		}

		// concept node
		final int nodeNamePos = fConceptPos.apply(Constants.NODE_NAME_HEADER_NAME);
		final int nodeIdPos = fConceptPos.apply(Constants.NODE_ID_HEADER_NAME);

		List<List<String>> lstConceptFilter = concepts.stream()
				.filter(o -> conceptValueNodes.contains(o.get(nodeNamePos))).collect(Collectors.toList());

		// each concept may from multi-position, than it has many id
		Map<String, List<String>> concept2ids = new HashMap<>();
		Map<String, List<List<String>>> test = lstConceptFilter.stream()
				.collect(Collectors.groupingBy(o -> o.get(nodeNamePos)));
		for (String concept : test.keySet()) {
			concept2ids.put(concept,
					test.get(concept).stream().map(o -> o.get(nodeIdPos)).collect(Collectors.toList()));
		}

		logger.debug("lstConceptFilter" + lstConceptFilter.size());

		for (List<String> o : lstConceptFilter) {
			if (!nodeIdSet.contains(o.get(nodeNamePos))) {
				String title = "";
				title = "<p>NodeName: " + o.get(nodeNamePos) + "</p>";
				// title += "<p>ObjectIndex: " +
				// o.get(fConceptPos.apply(Constants.OBJECT_INDEX_HEADER_NAME)) + "</p>";
				title += "<p>ObjectIndex: " + String.join(" | ", concept2ids.get(o.get(nodeNamePos))) + "</p>";

				// get all sentences has this concept
				if(concept2Sents != null && concept2Sents.containsKey(o.get(nodeNamePos))) {
					for (String connectSentId : concept2Sents.get(o.get(nodeNamePos))) {
						if (null == sentencesMap.get(connectSentId))
							continue; // omit null (maybe because filter those sentences)

						title += "<p>DislayName: " + sentencesMap.get(connectSentId) + "</p>";
						title += "<p>LinkID: " + connectSentId + "</p>";
					}
				}

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

				JsonObject scolor = Json.createObjectBuilder().add("background", nodeFillColor)
						.add("border", nodeOutlineColor).build();

				// color, shape must in list
				if(isCall) {
					nodes.add(Json.createObjectBuilder().add("id", o.get(nodeIdPos)).add("label", o.get(nodeNamePos))
							.add("title", titlePre + title + titlePost).add("color", scolor).add("shape", nodeShape)
							.build());

				}else {
					nodes.add(Json.createObjectBuilder().add("id", o.get(nodeNamePos)).add("label", o.get(nodeNamePos))
							.add("title", titlePre + title + titlePost).add("color", scolor).add("shape", nodeShape)
							.build());

				}
				
				this.metaInfo.add(o.get(nodeNamePos),
						Json.createObjectBuilder().add("title", title).add("isSentence", true).build());

				nodeIdSet.add(o.get(nodeNamePos));
			}
		}
		// finish build nodes
	}

	/**
	 * Find merge sentence
	 * 
	 * TODO big problem a merge with b, b merge with c but a not merge with c
	 * 
	 * @param connections
	 * @param             threshold: two sentence same concepts above this value
	 *                    then merge, 0->1
	 * @return oldSentID => mergedSentID (one of old)
	 */
	protected Map<String, String> findMergeSentence(List<List<String>> connections, float threshold) {
		Map<String, String> ret = new HashMap<>();

		int collLinkPos = fCollectionPos.apply(Constants.COLL_LINK_ID);
		int collSourcePos = fCollectionPos.apply(Constants.COLL_SOURCE_OJBECT);

		Map<String, Set<String>> sent2setConcept = new HashMap<>();
		for (List<String> connection : connections) {
			String sentid = connection.get(collLinkPos);
			String conceptVal = connection.get(collSourcePos);

			if (sent2setConcept.get(sentid) == null)
				sent2setConcept.put(sentid, new HashSet<>());

			sent2setConcept.get(sentid).add(conceptVal);
		}

		List<String> sentids = sent2setConcept.keySet().stream().collect(Collectors.toList());
		int len = sentids.size();
		for (int pos1 = 0; pos1 < len; ++pos1) {
			for (int pos2 = pos1 + 1; pos2 < len; ++pos2) {
				// check if above threshold
				Set<String> d = new HashSet<String>(sent2setConcept.get(sentids.get(pos1)));
				d.retainAll(sent2setConcept.get(sentids.get(pos2)));

				// compare to max length of two sentence
				if (d.size() >= threshold * Math.max(sent2setConcept.get(sentids.get(pos1)).size(),
						sent2setConcept.get(sentids.get(pos2)).size())) {
					// merger smaller to larger
					if (sent2setConcept.get(sentids.get(pos2)).size() < sent2setConcept.get(sentids.get(pos1)).size())
						// merge 2 to 1
						ret.put(sentids.get(pos2), sentids.get(pos1));
					else
						ret.put(sentids.get(pos1), sentids.get(pos2));
				}
			}
		}

		// check chain merge id1 <- id2 <- id3 => id1 <- id3 too
		while (true) {
			boolean isHave = false;
			for (String id3 : ret.keySet()) {
				String id2 = ret.get(id3);
				String id1 = ret.get(id2);
				if (id1 != null) { // id2 has map
					isHave = true;
					ret.put(id3, id1);
				}
			}

			if (!isHave)
				break;
		}

		// set point to itself
		for (String sid : sent2setConcept.keySet()) {
			if (ret.get(sid) == null)
				ret.put(sid, sid);
		}

		return ret;
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
