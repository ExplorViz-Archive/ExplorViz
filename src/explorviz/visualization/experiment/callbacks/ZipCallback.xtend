package explorviz.visualization.experiment.callbacks

import com.google.gwt.user.client.rpc.AsyncCallback
import explorviz.visualization.main.ErrorDialog
import explorviz.visualization.main.JSHelpers

/**
 * Callback to save the fetched content in a file called 'answers.zip'.
 * @author Santje Finke
 */
class ZipCallback implements AsyncCallback<String> {
	
	var String filename = null
	
	new(String filename) {
		this.filename = filename
	}
	
	new() {}
	
	override onFailure(Throwable caught) {
		ErrorDialog::showError(caught)
	}
	
	override onSuccess(String content) {
		if(filename == null) {
			JSHelpers::downloadAsZip("answers.zip", JSHelpers::toByte64(content))
		}
		else {
			JSHelpers::downloadAsZip(filename, JSHelpers::toByte64(content))
		}
		
	}
	
}