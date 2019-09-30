package simple;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import bayesian.BayesianUtils;
import bayesian.QCBayesianNetwork;
import static bayesian.QCBayesianNetwork.*;

public class TestBayesianNetwork1 {
	
	private static int gt(int n) {
		if (n <2)
			return 1;
		return n * gt(n - 1);
	}
	
	private static String upTextSym(char text) {
		return "-".concat(String.valueOf(text));
	}
	
	private static List<Set<String>> permute(String input) {
		int n = input.length();
		int max = 1 << n;
		input = input.toLowerCase();
		List<Set<String>> rs =new ArrayList<>();
		for (int i = 0; i < max; i++) {
			char combination[] = input.toCharArray();
			String [] array = new String[n];
			Set<String> set =new HashSet<>();
			for (int j = 0; j < n; j++) {
				if (((i >> j) & 1) == 1)
					combination[j] = (char) (combination[j] - 32);
			}
			for (int m = 0; m < combination.length; m++) {
				char x = combination[m];
				if (x == Character.toUpperCase(combination[m])) {
					array[m] = upTextSym(x);
					set.add(upTextSym(x));
				}else {
					array[m] = String.valueOf(Character.toUpperCase(x));
					set.add(String.valueOf(Character.toUpperCase(x)));
				}
			}
			rs.add(set);
			//result.add(combination.toString());
		}
		return rs;
	}
	
	
	public static List<Set<String>> calculatePermutation(String S) {
		List<String> ans = new ArrayList<>();
		dfs(S.toCharArray(), 0, ans);
		//q1q2q3q4
		List<Set<String>> rs =new ArrayList<Set<String>>();
		ans.forEach(q->{
			char [] ch = q.toCharArray();
			Set<String> set =new HashSet<>();
			for (int j = 0; j < ch.length; j++) {
				if (j % 2 != 0) {
					if (ch[j-1] == Character.toUpperCase(ch[j-1])) {
						set.add(upTextSym(ch[j-1]) +String.valueOf(ch[j]));
					}else {
						set.add(String.valueOf(Character.toUpperCase(ch[j - 1])) + ch[j]);
					}
					//set.add(String.valueOf(ch[j-1]+ch[j]));
				}
			}
			rs.add(set);
		});
		
		
		return rs;
	}

	private static void dfs(char[] S, int i, List<String> ans) {
		if (i == S.length) {
			ans.add(new String(S));
			return;
		}
		dfs(S, i + 1, ans);
		if (!Character.isLetter(S[i]))
			return;
		S[i] ^= 1 << 5;
		dfs(S, i + 1, ans);
		S[i] ^= 1 << 5;
	}
	
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {

		final String fileName = "tmp/trung99.xlsx";
		final String sheetName = "CALLink";
		final String sheetParam = "Para";
		final String sheetAllNodes = "AllNodes";
		final String nodeType = "Collection";//Concept
		InputStream is = new FileInputStream(fileName);
		Workbook workbook = WorkbookFactory.create(is);
		Map<String, Object> out = BayesianUtils.loadSimpleNetworkFormat(workbook, sheetName ,sheetParam ,sheetAllNodes);
		//get data
		Map<String, Set<String>> outNodes = (Map<String, Set<String>>) out.get("graph");
		Map<String, Set<String>> inNodes = (Map<String, Set<String>>) out.get("graphToMe");
		Map<String, Object> kwargs = (Map<String, Object>) out.get("prob");
		Map<String, String> allNodes = (Map<String, String>) out.get("allNode");
		Map<String, String> collections = (Map<String, String>) out.get("collections");
		
		Set<String> qs = new HashSet<>();//Arrays.asList("Q1", "Q2")
		Set<String> cs = new HashSet<>();//Arrays.asList("C1", "C2", "C3", "C4", "C5")

		collections.forEach((k, v) -> {
			allNodes.forEach((nKey, nValue) -> {
				if (!"NULL".equals(nValue))
					if (k.equals(nKey)) {
						if (nValue.equals(nodeType)) {
							qs.add(v);
						} else {
							cs.add(v);
						}
					}
			});
		});
		
		// a k m n
		QCBayesianNetwork qcbn = new QCBayesianNetwork(outNodes, inNodes, qs, cs, kwargs);
		//{Q1=[C1, C2], C3=[C5], Q2=[C2], C4=[C5], C1=[C3, C4], C2=[C4]}
		//{C3=[C1], C4=[C1, C2], C5=[C3, C4], C1=[Q1], C2=[Q1, Q2]}
		//[Q1, Q2]
		//[C3, C4, C5, C1, C2]
		//{k=0.5, l=0.6, m=0.7, n=0.2}
		qcbn.logger.setLevel(Level.OFF);
		String str="";
		for (String string : qs) {
			str = str + string;
		}
		List<Set<String>> rs = calculatePermutation(str);
		//[[-Q1, -Q2], [Q2, -Q1], [Q1, -Q2], [Q1, Q2]]
		for (String ss : cs) {
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
