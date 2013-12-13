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

class WebGLStart {
	public static WebGLRenderingContext glContext
    public static boolean explorVizVisible = true
    
    static int		   viewportWidth
    static int		   viewportHeight
    
    static AnimationScheduler animationScheduler
	
	def static void initWebGL() {
	    explorVizVisible = true
	    val Element viewElement = Browser::getDocument().getElementById("view")
	    
	    animationScheduler = AnimationScheduler::get()
	    viewportWidth = viewElement.clientWidth
        viewportHeight = viewElement.clientHeight
        val webGLCanvas =  Browser::getDocument().createCanvasElement();
        
        webGLCanvas.setWidth(viewportWidth)
        webGLCanvas.setHeight(viewportHeight)
        
        webGLCanvas.setId("webglcanvas")
        Browser::getDocument().getElementById("view").appendChild(webGLCanvas)
        glContext = webGLCanvas.getContext("experimental-webgl") as WebGLRenderingContext
        if (glContext == null) {
            Window::alert("Sorry, Your Browser doesn't support WebGL!")
            return
        }
        glContext.viewport(0, 0, viewportWidth, viewportHeight)
        
        start()
	}
	
	def private static start() {
		Camera::init(new Vector3f(0f,0f,-17f))
		SceneDrawer::init(glContext)
		GLManipulation::init(glContext)
		FPSCounter::init(RootPanel::get("fpsLabel").getElement())
		
		initOpenGL()
		initPerspective()
		
		LandscapeExchangeManager::init()
		
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
    
    def private static initPerspective() {
		val perspectiveMatrix = Matrix44f::perspective(45.0f, viewportWidth
			/ (viewportHeight as float), 0.1f, 1000.0f)
		val uniformLocation = glContext.getUniformLocation(
			ShaderInitializer::getShaderProgram(), "perspectiveMatrix")
		glContext.uniformMatrix4fv(uniformLocation, false, FloatArray::create(perspectiveMatrix.entries))
		// TODO in GLManipulation
		Frustum::initPerspectiveMatrix(perspectiveMatrix)
		ObjectPicker::init(perspectiveMatrix)
    }
    
    def static tick(AnimationCallback animationCallBack) {
        if (explorVizVisible) {
	       animationScheduler.requestAnimationFrame(animationCallBack)
	    }
	    Navigation::navigationCallback()
	    SceneDrawer::drawScene()
	    FPSCounter::countFPS()
    }
    
    def static disable() {
    	explorVizVisible = false
    }
}


class MyAnimationCallBack implements AnimationCallback {
	override execute(double timestamp) {
	    WebGLStart::tick(this)
	}
}