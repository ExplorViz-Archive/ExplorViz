package explorviz.visualization.experiment.services;

import java.io.IOException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import explorviz.shared.experiment.Answer;
import explorviz.shared.experiment.Question;

@RemoteServiceRelativePath("questionservice")
public interface QuestionService extends RemoteService {
	public Question[] getQuestions() throws IOException;

	public void writeAnswer(Answer answer) throws IOException;

	public String[] getVocabulary() throws IOException;

	public void writeString(String string, String id) throws IOException;

	void setMaxTimestamp(long timestamp);

	void downloadAnswers() throws IOException;

}
