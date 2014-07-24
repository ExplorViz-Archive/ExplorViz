package explorviz.visualization.layout.datastructures.graph.model;

import java.awt.Point;

/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
/**
 *
 * @author Erich
 */
/**
 * A vertex in a graph. Every vertex has an identifying, immutable label.
 * 
 * @param <E>
 */
public class Vertex<E> {

	private final E label;
	private Point coordinate;

	/**
	 * Create a new vertex storing <code>theLabel</code>.
	 * 
	 * @param theLabel
	 *            E The label to store in this vertex.
	 */
	public Vertex(final E theLabel) {
		this.label = theLabel;

	}

	public int getXPoint() {

		return coordinate.x;
	}

	public int getYPoint() {

		return coordinate.y;
	}

	/**
	 * Get the label stored by this vertex.
	 * 
	 * @return E The label stored in this vertex.
	 */
	public E getLabel() {
		return this.label;
	}

	public int setXPos(final Point p) {
		return (int) p.getX();
	}

	public int setYPos(final Point p) {
		return (int) p.getY();
	}

	public int getXPos() {
		return (int) this.coordinate.getY();
	}

	public int getYPos() {
		return (int) this.coordinate.getX();
	}

	/**
	 * Determine if this Vertex is equal to <code>o</code>. Comparison is based
	 * on the value stored in the vertex. Overridden method from
	 * <code>Object</code>.
	 * 
	 * @param o
	 *            The other vertex.
	 * @return boolean <code>true</code> if this vertice's label is equal to the
	 *         other's label.
	 * @throws ClassCastException
	 *             if <code>o</code> is not a <code>Vertex</code>
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof Vertex)) {
			throw new ClassCastException();
		}
		return this.label.equals(((Vertex<E>) o).getLabel());
	}

	/**
	 * Return the hashcode for this vertex. Overridden method from
	 * <code>Object</code>.
	 * 
	 * @return int The hashcode for this vertex.
	 */
	@Override
	public int hashCode() {
		return this.label.hashCode();
	}

	/**
	 * Return the <code>String</code> representation version of this vertex.
	 * Overridden method from <code>Object</code>.
	 * 
	 * @return String The <code>String</code> representation of this vertex.
	 */
	@Override
	public String toString() {
		return this.label.toString();
	}
}
