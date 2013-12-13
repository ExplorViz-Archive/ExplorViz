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

class Navigation {
    static val keyPressed = createBooleanArray(256)
    static var initialized = false
    
    static var HandlerRegistration keyDownHandler
    static var HandlerRegistration keyUpHandler
    
    static var HandlerRegistration mouseWheelHandler
    static var HandlerRegistration mouseDoubleClickHandler
    static var HandlerRegistration mouseMoveHandler
    static var HandlerRegistration mouseUpHandler
    
    private new() {}
    
    def static Vector3f getCameraPoint() {
        return Camera::getVector()
    }
    
    def static Vector3f getCameraRotate() {
        return Camera::getCameraRotate()
    }
    
    def static navigationCallback() {
        if (keyPressed.getElement(KeyConstants::W) || 
                keyPressed.getElement(KeyConstants::KEY_UP)) {
            Camera::moveUp()
        }
        if (keyPressed.getElement(KeyConstants::S) || 
                keyPressed.getElement(KeyConstants::KEY_DOWN)) {
            Camera::moveDown()
        }
        if (keyPressed.getElement(KeyConstants::A) || 
                keyPressed.getElement(KeyConstants::KEY_LEFT)) {
            Camera::moveLeft()
        }
        if (keyPressed.getElement(KeyConstants::D) || 
                keyPressed.getElement(KeyConstants::KEY_RIGHT)) {
            Camera::moveRight()
        }
    }
    
    def static deregisterWebGLKeys() {
        if (initialized) {
            keyDownHandler.removeHandler()
            keyUpHandler.removeHandler()
            
            mouseWheelHandler.removeHandler()
            mouseDoubleClickHandler.removeHandler()
            mouseMoveHandler.removeHandler()
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
            viewPanel.sinkEvents(Event::ONMOUSEMOVE)
            
            keyDownHandler = documentPanel.addDomHandler([
                    keyPressed.setElement(it.getNativeKeyCode(), true)
            ], KeyDownEvent::getType())
            
            keyUpHandler = documentPanel.addDomHandler([ 
                keyPressed.setElement(it.getNativeKeyCode(), false)
            ], KeyUpEvent::getType())
            
            mouseWheelHandler = viewPanel.addDomHandler( [
                it.stopPropagation()
                it.preventDefault()
                
                if (it.getDeltaY() > 0) Camera::zoomOut() else
                if (it.getDeltaY() < 0) Camera::zoomIn()
            ], MouseWheelEvent::getType())
            
            mouseDoubleClickHandler = viewPanel.addDomHandler( [
                it.stopPropagation()
                it.preventDefault()
                
                val width = it.relativeElement.clientWidth
                val heigth = it.relativeElement.clientHeight
                
                ObjectPicker::handleDoubleClick(it.x, it.y, width, heigth)
            ], DoubleClickEvent::getType())
            
            mouseMoveHandler = viewPanel.addDomHandler( [
                it.stopPropagation()
                it.preventDefault()
                
                val width = it.relativeElement.clientWidth
                val heigth = it.relativeElement.clientHeight
                
                ObjectPicker::handleMouseMove(it.x, it.y, width, heigth)
            ], MouseMoveEvent::getType())
            
            mouseUpHandler = viewPanel.addDomHandler( [
                it.stopPropagation()
                it.preventDefault()
                
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
}
