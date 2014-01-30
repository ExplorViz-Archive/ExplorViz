package explorviz.visualization.engine.picking.observer

import explorviz.visualization.engine.picking.handler.MouseHoverHandler

interface MouseHoverObserver {
	def void setMouseHoverHandler(MouseHoverHandler handler)
	
	def MouseHoverHandler getMouseHoverHandler()
}