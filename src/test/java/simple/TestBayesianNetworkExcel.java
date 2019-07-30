package simple;

import static bayesian.QCBayesianNetwork.toSet;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Level;

import bayesian.QCBayesianNetwork;

public class TestBayesianNetworkExcel {
	public static void main(String[] args) throws Exception {

		final String fileName = "tmp/inputtest2.xlsx";
		final String sheetName = "Sheet1";

		QCBayesianNetwork qcbn = QCBayesianNetwork.fromExcel(fileName, sheetName);
		qcbn.logger.setLevel(Level.OFF);

		List<Set<String>> rs = Arrays.asList(toSet("Q1", "Q2"), toSet("-Q1", "Q2"), toSet("Q1", "-Q2"),
				toSet("-Q1", "-Q2"));
//		rs = Arrays.asList(toSet("Q1", "Q2"));
		// r1 = Arrays.asList(toSet("C2"));

		// put(getNeg("C1"), "Q1", sqrt(l));
		// put(getNeg("C2"), "Q1", sqrt(l));

		// put(getNeg("C2"), "Q2", l);
//		rs = Arrays.asList(toSet("Q1"), toSet("Q2"));

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
