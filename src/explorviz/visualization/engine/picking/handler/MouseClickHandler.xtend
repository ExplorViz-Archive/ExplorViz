package explorviz.visualization.engine.picking.handler

import explorviz.visualization.engine.picking.ClickEvent

interface MouseClickHandler {
	def void handleClick(ClickEvent event)
}