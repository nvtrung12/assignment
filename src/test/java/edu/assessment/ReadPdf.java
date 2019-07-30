package edu.assessment;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import util.MultiReader;

import java.io.File;
import java.io.IOException;

public class ReadPdf {

	public static void main(String[] args) throws IOException, InvalidFormatException {

		try (PDDocument document = PDDocument.load(new File("tmp/0.pdf"))) {

			document.getClass();

			if (!document.isEncrypted()) {

				PDFTextStripperByArea stripper = new PDFTextStripperByArea();
				stripper.setSortByPosition(true);

				PDFTextStripper tStripper = new PDFTextStripper();

				String pdfFileInText = tStripper.getText(document);
				// System.out.println("Text:" + st);

				// split by whitespace
				String lines[] = pdfFileInText.split("\\r?\\n");
				for (String line : lines) {
					System.out.println(line);
				}

			}

			String text = MultiReader.parseMulti("tmp/0.pdf");
			System.out.println(text);

		}

	}
}