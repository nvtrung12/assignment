package webservice.api;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.lang3.tuple.Pair;

import webservice.Constants;

public class Utils {
	/**
	 * Utility method to get file name from HTTP header content-disposition
	 */
	public static String getFileName(Part part) {
		String contentDisp = part.getHeader("content-disposition");
		String[] tokens = contentDisp.split(";");
		for (String token : tokens) {
			if (token.trim().startsWith("filename")) {
				return token.substring(token.indexOf("=") + 2, token.length() - 1);
			}
		}
		return "";
	}

	/**
	 * store files upload to uploads folder
	 * 
	 * @param request
	 * @param resp
	 * @return
	 * @throws IOException
	 * @throws ServletException
	 */
	public static List<Pair<String, String>> storeFiles(HttpServletRequest request, HttpServletResponse resp)
			throws IOException, ServletException {
		String appPath = request.getServletContext().getRealPath("/");
		String uploadFolder = String.format("%s/%s", appPath, Constants.UPLOAD_FOLDER);

		String fileName = null;
		List<Pair<String, String>> fileNames = new LinkedList<>();
		// Get all the parts from request and write it to the file on server
		Collection<Part> p = request.getParts();
		for (Part part : request.getParts()) {
			try {
				fileName = Utils.getFileName(part);
				if (!"".equals(fileName)) {
					//TODO
					String fileN = fileName.substring(0,fileName.lastIndexOf("."));
					String uuid = UUID.randomUUID().toString().replace("-", "");
					String internalFileName = String.format("%s/%s.%s", uploadFolder, fileN, uuid+".xlsx");
					System.out.println("write for " + fileName);
					part.write(internalFileName);

					// only save if success uploaded (in case of empty upload)
					Pair<String, String> pair = Pair.of(fileName, String.format("%s.%s", fileN, uuid+".xlsx"));
					fileNames.add(pair);
				}
			} catch (Exception e) {
				System.out.println(e);
			}
		}
		return fileNames;
	}

	final public static Function<String, Integer> fConceptPos = headerName -> Arrays.asList(Constants.CONCEPT_HEADER)
			.indexOf(headerName);
	final public static Function<String, Integer> fConnectionPos = headerName -> Arrays.asList(Constants.CONNECT_HEADER)
			.indexOf(headerName);

}
