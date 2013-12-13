package explorviz.visualization.engine.picking.handler

import explorviz.visualization.engine.picking.ClickEvent

interface MouseHoverHandler {
	def void handleHover(ClickEvent event)
}