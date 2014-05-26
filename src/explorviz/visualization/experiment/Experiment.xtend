package explorviz.visualization.experiment

import com.google.gwt.core.client.GWT
import com.google.gwt.user.client.rpc.ServiceDefTarget
import explorviz.shared.experiment.Answer
import explorviz.shared.experiment.Question
import explorviz.shared.experiment.Step
import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.engine.math.Vector4f
import explorviz.visualization.engine.primitives.Rectangle
import explorviz.visualization.engine.primitives.Triangle
import explorviz.visualization.experiment.callbacks.TextCallback
import explorviz.visualization.experiment.services.TutorialService
import explorviz.visualization.experiment.services.TutorialServiceAsync
import java.util.ArrayList

class Experiment {
	public static boolean tutorial = false
	public static boolean question = false
	public static var tutorialStep = 0
	public static var questionNr = 0
	public static var questions = new ArrayList<Question>()
	public static var answers = new ArrayList<Answer>()
	public static ArrayList<Step> tutorialsteps;
	
	def static getTutorialText(int number) {
		val TutorialServiceAsync tutorialService = GWT::create(typeof(TutorialService))
		val endpoint = tutorialService as ServiceDefTarget
		endpoint.serviceEntryPoint = GWT::getModuleBaseURL() + "tutorialservice"
		tutorialService.getText(number, new TextCallback())
	}
	
	def static incStep(){
		if(tutorialStep == tutorialsteps.size){
			//TODO tutorial ist zuende, was jetzt? 
			tutorialStep = 0
			tutorial = false
		}else{
			tutorialStep = tutorialStep + 1
			getTutorialText(tutorialStep)
		}
	}
		
	def static getStep(){
		tutorialsteps.get(tutorialStep)
	}
	
	def static getQuestionBox(){
		val question = questions.get(questionNr)
		
		return '''<script>
					
				</scrip>
				<div>
					<p></p>
				</div>'''.toString()
		
	}
	

	/**
	 * Draws an Arrow that points (from above) to the given coordinates
	 */
	def static drawArrow(int x, int y, int z){
		val point = new Vector3f(x,y,z)
		val l = new Vector3f(x-15,y+20,z)
		val r = new Vector3f(x+15,y+20,z)
		var arrowhead = new Triangle()
		arrowhead.addPoint(point)
		arrowhead.addPoint(l)
		arrowhead.addPoint(r)
		arrowhead.draw()
		val bl = new Vector3f(x-10, y+20,z)
		val br = new Vector3f(x+10, y+20,z)
		val tl = new Vector3f(x-10, y+80, z)
		val tr = new Vector3f(x+10, y+80, z)
		var color = new Vector4f(1f,0f,0f,1f)
		var arrowshaft = new Rectangle(bl,br, tl, tr, color, false, 1)
		arrowshaft.draw()
	}
}