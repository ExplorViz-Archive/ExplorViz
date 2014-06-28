package explorviz.visualization.experiment.callbacks

import com.google.gwt.user.client.rpc.AsyncCallback
import explorviz.visualization.engine.Logging
import java.util.List
import explorviz.visualization.experiment.Questionnaire
import explorviz.visualization.experiment.ExperimentJS

class VocabCallback implements AsyncCallback<String[]> {
	
	override onFailure(Throwable caught) {
		Logging.log("Failure on vocabCallback: "+ caught.message)
	}
	
	override onSuccess(String[] result) {	
		var List<String> personal
		var List<String> comment
		//parse vocabulary
		
		
		Questionnaire::commentVocab = comment
		//load personal info immediately
		ExperimentJS.personalDataDialog(Questionnaire::getPersonalInformationBox(personal))
	}
	
}