package explorviz.shared.experiment

class Postquestion extends Prequestion {
	
	//for serialization
	private new() {}
	
	new(int id, String type, String text, String[] answers, int min, int max) {
		super(id, type, text, answers, min, max)
	}
	
}