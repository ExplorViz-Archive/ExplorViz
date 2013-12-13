package explorviz.visualization.engine.picking.handler

import explorviz.visualization.engine.picking.ClickEvent

interface MouseRightClickHandler {
	def void handleRightClick(ClickEvent event)
}