package explorviz.visualization.experiment.data

import java.util.ArrayList
import explorviz.visualization.experiment.data.Step

class TutorialData {
	static var tutorial = new ArrayList<Step>()
	
	def static loadTutorial(){
		//fill tutorial with steps
	}
	
	def static getStep(int i){
		tutorial.get(i)
	}
	
	def static isLastStep(int i){
		if(i == tutorial.size-1) true else false
	}
	
}


