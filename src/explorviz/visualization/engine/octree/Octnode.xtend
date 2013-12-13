package explorviz.visualization.engine.octree

import java.util.ArrayList
import java.util.List
import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.engine.primitives.PrimitiveObject

import static extension explorviz.visualization.main.ArrayExtensions.*

public class Octnode {
	static val MAX_POLYGONS_IN_NODE = 500
	
	val Vector3f	 posOfCenter
	val float		 size
	
	val polys    = new ArrayList<PrimitiveObject>()
	val children = new ArrayList<Octnode>()
	
	var	boolean	     smallest = false
	
	new (Vector3f posOfCenter, float size) {
		this.posOfCenter = posOfCenter
		this.size = size
    }

	def int getHowManyPolygons(List<PrimitiveObject> polygons) {
		polygons.forEach([if (it.isPolygonWithIn) polys.add(it)])
			
		if (polys.size > 0) {
			polygons.removeAll(polys)
			if (polys.size <= MAX_POLYGONS_IN_NODE) {
				smallest = true
			}
		}
		
		return polys.size
	}

	def private isPolygonWithIn(PrimitiveObject polygon) {
		val vertices = polygon.getVertices()
		
		for (int i: 0..((vertices.length / 3) - 1)){
		    if ((vertices.getElement(i * 3) >= (posOfCenter.x - size))
			    && (vertices.getElement((i * 3) + 1) >= (posOfCenter.y - size))
			    && (vertices.getElement((i * 3) + 2) >= (posOfCenter.z - size))
			    && (vertices.getElement(i * 3) <= (posOfCenter.x + size))
			    && (vertices.getElement((i * 3) + 1) <= (posOfCenter.y + size))
			    && (vertices.getElement((i * 3) + 2) <= (posOfCenter.z + size))) {
			return true
	    	}
		}
		
		return false
	}

	def void divide(List<PrimitiveObject> polygons) {
		val halfSize = size / 2f
		
		children.add(new Octnode(new Vector3f(posOfCenter.x - halfSize, posOfCenter.y + halfSize,
		posOfCenter.z - halfSize), halfSize))
		children.add(new Octnode(new Vector3f(posOfCenter.x + halfSize, posOfCenter.y + halfSize,
		posOfCenter.z - halfSize), halfSize))
		children.add(new Octnode(new Vector3f(posOfCenter.x + halfSize, posOfCenter.y + halfSize,
		posOfCenter.z + halfSize), halfSize))
		children.add(new Octnode(new Vector3f(posOfCenter.x - halfSize, posOfCenter.y + halfSize,
		posOfCenter.z + halfSize), halfSize))
		children.add(new Octnode(new Vector3f(posOfCenter.x - halfSize, posOfCenter.y - halfSize,
		posOfCenter.z - halfSize), halfSize))
		children.add(new Octnode(new Vector3f(posOfCenter.x + halfSize, posOfCenter.y - halfSize,
		posOfCenter.z - halfSize), halfSize))
		children.add(new Octnode(new Vector3f(posOfCenter.x + halfSize, posOfCenter.y - halfSize,
		posOfCenter.z + halfSize), halfSize))
		children.add(new Octnode(new Vector3f(posOfCenter.x - halfSize, posOfCenter.y - halfSize,
		posOfCenter.z + halfSize), halfSize))
		
		val childrenToDelete = new ArrayList<Octnode>()
		
		for (child : children) {
			if (child.getHowManyPolygons(polygons) > 0) {
				if (!child.smallest) child.divide(polygons)
			} else {
				childrenToDelete.add(child)
			}
		}
		
		children.removeAll(childrenToDelete)
	}

	def void checkForVisibilityAndDraw() {
	   //if (Frustum::isBoxWithin(posOfCenter, size)) {
		  if (smallest) drawPolygons else children.forEach([it.checkForVisibilityAndDraw])
	   //}
	}

	def private drawPolygons() {
			polys.forEach([it.draw])
	}
}
