package explorviz.visualization.engine.picking.observer

import java.util.List
import explorviz.visualization.engine.picking.handler.MouseClickHandler

interface MouseClickObserver {
	def void addMouseClickHandler(MouseClickHandler handler)
	def void removeMouseClickHandler(MouseClickHandler handler)
	
	def List<MouseClickHandler> getMouseClickHandlers()
}