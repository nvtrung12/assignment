package distance;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;

public class ShortestPath {
	public static void main(String[] args) {

		List<String> nodes = new LinkedList<>();
		List<List<String>> edges = new LinkedList<>();
		int N = 1000;
		for (int i = 0; i < N; ++i) {
			nodes.add("" + i);
			List<String> edge = new LinkedList<>();
			edge.add("" + i);
			edge.add("" + (i + 1));
			if (i < N - 1)
				edges.add(edge);
		}
		long stime = System.currentTimeMillis();
		int out = shortestPath(nodes, edges);
		System.out.println("max len: " + out);
		long etime = System.currentTimeMillis();
		System.out.println("Run time: " + (etime - stime));
	}

	/**
	 * 
	 * @return
	 */
	protected static int shortestPath(List<String> nodes, List<List<String>> edges) {
		Map<Integer, List<Integer>> d = new HashMap<>();
		Map<String, Integer> node2id = new HashMap<>();
		Map<Integer, String> id2node = new HashMap<>();
		List<Integer> ids = new LinkedList<>();

		final int MAX = 10000;

		SparkConf sparkConf = new SparkConf().setAppName("hungtest").setMaster("local[*]");
		JavaSparkContext sc = new JavaSparkContext(sparkConf);

		for (int i = 0; i < nodes.size(); ++i) {
			node2id.put(nodes.get(i), i);
			id2node.put(i, nodes.get(i));
			ids.add(i);
		}

		for (int nodeId : ids) {
			d.put(nodeId, new LinkedList<>());
			for (int idx : ids) {
				d.get(nodeId).add(idx != nodeId ? MAX : 0);
			}
		}

		for (List<String> edge : edges) {
			int u = node2id.get(edge.get(0));
			int v = node2id.get(edge.get(1));

			d.get(u).set(v, 1);
			d.get(v).set(u, 1);
		}

		for (int k : ids) {
			List<List<Integer>> nu = sc.parallelize(ids).map(u -> {
				List<Integer> out = new LinkedList<>();
				for (int v : ids) {
					out.add(Math.min(d.get(u).get(v), d.get(u).get(k) + d.get(k).get(v)));
				}
				return out;
			}).collect();
			for (int u : ids) {
				d.put(u, nu.get(u));
			}
		}

		int kmax = 0;

		for (int u : ids) {
			for (int v : ids)
				if (kmax < d.get(u).get(v))
					kmax = d.get(u).get(v);
		}
		sc.close();

		return kmax;
	}

	protected static int shortestPath2(List<String> nodes, List<List<String>> edges) {
		Map<Integer, List<Integer>> d = new HashMap<>();
		Map<String, Integer> node2id = new HashMap<>();
		Map<Integer, String> id2node = new HashMap<>();
		List<Integer> ids = new LinkedList<>();

		final int MAX = 10000;

		for (int i = 0; i < nodes.size(); ++i) {
			node2id.put(nodes.get(i), i);
			id2node.put(i, nodes.get(i));
			ids.add(i);
		}

		for (int nodeId : ids) {
			d.put(nodeId, new LinkedList<>());
			for (int idx : ids) {
				d.get(nodeId).add(idx != nodeId ? MAX : 0);
			}
		}

		for (List<String> edge : edges) {
			int u = node2id.get(edge.get(0));
			int v = node2id.get(edge.get(1));

			d.get(u).set(v, 1);
			d.get(v).set(u, 1);
		}

		for (int k : ids) {
			for (int u : ids) {
				for (int v : ids) {
					d.get(u).set(v, Math.min(d.get(u).get(v), d.get(u).get(k) + d.get(k).get(v)));
				}
			}
		}

		int kmax = 0;
		for (int u : ids) {
			for (int v : ids)
				if (kmax < d.get(u).get(v))
					kmax = d.get(u).get(v);
		}

		return kmax;
	}
}
