package graph;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.json.JsonObject;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import edu.stanford.nlp.ling.TaggedWord;
import webservice.Constants;

/**
 * Implement content graph, it manage nodes and edges for easy to draw later
 * (currently use Visjs)
 * 
 * @author thanhhungqb
 *
 */
public class ContentGraph implements IGraph {
	private Set<JSONObject> nodes = new HashSet<JSONObject>();
	private Set<JSONObject> edges = new HashSet<JSONObject>();
	private JSONObject metaInfo = new JSONObject();

	public ContentGraph() {
	}

	/**
	 * If no sentence map
	 * 
	 * @param lobj
	 */
	public ContentGraph(List<Object[]> lobj) {
		buildGraph(lobj, null);
	}

	/**
	 * Con
	 * 
	 * @param lobj
	 */
	public ContentGraph(List<Object[]> lobj, Map<String, List<TaggedWord>> sentencesMap) {
		buildGraph(lobj, sentencesMap);
	}

	private void buildGraph(List<Object[]> lobj, Map<String, List<TaggedWord>> sentencesMap) {

		List<String> sentIdNodes = lobj.stream().map(o -> Utils.extractSentenceIndex((String) o[fConceptPos.apply(Constants.NODE_ID_HEADER_NAME)]))
				.collect(Collectors.toList());
		this.nodes = new HashSet<>();

		Set<String> nodeIdSet = new HashSet<>();

		Set<String> sentIdSet = new HashSet<String>(sentIdNodes);
		for (String o : sentIdSet) {
			String title = o;
			if (sentencesMap != null) {
				title = title + ": "
						+ sentencesMap.get(o).stream().map(o1 -> o1.value()).collect(Collectors.joining(" "));

				this.metaInfo.put(o, title);
			}
			nodes.add(Utils.create_Node(o, o, o, null));
			nodeIdSet.add(title);
		}

		for (Object[] o : lobj) {
			String key = (String) o[1];
			String docId = (String) o[fConceptPos.apply(Constants.IGNORED_CONCEPTS_FILE_DEFAULT)];

			if (!nodeIdSet.contains(key)) {
				nodeIdSet.add(key);

				String title = (String) o[0];
				if (sentencesMap != null) {
					title = title + ": "
							+ sentencesMap.get(docId).stream().map(o1 -> o1.value()).collect(Collectors.joining(" "));

					this.metaInfo.put(key, title);
				}

				// create new node if not found
				nodes.add(Utils.create_Node(key, key, docId, null));
			}
		}

		this.edges = lobj
				.stream().map(o -> Utils.createEdge((String) o[1],
						Utils.extractSentenceIndex((String) o[fConceptPos.apply(Constants.NODE_ID_HEADER_NAME)]), null))
				.collect(Collectors.toSet());
	}

	@SuppressWarnings("unchecked")
	@Override
	public String getJsonVisjsFormat() {

		JSONObject ret = new JSONObject();

		JSONArray nodejson = new JSONArray();
		nodejson.addAll(this.nodes);
		ret.put("nodes", nodejson);

		JSONArray edgesjson = new JSONArray();
		edgesjson.addAll(this.edges);
		ret.put("edges", edgesjson);

		ret.put("metaInfo", this.metaInfo);

		return ret.toJSONString();
	}

	// protected String getDocID(String st) {
	// int p = st.lastIndexOf(".");
	// if (p > 0) {
	// return st.substring(0, p);
	// } else
	// return st;
	// }

	protected final String conceptName = Constants.NODE_NAME_HEADER_NAME; // "NodeName"
	protected final String conceptIndex = Constants.OBJECT_INDEX_HEADER_NAME; // "ObjectIndex";
	protected final String conceptNodeId = Constants.NODE_ID_HEADER_NAME; // "NodeID";

	protected final Function<String, Integer> fConceptPos = headerName -> Arrays.asList(Constants.CONCEPT_HEADER)
			.indexOf(headerName);

	protected final Function<String, Integer> fCollectionPos = headerName -> Arrays.asList(Constants.COLLECTION_HEADER)
			.indexOf(headerName);

	@Override
	public JsonObject getJsonVisjsObj() {
		// TODO Auto-generated method stub
		return null;
	}
}
