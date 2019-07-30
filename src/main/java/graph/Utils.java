package graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;

import org.json.simple.JSONObject;

public class Utils {
	public static JSONObject create_Node(int id, String label, String group, String title) {
		JSONObject obj = new JSONObject();
		obj.put("id", id);
		obj.put("label", label);
		obj.put("group", group);
		obj.put("title", title);
		return obj;
	}

	/**
	 * Create VisJs node, id is string
	 * 
	 * @param id
	 * @param label
	 * @param group
	 * @param title
	 * @return
	 */
	public static JSONObject create_Node(String id, String label, String group, String title) {
		JSONObject obj = new JSONObject();
		obj.put("id", id);
		obj.put("label", label);

		if (null != group)
			obj.put("group", group);
		if (null != title)
			obj.put("title", title);
		return obj;
	}

	/**
	 * Create edge
	 * 
	 * @param from
	 * @param to
	 * @param val
	 * @return
	 */
	public static JSONObject createEdge(String from, String to, Integer val) {
		JSONObject obj = new JSONObject();
		obj.put("from", from);
		obj.put("to", to);

		if (null != val)
			obj.put("val", val);

		return obj;
	}

	/**
	 * Extract sentence Index from objectIndex
	 * 
	 * @param objectIndex: format x.x.x....x.k
	 * @return x.x.x....x.0 (replace latest k by 0)
	 */
	public static String extractSentenceIndex(String objectIndex) {

		int pos = objectIndex.lastIndexOf('.');

		// when empty string or wrong format, return orignal
		if (pos < 0)
			return objectIndex;

		String out = objectIndex.substring(0, pos) + ".0";
		return out;
	}

	public static List<String> graphCutByDegree(List<String> nodes, List<List<String>> edges, int numberKeep) {
		Map<String, Set<String>> next_node = new HashMap<>();
		for (String node : nodes)
			next_node.put(node, new HashSet<>());

		for (List<String> edge : edges) {
			String u = edge.get(0);
			String v = edge.get(1);

			next_node.get(u).add(v);
			next_node.get(v).add(u);
		}

		OptionalInt tmp = nodes.stream().map(o -> next_node.get(o).size()).mapToInt(Integer::intValue).max();
		int max_val = tmp.orElse(0);

		List<String> selectedNodes = new LinkedList<>();
		Queue<GrEntry> queue = new PriorityBlockingQueue<GrEntry>();

		for (String node : nodes) {
			if (next_node.get(node).size() == max_val) {
				queue.add(new GrEntry(node, max_val));
			}
		}
		Set<String> markNode = new HashSet<>(selectedNodes);

		while (queue.size() > 0 && selectedNodes.size() < numberKeep) {
			GrEntry n = queue.poll();
			selectedNodes.add(n.node);
			for (String n_n : next_node.get(n.node)) {
				if (!markNode.contains(n_n)) {
					markNode.add(n_n);
					queue.add(new GrEntry(n_n, next_node.get(n_n).size()));
				}
			}
		}

		return selectedNodes;
	}
}

class GrEntry implements Comparable<GrEntry> {

	final String node;
	final int degree;

	public GrEntry(String node, int degree) {
		this.node = node;
		this.degree = degree;
	}

	@Override
	public int compareTo(GrEntry o) {
		if (this.degree == o.degree)
			return 0;

		return this.degree < o.degree ? 1 : -1;
	}

}
