package explorviz.visualization.experiment.callbacks

import com.google.gwt.user.client.rpc.AsyncCallback
import explorviz.shared.experiment.Question
import explorviz.visualization.experiment.Questionnaire
import java.util.List
import java.util.ArrayList
import explorviz.visualization.main.ErrorDialog

/**
 * A callback to copy the questions fetched from the server into the question list.
 * @author Santje Finke
 */
class QuestionsCallback implements AsyncCallback<Question[]> {
	
	override onFailure(Throwable caught) {
		ErrorDialog::showError(caught)
	}
	
	override onSuccess(Question[] result) {
		var List<Question> list = new ArrayList<Question>();
		for(Question q : result){
			list.add(q)
		}
		Questionnaire::questions = list
	}
	
}