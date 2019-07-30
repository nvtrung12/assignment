package edu.assessment;

import java.io.File;
import java.io.FileInputStream;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;

public class ReadDocx {

	public static void main(String[] args) {
		File file = null;
		WordExtractor extractor = null;
		try {

			file = new File("/home/hung/tmp/b.doc");
			FileInputStream fis = new FileInputStream(file.getAbsolutePath());
			HWPFDocument document = new HWPFDocument(fis);
			extractor = new WordExtractor(document);
			String[] fileData = extractor.getParagraphText();
			for (int i = 0; i < fileData.length; i++) {
				if (fileData[i] != null)
					System.out.println(fileData[i]);
			}
		} catch (Exception exep) {
			exep.printStackTrace();
		}
	}
}