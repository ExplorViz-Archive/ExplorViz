package explorviz.visualization.experiment.callbacks

import com.google.gwt.user.client.rpc.AsyncCallback
import explorviz.visualization.experiment.ExperimentJS
import explorviz.visualization.main.ErrorDialog

class TextCallback implements AsyncCallback<String> {
			
	override onFailure(Throwable caught) {
		ErrorDialog::showError(caught)
	}
	
	override onSuccess(String result) {
		ExperimentJS.changeTutorialDialog(result)	
	}
}