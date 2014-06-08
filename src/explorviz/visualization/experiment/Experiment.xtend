package explorviz.visualization.experiment

import com.google.gwt.core.client.GWT
import com.google.gwt.user.client.rpc.ServiceDefTarget
import explorviz.shared.experiment.Answer
import explorviz.shared.experiment.Question
import explorviz.shared.experiment.Step
import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.engine.math.Vector4f
import explorviz.visualization.engine.primitives.Triangle
import explorviz.visualization.experiment.callbacks.TextCallback
import explorviz.visualization.experiment.services.TutorialService
import explorviz.visualization.experiment.services.TutorialServiceAsync
import java.util.ArrayList
import java.util.List
import explorviz.visualization.engine.primitives.PrimitiveObject
import explorviz.visualization.experiment.callbacks.StepsCallback
import explorviz.shared.experiment.Type
import explorviz.visualization.engine.primitives.Quad

class Experiment {
	public static boolean tutorial = false
	public static boolean question = false
	public static var tutorialStep = 0
	public static var questionNr = 0
	public static var questions = new ArrayList<Question>()
	public static var answers = new ArrayList<Answer>()
	public static List<Step> tutorialsteps;
	
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
		if(null == tutorialsteps){
			loadTutorial()
		}
		tutorialsteps.get(tutorialStep)
	}
	
	def static getQuestionBox(){
		val question = questions.get(questionNr)
		var html = "<p>"
		//text
		if(question.type == Type.Free){
			//freifeld
		}else if(question.type == Type.MC){
			//antwortmöglichkeiten mit radiobuttons
		}else if(question.type == Type.MMC){
			//antwortmöglichkeiten mit checkboxes
		}
		//skipbutton
		html = html+"</p>"
		return html
	}
	
	def static incTutorial(String name, boolean left, boolean right, boolean doubleC){
		if(tutorial){
			val step = getStep()
			if(!step.connection && name.equals(step.source) && 
				((left && step.leftClick) || (right && step.rightClick) || (doubleC && step.doubleClick))
			){
				incStep()
			}
		}
	}
	
	def static incTutorial(String source, String target, boolean left, boolean right){
		if(tutorial){
			val step = getStep()
			if(step.connection && source.equals(step.source) && target.equals(step.dest)&& 
				((left && step.leftClick) || (right && step.rightClick))
			){
				incStep()
			}
		}
	}

	/**
	 * Draws an Arrow that points (from above) to the given coordinates
	 * @param x - x coordinate of the tip of the arrowhead
	 * @param y - y coordinate of the tip of the arrowhead
	 */
	def static List<PrimitiveObject> drawArrow(float x, float y, float z, List<PrimitiveObject> polygons){
		var arrowhead = new Triangle()
		arrowhead.begin()
		arrowhead.transparent = false
		arrowhead.setColor(new Vector4f(1f,0f,0f,1f))
		arrowhead.addPoint(new Vector3f(x,y,z))
		arrowhead.addTexturePoint(1f, 0f)
		arrowhead.addPoint(new Vector3f(x+0.5f,y+0.5f,z))
		arrowhead.addTexturePoint(0f, 1f)
		arrowhead.addPoint(new Vector3f(x-0.5f,y+0.5f,z))
		arrowhead.addTexturePoint(1f, 1f)
		arrowhead.end()
		val bl = new Vector3f(x-0.25f, y+0.5f,z)
		val br = new Vector3f(x+0.25f, y+0.5f,z)
		val tl = new Vector3f(x-0.25f, y+1.5f, z)
		val tr = new Vector3f(x+0.25f, y+1.5f, z)
		var color = new Vector4f(1f,0f,0f,1f)
		var arrowshaft = new Quad(bl,br,tr,tl, null, color)
		polygons.add(arrowhead)
		polygons.add(arrowshaft)
		var List<PrimitiveObject> arrow = new ArrayList()
		arrow.add(arrowshaft)
		arrow.add(arrowhead)
		return arrow
	}
	
		/**
	 * Draws an Arrow that points (from above) to the given coordinates
	 * @param x - x coordinate of the tip of the arrowhead
	 * @param y - y coordinate of the tip of the arrowhead
	 */
	def static List<PrimitiveObject> draw3DArrow(float x, float y, float z, List<PrimitiveObject> polygons){
		var arrowhead = new Triangle()
		arrowhead.begin()
		arrowhead.transparent = false
		arrowhead.setColor(new Vector4f(1f,0f,0f,1f))
		arrowhead.addPoint(new Vector3f(x,y,z+3f))
		arrowhead.addTexturePoint(1f, 0f)
		arrowhead.addPoint(new Vector3f(x+3f,y+3f,z+3f))
		arrowhead.addTexturePoint(0f, 1f)
		arrowhead.addPoint(new Vector3f(x-3f,y+3f,z+3f))
		arrowhead.addTexturePoint(1f, 1f)
		arrowhead.end()
		val bl = new Vector3f(x-2f, y+3f, z+3f)
		val br = new Vector3f(x+2f, y+3f, z+3f)
		val tl = new Vector3f(x-2f, y+8f, z+3f)
		val tr = new Vector3f(x+2f, y+8f, z+3f)
		var color = new Vector4f(1f,0f,0f,1f)
		var arrowshaft = new Quad(bl,br,tr,tl, null, color)
		polygons.add(arrowhead)
		polygons.add(arrowshaft)
		var List<PrimitiveObject> arrow = new ArrayList()
		arrow.add(arrowshaft)
		arrow.add(arrowhead)
		return arrow
	}
	
	def static List<PrimitiveObject> drawTutorial(String name, Vector3f pos, 
		float width, float height, Vector3f center, List<PrimitiveObject> polygons
	){
		if(tutorial){
			val step = getStep()
			if(!step.connection && name.equals(step.source)){
				var float x = pos.x + width/2f - center.x
				var float y = pos.y - height/16f - center.y
				drawArrow(x, y, pos.z+1f, polygons)
			}else{
				return new ArrayList()
			}
		}else{
			return new ArrayList()
		}
	}
	
	def static List<PrimitiveObject> draw3DTutorial(String name, Vector3f pos, 
		float width, float height, float depth, Vector3f center, List<PrimitiveObject> polygons
	){
		if(tutorial){
			val step = getStep()
			if(!step.connection && name.equals(step.source)){
				var x = pos.x + width/2f - center.x
				var y = pos.y + height/2f - center.y + height/2f
				var z = pos.z + depth/2f - center.z - depth/8
				draw3DArrow(x, y, z, polygons)
			}else{
				return new ArrayList()
			}
		}else{
			return new ArrayList()
		}
	}
	
	def static drawTutorialCom(String source, String dest, Vector3f pos, float width, 
		float height, Vector3f center, List<PrimitiveObject> polygons){
		if(tutorial){
			val step = getStep()
			if(step.connection && source.equals(step.source) && dest.equals(step.dest)){
				var x = pos.x - center.x + width/2f
				var y = pos.y - center.y + height/2f
				drawArrow(x, y, pos.z+0.5f, polygons)
			}else{
				return new ArrayList()
			}
		}else{
			return new ArrayList()
		}
	}

	def static draw3DTutorialCom(String source, String dest, Vector3f pos, float width, 
		float height, float depth, Vector3f center, List<PrimitiveObject> polygons){
		if(tutorial){
			val step = getStep()
			if(step.connection && source.equals(step.source) && dest.equals(step.dest)){
				var x = pos.x - center.x + width/2f
				var y = pos.y - center.y + 0.8f
				var z = pos.z - center.z + depth/2f
				drawArrow(x, y, z, polygons)
			}else{
				return new ArrayList()
			}
		}else{
			return new ArrayList()
		}
	}
	
	def static loadTutorial() {
		val TutorialServiceAsync tutorialService = GWT::create(typeof(TutorialService))
		val endpoint = tutorialService as ServiceDefTarget
		endpoint.serviceEntryPoint = GWT::getModuleBaseURL() + "tutorialservice"
		tutorialService.getSteps(new StepsCallback())
	}
	
}