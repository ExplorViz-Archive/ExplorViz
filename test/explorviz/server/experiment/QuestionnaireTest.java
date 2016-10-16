package explorviz.server.experiment;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.sql.SQLException;

import org.json.*;
import org.junit.*;

import explorviz.server.database.DBConnection;
import explorviz.server.util.JSONServiceImpl;

public class QuestionnaireTest {

	private static JSONServiceImpl service;
	private static boolean deleteLandscapeFile = false;
	private static JSONObject jsonExperiment = null;

	private static String sourcePathLand = "war/experiment/Test-Data/1467188123864-6247035.expl";
	private static String destPathLand = JSONServiceImpl.LANDSCAPE_FOLDER + File.separator
			+ "1467188123864-6247035.expl";
	private static String destPathExp = JSONServiceImpl.EXP_FOLDER + File.separator
			+ "exp_test_file.json";

	private static String sourcePathExp = "war/experiment/Test-Data/exp_test_file.json";

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

		// create basic experiment file
		final JSONObject newExperiment = new JSONObject();
		newExperiment.put("lastStarted", 1476274414793L);
		newExperiment.put("filename", "exp_test_file.json");
		newExperiment.put("ID", "exp1475325284666");
		newExperiment.put("lastModified", 1476274434548L);
		newExperiment.put("lastEnded", 1476274434548L);
		newExperiment.put("questionnaires", new JSONArray());

		try {
			service.saveJSONOnServer(newExperiment.toString());
		} catch (IOException | JSONException e) {
			fail("Couldn't create new experiment for test. Exception: " + e);
		}
	}

	@AfterClass
	public static void cleanup() {
		if (deleteLandscapeFile) {
			removeFile(destPathLand);
		}
		removeFile(destPathExp);
	}

	// Tests
	@Test
	public void testSaveQuestionnaireServer() {

		final JSONObject testData = new JSONObject();
		final JSONObject questionnaire = new JSONObject();

		questionnaire.put("questionnareID", "quest1475325290274");

		final JSONArray questions = new JSONArray();

		final JSONObject question = new JSONObject();
		question.put("expApplication", "");

		final JSONArray answers = new JSONArray();
		answers.put(new JSONObject("{answerText: Antwort 1, checkboxChecked: false}"));
		answers.put(new JSONObject("{answerText: Antwort 2, checkboxChecked: false}"));
		answers.put(new JSONObject("{answerText: Antwort 3, checkboxChecked: false}"));
		answers.put(new JSONObject("{answerText: Antwort 4, checkboxChecked: false}"));

		question.put("answers", answers);

		question.put("workingTime", 5);
		question.put("type", "freeText");
		question.put("expLandscape", "1467188123864-6247035");
		question.put("questionText", "Fragetext des Test-Questionnaires");

		questions.put(question);

		questionnaire.put("questions", questions);
		questionnaire.put("questionnareTitle", "Test-Questionnaire");

		testData.put("filename", "exp_test_file.json");
		testData.put("questionnaire", questionnaire);

		// Saves created questionnaire on server
		try {
			service.saveQuestionnaireServer(testData.toString());
		} catch (final IOException e) {
			e.printStackTrace();
		}

		// reads the whole experiment including the questionnaire saved above
		String createdObjectString = null;
		try {
			createdObjectString = service.getExperiment(testData.getString("filename"));
		} catch (final JSONException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		final JSONObject createdObject = new JSONObject(createdObjectString)
				.getJSONArray("questionnaires").getJSONObject(0);

		final JSONObject compareData = jsonExperiment.getJSONArray("questionnaires")
				.getJSONObject(0);

		// both objects should be similar
		assertTrue(createdObject.similar(compareData));
	}

	@Test(expected = IOException.class)
	public void testRemoveQuestionnaireIOException() throws JSONException, IOException {
		final JSONObject data = new JSONObject();
		data.put("filename", "ExplorViz is nice");
		data.put("questionnareID", "123");

		service.removeQuestionnaire(data.toString());
	}

	// @Test(expected = JSONException.class)
	public void testRemoveQuestionnaireJSONException() {
		final JSONObject data = new JSONObject();
		data.put("filename", "exp_1475325284666.json");
		data.put("questionnareID", "123");

		// try catch for debuggin, change to throw
		// value of key questionnaire is not an
		// JSONObject, because testSave-Test
		// saves value as String. But why?

		try {
			service.removeQuestionnaire(data.toString());
		} catch (JSONException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testRemoveQuestionnaire() {
		// Test auf Funktionalität
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