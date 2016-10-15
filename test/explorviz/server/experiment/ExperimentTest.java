package explorviz.server.experiment;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.*;
import java.sql.SQLException;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.*;
import org.junit.rules.ExpectedException;

import explorviz.server.database.DBConnection;
import explorviz.server.util.JSONServiceImpl;

public class ExperimentTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private static JSONServiceImpl service;

	private static JSONObject jsonExperiment = null;

	@BeforeClass
	public static void initialize() {

		try {
			DBConnection.connect();
		} catch (final SQLException e1) {
			System.err.println("Couldn't connect to database. Some tests will fail.");
		}

		service = new JSONServiceImpl();
		JSONServiceImpl.createExperimentFoldersIfNotExist();

		// copy Landscape to replay folder
		if (!FilesystemHelper.copyFile(FilesystemHelper.sourcePathLand,
				FilesystemHelper.destPathLand, true)) {
			fail("Couldn't copy Landscape");
		}

		// get and parse experiment
		try {
			final byte[] experimentBytes = Files
					.readAllBytes(Paths.get(FilesystemHelper.sourcePathExp));
			jsonExperiment = new JSONObject(new String(experimentBytes));
		} catch (final IOException e) {
			System.err.println("Couldn't read experiment file. Exception: " + e);
		}
	}

	@AfterClass
	public static void cleanup() {
		if (FilesystemHelper.deleteLandscapeFile) {
			FilesystemHelper.removeFile(FilesystemHelper.destPathLand);
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
		} catch (IOException | JSONException e) {
			fail("Couldn't save experiment. Exception: " + e);
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
	public void testGetExperiment() throws JSONException, IOException {
		final String experimentString = service.getExperiment(jsonExperiment.getString("filename"));

		// upload experiment because of included validation check
		final JSONObject data = new JSONObject();
		data.put("fileData", experimentString.toString());

		try {
			service.uploadExperiment(data.toString());
		} catch (IOException | JSONException e) {
			fail("testGetExperiment: Couldn't upload experiment. Exception: " + e);
		}
	}

	@Test
	public void testGetExperimentTitles() throws IOException {
		final List<String> titles = service.getExperimentTitles();
		assertTrue(titles.size() > 0);
		assertTrue(titles.contains("Test-Experiment"));
	}

	@Test
	public void testGetExperimentDetails() throws JSONException, IOException {

		testSaveJSONOnServer();

		final String details = service.getExperimentDetails(jsonExperiment.getString("filename"));

		assertTrue(details.contains("title"));
		assertTrue(details.contains("filename"));
		assertTrue(details.contains("numQuestionnaires"));
		assertTrue(details.contains("landscapes"));
		// assertTrue(details.contains("userCount"));
		assertTrue(details.length() > 20);
	}

	@Test
	public void testDownloadExperimentData() {

		String zipString = null;

		try {
			zipString = service.downloadExperimentData(jsonExperiment.getString("filename"));
		} catch (JSONException | IOException e) {
			fail("Couldn't test zip download. Exception: " + e);
		}

		assertTrue(zipString.length() > 50);

	}

	@Test
	public void testFailDownloadExperimentData() {

		String zipString = null;

		try {
			zipString = service.downloadExperimentData(jsonExperiment.getString("filename"));
		} catch (JSONException | IOException e) {
			fail("Couldn't test zip download. Exception: " + e);
		}

		assertTrue(zipString.length() > 50);

	}

	@Test
	public void testZipDownloadFail() throws IOException {
		thrown.expect(NoSuchFileException.class);
		service.downloadExperimentData("ExplorViz is nice");
	}

	@Test
	public void testIsExperimentReadyToStart() throws JSONException, IOException {
		final String readyStatus = service
				.isExperimentReadyToStart(jsonExperiment.getString("filename"));

		assertEquals(readyStatus, "ready");

	}

	@Test
	public void testGetExperimentTitle() throws JSONException, IOException {
		final String expTitle = service.getExperimentTitle(jsonExperiment.getString("filename"));
		assertEquals(expTitle, "Test-Experiment");
	}

	/////////////////////////////
	// upload experiment tests //
	/////////////////////////////
	@Test
	public void testUploadExperiment() {

		final JSONObject data = new JSONObject();
		data.put("fileData", jsonExperiment.toString());

		try {
			service.uploadExperiment(data.toString());
		} catch (IOException | JSONException e) {
			fail("testUploadExperiment: Couldn't upload experiment. Exception: " + e);
		}
	}

	@Test
	public void testGetExperimentAndUsers() {
		// service.getExperimentAndUsers(data);
	}

	@Test
	public void testIsUserInCurrentExperiment() throws JSONException, IOException {
		final boolean status = service.isUserInCurrentExperiment("ExplorViz-Master");

		assertFalse(status);
	}

	// @Test
	// public void testRemoveExperiment() {
	//
	// service.removeExperiment(jsonExperiment.getString("filename"));
	//
	// }

}
