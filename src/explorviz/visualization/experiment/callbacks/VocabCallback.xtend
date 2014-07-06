package explorviz.visualization.experiment.callbacks

import com.google.gwt.user.client.rpc.AsyncCallback
import explorviz.visualization.engine.Logging
import java.util.List
import explorviz.visualization.experiment.Questionnaire
import explorviz.visualization.experiment.ExperimentJS
import java.util.Arrays
import java.util.ArrayList
import explorviz.visualization.main.ErrorDialog

class VocabCallback implements AsyncCallback<String[]> {
	
	override onFailure(Throwable caught) {
		ErrorDialog::showError(caught)
	}
	
	override onSuccess(String[] result) {	
		var results = Arrays.asList(result)
		Logging.log(String.valueOf(results.size()))
		var List<String> personal = new ArrayList<String>()
		var List<String> comment = new ArrayList<String>()
		//parse vocabulary 1-21 are personal, 23-28 are comments
		var i = 1
		var String s
		while(i < 22){
			s = results.get(i)
			s = s.substring(s.indexOf(":")+1)
			personal.add(s)
			i = i + 1
		}
		i = i + 1 //unparsed line
		while(i < 29){
			s = results.get(i)
			s = s.substring(s.indexOf(":")+1)
			comment.add(s)
			i = i + 1
		}
		
		Questionnaire::commentVocab = comment
		//load personal info immediately
		ExperimentJS.personalDataDialog(Questionnaire::getPersonalInformationBox(personal))
	}
	
}