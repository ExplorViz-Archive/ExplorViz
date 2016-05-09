package explorviz.visualization.view

import explorviz.visualization.main.PageControl
import explorviz.visualization.engine.navigation.Navigation
import explorviz.visualization.engine.main.WebGLStart
import explorviz.visualization.experiment.Experiment
import explorviz.visualization.experiment.Questionnaire
import explorviz.visualization.experiment.TutorialJS
import explorviz.visualization.engine.Logging

class ExplorVizPage implements IPage {
	override render(PageControl pageControl) {
	    pageControl.setView("")

	    Experiment::tutorial = false
	    TutorialJS.closeTutorialDialog()
	    TutorialJS.hideArrows()
	    
		WebGLStart::initWebGL()
	    Navigation::registerWebGLKeys()
	    
	    Logging::log((Experiment::experiment).toString)
		if (Experiment::experiment) {
			Questionnaire::startQuestions()
		}
		
	}
}