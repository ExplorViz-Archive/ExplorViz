package explorviz.server.experiment;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import explorviz.shared.experiment.Answer;
import explorviz.shared.experiment.Question;
import explorviz.visualization.engine.Logging;
import explorviz.visualization.experiment.services.QuestionService;

public class QuestionServiceImpl extends RemoteServiceServlet implements QuestionService {

	private static final long serialVersionUID = 3071142731982595657L;

	@Override
	public Question[] getQuestions() throws IOException {
		final ArrayList<Question> questions = new ArrayList<Question>();
		String filePath = new File("").getAbsolutePath();
		filePath = filePath + "/../experiment/questions.txt";
		BufferedReader br = null;
		String text, answers, corrects, time;
		try {
			br = new BufferedReader(new FileReader(filePath));
			// read text
			text = br.readLine();
			int i = 0;
			while (null != text) {
				// read answers
				answers = br.readLine();
				// read correct answers
				corrects = br.readLine();
				// read timestamp
				time = br.readLine();
				questions.add(new Question(i, text, answers, corrects, time));
				// read text
				text = br.readLine();
				i++;
			}
			br.close();
		} catch (final FileNotFoundException e) {
			Logging.log(e.getMessage());

		}
		return questions.toArray(new Question[0]);
	}

	@Override
	public void writeAnswer(final List<Answer> answers) throws IOException {
		BufferedWriter out = null;
		final String id = answers.get(0).getUserID();
		try {
			out = new BufferedWriter(new FileWriter("./answers" + id + ".csv", true));
			for (final Answer a : answers) {
				out.write(a.toCSV());
			}
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			out.close();
		}
	}
}
