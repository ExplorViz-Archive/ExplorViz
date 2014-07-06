package explorviz.visualization.landscapeexchange

import com.google.gwt.user.client.rpc.AsyncCallback
import explorviz.shared.model.Landscape
import explorviz.visualization.engine.main.SceneDrawer
import explorviz.visualization.timeshift.TimeShiftExchangeManager

class LandscapeConverter<T> implements AsyncCallback<T> {

	var public static Landscape oldLandscape

	override onFailure(Throwable caught) {
		// TODO check for 0 (connection lost)
		//      new ErrorPage().renderWithMessage(pageControl, caught.getMessage())
	}

	def static reset() {
		destroyOldLandscape()
	}

	override onSuccess(T result) {
		val newLandscape = result as Landscape
		if (oldLandscape == null || newLandscape.hash != oldLandscape.hash) {
			if (oldLandscape != null) {
				destroyOldLandscape()
			}

			SceneDrawer::viewScene(newLandscape, true)
			oldLandscape = newLandscape
		}

		if (!LandscapeExchangeManager::timeshiftStopped) {
			TimeShiftExchangeManager::updateTimeShiftGraph()
		}
	}

	def static destroyOldLandscape() {
		if (oldLandscape != null) {
			oldLandscape.destroy()
			oldLandscape = null
		}
	}
}
