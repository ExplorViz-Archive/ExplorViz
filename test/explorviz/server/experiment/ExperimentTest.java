package explorviz.server.experiment;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.*;

import explorviz.server.util.JSONServiceImpl;

public class ExperimentTest {

	private static JSONServiceImpl service;
	private static boolean deleteLandscapeFile = false;
	private static JSONObject jsonExperiment = null;

	private static String sourcePathLand = "war/experiment/Test-Data/1467188123864-6247035.expl";
	private static String destPathLand = JSONServiceImpl.LANDSCAPE_FOLDER + File.separator
			+ "1467188123864-6247035.expl";

	private static String sourcePathExp = "war/experiment/Test-Data/exp_1475325284666.json";

	@BeforeClass
	public static void initialize() {
		service = new JSONServiceImpl();
		JSONServiceImpl.createExperimentFoldersIfNotExist();

		// copy Landscape to replay folder
		if (!copyFile(sourcePathLand, destPathLand, true)) {
			fail("Couldn't copy Landscape");
		}

		// get and parse experiment
		try {
			final byte[] experimentBytes = Files.readAllBytes(Paths.get(sourcePathExp));
			jsonExperiment = new JSONObject(new String(experimentBytes));
		} catch (final IOException e) {
			System.err.println("Couldn't read experiment file. Exception: " + e);
		}
	}

	@AfterClass
	public static void cleanup() {
		if (deleteLandscapeFile) {
			removeFile(destPathLand);
		}
	}

	// Tests

	@Test
	public void testGetExperimentFilenames() {

		// check if at least one landscape exists in folder
		final List<String> filenames = service.getExperimentFilenames();

		assertTrue("Filename list should be greater or equal zero", filenames.size() > 0);
	}

	@Test
	public void testSaveJSONOnServer() {
		try {
			service.saveJSONOnServer(jsonExperiment.toString());
		} catch (final JSONException e) {
			fail("Couldn't save experiment. Exception: " + e);
		} catch (final IOException e) {
			fail("Couldn't save experiment. Exception: " + e);
		}
	}

	@Test
	public void testUploadExperiment() {

		final JSONObject data = new JSONObject();
		data.put("fileData", jsonExperiment.toString());

		try {
			service.uploadExperiment(data.toString());
		} catch (final JSONException e) {
			fail("Couldn't upload experiment. Exception: " + e);
		} catch (final IOException e) {
			fail("Couldn't upload experiment. Exception: " + e);
		}
	}

	@Test
	public void testDuplicateExperiment() {

		final List<String> previousFilenames = service.getExperimentFilenames();
		try {
			service.duplicateExperiment(jsonExperiment.getString("filename"));
		} catch (final IOException e) {
			fail("Couldn't duplicate experiment. Exception: " + e);
		}

		final List<String> afterFilenames = service.getExperimentFilenames();

		assertTrue(previousFilenames.size() < afterFilenames.size());
	}

	@Test
	public void testGetExperiment() {
		final String experimentString = service.getExperiment(jsonExperiment.getString("filename"));

		// upload experiment because of included validation check
		final JSONObject data = new JSONObject();
		data.put("fileData", jsonExperiment.toString());

		try {
			service.uploadExperiment(data.toString());
		} catch (final JSONException e) {
			fail("Couldn't upload experiment. Exception: " + e);
		} catch (final IOException e) {
			fail("Couldn't upload experiment. Exception: " + e);
		}
	}

	@Test
	public void testGetExperimentTitles() {

	}

	@Test
	public void testGetExperimentDetails() {

	}

	@Test
	public void testDownloadExperimentData() {

	}

	@Test
	public void testGetExperimentTitlesAndFilenames() {

	}

	@Test
	public void testIsExperimentReadyToStart() {

	}

	@Test
	public void testGetExperimentTitle() {

	}

	@Test
	public void testUploadLandscape() {

	}

	@Test
	public void testGetExperimentAndUsers() {

	}

	@Test
	public void testIsUserInCurrentExperiment() {

	}

	@Test
	public void testRemoveExperiment() {

	}

	// Helper

	private static boolean copyFile(final String sourcePath, final String destPath,
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

	private static void removeFile(final String removePath) {

		try {
			final Path path = Paths.get(removePath);

			Files.delete(path);

		} catch (final IOException e) {
			System.err.println("Couldn't delete file. Exception: " + e);
		}
	}

}
