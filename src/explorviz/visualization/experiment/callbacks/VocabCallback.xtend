package explorviz.visualization.experiment.callbacks

import com.google.gwt.user.client.rpc.AsyncCallback
import java.util.List
import explorviz.visualization.experiment.Questionnaire
import java.util.Arrays
import java.util.ArrayList
import explorviz.visualization.main.ErrorDialog

class VocabCallback implements AsyncCallback<String[]> {
	
	override onFailure(Throwable caught) {
		ErrorDialog::showError(caught)
	}
	
	override onSuccess(String[] result) {	
		var results = Arrays.asList(result)
		var List<String> personal = new ArrayList<String>()
		var List<String> comment = new ArrayList<String>()
		//parse vocabulary 2-34 (dh 1-33) are personal, 36-55 (dh 35-54) are comments
		var i = 1
		var String s
		while(i < 34){
			s = results.get(i)
			s = s.substring(s.indexOf(":")+1)
			personal.add(s)
			i = i + 1
		}
		i = i + 1 //unparsed line
		while(i < 55){
			s = results.get(i)
			s = s.substring(s.indexOf(":")+1)
			comment.add(s)
			i = i + 1
		}
		
		Questionnaire::commentVocab = comment
		Questionnaire::personalVocab = personal
		Questionnaire::showPersonalDataDialog(personal)
	}
	
}