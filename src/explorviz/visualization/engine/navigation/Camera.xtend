package explorviz.visualization.engine.navigation

import explorviz.visualization.engine.math.Vector3f

class Camera {
     static var  Vector3f  cameraTranslate
     static var  Vector3f  cameraRotate
    
     static val xPitch = 0.015f
     static val yPitch = 0.01f
     static val zPitch = 0.35f
     
     private new() {}
    
    def static void init(Vector3f cameraInit) {
		cameraTranslate = cameraInit
		resetRotate()
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
    
    def static void moveUp() {
		cameraTranslate.y = cameraTranslate.y - yPitch * (Math.abs(cameraTranslate.z) / 4f)
    }
    
    def static void moveDown() {
		cameraTranslate.y = cameraTranslate.y + yPitch * (Math.abs(cameraTranslate.z) / 4f)
    }
    
    def static void moveRight() {
		cameraTranslate.x = cameraTranslate.x - xPitch * (Math.abs(cameraTranslate.z) / 4f)
    }
    
    def static void moveLeft() {
		cameraTranslate.x = cameraTranslate.x + xPitch * (Math.abs(cameraTranslate.z) / 4f)
    }
    
    def static void zoomOut() {
		cameraTranslate.z = cameraTranslate.z - zPitch * (Math.abs(cameraTranslate.z) / 4f)
    }
    
    def static void zoomIn() {
		cameraTranslate.z = cameraTranslate.z + zPitch * (Math.abs(cameraTranslate.z) / 4f)
    }
    
    def static Vector3f getVector() {
		cameraTranslate
    }
    
    def static Vector3f getCameraRotate() {
		cameraRotate
    }
}
