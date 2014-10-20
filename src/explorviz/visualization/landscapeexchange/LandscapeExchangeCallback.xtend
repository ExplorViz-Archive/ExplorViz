package explorviz.visualization.landscapeexchange

import com.google.gwt.user.client.rpc.AsyncCallback
import explorviz.shared.model.Landscape
import explorviz.visualization.engine.main.SceneDrawer
import explorviz.visualization.timeshift.TimeShiftExchangeManager
import explorviz.visualization.main.ErrorDialog
import explorviz.visualization.experiment.Experiment
import explorviz.visualization.experiment.landscapeexchange.TutorialLandscapeExchangeTimer

class LandscapeExchangeCallback<T> implements AsyncCallback<T> {

	var public static Landscape oldLandscape
	var public static boolean firstExchange = true
	val boolean recenter
	
	
	new(boolean recenter) {
		this.recenter = recenter
	}

	override onFailure(Throwable caught) {
		ErrorDialog::showError(caught)
	}

	def static reset() {
		destroyOldLandscape()
	}

	override onSuccess(T result) {
		val newLandscape = result as Landscape
		if (oldLandscape == null || newLandscape.timestamp != oldLandscape.timestamp) {
			if (oldLandscape != null) {
				destroyOldLandscape()
			}

			if ((firstExchange && !newLandscape.systems.empty) || recenter) {
				SceneDrawer::viewScene(newLandscape, false)
				firstExchange = false
			} else {
				SceneDrawer::viewScene(newLandscape, true)
			}
			oldLandscape = newLandscape
		}

		if (!LandscapeExchangeManager::timeshiftStopped) {
			TimeShiftExchangeManager::updateTimeShiftGraph()
		}
		if(Experiment::tutorial){
			TutorialLandscapeExchangeTimer::loadedFirstLandscape = true
		}
	}

	def static destroyOldLandscape() {
		if (oldLandscape != null) {
			oldLandscape.destroy()
			oldLandscape = null
		}
	}
}
