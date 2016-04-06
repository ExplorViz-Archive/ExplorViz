package explorviz.visualization.experiment.tools

import explorviz.visualization.main.JSHelpers
import com.google.gwt.user.client.ui.RootPanel
import com.google.gwt.user.client.Event
import com.google.gwt.event.dom.client.ClickEvent
import com.google.gwt.event.shared.HandlerRegistration
import explorviz.visualization.main.ExplorViz

class ExperimentTools {

	public static boolean toolsMode = false

	static HandlerRegistration eventViewHandler

	def static void showAndPrepareChangeLandscapeButton() {

		if (eventViewHandler != null) {
			eventViewHandler.removeHandler
		}

		JSHelpers::showElementById("newExperimentBtn")

		val button = RootPanel::get("newExperimentBtn")

		button.sinkEvents(Event::ONCLICK)
		eventViewHandler = button.addHandler(
			[	
			ExplorViz.getPageCaller().showNewExp()
			//PageCaller::showNewExp()
		], ClickEvent::getType())
	}

}
