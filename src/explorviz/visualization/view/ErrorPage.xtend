package explorviz.visualization.view

import explorviz.visualization.main.PageControl
import explorviz.visualization.engine.navigation.Navigation

class ErrorPage implements IPage {
	override render(PageControl pageControl) {
	    Navigation::deregisterWebGLKeys()
		pageControl.setView("Sorry there was an error")
	}
	
	def renderWithMessage(PageControl pageControl, String message) {
	    Navigation::deregisterWebGLKeys()
		pageControl.setView("Sorry there was an error: " + message)
	}
}