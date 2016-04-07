package explorviz.visualization.experiment

import explorviz.visualization.engine.navigation.Navigation
import explorviz.visualization.main.PageControl
import explorviz.visualization.view.IPage
import explorviz.visualization.engine.main.WebGLStart
import explorviz.visualization.experiment.tools.ExperimentTools

class NewExperiment implements IPage {
	override render(PageControl pageControl) {
		pageControl.setView("")
		
		ExperimentTools::toolsModeActive = true
		TutorialJS.closeTutorialDialog()
		TutorialJS.hideArrows()

		WebGLStart::initWebGL()
		Navigation::registerWebGLKeys()
		
	}
}
