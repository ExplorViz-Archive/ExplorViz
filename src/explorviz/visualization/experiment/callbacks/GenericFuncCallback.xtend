package explorviz.visualization.experiment.callbacks

import com.google.gwt.user.client.rpc.AsyncCallback

class GenericFuncCallback<T> implements AsyncCallback<T> {
	
	var (T)=>void callback

	new() {
	}

	new((T)=>void x) {
		this.callback = x
	}
	
	override onFailure(Throwable caught) {
		throw new UnsupportedOperationException("TODO: auto-generated method stub")
	}
	
	override onSuccess(T result) {
		callback.apply(result)
	}
	
}