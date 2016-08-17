package explorviz.visualization.experiment.callbacks

import com.google.gwt.user.client.rpc.AsyncCallback
import explorviz.visualization.main.ErrorDialog

class VoidFuncCallback<T> implements AsyncCallback<T> {

	var () => void callback

	new() {
	}

	new(() => void x) {
		this.callback = x
	}

	override onFailure(Throwable caught) {
		ErrorDialog::showError(caught)
	}

	override onSuccess(T result) {
		callback.apply()
	}

}
