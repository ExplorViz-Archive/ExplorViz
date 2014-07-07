package explorviz.visualization.experiment.callbacks

import com.google.gwt.user.client.rpc.AsyncCallback
import explorviz.shared.experiment.Step
import explorviz.visualization.experiment.Experiment
import java.util.List
import java.util.ArrayList
import explorviz.visualization.main.ErrorDialog

class StepsCallback implements AsyncCallback<Step[]> {
	
	override onFailure(Throwable caught) {
		ErrorDialog::showError(caught)
	}
	
	override onSuccess(Step[] result) {
		var List<Step> list = new ArrayList<Step>();
		for(Step s : result){
			list.add(s)
		}
		Experiment::tutorialsteps = list

	}
	
}