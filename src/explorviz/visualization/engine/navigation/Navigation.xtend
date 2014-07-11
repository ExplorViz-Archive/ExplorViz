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
import com.google.gwt.user.client.ui.RootPanel
import explorviz.visualization.engine.main.WebGLStart
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

	def static void registerWebGLKeys() {
		if (!initialized) {
			mousePressed = false
			mouseWasMoved = false
			initialized = false

			oldMousePressedX = 0
			oldMousePressedY = 0

			clicks = 0

			for (var int i = 0; i < 256; i++) {
				keyPressed.set(i, false)
			}

			mouseHoverTimer = new MouseHoverDelayTimer()
			singleClickTimer = new SingleClickDelayer()

			val viewPanel = RootPanel::get("view")

			val documentPanel = RootPanel::get()
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
					if (it.getDeltaY() > 0) Camera::zoomOut() else if (it.getDeltaY() < 0) Camera::zoomIn()
				], MouseWheelEvent::getType())

			MouseWheelFirefox::addNativeMouseWheelListener

			mouseDoubleClickHandler = viewPanel.addDomHandler(
				[
					cancelTimers
					if (it.y < it.relativeElement.clientHeight - WebGLStart::timeshiftHeight) {
						val width = it.relativeElement.clientWidth
						val heigth = it.relativeElement.clientHeight
						ObjectPicker::handleDoubleClick(it.x, it.y, width, heigth)
					}
				], DoubleClickEvent::getType())

			mouseMoveHandler = viewPanel.addDomHandler(
				[
					PopoverService::hidePopover()
					if (it.y < it.relativeElement.clientHeight - WebGLStart::timeshiftHeight) {
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
							setMouseHoverTimer(it.x, it.y, it.relativeElement.clientWidth,
								it.relativeElement.clientHeight)
						}
					}
				], MouseMoveEvent::getType())

			mouseDownHandler = viewPanel.addDomHandler(
				[
					mouseWasMoved = false
					if (it.y < it.relativeElement.clientHeight - WebGLStart::timeshiftHeight) {
						mousePressed = true
						oldMousePressedX = it.x
						oldMousePressedY = it.y
					}
				], MouseDownEvent::getType())

			mouseUpHandler = viewPanel.addDomHandler(
				[
					if (it.y < it.relativeElement.clientHeight - WebGLStart::timeshiftHeight) {
						mousePressed = false
						if (mouseWasMoved) {
							mouseWasMoved = false
							cancelTimers
							return
						}
						if (it.nativeButton == NativeEvent::BUTTON_RIGHT) {
							cancelTimers
							ObjectPicker::handleRightClick(x, y, relativeElement.clientWidth,
								relativeElement.clientHeight)
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
