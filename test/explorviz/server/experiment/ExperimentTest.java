package explorviz.server.experiment;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.sql.SQLException;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.codec.binary.Base64;
import org.json.*;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.zeroturnaround.zip.ZipUtil;

import explorviz.server.database.DBConnection;
import explorviz.server.main.FileSystemHelper;
import explorviz.server.util.JSONServiceImpl;

public class ExperimentTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private static JSONServiceImpl service;

	private static JSONObject jsonExperiment = null;
	private static JSONArray users = null;

	private static boolean doUserTest = true;

	@BeforeClass
	public static void initialize() {

		try {
			DBConnection.connect();
		} catch (final SQLException e1) {
			System.err.println(
					"Couldn't connect to database. Server might be running. User tests are disabled, since they may throw Nullpointer.");
			doUserTest = false;
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
			fail("Couldn't read experiment file. Exception: " + e);
		}

		// copy file
		FilesystemHelper.copyFile(FilesystemHelper.sourcePathExp, FilesystemHelper.destPathExp,
				false);

		if (doUserTest) {
			// create user
			final String prefix = jsonExperiment.getString("ID") + "_quest1475325290273";
			final JSONObject userData = new JSONObject(service.createUsersForQuestionnaire(10,
					prefix, jsonExperiment.getString("filename")));
			users = userData.getJSONArray("users");
		}
	}

	@AfterClass
	public static void cleanup() {
		if (FilesystemHelper.deleteLandscapeFile) {
			FilesystemHelper.removeFile(FilesystemHelper.destPathLand);
		}

		// remove users from DB
		if ((users != null) && doUserTest) {
			final int length = users.length();

			final JSONArray names = new JSONArray();

			for (int i = 0; i < length; i++) {
				final JSONObject jsonUser = users.getJSONObject(i);
				names.put(jsonUser.getString("username"));
			}

			final JSONObject data = new JSONObject();
			data.put("users", names);
			data.put("filename", jsonExperiment.getString("filename"));
			data.put("questionnareID", "quest1475325290273");

			try {
				service.removeQuestionnaireUser(data.toString());
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
	}

	///////////////////////////////////////
	// Experiment basic filesystem tests //
	///////////////////////////////////////
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
	public void testRemoveExperiment() {

		// service.removeExperiment(jsonExperiment.getString("filename"));

	}

	//////////////////////////////
	// General Experiment tests //
	//////////////////////////////
	@Test
	public void testGetExperimentFilenames() {
		final List<String> filenames = service.getExperimentFilenames();
		assertTrue("Filename list should be greater than zero", filenames.size() > 0);
	}

	@Test
	public void testGetExperimentTitles() throws IOException {
		final List<String> titles = service.getExperimentTitles();
		assertTrue(titles.size() > 0);
		assertTrue(titles.contains("Test-Experiment"));
	}

	/////////////////////////////
	// Single Experiment tests //
	/////////////////////////////
	@Test
	public void testGetExperiment() throws JSONException, IOException {
		final String experimentString = service.getExperiment(jsonExperiment.getString("filename"));

		// upload experiment because of included validation check
		final JSONObject data = new JSONObject();
		data.put("fileData", experimentString.toString());

		// try {
		// service.uploadExperiment(data.toString());
		// } catch (IOException | JSONException e) {
		// fail("testGetExperiment: Couldn't upload experiment. Exception: " +
		// e);
		// }
	}

	@Test
	public void testGetExperimentDetails() throws JSONException, IOException {

		Assume.assumeTrue(doUserTest);

		final String details = service.getExperimentDetails(jsonExperiment.getString("filename"));

		assertTrue(details.contains("title"));
		assertTrue(details.contains("filename"));
		assertTrue(details.contains("numQuestionnaires"));
		assertTrue(details.contains("landscapes"));
		assertTrue(details.contains("userCount"));
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

		final Path path = Paths.get(
				FileSystemHelper.getExplorVizDirectory() + File.separator + "experimentData.zip");

		try {
			Files.write(path, Base64.decodeBase64(zipString));
		} catch (final IOException e) {
			fail("Could not test zip download: " + e);
		}

		final File zip = path.toFile();

		final boolean containtsValidData = ZipUtil.containsEntry(zip,
				jsonExperiment.getString("filename"));

		assertTrue(containtsValidData);

		FilesystemHelper.removeFile(path.toString());
	}

	@Test
	public void testDownloadExperimentDataFailFilename() throws JSONException, IOException {

		thrown.expect(NoSuchFileException.class);
		thrown.expectMessage("/.explorviz/experiment/Super filename");

		service.downloadExperimentData("Super filename");

	}

	@Test
	public void testIsExperimentReadyToStart() throws JSONException, IOException {
		final String readyStatus = service
				.isExperimentReadyToStart(jsonExperiment.getString("filename"));

		assertEquals(readyStatus, "ready");
	}

	@Test
	public void testIsExperimentReadyToStartFailFilename() throws JSONException, IOException {
		thrown.expect(NoSuchFileException.class);
		thrown.expectMessage("/.explorviz/experiment/Super filename");

		service.isExperimentReadyToStart("Super filename");
	}

	@Test
	public void testGetExperimentTitle() throws JSONException, IOException {
		final String expTitle = service.getExperimentTitle(jsonExperiment.getString("filename"));
		assertEquals(expTitle, "Test-Experiment");
	}

	@Test
	public void testGetExperimentTitleFailFilename() throws JSONException, IOException {
		thrown.expect(NoSuchFileException.class);
		thrown.expectMessage("/.explorviz/experiment/Super filename");

		service.getExperimentTitle("Super filename");
	}

	/////////////////////////////
	// upload experiment tests //
	/////////////////////////////
	@Test
	public void testUploadExperiment() {

		final JSONObject data = new JSONObject();

		final Path path = Paths.get(FilesystemHelper.sourcePathExp);
		byte[] bytes = null;
		try {
			bytes = Files.readAllBytes(path);
		} catch (final IOException e) {
			fail("Could not test upload experiment: " + e);
		}

		data.put("fileData", "test," + DatatypeConverter.printBase64Binary(bytes));

		try {
			service.uploadExperiment(data.toString());
		} catch (IOException | JSONException e) {
			fail("testUploadExperiment: Couldn't upload experiment. Exception: " + e);
		}
	}

	@Test
	public void testUploadExperimentFailData() {

		final JSONObject data = new JSONObject();

		data.put("fileData", "FAIL");

		boolean valid = false;

		try {
			valid = service.uploadExperiment(data.toString());
		} catch (final IOException e) {
			fail("Could not test experiment upload: " + e);
		}

		assertFalse(valid);
	}

	@Test
	public void testUploadExperimentFailDataValidation() {

		final JSONObject data = new JSONObject();

		data.put("fileData", "test, FAIL");

		boolean valid = false;

		try {
			valid = service.uploadExperiment(data.toString());
		} catch (final IOException e) {
			fail("Could not test experiment upload: " + e);
		}
		assertFalse(valid);
	}

	///////////////////////////
	// Experiment user tests //
	///////////////////////////
	@Test
	public void testGetExperimentAndUsers() {

		Assume.assumeTrue(doUserTest);

		// service.getExperimentAndUsers(data);
	}

	@Test
	public void testIsUserInCurrentExperiment() throws JSONException, IOException {

		Assume.assumeTrue(doUserTest);

		final boolean status = service.isUserInCurrentExperiment("ExplorViz-Master");

		assertFalse(status);
	}

}
