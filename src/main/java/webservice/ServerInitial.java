package webservice;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * for initial when server start, make uploads, downloads folder
 * 
 * any other task
 *
 */
@WebServlet(urlPatterns = "/init", loadOnStartup = 1)
public class ServerInitial extends HttpServlet {
	private static final long serialVersionUID = -4751096228274971485L;
	final Logger logger = Logger.getLogger(this.getClass());

	@Override
	protected void doGet(HttpServletRequest reqest, HttpServletResponse response) throws ServletException, IOException {
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
	}

	@Override
	public void init() throws ServletException {
		System.out.println("Servlet " + this.getServletName() + " has started! Load models");
		try {
			String appPath = this.getServletContext().getRealPath("/");
			String uploadFolder = String.format("%s%s", appPath, Constants.UPLOAD_FOLDER);
			String downloadFolder = String.format("%s%s", appPath, Constants.DOWNLOAD_FOLDER);

			// make download folder if not exits
			File fileSaveDir = new File(downloadFolder);
			if (!fileSaveDir.exists())
				fileSaveDir.mkdirs();

			// make upload folder if not exits
			File uploadF = new File(uploadFolder);
			if (!uploadF.exists())
				uploadF.mkdirs();
		} catch (Exception e) {
			// just omit
		}
	}

	@Override
	public void destroy() {
		System.out.println("Servlet " + this.getServletName() + " has stopped");
	}
}