package webservice.api;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.json.Json;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import parser.IParser;
import parser.ParserV5NoSpark;
import util.XlsxUtil;
import webservice.Constants;
import webservice.StatisticalUtils;

/**
 * 
 * This is version 1.1 of processFile that only processFile
 * 
 * Call another api to show graph
 * 
 * @return fileName (xlsx)
 */

@WebServlet(urlPatterns = { "/api/parseFile", "/api/v1.1/parseFile" })
@MultipartConfig
public class ParseFileApi extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	final Logger logger = Logger.getLogger(this.getClass());

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		processParse(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
		processParse(request, resp);
	}

	@SuppressWarnings("unchecked")
	private void processParse(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		response.setContentType("application/json");
		request.getSession().setAttribute("numProcessed", 0);

		// get random number, string format
		String uuid = UUID.randomUUID().toString().replace("-", "");

		String appPath = request.getServletContext().getRealPath("");

		final Function<String, String> fGetParam = (key) -> {
			String value = request.getParameter(key);
			value = value == null || value.equals("") ? "0" : value;
			return value;
		};

		try {

			String downloadFolder = String.format("%s%s", appPath, Constants.DOWNLOAD_FOLDER);

			// use EbookFile, phrasefile and stopword file from additionInfo, not use file
			Map<String, String> info = parseSimpleString(request);
			logger.debug(info.toString());
			String fileName = info.get("ebookFile");

			if (null == fileName)
				throw new Exception("Must post ebookFile");

			String fileID = FilenameUtils.getBaseName(fileName); // get only file name
			String phrasefileName = info.get("phraseFile");
			String stopwordfileName = info.get("stopwordFile");

			logger.debug("ebookFile from additionInfo: " + fileName);

			// BookTitle.ColectionArea.ISBN.Author
			String collectionstring = String.format("%s.%s.%s.%s", fGetParam.apply("book_title"),
					fGetParam.apply("colection_area"), fGetParam.apply("isbn"), fGetParam.apply("author"));

			// call main process
			IParser p = new ParserV5NoSpark(stopwordfileName, phrasefileName, request.getSession());

			// set default equivalence file
			((ParserV5NoSpark) p).setEquivalenFile(
					this.getClass().getClassLoader().getResourceAsStream(Constants.EQUIVALENCE_LIST_FILE_DEFAULT));

			Map<String, Object> ret = p.processFile(fileName, collectionstring);

			// String conceptsFileName = String.format("%s-%s-concepts.xlsx", fileID, uuid);

			String[] tmp2 = null;
			tmp2 = fileID.split("\\.");
			String realFileID = tmp2[tmp2.length - 1];
			List<String> fileNames = new ArrayList<String>();
			downloadFolder = String.format("%s%s%s", downloadFolder, File.separator, realFileID);
			if(!new File(downloadFolder).exists()) {
				new File(downloadFolder).mkdir();
			}
			int maxFileNames = 0;
			fileNames.add(XlsxUtil.nextFile(fileNames, downloadFolder, realFileID));
			//String conceptsFileName = String.format("%s.xlsx", realFileID);
			//String conceptSfilePath = String.format("%s%s%s", downloadFolder, File.separator, conceptsFileName);

			// get list of concepts
			List<List<Object>> conceptToWrite = XlsxUtil
					.convertRowType(((List<Object[]>) ret.get(Constants.ConceptSheetName)));
			XlsxUtil.writeRowsXlsxAppendMuti(fileNames, downloadFolder , realFileID, Constants.ConceptSheetName, conceptToWrite);
			if(maxFileNames < fileNames.size()) {
				maxFileNames = fileNames.size();
			}
			fileNames = new ArrayList<String>();
			fileNames.add(XlsxUtil.nextFile(fileNames, downloadFolder, realFileID));
			List<List<Object>> connectionToWrite = XlsxUtil
					.convertRowType(((List<Object[]>) ret.get(Constants.ConnectionSheetName)));
			XlsxUtil.writeRowsXlsxAppendMuti(fileNames, downloadFolder, realFileID, Constants.ConnectionSheetName, connectionToWrite);
			
			if(maxFileNames < fileNames.size()) {
				maxFileNames = fileNames.size();
			}
			
			// prepaid to add to list
			Map<String, Object> metaData = (Map<String, Object>) ret.get(Constants.META_SHEET_NAME);
			List<List<Object>> metaInfo = this.buildMetaInfo(request, response, metaData);

			// write to file
			fileNames = new ArrayList<String>();
			fileNames.add(XlsxUtil.nextFile(fileNames, downloadFolder, realFileID));
			XlsxUtil.writeRowsXlsxAppendMuti(fileNames, downloadFolder, realFileID, Constants.META_SHEET_NAME, metaInfo);
			
			if(maxFileNames < fileNames.size()) {
				maxFileNames = fileNames.size();
			}

			// build json data for metadata
			String metaJson = buildMetaJson(metaData);
			JSONArray jsonArray = new JSONArray();
			for (int i = 0; i < maxFileNames; i++) {
				JSONObject jsonObject = new JSONObject();
				String conceptsFileName = String.format("%s.xlsx", realFileID + "_" + (i + 1));
				jsonObject.put("fileName", conceptsFileName);
				jsonObject.put("fileFolder", realFileID);
				jsonArray.add(jsonObject);
			}
			
			String jsonReturn = Json.createObjectBuilder().add("status", 0).add("conceptFile", realFileID)
					.add("fileDownload", jsonArray.toJSONString())
					.add("message", "output").add("metaJson", metaJson).build().toString();

			response.getWriter().println(jsonReturn);
		} catch (Exception e) {
			e.printStackTrace();
			String jsonReturn = Json.createObjectBuilder().add("status", 1).add("message", e.getMessage()).build()
					.toString();
			response.getWriter().println(jsonReturn);
		}
	}

	@SuppressWarnings("unchecked")
	private String buildMetaJson(Map<String, Object> metaData) {
		try {
			// get meta-info
			JSONObject json = new JSONObject(metaData);

			// sentenceDegreeDistribution
			Map<String, Integer> tmp5 = (Map<String, Integer>) metaData.get("sentenceDegreeDistribution");
			int[] o = StatisticalUtils.histogram(tmp5); // used for drawing chart
			JSONArray tmpx = new JSONArray();
			for (int k : o)
				tmpx.add(k);

			json.put("sentenceDegreeDistributionHis", tmpx);

			// conceptDegreeDistribution
			tmp5 = (Map<String, Integer>) metaData.get("conceptDegreeDistribution");
			o = StatisticalUtils.histogram(tmp5);
			tmpx = new JSONArray();
			for (int k : o)
				tmpx.add(k);
			json.put("conceptDegreeDistributionHis", tmpx);

			return json.toJSONString();

		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	private List<List<Object>> buildMetaInfo(HttpServletRequest request, HttpServletResponse response,
			Map<String, Object> metaData) {
		List<List<Object>> metaInfo = new LinkedList<>();

		final BiFunction<String, String, List<Object>> remapper = (name, key) -> {
			String value = request.getParameter(key);
			request.setAttribute(key, value == null || value.equals("/") ? "" : value);
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

		for (int i = 0; i < 5; ++i)
			metaInfo.add(Arrays.asList(objSpace));

		// Statistics:
		Object[] objtmp = { "Statistics:" };
		metaInfo.add(Arrays.asList(objtmp));

		final BiFunction<String, String, List<Object>> fToSave = (name, key) -> {
			Object value = metaData.get(key);
			value = null == value ? "NULL" : value;

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
		return metaInfo;
	}

	private Map<String, String> parseSimpleString(HttpServletRequest request) throws URISyntaxException {

		Map<String, String> ret = new HashMap<>();

		String appPath = request.getServletContext().getRealPath("");
		String uploadFolder = String.format("%s%s", appPath, Constants.UPLOAD_FOLDER);

		Enumeration<String> params = request.getParameterNames();
		while (params.hasMoreElements()) {
			String key = params.nextElement();
			try {
				// try str first
				ret.put(key, request.getParameter(key));
			} catch (Exception e) {
				logger.debug("not string: " + key);
			}
		}
		Set<String> fileKeys = Stream.of("ebookFile", "phraseFile", "stopwordFile").collect(Collectors.toSet());
		// add uploads to get full path
		for (String fileKey : fileKeys) {
			if (null != ret.get(fileKey))
				ret.put(fileKey, String.format("%s%s%s", uploadFolder, File.separator, ret.get(fileKey)));
		}

		// if not given then use default for phraseFile and stopwordFile
		ClassLoader cl = this.getClass().getClassLoader();
		logger.debug(cl.getResource(Constants.PHRASE_LIST_FILE_DEFAULT).toURI());

		String defaultPhraseFilePath = Paths.get(cl.getResource(Constants.PHRASE_LIST_FILE_DEFAULT).toURI())
				.toAbsolutePath().toString();
		String defaultStopwordFile = Paths.get(cl.getResource(Constants.IGNORED_CONCEPTS_FILE_DEFAULT).toURI())
				.toAbsolutePath().toString();

		if (null == ret.get("phraseFile"))
			ret.put("phraseFile", defaultPhraseFilePath);
		if (null == ret.get("stopwordFile"))
			ret.put("stopwordFile", defaultStopwordFile);

		return ret;
	}
}
