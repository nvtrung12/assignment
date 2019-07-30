package webservice.api;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import util.XlsxUtil;
import webservice.Constants;

/**
 *
 * Return a concept list from file (upload before under UPLOAD folder) <br/>
 * Support both get and post with same function <br/>
 * Given: dataFileName
 *
 */
@WebServlet(urlPatterns = "/api/getConceptsList")
public class QueryConceptList extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * same as post
	 * 
	 * @param req
	 * @param resp
	 * @throws ServletException
	 * @throws IOException
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// get full path
		String appPath = req.getServletContext().getRealPath("/");
		String uploadFolder = String.format("%s/%s", appPath, Constants.UPLOAD_FOLDER);

		String dataFile = req.getParameter("dataFileName");
		String path = String.format("%s%s%s", uploadFolder, File.separator, dataFile);

		String json = "[]";
		try {
			// read file and get only concepts
			List<List<String>> tmp = XlsxUtil.readXlsx(path, Constants.ConceptSheetName);
			List<String> x = tmp.stream().map(o -> o.get(2)).collect(Collectors.toList());

			// remove header line
			x.remove(0);

			// build json to return
			JsonArrayBuilder builder = Json.createArrayBuilder();
			for (String o : x)
				builder.add(o);

			json = builder.build().toString();
		} catch (Exception e) {
			json = "[]";
		}

		resp.setContentType("application/json");
		resp.getWriter().println(json);
	}

}
