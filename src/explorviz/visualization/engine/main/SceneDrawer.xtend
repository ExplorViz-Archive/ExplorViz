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

	private new() {
	}

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

	private static def void setNodeStatesFromOldApplicationHelper(List<Component> oldCompos,
		List<Component> newCompos) {
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

		LandscapeInteraction::createInteraction(landscape)

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

			Camera::rotateModelX(40)
			Camera::rotateModelY(50)
		}

		glContext.uniform1f(shaderObject.useLightingUniform, 1)

		Clustering::doSyntheticClustering(application)

		LayoutService::layoutApplication(application)

		LandscapeInteraction::clearInteraction(application.parent.parent.parent.parent)
		ApplicationInteraction::clearInteraction(application)

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

	def static void drawScene() {
		glContext.clear(clearMask)

		GLManipulation::loadIdentity

		val cameraModelRotate = Navigation::getCameraModelRotate()
		GLManipulation::rotateX(cameraModelRotate.x)
		GLManipulation::rotateY(cameraModelRotate.y)
		GLManipulation::rotateZ(cameraModelRotate.z)
		
		if (lastViewedApplication != null) {
			GLManipulation::translate(Navigation::getCameraPoint())
		
			val cameraRotate = Navigation::getCameraRotate()
			GLManipulation::rotateX(cameraRotate.x)
			GLManipulation::rotateY(cameraRotate.y)
			GLManipulation::rotateZ(cameraRotate.z)
			
			GLManipulation::translate(Navigation::getCameraPoint().mult(-1))
		}
		
		GLManipulation::translate(Navigation::getCameraPoint())
		
		GLManipulation::activateModelViewMatrix

		drawObjects()
	}

	def static private void drawObjects() {
		BoxContainer::drawLowLevelBoxes
		PipeContainer::drawTransparentPipes
		PipeContainer::drawPipes
		BoxContainer::drawHighLevelBoxes

		QuadContainer::drawQuads
		LineContainer::drawLines
		QuadContainer::drawQuadsWithAppTexture

		for (polygon : polygons) {
			polygon.draw()
		}

		LabelContainer::draw
	}

	def static void drawScene2() {
		glContext.clear(clearMask)
		
		if (perspectiveMatrixLeftEye != null) {
			glContext.uniformMatrix4fv(WebGLStart::perspectiveMatrixLocation, false,
				FloatArray::create(perspectiveMatrixLeftEye.entries))
		}

		glContext.viewport(0, 0, WebGLStart::viewportWidth / 2, WebGLStart::viewportHeight)
		GLManipulation::loadIdentity
		val leftEyeTrans = new Vector3f(Navigation::getCameraPoint())
		if (leftEyeCameraVector != null)
			leftEyeTrans.add(leftEyeCameraVector)
		GLManipulation::translate(leftEyeTrans)

		GLManipulation::scale(2, 1, 1)

		val cameraRotate = Navigation::getCameraModelRotate()
		GLManipulation::rotateX(cameraRotate.x)
		GLManipulation::rotateY(cameraRotate.y)
		GLManipulation::rotateZ(cameraRotate.z)

		GLManipulation::activateModelViewMatrix

		drawObjects()

		if (perspectiveMatrixRightEye != null) {
			glContext.uniformMatrix4fv(WebGLStart::perspectiveMatrixLocation, false,
				FloatArray::create(perspectiveMatrixRightEye.entries))
		}

		glContext.viewport(WebGLStart::viewportWidth / 2, 0, WebGLStart::viewportWidth / 2, WebGLStart::viewportHeight)
		GLManipulation::loadIdentity
		val rightEyeTrans = new Vector3f(Navigation::getCameraPoint())
		if (rightEyeCameraVector != null)
			rightEyeTrans.add(rightEyeCameraVector)
		GLManipulation::translate(rightEyeTrans)

		GLManipulation::scale(2, 1, 1)

		GLManipulation::rotateX(cameraRotate.x)
		GLManipulation::rotateY(cameraRotate.y)
		GLManipulation::rotateZ(cameraRotate.z)

		GLManipulation::activateModelViewMatrix

		drawObjects()
	}

	def static void redraw() {
		viewScene(lastLandscape, true)
	}
}
