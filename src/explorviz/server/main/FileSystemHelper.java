package explorviz.server.main;

import java.io.File;

public class FileSystemHelper {
	public static String getExplorVizDirectory() {

		// setting for dockerhub image - saving below temporary system directory
		final String property = "java.io.tmpdir";

		final String tempDir = System.getProperty(property);
		// final String homefolder = System.getProperty("user.home");
		// final String filePath = homefolder + "/.explorviz";
		final String filePath = tempDir + "/.explorviz";

		new File(filePath).mkdir();

		return filePath;
	}
}
