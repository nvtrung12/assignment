package webservice.api;

import java.io.IOException;

import javax.json.Json;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

/**
 *
 * return percentage parse progress<br/>
 * Given: None
 *
 */
@WebServlet(urlPatterns = { "/api/parse_progress", "/api/v1.1/parse_progress" })
public class QueryProgress extends HttpServlet {

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
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String json = null;
		try {
			HttpSession sess = req.getSession();
			int totalSentence = sess.getAttribute("totalSentence") != null ? (int) sess.getAttribute("totalSentence") : 1;
			int numProcessed = sess.getAttribute("numProcessed") != null ? (int) sess.getAttribute("numProcessed") : 0;

			// build json to return
			json = Json.createObjectBuilder().add("percentage", 100 * numProcessed / totalSentence).build().toString();
		} catch (Exception e) {
			this.logger.error(e);
			e.printStackTrace();
			json = Json.createObjectBuilder().add("percentage", 0).build().toString();
		}

		resp.setContentType("application/json");
		resp.getWriter().println(json);
	}
}
