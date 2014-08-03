package explorviz.visualization.view

import explorviz.visualization.main.PageControl
import explorviz.visualization.engine.navigation.Navigation
import explorviz.visualization.engine.main.WebGLStart
import explorviz.visualization.experiment.Experiment
import explorviz.visualization.experiment.ExperimentJS
import explorviz.visualization.experiment.Questionnaire

class ExplorVizPage implements IPage {
	override render(PageControl pageControl) {
	    pageControl.setView("")

	    Experiment::tutorial = false
	    ExperimentJS.closeTutorialDialog()
	    ExperimentJS.hideArrows()
	    
		WebGLStart::initWebGL()
	    Navigation::registerWebGLKeys()
	    
		if (Experiment::experiment) {
			Questionnaire::startQuestions()
		}
		
	}
}