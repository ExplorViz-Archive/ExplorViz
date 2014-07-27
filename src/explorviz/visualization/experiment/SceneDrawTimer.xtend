package explorviz.visualization.experiment

import com.google.gwt.user.client.Timer
import explorviz.visualization.engine.main.SceneDrawer
import explorviz.visualization.landscapeexchange.LandscapeExchangeCallback
import explorviz.visualization.engine.Logging

class SceneDrawTimer extends Timer {
	
	override run(){
		//redraw landscape + interaction
		Logging.log("Redraw Scene")
		LandscapeExchangeCallback::reset()
		SceneDrawer::redraw()
	}
}