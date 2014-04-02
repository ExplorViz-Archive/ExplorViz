package explorviz.visualization.export

import explorviz.visualization.model.ApplicationClientSide
import explorviz.visualization.engine.Logging
import explorviz.visualization.model.ComponentClientSide
import java.util.List
import explorviz.visualization.engine.primitives.Box
import explorviz.visualization.engine.primitives.Quad
import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.model.ClazzClientSide
import explorviz.visualization.model.helper.Draw3DNodeEntity
import java.util.ArrayList

class STLExporter {

	val static offset = new Vector3f(200.0f, 200.0f, 200.0f)
	val static scale = 2f

	def static void exportApplicationAsSTL(ApplicationClientSide application) {
		Logging::log(
			"solid " + application.name + "\n" + createApplicationComponents(application.components, true) +
				"endsolid " + application.name
		)
	}

	def static String createApplicationComponents(List<ComponentClientSide> components, boolean firstLayer) {
		var result = ""
		for (component : components) {
			if (!component.opened) {
				result = result + createApplicationClosedComponent(component, firstLayer)
			} else {
				result = result + createApplicationOpenedComponent(component, firstLayer) +
					createApplicationClazzes(component.clazzes, firstLayer)
			}
		}
		result
	}

	def static String createApplicationClosedComponent(ComponentClientSide component, boolean firstLayer) {
		createApplicationComponents(component.children, false) + createFromClosedPrimitiveObjects(component, firstLayer)
	}

	def static String createApplicationClazzes(List<ClazzClientSide> clazzes, boolean firstLayer) {
		var result = ""
		for (clazz : clazzes) {
			result = result + createFromClosedPrimitiveObjects(clazz, firstLayer)
		}
		result
	}

	def static String createFromClosedPrimitiveObjects(Draw3DNodeEntity entity, boolean firstLayer) {
		var result = ""
		for (primitiveObject : entity.primitiveObjects) {
			if (primitiveObject instanceof Box) {
				result = result + createFromClosedBox(primitiveObject as Box, firstLayer)
			}
		}
		result
	}

	def static String createFromClosedBox(Box box, boolean firstLayer) {
		var result = ""
		var i = 0
		for (quad : box.quads) {
			if (i != 3) {
				result = result + createNormalQuad(quad)
			}
			i = i + 1
		}
		result
	}

	def static String createNormalQuad(Quad quad) {
		val corner1 = quad.cornerPoints.get(0)
		val corner2 = quad.cornerPoints.get(1)
		val corner3 = quad.cornerPoints.get(2)
		val corner4 = quad.cornerPoints.get(3)

		val topLeft = new Vector3f(corner1.x, corner1.y, -1 * corner1.z).add(offset)
		topLeft.y = topLeft.y * scale
		val bottomLeft = new Vector3f(corner2.x, corner2.y, -1 * corner2.z).add(offset)
		bottomLeft.y = bottomLeft.y * scale
		val bottomRight = new Vector3f(corner3.x, corner3.y, -1 * corner3.z).add(offset)
		bottomRight.y = bottomRight.y * scale
		val topRight = new Vector3f(corner4.x, corner4.y, -1 * corner4.z).add(offset)
		topRight.y = topRight.y * scale

		val normalFloatArray = quad.triangles.get(0).normal
		val normal = new Vector3f(normalFloatArray.get(0), normalFloatArray.get(1), normalFloatArray.get(2))

		STLTriangleExportHelper::createTriangleFromPoints(topLeft, bottomLeft, bottomRight, normal) +
			STLTriangleExportHelper::createTriangleFromPoints(bottomRight, topRight, topLeft, normal)
	}

	def static String createApplicationOpenedComponent(ComponentClientSide component, boolean firstLayer) {
		createApplicationComponents(component.children, false) +
			createFromOpenedPrimitiveObjectComponent(component, firstLayer)
	}

	def static String createFromOpenedPrimitiveObjectComponent(ComponentClientSide component, boolean firstLayer) {
		var result = ""
		for (primitiveObject : component.primitiveObjects) {
			if (primitiveObject instanceof Box) {
				val topSideElements = new ArrayList<Quad>
				component.children.forEach [
					insertBottomQuadFromObject(it, topSideElements)
				]
				component.clazzes.forEach [
					insertBottomQuadFromObject(it, topSideElements)
				]

				result = result + createFromOpenedBox(primitiveObject as Box, topSideElements, firstLayer)
			}
		}
		result
	}

	def static void insertBottomQuadFromObject(Draw3DNodeEntity object, ArrayList<Quad> topSideElements) {
		for (primitiveObjectChildren : object.primitiveObjects) {
			if (primitiveObjectChildren instanceof Box) {
				var i = 0
				val primitiveObjectChildrenBox = primitiveObjectChildren as Box
				for (quad : primitiveObjectChildrenBox.quads) {
					if (i == 3) {
						topSideElements.add(quad)
					}
					i = i + 1
				}
			}
		}
	}

	def static String createFromOpenedBox(Box box, List<Quad> topSideElements, boolean firstLayer) {
		var result = ""
		var i = 0
		for (quad : box.quads) {
			if (i != 2) {
				result = result + createNormalQuad(quad)
			} else {
				result = result + glueTopToBottomOfTopElements(quad, topSideElements)
			}
			i = i + 1
		}
		result
	}

	def static String glueTopToBottomOfTopElements(Quad top, List<Quad> topSideElements) {
		var result = ""
		val quads = new ArrayList<Quad>

		quads.add(top)

		for (topSideElement : topSideElements) {
			insertQuadAndCutHole(topSideElement, quads)
		}

		for (quad : quads) {
			result = result + createNormalQuad(quad)
		}

		result
	}

	def static void insertQuadAndCutHole(Quad source, List<Quad> quads) {
		val bottomSourceLeft = source.cornerPoints.get(0)
		val bottomSourceRight = source.cornerPoints.get(1)
		val topSourceRight = source.cornerPoints.get(2)
		val topSourceLeft = source.cornerPoints.get(3)
		val y = bottomSourceLeft.y
		val color = source.color
		val tempQuads = new ArrayList<Quad>
		for (quad :  quads) {
			tempQuads.add(quad)
		}

		for (outer : tempQuads) {
			if (isQuadInsideQuad(source, outer)) {
				val bottomOuterLeft = source.cornerPoints.get(0)
				val bottomOuterRight = source.cornerPoints.get(1)
				val topOuterRight = source.cornerPoints.get(2)
				val topOuterLeft = source.cornerPoints.get(3)

				val helpPointBottomLeftFirst = new Vector3f(bottomOuterLeft.x, y, bottomSourceLeft.z)
				val helpPointBottomLeftSecond = new Vector3f(bottomSourceLeft.x, y, bottomOuterLeft.z)

				val helpPointBottomRightFirst = new Vector3f(bottomSourceRight.x, y, bottomOuterRight.z)
				val helpPointBottomRightSecond = new Vector3f(bottomOuterRight.x, y, bottomSourceRight.z)

				val helpPointTopRightFirst = new Vector3f(topOuterRight.x, y, topSourceRight.z)
				val helpPointTopRightSecond = new Vector3f(topSourceRight.x, y, topOuterRight.z)

				val helpPointTopLeftFirst = new Vector3f(topSourceLeft.x, y, topOuterLeft.z)
				val helpPointTopLeftSecond = new Vector3f(topOuterLeft.x, y, topSourceLeft.z)

				quads.add(new Quad(bottomOuterLeft, helpPointBottomLeftSecond, bottomSourceLeft, helpPointBottomLeftFirst, color))
				quads.add(new Quad(helpPointBottomLeftSecond, helpPointBottomRightFirst, bottomSourceRight, bottomSourceLeft, color))
				quads.add(new Quad(helpPointBottomRightFirst, bottomOuterRight, helpPointBottomRightSecond, bottomSourceRight, color))
				quads.add(new Quad(bottomSourceRight, helpPointBottomRightSecond, helpPointTopRightFirst, topSourceRight, color))
				quads.add(new Quad(topSourceRight, helpPointTopRightFirst, topOuterRight, helpPointTopRightSecond, color))
				quads.add(new Quad(topSourceLeft, topSourceRight, helpPointTopRightSecond, helpPointTopLeftFirst, color))
				quads.add(new Quad(helpPointTopLeftSecond, topSourceLeft, helpPointTopLeftFirst, topOuterLeft, color))
				quads.add(new Quad(helpPointBottomLeftFirst, bottomSourceLeft, topSourceLeft, helpPointTopLeftSecond, color))
				
				quads.remove(outer)
				return
			}
		}
	}

	def static boolean isQuadInsideQuad(Quad source, Quad outer) {
		outer.cornerPoints.get(0).x <= source.cornerPoints.get(0).x && outer.cornerPoints.get(0).z >= source.cornerPoints.get(0).z &&
		outer.cornerPoints.get(1).x >= source.cornerPoints.get(1).x && outer.cornerPoints.get(1).z >= source.cornerPoints.get(1).z &&
		outer.cornerPoints.get(2).x >= source.cornerPoints.get(2).x && outer.cornerPoints.get(2).z <= source.cornerPoints.get(2).z &&
		outer.cornerPoints.get(3).x <= source.cornerPoints.get(3).x && outer.cornerPoints.get(3).z <= source.cornerPoints.get(3).z
	}
}
