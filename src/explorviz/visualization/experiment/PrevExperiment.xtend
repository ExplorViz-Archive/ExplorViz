package explorviz.visualization.experiment

import explorviz.visualization.engine.navigation.Navigation
import explorviz.visualization.main.JSHelpers
import explorviz.visualization.main.PageControl
import explorviz.visualization.view.IPage

class PrevExperiment implements IPage {

	override render(PageControl pageControl) {

		Navigation::deregisterWebGLKeys()
		JSHelpers::hideAllButtonsAndDialogs()
		JSHelpers::showElementById("overwriteQuestions")
		JSHelpers::showElementById("addQuestion")

		pageControl.setView(
			'''Prev Experiment: Analysis, Replayer, Visualization'''.
				toString())
	}

}
