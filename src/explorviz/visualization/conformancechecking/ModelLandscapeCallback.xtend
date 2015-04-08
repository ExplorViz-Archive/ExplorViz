package explorviz.visualization.conformancechecking

import com.google.gwt.user.client.rpc.AsyncCallback
import explorviz.shared.model.Landscape
import explorviz.visualization.main.ErrorDialog

import static explorviz.visualization.conformancechecking.ConformanceChecker.*

class ModelLandscapeCallback<T> implements AsyncCallback<T> {

	override onFailure(Throwable caught) {
		ErrorDialog::showError(caught)
	}

	override onSuccess(T result) {
		ConformanceChecker::targetModel = result as Landscape
	}
}
