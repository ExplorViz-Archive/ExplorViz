package explorviz.visualization.engine.navigation

import com.google.gwt.event.dom.client.KeyDownEvent
import com.google.gwt.event.dom.client.KeyUpEvent
import com.google.gwt.event.dom.client.MouseMoveEvent
import com.google.gwt.event.dom.client.MouseOutEvent
import com.google.gwt.event.dom.client.MouseWheelEvent
import com.google.gwt.event.shared.HandlerRegistration
import com.google.gwt.user.client.ui.RootPanel
import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.engine.picking.ObjectPicker
import explorviz.visualization.engine.popover.PopoverService

import static extension explorviz.visualization.main.ArrayExtensions.*
import explorviz.visualization.engine.main.WebGLStart

class Navigation {
	private static val keyPressed = createBooleanArray(256)
	private static var mousePressed = false
	private static var initialized = false

	private static int oldMousePressedX = 0
	private static int oldMousePressedY = 0

	private static var HandlerRegistration keyDownHandler
	private static var HandlerRegistration keyUpHandler

	private static var HandlerRegistration mouseWheelHandler
	private static var HandlerRegistration mouseMoveHandler
	private static var HandlerRegistration mouseOutHandler

	private static val HOVER_DELAY_IN_MILLIS = 900
	private static var MouseHoverDelayTimer mouseHoverTimer
	

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
			cancelTimers

			keyDownHandler.removeHandler()
			keyUpHandler.removeHandler()

			mouseWheelHandler.removeHandler()
			MouseWheelFirefox::removeNativeMouseWheelListener
			mouseMoveHandler.removeHandler()
			mouseOutHandler.removeHandler()

			TouchNavigationJS::deregister()

			initialized = false
		}
	}

	public def static void keyDownHandler(KeyDownEvent event) {
		keyPressed.setElement(event.getNativeKeyCode(), true)
	}

	public def static void keyUpHandler(KeyUpEvent event) {
		keyPressed.setElement(event.getNativeKeyCode(), false)
	}

	public def static void mouseWheelHandler(int delta) {
		if (delta > 0) Camera::zoomOut() else if (delta < 0) Camera::zoomIn()
	}

	public def static void mouseDoubleClickHandler(int x, int y) {
		cancelTimers
		ObjectPicker::handleDoubleClick(x, y)
	}

	public def static void panningHandler(int x, int y, int clientWidth, int clientHeight) {
		val distanceX = x - oldMousePressedX
		val distanceY = y - oldMousePressedY

		// check if invalid jump in movement...
		if ((distanceX != 0 || distanceY != 0) && distanceX > -100 && distanceY > -100 && distanceX < 100 &&
			distanceY < 100) {
			val distanceXInPercent = (distanceX / clientWidth as float) * 100f
			val distanceYInPercent = (distanceY / clientHeight as float) * 100f

			Camera::moveX(distanceXInPercent)
			Camera::moveY(distanceYInPercent * -1)

			oldMousePressedX = x
			oldMousePressedY = y
		}
	}

	public def static void mouseMoveHandler(int x, int y, int height) {
		PopoverService::hidePopover()
		if (!mousePressed) {
			if (y < height - WebGLStart::timeshiftHeight) {
				setMouseHoverTimer(x, y)
			} else {
				cancelTimers
			}
		}
	}

	public def static void mouseDownHandler(int x, int y) {
		cancelTimers
		mousePressed = true
		oldMousePressedX = x
		oldMousePressedY = y
	}

	public def static void mouseUpHandler(int x, int y) {
		cancelTimers
		mousePressed = false
		oldMousePressedX = 0
		oldMousePressedY = 0
	}

	public def static void mouseSingleClickHandler(int x, int y) {
		ObjectPicker::handleClick(x, y)
	}

	def static void registerWebGLKeys() {
		if (!initialized) {
			mousePressed = false

			oldMousePressedX = 0
			oldMousePressedY = 0

			for (var int i = 0; i < 256; i++) {
				keyPressed.set(i, false)
			}

			mouseHoverTimer = new MouseHoverDelayTimer()

			val viewPanel = RootPanel::get("view")

			val documentPanel = RootPanel::get()
			keyDownHandler = documentPanel.addDomHandler(
				[
					Navigation.keyDownHandler(it)
				], KeyDownEvent::getType())

			keyUpHandler = documentPanel.addDomHandler(
				[
					Navigation.keyUpHandler(it)
				], KeyUpEvent::getType())

			mouseWheelHandler = viewPanel.addDomHandler(
				[
					Navigation.mouseWheelHandler(it.deltaY)
				], MouseWheelEvent::getType())

			MouseWheelFirefox::addNativeMouseWheelListener

			mouseMoveHandler = viewPanel.addDomHandler(
				[
					Navigation.mouseMoveHandler(x, y, relativeElement.clientHeight)
				], MouseMoveEvent::getType())
				
			mouseOutHandler = viewPanel.addDomHandler(
				[
					cancelTimers
				], MouseOutEvent::getType())

			TouchNavigationJS::register()

			initialized = true
		}
	}

	def static void cancelTimers() {
		PopoverService::hidePopover()
		mouseHoverTimer.myCanceled = true
		mouseHoverTimer.cancel
	}

	def static void setMouseHoverTimer(int x, int y) {
		mouseHoverTimer.myCanceled = true
		mouseHoverTimer.x = x
		mouseHoverTimer.y = y

		mouseHoverTimer.myCanceled = false
		mouseHoverTimer.schedule(HOVER_DELAY_IN_MILLIS)
	}
}
