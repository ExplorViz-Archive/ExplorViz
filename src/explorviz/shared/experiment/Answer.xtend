package explorviz.shared.experiment

class Answer {
	
	String userID
	String questionID
	String answer
	String correctAnswer
	String timestamp
	
	new(String userID, String questionID, String answer, String correctAnswer, String timestamp){
		this.userID = userID
		this.questionID = questionID
		this.answer = answer
		this.correctAnswer = correctAnswer
		this.timestamp = timestamp
	}
	
	def toCSV(){
		val s = userID+","+questionID+","+answer+","+correctAnswer+","+timestamp+"\n"
		return s
	}
	
}