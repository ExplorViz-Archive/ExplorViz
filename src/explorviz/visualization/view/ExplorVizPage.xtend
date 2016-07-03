package explorviz.visualization.view

import explorviz.visualization.main.PageControl
import explorviz.visualization.engine.navigation.Navigation
import explorviz.visualization.engine.main.WebGLStart
import explorviz.visualization.experiment.Experiment
import explorviz.visualization.experiment.Questionnaire
import explorviz.visualization.experiment.TutorialJS
import explorviz.visualization.experiment.tools.ExperimentTools
import explorviz.visualization.main.Util
import explorviz.visualization.experiment.callbacks.VoidCallback

class ExplorVizPage implements IPage {
	override render(PageControl pageControl) {
	    pageControl.setView("")
	    
	    if(ExperimentTools::toolsModeActive) {
	    	Util::landscapeService.resetLandscape(new VoidCallback())
	    }

	    Experiment::tutorial = false
	    ExperimentTools::toolsModeActive = false
	    TutorialJS.closeTutorialDialog()
	    TutorialJS.hideArrows()
	    
		WebGLStart::initWebGL()
	    Navigation::registerWebGLKeys()
	    
		if (Experiment::experiment) {
			Questionnaire::startQuestions()
		}
		
	}
}