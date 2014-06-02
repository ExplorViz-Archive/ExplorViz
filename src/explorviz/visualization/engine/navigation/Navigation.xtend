package explorviz.visualization.engine.navigation

import com.google.gwt.dom.client.NativeEvent
import com.google.gwt.event.dom.client.DoubleClickEvent
import com.google.gwt.event.dom.client.KeyDownEvent
import com.google.gwt.event.dom.client.KeyUpEvent
import com.google.gwt.event.dom.client.MouseMoveEvent
import com.google.gwt.event.dom.client.MouseUpEvent
import com.google.gwt.event.dom.client.MouseWheelEvent
import com.google.gwt.event.shared.HandlerRegistration
import com.google.gwt.user.client.Event
import com.google.gwt.user.client.ui.RootPanel
import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.engine.picking.ObjectPicker

import static extension explorviz.visualization.main.ArrayExtensions.*
import com.google.gwt.event.dom.client.MouseDownEvent
import explorviz.visualization.engine.popover.PopoverService

class Navigation {
	static val keyPressed = createBooleanArray(256)
	static var mousePressed = false
	static var initialized = false

	static var oldMousePressedX = 0
	static var oldMousePressedY = 0

	static var HandlerRegistration keyDownHandler
	static var HandlerRegistration keyUpHandler

	static var HandlerRegistration mouseWheelHandler
	static var HandlerRegistration mouseDoubleClickHandler
	static var HandlerRegistration mouseMoveHandler
	static var HandlerRegistration mouseDownHandler
	static var HandlerRegistration mouseUpHandler

	static val HOVER_DELAY_IN_MILLIS = 100

	static var MouseHoverDelayTimer mouseHoverTimer

	private new() {
	}

	def static Vector3f getCameraPoint() {
		return Camera::getVector()
	}

	def static Vector3f getCameraRotate() {
		return Camera::getCameraRotate()
	}

	def static navigationCallback() {
		if (keyPressed.getElement(KeyConstants::W) || keyPressed.getElement(KeyConstants::KEY_UP)) {
			Camera::moveUp()
		}
		if (keyPressed.getElement(KeyConstants::S) || keyPressed.getElement(KeyConstants::KEY_DOWN)) {
			Camera::moveDown()
		}
		if (keyPressed.getElement(KeyConstants::A) || keyPressed.getElement(KeyConstants::KEY_LEFT)) {
			Camera::moveLeft()
		}
		if (keyPressed.getElement(KeyConstants::D) || keyPressed.getElement(KeyConstants::KEY_RIGHT)) {
			Camera::moveRight()
		}
		if (keyPressed.getElement(KeyConstants::PLUS)) {
			Camera::zoomIn
		}
		if (keyPressed.getElement(KeyConstants::MINUS)) {
			Camera::zoomOut
		}
	}

	def static deregisterWebGLKeys() {
		if (initialized) {
			keyDownHandler.removeHandler()
			keyUpHandler.removeHandler()

			mouseWheelHandler.removeHandler()
			MouseWheelFirefox::removeNativeMouseWheelListener
			mouseDoubleClickHandler.removeHandler()
			mouseMoveHandler.removeHandler()
			mouseDownHandler.removeHandler()
			mouseUpHandler.removeHandler()

			initialized = false
		}
	}

	def static registerWebGLKeys() {
		if (!initialized) {
			val documentPanel = RootPanel::get()
			documentPanel.sinkEvents(Event::ONKEYDOWN)
			documentPanel.sinkEvents(Event::ONKEYUP)

			val viewPanel = RootPanel::get("view")
			viewPanel.sinkEvents(Event::ONMOUSEWHEEL)
			viewPanel.sinkEvents(Event::ONDBLCLICK)
			viewPanel.sinkEvents(Event::ONMOUSEUP)
			viewPanel.sinkEvents(Event::ONMOUSEDOWN)
			viewPanel.sinkEvents(Event::ONMOUSEMOVE)

			keyDownHandler = documentPanel.addDomHandler(
				[
					keyPressed.setElement(it.getNativeKeyCode(), true)
				], KeyDownEvent::getType())

			keyUpHandler = documentPanel.addDomHandler(
				[
					keyPressed.setElement(it.getNativeKeyCode(), false)
				], KeyUpEvent::getType())

			mouseWheelHandler = viewPanel.addDomHandler(
				[
					//					it.stopPropagation()
					//					it.preventDefault()
					if (it.getDeltaY() > 0) Camera::zoomOut() else if (it.getDeltaY() < 0) Camera::zoomIn()
				], MouseWheelEvent::getType())

			MouseWheelFirefox::addNativeMouseWheelListener

			mouseDoubleClickHandler = viewPanel.addDomHandler(
				[
					//					it.stopPropagation()
					//					it.preventDefault()
					val width = it.relativeElement.clientWidth
					val heigth = it.relativeElement.clientHeight
					ObjectPicker::handleDoubleClick(it.x, it.y, width, heigth)
				], DoubleClickEvent::getType())

			mouseMoveHandler = viewPanel.addDomHandler(
				[
					//					it.stopPropagation()
					//					it.preventDefault()
					PopoverService::hidePopover()
					if (mousePressed) {
						val xMovement = it.x - oldMousePressedX
						val yMovement = it.y - oldMousePressedY

						Camera::moveX(xMovement)
						Camera::moveY(yMovement * -1)

						oldMousePressedX = it.x
						oldMousePressedY = it.y
					} else {
						if (mouseHoverTimer != null && mouseHoverTimer.running)
							cancelTimer()
						else
							createTimer(it.x, it.y, it.relativeElement.clientWidth, it.relativeElement.clientHeight)
					}
				], MouseMoveEvent::getType())

			mouseDownHandler = viewPanel.addDomHandler(
				[
					mousePressed = true
					oldMousePressedX = it.x
					oldMousePressedY = it.y
				], MouseDownEvent::getType())

			mouseUpHandler = viewPanel.addDomHandler(
				[
					//					it.stopPropagation()
					//					it.preventDefault()
					mousePressed = false
					val width = it.relativeElement.clientWidth
					val heigth = it.relativeElement.clientHeight
					if (it.nativeButton == NativeEvent::BUTTON_LEFT) {
						ObjectPicker::handleClick(it.x, it.y, width, heigth)
					} else if (it.nativeButton == NativeEvent::BUTTON_RIGHT) {
						ObjectPicker::handleRightClick(it.x, it.y, width, heigth)
					}
				], MouseUpEvent::getType())

			initialized = true
		}
	}

	def static createTimer(int x, int y, int width, int height) {
		mouseHoverTimer = new MouseHoverDelayTimer(x, y, width, height)
		mouseHoverTimer.schedule(HOVER_DELAY_IN_MILLIS)
	}

	def static cancelTimer() {
		if (mouseHoverTimer != null)
			mouseHoverTimer.cancel()
	}
}
