package explorviz.visualization.experiment.services;

import java.io.IOException;
import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import explorviz.shared.experiment.Answer;
import explorviz.shared.experiment.Question;

@RemoteServiceRelativePath("question")
public interface QuestionService extends RemoteService {
	public Question[] getQuestions() throws IOException;

	public void writeAnswer(List<Answer> answers) throws IOException;
}
