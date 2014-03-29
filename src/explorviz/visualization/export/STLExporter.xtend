package explorviz.visualization.export

import explorviz.visualization.model.ApplicationClientSide
import explorviz.visualization.engine.Logging
import explorviz.visualization.model.ComponentClientSide
import java.util.List
import explorviz.visualization.engine.primitives.Box
import explorviz.visualization.engine.primitives.Quad
import explorviz.visualization.engine.math.Vector3f
import java.text.DecimalFormat
import explorviz.visualization.model.ClazzClientSide

class STLExporter {

	val static offset = new Vector3f(200.0f, 200.0f, 200.0f)
	val static scale = 1f

	def static void exportApplicationAsSTL(ApplicationClientSide application) {
		Logging::log(
			"solid " + application.name + "\n" + createApplicationComponents(application.components, true) +
				"endsolid " + application.name
		)
	}

	def static String createApplicationComponents(List<ComponentClientSide> components, boolean firstLayer) {
		var result = ""
		for (component : components) {
			result = result + createApplicationComponent(component, firstLayer) +
				createApplicationClazzes(component.clazzes, firstLayer)
		}
		result
	}

	def static String createApplicationClazzes(List<ClazzClientSide> clazzes, boolean firstLayer) {
		var result = ""
		for (clazz : clazzes) {
			result = result + createApplicationClass(clazz, firstLayer)
		}
		result
	}

	def static String createApplicationComponent(ComponentClientSide component, boolean firstLayer) {
		var result = createApplicationComponents(component.children, false)
		for (primitiveObject : component.primitiveObjects) {
			if (primitiveObject instanceof Box) {
				result = result + createFromBox(primitiveObject as Box, firstLayer)
			}
		}
		result
	}

	def static String createApplicationClass(ClazzClientSide clazz, boolean firstLayer) {
		var result = ""
		for (primitiveObject : clazz.primitiveObjects) {
			if (primitiveObject instanceof Box) {
				result = result + createFromBox(primitiveObject as Box, firstLayer)
			}
		}
		result
	}

	def static String createFromBox(Box box, boolean firstLayer) {
		var result = ""
		var i = 0
		for (quad : box.quads) {

			//			if (i != 3) {
			result = result + createQuad(quad)

			//			}
			i = i + 1
		}
		result
	}

	def static String createQuad(Quad quad) {
		val cornor1 = quad.cornerPoints.get(0)
		val cornor2 = quad.cornerPoints.get(1)
		val cornor3 = quad.cornerPoints.get(2)
		val cornor4 = quad.cornerPoints.get(3)

		val topLeft = new Vector3f(cornor1.x, cornor1.y,  -1 * cornor1.z).add(offset).div(scale)
		val bottomLeft = new Vector3f(cornor2.x, cornor2.y,  -1 * cornor2.z).add(offset).div(scale)
		val bottomRight = new Vector3f(cornor3.x, cornor3.y,  -1 * cornor3.z).add(offset).div(scale)
		val topRight = new Vector3f(cornor4.x, cornor4.y,  -1 * cornor4.z).add(offset).div(scale)

		val centerPoint = new Vector3f(topLeft.x + ((bottomRight.x - topLeft.x) / 2f),
			topLeft.y + ((bottomRight.y - topLeft.y) / 2f), topLeft.z + ((bottomRight.z - topLeft.z) / 2f))
		val normalFloatArray = quad.triangles.get(0).normal
		val normal = new Vector3f(normalFloatArray.get(0), normalFloatArray.get(1), normalFloatArray.get(2))

		createTriangleFromPoints(topLeft, bottomLeft, centerPoint, normal) +
			createTriangleFromPoints(bottomLeft, bottomRight, centerPoint, normal) +
			createTriangleFromPoints(bottomRight, topRight, centerPoint, normal) +
			createTriangleFromPoints(topRight, topLeft, centerPoint, normal)
	}

	def static String createTriangleFromPoints(Vector3f first, Vector3f second, Vector3f third, Vector3f normal) {
		"  facet normal " + normal.x.toIEEEFloat + " " + (-1 * normal.z).toIEEEFloat + " " + normal.y.toIEEEFloat + "\n" +
			"    outer loop" + "\n" + createVertex(first) + createVertex(second) + createVertex(third) + "    endloop" +
			"\n" + "  endfacet" + "\n"
	}

	def static String createVertex(Vector3f vec) {
		"      vertex " + vec.x.toIEEEFloat + " " +  vec.z.toIEEEFloat + " " + vec.y.toIEEEFloat + "\n"
	}

	def static String toIEEEFloat(float f) {
		var expo = if (f < 1.0f) 0 else (Math.floor(Math.log10(f) as float) as int)

		if (expo == 0) {
			roundToSixPlaces(f) + "e+00"
		} else {
			var sign = if (expo >= 0) "+" else "-"
			roundToSixPlaces(f / (Math.pow(10f, expo) as float)) + "e" + sign + roundToTwoPlaces(expo)
		}
	}

	def static String roundToTwoPlaces(int number) {
		if (number < 10) {
			"0" + number.toString()
		} else {
			number.toString()
		}
	}

	def static String roundToSixPlaces(float number) {
		var result = ""
		var i = 0
		var charArray = number.toString().toCharArray

		while (i < 8) {
			if (i < charArray.length) {
				result = result + charArray.get(i)
			} else {
				if (i == 1) {
					result = result + ".0"
				} else {
					result = result + "0"
				}
			}
			i = i + 1
		}

		result
	}
}
