package explorviz.visualization.renderer

import explorviz.shared.model.Application
import java.util.ArrayList
import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.engine.math.Matrix44f
import explorviz.visualization.engine.math.Vector4f
import explorviz.visualization.engine.main.WebGLStart
import explorviz.visualization.engine.navigation.Camera
import explorviz.shared.model.Landscape
import explorviz.shared.model.helper.DrawNodeEntity
import java.util.List

class ViewCenterPointerCalculator {
	static val MIN_X = 0
	static val MAX_X = 1
	static val MIN_Y = 2
	static val MAX_Y = 3
	
	def static Vector3f calculateLandscapeCenterAndZZoom(Landscape landscape) {
		val rect = getLandscapeRect(landscape)
		val SPACE_IN_PERCENT = 0.02f

		val perspective_factor = WebGLStart::viewportWidth / WebGLStart::viewportHeight as float

		var requiredWidth = Math.abs(rect.get(MAX_X) - rect.get(MIN_X))
		requiredWidth += requiredWidth * SPACE_IN_PERCENT
		var requiredHeight = Math.abs(rect.get(MAX_Y) - rect.get(MIN_Y))
		requiredHeight += requiredHeight * SPACE_IN_PERCENT

		val newZ_by_width = requiredWidth * -1f / perspective_factor
		val newZ_by_height = requiredHeight * -1f

		Camera::getVector.z = Math.min(Math.min(newZ_by_width, newZ_by_height), -10f)

		return new Vector3f(rect.get(MIN_X) + ((rect.get(MAX_X) - rect.get(MIN_X)) / 2f),
			rect.get(MIN_Y) + ((rect.get(MAX_Y) - rect.get(MIN_Y)) / 2f), 0)
	}
	
	def private static List<Float> getLandscapeRect(Landscape landscape) {
		val rect = new ArrayList<Float>
		rect.add(Float::MAX_VALUE)
		rect.add(-Float::MAX_VALUE)
		rect.add(Float::MAX_VALUE)
		rect.add(-Float::MAX_VALUE)

		if (landscape.systems.empty) {
			rect.set(MIN_X, 0f)
			rect.set(MAX_X, 1f)
			rect.set(MIN_Y, 0f)
			rect.set(MAX_Y, 1f)
		}

		for (system : landscape.systems) {
			getMinMaxFromQuad(system, rect)
			for (nodeGroup : system.nodeGroups)
				for (node : nodeGroup.nodes)
					getMinMaxFromQuad(node, rect)
		}

		rect
	}

	def private static void getMinMaxFromQuad(DrawNodeEntity it, ArrayList<Float> rect) {
		val curX = it.positionX
		val curY = it.positionY
		if (curX < rect.get(MIN_X)) {
			rect.set(MIN_X, curX)
		}
		if (rect.get(MAX_X) < curX + (it.width)) {
			rect.set(MAX_X, curX + (it.width))
		}
		if (curY > rect.get(MAX_Y)) {
			rect.set(MAX_Y, curY)
		}
		if (rect.get(MIN_Y) > curY - (it.height)) {
			rect.set(MIN_Y, curY - (it.height))
		}
	}
	
	def static Vector3f calculateAppCenterAndZZoom(Application application) {
		val foundation = application.components.get(0)

		val rect = new ArrayList<Float>
		rect.add(foundation.positionX)
		rect.add(foundation.positionX + foundation.width)
		rect.add(foundation.positionY)
		rect.add(foundation.positionY + foundation.height)
		rect.add(foundation.positionZ)
		rect.add(foundation.positionZ + foundation.depth)

		val SPACE_IN_PERCENT = 0.02f

		val MIN_X = 0
		val MAX_X = 1
		val MIN_Y = 2
		val MAX_Y = 3
		val MIN_Z = 4
		val MAX_Z = 5

		val viewCenterPoint = new Vector3f(rect.get(MIN_X) + ((rect.get(MAX_X) - rect.get(MIN_X)) / 2f),
			rect.get(MIN_Y) + ((rect.get(MAX_Y) - rect.get(MIN_Y)) / 2f),
			rect.get(MIN_Z) + ((rect.get(MAX_Z) - rect.get(MIN_Z)) / 2f))

		var modelView = new Matrix44f();
		modelView = Matrix44f.rotationX(33).mult(modelView)
		modelView = Matrix44f.rotationY(45).mult(modelView)

		val southPoint = new Vector4f(rect.get(MIN_X), rect.get(MIN_Y), rect.get(MAX_Z), 1.0f).sub(
			new Vector4f(viewCenterPoint, 0.0f))
		val northPoint = new Vector4f(rect.get(MAX_X), rect.get(MAX_Y), rect.get(MIN_Z), 1.0f).sub(
			new Vector4f(viewCenterPoint, 0.0f))

		val westPoint = new Vector4f(rect.get(MIN_X), rect.get(MIN_Y), rect.get(MIN_Z), 1.0f).sub(
			new Vector4f(viewCenterPoint, 0.0f))
		val eastPoint = new Vector4f(rect.get(MAX_X), rect.get(MAX_Y), rect.get(MAX_Z), 1.0f).sub(
			new Vector4f(viewCenterPoint, 0.0f))

		var requiredWidth = Math.abs(modelView.mult(westPoint).x - modelView.mult(eastPoint).x)
		requiredWidth += requiredWidth * SPACE_IN_PERCENT
		var requiredHeight = Math.abs(modelView.mult(southPoint).y - modelView.mult(northPoint).y)
		requiredHeight += requiredHeight * SPACE_IN_PERCENT

		val perspective_factor = WebGLStart::viewportWidth / WebGLStart::viewportHeight as float

		val newZ_by_width = requiredWidth * -1f / perspective_factor
		val newZ_by_height = requiredHeight * -1f

		Camera::getVector.z = Math.min(Math.min(newZ_by_width, newZ_by_height), -15f)
		
		return viewCenterPoint
	}
}
