package explorviz.shared.experiment

import com.google.gwt.user.client.rpc.IsSerializable

class Question implements IsSerializable {
	@Property String text
	@Property long timeframeEnd
	@Property int questionID
	@Property String type
	@Property String[] correctAnswers
	@Property String[] answers
	@Property int freeAnswers = 0
	
	new(int id, String text, String answs, String corrects, String frees, String time){
		//Used "string.length" for easier understandability and to make it obvious what
		//has to be changed if the format is changed
		this.text = text.substring("Question:".length).trim()
		this.questionID = id
		var ans = answs.substring("Answers:".length).trim()
		this.answers = ans.split(", ")
		var corAns = corrects.substring("Correct Answers:".length).trim()
		this.correctAnswers = corAns.split(", ")
		if(correctAnswers.length>1 && answers.length>1){
			type = "MMC"
		}else if(answers.length>1){
			type = "MC"
		}else{
			type = "Free"
			freeAnswers = Integer.parseInt(frees.substring("Free Answers:".length).trim())
			if(freeAnswers ==0){
				//There must be at least one input field
				freeAnswers = 1
			}
		}
		var t = time.substring("Time:".length).trim()
		this.timeframeEnd = if(t.equals("")){0}else{Long.parseLong(t)}
		
	}
	
	/**
	 * Default for serialization
	 */
	private new(){
		
	}
	
}