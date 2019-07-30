package parser;

import java.io.File;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

import util.MultiReader;
import util.XlsxUtil;
import webservice.Constants;

public class CbookGen {

	public static String tmpFolder = ".";

	public static void main(String[] args) throws Exception {

		// initial();

		String uploadFolder = tmpFolder;

		List<Map<String, String>> paramsLst = param(args);
		System.out.println(paramsLst);

		for (Map<String, String> params : paramsLst) {
			try {
				String uuid = UUID.randomUUID().toString().replace("-", "");
				String fileName = params.get("file");

				System.out.println("ten file:" + fileName);
				String stopwordfileName = null;// params.get("stopwordfileName");
				String phrasefileName = null;// params.get("phrasefileName");

				String outputFileName = params.get("output") != null ? params.get("output")
						: String.format("%s.xlsx", fileName);
				String[] tmp2 = null;
				tmp2 = outputFileName.split("/");

				// phrase file if not given
				if (null == phrasefileName) {
					phrasefileName = String.format("%s%sphrase.%s.%s", uploadFolder, File.separator, uuid, "docx");

					java.nio.file.Files.copy(
							CbookGen.class.getResourceAsStream("/" + Constants.PHRASE_LIST_FILE_DEFAULT),
							(new File(phrasefileName)).toPath(), StandardCopyOption.REPLACE_EXISTING);
				}

				// stopword file if not given

				if (null == stopwordfileName) {
					stopwordfileName = String.format("%s%sstopword.%s.%s", uploadFolder, File.separator, uuid, "txt");

					java.nio.file.Files.copy(
							CbookGen.class.getResourceAsStream("/" + Constants.IGNORED_CONCEPTS_FILE_DEFAULT),
							(new File(stopwordfileName)).toPath(), StandardCopyOption.REPLACE_EXISTING);
				}

				Map<String, Object> out = gen(fileName, stopwordfileName, phrasefileName, params, outputFileName);
				System.out.println(out);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static void initial() {

		// make download folder if not exits
		File fileSaveDir = new File(tmpFolder);
		if (!fileSaveDir.exists())
			fileSaveDir.mkdirs();

	}

	public static List<Map<String, String>> param(String[] args) throws Exception {

		String inputFilePath = "i.txt";

		inputFilePath = args.length > 0 ? args[0] : "i.txt";
		List<Map<String, String>> lst = new LinkedList<>();

		Map<String, String> ret = new HashMap<>();

		String content = MultiReader.parseMulti(inputFilePath);
		System.out.println("input content:" + content);
		for (String line : content.split("\n")) {
			if (line.contains("-----")) {
				lst.add(ret);
				ret = new HashMap<>();
			} else {
				String[] arr = line.split(":");
				if (arr.length > 1)
					ret.put(arr[0].trim(), arr[1].trim());
			}
		}
		lst.add(ret);

		return lst;
	}

	/**
	 * 
	 * @param stopwordfileName
	 * @param phrasefileName
	 * @param                  params: book_title, colection_area, isbn, author
	 * @output: cbook
	 */
	public static Map<String, Object> gen(String fileName, String stopwordfileName, String phrasefileName,
			Map<String, String> params, String outputFile) {

		// contain return values
		Map<String, Object> retP = new HashMap<>();

		String uuid = UUID.randomUUID().toString().replace("-", ""); // get random number, string format
		String downloadFolder = tmpFolder;

		try {
			final Function<String, String> fGetParam = (key) -> {
				String value = params.get(key);
				value = value == null || value.equals("") ? "0" : value;
				return value;
			};

			// BookTitle.ColectionArea.ISBN.Author
			String collectionstring = String.format("%s.%s.%s.%s", fGetParam.apply("book_title"),
					fGetParam.apply("colection_area"), fGetParam.apply("isbn"), fGetParam.apply("author"));

			IParser p = new ParserV5NoSpark(stopwordfileName, phrasefileName);
			Map<String, Object> ret = p.processFile(fileName, collectionstring);

			String conceptsFileName = String.format("%s.xlsx", outputFile);
			String conceptSfilePath = String.format("%s%s%s", downloadFolder, File.separator, conceptsFileName);
			System.out.println(
					String.format("test: %s  -- %s -- %s", conceptSfilePath, downloadFolder, conceptsFileName));

			// get list of concepts
			List<List<Object>> conceptToWrite = XlsxUtil
					.convertRowType(((List<Object[]>) ret.get(Constants.ConceptSheetName)));
			XlsxUtil.writeXlsx(conceptSfilePath, Constants.ConceptSheetName, conceptToWrite);

			List<List<Object>> connectionToWrite = XlsxUtil
					.convertRowType(((List<Object[]>) ret.get(Constants.ConnectionSheetName)));
			XlsxUtil.writeRowsXlsxAppend(conceptSfilePath, Constants.ConnectionSheetName, connectionToWrite);

			// prepaid to add to list
			List<List<Object>> metaInfo = new LinkedList<>();

			final BiFunction<String, String, List<Object>> remapper = (name, key) -> {
				String value = params.get(key);
				Object[] tmp = { name, value == null || value.equals("") ? "NULL" : value };
				return Arrays.asList(tmp);
			};

			// PDF FileName(s):

			metaInfo.add(remapper.apply("Book Title:", "book_title"));
			metaInfo.add(remapper.apply("Author:", "author"));

			metaInfo.add(remapper.apply("Publisher:", "publisher"));

			metaInfo.add(remapper.apply("ISBN:", "isbn"));

			metaInfo.add(remapper.apply("DateofPublication:", "date_of_publication"));

			metaInfo.add(remapper.apply("Collection area:", "colection_area"));

			metaInfo.add(remapper.apply("colection_subarea", "colection_subarea"));

			metaInfo.add(remapper.apply("date_of_creation", "date_of_creation"));

			// make row space
			Object[] objSpace = { " " };

			metaInfo.add(Arrays.asList(objSpace));

			metaInfo.add(Arrays.asList(objSpace));

			metaInfo.add(remapper.apply("StopWordListFile:", " "));

			metaInfo.add(remapper.apply("PhraseListFile:", " "));

			metaInfo.add(remapper.apply("GeneratorVersion:", " "));

			metaInfo.add(remapper.apply("Date&TimeofCBookCreation:", " "));

			metaInfo.add(remapper.apply("ProcessingTimetoCreate:", " "));

			metaInfo.add(Arrays.asList(objSpace));

			metaInfo.add(Arrays.asList(objSpace));

			metaInfo.add(Arrays.asList(objSpace));

			// Statistics:
			Object[] objtmp = { "Statistics:" };
			metaInfo.add(Arrays.asList(objtmp));
			Map<String, Object> metaData = (Map<String, Object>) ret.get(Constants.META_SHEET_NAME);
			final BiFunction<String, String, List<Object>> fToSave = (name, key) -> {
				Object value = metaData.get(key);
				value = null == value ? "NULL" : value;

				// Object[] tmp = { "", key, value };
				Object[] tmp = { key, value };

				return Arrays.asList(tmp);
			};

			// get metaData from parser and save
			String[] arr = { "numberOfPage", "numberOfSentence", "numberOfWord", "uniqueWord", "uniqueDomainConcept",
					"numberOfSentenceNodes", "numberOfSentencesToConceptLinks", "sentenceDegreeDistributionAverage",
					"conceptDegreeDistributionAverage" };

			for (String key : arr)
				metaInfo.add(fToSave.apply(key, key));

			// Update History:
			metaInfo.add(new LinkedList<>());
			Object[] objtmp2 = { "Update History:" };
			metaInfo.add(Arrays.asList(objtmp2));

			// write to file
			XlsxUtil.writeRowsXlsxAppend(conceptSfilePath, Constants.META_SHEET_NAME, metaInfo);
			// done write to files

			retP.put("outfile", conceptSfilePath);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return retP;
	}
}
