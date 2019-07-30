package simple;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Level;

import bayesian.BayesianUtils;
import bayesian.QCBayesianNetwork;
import static bayesian.QCBayesianNetwork.*;

public class TestBayesianNetwork1 {
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {

		final String fileName = "tmp/inputtest1.xlsx";
		final String sheetName = "sheet1";

		InputStream is = new FileInputStream(fileName);
		Map<String, Object> out = BayesianUtils.loadSimpleNetworkFormat(is, sheetName);

		Map<String, Set<String>> outNodes = (Map<String, Set<String>>) out.get("graph");
		Map<String, Set<String>> inNodes = (Map<String, Set<String>>) out.get("graphToMe");

		Set<String> qs = new HashSet<>(Arrays.asList("Q1", "Q2"));

		Set<String> cs = new HashSet<>(Arrays.asList("C1", "C2", "C3", "C4", "C5"));

		// a k m n
		List params = Arrays.asList("a", "k", "m", "n", 0.716981132, 0.6, 0.22, 0.2);
		params = Arrays.asList("a", "k", "m", "n", 0.6, 0.9, 0.01, 0.2);
		params = Arrays.asList("a", "k", "m", "n", 0.6, 0.4, 0.22, 0.2);
		params = Arrays.asList("a", "k", "m", "n", 0.6, 0.3, 0.22, 0.2);
		params = Arrays.asList("a", "k", "m", "n", 0.6, 0.5, 0.4, 0.2);
		params = Arrays.asList("a", "k", "m", "n", 0.6, 0.5, 0.22, 0.2);
		params = Arrays.asList("a", "k", "m", "n", 0.6, 0.5, 0.1, 0.2);
		params = Arrays.asList("a", "k", "m", "n", 0.6, 0.5, 0.22, 0.2);
		params = Arrays.asList("a", "k", "m", "n", 0.6, 0.5, 0.7, 0.2);

		Map<String, Object> kwargs = new HashMap<>();
		int x2 = params.size() / 2;
		for (int i = 0; i < params.size() / 2; ++i)
			kwargs.put((String) params.get(i), params.get(x2 + i));

		QCBayesianNetwork qcbn = new QCBayesianNetwork(outNodes, inNodes, qs, cs, kwargs);
		qcbn.logger.setLevel(Level.OFF);

		List<Set<String>> rs = Arrays.asList(toSet("Q1", "Q2"), toSet("-Q1", "Q2"), toSet("Q1", "-Q2"),
				toSet("-Q1", "-Q2"));
//		rs = Arrays.asList(toSet("Q1", "Q2"));
		// r1 = Arrays.asList(toSet("C2"));

		// put(getNeg("C1"), "Q1", sqrt(l));
		// put(getNeg("C2"), "Q1", sqrt(l));

		// put(getNeg("C2"), "Q2", l);
//		rs = Arrays.asList(toSet("Q1"), toSet("Q2"));

		System.out.println(kwargs);
		System.out.println();
		for (String ss : Arrays.asList("C1", "C2", "C3", "C4", "C5")) {
//		for (String ss : Arrays.asList("C4", "C5")) {
//		for (String ss : Arrays.asList("-C1", "-C2")) {
			for (Set<String> g : rs)
				try {
					System.out.println(String.format("P(%s|%s)=%s ", ss, g, qcbn.p(ss, g)));
				} catch (Exception e) {
					System.out.println(e);
					throw e;
				}
		}

		System.out.println();
		System.out.println(String.format("l* = P(%s|%s)=%s ", "-C1", "Q1", qcbn.p("-C1", toSet("Q1"))));
		System.out.println(String.format("l* = P(%s|%s)=%s ", "-C2", "Q1", qcbn.p("-C2", toSet("Q1"))));
		System.out.println(String.format("l* = P(%s|%s)=%s ", "-C2", "Q2", qcbn.p("-C2", toSet("Q2"))));
		// qcbn.printTest();

	}
}
