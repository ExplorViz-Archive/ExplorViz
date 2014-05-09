package explorviz.shared.experiment

import java.util.List

class Question {
	@Property String text
	@Property int timeframeStart
	@Property int timeframeEnd
	@Property String questionID
	@Property Type type
	@Property List<String> correctAnswers
	@Property List<String> answers
	
	new(String question){
		//TODO: string zu Frage umwandeln
		
		
		if(correctAnswers.length>1){
			type = Type.MMC
		}else if(answers.length>1){
			type = Type.MC
		}else{
			type = Type.Free
		}
		
	}
	
}

enum Type {
    Free, MC, MMC
}