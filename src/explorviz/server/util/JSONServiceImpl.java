package explorviz.server.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import explorviz.server.main.FileSystemHelper;
import explorviz.shared.experiment.Question;
import explorviz.visualization.engine.Logging;
import explorviz.visualization.experiment.services.JSONService;

public class JSONServiceImpl extends RemoteServiceServlet implements JSONService {

	private static final long serialVersionUID = 6576514774419481521L;

	private static String FULL_FOLDER = FileSystemHelper.getExplorVizDirectory() + File.separator
			+ "experiment";

	@Override
	public String getJSON() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void sendJSON(final String json) throws IOException {
		final JSONObject jsonObj = new JSONObject(json);
		final String filename = jsonObj.getString("filename");
		final Path experimentFolder = Paths.get(FULL_FOLDER + File.separator + filename);
		final byte[] bytes = jsonObj.toString(4).getBytes(StandardCharsets.UTF_8);

		try {
			Files.write(experimentFolder, bytes, StandardOpenOption.CREATE_NEW);
		} catch (final java.nio.file.FileAlreadyExistsException e) {
			Files.write(experimentFolder, bytes, StandardOpenOption.TRUNCATE_EXISTING);
		}
	}

	@Override
	public List<String> getExperimentFilenames() {
		final List<String> filenames = new ArrayList<String>();
		final File directory = new File(FULL_FOLDER);

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
		final File directory = new File(FULL_FOLDER);

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
	public Question[] getQuestionsOfExp(final String title) {

		final ArrayList<Question> questions = new ArrayList<Question>();

		final String jsonString = readExperiment(getFilename(title));
		final JSONArray jsonQuestions = new JSONObject(jsonString).getJSONArray("questions");

		final int length = jsonQuestions.length();

		String text;
		String type;
		String[] answers;
		ArrayList<String> corrects;
		int procTime;
		long timestamp;

		for (int i = 0; i < length; i++) {

			final JSONObject jsonObj = jsonQuestions.getJSONObject(i);

			text = jsonObj.getString("questionText");
			type = jsonObj.getString("type");

			procTime = Integer.parseInt(jsonObj.getString("workingTime"));
			// timestamp = Long.parseLong(jsonObj.getString("expLandscape"));
			timestamp = 1L;

			final JSONArray correctsArray = jsonObj.getJSONArray("answers");
			final int lengthQuestions = correctsArray.length();

			corrects = new ArrayList<String>();
			answers = new String[lengthQuestions];

			for (int j = 0; j < lengthQuestions; j++) {
				final JSONObject jsonAnswer = correctsArray.getJSONObject(j);

				answers[j] = jsonAnswer.keySet().iterator().next();

				if (jsonAnswer.get(answers[j]).toString().equals("true")) {

					corrects.add(answers[j]);

				}

			}

			final Question question = new Question(i, type, text, answers,
					corrects.toArray(new String[0]), procTime, timestamp);

			questions.add(question);
		}

		return questions.toArray(new Question[0]);
	}

	@Override
	public String getExperimentByTitle(final String title) {
		final String jsonString = readExperiment(getFilename(title));

		return jsonString;
	}

	@Override
	public void removeExperiment(final String title) {
		final String filename = getFilename(title);
		final Path experimentFile = Paths.get(FULL_FOLDER + File.separator + filename);
		try {
			Files.delete(experimentFile);
		} catch (final IOException e) {
			Logging.log("Experiment " + filename + " could not be removed");
		}
	}

	@Override
	public String getExperimentDetails(final String title) {
		final String jsonString = readExperiment(getFilename(title));

		final JSONObject jsonExperiment = new JSONObject(jsonString);

		final JSONObject jsonDetails = new JSONObject();

		jsonDetails.putOnce("title", jsonExperiment.get("title"));

		jsonDetails.putOnce("prefix", jsonExperiment.get("prefix"));

		final int numberOfQuestions = jsonExperiment.getJSONArray("questions").length();
		jsonDetails.putOnce("numQuestions", numberOfQuestions);

		// TODO used Landscapes

		// TODO started / ended pair array

		// TODO number of users

		return jsonDetails.toString();

	}

	private String readExperiment(final String filename) {
		byte[] jsonBytes = null;
		try {
			jsonBytes = Files.readAllBytes(Paths.get(FULL_FOLDER + File.separator + filename));
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return new String(jsonBytes, StandardCharsets.UTF_8);

	}

	private String getFilename(final String title) {
		final List<String> filenames = getExperimentFilenames();
		for (final String filename : filenames) {
			final String currentTitle = new JSONObject(readExperiment(filename)).getString("title");
			if (currentTitle.equals(title)) {
				return filename;
			}
		}
		return null;
	}

	@Override
	public void duplicateExperiment(final String json) throws IOException {
		final JSONObject jsonObj = new JSONObject(getExperimentByTitle(json));
		final String title = jsonObj.getString("title");
		jsonObj.put("title", title + "_dup");
		jsonObj.put("filename", "exp_" + (new Date().getTime()) + ".json");
		sendJSON(jsonObj.toString());
	}

}
