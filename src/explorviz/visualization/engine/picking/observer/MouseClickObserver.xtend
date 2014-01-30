package explorviz.visualization.engine.picking.observer

import explorviz.visualization.engine.picking.handler.MouseClickHandler

interface MouseClickObserver {
	def void setMouseClickHandler(MouseClickHandler handler)
	
	def MouseClickHandler getMouseClickHandler()
}