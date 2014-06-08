package explorviz.visualization.experiment.callbacks

import com.google.gwt.user.client.rpc.AsyncCallback
import explorviz.visualization.experiment.ExperimentJS
import explorviz.visualization.engine.Logging

class TextCallback implements AsyncCallback<String> {
			
	override onFailure(Throwable caught) {
		Logging.log("Failure on textCallback: "+ caught.message)
	}
	
	override onSuccess(String result) {
		ExperimentJS.changeTutorialDialog(result)	
	}
}