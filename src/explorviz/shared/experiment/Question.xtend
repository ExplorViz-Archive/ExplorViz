package explorviz.shared.experiment

import com.google.gwt.user.client.rpc.IsSerializable
import org.eclipse.xtend.lib.annotations.Accessors

/**
 * @author Santje Finke
 * 
 */
class Question implements IsSerializable {
	@Accessors String text
	@Accessors long timeframeEnd
	@Accessors int questionID
	@Accessors String type
	@Accessors String[] correctAnswers
	@Accessors String[] answers
	@Accessors int worktime
	@Accessors int freeAnswers = 0
	@Accessors long timestamp
	@Accessors long activity
	@Accessors String maybeApplication

	new(int id, String text, String answs, String corrects, String frees, String worktime, String timestamp) {

		// Used "string.length" for easier understandability and to make it obvious what
		// has to be changed if the format is changed
		this.text = text.substring("Question:".length).trim()
		this.questionID = id
		var ans = answs.substring("Answers:".length).trim()
		this.answers = ans.split(", ")
		var corAns = corrects.substring("Correct Answers:".length).trim()
		this.correctAnswers = corAns.split(", ")
		if (correctAnswers.length > 1 && answers.length > 1) {
			type = "MMC"
		} else if (answers.length > 1) {
			type = "MC"
		} else {
			type = "Free"
			freeAnswers = Integer.parseInt(frees.substring("Free Answers:".length).trim())
			if (freeAnswers == 0) {

				// There must be at least one input field
				freeAnswers = 1
			}
		}
		var t = timestamp.substring("Timestamp:".length).trim()
		this.timeframeEnd = if (t.equals("")) {
			0
		} else {
			Long.parseLong(t)
		}
		var w = worktime.substring("Processing time:".length).trim()
		this.worktime = if (w.equals("")) {
			8
		} else {
			Integer.parseInt(w)
		}

	}

	/**
	 * Default for serialization
	 */
	private new() {
	}

	new(int id, String text, String[] answers, String[] correctAnswers, int freeAnswers, int workTime, long timeEnd) {
		this.questionID = id
		this.text = text
		this.correctAnswers = correctAnswers
		this.answers = answers
		this.freeAnswers = freeAnswers
		this.worktime = workTime
		this.timeframeEnd = timeEnd

		if (correctAnswers.length > 1 && answers.length > 1) {
			type = "MMC"
		} else if (answers.length > 1) {
			type = "MC"
		} else {
			type = "Free"
		}
	}

	new(int id, String type, String text, String[] answers, String[] correctAnswers, int workTime, long timestamp, long activity, String application) {
		this.questionID = id
		this.text = text
		this.correctAnswers = correctAnswers
		this.answers = answers
		this.freeAnswers = answers.length
		this.worktime = workTime
		this.timestamp = timestamp
		this.activity = activity
		this.maybeApplication = application

		if (type.equals("multipleChoice")) {

			if (correctAnswers.length > 1 && answers.length > 1) {

				this.type = "MMC"

			} else if (answers.length > 1) {

				this.type = "MC"

			}

		} else if (type.equals("freeText")) {
			this.type = "Free"
		}
	}

	/**
	 * Converts the question into the format in which questions are saved on the server.
	 */
	def String toFormat() {
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
		sb.append("Processing time:")
		sb.append(worktime.toString())
		sb.append("\n")
		sb.append("Timestamp: ")
		sb.append(timeframeEnd.toString)
		sb.append("\n")
		sb.toString()
	}

}
