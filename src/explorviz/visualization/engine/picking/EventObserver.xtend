package explorviz.visualization.engine.picking

import explorviz.shared.model.helper.GenericModelElement
import explorviz.visualization.engine.picking.handler.MouseClickHandler
import explorviz.visualization.engine.picking.handler.MouseDoubleClickHandler
import explorviz.visualization.engine.picking.handler.MouseHoverHandler
import explorviz.visualization.engine.picking.handler.MouseRightClickHandler
import explorviz.visualization.engine.picking.observer.MouseClickObserver
import explorviz.visualization.engine.picking.observer.MouseDoubleClickObserver
import explorviz.visualization.engine.picking.observer.MouseHoverObserver
import explorviz.visualization.engine.picking.observer.MouseRightClickObserver
import com.google.gwt.user.client.rpc.IsSerializable
import org.eclipse.xtend.lib.annotations.Accessors

abstract class EventObserver extends GenericModelElement implements MouseClickObserver, MouseDoubleClickObserver, MouseHoverObserver, MouseRightClickObserver {
	transient var MouseClickHandler mouseClickHandler
	transient var MouseDoubleClickHandler mouseDoubleClickHandler
	transient var MouseHoverHandler mouseHoverHandler
	transient var MouseRightClickHandler mouseRightClickHandler
	
	@Accessors transient val primitiveObjects = new ArrayList<PrimitiveObject>
	
	def void destroy() {
		clearAllHandlers()

		primitiveObjects.clear()
	}

	def clearAllHandlers() {
		this.mouseClickHandler = null
		this.mouseDoubleClickHandler = null
		this.mouseHoverHandler = null
		this.mouseRightClickHandler = null
	}

	override setMouseClickHandler(MouseClickHandler handler) {
		this.mouseClickHandler = handler
		ObjectPicker::addObject(this, EventType::CLICK_EVENT)
	}

	override setMouseDoubleClickHandler(MouseDoubleClickHandler handler) {
		this.mouseDoubleClickHandler = handler
		ObjectPicker::addObject(this, EventType::DOUBLECLICK_EVENT)
	}

	override setMouseHoverHandler(MouseHoverHandler handler) {
		this.mouseHoverHandler = handler
		ObjectPicker::addObject(this, EventType::MOUSEMOVE_EVENT)
	}

	override setMouseRightClickHandler(MouseRightClickHandler handler) {
		this.mouseRightClickHandler = handler
		ObjectPicker::addObject(this, EventType::RIGHTCLICK_EVENT)
	}

	override MouseClickHandler getMouseClickHandler() {
		this.mouseClickHandler
	}

	override getMouseDoubleClickHandler() {
		this.mouseDoubleClickHandler
	}

	override getMouseHoverHandler() {
		this.mouseHoverHandler
	}

	override getMouseRightClickHandler() {
		this.mouseRightClickHandler
	}

}
