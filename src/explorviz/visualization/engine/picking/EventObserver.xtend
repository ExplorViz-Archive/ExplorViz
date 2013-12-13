package explorviz.visualization.engine.picking

import java.util.ArrayList
import explorviz.visualization.engine.picking.ObjectPicker
import explorviz.visualization.engine.picking.observer.MouseClickObserver
import explorviz.visualization.engine.picking.observer.MouseDoubleClickObserver

import explorviz.visualization.engine.picking.handler.MouseClickHandler
import explorviz.visualization.engine.picking.handler.MouseDoubleClickHandler
import explorviz.visualization.engine.picking.EventType
import explorviz.visualization.engine.picking.observer.MouseHoverObserver
import explorviz.visualization.engine.picking.handler.MouseHoverHandler
import explorviz.visualization.engine.primitives.PrimitiveObject
import explorviz.visualization.engine.picking.handler.MouseRightClickHandler
import explorviz.visualization.engine.picking.observer.MouseRightClickObserver

class EventObserver implements MouseClickObserver, MouseDoubleClickObserver, MouseHoverObserver, MouseRightClickObserver {
	@Property val mouseClickHandlers = new ArrayList<MouseClickHandler>
	@Property val mouseDoubleClickHandlers = new ArrayList<MouseDoubleClickHandler>
	@Property val mouseHoverHandlers = new ArrayList<MouseHoverHandler>
	@Property val mouseRightClickHandlers = new ArrayList<MouseRightClickHandler>
	
	@Property val primitiveObjects = new ArrayList<PrimitiveObject>
	
	def void destroy() {
	    clearAllHandlers()
	    
	    primitiveObjects.clear()
	}

	def clearAllHandlers() {
		ObjectPicker::removeObject(this, EventType::CLICK_EVENT)
	    ObjectPicker::removeObject(this, EventType::DOUBLECLICK_EVENT)
	    ObjectPicker::removeObject(this, EventType::MOUSEMOVE_EVENT)
	    ObjectPicker::removeObject(this, EventType::RIGHTCLICK_EVENT)
	    
	    mouseClickHandlers.clear()
	    mouseDoubleClickHandlers.clear()
	    mouseHoverHandlers.clear()
	    mouseRightClickHandlers.clear()
	}
	
	override addMouseClickHandler(MouseClickHandler handler) {
		mouseClickHandlers.add(handler)
		ObjectPicker::addObject(this, EventType::CLICK_EVENT)
	}
	
	override removeMouseClickHandler(MouseClickHandler handler) {
		ObjectPicker::removeObject(this, EventType::CLICK_EVENT)
		mouseClickHandlers.remove(handler)
	}
	
	override addMouseDoubleClickHandler(MouseDoubleClickHandler handler) {
		mouseDoubleClickHandlers.add(handler)
		ObjectPicker::addObject(this, EventType::DOUBLECLICK_EVENT)
	}
	
	override removeMouseDoubleClickHandler(MouseDoubleClickHandler handler) {
		ObjectPicker::removeObject(this, EventType::DOUBLECLICK_EVENT)
		mouseDoubleClickHandlers.remove(handler)
	}
	
	override addMouseHoverHandler(MouseHoverHandler handler) {
		mouseHoverHandlers.add(handler)
		ObjectPicker::addObject(this, EventType::MOUSEMOVE_EVENT)
	}
	
	override removeMouseHoverHandler(MouseHoverHandler handler) {
		ObjectPicker::removeObject(this, EventType::MOUSEMOVE_EVENT)
		mouseHoverHandlers.remove(handler)
	}
	
    override addMouseRightClickHandler(MouseRightClickHandler handler) {
        mouseRightClickHandlers.add(handler)
        ObjectPicker::addObject(this, EventType::RIGHTCLICK_EVENT)
    }
    
    override removeMouseRightClickHandler(MouseRightClickHandler handler) {
        ObjectPicker::removeObject(this, EventType::RIGHTCLICK_EVENT)
        mouseRightClickHandlers.remove(handler)
    }
}