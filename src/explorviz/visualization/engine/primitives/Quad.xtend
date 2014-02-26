package explorviz.visualization.engine.primitives

import java.util.ArrayList
import explorviz.visualization.engine.math.Vector3f
import elemental.html.WebGLTexture
import explorviz.visualization.engine.math.Vector4f

class Quad extends PrimitiveObject {
    @Property val triangles = new ArrayList<Triangle>
    @Property val cornerPoints = new ArrayList<Vector3f>
    @Property val color = new Vector4f(0f,0f,0f,1f)
    @Property var WebGLTexture texture
    
    new(Vector3f center, Vector3f extensionInEachDirection, WebGLTexture texture, Vector4f color) {
        val BOTTOM_LEFT = new Vector3f(center.x - extensionInEachDirection.x, center.y - extensionInEachDirection.y,
            center.z - extensionInEachDirection.z)
        val BOTTOM_RIGHT = new Vector3f(center.x + extensionInEachDirection.x, center.y - extensionInEachDirection.y,
            center.z + extensionInEachDirection.z)
        val TOP_RIGHT = new Vector3f(center.x + extensionInEachDirection.x, center.y + extensionInEachDirection.y,
            center.z + extensionInEachDirection.z)
        val TOP_LEFT = new Vector3f(center.x - extensionInEachDirection.x, center.y + extensionInEachDirection.y,
            center.z - extensionInEachDirection.z)

        createFrom4Vector3f(BOTTOM_LEFT, BOTTOM_RIGHT, TOP_RIGHT, TOP_LEFT, texture, color, false)
    }
    
    new(Vector3f center, Vector3f extensionInEachDirection, WebGLTexture texture, Vector4f color, boolean transparent) {
        val BOTTOM_LEFT = new Vector3f(center.x - extensionInEachDirection.x, center.y - extensionInEachDirection.y,
            center.z - extensionInEachDirection.z)
        val BOTTOM_RIGHT = new Vector3f(center.x + extensionInEachDirection.x, center.y - extensionInEachDirection.y,
            center.z + extensionInEachDirection.z)
        val TOP_RIGHT = new Vector3f(center.x + extensionInEachDirection.x, center.y + extensionInEachDirection.y,
            center.z + extensionInEachDirection.z)
        val TOP_LEFT = new Vector3f(center.x - extensionInEachDirection.x, center.y + extensionInEachDirection.y,
            center.z - extensionInEachDirection.z)

        createFrom4Vector3f(BOTTOM_LEFT, BOTTOM_RIGHT, TOP_RIGHT, TOP_LEFT, texture, color, transparent)
    }
    
    new(Vector3f BOTTOM_LEFT, Vector3f BOTTOM_RIGHT, Vector3f TOP_RIGHT, Vector3f TOP_LEFT, Vector4f color) {
        createFrom4Vector3f(BOTTOM_LEFT, BOTTOM_RIGHT, TOP_RIGHT, TOP_LEFT, null, color, false)
    }
    
    new(Vector3f BOTTOM_LEFT, Vector3f BOTTOM_RIGHT, Vector3f TOP_RIGHT, Vector3f TOP_LEFT, Vector4f color, boolean transparent) {
        createFrom4Vector3f(BOTTOM_LEFT, BOTTOM_RIGHT, TOP_RIGHT, TOP_LEFT, null, color, transparent)
    }

    new(Vector3f BOTTOM_LEFT, Vector3f BOTTOM_RIGHT, Vector3f TOP_RIGHT, Vector3f TOP_LEFT, WebGLTexture texture) {
        createFrom4Vector3f(BOTTOM_LEFT, BOTTOM_RIGHT, TOP_RIGHT, TOP_LEFT, texture, null, false)
    }
    
    new(Vector3f BOTTOM_LEFT, Vector3f BOTTOM_RIGHT, Vector3f TOP_RIGHT, Vector3f TOP_LEFT, WebGLTexture texture, boolean transparent) {
        createFrom4Vector3f(BOTTOM_LEFT, BOTTOM_RIGHT, TOP_RIGHT, TOP_LEFT, texture, null, transparent)
    }
    
    new(Vector3f BOTTOM_LEFT, Vector3f BOTTOM_RIGHT, Vector3f TOP_RIGHT, Vector3f TOP_LEFT, WebGLTexture texture, Vector4f color) {
        createFrom4Vector3f(BOTTOM_LEFT, BOTTOM_RIGHT, TOP_RIGHT, TOP_LEFT, texture, color, false)
    }
    
        new(Vector3f BOTTOM_LEFT, Vector3f BOTTOM_RIGHT, Vector3f TOP_RIGHT, Vector3f TOP_LEFT, WebGLTexture texture, Vector4f color, boolean transparent) {
        createFrom4Vector3f(BOTTOM_LEFT, BOTTOM_RIGHT, TOP_RIGHT, TOP_LEFT, texture, color, transparent)
    }

    def private createFrom4Vector3f(Vector3f BOTTOM_LEFT, Vector3f BOTTOM_RIGHT, Vector3f TOP_RIGHT, Vector3f TOP_LEFT,
        WebGLTexture texture, Vector4f color, boolean transparent) {
        this.texture = texture
        cornerPoints.add(BOTTOM_LEFT)
        cornerPoints.add(BOTTOM_RIGHT)
        cornerPoints.add(TOP_RIGHT)
        cornerPoints.add(TOP_LEFT)
            
        val triangleOne = new Triangle()
        triangleOne.begin
            triangleOne.texture = texture
            triangleOne.color = color
            triangleOne.transparent = transparent
    
            triangleOne.addPoint(BOTTOM_LEFT)
            triangleOne.addTexturePoint(0f, 1f)
    
            triangleOne.addPoint(BOTTOM_RIGHT)
            triangleOne.addTexturePoint(1f, 1f)
    
            triangleOne.addPoint(TOP_RIGHT)
            triangleOne.addTexturePoint(1f, 0f)
        triangleOne.end

        triangles.add(triangleOne)

        val triangleTwo = new Triangle()

        triangleTwo.begin
            triangleTwo.texture = texture
            triangleTwo.color = color
            triangleTwo.transparent = transparent
    
            triangleTwo.addPoint(TOP_RIGHT)
            triangleTwo.addTexturePoint(1f, 0f)
    
            triangleTwo.addPoint(TOP_LEFT)
            triangleTwo.addTexturePoint(0f, 0f)
    
            triangleTwo.addPoint(BOTTOM_LEFT)
            triangleTwo.addTexturePoint(0f, 1f)
        triangleTwo.end

        triangles.add(triangleTwo)
    }

    override draw() {
        triangles.forEach([it.draw])
    }

    override getVertices() {
        triangles.get(0).vertices
    }

	override highlight(Vector4f color) {
		triangles.forEach [it.highlight(color)]
	}
    
    override unhighlight() {
        triangles.forEach [it.unhighlight()]
    }
    
    override moveByVector(Vector3f vector) {
        triangles.get(0).moveByVector(vector)
        triangles.get(1).moveByVector(vector)
    }
    
    override reAddToBuffer() {
        triangles.get(0).reAddToBuffer()
        triangles.get(1).reAddToBuffer()
    }
    
}
