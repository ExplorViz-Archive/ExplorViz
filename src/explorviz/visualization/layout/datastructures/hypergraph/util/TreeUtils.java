/*
 * Created on Mar 3, 2007
 *
 * Copyright (c) 2007, the JUNG Project and the Regents of the University 
 * of California
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * http://jung.sourceforge.net/license.txt for a description.
 */
package explorviz.visualization.layout.datastructures.hypergraph.util;

import java.util.*;

import explorviz.visualization.layout.datastructures.hypergraph.Forest;
import explorviz.visualization.layout.datastructures.hypergraph.Tree;

/**
 * Contains static methods for operating on instances of <code>Tree</code>.
 */
public class TreeUtils {
	/**
	 * Returns the roots of this forest.
	 * 
	 * @param <V>
	 *            the vertex type
	 * @param <E>
	 *            the edge type
	 */
	public static <V, E> List<V> getRoots(final Forest<V, E> forest) {
		final List<V> roots = new ArrayList<V>();
		for (final Tree<V, E> tree : forest.getTrees()) {
			roots.add(tree.getRoot());
		}
		return roots;
	}

	/**
	 * Returns the subtree of <code>tree</code> which is rooted at
	 * <code>root</code> as a <code>Forest<V,E></code>. The tree returned is an
	 * independent entity, although it uses the same vertex and edge objects.
	 * 
	 * @param <V>
	 *            the vertex type
	 * @param <E>
	 *            the edge type
	 * @param forest
	 *            the tree whose subtree is to be extracted
	 * @param root
	 *            the root of the subtree to be extracted
	 * @return the subtree of <code>tree</code> which is rooted at
	 *         <code>root</code>
	 * @throws InstantiationException
	 *             if a new tree of the same type cannot be created
	 * @throws IllegalAccessException
	 */
	@SuppressWarnings("unchecked")
	public static <V, E> Tree<V, E> getSubTree(final Forest<V, E> forest, final V root)
			throws InstantiationException, IllegalAccessException {
		if (!forest.containsVertex(root)) {
			throw new IllegalArgumentException(
					"Specified tree does not contain the specified root as a vertex");
		}
		final Forest<V, E> subforest = forest.getClass().newInstance();
		subforest.addVertex(root);
		growSubTree(forest, subforest, root);

		return subforest.getTrees().iterator().next();
	}

	/**
	 * Populates <code>subtree</code> with the subtree of <code>tree</code>
	 * which is rooted at <code>root</code>.
	 * 
	 * @param <V>
	 *            the vertex type
	 * @param <E>
	 *            the edge type
	 * @param tree
	 *            the tree whose subtree is to be extracted
	 * @param subTree
	 *            the tree instance which is to be populated with the subtree of
	 *            <code>tree</code>
	 * @param root
	 *            the root of the subtree to be extracted
	 */
	public static <V, E> void growSubTree(final Forest<V, E> tree, final Forest<V, E> subTree,
			final V root) {
		if (tree.getSuccessorCount(root) > 0) {
			final Collection<E> edges = tree.getOutEdges(root);
			for (final E e : edges) {
				subTree.addEdge(e, tree.getEndpoints(e));
			}
			final Collection<V> kids = tree.getSuccessors(root);
			for (final V kid : kids) {
				growSubTree(tree, subTree, kid);
			}
		}
	}

	/**
	 * Connects <code>subTree</code> to <code>tree</code> by attaching it as a
	 * child of <code>node</code> with edge <code>connectingEdge</code>.
	 * 
	 * @param <V>
	 *            the vertex type
	 * @param <E>
	 *            the edge type
	 * @param tree
	 *            the tree to which <code>subTree</code> is to be added
	 * @param subTree
	 *            the tree which is to be grafted on to <code>tree</code>
	 * @param node
	 *            the parent of <code>subTree</code> in its new position in
	 *            <code>tree</code>
	 * @param connectingEdge
	 *            the edge used to connect <code>subtree</code>'s root as a
	 *            child of <code>node</code>
	 */
	public static <V, E> void addSubTree(final Forest<V, E> tree, final Forest<V, E> subTree,
			final V node, final E connectingEdge) {
		if ((node != null) && !tree.containsVertex(node)) {
			throw new IllegalArgumentException(
					"Specified tree does not contain the specified node as a vertex");
		}
		final V root = subTree.getTrees().iterator().next().getRoot();
		addFromSubTree(tree, subTree, connectingEdge, node, root);
	}

	/**
	 * Adds the trees in <code>source</code> to <code>destination</code>.
	 * <code>source</code> is left unchanged. The vertex and edge objects in
	 * <code>source</code> will also be used in <code>destination</code>, in the
	 * same (structural) roles.
	 * 
	 * @param <V>
	 *            the vertex type
	 * @param <E>
	 *            the edge type
	 * @param destination
	 *            the forest to which the trees in <code>source</code> will be
	 *            added
	 * @param source
	 *            the forest whose trees will be added to
	 *            <code>destination</code> FIXME also note that this is
	 *            redundant with DelegateForest.addTree()
	 * 
	 */
	// public static <V,E> void mergeForests(Forest<V,E> destination,
	// Forest<V,E> source)
	// {
	// for (Tree<V,E> tree : source.getTrees())
	// {
	// V root = tree.getRoot();
	// // FIXME this is not done: addChildrenToForest is not yet complete
	// // also still need to integrate into MST2, etc. (see email thread)
	// // addChildrenToForest(destination, tree, root);
	// for (E e: tree.getOutEdges(root))
	// {
	// V child = tree.getOpposite(root, e);
	// addFromSubTree(destination, source, e, root, child);
	// }
	// }
	// }

	public static <V, E> void addFromSubTree(final Forest<V, E> tree, final Forest<V, E> subTree,
			final E edge, final V parent, final V root) {

		// add edge connecting parent and root to tree
		if ((edge != null) && (parent != null)) {
			tree.addEdge(edge, parent, root);
		} else {
			tree.addVertex(root);
		}

		final Collection<E> outEdges = subTree.getOutEdges(root);
		for (final E e : outEdges) {
			final V opposite = subTree.getOpposite(root, e);
			addFromSubTree(tree, subTree, e, root, opposite);
		}
	}

	// FIXME: not done or integrated yet
	// private static <V,E> void addChildrenToForest(Forest<V,E> forest,
	// Tree<V,E> tree,
	// V subtree_root)
	// {
	// V parent = tree.getPredecessors(subtree_root).iterator().next();
	// for (E e : tree.getOutEdges(subtree_root))
	// {
	// V child = tree.getOpposite(subtree_root, e);
	// addChildrenToForest(forest, tree, child);
	// }
	// }
}
