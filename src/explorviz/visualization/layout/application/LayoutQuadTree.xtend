package explorviz.visualization.layout.application

import explorviz.shared.model.Clazz
import explorviz.shared.model.Component
import explorviz.shared.model.datastructures.quadtree.MergedQuadTree
import explorviz.shared.model.datastructures.quadtree.QuadTree
import explorviz.shared.model.helper.Bounds
import explorviz.visualization.engine.Logging
import java.util.ArrayList
import java.util.List

class LayoutQuadTree {

	public val insetSpace = 4.0f
	public val labelInsetSpace = 8.0f
	val floorHeight = 0.75f
	val static comp = new ComponentAndClassComparator()

	new(Component component) {
		createQuadTree(component)
	}

	def private void createQuadTree(Component component) {

		val QuadTree quad = new QuadTree(0,
			new Bounds(component.positionX + labelInsetSpace, component.positionY + floorHeight, component.positionZ,
				component.width - labelInsetSpace, 0, component.depth))

		//		val compi = new RankComperator(graph)
		component.children.sortInplace(comp).reverse

		//		var List<Component> compList = graph.orderComponents(component)
		component.children.forEach [
			quad.insert(quad, it)
			createQuadTree(it)
		]
		
		component.clazzes.forEach [
			quad.insert(quad, it)
		]

		//			if (component.opened) {
		//				pipeGraph.merge(quad.getPipeEdges(quad))
		//			}
		if (quad.nodes.get(0) != null) {
//			if (emptyQuad(quad.nodes.get(2)) == true && emptyQuad(quad.nodes.get(3)) == true) {
//				component.depth = component.depth / 2f
//			}
//			if (emptyQuad(quad.nodes.get(1)) == true && emptyQuad(quad.nodes.get(2)) == true) {
//				component.width = component.width / 2f
//			}

			quad.merge(quad)
			quad.adjustQuadTree(quad)
		}
		
		component.quadTree = quad
		component.adjust

	//						cleanUpMissingSpaces(component)
	}

	def void merge(QuadTree quad) {
		if (quad.nodes.get(0) != null) {
			val QuadTree quadsToMerge = findMergeQuad(quad.nodes.get(0))

			if (quadsToMerge != null) {
				var List<QuadTree> quads = new ArrayList<QuadTree>()
				quads.add(quadsToMerge)
				if (!quad.nodes.get(1).objects.empty) {
					quads.add(quad.nodes.get(1))
					var MergedQuadTree newQuad = new MergedQuadTree(quads)
				}
			}
		}
	}

	def QuadTree findMergeQuad(QuadTree quad) {
		var QuadTree quadsToMerge = null

		if (quad.nodes.get(0) != null) {
			quadsToMerge = findMergeQuad(quad.nodes.get(0))

			if (emptyQuad(quad.nodes.get(1)) && emptyQuad(quad.nodes.get(2)) && emptyQuad(quad.nodes.get(3))) {
				if (quadsToMerge == null) {
					quadsToMerge = quad.nodes.get(0)

					if (!quadsToMerge.objects.empty) {
//						Logging.log("hier ist:  " + quadsToMerge.objects.get(0).name + " und bounds: X: " + quadsToMerge.bounds.positionX + " Width: " + quadsToMerge.bounds.width)
					}

//					Logging.log("found one " + quadsToMerge + " und parent: " + quad)
				}
			}
		}

		return quadsToMerge
	}

	def boolean emptyQuad(QuadTree quad) {
		return quad.nodes.get(0) == null && quad.objects.empty == true
	}

	def void cleanUpMissingSpaces(Component component) {

		//		component.children.forEach [
		//			cleanUpMissingSpaces(it)
		//		]
		if (mostLeftPosition(component) != 0 && !component.children.empty) {
			cutQuadX(component)
		}

		val float biggestZ = biggestZ(component)
		val float biggestX = biggestX(component)

		if (!component.children.empty) {
			if (component.positionZ + component.depth <= biggestZ) {
				component.depth = (biggestZ - component.positionZ) + insetSpace
			} else if (component.positionZ + component.depth > biggestZ) {
				component.depth = (biggestZ - component.positionZ) + insetSpace
			}

			if (component.positionX + component.width <= biggestX) {
				component.width = (biggestX - component.positionX) + labelInsetSpace
			} else if (component.positionX + component.width > biggestX) {
				component.width = (biggestX - component.positionX) + labelInsetSpace
			}
		}

	}

	def private float mostLeftPosition(Component component) {
		var float mostLeftPosition = 0f

		if (!component.children.empty) {
			mostLeftPosition = component.children.get(0).positionX

			for (Component child : component.children) {
				if (child.positionX < mostLeftPosition) mostLeftPosition = child.positionX
			}
		} else if (!component.clazzes.empty) {
			mostLeftPosition = component.clazzes.get(0).positionX

			for (Clazz clazz : component.clazzes) {
				if (clazz.positionX < mostLeftPosition) mostLeftPosition = clazz.positionX
			}
		}

		return mostLeftPosition
	}

	def private void cutQuadX(Component component) {
		var float mostLeftPosition = mostLeftPosition(component)

		if (mostLeftPosition > component.positionX + 4 * labelInsetSpace) {
			component.width = component.width - (mostLeftPosition - component.positionX - labelInsetSpace) +
				labelInsetSpace
		}

		moveComponentsX(component, -(mostLeftPosition - component.positionX - labelInsetSpace) + labelInsetSpace)
	}

	def private void moveComponentsX(Component component, float moveParameter) {
		component.children.forEach [
			moveComponentsX(it, moveParameter)
			it.positionX = it.positionX + moveParameter
		]

		component.clazzes.forEach [
			it.positionX = it.positionX + moveParameter
		]
	}

	def private float biggestZ(Component component) {
		var float mostBottomPosition = 0f

		if (!component.children.empty) {
			mostBottomPosition = component.children.get(0).positionZ + component.children.get(0).depth
		}

		for (Component child : component.children) {
			if (child.positionZ + child.depth > mostBottomPosition) mostBottomPosition = child.positionZ + child.depth
		}

		for (Clazz clazz : component.clazzes) {
			if (clazz.positionZ + clazz.depth > mostBottomPosition) mostBottomPosition = clazz.positionZ + clazz.depth
		}

		return mostBottomPosition
	}

	def private float biggestX(Component component) {
		var float mostRightPosition = 0f
		component.children.sortInplace(comp)

		if (!component.children.empty) {
			mostRightPosition = component.children.get(0).positionX + component.children.get(0).width

			for (Component child : component.children) {
				if (child.positionX + child.width > mostRightPosition) mostRightPosition = child.positionX + child.width
			}

		} else if (!component.clazzes.empty) {
			mostRightPosition = component.clazzes.get(0).positionX + component.clazzes.get(0).width

			for (Clazz clazz : component.clazzes) {
				if (clazz.positionX + clazz.width > mostRightPosition) mostRightPosition = clazz.positionX + clazz.width
			}
		}

		return mostRightPosition
	}

	def private void cutQuadZ(Component component) {
		component.depth = biggestZ(component)
	}
}
