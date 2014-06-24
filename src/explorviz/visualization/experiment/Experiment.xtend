package explorviz.visualization.experiment

import com.google.gwt.core.client.GWT
import com.google.gwt.user.client.rpc.ServiceDefTarget
import explorviz.shared.experiment.Step
import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.engine.math.Vector4f
import explorviz.visualization.engine.primitives.PrimitiveObject
import explorviz.visualization.engine.primitives.Quad
import explorviz.visualization.engine.primitives.Triangle
import explorviz.visualization.experiment.callbacks.StepsCallback
import explorviz.visualization.experiment.callbacks.TextCallback
import explorviz.visualization.experiment.services.TutorialService
import explorviz.visualization.experiment.services.TutorialServiceAsync
import java.util.ArrayList
import java.util.List
import explorviz.visualization.engine.main.SceneDrawer
import explorviz.visualization.landscapeexchange.LandscapeConverter
import explorviz.visualization.engine.Logging
import com.google.gwt.user.client.rpc.AsyncCallback

class Experiment {
	public static boolean tutorial = false
	public static boolean experiment = false
	public static var tutorialStep = 0

	public static List<Step> tutorialsteps = new ArrayList<Step>()
	public static boolean loadOtherLandscape = false
	
	
	def static loadTutorial() {
		val TutorialServiceAsync tutorialService = GWT::create(typeof(TutorialService))
		val endpoint = tutorialService as ServiceDefTarget
		endpoint.serviceEntryPoint = GWT::getModuleBaseURL() + "tutorialservice"
		tutorialService.getSteps(new StepsCallback())
		tutorialService.isExperiment(new IsExperimentCallback())
	}
	
	def static resetTutorial(){
		tutorialStep = 0
		loadOtherLandscape = false
	}
	
	def static getTutorialText(int number) {
		val TutorialServiceAsync tutorialService = GWT::create(typeof(TutorialService))
		val endpoint = tutorialService as ServiceDefTarget
		endpoint.serviceEntryPoint = GWT::getModuleBaseURL() + "tutorialservice"
		tutorialService.getText(number, new TextCallback())
	}
	
	def static void incStep(){
		//Tutorial completed
		if(tutorialStep+1 == tutorialsteps.size){
			ExperimentJS::closeTutorialDialog()
			ExperimentJS::hideArrows()
			tutorialStep = 0
			tutorial = false
			//ExperimentJS::clickExplorVizRibbon()
			//change to explorviz normal
			if(experiment){
				Logging.log("Start questionnaire")
				Questionnaire::startQuestions()
			}
		}else{
			tutorialStep = tutorialStep + 1
			getTutorialText(tutorialStep)
			if(step.requiresButton){
				ExperimentJS::showTutorialContinueButton()
			}else{
				ExperimentJS::removeTutorialContinueButton()
			}
			if(step.backToLandscape){			
				ExperimentJS::showBackToLandscapeArrow()
			}else if(step.timeshift){
				ExperimentJS::showTimshiftArrow()
			}else{
				ExperimentJS::hideArrows()
			}
			//redraw landscape + interaction
			LandscapeConverter::reset()
			SceneDrawer::redraw()
			//if next step is a timeshift step
			if((tutorialStep+1 < tutorialsteps.size) 
				&& (tutorialsteps.get(tutorialStep+1).timeshift)){
				loadOtherLandscape = true
			}
		}
	}
			
	def static getStep(){
		if(null == tutorialsteps){
			loadTutorial()
		}
		tutorialsteps.get(tutorialStep)
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
		val tl = new Vector3f(x-2f, y+11f, z+3f)
		val tr = new Vector3f(x+2f, y+11f, z+3f)
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

	def static draw3DTutorialCom(String source, String dest, Vector3f pos, Vector3f pos2, Vector3f center, List<PrimitiveObject> polygons){
		if(tutorial){
			val step = getStep()
			if(step.connection && source.equals(step.source) && dest.equals(step.dest)){
				var x = pos.x - center.x - (pos.x-pos2.x)/8f
				var y = pos.y - center.y - (pos.y-pos2.y)
				var z = pos.z - center.z - (pos.z-pos2.z)/4f
				draw3DArrow(x, y, z, polygons)
			}else{
				return new ArrayList()
			}
		}else{
			return new ArrayList()
		}
	}
		
}

class IsExperimentCallback implements AsyncCallback<Boolean>{
	
	override onFailure(Throwable caught) {
		Logging.log("Something went wrong when fetching the experiment flag: "+ caught.message)
	}
	
	override onSuccess(Boolean result) {
		Experiment::experiment = result
	}
	
}