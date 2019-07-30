package webservice.api;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;

import graph.ContentJSONGraph;
import graph.IGraph;
import util.XlsxUtil;
import webservice.Constants;

/**
 * 
 * 
 * api<br/>
 * To merge multi files (xlsx) give fileNames (in upload folder) <br/>
 * merge and create combinate in download folder, return fileName
 * 
 * @deprecated: replace by v1.1 from MergeApi
 *
 */

@WebServlet(urlPatterns = "/api/merge")
@MultipartConfig
public class GraphMergeApi extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		processMerge(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
		processMerge(request, resp);
	}

	private void processMerge(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		response.setContentType("application/json");

		String appPath = request.getServletContext().getRealPath("/");
		String uploadFolder = String.format("%s%s%s", appPath, File.separator, Constants.UPLOAD_FOLDER);
		String downloadFolder = String.format("%s%s%s", appPath, File.separator, Constants.DOWNLOAD_FOLDER);

		// TODO clean code get from query
		String json = request.getParameter("test");
		System.out.println("test param: " + json);
		String[] books = request.getParameter("books").split(":");
		System.out.println(books);

		List<String> fileNames = Arrays.asList(books);
		System.out.println(fileNames);
		System.out.println(fileNames.size());

		try {
			Map<String, Object> ret = mergeGraph(uploadFolder, fileNames);

			List<List<Object>> concepts = (List<List<Object>>) ret.get(Constants.ConceptSheetName);
			List<List<Object>> connections = (List<List<Object>>) ret.get(Constants.ConnectionSheetName);

			// write to combine file
			List<String> mainFileNames = fileNames.stream().map(o -> {
				String[] tmp = o.split("\\.");
				return tmp[0];
			}).collect(Collectors.toList());
			String combinationFile = String.join("_", mainFileNames) + ".xlsx";
			String combineFile = String.format("%s%s%s", downloadFolder, File.separator, combinationFile);
			XlsxUtil.writeXlsx(combineFile, Constants.ConceptSheetName, concepts);
			XlsxUtil.writeRowsXlsxAppend(combineFile, Constants.ConnectionSheetName, connections);
			XlsxUtil.writeRowsXlsxAppend(combineFile, Constants.META_SHEET_NAME, new LinkedList<>());

			// make a combine file used by filter, always use file this name
			File original = new File(combineFile);
			File copied = new File(String.format("%s%scombination.xlsx", File.separator, downloadFolder));
			FileUtils.copyFile(original, copied);

			String jsonReturn = Json.createObjectBuilder().add("status", 0).add("file", combinationFile)
					.add("grData", (String) ret.get("graph")).build().toString();

			response.getWriter().println(jsonReturn);
		} catch (Exception e) {
			e.printStackTrace();
			String jsonReturn = Json.createObjectBuilder().add("status", 1).add("message", e.getMessage()).build()
					.toString();
			response.getWriter().println(jsonReturn);
		}
	}

	private Map<String, Object> mergeGraph(String uploadFilePath, List<String> fileNames) throws Exception {
		String filePath = null;
		List<List<String>> concepts = new LinkedList<>();
		List<List<String>> connections = new LinkedList<>();

		for (String fileName : fileNames) {
			filePath = String.format("%s%s%s", uploadFilePath, File.separator, fileName);
			System.out.println("read file: " + filePath);

			try {
				List<List<String>> sub_concepts = XlsxUtil.readXlsx(filePath, Constants.ConceptSheetName);
				sub_concepts.remove(0); // remove header
				concepts.addAll(sub_concepts);

				List<List<String>> sub_connection = XlsxUtil.readXlsx(filePath, Constants.ConnectionSheetName);
				sub_connection.remove(0); // remove header
				connections.addAll(sub_connection);
			} catch (Exception e) {
				e.printStackTrace();
				throw new Exception(String.format("Read file %s error, please make sure correct format", fileName));
			}
		}

		List<Object[]> allConnection = new LinkedList<>();
		for (List<String> lst : connections) {
			allConnection.add(lst.toArray());
		}

		// build sentence map: sentID -> sentence content from concepts
		Map<String, String> sentMap = new HashMap<>();
		final Function<String, Integer> fConceptPos = hname -> Arrays.asList(Constants.CONCEPT_HEADER).indexOf(hname);
		int nodeIdPos = fConceptPos.apply(Constants.NODE_ID_HEADER_NAME);
		int nodeNamePos = fConceptPos.apply(Constants.NODE_NAME_HEADER_NAME);

		for (List<String> one : concepts) {
			// sentMap.put(one.get(0), one.get(5));
			sentMap.put(graph.Utils.extractSentenceIndex(one.get(nodeIdPos)),
					one.get(fConceptPos.apply(Constants.DISPLAY_NAME_HEADER_NAME)));
		}

		// get SentID for sentence and nodename (text value) for concept
		List<String> nodeList = concepts.stream()
				.map(o -> "NULL".equals(o.get(nodeNamePos)) ? o.get(nodeIdPos) : o.get(nodeNamePos))
				.collect(Collectors.toList());

		IGraph graph = new ContentJSONGraph(allConnection, sentMap, new HashSet<>(nodeList), concepts);

		String st = graph.getJsonVisjsFormat();

		// add headers
		concepts.add(0, Arrays.asList(Constants.CONCEPT_HEADER));
		connections.add(0, Arrays.asList(Constants.CONNECT_HEADER));

		Map<String, Object> ret = new HashMap<>();
		ret.put("graph", st);
		ret.put(Constants.ConceptSheetName, concepts);
		ret.put(Constants.ConnectionSheetName, connections);

		return ret;
	}
}
