package explorviz.visualization.engine.picking.observer

import java.util.List
import explorviz.visualization.engine.picking.handler.MouseDoubleClickHandler

interface MouseDoubleClickObserver {
	def void addMouseDoubleClickHandler(MouseDoubleClickHandler handler)
	def void removeMouseDoubleClickHandler(MouseDoubleClickHandler handler)
	
	def List<MouseDoubleClickHandler> getMouseDoubleClickHandlers()
}