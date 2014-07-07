package explorviz.visualization.engine.navigation

import com.google.gwt.dom.client.NativeEvent
import com.google.gwt.event.dom.client.DoubleClickEvent
import com.google.gwt.event.dom.client.KeyDownEvent
import com.google.gwt.event.dom.client.KeyUpEvent
import com.google.gwt.event.dom.client.MouseDownEvent
import com.google.gwt.event.dom.client.MouseMoveEvent
import com.google.gwt.event.dom.client.MouseUpEvent
import com.google.gwt.event.dom.client.MouseWheelEvent
import com.google.gwt.event.shared.HandlerRegistration
import com.google.gwt.user.client.Event
import com.google.gwt.user.client.ui.RootPanel
import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.engine.picking.ObjectPicker
import explorviz.visualization.engine.popover.PopoverService

import static extension explorviz.visualization.main.ArrayExtensions.*

class Navigation {
	static val keyPressed = createBooleanArray(256)
	static var mousePressed = false
	static var mouseWasMoved = false
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

	static val HOVER_DELAY_IN_MILLIS = 900
	static val SINGLE_CLICK_DELAY_IN_MILLIS = 300

	static var MouseHoverDelayTimer mouseHoverTimer
	static var SingleClickDelayer singleClickTimer

	public static int clicks = 0

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

			mouseHoverTimer = new MouseHoverDelayTimer()
			singleClickTimer = new SingleClickDelayer()

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
					cancelTimers
					val width = it.relativeElement.clientWidth
					val heigth = it.relativeElement.clientHeight
					ObjectPicker::handleDoubleClick(it.x, it.y, width, heigth)
				], DoubleClickEvent::getType())

			mouseMoveHandler = viewPanel.addDomHandler(
				[
					//					it.stopPropagation()
					//					it.preventDefault()
					PopoverService::hidePopover()
					clicks = 0
					mouseWasMoved = true
					if (mousePressed) {
						val xMovement = it.x - oldMousePressedX
						val yMovement = it.y - oldMousePressedY

						Camera::moveX(xMovement)
						Camera::moveY(yMovement * -1)

						oldMousePressedX = it.x
						oldMousePressedY = it.y
					} else {
						setMouseHoverTimer(it.x, it.y, it.relativeElement.clientWidth, it.relativeElement.clientHeight)
					}
				], MouseMoveEvent::getType())

			mouseDownHandler = viewPanel.addDomHandler(
				[
					mousePressed = true
					mouseWasMoved = false
					oldMousePressedX = it.x
					oldMousePressedY = it.y
				], MouseDownEvent::getType())

			mouseUpHandler = viewPanel.addDomHandler(
				[
					mousePressed = false
					if (mouseWasMoved) {
						mouseWasMoved = false
						return
					}
					
					if (it.nativeButton == NativeEvent::BUTTON_RIGHT) {
						cancelTimers
						ObjectPicker::handleRightClick(x, y, relativeElement.clientWidth, relativeElement.clientHeight)
					} else {
						if (clicks == 0) {
							clicks = 1
							singleClickTimer.x = it.x
							singleClickTimer.y = it.y
							singleClickTimer.width = it.relativeElement.clientWidth
							singleClickTimer.height = it.relativeElement.clientHeight
							singleClickTimer.myCanceled = false

							singleClickTimer.schedule(SINGLE_CLICK_DELAY_IN_MILLIS)
						} else if (clicks > 0) {

							// double clicked
							cancelTimers
						}
					}
				], MouseUpEvent::getType())

			initialized = true
		}
	}

	def static cancelTimers() {
		singleClickTimer.myCanceled = true
		singleClickTimer.cancel
		clicks = 0
		mouseHoverTimer.myCanceled = true
		mouseHoverTimer.cancel
	}

	def static setMouseHoverTimer(int x, int y, int width, int height) {
		mouseHoverTimer.myCanceled = true
		mouseHoverTimer.x = x
		mouseHoverTimer.y = y
		mouseHoverTimer.width = width
		mouseHoverTimer.height = height

		mouseHoverTimer.myCanceled = false
		mouseHoverTimer.schedule(HOVER_DELAY_IN_MILLIS)
	}
}
