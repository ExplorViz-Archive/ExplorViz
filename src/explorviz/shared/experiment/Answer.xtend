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
	
	new(int questionID, String answer, long timeTaken, String userID){
		//parse answer
		//radio: radio=antwort
		//checkbox: check=antwort
		//input: input=antwort
		Logging.log("String answers: "+answer)
		var List<String> ansList = new ArrayList<String>()
		if(!answer.equals("")){
			var String[] answerList = answer.split("&")
			if(answerList.length == 1){
				ansList.add(answerList.get(0).substring(6))
			}else if(answerList.length > 1){
				var i = 0
				while(i < answerList.length){
					ansList.add(answerList.get(i).substring(6))
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
				
		this.userID = userID
		this.questionID = questionID
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
	
	private new(){
		//default constructor
	}
	
	
	def getUserID(){
		userID
	}
	
}