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
import explorviz.shared.experiment.*;
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

	public static String LOCAL_VIDEO_DATA_FOLDER = "/experiment/screenRecords";

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

		boolean isValid = false;

		try {
			isValid = validateExperiment(jsonObj.toString());
		} catch (final ProcessingException e) {
			System.err.println("There was an error while saving an experiment. Exception: " + e);
			return;
		}

		if (isValid) {
			final String filename = jsonObj.getString("filename");
			final Path experimentFolder = Paths.get(EXP_FOLDER + File.separator + filename);

			final byte[] bytes = jsonObj.toString(4).getBytes(StandardCharsets.UTF_8);

			createFileByPath(experimentFolder, bytes);
		}
	}

	@Override
	public void saveQuestionnaireServer(final String data) throws IOException {
		final JSONObject filenameAndQuestionnaire = new JSONObject(data);
		final String filename = filenameAndQuestionnaire.getString("filename");

		final String jsonString = getExperiment(filename);
		final JSONObject jsonExperiment = new JSONObject(jsonString);

		final JSONObject questionnaire = filenameAndQuestionnaire.getJSONObject("questionnaire");

		final JSONArray questionnaires = jsonExperiment.getJSONArray("questionnaires");

		boolean questionnaireUpdated = false;

		for (int i = 0; i < questionnaires.length(); i++) {

			final JSONObject questionnaireTemp = questionnaires.getJSONObject(i);

			// find questionnaire to update
			if (questionnaireTemp.has("questionnareTitle")
					&& questionnaireTemp.getString("questionnareID")
							.equals(questionnaire.getString("questionnareID"))) {

				questionnaireUpdated = true;
				questionnaires.put(i, questionnaire);
			}
		}

		if (!questionnaireUpdated) {
			questionnaires.put(questionnaire);
		}

		try {
			saveJSONOnServer(jsonExperiment.toString());
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
		try {
			removeFileByPath(experimentFile);
		} catch (final IOException e) {
			System.err.println(
					"Couldn't delete file with path: " + experimentFile + ". Exception: " + e);
		}

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

	/**
	 * Returns the prequestions of a questionnaire Depending on the (current)
	 * user and experiment the user is completing
	 *
	 * @param filename
	 *            String of the name of the experiment file
	 * @param userName
	 *            String of the name of a specific (current) user
	 * @return ArrayList of prequestions
	 */
	@Override
	public ArrayList<Prequestion> getQuestionnairePrequestionsForUser(final String filename,
			final String userName) throws IOException {

		final ArrayList<Prequestion> prequestions = new ArrayList<Prequestion>();

		final String jsonString = getExperiment(filename);
		final JSONObject jsonExperiment = new JSONObject(jsonString);

		String questionnairePrefix = DBConnection.getUserByName(userName).getQuestionnairePrefix();

		questionnairePrefix = questionnairePrefix.replace(jsonExperiment.getString("ID") + "_", "");

		final JSONArray questionnaires = jsonExperiment.getJSONArray("questionnaires");

		for (int i = 0; i < questionnaires.length(); i++) {

			final JSONObject questionnaire = questionnaires.getJSONObject(i);

			if (questionnaire.getString("questionnareID").equals(questionnairePrefix)) {
				JSONArray jsonQuestions = null;
				try {
					jsonQuestions = questionnaire.getJSONArray("prequestions");
				} catch (final JSONException e) {
					// there exists the possibility that there are no
					// prequestions
					return null;
				}
				for (int w = 0; w < jsonQuestions.length(); w++) {

					final JSONObject jsonQuestion = jsonQuestions.getJSONObject(w);

					final String text = jsonQuestion.getString("questionText");
					if (text.equals("")) {
						break;
					}
					final String type = jsonQuestion.getString("type");

					final JSONArray correctsArray = jsonQuestion.getJSONArray("answers");
					final int lengthQuestions = correctsArray.length();

					final String[] answers = new String[lengthQuestions];

					for (int j = 0; j < lengthQuestions; j++) {
						final JSONObject jsonAnswer = correctsArray.getJSONObject(j);

						if (jsonAnswer.getString("answerText") != "") {

							answers[j] = jsonAnswer.getString("answerText");
						}
					}

					int min = 0;
					int max = 0;

					if ((type != null) && (type == "numberRange")) {
						min = Integer.parseInt(jsonQuestion.getString("answer_min"), 0);
						max = Integer.parseInt(jsonQuestion.getString("answer_max"), 0);
					}

					final Prequestion question = new Prequestion(i, type, text, answers, min, max);

					prequestions.add(question);
				}
			}
		}

		return prequestions;

	}

	/**
	 * Returns the questions of a questionnaire Depending on the (current) user
	 * and experiment the user is completing
	 *
	 * @param filename
	 *            String of the name of the experiment file
	 * @param userName
	 *            String of the name of a specific (current) user
	 * @return ArrayList of questions
	 */
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

	/**
	 * Returns the postquestions of a questionnaire Depending on the (current)
	 * user and experiment the user is completing
	 *
	 * @param filename
	 *            String of the name of the experiment file
	 * @param userName
	 *            String of the name of a specific (current) user
	 * @return ArrayList of postquestions
	 */
	@Override
	public ArrayList<Postquestion> getQuestionnairePostquestionsForUser(final String filename,
			final String userName) throws IOException {

		final ArrayList<Postquestion> postquestions = new ArrayList<Postquestion>();

		final String jsonString = getExperiment(filename);
		final JSONObject jsonExperiment = new JSONObject(jsonString);

		String questionnairePrefix = DBConnection.getUserByName(userName).getQuestionnairePrefix();

		questionnairePrefix = questionnairePrefix.replace(jsonExperiment.getString("ID") + "_", "");

		final JSONArray questionnaires = jsonExperiment.getJSONArray("questionnaires");

		for (int i = 0; i < questionnaires.length(); i++) {

			final JSONObject questionnaire = questionnaires.getJSONObject(i);

			if (questionnaire.getString("questionnareID").equals(questionnairePrefix)) {

				JSONArray jsonQuestions = null;
				try {
					jsonQuestions = questionnaire.getJSONArray("postquestions");
				} catch (final JSONException e) {
					// in case there are no postquestions
					return null;
				}

				for (int w = 0; w < jsonQuestions.length(); w++) {

					final JSONObject jsonQuestion = jsonQuestions.getJSONObject(w);

					final String text = jsonQuestion.getString("questionText");
					final String type = jsonQuestion.getString("type");

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

					int min = 0;
					int max = 0;

					if ((type != null) && (type == "numberRange")) {
						min = Integer.parseInt(jsonQuestion.getString("answer_min"), 0);
						max = Integer.parseInt(jsonQuestion.getString("answer_max"), 0);

					}

					final Postquestion question = new Postquestion(i, type, text, answers, min,
							max);

					postquestions.add(question);
				}
			}
		}

		return postquestions;

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

		String encodedExperiment = null;

		try {
			encodedExperiment = new String(encodedExpFile.getString("fileData").split(",")[1]
					.getBytes(StandardCharsets.UTF_8));
		} catch (final ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
			return false;
		}

		byte[] bytes = null;
		try {
			bytes = DatatypeConverter.parseBase64Binary(encodedExperiment);
		} catch (final IllegalArgumentException e) {
			e.printStackTrace();
			return false;
		}

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

			// add eyeTracking data, if there are any
			final File eyeTrackingDataFolder = new File(EXP_ANSWER_FOLDER + File.separator
					+ questPrefix + File.separator + "eyeTrackingData");
			listOfFiles = eyeTrackingDataFolder.listFiles();
			String answersZipFolder = "answers/" + questPrefix + "/" + "eyeTrackingData/";

			if (listOfFiles != null) {
				for (final File file : listOfFiles) {
					if (file.isFile()) {
						ZipUtil.addEntry(zip, answersZipFolder + file.getName(), file);
					}
				}
			}

			// add screenRecords, if there are any
			final File screenRecordsFolder = new File(EXP_ANSWER_FOLDER + File.separator
					+ questPrefix + File.separator + "screenRecords");
			listOfFiles = screenRecordsFolder.listFiles();
			answersZipFolder = "answers/" + questPrefix + "/" + "screenRecords/";

			if (listOfFiles != null) {
				for (final File file : listOfFiles) {
					if (file.isFile()) {
						ZipUtil.addEntry(zip, answersZipFolder + file.getName(), file);
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
						questionnaireData.put("eyeTracking",
								questionnaireObj.getBoolean("eyeTracking"));
						questionnaireData.put("recordScreen",
								questionnaireObj.getBoolean("recordScreen"));
						questionnaireData.put("preAndPostquestions",
								questionnaireObj.getBoolean("preAndPostquestions"));
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
		try {
			// timestamp
			Long.parseLong(filename.split("-")[0]);
			// activity
			Long.parseLong(filename.split("-")[1].split(".expl")[0]);
		} catch (final NumberFormatException e) {
			return false;
		}

		final Path landscapePath = Paths.get(LANDSCAPE_FOLDER + File.separator + filename);

		String encodedLandscape = null;
		try {
			encodedLandscape = new String(encodedLandscapeFile.getString("fileData").split(",")[1]
					.getBytes(StandardCharsets.UTF_8));
		} catch (final ArrayIndexOutOfBoundsException e) {
			return false;
		}

		byte[] bytes = null;

		try {
			bytes = DatatypeConverter.parseBase64Binary(encodedLandscape);
		} catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
			return false;
		}

		// second validation check -> deserialization
		try {
			LandscapeExchangeServiceImpl.getLandscapeByByte(bytes);
		} catch (final Exception e) {
			// e.printStackTrace();
			return false;
		}

		// everything is fine -> save file
		createFileByPath(landscapePath, bytes);
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

				final int numberOfPrequestions = questionnaire.getJSONArray("prequestions")
						.length();
				jsonDetails.putOnce("numPrequestions", numberOfPrequestions);

				final int numberOfQuestionnaires = questionnaire.getJSONArray("questions").length();
				jsonDetails.putOnce("numQuestions", numberOfQuestionnaires);

				final int numberOfPostquestions = questionnaire.getJSONArray("postquestions")
						.length();
				jsonDetails.putOnce("numPostquestions", numberOfPostquestions);

				final List<String> landscapeNames = getLandscapesUsedInQuestionnaire(jsonExperiment,
						questionnaireID);
				jsonDetails.putOnce("landscapes", landscapeNames.toArray());

				final String jsonUserList = getQuestionnaireUsers(jsonExperiment, questionnaireID);

				jsonDetails.putOnce("numUsers", new JSONArray(jsonUserList).length());

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
		final String screenRecords = "screenRecords";
		final String eyeTracking = "eyeTrackingData";

		final User user = DBConnection.getUserByName(username);

		final String filenameResults = EXP_ANSWER_FOLDER + File.separator
				+ user.getQuestionnairePrefix() + File.separator + username + ".csv";

		final String filenameTracking = Tracking_FOLDER + File.separator
				+ user.getQuestionnairePrefix() + File.separator + username + "_tracking.log";

		final String filenameEyeTracking = EXP_ANSWER_FOLDER + File.separator
				+ user.getQuestionnairePrefix() + File.separator + eyeTracking + File.separator
				+ username + ".txt";

		final String filenameScreenRecording = EXP_ANSWER_FOLDER + File.separator
				+ user.getQuestionnairePrefix() + File.separator + screenRecords + File.separator
				+ username + ".mp4";
		try {
			Path path = Paths.get(filenameResults);
			removeFileByPath(path);

			final Path eyeTrackingPath = Paths.get(filenameEyeTracking);
			removeFileByPath(eyeTrackingPath);

			final Path screenRecordsPath = Paths.get(filenameScreenRecording);
			removeFileByPath(screenRecordsPath);

			path = Paths.get(filenameTracking);
			removeFileByPath(path);
		} catch (final IOException e) {
			System.err.println("Couldn't delete a file: Exception: " + e);
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

	private void removeFileByPath(final Path toDelete) throws IOException {
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
			// System.err.println("Couldn't delete file with path: " + toDelete
			// + ". It doesn't exist. Exception: " + e);
		}
	}

	private boolean validateExperiment(final String jsonExperiment)
			throws IOException, ProcessingException {

		final JsonNode experiment = JsonLoader.fromString(jsonExperiment);

		String schemaPath = null;

		try {
			schemaPath = getServletContext().getRealPath("/experiment/") + "/"
					+ "experimentJSONSchema.json";
		} catch (final IllegalStateException e) {
			// catched => no servlet context => try to use
			// relative path of project for Unit testing
			schemaPath = "./war/experiment/experimentJSONSchema.json";
		}

		final JsonNode schemaNode = JsonLoader.fromPath(schemaPath);
		final JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
		final JsonSchema schema = factory.getJsonSchema(schemaNode);

		final ProcessingReport report = schema.validate(experiment);

		return report.isSuccess();
	}

	/**
	 * Returns boolean attribute preAndPostquestions of a Questionnaire
	 *
	 * @param filename
	 *            a String with the name of the experiment file
	 * @param userName
	 *            can be either the username of the user that is currently
	 *            completing the questionnaire or empty
	 * @param questionnaireID
	 *            is either empty or the questionnaireID of the questionnaire of
	 *            the experiment
	 * @return the boolean attribute of preAndPostquestions of wanted
	 *         questionnaire
	 */
	@Override
	public boolean getQuestionnairePreAndPostquestions(final String filename, final String userName,
			final String questionnaireID) throws IOException {
		boolean preAndPostquestions = false;

		final String jsonString = getExperiment(filename);
		final JSONObject jsonExperiment = new JSONObject(jsonString);

		String questionnairePrefix = "";
		if (userName.equals("")) { // use the same variable
			questionnairePrefix = questionnaireID;

		} else {
			questionnairePrefix = DBConnection.getUserByName(userName).getQuestionnairePrefix();

			questionnairePrefix = questionnairePrefix.replace(jsonExperiment.getString("ID") + "_",
					"");
		}

		final JSONArray questionnaires = jsonExperiment.getJSONArray("questionnaires");

		for (int i = 0; i < questionnaires.length(); i++) {

			JSONObject questionnaire = questionnaires.getJSONObject(i);

			if (questionnaire.getString("questionnareID").equals(questionnairePrefix)) {
				try {
					preAndPostquestions = questionnaire.getBoolean("preAndPostquestions");
				} catch (final JSONException e) {
					// update questionnaire with a false as preAndPostquestions
					questionnaire = questionnaire.put("preAndPostquestions", false);
					// for saving in the correct experiment
					JSONObject data = new JSONObject();
					data = data.put("filename", filename);
					data = data.put("questionnaire", questionnaire);
					saveQuestionnaireServer(data.toString());
				}
			}
		}
		return preAndPostquestions;
	}

	/**
	 * Returns boolean attribute eyetRacking of a Questionnaire
	 *
	 * @param filename
	 *            a String with the name of the experiment file
	 * @param userName
	 *            can be either the username of the user that is currently
	 *            completing the questionnaire or empty
	 * @param questionnaireID
	 *            is either empty or the questionnaireID of the questionnaire of
	 *            the experiment
	 * @return the boolean attribute of eyeTracking of wanted questionnaire
	 */
	@Override
	public boolean getQuestionnaireEyeTracking(final String filename, final String userName,
			final String questionnaireID) throws IOException {
		boolean eyeTracking = false;

		final String jsonString = getExperiment(filename);
		final JSONObject jsonExperiment = new JSONObject(jsonString);

		String questionnairePrefix = "";
		if (userName.equals("")) { // use the same variable
			questionnairePrefix = questionnaireID;
		} else {
			questionnairePrefix = DBConnection.getUserByName(userName).getQuestionnairePrefix();

			questionnairePrefix = questionnairePrefix.replace(jsonExperiment.getString("ID") + "_",
					"");
		}

		final JSONArray questionnaires = jsonExperiment.getJSONArray("questionnaires");

		for (int i = 0; i < questionnaires.length(); i++) {

			JSONObject questionnaire = questionnaires.getJSONObject(i);

			if (questionnaire.getString("questionnareID").equals(questionnairePrefix)) {
				try {
					eyeTracking = questionnaire.getBoolean("eyeTracking");
				} catch (final JSONException e) {
					// update questionnaire with a false as eyeTracking
					// attribute
					questionnaire = questionnaire.put("eyeTracking", false);
					// for saving in the correct experiment
					JSONObject data = new JSONObject();
					data = data.put("filename", filename);
					data = data.put("questionnaire", questionnaire);
					saveQuestionnaireServer(data.toString());
				}
			}
		}
		return eyeTracking;
	}

	/**
	 * Returns boolean attribute recordScreen of a Questionnaire
	 *
	 * @param filename
	 *            a String with the name of the experiment file
	 * @param userName
	 *            can be either the username of the user that is currently
	 *            completing the questionnaire or empty
	 * @param questionnaireID
	 *            is either empty or the questionnaireID of the questionnaire of
	 *            the experiment
	 * @return the boolean attribute of recordScreen of wanted questionnaire
	 */
	@Override
	public boolean getQuestionnaireRecordScreen(final String filename, final String userName,
			final String questionnaireID) throws IOException {
		boolean recordScreen = false;

		final String jsonString = getExperiment(filename);
		final JSONObject jsonExperiment = new JSONObject(jsonString);

		String questionnairePrefix = "";
		if (userName.equals("")) { // use the same variable
			questionnairePrefix = questionnaireID;
		} else {
			questionnairePrefix = DBConnection.getUserByName(userName).getQuestionnairePrefix();

			questionnairePrefix = questionnairePrefix.replace(jsonExperiment.getString("ID") + "_",
					"");
		}

		final JSONArray questionnaires = jsonExperiment.getJSONArray("questionnaires");

		for (int i = 0; i < questionnaires.length(); i++) {

			JSONObject questionnaire = questionnaires.getJSONObject(i);

			if (questionnaire.getString("questionnareID").equals(questionnairePrefix)) {
				try {
					recordScreen = questionnaire.getBoolean("recordScreen");
				} catch (final JSONException e) {
					// update questionnaire with a false as recordScreen
					// attribute
					questionnaire = questionnaire.put("recordScreen", false);
					// for saving in the correct experiment
					// for saving in the correct experiment
					JSONObject data = new JSONObject();
					data = data.put("filename", filename);
					data = data.put("questionnaire", questionnaire);
					saveQuestionnaireServer(data.toString());
				}
			}
		}
		return recordScreen;
	}

	/**
	 * Sets boolean attribute preAndPostquestions of a Questionnaire
	 *
	 * @param filename
	 *            a String with the name of the experiment file
	 * @param userName
	 *            can be either the username of the user that is currently
	 *            completing the questionnaire or empty
	 * @param questionnaireID
	 *            is either empty or the questionnaireID of the questionnaire of
	 *            the experiment
	 */
	@Override
	public void setQuestionnairePreAndPostquestions(final String filename,
			final String questionnaireID, final boolean preAndPostquestions) throws IOException {

		final String jsonString = getExperiment(filename);
		final JSONObject jsonExperiment = new JSONObject(jsonString);

		final JSONArray questionnaires = jsonExperiment.getJSONArray("questionnaires");

		for (int i = 0; i < questionnaires.length(); i++) {

			JSONObject questionnaire = questionnaires.getJSONObject(i);

			if (questionnaire.getString("questionnareID").equals(questionnaireID)) {
				// update questionnaire with a false as preAndPostquestions
				questionnaire.remove("preAndPostquestions");
				questionnaire = questionnaire.put("preAndPostquestions", preAndPostquestions);
				// for saving in the correct experiment
				JSONObject data = new JSONObject();
				data = data.put("filename", filename);
				data = data.put("questionnaire", questionnaire);
				saveQuestionnaireServer(data.toString());
			}
		}
	}

	/**
	 * Sets boolean attribute eyeTracking of a Questionnaire
	 *
	 * @param filename
	 *            a String with the name of the experiment file
	 * @param userName
	 *            can be either the username of the user that is currently
	 *            completing the questionnaire or empty
	 * @param questionnaireID
	 *            is either empty or the questionnaireID of the questionnaire of
	 *            the experiment
	 */
	@Override
	public void setQuestionnaireEyeTracking(final String filename, final String questionnaireID,
			final boolean eyeTracking) throws IOException {

		final String jsonString = getExperiment(filename);
		final JSONObject jsonExperiment = new JSONObject(jsonString);

		final JSONArray questionnaires = jsonExperiment.getJSONArray("questionnaires");
		for (int i = 0; i < questionnaires.length(); i++) {

			JSONObject questionnaire = questionnaires.getJSONObject(i);

			if (questionnaire.getString("questionnareID").equals(questionnaireID)) {
				// update questionnaire with a false preAndPostquestions
				questionnaire.remove("eyeTracking");
				questionnaire = questionnaire.put("eyeTracking", eyeTracking);
				// for saving in the correct experiment
				JSONObject data = new JSONObject();
				data = data.put("filename", filename);
				data = data.put("questionnaire", questionnaire);
				saveQuestionnaireServer(data.toString());
			}
		}
	}

	/**
	 * Sets boolean attribute recordScreen of a Questionnaire
	 *
	 * @param filename
	 *            a String with the name of the experiment file
	 * @param userName
	 *            can be either the username of the user that is currently
	 *            completing the questionnaire or empty
	 * @param questionnaireID
	 *            is either empty or the questionnaireID of the questionnaire of
	 *            the experiment
	 */
	@Override
	public void setQuestionnaireRecordScreen(final String filename, final String questionnaireID,
			final boolean recordScreen) throws IOException {

		final String jsonString = getExperiment(filename);
		final JSONObject jsonExperiment = new JSONObject(jsonString);

		final JSONArray questionnaires = jsonExperiment.getJSONArray("questionnaires");

		for (int i = 0; i < questionnaires.length(); i++) {

			JSONObject questionnaire = questionnaires.getJSONObject(i);

			if (questionnaire.getString("questionnareID").equals(questionnaireID)) {
				// update questionnaire with a false preAndPostquestions
				questionnaire.remove("recordScreen");
				questionnaire = questionnaire.put("recordScreen", recordScreen);
				// for saving in the correct experiment
				JSONObject data = new JSONObject();
				data = data.put("filename", filename);
				data = data.put("questionnaire", questionnaire);
				saveQuestionnaireServer(data.toString());
			}
		}
	}

	/**
	 *
	 * @param experimentName
	 *            Name of the experiment as a String
	 * @param userID
	 *            id of the user as String
	 * @param eyeTrackingData
	 *            String tracking data of the eye of the user
	 * @return boolean true if upload is successful
	 * @throws IOException
	 */
	public boolean uploadEyeTrackingData(final String experimentName, final String userID,
			final String eyeTrackingData) throws IOException {

		// create a folder for saving answers if it does not exist
		final String answerFolder = FileSystemHelper.getExplorVizDirectory()
				+ "/experiment/answers";
		final User user = DBConnection.getUserByName(userID);
		final String pathname = answerFolder + File.separator + user.getQuestionnairePrefix()
				+ "/eyeTrackingData";

		final File folder = new File(pathname);

		if (!folder.exists()) {
			folder.mkdir();
		}

		final JSONObject jsonData = new JSONObject(eyeTrackingData);
		final boolean isUploadSuccessful = true;
		final Path folderPath = Paths.get(pathname + File.separator + userID + ".txt");

		final byte[] bytes = jsonData.toString(4).getBytes(StandardCharsets.UTF_8);

		try {
			Files.write(folderPath, bytes, StandardOpenOption.CREATE_NEW);
		} catch (final java.nio.file.FileAlreadyExistsException e) {
			Files.write(folderPath, bytes, StandardOpenOption.TRUNCATE_EXISTING);
		}

		return isUploadSuccessful;
	}

	/**
	 * Gets the eyetracking data of a user during a specific experiment and
	 * returns it as a String
	 *
	 * @param filename
	 *            is the filename of the experiment
	 * @param userID
	 *            of the user the eye tracking data is wanted for, as a String
	 * @return content is the eyetracking data of the user during an experiment
	 *         as a String
	 */
	public String getEyeTrackingData(final String filename, final String userID) {
		final User user = DBConnection.getUserByName(userID);
		final String filePath = EXP_ANSWER_FOLDER + File.separator + user.getQuestionnairePrefix()
				+ File.separator + "eyeTrackingData";
		byte[] encoded = null;
		String content = "";
		try {
			final Path path = Paths.get(filePath, user.getUsername() + ".txt");
			encoded = Files.readAllBytes(path);
			content = new String(encoded);
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return content;
	}

	/**
	 * Gets the questionnairePrefix for a specific user. Overloaded function.
	 *
	 * @param username
	 *            is the userID as String
	 * @return String with the questionnairePrefix
	 */
	public String getQuestionnairePrefix(final String username) {
		final User user = DBConnection.getUserByName(username);
		System.out.println(user.getQuestionnairePrefix());
		return user.getQuestionnairePrefix();
	}

	/**
	 * Copies a screenRecord video of a specific user at another location, so a
	 * video can access it as resource to replay it through access to the file
	 * on the explorViz website
	 *
	 * @param experimentName
	 *            is the name of the specific experiment as String
	 * @param userID
	 *            is the name of the user which screen Record is requested for
	 *            replay as String
	 * @return String containing a resourceLocation of the website source for
	 *         the video to replay the screenRecording
	 */
	public String getScreenRecordData(final String experimentName, final String userID) {
		final String DOWNLOAD_LOCATION = "/experiment/screenRecords/";
		final User user = DBConnection.getUserByName(userID);

		final String filePath = FileSystemHelper.getExplorVizDirectory() + "/experiment/answers"
				+ File.separator + user.getQuestionnairePrefix() + "/screenRecords";

		// load file
		final File file = new File(filePath + File.separator + user.getUsername() + ".mp4");

		// create folder path on website resources side if it does not exist
		final String path = getServletContext().getRealPath("") + DOWNLOAD_LOCATION;
		final File folderPath = new File(path);
		if (!folderPath.exists()) {
			folderPath.mkdir();
		}
		// save it at the websites resources
		final File newFile = new File(
				path + user.getQuestionnairePrefix() + user.getUsername() + ".mp4");

		byte[] fileBytes = null;
		try {
			newFile.createNewFile();
			fileBytes = loadFile(file);
			try {
				final FileOutputStream writer = new FileOutputStream(newFile);
				writer.write(fileBytes);
			} catch (final FileNotFoundException e) {
				e.printStackTrace();
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}

		// return location of the resource
		final String resourceLocation = new String(
				DOWNLOAD_LOCATION + user.getQuestionnairePrefix() + user.getUsername() + ".mp4");

		return resourceLocation;
	}

	/**
	 * Helper function for function getScreenRecordData to load a file
	 *
	 * @param file
	 *            as class File, who's to be loaded
	 * @return bytes is a byteArray containing the content of the parameter file
	 * @throws IOException
	 */
	private static byte[] loadFile(final File file) throws IOException {
		final InputStream is = new FileInputStream(file);

		final long length = file.length();
		if (length > Integer.MAX_VALUE) {
			// File is too large
		}
		final byte[] bytes = new byte[(int) length];

		int offset = 0;
		int numRead = 0;
		while ((offset < bytes.length)
				&& ((numRead = is.read(bytes, offset, bytes.length - offset)) >= 0)) {
			offset += numRead;
		}

		if (offset < bytes.length) {
			throw new IOException("Could not completely read file " + file.getName());
		}

		is.close();
		return bytes;
	}

	/**
	 * Downloads answers, experiment and if they are there, eyetracking data and
	 * screen records
	 *
	 * @param experimentFilename
	 *            String of filename
	 * @param userID
	 *            String of user, whose data is to be downloaded
	 * @return encoded String of a zip archive containing answers, experiment
	 *         and other data of a user
	 */
	@Override
	public String downloadDataOfUser(final String experimentFilename, final String userID)
			throws IOException {

		// # add user results and logs to zip #
		final JSONObject experimentJson = new JSONObject(getExperiment(experimentFilename));

		// # create zip #
		final File zip = new File(EXP_FOLDER + File.separator + "userData.zip");

		// # add .json to zip #
		final File experimentFile = new File(EXP_FOLDER + File.separator + experimentFilename);
		ZipUtil.packEntries(new File[] { experimentFile }, zip);

		final JSONArray jsonQuestionnaires = experimentJson.getJSONArray("questionnaires");
		final int length = jsonQuestionnaires.length();

		for (int i = 0; i < length; i++) {

			final JSONObject questionnaire = jsonQuestionnaires.getJSONObject(i);

			final String questPrefix = experimentJson.getString("ID") + "_"
					+ getQuestionnairePrefix(questionnaire.getString("questionnareID"),
							jsonQuestionnaires);

			final File answersFile = new File(EXP_ANSWER_FOLDER + File.separator + questPrefix
					+ File.separator + userID + ".csv");

			if (answersFile.exists()) {
				ZipUtil.addEntry(zip, "answers/" + questPrefix + "/" + userID + ".csv",
						answersFile);
			}

			final File eyeTrackingFile = new File(EXP_ANSWER_FOLDER + File.separator + questPrefix
					+ File.separator + "eyeTrackingData/" + userID + ".txt");
			if (eyeTrackingFile.exists()) {
				ZipUtil.addEntry(zip,
						"answers/" + questPrefix + "/eyeTrackingData/" + userID + ".txt",
						eyeTrackingFile);
			}

			final File recordScrFile = new File(EXP_ANSWER_FOLDER + File.separator + questPrefix
					+ File.separator + "screenRecords" + File.separator + userID + ".mp4");
			if (recordScrFile.exists()) {
				ZipUtil.addEntry(zip,
						"answers/" + questPrefix + "/screenRecords/" + userID + ".mp4",
						recordScrFile);
			}

			final File trackingFile = new File(FileSystemHelper.getExplorVizDirectory()
					+ File.separator + "usertracking" + File.separator + questPrefix
					+ File.separator + userID + "_tracking.log");

			if (trackingFile.exists()) {

				ZipUtil.addEntry(zip,
						"usertracking/" + questPrefix + "/" + userID + "_tracking.log",
						trackingFile);

			}
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

	/**
	 * remove files from local temp folder of videoData on webserver if possible
	 */
	@Override
	public void removeLocalVideoData() throws JSONException, IOException {
		final File answersFolder = new File(
				getServletContext().getRealPath("") + LOCAL_VIDEO_DATA_FOLDER);
		final File[] listOfFiles = answersFolder.listFiles();
		if (listOfFiles != null) {
			for (final File file : listOfFiles) {
				if (file.isFile()) {
					final Path fileToRemove = Paths.get(file.getAbsolutePath());
					try {
						removeFileByPath(fileToRemove);
					} catch (final IOException e) {
						// do nothing, it will be removed next time
					}
				}
			}
		}
	}

	/**
	 * Checks whether the given file exists in experiment answer folder
	 *
	 * @param filename
	 *            String name with a filename
	 * @return boolean whether filename exists or not
	 */
	public boolean existsFileInsideAnswerFolder(final String filename) {
		final File maybeFile = new File(EXP_ANSWER_FOLDER + File.separator + filename);
		return maybeFile.exists();
	}

	/**
	 * Check whether a file exists for all users who are assigned to a given
	 * questionnaire, inside a folder 'path'
	 *
	 * @param questionnairePrefix
	 *            is a String with the name of the experiment and questionnaire
	 *            (form : expXXX_qestXXX)
	 * @param path
	 *            String in form '/eyeTrackingData' or '/screenRecords'
	 * @return String containing a list of entries, with username as key and
	 *         value true or false
	 */
	public String existsFilesForAllUsers(final String questionnairePrefix, final String path) {
		// get users, for each user a hashmap entry
		// check whether for this user exists inside the path a file
		final JSONArray jsonUsers = DBConnection.getQuestionnaireUsers(questionnairePrefix);
		final JSONObject usersFilesExists = new JSONObject();

		// add correct tfile ending to filename
		String fileEnding = null;
		if (path.equals("/screenRecords")) {
			fileEnding = ".mp4";
		} else if (path.equals("/eyeTrackingData")) {
			fileEnding = ".txt";
		}

		for (int i = 0; i < jsonUsers.length(); i++) {
			final String filename = questionnairePrefix + path + File.separator
					+ jsonUsers.getJSONObject(i).getString("username") + fileEnding;
			usersFilesExists.put(jsonUsers.getJSONObject(i).getString("username"),
					existsFileInsideAnswerFolder(filename));
		}
		return usersFilesExists.toString();
	}

}
