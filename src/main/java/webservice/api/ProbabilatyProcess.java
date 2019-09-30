package webservice.api;

import static bayesian.QCBayesianNetwork.toSet;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import bayesian.BayesianUtils;
import bayesian.QCBayesianNetwork;
import util.XlsxUtil;

public class ProbabilatyProcess {

	private String upTextSym(char text) {
		return "-".concat(String.valueOf(text));
	}

	private List<Set<String>> permute(String input) {
		int n = input.length();
		int max = 1 << n;
		input = input.toLowerCase();
		List<Set<String>> rs = new ArrayList<>();
		for (int i = 0; i < max; i++) {
			char combination[] = input.toCharArray();
			String[] array = new String[n];
			Set<String> set = new HashSet<>();
			for (int j = 0; j < n; j++) {
				if (((i >> j) & 1) == 1)
					combination[j] = (char) (combination[j] - 32);
			}
			for (int m = 0; m < combination.length; m++) {
				char x = combination[m];
				if (x == Character.toUpperCase(combination[m])) {
					array[m] = upTextSym(x);
					set.add(upTextSym(x));
				} else {
					array[m] = String.valueOf(Character.toUpperCase(x));
					set.add(String.valueOf(Character.toUpperCase(x)));
				}
			}
			rs.add(set);
			// result.add(combination.toString());
		}
		return rs;
	}

	public List<Set<String>> calculatePermutation(String S) {
		List<String> ans = new ArrayList<>();
		dfs(S.toCharArray(), 0, ans);
		// q1q2q3q4
		List<Set<String>> rs = new ArrayList<Set<String>>();
		ans.forEach(q -> {
			char[] ch = q.toCharArray();
			Set<String> set = new HashSet<>();
			for (int j = 0; j < ch.length; j++) {
				if (j % 2 != 0) {
					if (ch[j - 1] == Character.toUpperCase(ch[j - 1])) {
						set.add(upTextSym(ch[j - 1]) + String.valueOf(ch[j]));
					} else {
						set.add(String.valueOf(Character.toUpperCase(ch[j - 1])) + ch[j]);
					}
					// set.add(String.valueOf(ch[j-1]+ch[j]));
				}
			}
			rs.add(set);
		});

		return rs;
	}

	private void dfs(char[] S, int i, List<String> ans) {
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

	public String probability(String fileName, String path, String downloadFile) throws Exception {
		// final String fileName = "tmp/inputtest1.xlsx";
		final String sheetName = "CALLink";
		final String sheetParam = "Para";
		final String sheetAllNodes = "AllNodes";
		final String nodeType = "Collection";// Concept
		InputStream is = new FileInputStream(path + fileName);
		Workbook workbook = WorkbookFactory.create(is);
		Map<String, Object> out = BayesianUtils.loadSimpleNetworkFormat(workbook, sheetName, sheetParam, sheetAllNodes);
		// get data
		Map<String, Set<String>> outNodes = (Map<String, Set<String>>) out.get("graph");
		Map<String, Set<String>> inNodes = (Map<String, Set<String>>) out.get("graphToMe");
		Map<String, Object> kwargs = (Map<String, Object>) out.get("prob");
		Map<String, String> allNodes = (Map<String, String>) out.get("allNode");
		Map<String, String> collections = (Map<String, String>) out.get("collections");
		Set<String> questionParam = (Set<String>) out.get("questionParam");
		
		
		Set<String> qs = new HashSet<>();// Arrays.asList("Q1", "Q2")
		Set<String> cs = new HashSet<>();// Arrays.asList("C1", "C2", "C3", "C4", "C5")

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
		if(questionParam.size() != qs.size()) return "Lỗi nhập param không đủ số câu hỏi";
		
		// a k m n
		QCBayesianNetwork qcbn = new QCBayesianNetwork(outNodes, inNodes, qs, cs, kwargs);
		// {Q1=[C1, C2], C3=[C5], Q2=[C2], C4=[C5], C1=[C3, C4], C2=[C4]}
		// {C3=[C1], C4=[C1, C2], C5=[C3, C4], C1=[Q1], C2=[Q1, Q2]}
		// [Q1, Q2]
		// [C3, C4, C5, C1, C2]
		// {k=0.5, l=0.6, m=0.7, n=0.2}
		qcbn.logger.setLevel(Level.OFF);
		String str = "";
		for (String string : qs) {
			str = str + string;
		}
		
		//List<Set<String>> rs = calculatePermutation(str);
		// [[-Q1, -Q2], [Q2, -Q1], [Q1, -Q2], [Q1, Q2]]
		
		// create sheet new and write file
		String sheetProName = "Probability";

		Sheet sheetPro = workbook.getSheet(sheetProName);
		if (sheetPro == null) {
			sheetPro = workbook.createSheet(sheetProName);
		}
		// add title header row0
		List<String> title = new ArrayList<>() ;
		title.add("NodeName");
		for (String question : qs) {
			title.add(question);
		}
		String[] StringTitle = { "Probability", "Bloom level", "Value" };//
		for (String string : StringTitle) {
			title.add(string);
		}
		Row headerTitle = sheetPro.createRow(0);
		for (int i = 0; i < title.size(); i++) {
			Cell cell1 = headerTitle.createCell(i);
			cell1.setCellValue(title.get(i));
		}
		//row1 cell0
		//questionParam [Q1,-Q2]
		String[] arrayParam = questionParam.stream().toArray(String[]::new);
		int j = 0;
		for (String ss : cs) {// Arrays.asList("C1", "C2", "C3", "C4", "C5")
			j++;
			Row headerRow = sheetPro.createRow(j);
			Cell node = headerRow.createCell(0);
			node.setCellValue(ss);
			for (int i = 1; i < title.size() - StringTitle.length; i++) {
				Cell cellQuestion = headerRow.createCell(i);
				if(title.get(i).equals(arrayParam[i-1])) {
					cellQuestion.setCellValue(1);
				}else {
					cellQuestion.setCellValue(0);
				}
			}
			int k=0;
			for (int i = arrayParam.length + 1; i < title.size(); i++) {
				Cell cellFinal = headerRow.createCell(i);
				if(k==0) {
					String data = String.format("P(%s|%s) ", ss, questionParam.toString());
					cellFinal.setCellValue(data);
				}else if (k==1) {
					cellFinal.setCellValue(1);
				}else {
					cellFinal.setCellValue(qcbn.p(ss, questionParam));
				}
				k++;
			}
		}

		FileOutputStream fileOut = new FileOutputStream(downloadFile + fileName);
		workbook.write(fileOut);
		fileOut.close();

		workbook.close();
		return downloadFile + fileName;
	}

}
