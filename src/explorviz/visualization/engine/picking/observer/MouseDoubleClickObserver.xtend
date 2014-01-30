package explorviz.visualization.engine.picking.observer

import explorviz.visualization.engine.picking.handler.MouseDoubleClickHandler

interface MouseDoubleClickObserver {
	def void setMouseDoubleClickHandler(MouseDoubleClickHandler handler)
	
	def MouseDoubleClickHandler getMouseDoubleClickHandler()
}