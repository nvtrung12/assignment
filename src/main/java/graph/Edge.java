package graph;

import java.util.Map;

import org.json.simple.JSONObject;

public class Edge {
	private String from;
	private String to;
	private double val;

	public Edge() {

	}

	public Edge(String from, String to) {
		madeEdge(from, to, 0.0);
	}

	public Edge(String from, String to, double val) {
		madeEdge(from, to, val);
	}

	private void madeEdge(String from, String to, double val) {
		this.from = from;
		this.to = to;
		this.val = val;
	}

	/**
	 * 
	 * @param nodeMap
	 * @return VisJs json present edge, node id is Int
	 */
	public JSONObject getJsonVisjsFormat(Map<String, Integer> nodeMap) {
		JSONObject obj = new JSONObject();
		obj.put("from", nodeMap.get(this.from));
		obj.put("to", nodeMap.get(this.to));

		return obj;
	}

	/**
	 * 
	 * @return VisJs json present edge, node id is string
	 */
	public JSONObject getJsonVisjsFormat() {
		JSONObject obj = new JSONObject();
		obj.put("from", this.from);
		obj.put("to", this.to);

		return obj;

	}

}