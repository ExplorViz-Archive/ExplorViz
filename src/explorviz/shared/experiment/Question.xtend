package explorviz.shared.experiment

import java.io.Serializable

class Question implements Serializable{
	@Property String text
	@Property int timeframeEnd
	@Property int questionID
	@Property String type
	@Property String[] correctAnswers
	@Property String[] answers
	
	new(int id, String text, String answs, String corrects, String time){
		//Used "string.length" for easier understandability and to make it obvious what
		//has to be changed if the format is changed
		this.text = text.substring("Question:".length).trim()
		this.questionID = id
		var ans = answs.substring("Answers:".length).trim()
		this.answers = ans.split(", ")
		var corAns = corrects.substring("Correct Answers:".length).trim()
		this.correctAnswers = corAns.split(", ")
//		Logging.log("Question: "+text+" answers: "+ans+" corrects answers: "+corAns)
//		Logging.log("Answers: "+this.answers.length)
//		var i = 0
//		while(i < this.answers.length){
//			Logging.log("One answer is "+this.answers.get(i))
//			i = i + 1
//		}
		if(correctAnswers.length>1){
			type = "MMC"
		}else if(answers.length>1){
			type = "MC"
		}else{
			type = "Free"
		}
//		Logging.log("Type is "+type.toString)
		
	}
	
	/**
	 * Default for serialization
	 */
	private new(){
		
	}
	
}