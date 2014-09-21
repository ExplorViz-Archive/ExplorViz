package explorviz.visualization.experiment

import com.google.gwt.user.client.Timer
import explorviz.visualization.engine.main.SceneDrawer
import explorviz.visualization.landscapeexchange.LandscapeExchangeCallback

/**
 * @author Santje Finke
 * 
 */
class SceneDrawTimer extends Timer {
	
	override run(){
		//redraw landscape + interaction
		LandscapeExchangeCallback::reset()
		SceneDrawer::redraw()
	}
}