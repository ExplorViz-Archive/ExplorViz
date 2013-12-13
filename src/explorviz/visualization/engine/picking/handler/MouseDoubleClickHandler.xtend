package explorviz.visualization.engine.picking.handler

import explorviz.visualization.engine.picking.ClickEvent

interface MouseDoubleClickHandler {
	def void handleDoubleClick(ClickEvent event)
}