package explorviz.visualization.engine.main

import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.engine.navigation.Camera
import explorviz.visualization.engine.main.SceneDrawer
import explorviz.visualization.engine.main.GLManipulation
import explorviz.visualization.engine.optional.FPSCounter

import com.google.gwt.user.client.ui.RootPanel
import elemental.html.WebGLRenderingContext
import com.google.gwt.user.client.Window
import com.google.gwt.animation.client.AnimationScheduler$AnimationCallback
import com.google.gwt.animation.client.AnimationScheduler
import explorviz.visualization.engine.math.Matrix44f
import explorviz.visualization.engine.shaders.ShaderInitializer
import explorviz.visualization.engine.octree.Frustum
import explorviz.visualization.engine.FloatArray
import elemental.client.Browser
import explorviz.visualization.engine.navigation.Navigation
import explorviz.visualization.engine.picking.ObjectPicker
import elemental.dom.Element
import explorviz.visualization.landscapeexchange.LandscapeExchangeManager
import explorviz.visualization.timeshift.TimeShiftExchangeManager
import static explorviz.visualization.engine.main.WebGLStart.*
import explorviz.visualization.main.JSHelpers
import com.google.gwt.user.client.Event
import com.google.gwt.event.dom.client.ClickEvent
import com.google.gwt.event.shared.HandlerRegistration

class WebGLStart {
	public static WebGLRenderingContext glContext
	public static boolean explorVizVisible = true

	static int viewportWidth
	static int viewportHeight

	static HandlerRegistration startAndStopTimeshiftHandler
	static val startAndStopTimeshiftButtonId = "startStopBtn"
	static boolean timeshiftStopped = false

	static AnimationScheduler animationScheduler
	
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

		glContext = webGLCanvas.getContext("experimental-webgl") as WebGLRenderingContext
		if (glContext == null) {
			Window::alert("Sorry, Your Browser doesn't support WebGL!")
			return
		}
		glContext.viewport(0, 0, viewportWidth, viewportHeight)

		start()
	}

	def private static start() {
		Camera::init(new Vector3f(0f, 0f, -10f))
		SceneDrawer::init(glContext)
		GLManipulation::init(glContext)
		FPSCounter::init(RootPanel::get("fpsLabel").getElement())

		initOpenGL()
		ObjectPicker::init()
		setPerspective(-Camera::vector.z)

		LandscapeExchangeManager::init()
		TimeShiftExchangeManager::init()

		val animationCallBack = new MyAnimationCallBack()

		tick(animationCallBack)
	}

	def private static initOpenGL() {
		glContext.clearColor(1.0f, 1.0f, 1.0f, 1.0f)
		glContext.clearDepth(1.0f)
		glContext.enable(WebGLRenderingContext::DEPTH_TEST)
		glContext.depthFunc(WebGLRenderingContext::LEQUAL)

		glContext.enable(WebGLRenderingContext::CULL_FACE)
		glContext.cullFace(WebGLRenderingContext::BACK)
	}

	def private static setPerspective(float z) {

		//		val perspectiveMatrix = Matrix44f::perspective(45.0f, viewportWidth
		//			/ (viewportHeight as float), 0.1f, 1000.0f)
		val perspectiveMatrix = Matrix44f::ortho(((viewportWidth / (viewportHeight as float)) * z) / 2f, 0.5f * z,
			100000f)
		val uniformLocation = glContext.getUniformLocation(ShaderInitializer::getShaderProgram(), "perspectiveMatrix")
		glContext.uniformMatrix4fv(uniformLocation, false, FloatArray::create(perspectiveMatrix.entries))

		// TODO in GLManipulation
		Frustum::initPerspectiveMatrix(perspectiveMatrix)
		ObjectPicker::setMatrix(perspectiveMatrix)
	}

	def static tick(AnimationCallback animationCallBack) {
		if (explorVizVisible) {
			animationScheduler.requestAnimationFrame(animationCallBack)
		}
		Navigation::navigationCallback()
		setPerspective(-Camera::vector.z)
		SceneDrawer::drawScene()
		FPSCounter::countFPS()
		return null
	}

	def static disable() {
		explorVizVisible = false
	}

	def static showAndPrepareStartAndStopTimeshiftButton() {
		if (startAndStopTimeshiftHandler != null) {
			startAndStopTimeshiftHandler.removeHandler
		}

		JSHelpers::showElementById(startAndStopTimeshiftButtonId)

		val startAndStopTimeshift = RootPanel::get(startAndStopTimeshiftButtonId)
		startAndStopTimeshift.element.innerHTML = "<span class='glyphicon glyphicon glyphicon-pause'></span> Pause"

		startAndStopTimeshift.sinkEvents(Event::ONCLICK)
		startAndStopTimeshiftHandler = startAndStopTimeshift.addHandler(
			[
				if (timeshiftStopped) {
					LandscapeExchangeManager::startAutomaticExchange
					timeshiftStopped = false
					
					startAndStopTimeshift.element.innerHTML = "<span class='glyphicon glyphicon glyphicon-pause'></span> Pause"
				} else {
					LandscapeExchangeManager::stopAutomaticExchange
					timeshiftStopped = true
					
					startAndStopTimeshift.element.innerHTML = "<span class='glyphicon glyphicon glyphicon-play'></span> Continue"
				}
			], ClickEvent::getType())
	}
}

class MyAnimationCallBack implements AnimationCallback {
	override execute(double timestamp) {
		WebGLStart::tick(this)
	}
}
