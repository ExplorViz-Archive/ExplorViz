package explorviz.server.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

import org.apache.commons.codec.binary.Base64;
import org.json.*;
import org.zeroturnaround.zip.ZipUtil;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import explorviz.server.database.DBConnection;
import explorviz.server.main.FileSystemHelper;
import explorviz.shared.experiment.Question;
import explorviz.visualization.engine.Logging;
import explorviz.visualization.experiment.services.JSONService;

public class JSONServiceImpl extends RemoteServiceServlet implements JSONService {

	private static final long serialVersionUID = 6576514774419481521L;

	// private static String FULL_FOLDER =
	// FileSystemHelper.getExplorVizDirectory() + File.separator;

	private static String EXP_FOLDER = FileSystemHelper.getExplorVizDirectory() + File.separator
			+ "experiment";

	// private static String EXP_ANSWER_FOLDER =
	// FileSystemHelper.getExplorVizDirectory()
	// + File.separator + "experiment" + File.separator + "answers";

	private static String LANDSCAPE_FOLDER = FileSystemHelper.getExplorVizDirectory()
			+ File.separator + "replay";

	/////////////////
	// RPC Methods //
	/////////////////

	@Override
	public void saveJSONOnServer(final String json) throws IOException {
		final JSONObject jsonObj = new JSONObject(json);

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
	public String getQuestionnaireDetails(final String data) {

		final JSONObject filenameAndQuestionnaireTitle = new JSONObject(data);
		final String filename = filenameAndQuestionnaireTitle.keySet().iterator().next();

		final String jsonString = getExperiment(filename);
		final JSONObject jsonExperiment = new JSONObject(jsonString);

		final String questionnaireName = filenameAndQuestionnaireTitle.getString(filename);

		final JSONArray questionnaires = jsonExperiment.getJSONArray("questionnaires");

		final JSONObject jsonDetails = new JSONObject();

		for (int i = 0; i < questionnaires.length(); i++) {

			final JSONObject questionnaire = questionnaires.getJSONObject(i);

			if (questionnaire.get("questionnareTitle").equals(questionnaireName)) {
				jsonDetails.putOnce("questionnareTitle", questionnaire.get("questionnareTitle"));
				jsonDetails.putOnce("questionnarePrefix", questionnaire.get("questionnarePrefix"));
				jsonDetails.putOnce("questionnareID", questionnaire.get("questionnareID"));

				final int numberOfQuestionnaires = questionnaire.getJSONArray("questions").length();
				jsonDetails.putOnce("numQuestions", numberOfQuestionnaires);

				// final List<String> landscapeNames =
				// getLandScapeNamesOfExperiment(filename);
				jsonDetails.putOnce("landscapes", "TODO");

				jsonDetails.putOnce("numUsers", "TODO");

				jsonDetails.putOnce("started", "TODO");
				jsonDetails.putOnce("ended", "TODO");

				break;
			}

		}

		return jsonDetails.toString();

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
	public String downloadExperimentData(final String filename) throws IOException {

		final File zip = new File(EXP_FOLDER + File.separator + "experimentData.zip");

		// # create zip and add files via packEntries # ;
		final File experimentJson = new File(EXP_FOLDER + File.separator + filename);

		// TODO add User results
		// TODO add User logs

		ZipUtil.packEntries(new File[] { experimentJson }, zip);

		final List<String> landscapeNames = getLandScapeNamesOfExperiment(filename);
		for (final String landscapeName : landscapeNames) {

			final File landscape = new File(
					LANDSCAPE_FOLDER + File.separator + landscapeName + ".expl");

			ZipUtil.addEntry(zip, "/" + landscapeName + ".expl", landscape);

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
				final JSONObject jsonObj = new JSONObject(json);

				if (jsonObj.has("questionnaires")) {

					final JSONArray questionnaires = jsonObj.getJSONArray("questionnaires");

					final ArrayList<String> questionNames = new ArrayList<>();

					for (int i = 0; i < questionnaires.length(); i++) {
						questionNames.add(questionnaires.getJSONObject(i).get("questionnareTitle")
								.toString());
					}

					final JSONObject questionnaireObj = new JSONObject();
					questionnaireObj.put(jsonObj.get("title").toString(), questionNames);

					data.put(jsonObj.get("filename").toString(), questionnaireObj);

				} else {
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
		final String users = DBConnection.createUsersForQuestionnaire(prefix, count);
		return users;
	}

	@Override
	public void removeQuestionnaireUser(final String username) {
		DBConnection.removeUser(username);
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

		// calculate prefix fpr questionnaire => get users
		final String prefix = jsonExperiment.getString("prefix") + "_"
				+ getQuestionnairePrefix(questionnaireName, questionnaires);

		final JSONObject jsonUsers = new JSONObject(DBConnection.getUsersByPrefix(prefix));

		returnObj.put("users", jsonUsers.get("users"));

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

		// TODO need questionnaire id as well

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

}
