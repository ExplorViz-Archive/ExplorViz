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
import explorviz.visualization.engine.primitives.LabelContainer
import explorviz.visualization.engine.shaders.ShaderInitializer
import explorviz.visualization.engine.textures.TextureManager
import explorviz.visualization.interaction.Usertracking
import explorviz.visualization.landscapeexchange.LandscapeExchangeManager
import explorviz.visualization.main.JSHelpers
import explorviz.visualization.renderer.LandscapeRenderer
import explorviz.visualization.timeshift.TimeShiftExchangeManager

class WebGLStart {
	public static WebGLRenderingContext glContext
	public static var Matrix44f perspectiveMatrix
	public static boolean explorVizVisible = true

	public static int viewportWidth
	public static int viewportHeight

	static HandlerRegistration startAndStopTimeshiftHandler
	static val startAndStopTimeshiftButtonId = "startStopBtn"
	static val startAndStopTimeshiftLabelId = "startStopLabel"
	
	static var WebGLUniformLocation perspectiveMatrixLocation
	static var float lastPerspectiveZ
	
	static AnimationScheduler animationScheduler
	static AnimationHandle animationHandler
	
	static com.google.gwt.dom.client.Element webglCanvasElement
	
	def static void initWebGL() {
		explorVizVisible = true
		val Element viewElement = Browser::getDocument().getElementById("view")

		val Element webglDiv = Browser::getDocument().createDivElement()
		webglDiv.setId("webglDiv")

		val Element timeshiftChart = Browser::getDocument().createDivElement()
		timeshiftChart.setId("timeshiftChartDiv")
		timeshiftChart.style.setPosition("absolute")
		timeshiftChart.style.setTop(viewElement.clientTop + viewElement.clientHeight - 100 + "px")
		timeshiftChart.style.setLeft("5px")
		timeshiftChart.style.setHeight("100px")
		timeshiftChart.style.setWidth(viewElement.clientWidth + 5 + "px")
		val Element svgChart = Browser::getDocument().createSVGElement()
		svgChart.setId("timeshiftChart")

		animationScheduler = AnimationScheduler::get()
		viewportWidth = viewElement.clientWidth
		viewportHeight = viewElement.clientHeight - 100
		val webGLCanvas = Browser::getDocument().createCanvasElement()

		webGLCanvas.setWidth(viewportWidth)
		webGLCanvas.setHeight(viewportHeight)

		webGLCanvas.setId("webglcanvas")
		
		Browser::getDocument().getElementById("view").appendChild(webglDiv)
		Browser::getDocument().getElementById("webglDiv").appendChild(webGLCanvas)

		Browser::getDocument().getElementById("view").appendChild(timeshiftChart)
		Browser::getDocument().getElementById("timeshiftChartDiv").appendChild(svgChart)
		
		showAndPrepareStartAndStopTimeshiftButton()

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
		GLManipulation::init(glContext)
		TextureManager::init()
		LabelContainer::init()
		FPSCounter::init(RootPanel::get("fpsLabel").getElement())
		lastPerspectiveZ = 10000f

		initOpenGL()
		ObjectPicker::init()
		AdaptiveMonitoring::init()

		perspectiveMatrixLocation = glContext.getUniformLocation(ShaderInitializer::getShaderProgram(), "perspectiveMatrix")
		
		LandscapeExchangeManager::init()
		TimeShiftExchangeManager::init()
		
		CodeViewer::init()
		
		if (SceneDrawer::lastLandscape != null) {
			LandscapeRenderer::calculateCenterAndZZoom(SceneDrawer::lastLandscape)
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

	def private static void setPerspective(float z) {
		if (z - lastPerspectiveZ < 0.001f && z - lastPerspectiveZ > -0.001f) {
			return
		}
		
		perspectiveMatrix = Matrix44f::ortho(((viewportWidth / (viewportHeight as float)) * z) / 2f, z / 2f,
			100000f)
		glContext.uniformMatrix4fv(perspectiveMatrixLocation, false, FloatArray::create(perspectiveMatrix.entries))

		ProjectionHelper::setMatrix(perspectiveMatrix)
		lastPerspectiveZ = z
	}
	
	def static void cancelAnimationHandler() {
		animationHandler.cancel
	}

	def static void tick(AnimationCallback animationCallBack) {
		if (explorVizVisible) {
			animationHandler = animationScheduler.requestAnimationFrame(animationCallBack, webglCanvasElement)
		}
		Navigation::navigationCallback()
		setPerspective(-Camera::vector.z)
		SceneDrawer::drawScene()

		FPSCounter::countFPS()
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
}

class MyAnimationCallBack implements AnimationCallback {
	override execute(double timestamp) {
		WebGLStart::tick(this)
	}
}
