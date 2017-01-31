package explorviz.shared.experiment

class Postquestion extends Prequestion {
	
	//for serialization
	private new() {}
	
	new(int id, String type, String text, String[] answers, String[] correctAnswers) {
		super(id, type, text, answers, correctAnswers)
	}
	
}