package explorviz.shared.experiment

import java.util.List
import java.util.ArrayList
import com.google.gwt.user.client.rpc.IsSerializable

class Answer implements IsSerializable {
	
	String userID
	int questionID
	String[] answers = #[""] 
	long timeTaken
	long startTime
	long endTime
	
	new(int questionID, String answer, long timeTaken, long start, long end, String userID){
		//parse answer
		//radio: radio=antwort
		//checkbox: check=antwort
		//input: input=antwort
		var List<String> ansList = new ArrayList<String>()
		if(!answer.equals("")){
			var String[] answerList = answer.split("&")
			if(answerList.length == 1){
				ansList.add(answerList.get(0).substring(6).replace("+"," "))
			}else if(answerList.length > 1){
				var i = 0
				while(i < answerList.length){
					ansList.add(answerList.get(i).substring(6).replace("+"," "))
					i = i + 1
				}
			}
		}else{
			//Skipped Question
			ansList.add("")
		}
		this.answers = ansList.toArray(answers)
				
		this.userID = userID
		this.questionID = questionID
		this.timeTaken = timeTaken
		this.startTime = start
		this.endTime = end
	}
	
	def toCSV(){
		var s = questionID.toString()+","+timeTaken.toString()+","+startTime.toString()+","+endTime.toString()
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