package webservice.api;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import util.XlsxUtil;
import webservice.Constants;

/**
 * 
 * This is version 1.1 of merge that only merge and return combined file (in
 * downloads folder) and do not make graph user will need call /api/graph to
 * build graph in separated query<br/>
 * api<br/>
 * 
 * To merge multi files (xlsx) give fileNames (in upload folder) <br/>
 * merge and create combined in download folder, return fileName
 *
 */

@WebServlet(urlPatterns = "/api/v1.1/merge")
@MultipartConfig
public class MergeApi extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	final Logger logger = Logger.getLogger(this.getClass());

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
		String uploadFolder = String.format("%s%s", appPath, Constants.UPLOAD_FOLDER);
		String downloadFolder = String.format("%s%s", appPath, Constants.DOWNLOAD_FOLDER);

		// get from query
		String[] books = request.getParameter("books").split(":");
		String[] realNames = request.getParameter("realNames").split(":");
		logger.info("books: " + books);

		List<String> fileNames = Arrays.asList(books);
		logger.debug("size: " + fileNames.size());

		try {
			Map<String, Object> ret = mergeGraph(uploadFolder, fileNames);

			List<List<Object>> concepts = (List<List<Object>>) ret.get(Constants.ConceptSheetName);
			List<List<Object>> connections = (List<List<Object>>) ret.get(Constants.ConnectionSheetName);

			// write to combine file
			List<String> mainFileNames = Arrays.asList(realNames).stream().map(o -> {
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
			File copied = new File(String.format("%s%scombination.xlsx", downloadFolder, File.separator));
			FileUtils.copyFile(original, copied);

			String jsonReturn = Json.createObjectBuilder().add("status", 0).add("file", combinationFile).build()
					.toString();

			response.getWriter().println(jsonReturn);
		} catch (Exception e) {
			e.printStackTrace();
			String jsonReturn = Json.createObjectBuilder().add("status", 1).add("message", e.getMessage()).build()
					.toString();
			response.getWriter().println(jsonReturn);
		}
	}

	/**
	 * merge multi file into one
	 * 
	 * @param uploadFilePath
	 * @param fileNames
	 * @return
	 * @throws Exception
	 */
	private Map<String, Object> mergeGraph(String uploadFilePath, List<String> fileNames) throws Exception {
		String filePath = null;
		List<List<String>> concepts = new LinkedList<>();
		List<List<String>> connections = new LinkedList<>();

		for (String fileName : fileNames) {
			filePath = String.format("%s%s%s", uploadFilePath, File.separator, fileName);
			System.out.println("read file: " + filePath);

			try {
				List<List<String>> subConcepts = XlsxUtil.readXlsx(filePath, Constants.ConceptSheetName);
				subConcepts.remove(0); // remove header
				concepts.addAll(subConcepts);

				List<List<String>> subConnection = XlsxUtil.readXlsx(filePath, Constants.ConnectionSheetName);
				subConnection.remove(0); // remove header
				connections.addAll(subConnection);
			} catch (Exception e) {
				e.printStackTrace();
				throw new Exception(String.format("Read file %s error, please make sure correct format", fileName));
			}
		}

		// add headers
		concepts.add(0, Arrays.asList(Constants.CONCEPT_HEADER));
		connections.add(0, Arrays.asList(Constants.CONNECT_HEADER));

		Map<String, Object> ret = new HashMap<>();
		ret.put(Constants.ConceptSheetName, concepts);
		ret.put(Constants.ConnectionSheetName, connections);

		return ret;
	}
}
