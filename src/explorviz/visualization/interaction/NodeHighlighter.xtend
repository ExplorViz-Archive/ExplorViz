package explorviz.visualization.interaction

import explorviz.shared.model.helper.Draw3DNodeEntity
import explorviz.shared.model.Component
import explorviz.shared.model.Clazz

class NodeHighlighter {

	def static void highlight3DNode(Draw3DNodeEntity node) {
		val highlightedBefore = node.highlighted
		val app = if (node instanceof Component) node.belongingApplication else if (node instanceof Clazz) node.parent.belongingApplication 

		app.unhighlight()

		if (!highlightedBefore) {
			node.highlight()
		}
	}

}
