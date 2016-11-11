package explorviz.visualization.engine.main

import com.google.gwt.animation.client.AnimationScheduler
import com.google.gwt.animation.client.AnimationScheduler.AnimationCallback
import com.google.gwt.animation.client.AnimationScheduler.AnimationHandle
import com.google.gwt.event.dom.client.ClickEvent
import com.google.gwt.event.shared.HandlerRegistration
import com.google.gwt.user.client.DOM
import com.google.gwt.user.client.Event
import com.google.gwt.user.client.Window
import com.google.gwt.user.client.ui.RootPanel
import elemental.client.Browser
import elemental.dom.Element
import elemental.html.WebGLRenderingContext
import elemental.html.WebGLUniformLocation
import explorviz.visualization.adaptivemonitoring.AdaptiveMonitoring
import explorviz.visualization.codeviewer.CodeViewer
import explorviz.visualization.engine.FloatArray
import explorviz.visualization.engine.math.Matrix44f
import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.engine.navigation.Camera
import explorviz.visualization.engine.navigation.Navigation
import explorviz.visualization.engine.optional.FPSCounter
import explorviz.visualization.engine.picking.ObjectPicker
import explorviz.visualization.engine.primitives.BoxContainer
import explorviz.visualization.engine.primitives.LabelContainer
import explorviz.visualization.engine.primitives.LineContainer
import explorviz.visualization.engine.primitives.QuadContainer
import explorviz.visualization.engine.shaders.ShaderInitializer
import explorviz.visualization.engine.textures.TextureManager
import explorviz.visualization.interaction.Usertracking
import explorviz.visualization.landscapeexchange.LandscapeExchangeManager
import explorviz.visualization.main.JSHelpers
import explorviz.visualization.monitoring.MonitoringManager
import explorviz.visualization.renderer.ApplicationRenderer
import explorviz.visualization.renderer.LandscapeRenderer
import explorviz.visualization.timeshift.TimeShiftExchangeManager

import static explorviz.visualization.engine.main.SceneDrawer.*

class WebGLStart {
	public static WebGLRenderingContext glContext
	public static var Matrix44f perspectiveMatrix
	public static boolean explorVizVisible = true
	public static boolean webVRMode = false
	public static boolean modelingMode = false

	public static var timeshiftHeight = 100 + 30 + 5
	public static val navigationHeight = 60
	
	public static int tempTimeshiftHeight = timeshiftHeight
	public static int tempNavigationHeight = navigationHeight

	public static int viewportWidth
	public static int viewportHeight
	public static float viewportRatio

	static HandlerRegistration startAndStopTimeshiftHandler
	static val startAndStopTimeshiftButtonId = "startStopBtn"
	static val startAndStopTimeshiftLabelId = "startStopLabel"

	public static var WebGLUniformLocation perspectiveMatrixLocation
	static var float lastPerspectiveZ

	static AnimationScheduler animationScheduler
	static AnimationHandle animationHandler

	static com.google.gwt.dom.client.Element webglCanvasElement

	def static void initWebGL() {
		explorVizVisible = true
		val Element viewElement = Browser::getDocument().getElementById("view")

		MonitoringManager::init()

		val Element webglDiv = Browser::getDocument().createDivElement()
		//webglDiv.style.setCssText("position: relative")
		webglDiv.setId("webglDiv")

		if (!modelingMode) {
			val Element timeshiftChart = Browser::getDocument().createDivElement()
			timeshiftChart.setId("timeshiftChartDiv")
			timeshiftChart.style.setPosition("absolute")
			timeshiftChart.style.setTop(viewElement.clientTop + viewElement.clientHeight - 100 + "px")
			timeshiftChart.style.setHeight("100px")
			timeshiftChart.style.setWidth(viewElement.clientWidth + "px")
			//val Element svgChart = Browser::getDocument().createSVGElement()
			//svgChart.setId("timeshiftChart")

			Browser::getDocument().getElementById("view").appendChild(timeshiftChart)
			//Browser::getDocument().getElementById("timeshiftChartDiv").appendChild(svgChart)
		} else {		
			JSHelpers::showElementById("legendDiv")
			timeshiftHeight = 0
		}

		animationScheduler = AnimationScheduler::get()
		viewportWidth = viewElement.clientWidth
		viewportHeight = viewElement.clientHeight - timeshiftHeight
		viewportRatio = viewportWidth / (viewportHeight as float)
		
		
		
		Browser::getDocument().getElementById("view").appendChild(webglDiv)
		
		
		val Element webGLCanvasDiv = Browser::getDocument().createDivElement()
		webGLCanvasDiv.style.setCssText("position: absolute")
		webGLCanvasDiv.style.setCssText("z-index: 8")
		webGLCanvasDiv.setId("webGLCanvasDiv")		
		Browser::getDocument().getElementById("webglDiv").appendChild(webGLCanvasDiv)
		
		val webGLCanvas = Browser::getDocument().createCanvasElement()
		webGLCanvas.setWidth(viewportWidth)
		webGLCanvas.setHeight(viewportHeight)
		webGLCanvas.style.setCssText("border-bottom: solid 1px #DDDDDD")
		webGLCanvas.setId("webglcanvas")		
		Browser::getDocument().getElementById("webGLCanvasDiv").appendChild(webGLCanvas)				

		if (!modelingMode) {
			showAndPrepareStartAndStopTimeshiftButton()
		}

		glContext = webGLCanvas.getContext("webgl") as WebGLRenderingContext
		if (glContext == null) {
			Window::alert("Sorry, Your Browser doesn't support WebGL!")
			return
		}
		glContext.viewport(0, 0, viewportWidth, viewportHeight)

		webglCanvasElement = DOM.getElementById("webglcanvas")

		start()

	}

	def private static start() {
		Camera::init(new Vector3f(0f, 0f, -15f))
		SceneDrawer::init(glContext)
		WebGLManipulation::init(glContext)
		TextureManager::init()

		LabelContainer::init()
		BoxContainer::init()
		QuadContainer::init()
		LineContainer::init()

		FPSCounter::init(RootPanel::get("fpsLabel").getElement())
		lastPerspectiveZ = 10000f

		initOpenGL()
		ObjectPicker::init()
		LandscapeRenderer::init()
		ApplicationRenderer::init()
		AdaptiveMonitoring::init()

		perspectiveMatrixLocation = glContext.getUniformLocation(ShaderInitializer::getShaderProgram(),
			"perspectiveMatrix")

		LandscapeExchangeManager::init()
		TimeShiftExchangeManager::init()

		CodeViewer::init()

		if (SceneDrawer::lastLandscape != null) {
			LandscapeRenderer::calcViewCenterPoint(SceneDrawer::lastLandscape, true)
		}

		SceneDrawer::lastViewedApplication = null

		val animationCallBack = new MyAnimationCallBack()

		tick(animationCallBack)
	}

	def private static initOpenGL() {
		glContext.clearColor(1.0f, 1.0f, 1.0f, 1.0f)
		glContext.clearDepth(1.0f)
		glContext.enable(WebGLRenderingContext::DEPTH_TEST)
		glContext.depthFunc(WebGLRenderingContext::LESS)

		glContext.enable(WebGLRenderingContext::CULL_FACE)
		glContext.cullFace(WebGLRenderingContext::BACK)
	}

	def private static void setPerspective(float z, boolean forced) {
		if (z - lastPerspectiveZ < 0.0001f && z - lastPerspectiveZ > -0.0001f && !forced) {
			return
		}
		perspectiveMatrix = if (SceneDrawer::lastViewedApplication == null) {
			Matrix44f::ortho(((viewportWidth / (viewportHeight as float)) * z) / 2f, z / 2f, 100000f)
		} else {
			Matrix44f::perspective(45.0f, viewportWidth / (viewportHeight as float), 0.1f, 100000f)
		}
		glContext.uniformMatrix4fv(perspectiveMatrixLocation, false, FloatArray::create(perspectiveMatrix.entries))

		ProjectionHelper::setMatrix(perspectiveMatrix)
		lastPerspectiveZ = z
	}

	def static void cancelAnimationHandler() {
		if (animationHandler != null)
			animationHandler.cancel
	}

	def static void tick(AnimationCallback animationCallBack) {
		if (explorVizVisible) {
			animationHandler = animationScheduler.requestAnimationFrame(animationCallBack, webglCanvasElement)
		}
		setPerspective(-Camera::vector.z, false)
		if (!webVRMode) {
			SceneDrawer::drawScene()
		} else {
			WebVRJS::animationTick()
			SceneDrawer::drawSceneForWebVR()

		}

		FPSCounter::countFPS()
	}

	def static void setWebVRMode(boolean enabled) {
		webVRMode = enabled	
		tempTimeshiftHeight = 0
		tempNavigationHeight = 0		

		if (!webVRMode) {		
			SceneDrawer::vrDeviceSet = false	
			tempTimeshiftHeight = timeshiftHeight
		 	tempNavigationHeight = navigationHeight
			
			glContext.viewport(0, 0, WebGLStart::viewportWidth, WebGLStart::viewportHeight)
			setPerspective(-Camera::vector.z, true)			
			SceneDrawer::createObjectsFromApplication(SceneDrawer::lastViewedApplication, false)				
		}
	}

	def static void disable() {
		explorVizVisible = false

		LandscapeExchangeManager::stopAutomaticExchange("0")
		Navigation::deregisterWebGLKeys
		WebGLStart::cancelAnimationHandler
	}

	def static showAndPrepareStartAndStopTimeshiftButton() {
		if (startAndStopTimeshiftHandler != null) {
			startAndStopTimeshiftHandler.removeHandler
		}

		JSHelpers::showElementById(startAndStopTimeshiftButtonId)
		JSHelpers::showElementById(startAndStopTimeshiftLabelId)

		val startAndStopTimeshift = RootPanel::get(startAndStopTimeshiftButtonId)
		startAndStopTimeshift.element.innerHTML = "<span class='glyphicon glyphicon glyphicon-pause'></span> Pause"

		val startAndStopTimeshiftLabel = RootPanel::get(startAndStopTimeshiftLabelId)
		startAndStopTimeshiftLabel.element.innerHTML = ""

		startAndStopTimeshift.sinkEvents(Event::ONCLICK)
		startAndStopTimeshiftHandler = startAndStopTimeshift.addHandler(
			[
				if (LandscapeExchangeManager::timeshiftStopped) {
					Usertracking::trackContinuedLandscapeExchange()
					LandscapeExchangeManager::startAutomaticExchange
				} else {
					val time = System::currentTimeMillis().toString()
					Usertracking::trackStoppedLandscapeExchange(time)
					LandscapeExchangeManager::stopAutomaticExchange(time)
				}
			], ClickEvent::getType())
	}

	def static setModeling(boolean value) {
		modelingMode = value
	}

}

class MyAnimationCallBack implements AnimationCallback {
	override execute(double timestamp) {
		WebGLStart::tick(this)
	}
}
