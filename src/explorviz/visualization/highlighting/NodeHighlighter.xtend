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
	static var Application app = null

	def static void highlight3DNode(Draw3DNodeEntity node) {
		val highlightedBefore = node.highlighted
		app = if (node instanceof Component)
			node.belongingApplication
		else if (node instanceof Clazz)
			node.parent.belongingApplication

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
		if (app != null) {
			app.unhighlight()

			SceneDrawer::createObjectsFromApplication(app, true)
		}
	}

	public def static void reset() {
		if (app != null)
			app.unhighlight()
		highlightedNode = null
	}

	public def static void resetApplication() {
		if (app != null)
			app.unhighlight()
		app = null
		highlightedNode = null
	}

	public def static void applyHighlighting(Application applicationParam) {
		if (highlightedNode != null) {
			applicationParam.communicationsAccumulated.forEach [
				if ((source != null && source.fullQualifiedName == highlightedNode.fullQualifiedName) ||
					(target != null && target.fullQualifiedName == highlightedNode.fullQualifiedName)) {

					val outgoing = determineOutgoing(it)
					val incoming = determineIncoming(it)

					if (incoming && outgoing) {
						it.state = EdgeState.SHOW_DIRECTION_IN_AND_OUT
					} else if (incoming) {
						it.state = EdgeState.SHOW_DIRECTION_IN
					} else if (outgoing) {
						it.state = EdgeState.SHOW_DIRECTION_OUT
					}
				} else {
					it.state = EdgeState.TRANSPARENT
				}
			]
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
