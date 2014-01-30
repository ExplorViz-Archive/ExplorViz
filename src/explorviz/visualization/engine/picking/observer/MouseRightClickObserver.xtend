package explorviz.visualization.engine.picking.observer

import explorviz.visualization.engine.picking.handler.MouseRightClickHandler

interface MouseRightClickObserver {
	def void setMouseRightClickHandler(MouseRightClickHandler handler)
	
	def MouseRightClickHandler getMouseRightClickHandler()
}