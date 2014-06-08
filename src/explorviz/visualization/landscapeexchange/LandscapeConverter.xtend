package explorviz.visualization.landscapeexchange

import com.google.gwt.user.client.rpc.AsyncCallback
import explorviz.shared.model.Application
import explorviz.shared.model.Clazz
import explorviz.shared.model.Communication
import explorviz.shared.model.CommunicationClazz
import explorviz.shared.model.Component
import explorviz.shared.model.Landscape
import explorviz.shared.model.Node
import explorviz.shared.model.NodeGroup
import explorviz.shared.model.System
import explorviz.visualization.engine.main.SceneDrawer
import explorviz.visualization.model.ApplicationClientSide
import explorviz.visualization.model.ClazzClientSide
import explorviz.visualization.model.CommunicationClazzClientSide
import explorviz.visualization.model.CommunicationClientSide
import explorviz.visualization.model.ComponentClientSide
import explorviz.visualization.model.LandscapeClientSide
import explorviz.visualization.model.NodeClientSide
import explorviz.visualization.model.NodeGroupClientSide
import explorviz.visualization.model.SystemClientSide
import explorviz.visualization.renderer.ColorDefinitions
import java.util.HashMap
import java.util.List

class LandscapeConverter<T> implements AsyncCallback<T> {

	var public static LandscapeClientSide oldLandscape

	static val clazzesCache = new HashMap<String, ClazzClientSide>

	override onFailure(Throwable caught) {
		// TODO
		//      new ErrorPage().renderWithMessage(pageControl, caught.getMessage())
	}

	def static reset() {
		destroyOldLandscape()
	}

	override onSuccess(T result) {
		val newLandscape = result as Landscape
		if (oldLandscape == null || newLandscape.hash != oldLandscape.hash) {
			if (oldLandscape != null) {
				destroyOldLandscape()
			}

			clazzesCache.clear()

			// TODO only update
			var landscapeCS = convertToLandscapeCS(result as Landscape)
			clazzesCache.clear()
			SceneDrawer::viewScene(landscapeCS, false)

			oldLandscape = landscapeCS
		}
	}

	def static destroyOldLandscape() {
		if (oldLandscape != null) {
			oldLandscape.destroy()
			oldLandscape = null
		}
	}

	def LandscapeClientSide convertToLandscapeCS(Landscape landscape) {
		val landscapeCS = new LandscapeClientSide()
		landscapeCS.hash = landscape.hash

		landscape.systems.forEach [
			landscapeCS.systems.add(convertToSystemCS(it, landscapeCS))
		]

		landscape.applicationCommunication.forEach [
			landscapeCS.applicationCommunication.add(convertToCommunicationCS(it, landscapeCS))
		]

		landscapeCS
	}

	def CommunicationClientSide convertToCommunicationCS(Communication communication, LandscapeClientSide landscapeCS) {
		val communicationCS = new CommunicationClientSide()
		communicationCS.requestsPerSecond = communication.requestsPerSecond

		communicationCS.source = seekForIdApplication(communication.source.id, landscapeCS)
		communicationCS.target = seekForIdApplication(communication.target.id, landscapeCS)

		if (communicationCS.source != null && communication.sourceClazz != null) {
			communicationCS.sourceClazz = seekForClazz(communication.sourceClazz, communicationCS.source.components)
			communicationCS.source.outgoingCommunications.add(communicationCS)
		}
		if (communicationCS.target != null && communication.targetClazz != null) {
			communicationCS.targetClazz = seekForClazz(communication.targetClazz, communicationCS.target.components)
			communicationCS.target.incomingCommunications.add(communicationCS)
		}

		communicationCS
	}

	def seekForIdApplication(int id, LandscapeClientSide landscapeCS) {
		for (system : landscapeCS.systems) {
			for (nodeGroup : system.nodeGroups) {
				for (node : nodeGroup.nodes) {
					for (application : node.applications) {
						if (application.id == id) {
							return application
						}
					}
				}
			}
		}

		return null
	}

	def ClazzClientSide seekForClazz(Clazz clazz, List<ComponentClientSide> components) {
		for (component : components) {
			for (childClazz : component.clazzes) {
				if (childClazz.fullQualifiedName == clazz.fullQualifiedName) {
					return childClazz
				}
			}
			val result = seekForClazz(clazz, component.children)
			if (result != null) return result
		}

		return null
	}

	def SystemClientSide convertToSystemCS(System system, LandscapeClientSide parent) {
		val systemCS = new SystemClientSide()
		systemCS.parent = parent
		systemCS.name = system.name

		system.nodeGroups.forEach [
			systemCS.nodeGroups.add(convertToNodeGroupCS(it, systemCS))
		]

		// position is important since children have to be created first
		//		systemCS.setOpened(true)
		systemCS
	}

	def NodeGroupClientSide convertToNodeGroupCS(NodeGroup nodeGroup, SystemClientSide parent) {
		val nodeGroupCS = new NodeGroupClientSide()
		nodeGroupCS.parent = parent
		nodeGroupCS.name = nodeGroup.name

		nodeGroup.nodes.forEach [
			nodeGroupCS.nodes.add(convertToNodeCS(it, nodeGroupCS))
		]

		// position is important since children have to be created first
		nodeGroupCS.setOpened(false)

		nodeGroupCS
	}

	def convertToNodeCS(Node node, NodeGroupClientSide parent) {
		val nodeCS = new NodeClientSide()
		nodeCS.parent = parent

		nodeCS.ipAddress = node.ipAddress
		nodeCS.name = node.name
		nodeCS.cpuUtilization = node.cpuUtilization
		nodeCS.freeRAM = node.freeRAM
		nodeCS.usedRAM = node.usedRAM

		node.applications.forEach [
			nodeCS.applications.add(convertToApplicationCS(it, nodeCS))
		]

		nodeCS
	}

	def ApplicationClientSide convertToApplicationCS(Application application, NodeClientSide parent) {
		val applicationCS = new ApplicationClientSide()
		applicationCS.parent = parent

		applicationCS.id = application.id
		applicationCS.database = application.database
		applicationCS.name = application.name
		applicationCS.image = application.image
		applicationCS.lastUsage = application.lastUsage

		val foundationComponent = new ComponentClientSide()
		foundationComponent.setOpened(true)
		foundationComponent.name = application.name
		foundationComponent.fullQualifiedName = application.name
		foundationComponent.belongingApplication = applicationCS
		foundationComponent.color = ColorDefinitions::componentColors.get(0)

		applicationCS.components.add(foundationComponent)

		application.components.forEach [
			foundationComponent.children.add(convertToComponentCS(it, applicationCS, null, 1, true))
		]

		application.communcations.forEach [
			applicationCS.communications.add(convertToCommunicationComponentCS(it, applicationCS.components))
		]

		applicationCS
	}

	def CommunicationClazzClientSide convertToCommunicationComponentCS(CommunicationClazz commu,
		List<ComponentClientSide> components) {
		val commuCS = new CommunicationClazzClientSide()

		commuCS.requestsPerSecond = commu.requestsPerSecond
		commuCS.averageResponseTime = commu.averageResponseTime

		commuCS.source = clazzesCache.get(commu.source.fullQualifiedName)
		commuCS.target = clazzesCache.get(commu.target.fullQualifiedName)

		commuCS
	}

	def ComponentClientSide convertToComponentCS(Component component, ApplicationClientSide belongingApplication,
		ComponentClientSide parentComponent, int index, boolean shouldBeOpened) {
		val componentCS = new ComponentClientSide()
		componentCS.belongingApplication = belongingApplication
		componentCS.parentComponent = parentComponent
		var openNextLevel = shouldBeOpened

		componentCS.name = component.name
		componentCS.fullQualifiedName = component.fullQualifiedName

		if (!openNextLevel) {
			componentCS.opened = false
		} else if (component.children.size == 1) {
			componentCS.opened = true
		} else {
			componentCS.opened = true
			openNextLevel = false
		}

		if (index < ColorDefinitions::componentColors.size()) {
			componentCS.color = ColorDefinitions::componentColors.get(index)
		} else {
			componentCS.color = ColorDefinitions::componentColors.get(ColorDefinitions::componentColors.size() - 1)
		}

		for (child : component.children)
			componentCS.children.add(
				convertToComponentCS(child, belongingApplication, componentCS, index + 1, openNextLevel))

		component.clazzes.forEach [
			componentCS.clazzes.add(convertToClazzCS(it, componentCS, index + 1))
		]

		componentCS
	}

	def ClazzClientSide convertToClazzCS(Clazz clazz, ComponentClientSide parent, int index) {
		val clazzCS = new ClazzClientSide()
		clazzCS.parent = parent

		clazzCS.name = clazz.name
		clazzCS.fullQualifiedName = clazz.fullQualifiedName
		clazzCS.instanceCount = clazz.instanceCount

		clazzesCache.put(clazzCS.fullQualifiedName, clazzCS)

		clazzCS.color = ColorDefinitions::clazzColor

		clazzCS
	}
}
