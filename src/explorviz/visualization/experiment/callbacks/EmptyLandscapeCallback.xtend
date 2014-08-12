package explorviz.visualization.experiment.callbacks

import com.google.gwt.user.client.rpc.AsyncCallback
import explorviz.shared.model.Landscape
import explorviz.visualization.engine.main.SceneDrawer
import explorviz.visualization.main.ErrorDialog

class EmptyLandscapeCallback implements AsyncCallback<Landscape> {
	
	override onFailure(Throwable caught) {
		ErrorDialog::showError(caught)
	}
	
	override onSuccess(Landscape result) {
		SceneDrawer::viewScene(result, true)
	}
	
}