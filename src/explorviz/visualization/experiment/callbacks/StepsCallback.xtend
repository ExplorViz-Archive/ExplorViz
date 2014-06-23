package explorviz.visualization.experiment.callbacks

import com.google.gwt.user.client.rpc.AsyncCallback
import explorviz.shared.experiment.Step
import explorviz.visualization.experiment.Experiment
import explorviz.visualization.engine.Logging
import java.util.List
import java.util.ArrayList

class StepsCallback implements AsyncCallback<Step[]> {
	
	override onFailure(Throwable caught) {
		Logging.log("Failure on stepsCallback: "+ caught.message)
	}
	
	override onSuccess(Step[] result) {
		var List<Step> list = new ArrayList<Step>();
		for(Step s : result){
			list.add(s)
		}
		Experiment::tutorialsteps = list

	}
	
}