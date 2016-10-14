package explorviz.server.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.codec.binary.Base64;
import org.json.*;
import org.zeroturnaround.zip.ZipUtil;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import explorviz.server.database.DBConnection;
import explorviz.server.landscapeexchange.LandscapeExchangeServiceImpl;
import explorviz.server.main.Configuration;
import explorviz.server.main.FileSystemHelper;
import explorviz.shared.auth.User;
import explorviz.shared.experiment.Question;
import explorviz.visualization.experiment.services.JSONService;

public class JSONServiceImpl extends RemoteServiceServlet implements JSONService {

	private static final long serialVersionUID = 6576514774419481521L;

	public static String EXP_FOLDER = FileSystemHelper.getExplorVizDirectory() + File.separator
			+ "experiment";

	public static String EXP_ANSWER_FOLDER = FileSystemHelper.getExplorVizDirectory()
			+ File.separator + "experiment" + File.separator + "answers";

	public static String LANDSCAPE_FOLDER = FileSystemHelper.getExplorVizDirectory()
			+ File.separator + "replay";

	public static String Tracking_FOLDER = FileSystemHelper.getExplorVizDirectory() + File.separator
			+ "usertracking";

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

		final long timestamp = new Date().getTime();
		jsonObj.put("lastModified", timestamp);

		final String filename = jsonObj.getString("filename");
		final Path experimentFolder = Paths.get(EXP_FOLDER + File.separator + filename);

		final byte[] bytes = jsonObj.toString(4).getBytes(StandardCharsets.UTF_8);

		createFileByPath(experimentFolder, bytes);
	}

	@Override
	public void saveQuestionnaireServer(final String data) throws IOException {
		final JSONObject filenameAndQuestionnaire = new JSONObject(data);
		final String filename = filenameAndQuestionnaire.getString("filename");

		final String jsonString = getExperiment(filename);
		final JSONObject jsonExperiment = new JSONObject(jsonString);

		final JSONObject questionnaire = new JSONObject(
				filenameAndQuestionnaire.getString("questionnaire"));

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
	public List<String> getExperimentTitles() throws IOException {
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
				final String json = getExperiment(filename);
				final JSONObject jsonObj = new JSONObject(json);
				titles.add(jsonObj.getString("title"));
			}
		}
		return titles;
	}

	@Override
	public String getExperiment(final String filename) throws IOException {
		byte[] jsonBytes = null;

		createExperimentFoldersIfNotExist();

		jsonBytes = Files.readAllBytes(Paths.get(EXP_FOLDER + File.separator + filename));

		return new String(jsonBytes, StandardCharsets.UTF_8);
	}

	@Override
	public void removeExperiment(final String filename) throws JSONException, IOException {

		final JSONObject jsonExperiment = new JSONObject(getExperiment(filename));

		final JSONArray questionnaires = jsonExperiment.getJSONArray("questionnaires");

		for (int i = 0; i < questionnaires.length(); i++) {
			final JSONObject questionnaire = questionnaires.getJSONObject(i);

			final JSONObject data = new JSONObject();
			data.put("filename", filename);
			data.put("questionnareID", questionnaire.getString("questionnareID"));
			removeQuestionnaire(data.toString());
		}

		final Path experimentFile = Paths.get(EXP_FOLDER + File.separator + filename);
		removeFileByPath(experimentFile);
	}

	@Override
	public String getExperimentDetails(final String filename) throws IOException {
		final String jsonString = getExperiment(filename);

		final JSONObject jsonExperiment = new JSONObject(jsonString);

		final JSONObject jsonDetails = new JSONObject();

		final SimpleDateFormat df = new SimpleDateFormat("HH:mm - dd-MM-yyyy");

		jsonDetails.put("title", jsonExperiment.get("title"));

		jsonDetails.put("filename", jsonExperiment.get("filename"));

		final int numberOfQuestionnaires = jsonExperiment.getJSONArray("questionnaires").length();
		jsonDetails.put("numQuestionnaires", numberOfQuestionnaires);

		final List<String> landscapeNames = getLandScapeNamesOfExperiment(filename);
		jsonDetails.put("landscapes", landscapeNames.toArray());

		final int userCount = getExperimentUsers(jsonExperiment);
		jsonDetails.put("userCount", userCount);

		final long lastStarted = jsonExperiment.getLong("lastStarted");
		if (lastStarted != 0) {
			jsonDetails.put("lastStarted", df.format(new Date(lastStarted)));
		} else {
			jsonDetails.put("lastStarted", "");
		}

		final long lastEnded = jsonExperiment.getLong("lastEnded");
		if (lastEnded != 0) {
			jsonDetails.put("lastEnded", df.format(new Date(lastEnded)));
		} else {
			jsonDetails.put("lastEnded", "");
		}

		final long lastModified = jsonExperiment.getLong("lastModified");
		jsonDetails.put("lastModified", df.format(new Date(lastModified)));

		return jsonDetails.toString();
	}

	@Override
	public void removeQuestionnaire(final String data) throws JSONException, IOException {

		final JSONObject jsonData = new JSONObject(data);
		final String filename = jsonData.getString("filename");

		String jsonString;

		jsonString = getExperiment(filename);

		final JSONObject jsonExperiment = new JSONObject(jsonString);

		final String questionnaireID = jsonData.getString("questionnareID");

		final JSONArray questionnaires = jsonExperiment.getJSONArray("questionnaires");

		for (int i = 0; i < questionnaires.length(); i++) {

			final JSONObject questionnaire = questionnaires.getJSONObject(i);

			if (questionnaire.get("questionnareID").equals(questionnaireID)) {

				// remove users
				final String prefix = jsonExperiment.getString("ID") + "_" + getQuestionnairePrefix(
						questionnaire.getString("questionnareID"), questionnaires);
				removeQuestionnaireUsers(prefix);

				// remove answers
				final Path answers = Paths.get(EXP_ANSWER_FOLDER + File.separator + prefix);
				removeFileByPath(answers);

				// remove user logs
				final Path logs = Paths.get(Tracking_FOLDER + File.separator + prefix);
				removeFileByPath(logs);

				// remove file and save new json on server
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
	public String getQuestionnaire(final String data) throws IOException {

		final JSONObject filenameAndQuestionnaireID = new JSONObject(data);
		final String filename = filenameAndQuestionnaireID.getString("filename");

		final String jsonString = getExperiment(filename);
		final JSONObject jsonExperiment = new JSONObject(jsonString);

		final String questionnareID = filenameAndQuestionnaireID.getString("questionnareID");

		final JSONArray questionnaires = jsonExperiment.getJSONArray("questionnaires");

		for (int i = 0; i < questionnaires.length(); i++) {

			final JSONObject questionnaire = questionnaires.getJSONObject(i);

			if (questionnaire.get("questionnareID").equals(questionnareID)) {
				return questionnaire.toString();
			}
		}
		return null;

	}

	@Override
	public Boolean isUserInCurrentExperiment(final String username)
			throws JSONException, IOException {

		final User tempUser = DBConnection.getUserByName(username);

		if (tempUser == null) {
			return false;
		}

		final String questionnairePrefix = tempUser.getQuestionnairePrefix();

		JSONObject experiment = null;

		try {
			experiment = new JSONObject(getExperiment(Configuration.experimentFilename));
		} catch (final IOException e) {
			// System.err.println("Could not read experiment. This may not be
			// bad. Exception: " + e);
			return false;
		}

		final String prefix = experiment.getString("ID");

		return questionnairePrefix.startsWith(prefix);

	}

	@Override
	public Question[] getQuestionnaireQuestionsForUser(final String filename, final String userName)
			throws IOException {

		final ArrayList<Question> questions = new ArrayList<Question>();

		final String jsonString = getExperiment(filename);
		final JSONObject jsonExperiment = new JSONObject(jsonString);

		String questionnairePrefix = DBConnection.getUserByName(userName).getQuestionnairePrefix();

		questionnairePrefix = questionnairePrefix.replace(jsonExperiment.getString("ID") + "_", "");

		final JSONArray questionnaires = jsonExperiment.getJSONArray("questionnaires");

		for (int i = 0; i < questionnaires.length(); i++) {

			final JSONObject questionnaire = questionnaires.getJSONObject(i);

			if (questionnaire.getString("questionnareID").equals(questionnairePrefix)) {

				final JSONArray jsonQuestions = questionnaire.getJSONArray("questions");

				for (int w = 0; w < jsonQuestions.length(); w++) {

					final JSONObject jsonQuestion = jsonQuestions.getJSONObject(w);

					final String text = jsonQuestion.getString("questionText");
					final String type = jsonQuestion.getString("type");

					final int procTime = Integer.parseInt(jsonQuestion.getString("workingTime"));
					final String timestampData = jsonQuestion.getString("expLandscape");
					String maybeApplication = "";

					try {
						maybeApplication = jsonQuestion.getString("expApplication");
					} catch (final JSONException e) {
						// there was no application for this question
						// => no problem => landscape question
					}

					final long timestamp = Long.parseLong(timestampData.split("-")[0]);
					final long activity = Long
							.parseLong(timestampData.split("-")[1].split(".expl")[0]);

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
							corrects.toArray(new String[0]), procTime, timestamp, activity,
							maybeApplication);

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
		final long timestamp = new Date().getTime();
		jsonObj.put("filename", "exp_" + timestamp + ".json");
		jsonObj.put("ID", "exp" + timestamp);
		saveJSONOnServer(jsonObj.toString());
	}

	@Override
	public boolean uploadExperiment(final String jsonExperimentFile) throws IOException {
		final JSONObject encodedExpFile = new JSONObject(jsonExperimentFile);

		final String encodedExperiment = new String(
				encodedExpFile.getString("fileData").split(",")[1]
						.getBytes(StandardCharsets.UTF_8));

		final byte[] bytes = DatatypeConverter.parseBase64Binary(encodedExperiment);

		final String jsonExperiment = new String(bytes);

		boolean isValidExperiment = false;

		try {
			isValidExperiment = validateExperiment(jsonExperiment);
		} catch (IOException | ProcessingException e) {
			System.err.println(
					"Method: uploadExperiment. Couldn't upload experiment. Exception: " + e);
			return false;
		}

		if (isValidExperiment) {
			saveJSONOnServer(jsonExperiment);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public String downloadExperimentData(final String filename) throws IOException {

		// # add user results and logs to zip #
		final JSONObject experimentJson = new JSONObject(getExperiment(filename));

		// # create zip #
		final File zip = new File(EXP_FOLDER + File.separator + "experimentData.zip");

		// # add .json to zip #
		final File experimentFile = new File(EXP_FOLDER + File.separator + filename);
		ZipUtil.packEntries(new File[] { experimentFile }, zip);

		final JSONArray jsonQuestionnaires = experimentJson.getJSONArray("questionnaires");
		final int length = jsonQuestionnaires.length();

		for (int i = 0; i < length; i++) {

			final JSONObject questionnaire = jsonQuestionnaires.getJSONObject(i);

			final String questPrefix = experimentJson.getString("ID") + "_"
					+ getQuestionnairePrefix(questionnaire.getString("questionnareID"),
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
	public String getExperimentTitlesAndFilenames() throws IOException {

		final JSONObject returnObj = new JSONObject();

		final File directory = new File(EXP_FOLDER);

		final JSONArray failingExperiments = new JSONArray();

		// Filter valid experiments out in experiment folder
		final File[] fList = directory.listFiles(new FileFilter() {
			@Override
			public boolean accept(final File pathname) {
				final String name = pathname.getName().toLowerCase();

				try {

					if (!name.endsWith(".json") || !pathname.isFile()) {
						return false;
					}

					final boolean isValidExperiment = validateExperiment(getExperiment(name));
					if (!isValidExperiment) {
						failingExperiments.put(name);
					}
					return isValidExperiment;
				} catch (IOException | ProcessingException e) {
					System.err.println("Couldn't process file " + name + ". Exception: " + e);
				}

				return false;
			}
		});

		returnObj.put("failingExperiments", failingExperiments);
		final JSONArray experimentsData = new JSONArray();
		returnObj.put("experimentsData", experimentsData);

		// collect necessary data of experiments for
		// experiment overview
		if (fList != null) {
			JSONObject tmpData = null;
			long tmpLastModified = 0;
			for (final File f : fList) {
				final JSONObject data = new JSONObject();

				final String filename = f.getName();
				final String jsonExperiment = getExperiment(filename);

				JSONObject jsonObj = null;
				try {
					jsonObj = new JSONObject(jsonExperiment);
				} catch (final JSONException e) {
					System.err.println(
							"Method: getExperimentTitlesAndFilenames; Couldn't create JSONObject for file: "
									+ filename);
					continue;
				}

				final long lastModified = jsonObj.getLong("lastModified");
				if (lastModified > tmpLastModified) {
					data.put("lastTouched", "true");
					if (tmpData != null) {
						tmpData.put("lastTouched", "false");
					}
					tmpLastModified = lastModified;
					tmpData = data;
				} else {
					data.put("lastTouched", "false");
				}

				data.put("filename", jsonObj.get("filename").toString());
				data.put("title", jsonObj.get("title").toString());

				final JSONArray questionnairesData = new JSONArray();

				if ((jsonObj != null) && jsonObj.has("questionnaires")) {

					final JSONArray questionnaires = jsonObj.getJSONArray("questionnaires");

					for (int i = 0; i < questionnaires.length(); i++) {
						final JSONObject questionnaireObj = questionnaires.getJSONObject(i);
						final JSONObject questionnaireData = new JSONObject();
						questionnaireData.put("questionnareTitle",
								questionnaireObj.getString("questionnareTitle"));
						questionnaireData.put("questionnareID",
								questionnaireObj.getString("questionnareID"));
						questionnairesData.put(questionnaireData);
					}

					data.put("questionnaires", questionnairesData);
				}

				experimentsData.put(data);
			}
		}

		return returnObj.toString();
	}

	@Override
	public String createUsersForQuestionnaire(final int count, final String prefix,
			final String filename) {
		final JSONArray users = DBConnection.createUsersForQuestionnaire(prefix, count);

		final JSONObject returnObj = new JSONObject();
		returnObj.put("users", users);

		// update lastModified stamp
		JSONObject experiment = null;
		try {
			experiment = new JSONObject(getExperiment(filename));
		} catch (IOException | JSONException e) {
			System.err.println("Couldn not update timestamp. Exception: " + e);
		}

		experiment.put("lastModified", new Date().getTime());

		try {
			saveJSONOnServer(experiment.toString());
		} catch (final IOException e) {
			System.err.println("Could not save updated experiment. Exception: " + e);
		}

		return returnObj.toString();
	}

	@Override
	public String isExperimentReadyToStart(final String filename)
			throws JSONException, IOException {

		final JSONObject experiment = new JSONObject(getExperiment(filename));

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

		// experiment is ready
		try {
			saveJSONOnServer(experiment.toString());
		} catch (final IOException e) {
			System.err.println(e);
		}

		return "ready";
	}

	@Override
	public void setExperimentTimeAttr(final String filename, final boolean isLastStarted)
			throws JSONException, IOException {
		final JSONObject experiment = new JSONObject(getExperiment(filename));

		// => update time attributes
		if (isLastStarted) {
			experiment.put("lastStarted", new Date().getTime());
		} else {
			experiment.put("lastEnded", new Date().getTime());
		}

		try {
			saveJSONOnServer(experiment.toString());
		} catch (final IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public String getExperimentTitle(final String filename) throws JSONException, IOException {
		final JSONObject experiment = new JSONObject(getExperiment(filename));
		return experiment.getString("title");
	}

	@Override
	public String removeQuestionnaireUser(final String data) throws IOException {
		final JSONObject jsonData = new JSONObject(data);

		final JSONArray usernames = jsonData.getJSONArray("users");
		final String filename = jsonData.getString("filename");
		final String questionnareID = jsonData.getString("questionnareID");

		final JSONObject filenameAndQuestID = new JSONObject();
		filenameAndQuestID.put("filename", filename);
		filenameAndQuestID.put("questionnareID", questionnareID);

		final int length = usernames.length();

		for (int i = 0; i < length; i++) {
			final String username = usernames.getString(i);
			removeUserData(username);
			DBConnection.removeUser(username);
		}

		// update lastModified stamp
		JSONObject experiment = null;
		try {
			experiment = new JSONObject(getExperiment(filename));
		} catch (IOException | JSONException e) {
			System.err.println("Couldn not update timestamp. Exception: " + e);
		}

		experiment.put("lastModified", new Date().getTime());

		try {
			saveJSONOnServer(experiment.toString());
		} catch (final IOException e) {
			System.err.println("Could not save updated experiment. Exception: " + e);
		}

		return getExperimentAndUsers(filenameAndQuestID.toString());
	}

	@Override
	public boolean uploadLandscape(final String data) throws IOException {
		final JSONObject encodedLandscapeFile = new JSONObject(data);

		final String filename = encodedLandscapeFile.getString("filename");

		// first validation check -> filename
		long timestamp;
		long activity;

		try {
			timestamp = Long.parseLong(filename.split("-")[0]);
			activity = Long.parseLong(filename.split("-")[1].split(".expl")[0]);
		} catch (final NumberFormatException e) {
			return false;
		}

		final Path landscapePath = Paths.get(LANDSCAPE_FOLDER + File.separator + filename);

		final String encodedLandscape = new String(
				encodedLandscapeFile.getString("fileData").split(",")[1]
						.getBytes(StandardCharsets.UTF_8));

		final byte[] bytes = DatatypeConverter.parseBase64Binary(encodedLandscape);

		createFileByPath(landscapePath, bytes);

		// second validation check -> deserialization
		try {
			LandscapeExchangeServiceImpl.getLandscapeStatic(timestamp, activity);
		} catch (final Exception e) {
			removeFileByPath(landscapePath);
			return false;
		}

		return true;

	}

	@Override
	public String getQuestionnaireDetails(final String data) throws IOException {

		final JSONObject filenameAndQuestionnaireID = new JSONObject(data);
		final String filename = filenameAndQuestionnaireID.getString("filename");

		final String jsonString = getExperiment(filename);
		final JSONObject jsonExperiment = new JSONObject(jsonString);

		final String questionnaireID = filenameAndQuestionnaireID.getString("questionnareID");

		final JSONArray questionnaires = jsonExperiment.getJSONArray("questionnaires");

		final JSONObject jsonDetails = new JSONObject();

		jsonDetails.put("filename", filename);

		for (int i = 0; i < questionnaires.length(); i++) {

			final JSONObject questionnaire = questionnaires.getJSONObject(i);

			if (questionnaire.get("questionnareID").equals(questionnaireID)) {
				jsonDetails.putOnce("questionnareTitle", questionnaire.get("questionnareTitle"));
				jsonDetails.putOnce("questionnareID", questionnaire.get("questionnareID"));

				final int numberOfQuestionnaires = questionnaire.getJSONArray("questions").length();
				jsonDetails.putOnce("numQuestions", numberOfQuestionnaires);

				final List<String> landscapeNames = getLandscapesUsedInQuestionnaire(jsonExperiment,
						questionnaireID);
				jsonDetails.putOnce("landscapes", landscapeNames.toArray());

				final String jsonUserList = getQuestionnaireUsers(jsonExperiment, questionnaireID);

				jsonDetails.putOnce("numUsers", new JSONArray(jsonUserList).length());

				jsonDetails.putOnce("started", "TODO");
				jsonDetails.putOnce("ended", "TODO");

				break;
			}

		}

		return jsonDetails.toString();

	}

	@Override
	public String getExperimentAndUsers(final String data) throws IOException {

		final JSONObject filenameAndQuestionnaireID = new JSONObject(data);
		final String filename = filenameAndQuestionnaireID.getString("filename");

		final String jsonString = getExperiment(filename);
		final JSONObject jsonExperiment = new JSONObject(jsonString);

		final JSONObject returnObj = new JSONObject();
		returnObj.put("experiment", jsonExperiment.toString());

		final String questionnaireID = filenameAndQuestionnaireID.getString("questionnareID");
		final JSONArray questionnaires = jsonExperiment.getJSONArray("questionnaires");

		// calculate prefix for questionnaire => get users
		final String prefix = jsonExperiment.getString("ID") + "_"
				+ getQuestionnairePrefix(questionnaireID, questionnaires);

		final JSONArray jsonUsers = DBConnection.getQuestionnaireUsers(prefix);

		returnObj.put("users", jsonUsers);
		returnObj.put("questionnareID", questionnaireID);

		return returnObj.toString();
	}

	////////////
	// Helper //
	////////////

	private String getQuestionnaireUsers(final JSONObject experiment,
			final String questionnaireID) {

		final JSONArray questionnaires = experiment.getJSONArray("questionnaires");

		final String prefix = experiment.getString("ID") + "_"
				+ getQuestionnairePrefix(questionnaireID, questionnaires);

		final JSONArray jsonUsers = DBConnection.getQuestionnaireUsers(prefix);

		return jsonUsers.toString();

	}

	private int getExperimentUsers(final JSONObject experiment) {

		final JSONArray questionnaires = experiment.getJSONArray("questionnaires");

		int userCount = 0;

		for (int i = 0; i < questionnaires.length(); i++) {

			final JSONObject questionnaire = questionnaires.getJSONObject(i);

			final String prefix = experiment.getString("ID") + "_" + getQuestionnairePrefix(
					questionnaire.getString("questionnareID"), questionnaires);

			final JSONArray jsonUsers = DBConnection.getQuestionnaireUsers(prefix);

			if (jsonUsers == null) {
				break;
			}

			userCount += jsonUsers.length();

		}

		return userCount;

	}

	private String getQuestionnairePrefix(final String questionnaireID,
			final JSONArray questionnaires) {

		for (int i = 0; i < questionnaires.length(); i++) {

			final JSONObject questionnaire = questionnaires.getJSONObject(i);

			if (questionnaire.get("questionnareID").equals(questionnaireID)) {

				return questionnaire.getString("questionnareID");

			}

		}
		return null;
	}

	private List<String> getLandScapeNamesOfExperiment(final String filename) throws IOException {

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
			final String questionnaireID) {

		final ArrayList<String> names = new ArrayList<>();

		final JSONArray jsonQuestionnaires = jsonExperiment.getJSONArray("questionnaires");

		final int length = jsonQuestionnaires.length();

		for (int i = 0; i < length; i++) {

			final JSONObject questionnaire = jsonQuestionnaires.getJSONObject(i);

			if (questionnaire.getString("questionnareID").equals(questionnaireID)) {

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

	private void removeUserData(final String username) {

		final User user = DBConnection.getUserByName(username);

		final String filenameResults = EXP_ANSWER_FOLDER + File.separator
				+ user.getQuestionnairePrefix() + File.separator + username + ".csv";

		final String filenameTracking = Tracking_FOLDER + File.separator
				+ user.getQuestionnairePrefix() + File.separator + username + "_tracking.log";

		Path path = Paths.get(filenameResults);
		removeFileByPath(path);

		path = Paths.get(filenameTracking);
		removeFileByPath(path);

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

	private void removeQuestionnaireUsers(final String questionnairePrefix) {

		final JSONArray jsonUsers = DBConnection.getQuestionnaireUsers(questionnairePrefix);

		final int length = jsonUsers.length();

		for (int i = 0; i < length; i++) {
			final JSONObject user = jsonUsers.getJSONObject(i);
			DBConnection.removeUser(user.getString("username"));
		}
	}

	private void createFileByPath(final Path toCreate, final byte[] bytes) throws IOException {
		createExperimentFoldersIfNotExist();
		try {
			Files.write(toCreate, bytes, StandardOpenOption.CREATE_NEW);
		} catch (final java.nio.file.FileAlreadyExistsException e) {
			Files.write(toCreate, bytes, StandardOpenOption.TRUNCATE_EXISTING);
		}
	}

	private void removeFileByPath(final Path toDelete) {
		createExperimentFoldersIfNotExist();
		try {
			Files.delete(toDelete);
		} catch (final DirectoryNotEmptyException e) {
			// if directory, remove everything inside
			// and then the parent folder
			final File directory = toDelete.toFile();

			final String[] fList = directory.list();
			for (final String filename : fList) {
				removeFileByPath(Paths.get(directory + File.separator + filename));
			}

			removeFileByPath(toDelete);

		} catch (final NoSuchFileException e) {
			System.err.println("Couldn't delete file with path: " + toDelete
					+ ". It doesn't exist. Exception: " + e);
		} catch (final IOException e) {
			System.err.println("Couldn't delete file with path: " + toDelete + ". Exception: " + e);
		}
	}

	private boolean validateExperiment(final String jsonExperiment)
			throws IOException, ProcessingException {

		final JsonNode experiment = JsonLoader.fromString(jsonExperiment);

		final String schemaPath = getServletContext().getRealPath("/experiment/") + "/"
				+ "experimentJSONSchema.json";

		final JsonNode schemaNode = JsonLoader.fromPath(schemaPath);
		final JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
		final JsonSchema schema = factory.getJsonSchema(schemaNode);

		final ProcessingReport report = schema.validate(experiment);

		return report.isSuccess();
	}
}
