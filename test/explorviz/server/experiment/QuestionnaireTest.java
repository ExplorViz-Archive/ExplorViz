package explorviz.server.experiment;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.*;

import explorviz.server.util.JSONServiceImpl;

public class QuestionnaireTest {

	private static JSONServiceImpl service;
	private static boolean deleteLandscapeFile = false;

	@BeforeClass
	public static void initialize() {
		service = new JSONServiceImpl();
		JSONServiceImpl.createExperimentFoldersIfNotExist();
		if (!copyLandscape()) {
			fail("Couldn't copy Landscape");
		}
	}

	@AfterClass
	public static void cleanup() {
		if (deleteLandscapeFile) {
			removeLandscape();
		}
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
		question.put("answers", answers.toString());

		question.put("workingTime", 5);
		question.put("type", "freeText");
		question.put("expLandscape", "1467188123864-6247035");
		question.put("questionText", "Fragetext des Test-Questionnaires");

		questions.put(question.toString());

		questionnaire.put("questions", questions.toString());
		questionnaire.put("questionnareTitle", "Test-Experiment");

		testData.put("filename", "exp_1475325284666.json");
		testData.put("questionnaire", questionnaire.toString());

		System.out.println(testData.toString());
		try {
			service.saveQuestionnaireServer(testData.toString());
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} /*
			 * final String exp =
			 * service.getExperiment(testData.getString("filename")); final
			 * JSONObject jbo = new JSONObject(exp);
			 *
			 * final JSONArray questionnaires =
			 * jbo.getJSONArray("questionnaires");
			 *
			 * for (int i = 0; i < questionnaires.length(); i++) {
			 *
			 * final JSONObject q = questionnaires.getJSONObject(i);
			 *
			 * if (q.equals(questionnaire)) { System.out.println("true"); }
			 *
			 * }
			 */
	}

	// Helper

	private String loadJson() {
		byte[] jsonBytes = null;
		try {
			jsonBytes = Files.readAllBytes(Paths
					.get(JSONServiceImpl.EXP_FOLDER + File.separator + "exp_1475325284666.json"));

		} catch (final IOException e) {
			e.printStackTrace();
		}
		return new String(jsonBytes, StandardCharsets.UTF_8);
	}

	private static boolean copyLandscape() {
		deleteLandscapeFile = false;
		// copy one experiment from workspace to replay folder
		final Path relativePath = Paths.get("war/replay/1467188123864-6247035.expl");
		byte[] byteLandscape = null;
		try {
			byteLandscape = Files.readAllBytes(relativePath);
		} catch (final IOException e) {
			System.err.println("Couldn't read landscape from workspace. Exception: " + e);
			return false;
		}

		final Path experimentFolder = Paths.get(
				JSONServiceImpl.LANDSCAPE_FOLDER + File.separator + "1467188123864-6247035.expl");

		if (byteLandscape == null) {
			return false;
		}

		try {
			Files.write(experimentFolder, byteLandscape, StandardOpenOption.CREATE_NEW);
		} catch (final FileAlreadyExistsException e) {
			deleteLandscapeFile = false;
			return true;
		} catch (final IOException e) {
			System.err.println("Couldn't write landscape to replay folder. Exception: " + e);
			return false;
		}
		deleteLandscapeFile = true;
		return true;
	}

	private static void removeLandscape() {

		try {
			final Path landscapePath = Paths.get(JSONServiceImpl.LANDSCAPE_FOLDER + File.separator
					+ "1467188123864-6247035.expl");

			Files.delete(landscapePath);

		} catch (final IOException e) {
			System.err.println(
					"Couldn't delete file 1467188123864-6247035.expl in war/replay folder. Exception: "
							+ e);
		}
	}
}