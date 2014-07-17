package explorviz.visualization.codeviewer

import com.google.gwt.user.client.rpc.AsyncCallback
import explorviz.visualization.main.ErrorDialog
import explorviz.visualization.main.AlertDialogJS

class CodeViewerRenderCodeStructure<T> implements AsyncCallback<T> {
	override onFailure(Throwable caught) {
		ErrorDialog::showError(caught)
	}

	override onSuccess(T result) {
		val codeStructure = result as String

		if (codeStructure.contains("empty source folder")) {
			AlertDialogJS::showAlertDialog("Code Viewer Error", "Sorry, source code for application " + CodeViewer::currentProject + " is unavailable.")
		} else {
			CodeMirrorJS::openDialog(CodeViewer::currentProject)
			CodeMirrorJS::fillCodeTree(codeStructure)
		}
	}
}
