package explorviz.visualization.experiment.callbacks

import com.google.gwt.user.client.rpc.AsyncCallback
import explorviz.visualization.main.ErrorDialog
import elemental.json.JsonObject
import elemental.json.Json

class StringWithJSONCallback<T> implements AsyncCallback<T> {

	var (String)=>void callback
	var String par

	new() {
	}

	new((String)=>void x, String par) {
		this.callback = x
		this.par = par
	}

	override onFailure(Throwable caught) {
		ErrorDialog::showError(caught)
	}

	override onSuccess(T result) {
		var JsonObject obj = Json.createObject
		obj.put(par, result as String)
		
		callback.apply(obj.toJson)
	}

}
