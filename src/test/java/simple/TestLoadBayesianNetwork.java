package simple;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import bayesian.BayesianUtils;

public class TestLoadBayesianNetwork {
	public static void main(String[] args) throws EncryptedDocumentException, InvalidFormatException, IOException {
		final String fileName = "tmp/inputtest1.xlsx";
		final String sheetName = "sheet1";
		final String sheetParam = "Para";
		InputStream is = new FileInputStream(fileName);
		Map<String, Object> out = BayesianUtils.loadSimpleNetworkFormat(is, sheetName ,sheetParam);

		for (String key : out.keySet()) {
			System.out.println("key: " + key);
			System.out.println("values: " + out.get(key));
		}
	}
}
