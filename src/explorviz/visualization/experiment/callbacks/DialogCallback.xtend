package explorviz.visualization.experiment.callbacks

import com.google.gwt.user.client.rpc.AsyncCallback
import java.util.List
import explorviz.visualization.experiment.Questionnaire
import java.util.ArrayList
import explorviz.visualization.main.ErrorDialog
import explorviz.shared.experiment.StatisticQuestion
//import explorviz.visualization.engine.Logging

class DialogCallback implements AsyncCallback<String[]> {
	
	override onFailure(Throwable caught) {
		ErrorDialog::showError(caught)
	}
	
	override onSuccess(String[] result) {	
		var List<List<StatisticQuestion>> dialog = new ArrayList<List<StatisticQuestion>>()
		for(var x = 0; x < 6; x++){
			dialog.add(new ArrayList<StatisticQuestion>())
		}
		
		var String line
		var int i = 1
		var int d = 0
		var int lines = 0
		//Logging.log(result.length+" Zeilen zu parsen")
		while(i<result.length){
			//Logging.log("parsing line "+i)
			line = result.get(i)
			if(line.startsWith("--Dialog")){
				d++ //next dialog
				i++
			}else{		
				var StringBuilder question = new StringBuilder()
				lines = parseQuestionType(line)
				//Logging.log(lines + "lines beloning to question")
				question.append(line)
				for(var k = 1; k<lines; k++){
					//Logging.log("trying to parse line "+(i+k))
					question.append("\n")
					question.append(result.get(i+k))
				}
				dialog.get(d).add(new StatisticQuestion(question.toString(),i))
				i = i+lines //+1?????
			}
		}
		
		Questionnaire::showFirstDialog(dialog.get(0), dialog.get(1), dialog.get(2), dialog.get(3), dialog.get(4), dialog.get(5))
	}
	
	def int parseQuestionType(String question){
		if(question.startsWith("Text") || question.startsWith("Comment")){
			return 1
		}else if(question.startsWith("Binary") || question.startsWith("E-Mail")){
			return 2
		}else if(question.startsWith("Input") || question.startsWith("Combobox")){
			return 3
		}else if(question.startsWith("Number")){
			return 4
		}else{
			return 0
		}
	}
	
}