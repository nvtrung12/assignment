package webservice;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
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
import javax.json.JsonObject;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import edu.stanford.nlp.ling.TaggedWord;
import graph.ContentJSONGraph;
import graph.IGraph;
import parser.IParser;
import parser.ParserV5NoSpark;
import util.XlsxUtil;

/**
 * @deprecated replace by /api/parseFile
 * 
 *             Will remove in next version
 *
 */
@WebServlet(urlPatterns = "/upload")
@MultipartConfig
public class ProcessFile extends HttpServlet {
	private static final long serialVersionUID = -4751096228274971485L;
	public static String baseFilePath = "/tmp";
	final Logger logger = Logger.getLogger(this.getClass());

	private String appPath = null;

	@Override
	protected void doGet(HttpServletRequest reqest, HttpServletResponse response) throws ServletException, IOException {
		response.getWriter().println("must post method");
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
		this.appPath = request.getServletContext().getRealPath(""); // get path of the current folder
		logger.debug("appPath: " + appPath);

		// full path of folder
		ProcessFile.baseFilePath = String.format("%s%s", this.appPath, Constants.UPLOAD_FOLDER);

		process2(request, resp);
	}

	/*
	 * purpose: get all submitted files's content and save
	 */
	protected Map<String, Object> getSubmitContent(HttpServletRequest request, String uuid)
			throws IOException, ServletException {
		String[] tmp = null;
		String uploadFolder = String.format("%s%s", this.appPath, Constants.UPLOAD_FOLDER);

		Part filePart = request.getPart("file");
		String fileID2 = request.getParameter("hid_file");
		String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString(); // get Filename
		String fileID = FilenameUtils.getBaseName(fileName); // get only file name
		InputStream fileContent = filePart.getInputStream();

		// uploadFolder + file extension
		tmp = fileName.split("\\.");
		fileName = String.format("%s%smain.%s.%s", uploadFolder, File.separator, uuid, tmp[tmp.length - 1]);
		// fileID2 = fileName;
		// copy file source to destination
		java.nio.file.Files.copy(fileContent, (new File(fileName)).toPath(), StandardCopyOption.REPLACE_EXISTING);

		// for phrasefile
		Part filePartPhrasefile = request.getPart("phrasefile");
		String phrasefileName = null;
		String phrasefileName2 = null;
		String default_phrasefileName2 = null;
		String header = filePartPhrasefile.getHeader("content-disposition");
		System.out.println("Test: " + header);
		if (header.contains("filename=\"\"")) { // if filename is empty, user don't upload file
			// no file upload -> use default
			phrasefileName = String.format("%s%sphrase.%s.%s", uploadFolder, File.separator, uuid, "docx");

			ClassLoader classLoader = getClass().getClassLoader();
			File file = new File(classLoader.getResource(Constants.PHRASE_LIST_FILE_DEFAULT).getFile());
			java.nio.file.Files.copy(new FileInputStream(file), (new File(phrasefileName)).toPath(),
					StandardCopyOption.REPLACE_EXISTING);
		} else {
			phrasefileName = Paths.get(filePartPhrasefile.getSubmittedFileName()).getFileName().toString();
			phrasefileName2 = FilenameUtils.getBaseName(phrasefileName); // get only file name
			InputStream phrasefileContent = filePartPhrasefile.getInputStream();
			tmp = phrasefileName.split("\\.");
			phrasefileName = String.format("%s%sphrase.%s.%s", uploadFolder, File.separator, uuid, tmp[tmp.length - 1]);
			phrasefileName2 = header.toString();
			java.nio.file.Files.copy(phrasefileContent, (new File(phrasefileName)).toPath(),
					StandardCopyOption.REPLACE_EXISTING);
		}

		// for stopwordfile
		Part filePartStopwordfile = request.getPart("stopwordfile");
		String stopwordfileName = null;
		String stopwordfileName2 = null;
		header = filePartStopwordfile.getHeader("content-disposition");
		if (header.contains("filename=\"\"")) {
			stopwordfileName = String.format("%s%sstopword.%s.%s", uploadFolder, File.separator, uuid, "txt");

			ClassLoader classLoader = getClass().getClassLoader();
			File file = new File(classLoader.getResource(Constants.IGNORED_CONCEPTS_FILE_DEFAULT).getFile());
			java.nio.file.Files.copy(new FileInputStream(file), (new File(stopwordfileName)).toPath(),
					StandardCopyOption.REPLACE_EXISTING);
		} else {
			InputStream stopwordfileContent = filePartStopwordfile.getInputStream();
			stopwordfileName = Paths.get(filePartStopwordfile.getSubmittedFileName()).getFileName().toString();
			stopwordfileName2 = FilenameUtils.getBaseName(stopwordfileName); // get only file name
			tmp = stopwordfileName.split("\\.");
			stopwordfileName = String.format("%s%sstopword.%s.%s", uploadFolder, File.separator, uuid,
					tmp[tmp.length - 1]);
			stopwordfileName2 = stopwordfileName2 + "." + tmp[tmp.length - 1];
			java.nio.file.Files.copy(stopwordfileContent, (new File(stopwordfileName)).toPath(),
					StandardCopyOption.REPLACE_EXISTING);
		}

		Map<String, Object> ret = new HashMap<>();
		ret.put("mainFile", fileName); // Filename now has full path
		ret.put("fileID", fileID); // only basename of file
		ret.put("fileID2", fileID2);
		ret.put("stopwordfileName", stopwordfileName);
		ret.put("phrasefileName", phrasefileName);
		ret.put("phrasefileName2", phrasefileName2);
		ret.put("stopwordfileName2", stopwordfileName2);
		// ret.put("default_phrasefileName2", default_phrasefileName2);

		return ret;
	}

	/**
	 * Process each upload separated (no combined in big graph as 1)
	 * 
	 * @param request
	 * @param resp
	 * @throws ServletException
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	protected void process2(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {

		String uuid = UUID.randomUUID().toString().replace("-", ""); // get random number, string format

		Map<String, Object> submitcontent = getSubmitContent(request, uuid);
		// String fileName = (String) submitcontent.get("mainFile");
		String fileID = (String) submitcontent.get("fileID");
		String fileID2 = (String) submitcontent.get("fileID2"); // get file name with extension
		// String stopwordfileName = (String) submitcontent.get("stopwordfileName");
		String stopwordfileName2 = (String) submitcontent.get("stopwordfileName2");
		// String phrasefileName = (String) submitcontent.get("phrasefileName");
		String phrasefileName2 = (String) submitcontent.get("phrasefileName2");
		String default_phrasefileName2 = (String) submitcontent.get("default_phrasefileName2");

		System.out.println("submit content " + submitcontent.toString());
		String downloadFolder = String.format("%s%s", this.appPath, Constants.DOWNLOAD_FOLDER);

		try {
			final Function<String, String> fGetParam = (key) -> {
				String value = request.getParameter(key);
				value = value == null || value.equals("") ? "0" : value;
				return value;
			};

			// use EbookFile, phrasefile and stopword file from additionInfo, not use file
			Map<String, String> additionInfo = loadAdditionInfo(request);
			String fileName = additionInfo.get("ebookFile");
			String phrasefileName = additionInfo.get("phraseFile");
			String stopwordfileName = additionInfo.get("stopwordFile");

			logger.debug("ebookFile from additionInfo: " + fileName);

			// BookTitle.ColectionArea.ISBN.Author
			String collectionstring = String.format("%s.%s.%s.%s", fGetParam.apply("book_title"),
					fGetParam.apply("colection_area"), fGetParam.apply("isbn"), fGetParam.apply("author"));

			IParser p = new ParserV5NoSpark(stopwordfileName, phrasefileName);
			Map<String, Object> ret = p.processFile(fileName, collectionstring);

			// String conceptsFileName = String.format("%s-%s-concepts.xlsx", uuid, fileID);
			//String conceptsFileName = String.format("%s-%s-concepts.xlsx", fileID, uuid);
			String conceptsFileName = String.format("%s.xlsx", fileID);
			String conceptSfilePath = String.format("%s%s%s", downloadFolder, File.separator, conceptsFileName);

			request.setAttribute("message", "output");
			request.setAttribute("conceptFile", conceptsFileName); // set link for users to download file

			// request.setAttribute("fileID", fileID);
			request.setAttribute("additionInfo", StringEscapeUtils.escapeHtml4(additionInfo.get("additionInfoStr")));
			request.setAttribute("fileID2", fileID2);
			request.setAttribute("phrasefileName", phrasefileName);
			request.setAttribute("stopwordfileName", stopwordfileName);
			request.setAttribute("phrasefileName2", phrasefileName2);
			request.setAttribute("stopwordfileName2", stopwordfileName2);

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

			metaInfo.add(Arrays.asList(objSpace));

			metaInfo.add(Arrays.asList(objSpace));

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

			// build information for graph to display
			List<Object[]> sub_graph = (List<Object[]>) ret.get(Constants.ConnectionSheetName);
			sub_graph.remove(0);

			// TODO later, filter sentence nodes, not process now
			List<Object[]> subGraphNoSentenceNode = sub_graph.stream()
					.filter(o -> !"NULL".equals(o[Constants.fNodeHeaderPos.apply(Constants.NODE_NAME_HEADER_NAME)]))
					.collect(Collectors.toList());

			Map<String, String> sentMap = new HashMap<>();
			// get map information of sentence id and sentence content
			Map<String, Object> sm = (Map<String, Object>) ret.get("sentencesMap");
			if ((sm.values()).iterator().next() instanceof String) {
				sentMap = (Map<String, String>) ret.get("sentencesMap"); //
			} else {
				Map<String, List<TaggedWord>> sub_sentenceMap = (Map<String, List<TaggedWord>>) ret.get("sentencesMap");
				for (String key : sub_sentenceMap.keySet())
					sentMap.put(key,
							sub_sentenceMap.get(key).stream().map(o1 -> o1.value()).collect(Collectors.joining(" ")));
			}

			// return a JsonObject which content information of nodes and edges and metaInfo
			IGraph graph = new ContentJSONGraph(subGraphNoSentenceNode, sentMap);

			String st = graph.getJsonVisjsFormat();
			request.setAttribute("gr_data", st);

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
				json.put("runtime", (long) ret.get("runtime"));

				request.setAttribute("meta_data", json.toJSONString());

			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
			request.setAttribute("message", e.getMessage());
		}

		request.getRequestDispatcher("index.jsp").forward(request, resp);
	}

	private Map<String, String> loadAdditionInfo(HttpServletRequest request) throws URISyntaxException {
		final Function<String, String> fGetParamJSON = (key) -> {
			String value = request.getParameter(key);
			value = value == null || value.equals("") ? "{}" : value;
			return value;
		};

		String uploadPath = String.format("%s%s", this.appPath, Constants.UPLOAD_FOLDER);
		logger.debug("upload path: " + uploadPath);

		Set<String> fileKeys = Stream.of("ebookFile", "phraseFile", "stopwordFile").collect(Collectors.toSet());

		// parse and get additionInfo {ebookFile:..., phraseFile:..., stopwordFile:...}
		String additionInfoStr = StringEscapeUtils.unescapeHtml4(fGetParamJSON.apply("additionInfo"));
		logger.debug("infoStr: " + additionInfoStr);
		JsonObject js = Json.createReader(new StringReader(additionInfoStr)).readObject();
		Map<String, String> additionInfo = new HashMap<>();
		for (String key : js.keySet()) {
			try {
				// currently, only get string, other will be omit
				// example realName which is object, not string
				additionInfo.put(key, js.getString(key));
			} catch (Exception e) {
			}
		}

		// add uploads to get full path
		for (String fileKey : fileKeys) {
			if (null != additionInfo.get(fileKey))
				additionInfo.put(fileKey,
						String.format("%s%s%s", uploadPath, File.separator, additionInfo.get(fileKey)));
		}

		// if not given then use default for phraseFile and stopwordFile
		ClassLoader cl = this.getClass().getClassLoader();
		logger.debug(cl.getResource(Constants.PHRASE_LIST_FILE_DEFAULT).toURI());

		String defaultPhraseFilePath = Paths.get(cl.getResource(Constants.PHRASE_LIST_FILE_DEFAULT).toURI())
				.toAbsolutePath().toString();
		String defaultStopwordFile = Paths.get(cl.getResource(Constants.IGNORED_CONCEPTS_FILE_DEFAULT).toURI())
				.toAbsolutePath().toString();

		if (null == additionInfo.get("phraseFile"))
			additionInfo.put("phraseFile", defaultPhraseFilePath);
		if (null == additionInfo.get("stopwordFile"))
			additionInfo.put("stopwordFile", defaultStopwordFile);

		logger.debug("additionInfo: " + additionInfoStr + additionInfo.toString());
		logger.debug("path: " + additionInfo.get("phraseFile") + " --- " + additionInfo.get("stopwordFile"));

		additionInfo.put("additionInfoStr", additionInfoStr);

		return additionInfo;
	}
}