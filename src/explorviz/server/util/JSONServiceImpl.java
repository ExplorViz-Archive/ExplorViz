package explorviz.server.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.codec.binary.Base64;
import org.json.*;
import org.zeroturnaround.zip.ZipUtil;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import explorviz.server.database.DBConnection;
import explorviz.server.main.Configuration;
import explorviz.server.main.FileSystemHelper;
import explorviz.shared.auth.User;
import explorviz.shared.experiment.Question;
import explorviz.visualization.engine.Logging;
import explorviz.visualization.experiment.services.JSONService;

public class JSONServiceImpl extends RemoteServiceServlet implements JSONService {

	private static final long serialVersionUID = 6576514774419481521L;

	// private static String FULL_FOLDER =
	// FileSystemHelper.getExplorVizDirectory() + File.separator;

	private static String EXP_FOLDER = FileSystemHelper.getExplorVizDirectory() + File.separator
			+ "experiment";

	private static String EXP_ANSWER_FOLDER = FileSystemHelper.getExplorVizDirectory()
			+ File.separator + "experiment" + File.separator + "answers";

	private static String LANDSCAPE_FOLDER = FileSystemHelper.getExplorVizDirectory()
			+ File.separator + "replay";

	private static String Tracking_FOLDER = FileSystemHelper.getExplorVizDirectory()
			+ File.separator + "usertracking";

	/////////////////
	// RPC Methods //
	/////////////////

	@Override
	public void saveJSONOnServer(final String json) throws IOException {
		JSONObject jsonObj = null;
		try {
			jsonObj = new JSONObject(json);
		} catch (final JSONException e) {
			System.err.println("Method: saveJSONOnServer; Couldn't create JSONObject");
		}

		if (jsonObj == null) {
			return;
		}

		final String filename = jsonObj.getString("filename");
		final Path experimentFolder = Paths.get(EXP_FOLDER + File.separator + filename);

		final byte[] bytes = jsonObj.toString(4).getBytes(StandardCharsets.UTF_8);

		try {
			Files.write(experimentFolder, bytes, StandardOpenOption.CREATE_NEW);
		} catch (final java.nio.file.FileAlreadyExistsException e) {
			Files.write(experimentFolder, bytes, StandardOpenOption.TRUNCATE_EXISTING);
		}
	}

	@Override
	public void saveQuestionnaireServer(final String data) throws IOException {
		final JSONObject filenameAndQuestionnaireTitle = new JSONObject(data);
		final String filename = filenameAndQuestionnaireTitle.keySet().iterator().next();

		final String jsonString = getExperiment(filename);
		final JSONObject jsonExperiment = new JSONObject(jsonString);

		final JSONObject questionnaire = new JSONObject(
				filenameAndQuestionnaireTitle.getString(filename));

		final JSONArray questionnaires = jsonExperiment.getJSONArray("questionnaires");

		boolean questionnaireUpdated = false;

		for (int i = 0; i < questionnaires.length(); i++) {

			final JSONObject questionnaireTemp = questionnaires.getJSONObject(i);

			// find questionnaire to update
			if (questionnaireTemp.has("questionnareTitle")
					&& questionnaireTemp.getString("questionnareTitle")
							.equals(questionnaire.getString("questionnareTitle"))) {

				questionnaireUpdated = true;

				questionnaires.remove(i);
				questionnaires.put(i, questionnaire);
			}
		}

		if (!questionnaireUpdated) {
			// not added => new questionnaire
			questionnaires.put(questionnaire.toString());
		}

		try {
			saveJSONOnServer(jsonExperiment.toString(4));
		} catch (final IOException e) {
			System.err.println("Couldn't save experiment when removing questionnaire.");
		}

	}

	@Override
	public List<String> getExperimentFilenames() {
		final List<String> filenames = new ArrayList<String>();
		final File directory = new File(EXP_FOLDER);

		// final File[] fList = directory.listFiles();
		// Filters Files only; no folders are added
		final File[] fList = directory.listFiles(new FileFilter() {
			@Override
			public boolean accept(final File pathname) {
				final String name = pathname.getName().toLowerCase();
				return name.endsWith(".json") && pathname.isFile();
			}
		});
		if (fList != null) {
			for (final File f : fList) {
				filenames.add(f.getName());
			}
		}
		return filenames;
	}

	@Override
	public List<String> getExperimentTitles() {
		final List<String> titles = new ArrayList<String>();
		final File directory = new File(EXP_FOLDER);

		// Filters Files only; no folders are added
		final File[] fList = directory.listFiles(new FileFilter() {
			@Override
			public boolean accept(final File pathname) {
				final String name = pathname.getName().toLowerCase();
				return name.endsWith(".json") && pathname.isFile();
			}
		});

		if (fList != null) {
			for (final File f : fList) {
				final String filename = f.getName();
				final String json = readExperiment(filename);
				final JSONObject jsonObj = new JSONObject(json);
				titles.add(jsonObj.getString("title"));
			}
		}
		return titles;
	}

	@Override
	public Question[] getQuestionsOfExp(final String filename) {

		// TODO depending on user of questionnaire

		final ArrayList<Question> questions = new ArrayList<Question>();
		//
		// final String jsonString = readExperiment(filename);
		// final JSONArray jsonQuestionnaires = new
		// JSONObject(jsonString).getJSONArray("questionnaires");
		//
		// final int length = jsonQuestionnaires.length();
		//
		// String text;
		// String type;
		// String[] answers;
		// ArrayList<String> corrects;
		// int procTime;
		// long timestamp;
		//
		// for (int i = 0; i < length; i++) {
		//
		// final JSONObject jsonObj = jsonQuestions.getJSONObject(i);
		//
		// text = jsonObj.getString("questionText");
		// type = jsonObj.getString("type");
		//
		// procTime = Integer.parseInt(jsonObj.getString("workingTime"));
		// // timestamp = Long.parseLong(jsonObj.getString("expLandscape"));
		// timestamp = 1L;
		//
		// final JSONArray correctsArray = jsonObj.getJSONArray("answers");
		// final int lengthQuestions = correctsArray.length();
		//
		// corrects = new ArrayList<String>();
		// answers = new String[lengthQuestions];
		//
		// for (int j = 0; j < lengthQuestions; j++) {
		// final JSONObject jsonAnswer = correctsArray.getJSONObject(j);
		//
		// answers[j] = jsonAnswer.keySet().iterator().next();
		//
		// if (jsonAnswer.get(answers[j]).toString().equals("true")) {
		//
		// corrects.add(answers[j]);
		//
		// }
		//
		// }
		//
		// final Question question = new Question(i, type, text, answers,
		// corrects.toArray(new String[0]), procTime, timestamp);
		//
		// questions.add(question);
		// }

		return questions.toArray(new Question[0]);
	}

	@Override
	public String getExperiment(final String filename) {
		final String jsonString = readExperiment(filename);

		return jsonString;
	}

	@Override
	public void removeExperiment(final String filename) {

		final Path experimentFile = Paths.get(EXP_FOLDER + File.separator + filename);

		try {
			Files.delete(experimentFile);
		} catch (final IOException e) {
			Logging.log("Experiment " + filename + " could not be removed");
		}
	}

	@Override
	public String getExperimentDetails(final String filename) {
		final String jsonString = getExperiment(filename);

		final JSONObject jsonExperiment = new JSONObject(jsonString);

		final JSONObject jsonDetails = new JSONObject();

		jsonDetails.putOnce("title", jsonExperiment.get("title"));

		jsonDetails.putOnce("prefix", jsonExperiment.get("prefix"));

		jsonDetails.putOnce("filename", jsonExperiment.get("filename"));

		final int numberOfQuestionnaires = jsonExperiment.getJSONArray("questionnaires").length();
		jsonDetails.putOnce("numQuestionnaires", numberOfQuestionnaires);

		final List<String> landscapeNames = getLandScapeNamesOfExperiment(filename);
		jsonDetails.putOnce("landscapes", landscapeNames.toArray());

		// TODO started / ended pair array

		// TODO number of questionnaires : number of related users

		return jsonDetails.toString();

	}

	@Override
	public void removeQuestionnaire(final String data) {

		final JSONObject filenameAndQuestionnaireTitle = new JSONObject(data);
		final String filename = filenameAndQuestionnaireTitle.keySet().iterator().next();

		final String jsonString = readExperiment(filename);
		final JSONObject jsonExperiment = new JSONObject(jsonString);

		final String questionnaireName = filenameAndQuestionnaireTitle.getString(filename);

		final JSONArray questionnaires = jsonExperiment.getJSONArray("questionnaires");

		for (int i = 0; i < questionnaires.length(); i++) {

			final JSONObject questionnaire = questionnaires.getJSONObject(i);

			if (questionnaire.get("questionnareTitle").equals(questionnaireName)) {
				questionnaires.remove(i);
				try {
					saveJSONOnServer(jsonExperiment.toString());
				} catch (final IOException e) {
					System.err.println("Couldn't save experiment when removing questionnaire.");
				}
			}
		}
	}

	@Override
	public String getQuestionnaire(final String data) {

		final JSONObject filenameAndQuestionnaireTitle = new JSONObject(data);
		final String filename = filenameAndQuestionnaireTitle.keySet().iterator().next();

		final String jsonString = getExperiment(filename);
		final JSONObject jsonExperiment = new JSONObject(jsonString);

		final String questionnaireName = filenameAndQuestionnaireTitle.getString(filename);

		final JSONArray questionnaires = jsonExperiment.getJSONArray("questionnaires");

		for (int i = 0; i < questionnaires.length(); i++) {

			final JSONObject questionnaire = questionnaires.getJSONObject(i);

			if (questionnaire.get("questionnareTitle").equals(questionnaireName)) {
				return questionnaire.toString();
			}

		}

		return null;

	}

	@Override
	public Boolean isUserInCurrentExperiment(final String username) {
		final String questionnairePrefix = DBConnection.getUserByName(username)
				.getQuestionnairePrefix();

		final JSONObject experiment = new JSONObject(
				readExperiment(Configuration.experimentFilename));

		final String prefix = experiment.getString("prefix");

		return questionnairePrefix.startsWith(prefix);

	}

	@Override
	public Question[] getQuestionnaireQuestionsForUser(final String filename,
			final String userName) {

		final ArrayList<Question> questions = new ArrayList<Question>();

		final String jsonString = readExperiment(filename);
		final JSONObject jsonExperiment = new JSONObject(jsonString);

		String questionnairePrefix = DBConnection.getUserByName(userName).getQuestionnairePrefix();

		questionnairePrefix = questionnairePrefix.replace(jsonExperiment.getString("prefix") + "_",
				"");

		final JSONArray questionnaires = jsonExperiment.getJSONArray("questionnaires");

		for (int i = 0; i < questionnaires.length(); i++) {

			final JSONObject questionnaire = questionnaires.getJSONObject(i);

			if (questionnaire.getString("questionnarePrefix").equals(questionnairePrefix)) {

				final JSONArray jsonQuestions = questionnaire.getJSONArray("questions");

				for (int w = 0; w < jsonQuestions.length(); w++) {

					final JSONObject jsonQuestion = jsonQuestions.getJSONObject(w);

					final String text = jsonQuestion.getString("questionText");
					final String type = jsonQuestion.getString("type");

					final int procTime = Integer.parseInt(jsonQuestion.getString("workingTime"));
					final String timestampData = jsonQuestion.getString("expLandscape");
					final long timestamp = Long.parseLong(timestampData.split("-")[0]);
					final long activity = Long
							.parseLong(timestampData.split("-")[1].split(".expl")[0]);

					System.out.println(timestamp + " " + activity);

					final JSONArray correctsArray = jsonQuestion.getJSONArray("answers");
					final int lengthQuestions = correctsArray.length();

					final ArrayList<String> corrects = new ArrayList<String>();
					final String[] answers = new String[lengthQuestions];

					for (int j = 0; j < lengthQuestions; j++) {
						final JSONObject jsonAnswer = correctsArray.getJSONObject(j);

						if (jsonAnswer.getString("answerText") != "") {

							answers[j] = jsonAnswer.getString("answerText");

							if (jsonAnswer.getBoolean("checkboxChecked")) {

								corrects.add(answers[j]);

							}
						}
					}

					final Question question = new Question(i, type, text, answers,
							corrects.toArray(new String[0]), procTime, timestamp, activity);

					questions.add(question);
				}
			}
		}

		return questions.toArray(new Question[0]);

	}

	@Override
	public void duplicateExperiment(final String filename) throws IOException {
		final JSONObject jsonObj = new JSONObject(getExperiment(filename));
		final String title = jsonObj.getString("title");
		jsonObj.put("title", title + "_dup");
		jsonObj.put("filename", "exp_" + (new Date().getTime()) + ".json");
		saveJSONOnServer(jsonObj.toString());
	}

	@Override
	public void uploadExperiment(final String jsonExperimentFile) throws IOException {
		final JSONObject encodedExpFile = new JSONObject(jsonExperimentFile);

		final String encodedExperiment = new String(
				encodedExpFile.getString("fileData").split(",")[1]
						.getBytes(StandardCharsets.UTF_8));

		final byte[] bytes = DatatypeConverter.parseBase64Binary(encodedExperiment);

		saveJSONOnServer(new String(bytes));
	}

	@Override
	public String downloadExperimentData(final String filename) throws IOException {

		// # create zip #
		final File zip = new File(EXP_FOLDER + File.separator + "experimentData.zip");

		// # add .json to zip #
		final File experimentFile = new File(EXP_FOLDER + File.separator + filename);
		ZipUtil.packEntries(new File[] { experimentFile }, zip);

		// # add user results and logs to zip #
		final JSONObject experimentJson = new JSONObject(readExperiment(filename));

		final JSONArray jsonQuestionnaires = experimentJson.getJSONArray("questionnaires");
		final int length = jsonQuestionnaires.length();

		for (int i = 0; i < length; i++) {

			final JSONObject questionnaire = jsonQuestionnaires.getJSONObject(i);

			final String questPrefix = experimentJson.getString("prefix") + "_"
					+ getQuestionnairePrefix(questionnaire.getString("questionnareTitle"),
							jsonQuestionnaires);

			final File answersFolder = new File(EXP_ANSWER_FOLDER + File.separator + questPrefix);
			File[] listOfFiles = answersFolder.listFiles();

			if (listOfFiles != null) {
				for (final File file : listOfFiles) {
					if (file.isFile()) {
						ZipUtil.addEntry(zip, "answers/" + questPrefix + "/" + file.getName(),
								file);
					}
				}
			}

			final File trackingFolder = new File(FileSystemHelper.getExplorVizDirectory()
					+ File.separator + "usertracking" + File.separator + questPrefix);
			listOfFiles = trackingFolder.listFiles();

			if (listOfFiles != null) {
				for (final File file : listOfFiles) {
					if (file.isFile()) {
						ZipUtil.addEntry(zip, "usertracking/" + questPrefix + "/" + file.getName(),
								file);
					}
				}
			}
		}

		// # add all related landscapes to zip #
		final List<String> landscapeNames = getLandScapeNamesOfExperiment(filename);
		for (final String landscapeName : landscapeNames) {

			final File landscape = new File(
					LANDSCAPE_FOLDER + File.separator + landscapeName + ".expl");

			ZipUtil.addEntry(zip, "landscapes/" + landscapeName + ".expl", landscape);

		}

		// # Now encode to Base64 #
		final List<Byte> result = new ArrayList<Byte>();

		final byte[] buffer = new byte[1024];

		final InputStream is = new FileInputStream(zip);
		int b = is.read(buffer);
		while (b != -1) {
			for (int i = 0; i < b; i++) {
				result.add(buffer[i]);
			}
			b = is.read(buffer);
		}
		is.close();

		final byte[] buf = new byte[result.size()];
		for (int i = 0; i < result.size(); i++) {
			buf[i] = result.get(i);
		}
		final String encoded = Base64.encodeBase64String(buf);

		// # Send back to client #
		return encoded;
	}

	@Override
	public String getExperimentTitlesAndFilenames() {

		final JSONObject data = new JSONObject();

		final File directory = new File(EXP_FOLDER);

		// Filters Files only; no folders are added
		final File[] fList = directory.listFiles(new FileFilter() {
			@Override
			public boolean accept(final File pathname) {
				final String name = pathname.getName().toLowerCase();
				return name.endsWith(".json") && pathname.isFile();
			}
		});

		if (fList != null) {
			for (final File f : fList) {
				final String filename = f.getName();
				final String json = readExperiment(filename);

				JSONObject jsonObj = null;
				try {
					jsonObj = new JSONObject(json);
				} catch (final JSONException e) {
					System.err.println(
							"Method: getExperimentTitlesAndFilenames; Couldn't create JSONObject for file: "
									+ filename);
				}

				if ((jsonObj != null) && jsonObj.has("questionnaires")) {

					final JSONArray questionnaires = jsonObj.getJSONArray("questionnaires");

					final ArrayList<String> questionNames = new ArrayList<>();

					for (int i = 0; i < questionnaires.length(); i++) {
						questionNames.add(questionnaires.getJSONObject(i).get("questionnareTitle")
								.toString());
					}

					final JSONObject questionnaireObj = new JSONObject();
					questionnaireObj.put(jsonObj.get("title").toString(), questionNames);

					data.put(jsonObj.get("filename").toString(), questionnaireObj);

				} else if (jsonObj != null) {
					final JSONObject questionnaireObj = new JSONObject();
					questionnaireObj.put(jsonObj.get("title").toString(), new ArrayList<String>());

					data.put(jsonObj.get("filename").toString(), questionnaireObj);
				}
			}
		}

		return data.toString();
	}

	@Override
	public String createUsersForQuestionnaire(final int count, final String prefix) {
		final JSONArray users = DBConnection.createUsersForQuestionnaire(prefix, count);

		final JSONObject returnObj = new JSONObject();
		returnObj.put("users", users);
		return returnObj.toString();
	}

	@Override
	public String isExperimentReadyToStart(final String filename) {

		final JSONObject experiment = new JSONObject(readExperiment(filename));

		final JSONArray questionnaires = experiment.getJSONArray("questionnaires");

		final int length = questionnaires.length();

		if (length == 0) {
			return "Add a questionnaire first.";
		}

		for (int i = 0; i < length; i++) {

			final JSONObject questionnaire = questionnaires.getJSONObject(i);

			final JSONArray questions = questionnaire.getJSONArray("questions");

			if (questions.length() == 0) {
				return "Add at least one question to the questionnaire: "
						+ questionnaire.getString("questionnareTitle");
			}

			if (questionnaire.getString("questionnareTitle").equals("")) {
				return "There is at least one questionnaire without a name";
			}
		}

		return "ready";
	}

	@Override
	public String getExperimentTitle(final String filename) {
		final JSONObject experiment = new JSONObject(readExperiment(filename));
		return experiment.getString("title");
	}

	@Override
	public String removeQuestionnaireUser(final String data) {
		final JSONObject jsonData = new JSONObject(data);

		final JSONArray usernames = jsonData.getJSONArray("users");

		final int length = usernames.length();

		for (int i = 0; i < length; i++) {
			final String username = usernames.getString(i);
			removeUserResults(username);
			DBConnection.removeUser(username);
		}

		return getExperimentAndUsers(jsonData.getJSONObject("filenameAndQuestTitle").toString());
	}

	@Override
	public void uploadLandscape(final String data) throws IOException {
		final JSONObject encodedLandscapeFile = new JSONObject(data);

		final String filename = encodedLandscapeFile.getString("filename");

		final Path landscapePath = Paths.get(LANDSCAPE_FOLDER + File.separator + filename);

		final String encodedLandscape = new String(
				encodedLandscapeFile.getString("fileData").split(",")[1]
						.getBytes(StandardCharsets.UTF_8));

		final byte[] bytes = DatatypeConverter.parseBase64Binary(encodedLandscape);

		try {
			Files.write(landscapePath, bytes, StandardOpenOption.CREATE_NEW);
		} catch (final java.nio.file.FileAlreadyExistsException e) {
			Files.write(landscapePath, bytes, StandardOpenOption.TRUNCATE_EXISTING);
		}

	}

	@Override
	public String getQuestionnaireDetails(final String data) {

		final JSONObject filenameAndQuestionnaireTitle = new JSONObject(data);
		final String filename = filenameAndQuestionnaireTitle.keySet().iterator().next();

		final String jsonString = getExperiment(filename);
		final JSONObject jsonExperiment = new JSONObject(jsonString);

		final String questionnaireName = filenameAndQuestionnaireTitle.getString(filename);

		final JSONArray questionnaires = jsonExperiment.getJSONArray("questionnaires");

		final JSONObject jsonDetails = new JSONObject();

		jsonDetails.put("filename", filename);

		for (int i = 0; i < questionnaires.length(); i++) {

			final JSONObject questionnaire = questionnaires.getJSONObject(i);

			if (questionnaire.get("questionnareTitle").equals(questionnaireName)) {
				jsonDetails.putOnce("questionnareTitle", questionnaire.get("questionnareTitle"));
				jsonDetails.putOnce("questionnarePrefix", questionnaire.get("questionnarePrefix"));
				jsonDetails.putOnce("questionnareID", questionnaire.get("questionnareID"));

				final int numberOfQuestionnaires = questionnaire.getJSONArray("questions").length();
				jsonDetails.putOnce("numQuestions", numberOfQuestionnaires);

				final List<String> landscapeNames = getLandscapesUsedInQuestionnaire(jsonExperiment,
						questionnaireName);
				jsonDetails.putOnce("landscapes", landscapeNames.toArray());

				final String jsonUserList = getQuestionnaireUsers(jsonExperiment,
						questionnaireName);

				jsonDetails.putOnce("numUsers", new JSONArray(jsonUserList).length());

				jsonDetails.putOnce("started", "TODO");
				jsonDetails.putOnce("ended", "TODO");

				break;
			}

		}

		return jsonDetails.toString();

	}

	@Override
	public String getExperimentAndUsers(final String data) {

		final JSONObject filenameAndQuestionnaireTitle = new JSONObject(data);
		final String filename = filenameAndQuestionnaireTitle.keySet().iterator().next();

		final String jsonString = getExperiment(filename);
		final JSONObject jsonExperiment = new JSONObject(jsonString);

		final JSONObject returnObj = new JSONObject();
		returnObj.put("experiment", jsonExperiment.toString());

		final String questionnaireName = filenameAndQuestionnaireTitle.getString(filename);
		final JSONArray questionnaires = jsonExperiment.getJSONArray("questionnaires");

		// calculate prefix for questionnaire => get users
		final String prefix = jsonExperiment.getString("prefix") + "_"
				+ getQuestionnairePrefix(questionnaireName, questionnaires);

		final JSONArray jsonUsers = DBConnection.getQuestionnaireUsers(prefix);

		returnObj.put("users", jsonUsers);
		returnObj.put("questionnaireTitle", questionnaireName);

		return returnObj.toString();
	}

	////////////
	// Helper //
	////////////

	private String readExperiment(final String filename) {
		byte[] jsonBytes = null;
		try {

			jsonBytes = Files.readAllBytes(Paths.get(EXP_FOLDER + File.separator + filename));

		} catch (final IOException e) {
			e.printStackTrace();
		}
		return new String(jsonBytes, StandardCharsets.UTF_8);
	}

	private String getQuestionnaireUsers(final JSONObject experiment,
			final String questionnaireName) {

		final JSONArray questionnaires = experiment.getJSONArray("questionnaires");

		final String prefix = experiment.getString("prefix") + "_"
				+ getQuestionnairePrefix(questionnaireName, questionnaires);

		final JSONArray jsonUsers = DBConnection.getQuestionnaireUsers(prefix);

		return jsonUsers.toString();

	}

	private String getQuestionnairePrefix(final String questionnaireName,
			final JSONArray questionnaires) {

		for (int i = 0; i < questionnaires.length(); i++) {

			final JSONObject questionnaire = questionnaires.getJSONObject(i);

			if (questionnaire.get("questionnareTitle").equals(questionnaireName)) {

				return questionnaire.getString("questionnarePrefix");

			}

		}
		return null;
	}

	private List<String> getLandScapeNamesOfExperiment(final String filename) {

		final ArrayList<String> names = new ArrayList<>();

		final String jsonString = getExperiment(filename);
		final JSONArray jsonQuestionnaires = new JSONObject(jsonString)
				.getJSONArray("questionnaires");

		final int length = jsonQuestionnaires.length();

		for (int i = 0; i < length; i++) {

			final JSONObject questionnaire = jsonQuestionnaires.getJSONObject(i);

			JSONArray questions = null;

			try {
				questions = questionnaire.getJSONArray("questions");

				final int lengthQuestions = questions.length();

				for (int j = 0; j < lengthQuestions; j++) {
					final JSONObject question = questions.getJSONObject(j);

					try {
						if (!names.contains(question.getString("expLandscape"))) {
							names.add(question.getString("expLandscape"));
						}
					} catch (final JSONException e) {
						// no landscape for this question
					}

				}

			} catch (final JSONException e) {
				// no questions for this questionnaire
			}
		}

		return names;

	}

	private List<String> getLandscapesUsedInQuestionnaire(final JSONObject jsonExperiment,
			final String questionnaireName) {

		// TODO need questionnaire id as well

		final ArrayList<String> names = new ArrayList<>();

		final JSONArray jsonQuestionnaires = jsonExperiment.getJSONArray("questionnaires");

		final int length = jsonQuestionnaires.length();

		for (int i = 0; i < length; i++) {

			final JSONObject questionnaire = jsonQuestionnaires.getJSONObject(i);

			if (questionnaire.getString("questionnareTitle").equals(questionnaireName)) {

				JSONArray questions = null;

				try {
					questions = questionnaire.getJSONArray("questions");

					final int lengthQuestions = questions.length();

					for (int j = 0; j < lengthQuestions; j++) {
						final JSONObject question = questions.getJSONObject(j);

						try {
							if (!names.contains(question.getString("expLandscape"))) {
								names.add(question.getString("expLandscape"));
							}
						} catch (final JSONException e) {
							// no landscape for this question
						}

					}

				} catch (final JSONException e) {
					// no questions for this questionnaire
				}
			}
		}

		return names;

	}

	private void removeUserResults(final String username) {

		final User user = DBConnection.getUserByName(username);

		final String filename = EXP_ANSWER_FOLDER + File.separator + user.getQuestionnairePrefix()
				+ File.separator + username + ".csv";

		final Path userResult = Paths.get(filename);

		try {
			Files.delete(userResult);
		} catch (final IOException e) {
			System.err.println(
					"Method: removeUserResults; Couldn't delete file. This isn't bad, it's more like an information. Exception: "
							+ e);
		}
	}

	public static void createExperimentFoldersIfNotExist() {

		final File expDirectory = new File(EXP_FOLDER);
		final File replayDirectory = new File(LANDSCAPE_FOLDER);
		final File answersDirectory = new File(EXP_ANSWER_FOLDER);
		final File trackingDirectory = new File(Tracking_FOLDER);

		if (!expDirectory.exists()) {
			expDirectory.mkdir();
		}

		if (!replayDirectory.exists()) {
			replayDirectory.mkdir();
		}

		if (!answersDirectory.exists()) {
			answersDirectory.mkdir();
		}

		if (!trackingDirectory.exists()) {
			trackingDirectory.mkdir();
		}
	}

}
