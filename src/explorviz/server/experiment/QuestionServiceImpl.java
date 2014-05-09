package explorviz.server.experiment;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import explorviz.shared.experiment.Answer;
import explorviz.shared.experiment.Question;
import explorviz.visualization.experiment.services.QuestionService;

public class QuestionServiceImpl extends RemoteServiceServlet implements QuestionService {

	private static final long serialVersionUID = 3071142731982595657L;

	@Override
	public List<Question> getQuestions() throws IOException {
		final ArrayList<Question> questions = new ArrayList<Question>();
		// TODO
		final Question q = null;

		questions.add(q);

		return questions;
	}

	@Override
	public void writeAnswer(final List<Answer> answers) throws IOException {
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new FileWriter("./answers.csv", true));
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
