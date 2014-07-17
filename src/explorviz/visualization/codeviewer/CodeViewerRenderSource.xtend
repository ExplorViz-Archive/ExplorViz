package explorviz.visualization.codeviewer

import com.google.gwt.user.client.rpc.AsyncCallback
import explorviz.visualization.main.ErrorDialog

class CodeViewerRenderSource<T> implements AsyncCallback<T> {
	var String filename

	new(String filenameParam) {
		filename = filenameParam
	}

	override onFailure(Throwable caught) {
		ErrorDialog::showError(caught)
	}

	override onSuccess(T result) {
		val source = result as String

		CodeMirrorJS::showCode(source, filename)
	}
}
