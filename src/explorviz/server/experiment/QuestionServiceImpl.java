package explorviz.server.experiment;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;

import org.zeroturnaround.zip.ZipUtil;

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
	private static String folder;

	@Override
	public Question[] getQuestions() throws IOException {
		final ArrayList<Question> questions = new ArrayList<Question>();
		try {
			final String filePath = getServletContext().getRealPath("/experiment/")
					+ "/questions.txt";
			String text, answers, corrects, time, free;
			final BufferedReader br = new BufferedReader(new FileReader(filePath));
			text = br.readLine(); // read text
			int i = 0;
			while (null != text) {
				answers = br.readLine(); // read answers
				corrects = br.readLine(); // read correct answers
				free = br.readLine(); // read amount of free inputs
				time = br.readLine(); // read timestamp
				// Logging.log("Time is: " + time);
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
		writeString(answer.toCSV(), id);
	}

	@Override
	public void writeString(final String string, final String id) throws IOException {
		if (folder == null) {
			folder = FileSystemHelper.getExplorVizDirectory() + "/" + "experiment";
			new File(folder).mkdir();
		}

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
			final BufferedReader br = new BufferedReader(new FileReader(filePath));
			String line;
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

	@Override
	public void setMaxTimestamp(final long timestamp) {
		LandscapeReplayer.getReplayerForCurrentUser().setMaxTimestamp(timestamp);
	}

	@Override
	public void downloadAnswers() throws IOException {
		final List<Byte> result = new ArrayList<Byte>();
		final File folder = new File(FileSystemHelper.getExplorVizDirectory() + "/experiment/");
		final File zip = new File(FileSystemHelper.getExplorVizDirectory() + "/" + "answers.zip");
		ZipUtil.pack(folder, zip);

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

		final JFileChooser ch = new JFileChooser();
		final int action = ch.showSaveDialog(null);
		if ((action == JFileChooser.CANCEL_OPTION) || (action == JFileChooser.ERROR_OPTION)) {

		} else {
			final File saveTo = ch.getSelectedFile();
			final FileOutputStream os = new FileOutputStream(saveTo + ".zip");
			os.write(buf);
			os.close();
		}

		return;
	}
}
