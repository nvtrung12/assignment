package webservice;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;

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
		String fileName = request.getParameter("fileName");

		String appPath = request.getServletContext().getRealPath("/");
		String downloadFolder = String.format("%s%s", appPath, Constants.DOWNLOAD_FOLDER);
		downloadFolder = String.format("%s%s%s", downloadFolder, File.separator, fileName);
		logger.debug(downloadFolder);
		for (File file : new File(downloadFolder).listFiles()) {
			if (file.getName().endsWith(".xlsx") && file.getName().indexOf(fileName) != -1) {
				PrintWriter out = response.getWriter();
				response.setContentType("APPLICATION/OCTET-STREAM");
				response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
				FileInputStream fileInputStream = new FileInputStream(file);
				int i;
				while ((i = fileInputStream.read()) != -1) {
					out.write(i);
				}
				fileInputStream.close();
				out.close();
			}
		}

	}
}