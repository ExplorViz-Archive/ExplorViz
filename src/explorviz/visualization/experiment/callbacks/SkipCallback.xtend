package explorviz.visualization.experiment.callbacks

import com.google.gwt.user.client.rpc.AsyncCallback
import explorviz.visualization.experiment.Questionnaire
import explorviz.visualization.main.ErrorDialog

import static explorviz.visualization.experiment.Questionnaire.*

/**
 * Callback to get the value of the skip-configuration.
 * @author Santje Finke
 */
class SkipCallback implements AsyncCallback<Boolean> {
	
	override onFailure(Throwable caught) {
		ErrorDialog::showError(caught)
	}
	
	override onSuccess(Boolean result) {
		Questionnaire::allowSkip = result
	}
	
}