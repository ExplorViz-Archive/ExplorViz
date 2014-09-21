package explorviz.visualization.experiment.callbacks

import com.google.gwt.user.client.rpc.AsyncCallback
import explorviz.shared.model.Landscape
import explorviz.visualization.engine.main.SceneDrawer
import explorviz.visualization.main.ErrorDialog

/**
 * A Callback to display an empty landscape. Can be used to reduce lag when the 
 * previously loaded landscape isn't needed anymore.
 * @author Santje Finke
 */
class EmptyLandscapeCallback implements AsyncCallback<Landscape> {
	
	override onFailure(Throwable caught) {
		ErrorDialog::showError(caught)
	}
	
	override onSuccess(Landscape result) {
		SceneDrawer::viewScene(result, true)
	}
	
}