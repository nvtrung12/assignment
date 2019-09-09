package graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.json.JsonObject;

import org.apache.commons.collections4.map.HashedMap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Implement simple graph
 * 
 * @author thanhhungqb
 *
 */
public class SimpleGraph implements IGraph {
	private Set<String> nodes = new HashSet<>();
	private Map<String, String> node_groups = new HashMap<>();
	private List<Edge> edges = new LinkedList<>();

	public SimpleGraph() {
	}

	public SimpleGraph(List<Object[]> lobj) {

		List<String> sentIdNodes = lobj.stream().map(o -> getDocID((String) o[0])).collect(Collectors.toList());
		List<String> phraseNodes = lobj.stream().map(o -> (String) o[1]).collect(Collectors.toList());

		nodes = new HashSet<>(sentIdNodes);
		nodes.addAll(phraseNodes);

		// sentId to group sentId
		this.node_groups.putAll((new HashSet<>(sentIdNodes)).stream().collect(Collectors.toMap(o -> o, o -> o)));

		// set phrase to group of sentId
		for (Object[] o : lobj) {
			this.node_groups.put((String) o[1], getDocID((String) o[0]));
			// if concept in many sentence (then have many DocId) get latest
		}

		List<Edge> sentEdge = lobj.stream().map(o -> new Edge((String) o[1], getDocID((String) o[0])))
				.collect(Collectors.toList());

		edges.addAll(sentEdge);
	}

	@SuppressWarnings("unchecked")
	@Override
	public String getJsonVisjsFormat() {

		JSONObject ret = new JSONObject();
		HashedMap<String, Integer> nodeMap = new HashedMap<String, Integer>();
		int idCount = 1;
		for (String node : this.nodes)
			nodeMap.put(node, idCount++);

		JSONArray nodejson = new JSONArray();
		for (String lbl : nodeMap.keySet()) {
			JSONObject obj = new JSONObject();
			obj.put("id", nodeMap.get(lbl));
			obj.put("label", lbl);
			obj.put("title", "title of node"); // TODO
			obj.put("group", this.node_groups.get(lbl));

			nodejson.add(obj);
		}
		ret.put("nodes", nodejson);

		JSONArray edgesjson = new JSONArray();

		for (Edge edge : this.edges) {
			edgesjson.add(edge.getJsonVisjsFormat(nodeMap));
		}

		ret.put("edges", edgesjson);

		return ret.toJSONString();
	}

	private String getDocID(String st) {
		int p = st.lastIndexOf(".");
		if (p > 0) {
			return st.substring(0, p);
		} else
			return st;
	}

	@Override
	public JsonObject getJsonVisjsObj() {
		// TODO Auto-generated method stub
		return null;
	}
}
