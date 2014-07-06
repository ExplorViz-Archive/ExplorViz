package explorviz.visualization.main

import com.google.gwt.user.client.rpc.StatusCodeException

class ErrorDialog {
	static def void showError(Throwable caught) {
		if (caught instanceof StatusCodeException) {
			if (caught.statusCode == 0) {
				ErrorDialogJS::showErrorDialog("The server is not reachable.<br>Please check your network connection.",
					caught.getMessage())
			} else if (caught.statusCode == 0) {
				ErrorDialogJS::showErrorDialog("The specified endpoint/page was not found.",
					caught.getMessage())
			}
		} else {
			ErrorDialogJS::showErrorDialog("An unexpected error occurred.", caught.message + "<br>" + createStackStringFromThrowable(caught))
		}
	}
	
	def static createStackStringFromThrowable(Throwable t) {
		var stack = ""
		for (var int i = 0; i < t.stackTrace.length; i++) {
			stack = stack + "<br>" + (t.stackTrace.get(i))
		}
		stack
	}
}
