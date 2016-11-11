package explorviz.visualization.experiment.callbacks

import com.google.gwt.user.client.rpc.AsyncCallback
import explorviz.visualization.main.ErrorDialog
import elemental.json.JsonObject
import elemental.json.Json

class JsonExperimentCallback<T> implements AsyncCallback<T> {
	
	var (String)=>void callback
	var JsonObject additionalValues

	new() {
	}

	new((String)=>void x, JsonObject additionalValues) {
		this.callback = x
		this.additionalValues = additionalValues
	}
	
	override onFailure(Throwable caught) {
		ErrorDialog::showError(caught)
	}
	
	override onSuccess(T result) {		
		
		var JsonObject obj = Json.createObject
		
		if(additionalValues != null) {
			var keys = additionalValues.keys	
			var size = keys.length
			
			for(var i = 0; i < size; i++) {
				var jsonObject = additionalValues.getString(keys.get(i))
				
				obj.put(keys.get(i), jsonObject)
			}
		}
			
		obj.put("experiment", result as String)
		
		callback.apply(obj.toJson)
	}
	
}