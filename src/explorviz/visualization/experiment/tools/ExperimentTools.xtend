package explorviz.visualization.experiment.tools

import explorviz.visualization.main.JSHelpers
import com.google.gwt.user.client.ui.RootPanel
import com.google.gwt.user.client.Event
import com.google.gwt.event.dom.client.ClickEvent
import explorviz.visualization.main.ExplorViz

class ExperimentTools {

	public static boolean toolsModeActive = false

	def static void showAndPrepareNewExpButton() {

		JSHelpers::showElementById("newExperimentBtn")

		val button = RootPanel::get("newExperimentBtn")

		button.sinkEvents(Event::ONCLICK)
		button.addHandler(
			[
			ExplorViz.getPageCaller().showNewExp()
		], ClickEvent::getType())
	}

	def static void showAndPreparePrevExpButton() {

		JSHelpers::showElementById("prevExperimentBtn")

		val button = RootPanel::get("prevExperimentBtn")

		button.sinkEvents(Event::ONCLICK)
		button.addHandler(
			[
			ExplorViz.getPageCaller().showNewExp()
		], ClickEvent::getType())
	}

	def static void showRunningExperimentData() {

		JSHelpers::showElementById("prevExperimentBtn")

		val button = RootPanel::get("prevExperimentBtn")

		button.sinkEvents(Event::ONCLICK)
		button.addHandler(
			[
			ExplorViz.getPageCaller().showNewExp()
		], ClickEvent::getType())
	}

}
