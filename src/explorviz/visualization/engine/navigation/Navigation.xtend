package explorviz.visualization.engine.navigation

import com.google.gwt.event.dom.client.KeyDownEvent
import com.google.gwt.event.dom.client.KeyUpEvent
import com.google.gwt.event.dom.client.MouseMoveEvent
import com.google.gwt.event.dom.client.MouseOutEvent
import com.google.gwt.event.dom.client.MouseWheelEvent
import com.google.gwt.event.shared.HandlerRegistration
import com.google.gwt.user.client.ui.RootPanel
import explorviz.visualization.engine.main.WebGLStart
import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.engine.picking.ObjectPicker
import explorviz.visualization.engine.popover.PopoverService

import static extension explorviz.visualization.main.ArrayExtensions.*
import com.google.gwt.event.dom.client.MouseUpEvent
import com.google.gwt.event.dom.client.MouseDownEvent
import explorviz.visualization.engine.main.SceneDrawer
import explorviz.visualization.engine.main.WebVRJS

class Navigation {
	private static val keyPressed = createBooleanArray(256)
	private static var mouseLeftPressed = false
	private static var mouseRightPressed = false
	private static var initialized = false

	public static int oldMousePressedX = 0
	public static int oldMousePressedY = 0

	public static int oldMouseMoveX = 0
	public static int oldMouseMoveY = 0

	private static var HandlerRegistration mouseWheelHandler
	private static var HandlerRegistration mouseMoveHandler
	private static var HandlerRegistration mouseOutHandler
	private static var HandlerRegistration mouseDownHandler
	private static var HandlerRegistration mouseUpHandler
	private static var HandlerRegistration keyDownHandler

	private static val HOVER_DELAY_IN_MILLIS = 550
	private static var MouseHoverDelayTimer mouseHoverTimer

	def static Vector3f getCameraPoint() {
		return Camera::getVector()
	}

	def static Vector3f getCameraRotate() {
		return Camera::getCameraRotate()
	}

	def static Vector3f getCameraModelRotate() {
		return Camera::getCameraModelRotate()
	}

	def static void deregisterWebGLKeys() {
		if (initialized) {
			cancelTimers

			mouseWheelHandler.removeHandler()
			MouseWheelFirefox::removeNativeMouseWheelListener
			mouseMoveHandler.removeHandler()
			mouseOutHandler.removeHandler()
			mouseDownHandler.removeHandler()
			mouseUpHandler.removeHandler()
			keyDownHandler.removeHandler()

			TouchNavigationJS::deregister()

			initialized = false
		}
	}

	public def static void keyDownHandler(KeyDownEvent event) {
		keyPressed.setElement(event.getNativeKeyCode(), true)
		if(keyPressed.getElement(KeyConstants::KEY_UP)) {
			
			if(SceneDrawer::showVRObjects) {
				WebVRJS::resetSensor()
				SceneDrawer::createObjectsFromApplication(SceneDrawer::lastViewedApplication, false)				
			}
			
			SceneDrawer::showVRObjects = true		
				
		}
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

	public def static void mouseMoveHandler(int x, int y, int clientWidth, int clientHeight) {
		if (!mouseLeftPressed) {
			val distanceX = x - oldMouseMoveX
			val distanceY = y - oldMouseMoveY

			// check if invalid jump in movement...
			if ((distanceX != 0 || distanceY != 0) && distanceX > -100 && distanceY > -100 && distanceX < 100 &&
				distanceY < 100 && y < clientHeight - WebGLStart::timeshiftHeight) {
				if (mouseRightPressed && SceneDrawer::lastViewedApplication != null) {
					val distanceXInPercent = (distanceX / clientWidth as float) * 100f
					val distanceYInPercent = (distanceY / clientHeight as float) * 100f

					Camera::rotateModelX(distanceYInPercent * 2.5f)
					Camera::rotateModelY(distanceXInPercent * 4f)
				} else {
					setMouseHoverTimer(x, y)
				}
			} else {
				cancelTimers
			}

			oldMouseMoveX = x
			oldMouseMoveY = y

		}
		PopoverService::hidePopover()
	}

	public def static void mouseMoveVRHandler(int x, int y, boolean mouseLeftPressed, boolean mouseRightPressed) {
		// This handler is needed, because the pointer lock causes 
		// hammer.js to not detect pan actions

		val width = com.google.gwt.user.client.Window.getClientWidth()
		val height = com.google.gwt.user.client.Window.getClientHeight()

		val distanceXMoved = x - oldMouseMoveX
		val distanceYMoved = y - oldMouseMoveY

		if ((distanceXMoved != 0 || distanceYMoved != 0) && distanceXMoved > -100 && distanceYMoved > -100 &&
			distanceXMoved < 100 && distanceYMoved < 100) {
			if (mouseRightPressed && SceneDrawer::lastViewedApplication != null) {
				val distanceXInPercent = (distanceXMoved / width as float) * 100f
				val distanceYInPercent = (distanceYMoved / height as float) * 100f

				Camera::rotateModelX(distanceYInPercent * 2.5f)
				Camera::rotateModelY(distanceXInPercent * 4f)
			}
		}		

		oldMouseMoveX = x
		oldMouseMoveY = y
		
		val distanceXPressed = x - oldMousePressedX
		val distanceYPressed = y - oldMousePressedY

		if ((distanceXPressed != 0 || distanceYPressed != 0) && distanceXPressed > -100 && distanceYPressed > -100 &&
			distanceXPressed < 100 && distanceYPressed < 100) {

			if (mouseLeftPressed) {					
				
				val distanceXInPercent = (distanceXPressed / width as float) * 100f
				val distanceYInPercent = (distanceYPressed / height as float) * 100f

				Camera::moveX(distanceXInPercent)
				Camera::moveY(distanceYInPercent * -1)
			}
			
		}
		oldMousePressedX = x
		oldMousePressedY = y
	}

	public def static void mouseDownHandler(int x, int y) {
		cancelTimers
		mouseLeftPressed = true
		mouseRightPressed = false
		oldMousePressedX = x
		oldMousePressedY = y
	}

	public def static void mouseUpHandler(int x, int y) {
		cancelTimers
		mouseLeftPressed = false
		mouseRightPressed = false
		oldMousePressedX = 0
		oldMousePressedY = 0
		oldMouseMoveX = 0
		oldMouseMoveY = 0
	}

	public def static void mouseSingleClickHandler(int x, int y) {		
		ObjectPicker::handleClick(x, y)
	}

	def static void mouseRightClick(int x, int y) {
		ObjectPicker::handleRightClick(x, y)
	}

	def static void registerWebGLKeys() {
		
		if (!initialized) {
			mouseLeftPressed = false
			mouseRightPressed = false

			oldMousePressedX = 0
			oldMousePressedY = 0

			mouseHoverTimer = new MouseHoverDelayTimer()

			val viewPanel = RootPanel::get("view")
			val documentPanel = RootPanel::get()

			mouseWheelHandler = viewPanel.addDomHandler(
				[
					Navigation.mouseWheelHandler(it.deltaY)
				], MouseWheelEvent::getType())

			MouseWheelFirefox::addNativeMouseWheelListener

			mouseMoveHandler = viewPanel.addDomHandler(
				[
					if (!WebGLStart::webVRMode) {
						Navigation.mouseMoveHandler(x, y, relativeElement.clientWidth, relativeElement.clientHeight)
					}
				], MouseMoveEvent::getType())

			mouseOutHandler = viewPanel.addDomHandler(
				[
					cancelTimers
				], MouseOutEvent::getType())

			mouseDownHandler = viewPanel.addDomHandler(
				[
					if (it.nativeButton == com.google.gwt.dom.client.NativeEvent.BUTTON_RIGHT && !WebGLStart::webVRMode) {
						mouseRightPressed = true
						oldMouseMoveX = it.x
						oldMouseMoveY = it.y
					}
				], MouseDownEvent::getType())

			mouseUpHandler = viewPanel.addDomHandler(
				[
					if (it.nativeButton == com.google.gwt.dom.client.NativeEvent.BUTTON_RIGHT && !WebGLStart::webVRMode) {
						mouseRightPressed = false
						oldMouseMoveX = 0
						oldMouseMoveY = 0
					}
				], MouseUpEvent::getType())

			keyDownHandler = documentPanel.addDomHandler(
				[
					if (WebGLStart::webVRMode) {					
						Navigation.keyDownHandler(it)
					}
				], KeyDownEvent::getType())

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
