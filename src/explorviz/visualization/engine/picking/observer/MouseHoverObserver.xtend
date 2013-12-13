package explorviz.visualization.engine.picking.observer

import java.util.List
import explorviz.visualization.engine.picking.handler.MouseHoverHandler

interface MouseHoverObserver {
	def void addMouseHoverHandler(MouseHoverHandler handler)
	def void removeMouseHoverHandler(MouseHoverHandler handler)
	
	def List<MouseHoverHandler> getMouseHoverHandlers()
}