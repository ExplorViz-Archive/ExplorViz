package explorviz.visualization.codeviewer

import com.google.gwt.user.client.rpc.AsyncCallback
import explorviz.visualization.main.ErrorDialog

class CodeViewerRenderCodeStructure<T> implements AsyncCallback<T> {
	override onFailure(Throwable caught) {
		ErrorDialog::showError(caught)
	}

	override onSuccess(T result) {
		val codeStructure = result as String

		CodeMirrorJS::fillCodeTree(codeStructure)
	}
}
