package explorviz.visualization.landscapeexchange

import com.google.gwt.user.client.rpc.AsyncCallback
import explorviz.visualization.main.ErrorDialog
import java.util.List
import explorviz.visualization.experiment.tools.NewExperiment

class ReplayNamesExchangeCallback<T> implements AsyncCallback<T> {
	
	new(){}
	
	override onFailure(Throwable caught) {
		ErrorDialog::showError(caught)
	}
	
	override onSuccess(T result) {
		NewExperiment::finishInit(result as List<String>)
	}
	
}