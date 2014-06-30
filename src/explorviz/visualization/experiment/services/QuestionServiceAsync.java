package explorviz.visualization.experiment.services;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import explorviz.shared.experiment.Answer;
import explorviz.shared.experiment.Question;

public interface QuestionServiceAsync {

	void getQuestions(AsyncCallback<Question[]> callback);

	void writeAnswer(List<Answer> answers, AsyncCallback<Void> callback);

}
