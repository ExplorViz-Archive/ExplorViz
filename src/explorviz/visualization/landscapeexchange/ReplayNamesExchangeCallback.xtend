package explorviz.visualization.landscapeexchange

import com.google.gwt.user.client.rpc.AsyncCallback
import explorviz.visualization.main.ErrorDialog
import explorviz.visualization.engine.Logging
import java.util.List

class ReplayNamesExchangeCallback<T> implements AsyncCallback<T> {
	
	new(){}
	
	override onFailure(Throwable caught) {
		ErrorDialog::showError(caught)
	}
	
	override onSuccess(T result) {
		val newLandscape = result as List<String>
		Logging::log(newLandscape.toString)
	}
	
}