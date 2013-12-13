package explorviz.visualization.engine.picking.observer

import java.util.List
import explorviz.visualization.engine.picking.handler.MouseRightClickHandler

interface MouseRightClickObserver {
	def void addMouseRightClickHandler(MouseRightClickHandler handler)
	def void removeMouseRightClickHandler(MouseRightClickHandler handler)
	
	def List<MouseRightClickHandler> getMouseRightClickHandlers()
}