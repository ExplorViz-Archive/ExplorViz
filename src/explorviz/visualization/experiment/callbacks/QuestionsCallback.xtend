package explorviz.visualization.experiment.callbacks

import com.google.gwt.user.client.rpc.AsyncCallback
import explorviz.shared.experiment.Question
import explorviz.visualization.engine.Logging
import explorviz.visualization.experiment.Questionnaire
import java.util.List
import java.util.ArrayList

class QuestionsCallback implements AsyncCallback<Question[]> {
	
	override onFailure(Throwable caught) {
		Logging.log("Failure on questionCallback: "+ caught.message)
	}
	
	override onSuccess(Question[] result) {
		var List<Question> list = new ArrayList<Question>();
		for(Question q : result){
			list.add(q)
		}
		Questionnaire::questions = list
	}
	
}