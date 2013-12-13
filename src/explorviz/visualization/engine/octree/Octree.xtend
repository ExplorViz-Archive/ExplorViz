package explorviz.visualization.engine.octree

import explorviz.visualization.engine.primitives.PrimitiveObject
import java.util.List
import explorviz.visualization.engine.math.Vector3f

import static extension explorviz.visualization.main.ArrayExtensions.*

class Octree {
	val Octnode      mainNode

	new(List<PrimitiveObject> polygons) {
		var minSize = if (polygons.size > 0) polygons.get(0).vertices.getElement(0) else 0
		var maxSize = minSize
		
		for (PrimitiveObject polygon : polygons) {
		    val vertices = polygon.getVertices()
		    for (vertice : vertices) {
				if (vertice > maxSize)maxSize = vertice 
				    else if (vertice < minSize) minSize = vertice
		    }
		}
		
		val position = (maxSize + minSize) / 2f
		val size = (maxSize - minSize) / 2f
		
		mainNode = new Octnode(new Vector3f(position), size)
		mainNode.divide(polygons)
    }

	def draw() {
		mainNode.checkForVisibilityAndDraw
	}
}