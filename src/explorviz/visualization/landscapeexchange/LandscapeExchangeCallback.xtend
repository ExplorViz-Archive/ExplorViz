package explorviz.visualization.landscapeexchange

import com.google.gwt.user.client.rpc.AsyncCallback
import explorviz.shared.model.Landscape
import explorviz.visualization.engine.main.SceneDrawer
import explorviz.visualization.timeshift.TimeShiftExchangeManager
import explorviz.visualization.main.ErrorDialog

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
		if (oldLandscape == null || newLandscape.hash != oldLandscape.hash) {
			if (oldLandscape != null) {
				destroyOldLandscape()
			}

			if ((firstExchange && !newLandscape.systems.empty) || recenter) {
				SceneDrawer::viewScene(newLandscape, false)
				LandscapeExchangeTimer::alreadyExchanged = newLandscape.hash != 0
				firstExchange = true
			} else {
				LandscapeExchangeTimer::alreadyExchanged = newLandscape.hash != 0
				SceneDrawer::viewScene(newLandscape, true)
			}
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
