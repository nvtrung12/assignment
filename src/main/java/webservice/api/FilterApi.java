package webservice.api;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import util.XlsxUtil;
import webservice.Constants;
import webservice.GraphApiHelper;

/**
 * 
 * 
 * api<br/>
 * from a file (uploaded or in downloads, uploads folder) make and return a
 * graph (json) to show in browser, and other info
 * 
 * input: fileName (related path), type (default or name of class) of graph
 *
 */

@WebServlet(urlPatterns = { "/api/filter", "/api/v1.1/filter" })
@MultipartConfig
public class FilterApi extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	final Logger logger = Logger.getLogger(this.getClass());

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doFilter(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doFilter(request, response);
	}

	private void doFilter(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType("application/json");

		String uuid = UUID.randomUUID().toString().replace("-", "");
		String appPath = this.getServletContext().getRealPath("/");
		String ccName = String.format("filter_concepts.%s.xlsx", uuid);

		String uploadFolder = String.format("%s%s", appPath, Constants.UPLOAD_FOLDER);
		String downloadFolder = String.format("%s%s", appPath, Constants.DOWNLOAD_FOLDER);
		String cpath = String.format("%s%s%s", downloadFolder, File.separator, ccName);

		// get from query
		String fileToProcess = request.getParameter("uploaded_file");
		logger.debug("fileName: " + fileToProcess);
		String fileToProcessPath = String.format("%s%s%s", uploadFolder, File.separator, fileToProcess);

		try {

			// TODO get filter and nhops
			String filters = request.getParameter("filters").trim();
			logger.debug("filters: " + filters);

			// default if not given then show full
			if ("".equals(filters))
				filters = ":2";

			if (null == filters)
				throw new Exception("No filter given");

			Map<String, Integer> info = new HashMap<>();

			try {
				String[] tmp = filters.split(SEP);
				List<String[]> tmp1 = Arrays.asList(tmp).stream().map(o -> o.split(FSEP)).collect(Collectors.toList());

				for (String[] o : tmp1) {
					info.put(o[0], Integer.parseInt(o[1].trim()));
				}

			} catch (Exception e) {
				e.printStackTrace();
				throw new Exception("Filter wrong format");
			}

			// filter
			Map<String, Object> ret = GraphApiHelper.filterGraph(fileToProcessPath, info);

			// write
			List<List<Object>> concepts = (List<List<Object>>) ret.get(Constants.ConceptSheetName);
			List<List<Object>> connection = (List<List<Object>>) ret.get(Constants.ConnectionSheetName);

			// add header
			concepts.add(0, Arrays.asList(Constants.CONCEPT_HEADER));
			connection.add(0, Arrays.asList(Constants.CONNECT_HEADER));

			XlsxUtil.writeXlsx(cpath, Constants.ConceptSheetName, concepts);
			XlsxUtil.writeRowsXlsxAppend(cpath, Constants.ConnectionSheetName, connection);
			XlsxUtil.writeRowsXlsxAppend(cpath, Constants.META_SHEET_NAME, new LinkedList<>());

			// return json
			// filter file name in downloads folder
			// must call separated api to show graph
			String jsonReturn = Json.createObjectBuilder().add("status", 0).add("filterFileName", ccName).build()
					.toString();
			response.getWriter().println(jsonReturn);
		} catch (Exception e) {
			e.printStackTrace();
			String jsonReturn = Json.createObjectBuilder().add("status", 1).add("message", e.getMessage()).build()
					.toString();
			response.getWriter().println(jsonReturn);
		}
	}

	protected final String SEP = "\\|"; // re for | then must be has \\
	protected final String FSEP = ":";

}
