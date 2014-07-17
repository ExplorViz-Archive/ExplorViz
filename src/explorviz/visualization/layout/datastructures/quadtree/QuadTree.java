package explorviz.visualization.layout.datastructures.quadtree;

import java.awt.Rectangle;
import java.util.ArrayList;

import javax.swing.JFrame;

public class QuadTree implements QuadTreeInterface {
	// The MAX_OBJECTS and LEVEL constants
	private final int MAX_OBJECTS = 10;
	private final int level;

	// The objects list
	private final ArrayList<Rectangle> objects;
	// The retrieve list
	private ArrayList<Rectangle> retrieveList;

	// The bounds of this tree
	private final Rectangle bounds;

	// Branches of this tree a.k.a the quadrants
	private final QuadTree[] nodes;

	/**
	 * Construct a QuadTree with default values. Set's for the whole screen
	 */
	public QuadTree() {
		this(0, MapView.getVisibleRect());
	}

	/**
	 * Construct a QuadTree with custom values. Used to create sub trees or
	 * branches
	 * 
	 * @param l
	 *            The level of this tree
	 * @param b
	 *            The bounds of this tree
	 */
	public QuadTree(final int l, final Rectangle b) {
		level = l;
		bounds = b;
		objects = new ArrayList<Rectangle>();
		retrieveList = new ArrayList<Rectangle>();
		nodes = new QuadTree[4];
	}

	/**
	 * Set's the bounds of this tree.
	 * 
	 * @param x
	 *            The x-coordinate
	 * @param y
	 *            The y-coordinate
	 * @param width
	 *            The width
	 * @param height
	 *            The height
	 */
	public void setBounds(final int x, final int y, final int width, final int height) {
		bounds.x = x;
		bounds.y = y;
		bounds.width = width;
		bounds.height = height;
		clear();
		split();
	}

	/**
	 * Clear this tree. Also clears any subtrees.
	 */
	public void clear() {
		objects.clear();
		for (int i = 0; i < nodes.length; i++) {
			if (nodes[i] != null) {
				nodes[i].clear();
				nodes[i] = null;
			}
		}
	}

	// Split the tree into 4 quadrants
	private void split() {
		final int subWidth = (int) (bounds.getWidth() / 2);
		final int subHeight = (int) (bounds.getHeight() / 2);
		final int x = (int) bounds.getX();
		final int y = (int) bounds.getY();
		nodes[0] = new QuadTree(level + 1, new Rectangle(x + subWidth, y, subWidth, subHeight));
		nodes[1] = new QuadTree(level + 1, new Rectangle(x, y, subWidth, subHeight));
		nodes[2] = new QuadTree(level + 1, new Rectangle(x, y + subHeight, subWidth, subHeight));
		nodes[3] = new QuadTree(level + 1, new Rectangle(x + subWidth, y + subHeight, subWidth,
				subHeight));
	}

	// Get the index of an object
	private int getIndex(final Rectangle r) {
		int index = -1;
		final double verticalMidpoint = bounds.getX() + (bounds.getWidth() / 2);
		final double horizontalMidpoint = bounds.getY() + (bounds.getHeight() / 2);
		final boolean topQuadrant = ((r.getY() < horizontalMidpoint) && ((r.getY() + r.getHeight()) < horizontalMidpoint));
		final boolean bottomQuadrant = (r.getY() > horizontalMidpoint);
		if ((r.getX() < verticalMidpoint) && ((r.getX() + r.getWidth()) < verticalMidpoint)) {
			if (topQuadrant) {
				index = 1;
			} else if (bottomQuadrant) {
				index = 2;
			}
		} else if (r.getX() > verticalMidpoint) {
			if (topQuadrant) {
				index = 0;
			} else if (bottomQuadrant) {
				index = 3;
			}
		}
		return index;
	}

	// Get the index of a rectangle
	private int getIndex(final Rectangle r) {
		int index = -1;
		final double verticalMidpoint = bounds.x + (bounds.width / 2);
		final double horizontalMidpoint = bounds.y + (bounds.height / 2);
		final boolean topQuadrant = ((r.y < horizontalMidpoint) && ((r.y + r.height) < horizontalMidpoint));
		final boolean bottomQuadrant = (r.y > horizontalMidpoint);
		if ((r.x < verticalMidpoint) && ((r.x + r.width) < verticalMidpoint)) {
			if (topQuadrant) {
				index = 1;
			} else if (bottomQuadrant) {
				index = 2;
			}
		} else if (r.getX() > verticalMidpoint) {
			if (topQuadrant) {
				index = 0;
			} else if (bottomQuadrant) {
				index = 3;
			}
		}
		return index;
	}

	/**
	 * Insert an object into this tree
	 */
	public void insert(final Rectangle r) {
		if (nodes[0] != null) {
			final int index = getIndex(r);
			if (index != -1) {
				nodes[index].insert(r);
				return;
			}
		}
		objects.add(r);
		if (objects.size() > MAX_OBJECTS) {
			if (nodes[0] == null) {
				split();
			}
			for (int i = 0; i < objects.size(); i++) {
				final int index = getIndex(objects.get(i));
				if (index != -1) {
					nodes[index].insert(objects.remove(i));
				}
			}
		}
	}

	/**
	 * Insert an ArrayList of objects into this tree
	 */
	public void insert(final ArrayList<Rectangle> o) {
		for (int i = 0; i < o.size(); i++) {
			insert(o.get(i));
		}
	}

	/**
	 * Returns the collidable objects with the given object
	 */
	public ArrayList<Rectangle> retrieve(final Rectangle r) {
		retrieveList.clear();
		final int index = getIndex(r);
		if ((index != -1) && (nodes[0] != null)) {
			retrieveList = nodes[index].retrieve(r);
		}
		retrieveList.addAll(objects);
		return retrieveList;
	}

	public static void main(final String[] args) {
		final JFrame window = new JFrame();
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setBounds(30, 30, 300, 300);
		window.setVisible(true);

		final QuadTree quadtree = new QuadTree();

	}
}
