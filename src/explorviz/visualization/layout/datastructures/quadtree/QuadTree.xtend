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
				new Rectangle(x + quadDim.getWidth() as int, y + quadDim.getHeight() as int, quadDim.getWidth() as int,
					quadDim.getHeight() as int)))
	}

	def int lookUpQuadrant(Rectangle2D pRect, Rectangle2D bthBounds, int level) {
		var depth = level;
		var double verticalMidpoint = bthBounds.getX() + (bthBounds.width / 2)
		var double horizontalMidpoint = bthBounds.getY() + (bthBounds.height / 2)
		var Rectangle2D halfBounds = new Rectangle((bthBounds.width / 2) as int, (bthBounds.height / 2) as int)
		if (((pRect.getX() + pRect.width) < verticalMidpoint) && ((pRect.getY() + pRect.height) < horizontalMidpoint)) {
			depth = lookUpQuadrant(pRect, halfBounds, level + 1)
		}

		depth
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
//		var double fli = help.flaechenInhalt(space)
//		var double boundsArea = help.flaechenInhalt(new Rectangle(tree.bounds.width as int, tree.bounds.height as int))
//		if (fli > boundsArea - usedSpace(tree, space)) {
//			return false; 
//		}
//
//		if (tree.nodes.get(0) != null) {
//			var double node0area = usedSpace(tree.nodes.get(0), space)
//			var double node1area = usedSpace(tree.nodes.get(1), space)
//			var double node2area = usedSpace(tree.nodes.get(2), space)
//			var double node3area = usedSpace(tree.nodes.get(3), space)
//
//			if (fli < boundsArea/4 - node0area || fli < boundsArea/4 - node1area || fli < boundsArea/4 - node2area || fli < boundsArea/4 - node3area) {
//				return true
//			}
		if(tree.nodes.get(0) != null) {
			if(tree.nodes.get(0).objects.empty || tree.nodes.get(1).objects.empty || tree.nodes.get(2).objects.empty || tree.nodes.get(3).objects.empty) {
				return true
			} else {
				return false
			}
		} else if(tree.objects.size > 0) {
			return false
		} else {
			return true
		}
//		} else if (fli < boundsArea - usedSpace(tree, space)) {
//			if(tree.objects.size > 0) {
//				System::out.println("huhu oben")
//				return false
//			}
//			return true
//		}
	}

	def boolean partFilled(QuadTree quad) {
		if(quad.nodes.get(0) != null) {
			if(!quad.nodes.get(0).objects.empty) {
				return true	
			} else if(!quad.nodes.get(1).objects.empty) {
				return true
				
			} else if(!quad.nodes.get(2).objects.empty) {
				return true
				
			} else if(!quad.nodes.get(3).objects.empty) {
				return true
			} else {
				return false
			
			}
		} else if(!quad.objects.empty) {
			return true
		} else {
			return false
		}

	}

	def boolean insert(QuadTree quad, Rectangle2D pRect) {
		var Rectangle2D rectWithSpace = new Rectangle((pRect.width as int)+10, (pRect.height as int)+10)
		
		//if (haveSpace(quad, rectWithSpace) == false) return false
		if(quad.objects.size > 0) {
			return false
		}
		var rectDepth = lookUpQuadrant(rectWithSpace, new Rectangle(quad.bounds.width as int, quad.bounds.height as int),
			quad.level)
		if (rectDepth == quad.level && partFilled(quad) == true) {
			System::out.println(partFilled(quad))
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
			else if (insert(quad.nodes.get(3), pRect) == true) 
				return true 
			else 
				return false
		} else {
			if(quad.nodes.get(0) != null) return false

			quad.objects.add(new Rectangle(quad.bounds.x as int, quad.bounds.y as int, pRect.width as int, pRect.height as int))
			return true
		}
	}
	
	def boolean spaceForNeightbor(QuadTree quad, Rectangle2D pRect) {
		for(i : 0 ..< quad.objects.size) {
			if(quad.bounds.width - usedWidth(quad) < pRect.width) {
				return false
			}
		}
		
		return true;
	}
	
	def void arrangeObjects(QuadTree quad, Rectangle2D area) {
		var double dimX = 0;
		var double dimY = 0;

		if(quad.objects.size > 1) {
			var double usedWidth = usedWidth(quad)
			var double usedHeight = usedHeight(quad)
			System::out.println(quad.objects.size + " size")
			if(quad.bounds.width - usedWidth > area.width) {
				dimX = quad.objects.last.x + 10
				System::out.println("dimX: "+dimX)
			} else {
				dimX = quad.objects.get(quad.objects.indexOf(area)).x
			}
			
			if(quad.bounds.height-usedHeight > area.height) {
				dimY = quad.objects.last.y + 10
			} else {
				dimY = quad.objects.get(quad.objects.indexOf(area)).y
			}
				quad.objects.get(quad.objects.indexOf(area)).setRect(dimX as int, dimY as int, area.width as int, area.height as int)
				
					
			
		}
	}
	
	def double usedWidth(QuadTree quad) {
		var double usedWidth = 0;
		for(i : 0 ..< quad.objects.size) {
			if(i == 0) {
				usedWidth += quad.objects.get(i).width
			}
			if(quad.objects.get(i).x > usedWidth && quad.objects.get(i).y < (quad.objects.get(0).y + quad.objects.get(0).height)) {
				usedWidth += quad.objects.get(i).width
			}
			
			if(usedWidth >= quad.bounds.width) {
				usedWidth = quad.bounds.width
			}
		}
		
		return usedWidth		
	}
	
	def double usedHeight(QuadTree quad) {
		var double usedHeight = 0;
		for(i : 0 ..< quad.objects.size) {
			if(i == 0) {
				usedHeight += quad.objects.get(i).height
			}
			if(quad.objects.get(i).y > usedHeight && quad.objects.get(i).x < quad.objects.last.x) {
				usedHeight += quad.objects.get(i).height
			}
			
			if(usedHeight >= quad.bounds.height) {
				usedHeight = quad.bounds.height
			}
		}
		
		return usedHeight		
	}	
	
	def ArrayList<Rectangle2D> getObjectsBuh(QuadTree quad) {
		val ArrayList<Rectangle2D> rect = new ArrayList<Rectangle2D>();
		if(quad.objects.size > 1) {
			System::out.println("das ist ja quatsch")
		}
		quad.objects.forEach [
			rect.add(it)
		]

		if (quad.nodes.get(0) != null) {
			rect.addAll(getObjectsBuh(quad.nodes.get(0)))
			rect.addAll(getObjectsBuh(quad.nodes.get(1)))
			rect.addAll(getObjectsBuh(quad.nodes.get(2)))
			rect.addAll(getObjectsBuh(quad.nodes.get(3)))
		}
		
		return rect;
	}

}
