package webservice.api;

import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import webservice.Constants;

@WebServlet(urlPatterns = { "/api/callLink" })
@MultipartConfig
public class CallLinkApi extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	final Logger logger = Logger.getLogger(this.getClass());

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
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String json = null;
		// initial session for this userebookFileC
		request.getSession();
		String ebookFile = request.getParameter("ebookFileC");

		String appPath = request.getServletContext().getRealPath("/");
		String uploadFolder = String.format("%s/%s", appPath, "uploads/");
		String downloadFolder = String.format("%s%s", appPath, "downloads/");
		ProbabilatyProcess pp = new ProbabilatyProcess();
		String result = null ;
		try {
			result = pp.probability(ebookFile, uploadFolder ,downloadFolder);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		json = Json.createObjectBuilder().add("fileDownload", result).add("fileName", ebookFile).build().toString();
		response.setContentType("application/json");
		response.getWriter().println(json);
	}
}
