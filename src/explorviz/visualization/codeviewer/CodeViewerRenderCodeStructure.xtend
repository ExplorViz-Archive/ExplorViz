package explorviz.visualization.codeviewer

import com.google.gwt.user.client.rpc.AsyncCallback
import explorviz.visualization.main.ErrorDialog
import explorviz.visualization.main.AlertDialogJS

class CodeViewerRenderCodeStructure<T> implements AsyncCallback<T> {
	String filepath
	String filename
	
	
	new(String filepath, String filename) {
		this.filepath = filepath
		this.filename = filename
	}
	
	override onFailure(Throwable caught) {
		ErrorDialog::showError(caught)
	}

	override onSuccess(T result) {
		val codeStructure = result as String

		if (codeStructure.contains("empty source folder")) {
			AlertDialogJS::showAlertDialog("Code Viewer Error", "Sorry, source code for application " + CodeViewer::currentProject + " is unavailable.")
		} else {
			CodeMirrorJS::openDialog(CodeViewer::currentProject)
			CodeMirrorJS::fillCodeTree(codeStructure, filename)
			
			CodeViewer::getCode(filepath, filename);
		}
	}
}
