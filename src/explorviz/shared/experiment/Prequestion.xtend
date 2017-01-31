package explorviz.shared.experiment

class Prequestion extends Question {
	
	//for serialization
	new() {}
	
	new(int id, String type, String text, String[] answers, String[] correctAnswers) {
		this.questionID = id
		this.text = text
		this.correctAnswers = correctAnswers
		this.answers = answers
		this.freeAnswers = answers.length

		if (type.equals("multipleChoice")) {

			if (correctAnswers.length > 1 && answers.length > 1) {

				this.type = "MMC"

			} else if (answers.length > 1) {

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
	
}