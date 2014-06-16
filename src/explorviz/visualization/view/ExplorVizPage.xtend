package explorviz.visualization.view

import explorviz.visualization.main.PageControl
import explorviz.visualization.engine.navigation.Navigation
import explorviz.visualization.engine.main.WebGLStart
import explorviz.visualization.experiment.Experiment
import explorviz.visualization.experiment.ExperimentJS

class ExplorVizPage implements IPage {
	override render(PageControl pageControl) {
	    pageControl.setView("")

	    Experiment::tutorial = false
	    ExperimentJS.closeTutorialDialog()
	    ExperimentJS.hideArrows()
	    
	    Navigation::registerWebGLKeys()
		WebGLStart::initWebGL()
		
	}
}