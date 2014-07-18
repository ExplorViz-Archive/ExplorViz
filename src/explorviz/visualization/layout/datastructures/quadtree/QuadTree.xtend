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
	
	private val int MAX_OBJECTS = 4;
	private val int MAX_LEVELS = 5;

	@Property var QuadTree parent;
	
	new(int pLevel, Rectangle2D pBounds) {
		level = pLevel
		objects = new ArrayList<Rectangle2D>()
		bounds = pBounds
		nodes = newArrayOfSize(4)
	}
	
		/*
	 * Clears the quadtree
	 */
	def clear() {
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
	def split() {
		val Dimension quadDim = new Dimension => [
			width = (bounds.getWidth() / 2) as Integer
			height = (bounds.getHeight() / 2) as Integer
			]
		val x = bounds.getX() as Integer
		val y = bounds.getY() as Integer

		nodes.set(0, new QuadTree(this.level + 1, new Rectangle(x + quadDim.getWidth() as Integer,y, quadDim.getWidth() as Integer,quadDim.getHeight() as Integer)))
		nodes.set(1, new QuadTree(this.level + 1, new Rectangle(x, y, quadDim.getWidth() as Integer, quadDim.getHeight() as Integer)))
		nodes.set(2, new QuadTree(this.level + 1, new Rectangle(x, y + quadDim.getHeight() as Integer, quadDim.getWidth() as Integer, quadDim.getHeight() as Integer)))
		nodes.set(3, new QuadTree(this.level + 1, new Rectangle(x + quadDim.getWidth() as Integer, quadDim.getHeight() as Integer, quadDim.getWidth() as Integer, quadDim.getHeight() as Integer)))
	}

	/*
	 * Determine which node the object belongs to. -1 means object cannot
	 * completely fit within a child node and is part of the parent node
	 */
	def int getIndex(Rectangle2D pRect) {
		var int index = -1
		val double verticalMidpoint = bounds.getX() + (bounds.getWidth() / 2)
		val double horizontalMidpoint = bounds.getY() + (bounds.getHeight() / 2)

		// Object can completely fit within the top quadrants
		val boolean topQuadrant = ((pRect.getY() < horizontalMidpoint) && ((pRect.getY() + pRect
				.getHeight()) < horizontalMidpoint));
		// Object can completely fit within the bottom quadrants
		val boolean bottomQuadrant = (pRect.getY() > horizontalMidpoint);

		// Object can completely fit within the left quadrants
		if ((pRect.getX() < verticalMidpoint)
				&& ((pRect.getX() + pRect.getWidth()) < verticalMidpoint)) {
			if (topQuadrant) {
				index = 1;
			} else if (bottomQuadrant) {
				index = 2;
			}
		}
		// Object can completely fit within the right quadrants
		else if (pRect.getX() > verticalMidpoint) {
			if (topQuadrant) {
				index = 0;
			} else if (bottomQuadrant) {
				index = 3;
			}
		}

		return index;
	}

	/*
	 * Insert the object into the quadtree. If the node exceeds the capacity, it
	 * will split and add all objects to their corresponding nodes.
	 */
	def insert(Rectangle2D pRect) {
		if (nodes.get(0) != null) {
			val int index = getIndex(pRect);

			if (index != -1) {
				nodes.get(index).insert(pRect);

				return;
			}
		}

		objects.add(pRect);

		if (true) {
			if (nodes.get(0) == null) {
				split();
			}

			var int i = 0;
			while (i < objects.size) {
				val int index = getIndex(objects.get(i));
				if (index != -1) {
					nodes.get(index).insert(objects.remove(i));
				} else {
					i++;
				}
			}
		}
	}

	def getObjectsBuh(QuadTree quad) {
		val ArrayList<Rectangle2D> rect = new ArrayList<Rectangle2D>();
		this.objects.forEach[ 
			rect.add(it)
		]
		if (quad.nodes.get(0) != null) {
			getObjectsBuh(nodes.get(0));
		}

		return rect;
	}
	
	
}