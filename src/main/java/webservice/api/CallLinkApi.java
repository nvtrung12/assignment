package webservice.api;

import java.io.IOException;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

@WebServlet(urlPatterns = { "/api/callLink" })
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
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// initial session for this user
		request.getSession();
		List<Pair<String, String>> out = Utils.storeFiles(request, response);

		JsonArrayBuilder arrJsonBuilder = Json.createArrayBuilder();

		for (Pair<String, String> o : out) {
			JsonObject js = Json.createObjectBuilder().add("fileName", o.getLeft()).add("storedFileName", o.getRight())
					.build();
			arrJsonBuilder.add(js);
		}
		String jsonReturn = Json.createObjectBuilder().add("status", 0).add("files", arrJsonBuilder.build()).build()
				.toString();

		response.setContentType("application/json");
		response.getWriter().println(jsonReturn);
	}
}
