package explorviz.visualization.engine.main

import elemental.html.WebGLRenderingContext
import explorviz.shared.model.Application
import explorviz.shared.model.Component
import explorviz.shared.model.Landscape
import explorviz.shared.model.System
import explorviz.shared.model.NodeGroup
import explorviz.visualization.engine.animation.ObjectMoveAnimater
import explorviz.visualization.engine.buffer.BufferManager
import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.engine.navigation.Camera
import explorviz.visualization.engine.navigation.Navigation
import explorviz.visualization.engine.shaders.ShaderInitializer
import explorviz.visualization.engine.shaders.ShaderObject
import explorviz.visualization.interaction.ApplicationInteraction
import explorviz.visualization.interaction.LandscapeInteraction
import explorviz.visualization.layout.LayoutService
import explorviz.visualization.renderer.ApplicationRenderer
import explorviz.visualization.renderer.LandscapeRenderer
import java.util.ArrayList
import java.util.List
import explorviz.visualization.engine.primitives.PrimitiveObject

class SceneDrawer {
	static WebGLRenderingContext glContext
	static ShaderObject shaderObject

	public static Landscape lastLandscape
	public static Application lastViewedApplication

	static val clearMask = WebGLRenderingContext::COLOR_BUFFER_BIT.bitwiseOr(WebGLRenderingContext::DEPTH_BUFFER_BIT)
	static val polygons = new ArrayList<PrimitiveObject>(1024)

	static Vector3f lastCameraPoint
	static Vector3f lastCameraRotate

	private new() {
	}

	def static init(WebGLRenderingContext glContextParam) {
		glContext = glContextParam
		shaderObject = ShaderInitializer::initShaders(glContext)

		//ErrorChecker::init(glContext)
		BufferManager::init(glContext, shaderObject)

		lastCameraPoint = new Vector3f()
		lastCameraRotate = new Vector3f()

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
								setOpenedAndClosedStateFromOldApplication(lastViewedApplication, application)
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

	private static def void setOpenedAndClosedStateFromOldApplication(Application oldApplication,
		Application application) {
		setOpenedAndClosedStateFromOldApplicationHelper(oldApplication.components, application.components)
	}

	private static def void setOpenedAndClosedStateFromOldApplicationHelper(List<Component> oldCompos,
		List<Component> newCompos) {
		for (oldCompo : oldCompos) {
			for (newCompo : newCompos) {
				if (newCompo.name == oldCompo.name) {
					newCompo.opened = oldCompo.opened
					setOpenedAndClosedStateFromOldApplicationHelper(oldCompo.children, newCompo.children)
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
			Camera::resetRotate()
		}
		
//		glContext.enable(WebGLRenderingContext::DEPTH_TEST)

		glContext.uniform1f(shaderObject.useLightingUniform, 0)

		//        val startTime = new Date()
		LayoutService::layoutLandscape(landscape)

		//        Logging::log("Time for whole layouting: " + (new Date().time - startTime.time).toString + " msec")
		LandscapeInteraction::clearInteraction(landscape)

		BufferManager::begin
		LandscapeRenderer::drawLandscape(landscape, polygons, !doAnimation)
		BufferManager::end

		LandscapeInteraction::createInteraction(landscape)

		if (doAnimation) {
			ObjectMoveAnimater::startAnimation()
		}

	//        octree = new Octree(polygons)
	}

	def static void createObjectsFromApplication(Application application, boolean doAnimation) {
		polygons.clear
		lastViewedApplication = application
		if (!doAnimation) {
			Camera::resetTranslate
			Camera::resetRotate()

			Camera::rotateX(33)
			Camera::rotateY(45)
		}

		glContext.uniform1f(shaderObject.useLightingUniform, 1)
//		glContext.disable(WebGLRenderingContext::DEPTH_TEST)

		//        var startTime = new Date()
			LayoutService::layoutApplication(application)
		//        Logging::log("Time for whole layouting: " + (new Date().time - startTime.time).toString + " msec")
		
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

	def static drawScene() {
		glContext.clear(clearMask)

//		glContext.uniform1f(shaderObject.timePassedInPercentUniform, ObjectMoveAnimater::getAnimationTimePassedPercent())

		if (!Navigation::getCameraPoint().equals(lastCameraPoint) ||
			!Navigation::getCameraRotate().equals(lastCameraRotate)) {
			GLManipulation::loadIdentity

			GLManipulation::translate(Navigation::getCameraPoint())

			val cameraRotate = Navigation::getCameraRotate()
			GLManipulation::rotateX(cameraRotate.x)
			GLManipulation::rotateY(cameraRotate.y)

			GLManipulation::activateModelViewMatrix

			lastCameraPoint = new Vector3f(Navigation::getCameraPoint())
			lastCameraRotate = new Vector3f(Navigation::getCameraRotate())
		}

		for (polygon : polygons) {
			polygon.draw()
		}

	//        BufferManager::drawAllTriangles()
	//        glContext.flush()
	//        ErrorChecker::checkErrors()
	}

	def static redraw() {
		viewScene(lastLandscape, false)
		drawScene()
	}
}
