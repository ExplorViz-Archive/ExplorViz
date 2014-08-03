package explorviz.visualization.experiment.callbacks

import com.google.gwt.user.client.rpc.AsyncCallback
import explorviz.visualization.main.ErrorDialog
import explorviz.visualization.experiment.Questionnaire

class QuestionTimeCallback implements AsyncCallback<Integer> {
	
	override onFailure(Throwable caught) {
		ErrorDialog::showError(caught)
	}
	
	override onSuccess(Integer result) {
		Questionnaire::questionMaxTime = result
	}
	
}