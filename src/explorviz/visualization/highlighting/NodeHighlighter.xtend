package explorviz.visualization.highlighting

import explorviz.shared.model.Application
import explorviz.shared.model.Clazz
import explorviz.shared.model.Component
import explorviz.shared.model.helper.Draw3DNodeEntity
import explorviz.shared.model.helper.EdgeState
import explorviz.visualization.engine.main.SceneDrawer
import explorviz.shared.model.helper.CommunicationAppAccumulator

class NodeHighlighter {
	public static var Draw3DNodeEntity highlightedNode = null

	def static void highlight3DNode(Draw3DNodeEntity node) {
		val highlightedBefore = node.highlighted
		val app = SceneDrawer::lastViewedApplication

		app.unhighlight()

		if (!highlightedBefore) {
			node.highlight()
			highlightedNode = node
			TraceHighlighter::reset(false)
			SceneDrawer::createObjectsFromApplication(app, true)
		} else {
			unhighlight3DNodes()
		}
	}

	def static void unhighlight3DNodes() {
		highlightedNode = null
		
		val app = SceneDrawer::lastViewedApplication
		if (app != null) {
			app.unhighlight()

			SceneDrawer::createObjectsFromApplication(app, true)
		}
	}

	public def static void reset() {
		val app = SceneDrawer::lastViewedApplication
		if (app != null)
			app.unhighlight()
		highlightedNode = null
	}

	public def static void applyHighlighting(Application applicationParam) {
		if (highlightedNode != null) {
			for (commu : applicationParam.communicationsAccumulated) {
				if ((commu.source != null && commu.source.fullQualifiedName == highlightedNode.fullQualifiedName) ||
					(commu.target != null && commu.target.fullQualifiedName == highlightedNode.fullQualifiedName)) {

					val outgoing = determineOutgoing(commu)
					val incoming = determineIncoming(commu)

					if (incoming && outgoing) {
						commu.state = EdgeState.SHOW_DIRECTION_IN_AND_OUT
					} else if (incoming) {
						commu.state = EdgeState.SHOW_DIRECTION_IN
					} else if (outgoing) {
						commu.state = EdgeState.SHOW_DIRECTION_OUT
					}
				} else {
					commu.state = EdgeState.TRANSPARENT
				}
			}
		}
	}

	public def static boolean determineOutgoing(CommunicationAppAccumulator commuApp) {
		for (commu : commuApp.aggregatedCommunications) {
			if (isClazzChildOf(commu.source, highlightedNode)) {
				return true
			}
		}

		false
	}

	private def static isClazzChildOf(Clazz clazz, Draw3DNodeEntity entity) {
		if (entity instanceof Clazz) {
			return clazz.fullQualifiedName == entity.fullQualifiedName
		}

		isClazzChildOfHelper(clazz.parent, entity)
	}

	private def static boolean isClazzChildOfHelper(Component component, Draw3DNodeEntity entity) {
		if (component == null) {
			return false
		}

		if (component.fullQualifiedName == entity.fullQualifiedName) {
			return true
		}

		isClazzChildOfHelper(component.parentComponent, entity)
	}

	public def static boolean determineIncoming(CommunicationAppAccumulator commuApp) {
		for (commu : commuApp.aggregatedCommunications) {
			if (isClazzChildOf(commu.target, highlightedNode)) {
				return true
			}
		}

		false
	}

	def static isCurrentlyHighlighting() {
		highlightedNode != null
	}

}
