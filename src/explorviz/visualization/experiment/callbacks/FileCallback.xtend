package explorviz.visualization.experiment.callbacks

import com.google.gwt.user.client.rpc.AsyncCallback
import explorviz.visualization.main.ErrorDialog
import explorviz.visualization.main.JSHelpers
import explorviz.visualization.engine.Logging

class FileCallback implements AsyncCallback<String[][]> {
	
	override onFailure(Throwable caught) {
		ErrorDialog::showError(caught)
	}
	
	override onSuccess(String[][] result) {
		Logging.log("Downloading files")
		for(var i = 0; i < result.length; i++){
			var filename = result.get(i).get(0)
			var content = result.get(i).get(1)
			JSHelpers::downloadAsFile(filename, content)
		}
	}
	
}