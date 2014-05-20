package explorviz.visualization.view

import explorviz.visualization.main.PageControl
import explorviz.visualization.engine.navigation.Navigation
import explorviz.visualization.engine.main.WebGLStart
import explorviz.visualization.experiment.Experiment

class ExplorVizPage implements IPage {
	override render(PageControl pageControl) {
	    pageControl.setView("")
	    
	    Navigation::registerWebGLKeys()
	    Experiment::tutorial = false
		WebGLStart::initWebGL()
		
	}
}