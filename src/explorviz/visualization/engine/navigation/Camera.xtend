package explorviz.visualization.engine.navigation

import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.interaction.Usertracking

class Camera {
     static var  Vector3f  cameraTranslate
     static var  Vector3f  cameraModelRotate
     static var  Vector3f  cameraRotate
    
     static val xPitch = 0.015f
     static val yPitch = 0.01f
     static val zPitch = 0.35f
     
     private new() {}
    
    def static void init(Vector3f cameraInit) {
		cameraTranslate = cameraInit
		resetModelRotate()
		resetRotate()
    }
    
    def static void resetTranslate() {
        cameraTranslate.x = 0
        cameraTranslate.y = 0
    }
    
    def static void resetModelRotate() {
        cameraModelRotate = new Vector3f(0)
    }
    
    def static void rotateModelX(float degree) {
        cameraModelRotate.x = cameraModelRotate.x + degree 
    }
    
    def static void rotateModelY(float degree) {
        cameraModelRotate.y = cameraModelRotate.y + degree 
    }
    
    def static void rotateModelZ(float degree) {
        cameraModelRotate.z = cameraModelRotate.z + degree
    }
    
    def static void resetRotate() {
        cameraRotate = new Vector3f(0)
    }
    
    def static void rotateX(float degree) {
        cameraRotate.x = cameraRotate.x + degree 
    }
    
    def static void rotateY(float degree) {
        cameraRotate.y = cameraRotate.y + degree 
    }
    
    def static void rotateZ(float degree) {
        cameraRotate.z = cameraRotate.z + degree
    }
    
    def static void rotateAbsoluteX(float degree) {
        cameraRotate.x = degree 
    }
    
    def static void rotateAbsoluteY(float degree) {
        cameraRotate.y = degree 
    }
    
    def static void rotateAbsoluteZ(float degree) {
        cameraRotate.z = degree
    }
    
    def static void moveX(float distanceXInPercent) {
    	cameraTranslate.x = cameraTranslate.x + distanceXInPercent * 6f * xPitch * (Math.abs(cameraTranslate.z) / 4f)
		Usertracking::trackCameraMovedX(cameraTranslate.x)
    }
    
    def static void moveY(float distanceYInPercent) {
    	cameraTranslate.y = cameraTranslate.y + distanceYInPercent * 4f * yPitch * (Math.abs(cameraTranslate.z) / 4f)
		Usertracking::trackCameraMovedY(cameraTranslate.y)
    }
    
    
    def static void moveUp() {
		cameraTranslate.y = cameraTranslate.y - yPitch * (Math.abs(cameraTranslate.z) / 4f)
		Usertracking::trackCameraMovedUp(cameraTranslate.y)
    }
    
    def static void moveDown() {
		cameraTranslate.y = cameraTranslate.y + yPitch * (Math.abs(cameraTranslate.z) / 4f)
		Usertracking::trackCameraMovedDown(cameraTranslate.y)
    }
    
    def static void moveLeft() {
		cameraTranslate.x = cameraTranslate.x + xPitch * (Math.abs(cameraTranslate.z) / 4f)
		Usertracking::trackCameraMovedLeft(cameraTranslate.x)
    }
    
    def static void moveRight() {
		cameraTranslate.x = cameraTranslate.x - xPitch * (Math.abs(cameraTranslate.z) / 4f)
		Usertracking::trackCameraMovedRight(cameraTranslate.x)
    }
    
    def static void zoomOut() {
		cameraTranslate.z = cameraTranslate.z - zPitch * (Math.abs(cameraTranslate.z) / 4f)
		Usertracking::trackCameraZoomedOut(cameraTranslate.z)
    }
    
    def static void zoomIn() {
		cameraTranslate.z = cameraTranslate.z + zPitch * (Math.abs(cameraTranslate.z) / 4f)
		Usertracking::trackCameraZoomedIn(cameraTranslate.z)
    }
    
    def static void focus(Vector3f point) {
    	cameraTranslate.x = point.x
    	cameraTranslate.y = point.y
    	cameraTranslate.z = point.z
    }
    
    def static Vector3f getVector() {
		cameraTranslate
    }
    
    def static Vector3f getCameraModelRotate() {
		cameraModelRotate
    }
    
    def static Vector3f getCameraRotate() {
		cameraRotate
    }
}
