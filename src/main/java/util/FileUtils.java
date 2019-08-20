package util;

import java.io.File;

import org.apache.log4j.Logger;

public class FileUtils {
	static Logger logger = Logger.getLogger(FileUtils.class);
	
	public static void deleteFolder(File file) {
		if (file.isDirectory()) {

			// directory is empty, then delete it
			if (file.list().length == 0) {

				file.delete();
				logger.debug("Directory is deleted : " + file.getAbsolutePath());

			} else {

				// list all the directory contents
				String files[] = file.list();

				for (String temp : files) {
					// construct the file structure
					File fileDelete = new File(file, temp);

					// recursive delete
					deleteFolder(fileDelete);
				}

				// check the directory again, if empty then delete it
				if (file.list().length == 0) {
					file.delete();
					logger.debug("Directory is deleted : " + file.getAbsolutePath());
				}
			}

		} else {
			// if file, then delete it
			file.delete();
			logger.debug("File is deleted : " + file.getAbsolutePath());
		}
	}
}
