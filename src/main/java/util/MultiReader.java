package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;

/**
 * 
 * @author thanhhungqb
 *
 *         Multi reader (parser) filetype support pdf, doc, docx, txt, ...<br/>
 *         Copy from Dr. Hung with modified
 */
public class MultiReader {

	public static String newParagraph = "\n\n";
	public static String newPage = "\n\n\n";

	public static String parseMulti(String fileName) throws IOException, InvalidFormatException {
		File file = new File(fileName);

		System.out.println(fileName);
		System.out.println(file.toPath());

		String ret = null;

		String fileType = Files.probeContentType(file.toPath());
		// if can not detect then use file extension
		if (null == fileType) {
			String[] arr = file.toPath().toString().split("\\.");
			fileType = arr[arr.length - 1];
			switch (fileType) {
			case "pdf":
				fileType = "application/pdf";
				break;
			case "doc":
				fileType = "application/msword";
				break;
			case "docx":
				fileType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
				break;
			case "txt":
				fileType = "text/plain";
				break;
			}
		}

		System.out.println(fileType);
		switch (fileType) {
		case "application/pdf":
			ret = parsePdf(fileName);
			break;
		case "application/msword":
			ret = parseDoc(fileName);
			break;
		case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
			ret = parseDocx(fileName);
			break;
		case "text/plain":
			ret = new String(Files.readAllBytes(Paths.get(fileName)));
			break;
		}

		return ret;
	}

	public static String parseMulti(InputStream inputStream, String fileType)
			throws IOException, InvalidFormatException {

		String ret = null;

		System.out.println(fileType);
		switch (fileType) {
		case "application/pdf":
			ret = parsePdf(inputStream);
			break;
		case "application/msword":
			ret = parseDoc(inputStream);
			break;
		case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
			ret = parseDocx(inputStream);
			break;
		case "text/plain":
			// ret = new String(Files.readAllBytes(Paths.get(fileName)));
			ret = new String(IOUtils.toByteArray(inputStream));
			break;
		}

		return ret;
	}

	public static String getFileType(String fileName) throws IOException {
		String fileType = null;
		try {
			File file = new File(fileName);

			System.out.println(fileName);
			System.out.println(file.toPath());

			String ret = null;

			fileType = Files.probeContentType(file.toPath());
		} catch (Exception e) {
		}

		// if can not detect then use file extension
		if (null == fileType) {
			String[] arr = fileName.split("\\.");
			fileType = arr[arr.length - 1];
			switch (fileType) {
			case "pdf":
				fileType = "application/pdf";
				break;
			case "doc":
				fileType = "application/msword";
				break;
			case "docx":
				fileType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
				break;
			case "txt":
				fileType = "text/plain";
				break;
			}
		}
		return fileType;
	}

	/**
	 * parse PDF file and get content, page by page<br/>
	 * Copy from Dr. Hung with modified
	 * 
	 * @param pdfFileName
	 * @return content
	 * @throws IOException
	 */
	public static String parsePdf(String pdfFileName) throws IOException {
		PdfReader reader = new PdfReader(pdfFileName);
		PdfReaderContentParser parser = new PdfReaderContentParser(reader);
		StringBuffer out = new StringBuffer();
		TextExtractionStrategy strategy;
		for (int i = 1; i <= reader.getNumberOfPages(); i++) {
			strategy = parser.processContent(i, new SimpleTextExtractionStrategy());
			out.append(strategy.getResultantText());
			out.append(MultiReader.newPage);
		}
		reader.close();
		return out.toString();
	}

	/**
	 * parse PDF file and get content, page by page<br/>
	 * 
	 * @param inputStream
	 * @return content
	 * @throws IOException
	 */
	public static String parsePdf(InputStream inputStream) throws IOException {
		PdfReader reader = new PdfReader(inputStream);
		PdfReaderContentParser parser = new PdfReaderContentParser(reader);
		StringBuffer out = new StringBuffer();
		TextExtractionStrategy strategy;
		for (int i = 1; i <= reader.getNumberOfPages(); i++) {
			strategy = parser.processContent(i, new SimpleTextExtractionStrategy());
			out.append(strategy.getResultantText());
			out.append(MultiReader.newPage);
		}
		reader.close();
		return out.toString();
	}

	/**
	 * parse .doc and get content<br/>
	 * Copy from Dr. Hung with modified
	 * 
	 * @param docFileName
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static String parseDoc(String docFileName) throws FileNotFoundException, IOException {
		POIFSFileSystem fs = null;

		fs = new POIFSFileSystem(new FileInputStream(docFileName));
		HWPFDocument doc = new HWPFDocument(fs);
		WordExtractor we = new WordExtractor(doc);
		String text = we.getText();

		return text;
	}

	/**
	 * parse .doc and get content
	 * 
	 * @param docFileName
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static String parseDoc(InputStream inputStream) throws FileNotFoundException, IOException {
		POIFSFileSystem fs = null;

		fs = new POIFSFileSystem(inputStream);
		HWPFDocument doc = new HWPFDocument(fs);
		WordExtractor we = new WordExtractor(doc);
		String text = we.getText();

		return text;
	}

	/**
	 * parse .docx and get content<br/>
	 * Copy from Dr. Hung with modified
	 * 
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public String readDocxFile(String fileName) throws IOException {

		FileInputStream fis = new FileInputStream(fileName);

		XWPFDocument document = new XWPFDocument(fis);

		List<XWPFParagraph> paragraphs = document.getParagraphs();

		StringBuffer buff = new StringBuffer();
		// System.out.println("Total no of paragraph " + paragraphs.size());
		for (XWPFParagraph para : paragraphs) {
			buff.append(para.getText());
			buff.append(MultiReader.newParagraph);
		}
		fis.close();
		return buff.toString();
	}

	/**
	 * Another version of parse .docx file Copy from Dr. Hung with modified
	 * 
	 * @param docxFileName
	 * @return
	 * @throws InvalidFormatException
	 * @throws IOException
	 */
	public static String parseDocx(String docxFileName) throws InvalidFormatException, IOException {
		FileInputStream fis = new FileInputStream(docxFileName);
		XWPFDocument xdoc = new XWPFDocument(OPCPackage.open(fis));
		XWPFWordExtractor extractor = new XWPFWordExtractor(xdoc);
		return extractor.getText();
	}

	/**
	 * 
	 * @param inputStream
	 * @return
	 * @throws InvalidFormatException
	 * @throws IOException
	 */
	public static String parseDocx(InputStream inputStream) throws InvalidFormatException, IOException {
		XWPFDocument xdoc = new XWPFDocument(OPCPackage.open(inputStream));
		XWPFWordExtractor extractor = new XWPFWordExtractor(xdoc);
		return extractor.getText();
	}

	// not use functions
	public String x(String fileName) throws IOException {
		File file = new File(fileName);
		WordExtractor extractor = null;

		FileInputStream fis = new FileInputStream(file.getAbsolutePath());
		HWPFDocument document = new HWPFDocument(fis);
		extractor = new WordExtractor(document);
		String[] fileData = extractor.getParagraphText();
		for (int i = 0; i < fileData.length; i++) {
			if (fileData[i] != null)
				System.out.println(fileData[i]);
		}

		return null;

	}

	public String x2(String fileName) {
		File file = new File(fileName);
		// WordExtractor extractor = null;
		try {
			FileInputStream fis = new FileInputStream(file.getAbsolutePath());
			XWPFDocument docx = new XWPFDocument(fis);
			List<XWPFParagraph> paragraphList = docx.getParagraphs();
			for (XWPFParagraph paragraph : paragraphList) {
				System.out.print(paragraph.getText());
			}
		} catch (Exception exep) {
			exep.printStackTrace();
		}
		return null;

	}
}
