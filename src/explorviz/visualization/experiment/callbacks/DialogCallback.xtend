package explorviz.visualization.experiment.callbacks

import com.google.gwt.user.client.rpc.AsyncCallback
import java.util.List
import explorviz.visualization.experiment.Questionnaire
import java.util.ArrayList
import explorviz.visualization.main.ErrorDialog
import explorviz.shared.experiment.StatisticQuestion

/**
 * A Callback to provide the questions or texts to be displayed before and after the main tasks
 * of the questionnaire.
 * @author Santje Finke
 */
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
		while(i<result.length){
			line = result.get(i)
			if(line.startsWith("--Dialog")){
				d++ //next dialog
				i++
			}else{		
				var StringBuilder question = new StringBuilder()
				lines = parseQuestionType(line)
				question.append(line)
				for(var k = 1; k<lines; k++){
					question.append("\n")
					question.append(result.get(i+k))
				}
				dialog.get(d).add(new StatisticQuestion(question.toString(),i))
				i = i+lines
			}
		}
		
		//Questionnaire::showFirstDialog(dialog)
	}
	
	/**
	 * Returns the number of lines to be parse depending on the questiontype.
	 * @param question - The first line of the question that is to be parsed.
	 */
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
