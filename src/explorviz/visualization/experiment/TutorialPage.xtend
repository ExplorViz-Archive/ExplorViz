package explorviz.visualization.experiment

import explorviz.visualization.engine.main.WebGLStart
import explorviz.visualization.engine.navigation.Navigation
import explorviz.visualization.main.PageControl
import explorviz.visualization.view.IPage

import explorviz.visualization.main.JSHelpers
import explorviz.visualization.experiment.tools.ExperimentTools

/**
 * @author Santje Finke
 * 
 */
class TutorialPage implements IPage {
	override render(PageControl pageControl) {
		WebGLStart::disable()
		JSHelpers::hideAllButtonsAndDialogs()
		
		pageControl.setView("")
		Experiment::resetTutorial()
		
		Experiment::loadTutorial()
		Experiment::getTutorialText(Experiment::tutorialStep)
	    Experiment::tutorial = true
	    ExperimentTools::toolsModeActive = false
	    TutorialJS.showTutorialDialog()   
	    TutorialJS.showTutorialContinueButton()
		WebGLStart::initWebGL()
		Navigation::registerWebGLKeys()
	    
	}
	
}