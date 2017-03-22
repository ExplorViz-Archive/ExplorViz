package explorviz.visualization.experiment

import com.google.gwt.core.client.GWT
import com.google.gwt.user.client.rpc.AsyncCallback
import com.google.gwt.user.client.rpc.ServiceDefTarget
import explorviz.shared.experiment.Step
import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.engine.math.Vector4f
import explorviz.visualization.engine.primitives.PrimitiveObject
import explorviz.visualization.engine.primitives.Quad
import explorviz.visualization.engine.primitives.Triangle
import explorviz.visualization.experiment.callbacks.StepsCallback
import explorviz.visualization.experiment.callbacks.TextCallback
import explorviz.visualization.experiment.callbacks.VoidCallback
import explorviz.visualization.experiment.services.TutorialService
import explorviz.visualization.experiment.services.TutorialServiceAsync
import explorviz.visualization.main.ErrorDialog
import explorviz.visualization.main.ExplorViz
import java.util.ArrayList
import java.util.List
import explorviz.visualization.engine.main.SceneDrawer
import explorviz.visualization.experiment.landscapeexchange.TutorialLandscapeExchangeTimer
import explorviz.visualization.experiment.callbacks.GenericFuncCallback

/**
 * @author Santje Finke
 * 
 */
class Experiment {
	public static boolean tutorial = false
	public static boolean experiment = false
	public static var tutorialStep = 0
	public static var lastSafeStep = 0

	public static List<Step> tutorialsteps = new ArrayList<Step>()
	public static boolean loadOtherLandscape = false

	private static Vector4f RED = new Vector4f(1.0f, 0.0f, 0.0f, 1.0f)
	
	private static List<PrimitiveObject> emptyList = new ArrayList()

	private static SceneDrawTimer redrawTimer = new SceneDrawTimer()

	/**
	 * Initialises the tutorial: contacts server to get all necessary configurations.
	 */
	def static void loadTutorial() {
		val TutorialServiceAsync tutorialService = GWT::create(typeof(TutorialService))
		val endpoint = tutorialService as ServiceDefTarget
		endpoint.serviceEntryPoint = GWT::getModuleBaseURL() + "tutorialservice"
		if (ExplorViz::controlGroupActive) {
			tutorialService.getStepsControllGroup(new StepsCallback())
		} else {
			tutorialService.getSteps(new StepsCallback())
		}
		tutorialService.isExperiment(new IsExperimentCallback())
		tutorialService.setTime(System.currentTimeMillis, new VoidCallback())
		tutorialService.getExperimentFilename(new GenericFuncCallback<String>([String filename | Questionnaire.experimentFilename = filename]))
	}

	/**
	 * Reset all tutorial attributes.
	 */
	def static resetTutorial() {
		tutorialStep = 0
		TutorialLandscapeExchangeTimer::loadedFirstLandscape = false
		loadOtherLandscape = false
		setTutorialLandscape(false)
	}

	/**
	 * Fetches the text for the given step from the server.
	 * @param number - number of the step whose text is fetched
	 */
	def static getTutorialText(int number) {
		val TutorialServiceAsync tutorialService = GWT::create(typeof(TutorialService))
		val endpoint = tutorialService as ServiceDefTarget
		endpoint.serviceEntryPoint = GWT::getModuleBaseURL() + "tutorialservice"
		tutorialService.getText(number, ExplorViz::controlGroupActive, new TextCallback())
	}

	/**
	 * Complete a tutorial step, increment the counter, check for end of tutorial, 
	 * check for continue-button or arrows to display, set correct safe step, load new text.
	 */
	def static void incStep() {
		//Tutorial completed
//		if(tutorialStep == 0){
		if (tutorialStep + 1 == tutorialsteps.size) {
			redrawTimer.cancel()
			TutorialJS.closeTutorialDialog()
			TutorialJS.hideArrows()
			tutorialStep = 0
			tutorial = false
			SceneDrawer::lastViewedApplication = null
			ExplorViz.toMainPage()
		} else {
			tutorialStep = tutorialStep + 1
			getTutorialText(tutorialStep)
			if (step.requiresButton) {
				TutorialJS.showTutorialContinueButton()
			} else {
				TutorialJS.removeTutorialContinueButton()
			}
			if (step.backToLandscape) {
				TutorialJS.showBackToLandscapeArrow()
				lastSafeStep = tutorialStep //safe step
			} else if (step.timeshift) {
				TutorialJS.showTimshiftArrow()
				lastSafeStep = tutorialStep //safe step
			} else if (step.choosetrace || step.startanalysis || step.pauseanalysis || step.nextanalysis || step.codeview || step.leaveanalysis){
				//no safe step
				//change location of the tutorial Dialog into the upper corner
				if(step.startanalysis || step.pauseanalysis || step.nextanalysis || step.leaveanalysis) {
					TutorialJS.changeDialogLocationToUpperLeftCorner();
					//the location of the dialog gets set back to default after each step
					// we set it to upper left again afterwards (in case of analysis steps behind each other)
				}
			} else{
				lastSafeStep = tutorialStep //safe step
				TutorialJS.hideArrows()
			}
			redrawTimer.schedule(1000) 

			//if second next step is a timeshift step
			if ((tutorialStep + 2 < tutorialsteps.size) && (tutorialsteps.get(tutorialStep + 2).timeshift)) {
				loadOtherLandscape = true
				setTutorialLandscape(true)

			}
		}
	}

	/**
	 * Loads the correct tutorial landscape.
	 * @param secondLandscape - true if a timeshift-step is to be made to load the new landscape
	 */
	def static setTutorialLandscape(boolean secondLandscape) {
		val TutorialServiceAsync tutorialService = GWT::create(typeof(TutorialService))
		val endpoint = tutorialService as ServiceDefTarget
		endpoint.serviceEntryPoint = GWT::getModuleBaseURL() + "tutorialservice"
		tutorialService.setTimeshift(secondLandscape, System.currentTimeMillis(), new VoidCallback())
	}

	/**
	 * Returns current tutorial step and calls loadTutorial if there are no steps.
	 * 
	 * @return the current tutorial step
	 */
	def static getStep() {
		if (null == tutorialsteps || tutorialsteps.empty) {
			loadTutorial()
		}
		tutorialsteps.get(tutorialStep)
	}
	
	/**
	 * @return the current safe step
	 */
	def static getSafeStep(){
		if (null == tutorialsteps || tutorialsteps.empty) {
			loadTutorial()
		}
		tutorialsteps.get(lastSafeStep)
	}
	
	/**
	 * @return the previous step
	 */
	def static getPreviousStep(){
		if(tutorialStep > 0){
			tutorialsteps.get(tutorialStep-1)
		}
		else{
			tutorialsteps.get(tutorialStep)
		}
	}

	/**
 	* Tutorialsteps for components
 	* @param name name of the component
 	* @param left true if left click
 	* @param right true if right click
 	* @param doubleC true if double click
 	* @param hover true if hovering
 	*/
	def static incTutorial(String name, boolean left, boolean right, boolean doubleC, boolean hover) {
		if (tutorial) {
			val step = getStep()
			if ((!step.connection && name!=null && name.equals(step.source)) && ((left && step.leftClick) || (right && step.rightClick) ||
				(doubleC && step.doubleClick) || (hover && step.hover)
				)) {
				incStep()
			}
		}
	}

	/**
	 * tutorialsteps for communications
	 * @param source name of the source component
	 * @param target name of the target component
	 * @param left true if left click
 	 * @param right true if right click
 	 * @param hover true if hovering
	 */
	def static incTutorial(String source, String target, boolean left, boolean right, boolean hover) {
		if (tutorial) {
			val step = getStep()
			if (step.connection && source.equals(step.source) && target.equals(step.dest) &&
				((left && step.leftClick) || (right && step.rightClick) || (hover && step.hover))) {
				incStep()
			}
		}
	}

	/**
	 * Draws an Arrow that points (from above) to the given coordinates
	 * @param x - x coordinate of the tip of the arrowhead
	 * @param y - y coordinate of the tip of the arrowhead
	 */
	def static List<PrimitiveObject> drawArrow(float x, float y, float z) {
		var arrowhead = new Triangle(null, RED, false, true, new Vector3f(x, y, z), new Vector3f(x + 0.5f, y + 0.5f, z),
			new Vector3f(x - 0.5f, y + 0.5f, z), 1f, 0f, 0f, 1f, 1f, 1f)
		val bl = new Vector3f(x - 0.25f, y + 0.5f, z)
		val br = new Vector3f(x + 0.25f, y + 0.5f, z)
		val tl = new Vector3f(x - 0.25f, y + 1.5f, z)
		val tr = new Vector3f(x + 0.25f, y + 1.5f, z)
		var color = RED
		var arrowshaft = new Quad(bl, br, tr, tl, color, false, false)
		var List<PrimitiveObject> arrow = new ArrayList()
		arrow.add(arrowshaft)
		arrow.add(arrowhead)
		return arrow
	}

	/**
	 * Draws an Arrow that points (from above) to the given coordinates
	 * @param x - x coordinate of the tip of the arrowhead
	 * @param y - y coordinate of the tip of the arrowhead
	 * @param z - z coordinate of the tip of the arrowhead
	 */
	def static List<PrimitiveObject> draw3DArrow(float x, float y, float z) {
		var arrowhead = new Triangle(null, RED, false, true, new Vector3f(x, y, z + 3f),
			new Vector3f(x + 3f, y + 3f, z + 3f), new Vector3f(x - 3f, y + 3f, z + 3f), 1f, 0f, 0f, 1f, 1f, 1f)
		val bl = new Vector3f(x - 2f, y + 3f, z + 3f)
		val br = new Vector3f(x + 2f, y + 3f, z + 3f)
		val tl = new Vector3f(x - 2f, y + 11f, z + 3f)
		val tr = new Vector3f(x + 2f, y + 11f, z + 3f)
		var color = RED
		var arrowshaft = new Quad(bl, br, tr, tl, color, false, true)
		var List<PrimitiveObject> arrow = new ArrayList()
		arrow.add(arrowshaft)
		arrow.add(arrowhead)
		return arrow
	}

	def static List<PrimitiveObject> drawTutorial(
		String name,
		Vector3f pos,
		float width,
		float height,
		Vector3f center) {
		if (tutorial) {
			val step = getStep()
			if (!step.connection && name.equals(step.source)) {
				var float x = pos.x + width / 2f - center.x
				var float y = pos.y - height / 16f - center.y
				drawArrow(x, y, pos.z + 1f)
			}else{
				return emptyList
			}
		}else{
			return emptyList
		}
	}

	def static List<PrimitiveObject> draw3DTutorial(
		String name,
		Vector3f entityPos,
		float width,
		float height,
		float depth,
		Vector3f viewCenter, boolean clazz) {
		if (tutorial) {
			val step = getStep()
			if(!step.connection && name.equals(step.source)){
				if(clazz){
					val centerX = entityPos.x + (width / 2f) - viewCenter.x
					val centerY = entityPos.y + height - viewCenter.y
					val centerZ = entityPos.z - depth - viewCenter.z
					draw3DArrow(centerX, centerY, centerZ)
				}else{
					val centerX = entityPos.x + (width / 2f) - viewCenter.x
					val centerY = entityPos.y + height - viewCenter.y
					val centerZ = entityPos.z + (depth / 2f) - viewCenter.z
					draw3DArrow(centerX, centerY, centerZ)
				}
			}else{
				return emptyList
			}
		}else{
			return emptyList
		}
	}
	
	def static drawTutorialCom(String source, String dest, Vector3f pos, float width, 
		float height, Vector3f center){
		if(tutorial){
			val step = getStep()
			if(step.connection && source.equals(step.source) && dest.equals(step.dest)){
				var x = pos.x - center.x + width/2f
				var y = pos.y - center.y + height/2f
				drawArrow(x, y, pos.z+0.5f)
			}else{
				return emptyList
			}
		}else{
			return emptyList
		}
	}

	def static draw3DTutorialCom(String source, String dest, Vector3f pos, Vector3f pos2, Vector3f center){
		if(tutorial){
			val step = getStep()
			if (step.connection && source.equals(step.source) && dest.equals(step.dest)) {
				var x = pos.x + (pos2.x - pos.x) / 5f - center.x
				var y = pos.y + (pos2.y - pos.y) / 5f - center.y
				var z = pos.z + (pos2.z - pos.z) / 5f - center.z
				if(source.equals("FileUtils") && dest.equals("TransactionImpl")) {
					x = x + 5;	//not pretty, its hardcoded, but works -> there is (seems to be always) an offset with this arrow
				}
				draw3DArrow(x, y, z)
			}else{
				return emptyList
			}
		}else{
			return emptyList
		}
	}
}

class IsExperimentCallback implements AsyncCallback<Boolean> {

	override onFailure(Throwable caught) {
		ErrorDialog::showError(caught)
	}

	override onSuccess(Boolean result) {
		Experiment::experiment = result
	}

}