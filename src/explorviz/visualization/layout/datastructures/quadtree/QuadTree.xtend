package explorviz.visualization.layout.datastructures.quadtree

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

class QuadTree {
	@Property var int level
	@Property var ArrayList<Rectangle2D> objects;
	@Property var Rectangle2D bounds;
	@Property var QuadTree[] nodes;
	@Property var QuadTree parent;
	val Helper help = new Helper();
	val int MAX_OBJECTS = 10;
	val int MAX_LEVELS = 5;

	new(int pLevel, Rectangle2D pBounds) {
		level = pLevel
		objects = new ArrayList<Rectangle2D>()
		bounds = pBounds
		nodes = newArrayOfSize(4)
	}

	/*
	 * Clears the quadtree
	 */
	def void clear() {

		//TODO
		for (i : 0 ..< nodes.length) {
			if (nodes.get(i) != null) {
				nodes.get(i).clear()
				nodes.set(i, null)
			}
		}
	}

	/*
	 * Splits the node into 4 subnodes
	 */
	def void split() {
		var Dimension quadDim = new Dimension => [
			width = (bounds.getWidth() / 2) as int
			height = (bounds.getHeight() / 2) as int
		]
		var int x = bounds.getX() as int
		var int y = bounds.getY() as int

		nodes.set(0,
			new QuadTree(this.level + 1,
				new Rectangle(x + quadDim.getWidth() as int, y, quadDim.getWidth() as int, quadDim.getHeight() as int)))
		nodes.set(1,
			new QuadTree(this.level + 1, new Rectangle(x, y, quadDim.getWidth() as int, quadDim.getHeight() as int)))
		nodes.set(2,
			new QuadTree(this.level + 1,
				new Rectangle(x, y + quadDim.getHeight() as int, quadDim.getWidth() as int, quadDim.getHeight() as int)))
		nodes.set(3,
			new QuadTree(this.level + 1,
				new Rectangle(x + quadDim.getWidth() as int, quadDim.getHeight() as int, quadDim.getWidth() as int,
					quadDim.getHeight() as int)))
	}

	def void splitToDepth(QuadTree tree, int depth) {
		if (depth > 0 && tree.nodes.get(0) == null) {
			tree.split()
			splitToDepth(tree.nodes.get(0), depth - 1)
			System::out.println("splitte fröhlich: " + depth)
		}
	}

	/*
	 * Determine which node the object belongs to. -1 means object cannot
	 * completely fit within a child node and is part of the parent node
	 */
	def int getIndex(Rectangle2D pRect) {
		var int index = -1
		var double verticalMidpoint = bounds.getX() + (bounds.getWidth() / 2)
		var double horizontalMidpoint = bounds.getY() + (bounds.getHeight() / 2)

		if (haveSpace(this, pRect)) {
		}

		return index;
	}

	def int lookUpQuadrant(Rectangle2D pRect, Rectangle2D bthBounds, int level) {
		var depth = level;
		var double verticalMidpoint = bthBounds.getX() + (bthBounds.width / 2)
		var double horizontalMidpoint = bthBounds.getY() + (bthBounds.height / 2)
		var Rectangle2D quatsch = new Rectangle((bthBounds.width / 2) as int, (bthBounds.height / 2) as int)
		if (((pRect.getX() + pRect.width) < verticalMidpoint) && ((pRect.getY() + pRect.height) < horizontalMidpoint)) {
			depth = lookUpQuadrant(pRect, quatsch, level + 1)
		}

		depth
	}

	/*
	 * Insert the object into the quadtree. If the node exceeds the capacity, it
	 * will split and add all objects to their corresponding nodes.
	 */
	def boolean fit() {
		return true;
	}

	def double usedSpace(QuadTree tree, Rectangle2D space) {
		var double usedSpace = 0;
		var rectDepth = lookUpQuadrant(space, new Rectangle(tree.bounds.width as int, tree.bounds.height as int),
			tree.level)
		if (tree.nodes.get(0) != null && rectDepth > tree.level) {
			usedSpace += usedSpace(tree.nodes.get(0), space)
			usedSpace += usedSpace(tree.nodes.get(1), space)
			usedSpace += usedSpace(tree.nodes.get(2), space)
			usedSpace += usedSpace(tree.nodes.get(3), space)
		} else {
			for (i : 0 ..< tree.objects.size) {
				usedSpace += help.flaechenInhalt(tree.objects.get(i))
			}
		}
		return usedSpace
	}

	def boolean haveSpace(QuadTree tree, Rectangle2D space) {
		var boolean gotSpace = false;
		var double fli = help.flaechenInhalt(space)
		if (nodes.get(0) != null) {
			if (fli <
				help.flaechenInhalt(new Rectangle(bounds.width as int, bounds.height as int)) -
					usedSpace(nodes.get(0), space) ||
				fli <
					help.flaechenInhalt(new Rectangle(bounds.width as int, bounds.height as int)) -
						usedSpace(nodes.get(1), space) ||
				fli <
					help.flaechenInhalt(new Rectangle(bounds.width as int, bounds.height as int)) -
						usedSpace(nodes.get(2), space) ||
				fli <
					help.flaechenInhalt(new Rectangle(bounds.width as int, bounds.height as int)) -
						usedSpace(nodes.get(3), space)) {
				gotSpace = true;
			}
		} else if (fli <
			help.flaechenInhalt(new Rectangle(bounds.width as int, bounds.height as int)) - usedSpace(this, space)) {
			gotSpace = true;
		}
		System::out.println("gotSpace: " + gotSpace)
		return gotSpace
	}

	def void checkDepth(QuadTree tree) {
		if (tree.nodes.get(0) != null) {
			checkDepth(tree.nodes.get(0))
		}
	}

	def boolean insert(QuadTree quad, Rectangle2D pRect) {
		if (haveSpace(quad, pRect) == false) return false

		var rectDepth = lookUpQuadrant(pRect, new Rectangle(quad.bounds.width as int, quad.bounds.height as int), quad.level)
		if (rectDepth == quad.level && haveSpace(quad, pRect) == false) {
			return false
		}

		if (rectDepth > quad.level) {
			if (quad.nodes.get(0) == null) {
				quad.split()
			}
			if (insert(quad.nodes.get(0), pRect) == true)
				return true
			else if (insert(quad.nodes.get(1), pRect) == true)
				return true
			else if (insert(quad.nodes.get(2), pRect) == true)
				return true
			else if (insert(quad.nodes.get(3), pRect) == true) return true 
			else return false
		} else {
			quad.objects.add(new Rectangle(quad.bounds.x as int, quad.bounds.y as int, pRect.width as int, pRect.height as int))
			return true
		}
	}

	def ArrayList<Rectangle2D> getObjectsBuh(QuadTree quad) {
		val ArrayList<Rectangle2D> rect = new ArrayList<Rectangle2D>();
		System::out.println(objects.size)
		quad.objects.forEach [
			rect.add(it)
		]
		
		if(quad.nodes.get(0) != null) {
			System::out.println("fu")
			rect.addAll(getObjectsBuh(quad.nodes.get(0)))
		}

		return rect;
	}

}
