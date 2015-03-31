package explorviz.visualization.engine.main

import elemental.html.WebGLRenderingContext
import explorviz.shared.model.Application
import explorviz.shared.model.Component
import explorviz.shared.model.Landscape
import explorviz.shared.model.NodeGroup
import explorviz.shared.model.System
import explorviz.visualization.clustering.Clustering
import explorviz.visualization.engine.animation.ObjectMoveAnimater
import explorviz.visualization.engine.buffer.BufferManager
import explorviz.visualization.engine.navigation.Camera
import explorviz.visualization.engine.navigation.Navigation
import explorviz.visualization.engine.primitives.BoxContainer
import explorviz.visualization.engine.primitives.LabelContainer
import explorviz.visualization.engine.primitives.LineContainer
import explorviz.visualization.engine.primitives.PipeContainer
import explorviz.visualization.engine.primitives.PrimitiveObject
import explorviz.visualization.engine.primitives.QuadContainer
import explorviz.visualization.engine.shaders.ShaderInitializer
import explorviz.visualization.engine.shaders.ShaderObject
import explorviz.visualization.interaction.ApplicationInteraction
import explorviz.visualization.interaction.LandscapeInteraction
import explorviz.visualization.layout.LayoutService
import explorviz.visualization.renderer.ApplicationRenderer
import explorviz.visualization.renderer.LandscapeRenderer
import java.util.ArrayList
import java.util.List
import explorviz.visualization.engine.math.Matrix44f
import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.engine.FloatArray
import explorviz.visualization.interaction.ModelingInteraction
import explorviz.visualization.engine.math.Vector4f
import explorviz.visualization.engine.primitives.MouseCursor

class SceneDrawer {
	static WebGLRenderingContext glContext
	static ShaderObject shaderObject

	public static Landscape lastLandscape
	public static Application lastViewedApplication

	static val clearMask = WebGLRenderingContext::COLOR_BUFFER_BIT.bitwiseOr(WebGLRenderingContext::DEPTH_BUFFER_BIT)
	static val polygons = new ArrayList<PrimitiveObject>(256)

	static Matrix44f perspectiveMatrixLeftEye

	static Matrix44f perspectiveMatrixRightEye

	static Vector3f leftEyeCameraVector

	static Vector3f rightEyeCameraVector
	
	private static boolean mouseCursorAlreadySet = false
	private static MouseCursor mouseCursor

	def static init(WebGLRenderingContext glContextParam) {
		glContext = glContextParam
		shaderObject = ShaderInitializer::initShaders(glContext)

		//ErrorChecker::init(glContext)
		BufferManager::init(glContext, shaderObject)

		polygons.clear
	}

	def static void viewScene(Landscape landscape, boolean doAnimation) {
		if (lastViewedApplication == null) {
			if (lastLandscape != null) {
				setOpenedAndClosedStatesLandscape(lastLandscape, landscape)
			}
			createObjectsFromLandscape(landscape, doAnimation)
		} else {
			for (system : landscape.systems) {
				for (nodegroup : system.nodeGroups) {
					for (node : nodegroup.nodes) {
						for (application : node.applications) {
							if (lastViewedApplication.id == application.id) {
								setStatesFromOldApplication(lastViewedApplication, application)								
								createObjectsFromApplication(application, doAnimation)
								return;
							}
						}
					}
				}
			}
		}
	}

	private static def void setOpenedAndClosedStatesLandscape(Landscape oldLandscape, Landscape landscape) {
		for (system : landscape.systems) {
			setOpenedAndClosedStatesLandscapeHelper(oldLandscape, system)
		}
	}

	private static def void setOpenedAndClosedStatesLandscapeHelper(Landscape oldLandscape, System system) {
		for (oldSystem : oldLandscape.systems) {
			if (system.name == oldSystem.name) {
				for (nodegroup : system.nodeGroups) {
					setOpenedAndClosedStatesLandscapeHelperNodeGroup(oldSystem, nodegroup)
				}
				if (oldSystem.opened != system.opened) {
					system.opened = oldSystem.opened
				}
				return
			}
		}
	}

	private static def void setOpenedAndClosedStatesLandscapeHelperNodeGroup(System oldSystem, NodeGroup nodegroup) {
		for (oldNodegroup : oldSystem.nodeGroups) {
			if (nodegroup.name == oldNodegroup.name) {
				if (oldNodegroup.opened != nodegroup.opened) {
					nodegroup.opened = oldNodegroup.opened
				}
				return
			}
		}
	}

	private static def void setStatesFromOldApplication(Application oldApplication, Application application) {
		setNodeStatesFromOldApplicationHelper(oldApplication.components, application.components)
	}

	private static def void setNodeStatesFromOldApplicationHelper(List<Component> oldCompos, List<Component> newCompos) {
		for (oldCompo : oldCompos) {
			for (newCompo : newCompos) {
				if (newCompo.name == oldCompo.name) {
					newCompo.opened = oldCompo.opened
					newCompo.highlighted = oldCompo.highlighted

					for (oldClazz : oldCompo.clazzes) {
						for (newClazz : newCompo.clazzes) {
							if (oldClazz.name == newClazz.name) {
								newClazz.highlighted = oldClazz.highlighted
							}
						}
					}

					setNodeStatesFromOldApplicationHelper(oldCompo.children, newCompo.children)
				}
			}
		}
	}

	def static void createObjectsFromLandscape(Landscape landscape, boolean doAnimation) {
		polygons.clear
		//mouseCursor = null
		//mouseCursorAlreadySet = false
		lastLandscape = landscape
		lastViewedApplication = null
		if (!doAnimation) {
			Camera::resetTranslate
			Camera::resetModelRotate()
			Camera::resetRotate()
		}

		glContext.uniform1f(shaderObject.useLightingUniform, 0)

		LayoutService::layoutLandscape(landscape)

		LandscapeInteraction::clearInteraction(landscape)

		BufferManager::begin
		LandscapeRenderer::drawLandscape(landscape, polygons, !doAnimation)
		BufferManager::end

		if (WebGLStart::modelingMode) {
			ModelingInteraction::createInteraction(landscape)
		} else {
			LandscapeInteraction::createInteraction(landscape)
		}

		if (doAnimation) {
			ObjectMoveAnimater::startAnimation()
		}
	}

	def static void createObjectsFromApplication(Application application, boolean doAnimation) {
		polygons.clear
		lastViewedApplication = application
		if (!doAnimation) {
			Camera::resetTranslate
			Camera::resetModelRotate()
			Camera::resetRotate()

			Camera::rotateModelX(45)
			Camera::rotateModelY(45)
		}

		glContext.uniform1f(shaderObject.useLightingUniform, 1)

		Clustering::doSyntheticClustering(application)

		LayoutService::layoutApplication(application)

		LandscapeInteraction::clearInteraction(application.parent.parent.parent.parent)
		ApplicationInteraction::clearInteraction(application)
		
		if(WebGLStart::webVRMode && !polygons.contains(mouseCursor)) polygons.add(mouseCursor)		

		BufferManager::begin
		ApplicationRenderer::drawApplication(application, polygons, !doAnimation)
		BufferManager::end

		ApplicationInteraction::createInteraction(application)

		if (doAnimation) {
			ObjectMoveAnimater::startAnimation()
		}
	}

	def static void setPerspectiveLeftEye(float[] floatArr) {
		perspectiveMatrixLeftEye = new Matrix44f(floatArr.get(0), floatArr.get(1), floatArr.get(2), floatArr.get(3),
			floatArr.get(4), floatArr.get(5), floatArr.get(6), floatArr.get(7), floatArr.get(8), floatArr.get(9),
			floatArr.get(10), floatArr.get(11), floatArr.get(12), floatArr.get(13), floatArr.get(14), floatArr.get(15))
	}

	def static void setLeftEyeCamera(float[] floatArr) {
		leftEyeCameraVector = new Vector3f(floatArr.get(0), floatArr.get(1), floatArr.get(2))
	}

	def static void setPerspectiveRightEye(float[] floatArr) {
		perspectiveMatrixRightEye = new Matrix44f(floatArr.get(0), floatArr.get(1), floatArr.get(2), floatArr.get(3),
			floatArr.get(4), floatArr.get(5), floatArr.get(6), floatArr.get(7), floatArr.get(8), floatArr.get(9),
			floatArr.get(10), floatArr.get(11), floatArr.get(12), floatArr.get(13), floatArr.get(14), floatArr.get(15))
	}

	def static void setRightEyeCamera(float[] floatArr) {
		rightEyeCameraVector = new Vector3f(floatArr.get(0), floatArr.get(1), floatArr.get(2))
	}
	
	def static void setBothEyesCameras(float[] floatArrLeftEye, float[] floatArrRightEye) {
		leftEyeCameraVector = new Vector3f(floatArrLeftEye.get(0), floatArrLeftEye.get(1), floatArrLeftEye.get(2))
		rightEyeCameraVector = new Vector3f(floatArrRightEye.get(0), floatArrRightEye.get(1), floatArrRightEye.get(2))
	}

	def static void drawScene() {
		glContext.clear(clearMask)

		WebGLManipulation::loadIdentity

		val cameraModelRotate = Navigation::getCameraModelRotate()
		WebGLManipulation::rotateY(cameraModelRotate.y)
		WebGLManipulation::rotateX(cameraModelRotate.x)

		if (lastViewedApplication != null) {
			WebGLManipulation::translate(Navigation::getCameraPoint())

			val cameraRotate = Navigation::getCameraRotate()
			WebGLManipulation::rotateX(cameraRotate.x)
			WebGLManipulation::rotateY(cameraRotate.y)
			WebGLManipulation::rotateZ(cameraRotate.z)

			WebGLManipulation::translate(Navigation::getCameraPoint().mult(-1))
		}

		WebGLManipulation::translate(Navigation::getCameraPoint())

		WebGLManipulation::activateModelViewMatrix

		drawObjects()
	}

	def static private void drawObjects() {
		BoxContainer::drawLowLevelBoxes
		LabelContainer::drawDownwardLabels
		PipeContainer::drawTransparentPipes
		PipeContainer::drawPipes
		BoxContainer::drawHighLevelBoxes

		QuadContainer::drawQuads
		LineContainer::drawLines
		QuadContainer::drawQuadsWithAppTexture	
		
		var Integer cursorIndex = null
		var int polygonsSize = polygons.size()
		
		for(i : 0..< polygonsSize) {
			if(polygons.get(i) instanceof MouseCursor) {
				cursorIndex = new Integer(i)
			} else {				
				polygons.get(i).draw()			
			}				
		}			
		
		LabelContainer::draw
		
		if(cursorIndex != null && WebGLStart::webVRMode) drawMouseCursor(polygons.get(cursorIndex))
	}

	def static void drawSceneForWebVR() {
		glContext.clear(clearMask)

		if (perspectiveMatrixLeftEye != null) {
			glContext.uniformMatrix4fv(WebGLStart::perspectiveMatrixLocation, false,
				FloatArray::create(perspectiveMatrixLeftEye.entries))
		}

		glContext.viewport(0, 0, WebGLStart::viewportWidth / 2, WebGLStart::viewportHeight)

		setLeftEyeModelViewMatrix()

		WebGLManipulation::activateModelViewMatrix

		drawObjects()

		if (perspectiveMatrixRightEye != null) {
			glContext.uniformMatrix4fv(WebGLStart::perspectiveMatrixLocation, false,
				FloatArray::create(perspectiveMatrixRightEye.entries))
		}

		glContext.viewport(WebGLStart::viewportWidth / 2, 0, WebGLStart::viewportWidth / 2, WebGLStart::viewportHeight)

		setRightEyeModelViewMatrix()

		WebGLManipulation::activateModelViewMatrix

		drawObjects()
	}

	def static void setLeftEyeModelViewMatrix() {
		WebGLManipulation::loadIdentity
		val leftEyeTrans = new Vector3f(Navigation::getCameraPoint())
		if (leftEyeCameraVector != null)
			leftEyeTrans.add(leftEyeCameraVector)

		var cameraModelRotate = Navigation::getCameraModelRotate()
		WebGLManipulation::rotateY(cameraModelRotate.y)
		WebGLManipulation::rotateX(cameraModelRotate.x)

		if (lastViewedApplication != null) {
			WebGLManipulation::translate(leftEyeTrans)

			var cameraRotate = Navigation::getCameraRotate()
			WebGLManipulation::rotateX(cameraRotate.x)
			WebGLManipulation::rotateY(cameraRotate.y)
			WebGLManipulation::rotateZ(cameraRotate.z)

			WebGLManipulation::translate(leftEyeTrans.mult(-1))
		}

		WebGLManipulation::translate(leftEyeTrans)
	}

	def static void setRightEyeModelViewMatrix() {
		WebGLManipulation::loadIdentity
		val rightEyeTrans = new Vector3f(Navigation::getCameraPoint())
		if (rightEyeCameraVector != null)
			rightEyeTrans.add(rightEyeCameraVector)

		val cameraModelRotate = Navigation::getCameraModelRotate()
		WebGLManipulation::rotateY(cameraModelRotate.y)
		WebGLManipulation::rotateX(cameraModelRotate.x)

		if (lastViewedApplication != null) {
			WebGLManipulation::translate(rightEyeTrans)

			val cameraRotate = Navigation::getCameraRotate()
			WebGLManipulation::rotateX(cameraRotate.x)
			WebGLManipulation::rotateY(cameraRotate.y)
			WebGLManipulation::rotateZ(cameraRotate.z)

			WebGLManipulation::translate(rightEyeTrans.mult(-1))
		}

		WebGLManipulation::translate(rightEyeTrans)
	}

	def static void redraw() {
		viewScene(lastLandscape, true)
	}
	
	def static void drawMouseCursor(PrimitiveObject cursor) {								
			WebGLManipulation.loadIdentity		
			WebGLManipulation.activateModelViewMatrix
			
			cursor.draw()		
				
			WebGLManipulation::loadIdentity
			val cameraModelRotate = Navigation::getCameraModelRotate
			WebGLManipulation::rotateY(cameraModelRotate.y)
			WebGLManipulation::rotateX(cameraModelRotate.x)

			if (lastViewedApplication != null) {
				WebGLManipulation::translate(Navigation::getCameraPoint())

				val cameraRotate = Navigation::getCameraRotate()
				WebGLManipulation::rotateX(cameraRotate.x)
				WebGLManipulation::rotateY(cameraRotate.y)
				WebGLManipulation::rotateZ(cameraRotate.z)

				WebGLManipulation::translate(Navigation::getCameraPoint().mult(-1))
			}
			WebGLManipulation::translate(Navigation::getCameraPoint())
			WebGLManipulation::activateModelViewMatrix
	}
	
	def static void updateMousecursorVertices(float diffX, float diffY) {	
		if(WebGLStart::webVRMode){				
			mouseCursor.moveByVector(new Vector3f(diffX, diffY, 0f))		 
		 }		
	}
	
	def static void setMousecursor(boolean enable) {		
		if(enable) {
			
			if(!mouseCursorAlreadySet) {					
				mouseCursor = new MouseCursor(null, new Vector4f(0.0f, 0.0f, 0.1f, 1.0f), false, true, 
				new Vector3f(0f, 0f, -30f), 1f, 0f, 0f, 1f, 1f, 1f)
				mouseCursorAlreadySet = true						
			}		
			mouseCursor.resetCoordinates()		
			polygons.add(mouseCursor)							
		} else {						
			polygons.remove(mouseCursor)						
		}		
	}	
	
	
}
