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
		//input: inputx=antwort
		var List<String> ansList = new ArrayList<String>()
		if(!answer.equals("")){
			var String[] answerList = answer.split("&")
			var sub = 0
			if(answerList.get(0).startsWith("input")){
				sub = 7
			}else{
				sub = 6
			}
			for(var i = 0; i<answerList.length; i++){
				ansList.add(answerList.get(i).substring(sub).replace("+"," ").replace("%0D%0A"," "))
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