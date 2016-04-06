package explorviz.visualization.experiment

import explorviz.visualization.engine.navigation.Navigation
import explorviz.visualization.main.PageControl
import explorviz.visualization.view.IPage
import explorviz.visualization.experiment.tools.ExperimentTools
import explorviz.visualization.engine.main.WebGLStart

class PrevExperiment implements IPage {
	override render(PageControl pageControl) {
		pageControl.setView("")

		Experiment::tutorial = false
		ExperimentTools::toolsMode = true
		TutorialJS.closeTutorialDialog()
		TutorialJS.hideArrows()

		WebGLStart::initWebGL()
		Navigation::registerWebGLKeys()
	}

}
