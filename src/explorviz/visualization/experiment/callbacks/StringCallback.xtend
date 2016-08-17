package explorviz.visualization.experiment.callbacks

import com.google.gwt.user.client.rpc.AsyncCallback
import explorviz.visualization.main.ErrorDialog

class StringCallback<T> implements AsyncCallback<T> {

	var (String)=>void callback

	new() {
	}

	new((String)=>void x) {
		this.callback = x
	}

	override onFailure(Throwable caught) {
		ErrorDialog::showError(caught)
	}

	override onSuccess(T result) {
		callback.apply(result as String)
	}

}
