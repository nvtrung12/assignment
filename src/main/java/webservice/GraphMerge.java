package webservice;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import graph.ContentJSONGraph;
import graph.IGraph;
import util.XlsxUtil;
import webservice.api.Utils;

/**
 * @deprecated not use any more, remove in future <br/>
 *             use /api/merge instead
 * 
 *             To merge multi files (xlsx)
 *
 */
@WebServlet(urlPatterns = "/merge")
@MultipartConfig
public class GraphMerge extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// response.getWriter().println("must post method");
		request.getRequestDispatcher("combination.jsp").forward(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
		processMerge(request, resp);
	}

	private void processMerge(HttpServletRequest request, HttpServletResponse resp)
			throws IOException, ServletException {
		String uuid = UUID.randomUUID().toString().replace("-", "");
		String uploadFilePath = String.format("/tmp/%s", uuid);

		// store file, get fileNames
		List<String> fileNames = storeFiles(request, resp, uploadFilePath);
		try {
			Map<String, Object> ret = mergeGraph(uploadFilePath, fileNames);

			String graphjs = (String) ret.get("graph");
			List<List<Object>> concepts = (List<List<Object>>) ret.get(Constants.ConceptSheetName);
			List<List<Object>> connections = (List<List<Object>>) ret.get(Constants.ConnectionSheetName);

			// write to combine file
			String combineFile = String.format("/tmp/%s.xlsx", uuid);
			XlsxUtil.writeXlsx(combineFile, Constants.ConceptSheetName, concepts);
			XlsxUtil.writeRowsXlsxAppend(combineFile, Constants.ConnectionSheetName, connections);
			XlsxUtil.writeRowsXlsxAppend(combineFile, Constants.META_SHEET_NAME, new LinkedList<>());

			request.setAttribute("gr_data", graphjs);
			request.setAttribute("message", "process done");
			request.setAttribute("combinedConceptFile", String.format("%s.xlsx", uuid));
		} catch (Exception e) {
			e.printStackTrace();
			request.setAttribute("message", e.getMessage());
		}
		request.getRequestDispatcher("combination.jsp").forward(request, resp);
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

		List<Object[]> sub_graph = new LinkedList<>();
		for (List<String> lst : connections) {
			sub_graph.add(lst.toArray());
		}

		// build sentence map: sentID -> sentence content from concepts
		Map<String, String> sentMap = new HashMap<>();
		final Function<String, Integer> fConceptPos = hname -> Arrays.asList(Constants.CONCEPT_HEADER).indexOf(hname);
		for (List<String> lst : concepts) {
			// sentMap.put(one.get(0), one.get(5));
			System.out.println("Test pos: " + fConceptPos.apply(Constants.DISPLAY_NAME_HEADER_NAME));
			sentMap.put(graph.Utils.extractSentenceIndex(lst.get(fConceptPos.apply(Constants.NODE_ID_HEADER_NAME))),
					lst.get(fConceptPos.apply(Constants.DISPLAY_NAME_HEADER_NAME)));

		}

		IGraph graph = new ContentJSONGraph(sub_graph, sentMap);

		String st = graph.getJsonVisjsFormat();

		Map<String, Object> ret = new HashMap<>();
		ret.put("graph", st);
		ret.put(Constants.ConceptSheetName, concepts);
		ret.put(Constants.ConnectionSheetName, connections);

		return ret;
	}

	private List<String> storeFiles(HttpServletRequest request, HttpServletResponse resp, String uploadFilePath)
			throws IOException, ServletException {
		// creates the save directory if it does not exists
		File fileSaveDir = new File(uploadFilePath);
		if (!fileSaveDir.exists()) {
			fileSaveDir.mkdirs();
		}

		String fileName = null;
		List<String> fileNames = new LinkedList<>();
		// Get all the parts from request and write it to the file on server
		for (Part part : request.getParts()) {
			try {
				fileName = Utils.getFileName(part);
				if (!"".equals(fileName)) {
					System.out.println("write for " + fileName);
					part.write(uploadFilePath + File.separator + fileName);
					fileNames.add(fileName); // only save if success uploaded (in case of empty upload)
				}
			} catch (Exception e) {
				System.out.println(e);
			}
		}
		return fileNames;
	}
}
