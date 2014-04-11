package explorviz.visualization.export

import explorviz.visualization.engine.math.Vector3f

class STLTriangleExportHelper {
	def static String createTriangleFromPoints(Vector3f first, Vector3f second, Vector3f third, Vector3f normal) {
		"  facet normal " + normal.x.toIEEEFloat + " " + (-1 * normal.z).toIEEEFloat + " " + normal.y.toIEEEFloat + "\n" +
			"    outer loop" + "\n" + createVertex(first) + createVertex(second) + createVertex(third) + "    endloop" +
			"\n" + "  endfacet" + "\n"
	}

	def static String createVertex(Vector3f vec) {
		"      vertex " + vec.x.toIEEEFloat + " " + vec.z.toIEEEFloat + " " + vec.y.toIEEEFloat + "\n"
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

		while (i < 8) { // TODO -100000e
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
