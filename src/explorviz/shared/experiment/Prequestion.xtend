package explorviz.shared.experiment

import org.eclipse.xtend.lib.annotations.Accessors

class Prequestion extends Question {
	//for questionType numberRange
	@Accessors int min
	@Accessors int max
	
	//for serialization
	new() {}
	
	//constructor 
	new(int id, String type, String text, String[] answers, int min, int max) {
		this.questionID = id
		this.text = text
		this.answers = answers
		this.min = min
		this.max = max
		

		if (type.equals("multipleChoice")) {

			if (answers.length > 1) {

				this.type = "MC"

			}

		} else if (type.equals("freeText")) {
			this.type = "Free"
		} else if (type.equals("numberRange")) {
			this.type = "NumberRange"
		}
	}

	/**
	 * Converts the question into the format in which questions are saved on the server.
	 */
	override String toFormat() {
		var StringBuilder sb = new StringBuilder()
		sb.append("Question: ")
		sb.append(text)
		sb.append("\n")
		sb.append("Answers: ")
		if (answers != null) {
			sb.append(answers.get(0))
			for (var i = 1; i < answers.length; i++) {
				sb.append(",")
				sb.append(answers.get(i))
			}
		}
		sb.append("\n")
		sb.append("Correct Answers: ")
		if (correctAnswers != null) {
			sb.append(correctAnswers.get(0))
			for (var i = 1; i < correctAnswers.length; i++) {
				sb.append(",")
				sb.append(correctAnswers.get(i))
			}
		}
		sb.append("\n")
		sb.append("Free Answers: ")
		sb.append(freeAnswers.toString())
		sb.append("\n")
		sb.toString()
	}
	
	/**
	 * Decides for a specific input in HTML-Dialog, dependent on what kind of type the instance has
	 */
	def String getTypeDependentHTMLInput() {
		var String htmlInput = "";
		if(this.type == "Free") {
			htmlInput = freeTextTypeHTMLInput()
		} else if (this.type == "MMC" || this.type == "MC") {
			htmlInput = multipleChoiceTypeHTMLInput()
		} else if (this.type == "NumberRange") {
			htmlInput = numberRangeTypeHTMLInput()
		}
		return htmlInput
	}
	
	/**
	 * Type-dependent different input in HTML-Dialog, a textarea in case of free text
	 */
	def String freeTextTypeHTMLInput() {
		var StringBuilder htmlInput = new StringBuilder()
		
		htmlInput.append("<textarea class='form-control closureTextarea' id='"+ this.questionID +"' name='"+ this.questionID +"'></textarea>")
		
		return htmlInput.toString()
	}
	
	/**
	 * Type-dependent different input in HTML-Dialog, a dropdown selecter in case of multple choice
	 */
	def String multipleChoiceTypeHTMLInput() {
		var StringBuilder htmlInput = new StringBuilder()
		
		htmlInput.append("<select class='form-control' id='"+ this.questionID +"' name='"+ this.questionID +"' required>")
		for(var i = 0; i<answers.length; i++){
			htmlInput.append("<option>"+this.answers.get(i).trim()+"</option>")
		}
		htmlInput.append("</select>")
		
		return htmlInput.toString()
	}
	
	/**
	 * Type-dependent different input in HTML-Dialog, a number input in case of a number range
	 */
	def String numberRangeTypeHTMLInput() {
		var StringBuilder htmlInput = new StringBuilder()
		htmlInput.append("<input type='number' class='form-control' id='"+ this.questionID +"' name='"+ this.questionID +"' ")
		if(this.min!=0){
			htmlInput.append("min='"+this.min+"' ")
		}
		if(this.max!=0){
			htmlInput.append("max='"+this.max+"' ")
		}
		htmlInput.append("required>")
		
		return htmlInput.toString()
	}
}