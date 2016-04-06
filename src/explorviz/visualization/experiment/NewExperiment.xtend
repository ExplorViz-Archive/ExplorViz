package explorviz.visualization.experiment

import explorviz.visualization.engine.navigation.Navigation
import explorviz.visualization.main.PageControl
import explorviz.visualization.view.IPage
import explorviz.visualization.engine.main.WebGLStart

class NewExperiment implements IPage {
	override render(PageControl pageControl) {
		pageControl.setView("")

		Experiment::tutorial = false
		TutorialJS.closeTutorialDialog()
		TutorialJS.hideArrows()

		WebGLStart::initWebGL()
		Navigation::registerWebGLKeys()

		if (Experiment::experiment) {
			Questionnaire::startQuestions()
		}
	}
}
