package util;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;

/**
 * 
 * @author hung
 *
 *         For PDF reader or Word reader
 */
public class ReaderHelper {

	/**
	 * Load pdf content
	 * 
	 * @param filePath
	 * @return text in pdf file
	 * @throws IOException
	 */
	public static String loadPDF(String filePath) throws IOException {

		StringBuffer ret = new StringBuffer();
		try (PDDocument document = PDDocument.load(new File(filePath))) {

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
					ret.append(line);
					ret.append("\n");
				}
			}
		}
		return ret.toString();
	}

	/**
	 * Read Word file (only word XML)
	 * 
	 * @param filePath
	 * @return
	 */
	public static String loadWord(String filePath) {
		StringBuffer ret = new StringBuffer();
		return ret.toString();
	}

	/**
	 * Load text from pdf, doc, docx base on extense of file name
	 * 
	 * @param filePath
	 * @return String, content of file
	 * @throws IOException
	 */
	public static String loadGeneral(String filePath) throws IOException {
		if (null == filePath)
			return null;

		String upcasePath = filePath.toUpperCase();
		if (upcasePath.endsWith(".PDF"))
			return ReaderHelper.loadPDF(filePath);

		if (upcasePath.endsWith(".DOC"))
			return ReaderHelper.loadWord(filePath);

		return "Unknow file type";
	}
}
