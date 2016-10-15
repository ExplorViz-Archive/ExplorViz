package explorviz.server.experiment;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

import explorviz.server.util.JSONServiceImpl;

class FilesystemHelper {

	protected static boolean deleteLandscapeFile = false;

	protected static String sourcePathLand = "war/experiment/Test-Data/1467188123864-6247035.expl";
	protected static String destPathLand = JSONServiceImpl.LANDSCAPE_FOLDER + File.separator
			+ "1467188123864-6247035.expl";
	protected static String sourcePathExp = "war/experiment/Test-Data/exp_1475325284666.json";

	protected static void removeFile(final String removePath) {

		try {
			final Path path = Paths.get(removePath);

			Files.delete(path);

		} catch (final IOException e) {
			System.err.println("Couldn't delete file. Exception: " + e);
		}
	}

	protected static boolean copyFile(final String sourcePath, final String destPath,
			final boolean isLandscape) {

		if (isLandscape) {
			deleteLandscapeFile = false;
		}

		// get file from sourcePath
		final Path relativePath = Paths.get(sourcePath);
		byte[] bytes = null;
		try {
			bytes = Files.readAllBytes(relativePath);
		} catch (final IOException e) {
			System.err.println("Couldn't read file from workspace. Exception: " + e);
			return false;
		}

		final Path sourceFolder = Paths.get(destPath);

		if (bytes == null) {
			return false;
		}

		try {
			Files.write(sourceFolder, bytes, StandardOpenOption.CREATE_NEW);
		} catch (final FileAlreadyExistsException e) {
			if (isLandscape) {
				deleteLandscapeFile = false;
			}

			return true;
		} catch (final IOException e) {
			System.err.println("Couldn't write file to folder. Exception: " + e);
			return false;
		}
		if (isLandscape) {
			deleteLandscapeFile = true;
		}

		return true;
	}

}
