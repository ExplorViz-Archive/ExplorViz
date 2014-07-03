package explorviz.server.experiment;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import explorviz.server.main.Configuration;
import explorviz.server.main.FileSystemHelper;
import explorviz.shared.experiment.Answer;
import explorviz.shared.experiment.Question;
import explorviz.visualization.engine.Logging;
import explorviz.visualization.experiment.services.QuestionService;

public class QuestionServiceImpl extends RemoteServiceServlet implements QuestionService {

	private static final long serialVersionUID = 3071142731982595657L;
	private static FileOutputStream answerFile;

	@Override
	public Question[] getQuestions() throws IOException {
		final ArrayList<Question> questions = new ArrayList<Question>();
		try {
			final String filePath = getServletContext().getRealPath("/experiment/")
					+ "/questions.txt";
			BufferedReader br = null;
			String text, answers, corrects, time, free;
			br = new BufferedReader(new FileReader(filePath));
			text = br.readLine(); // read text
			int i = 0;
			while (null != text) {
				answers = br.readLine(); // read answers
				corrects = br.readLine(); // read correct answers
				free = br.readLine(); // read amount of free inputs
				time = br.readLine(); // read timestamp
				questions.add(new Question(i, text, answers, corrects, free, time));
				text = br.readLine(); // read text of next question
				i++;
			}
			br.close();
		} catch (final FileNotFoundException e) {
			Logging.log(e.getMessage());

		}
		return questions.toArray(new Question[0]);
	}

	@Override
	public void writeAnswer(final Answer answer) throws IOException {
		String id = answer.getUserID();
		if (id.equals("")) {
			id = "DummyUser";
		}
		try {
			answerFile = new FileOutputStream(new File(FileSystemHelper.getExplorVizDirectory()
					+ "/experiment/" + "/" + id + ".csv"), true);

			answerFile.write(answer.toCSV().getBytes("UTF-8"));
			answerFile.flush();
		} catch (final FileNotFoundException e) {
			Logging.log(e.getMessage());
		}
	}

	@Override
	public void writeString(final String string, final String id) throws IOException {
		// if (FOLDER == null) {
		// FOLDER = FileSystemHelper::getExplorVizDirectory() + "/" +
		// "rsfExport"
		// new File(FOLDER).mkdir()
		// }

		try {
			answerFile = new FileOutputStream(new File(FileSystemHelper.getExplorVizDirectory()
					+ "/experiment/" + "/" + id + ".csv"), true);
			final String writeString = id + "," + string;
			answerFile.write(writeString.getBytes("UTF-8"));
			answerFile.flush();
		} catch (final FileNotFoundException e) {
			Logging.log(e.getMessage());
		}
	}

	@Override
	public String[] getVocabulary() throws IOException {
		final List<String> vocab = new ArrayList<String>();
		try {
			final String filePath = getServletContext().getRealPath("/experiment/") + "/"
					+ Configuration.selectedLanguage + "Vocabulary.txt";
			BufferedReader br = null;
			String line;
			br = new BufferedReader(new FileReader(filePath));
			line = br.readLine();
			while (null != line) {
				vocab.add(line);
				line = br.readLine();
			}
			br.close();
		} catch (final FileNotFoundException e) {
			Logging.log(e.getMessage());

		}
		return vocab.toArray(new String[0]);
	}
}
