package webservice;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

@WebServlet("/download")
public class DownloadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	final Logger logger = Logger.getLogger(this.getClass());

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		String fileName = request.getParameter("fileName");

		response.setContentType("APPLICATION/OCTET-STREAM");
		response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

		String appPath = request.getServletContext().getRealPath("/");
		String downloadFolder = String.format("%s%s", appPath, Constants.DOWNLOAD_FOLDER);
		logger.debug(downloadFolder);

		FileInputStream fileInputStream = null;
		if (fileName.equals(Constants.PHRASE_LIST_FILE_DEFAULT)
				|| fileName.equals(Constants.IGNORED_CONCEPTS_FILE_DEFAULT)) {

			try {
				ClassLoader cl = this.getClass().getClassLoader();
				File file = Paths.get(cl.getResource(fileName).toURI()).toFile();
				fileInputStream = new FileInputStream(file);
			} catch (Exception e) {
			}
		} else {
			fileInputStream = new FileInputStream(String.format("%s%s%s", downloadFolder, File.separator, fileName));
		}

		int i;
		while ((i = fileInputStream.read()) != -1) {
			out.write(i);
		}
		fileInputStream.close();
		out.close();
	}
}