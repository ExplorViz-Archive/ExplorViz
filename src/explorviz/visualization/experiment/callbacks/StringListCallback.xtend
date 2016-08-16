package explorviz.visualization.experiment.callbacks

import com.google.gwt.user.client.rpc.AsyncCallback
import explorviz.visualization.main.ErrorDialog
import java.util.List

class StringListCallback<T> implements AsyncCallback<T> {

	var (List<String>)=>void callback

	new() {
	}

	new((List<String>)=>void x) {
		this.callback = x
	}

	override onFailure(Throwable caught) {
		ErrorDialog::showError(caught)
	}

	override onSuccess(T result) {
		callback.apply(result as List<String>)
	}

}
