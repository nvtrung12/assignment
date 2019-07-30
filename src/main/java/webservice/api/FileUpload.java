package webservice.api;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.lang3.tuple.Pair;

import webservice.Constants;

/**
 * 
 * 
 * upload file/multi-file and return json {status:..,
 * internalLink:[{fileName,internalFileName}]}
 *
 */

@WebServlet(urlPatterns = "/api/upload")
@MultipartConfig
public class FileUpload extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest reqest, HttpServletResponse response) throws ServletException, IOException {
		response.getWriter().println("must post method");
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
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

	/**
	 * Use Utils.storeFiles instead
	 * 
	 * @param request
	 * @param resp
	 * @return
	 * @throws IOException
	 * @throws ServletException
	 */
	@Deprecated
	private List<Pair<String, String>> storeFiles(HttpServletRequest request, HttpServletResponse resp)
			throws IOException, ServletException {
		String appPath = request.getServletContext().getRealPath("/");
		String uploadFolder = String.format("%s/%s", appPath, Constants.UPLOAD_FOLDER);

		String fileName = null;
		List<Pair<String, String>> fileNames = new LinkedList<>();
		// Get all the parts from request and write it to the file on server
		for (Part part : request.getParts()) {
			try {
				fileName = Utils.getFileName(part);
				if (!"".equals(fileName)) {
					String uuid = UUID.randomUUID().toString().replace("-", "");
					String internalFileName = String.format("%s/%s.%s", uploadFolder, uuid, fileName);
					System.out.println("write for " + fileName);
					part.write(internalFileName);

					// only save if success uploaded (in case of empty upload)
					Pair<String, String> pair = Pair.of(fileName, String.format("%s.%s", uuid, fileName));
					fileNames.add(pair);
				}
			} catch (Exception e) {
				System.out.println(e);
			}
		}
		return fileNames;
	}
}
