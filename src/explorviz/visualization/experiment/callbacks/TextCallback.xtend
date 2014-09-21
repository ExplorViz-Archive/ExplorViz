package explorviz.visualization.experiment.callbacks

import com.google.gwt.user.client.rpc.AsyncCallback
import explorviz.visualization.main.ErrorDialog
import explorviz.visualization.experiment.Experiment
import explorviz.visualization.experiment.TutorialJS

/**
 * Callback to update the tutorial dialog with the fetched text.
 * @author Santje Finke
 */
class TextCallback implements AsyncCallback<String> {

	override onFailure(Throwable caught) {
		ErrorDialog::showError(caught)
	}

	override onSuccess(String result) {
		var int i = Experiment::tutorialStep + 1
		var title = "Step " + i + " of " + Math.max(Experiment::tutorialsteps.size(), 16)
		TutorialJS.changeTutorialDialog(result, title)
	}
}
