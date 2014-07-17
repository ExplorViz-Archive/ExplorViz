package explorviz.visualization.experiment.callbacks

import com.google.gwt.user.client.rpc.AsyncCallback
import explorviz.visualization.main.ErrorDialog
import explorviz.visualization.main.JSHelpers

class ZipCallback implements AsyncCallback<String> {
	
	override onFailure(Throwable caught) {
		ErrorDialog::showError(caught)
	}
	
	override onSuccess(String content) {
		JSHelpers::downloadAsZip("answers.zip", content)
	}
	
}