package explorviz.shared.experiment

import explorviz.visualization.engine.Logging
import java.util.List
import java.util.ArrayList
import com.google.gwt.user.client.rpc.IsSerializable

class Answer implements IsSerializable {
	
	String userID
	int questionID
	String[] answers = #[""] //test - need an array
	long timeTaken
	
	new(int questionID, String answer, long timeTaken){
		//parse answer
		//radio/checkbox: name=on &
		//input: input=antwort
		var List<String> ansList = new ArrayList<String>()
		if(!answer.equals("")){
			var String[] answerList = answer.split("&")
			if(answerList.length == 1){
				if(answerList.get(0).startsWith("input=")){
					//free question
					ansList.add(answerList.get(0).substring(6))
				}else{
					//mc question
					ansList.add(answerList.get(0).substring(0,answerList.get(0).length-3))
				}
			}else if(answerList.length > 1){
				//mmc question
				var i = 0
				while(i < answerList.length){
					ansList.add(answerList.get(0).substring(0,answerList.get(0).length-3))
					i = i + 1
				}
			}
		}else{
			//Skipped Question
			ansList.add("")
		}
		this.answers = ansList.toArray(answers)
		//
		var j = 0
		Logging.log("Antworten")
		while(j < answers.length){
			Logging.log(ansList.get(j))
			j = j + 1
		}
		//
				
		//this.userID = userID
		this.questionID = questionID
		//this.answer = answer
		this.timeTaken = timeTaken
	}
	
	def toCSV(){
		var s = userID+","+questionID.toString()+","+timeTaken.toString()
		var i = 0
		while(i<answers.length){
			s = s + ","+answers.get(i)
			i = i + 1
		}
		s = s + "\n"
		return s
	}
	
	
	def getUserID(){
		userID
	}
	
}