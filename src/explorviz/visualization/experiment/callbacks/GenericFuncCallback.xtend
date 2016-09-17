package explorviz.visualization.experiment.callbacks

import com.google.gwt.user.client.rpc.AsyncCallback
import explorviz.visualization.main.ErrorDialog

class GenericFuncCallback<T> implements AsyncCallback<T> {
	
	var (T)=>void callback

	new() {
	}

	new((T)=>void x) {
		this.callback = x
	}
	
	override onFailure(Throwable caught) {
		ErrorDialog::showError(caught)
	}
	
	override onSuccess(T result) {
		callback.apply(result)
	}
	
}