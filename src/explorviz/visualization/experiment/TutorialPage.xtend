package explorviz.visualization.experiment

import explorviz.visualization.engine.main.WebGLStart
import explorviz.visualization.engine.navigation.Navigation
import explorviz.visualization.main.PageControl
import explorviz.visualization.view.IPage

import static explorviz.visualization.experiment.Experiment.*
import explorviz.visualization.main.JSHelpers

class TutorialPage implements IPage {
	override render(PageControl pageControl) {
		WebGLStart::disable()
		JSHelpers::hideAllButtonsAndDialogs()
		
		pageControl.setView("")
		Experiment::resetTutorial()
		
		Navigation::registerWebGLKeys()
		Experiment::loadTutorial()
		Experiment::getTutorialText(Experiment::tutorialStep)
	    Experiment::tutorial = true
	    ExperimentJS.showTutorialDialog()   
	    ExperimentJS.showTutorialContinueButton()
		WebGLStart::initWebGL()
	    
	}
	
}