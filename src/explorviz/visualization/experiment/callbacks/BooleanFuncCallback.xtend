package explorviz.visualization.experiment.callbacks

import com.google.gwt.user.client.rpc.AsyncCallback
import explorviz.visualization.main.ErrorDialog

class BooleanFuncCallback<T> implements AsyncCallback<T> {

	var (boolean)=>void callback

	new() {
	}

	new((boolean)=>void x) {
		this.callback = x
	}

	override onFailure(Throwable caught) {
		ErrorDialog::showError(caught)
	}

	override onSuccess(T result) {
		var x = result as Boolean
		callback.apply(x.booleanValue)
	}


}