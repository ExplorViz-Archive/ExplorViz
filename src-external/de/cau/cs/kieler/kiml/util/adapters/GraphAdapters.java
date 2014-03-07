/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 * 
 * Copyright 2014 by
 * + Christian-Albrechts-University of Kiel
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 * 
 * This code is provided under the terms of the Eclipse Public License (EPL).
 * See the file epl-v10.html for the license text.
 */
package de.cau.cs.kieler.kiml.util.adapters;

import de.cau.cs.kieler.core.math.KVector;
import de.cau.cs.kieler.core.properties.IProperty;
import de.cau.cs.kieler.kiml.options.LabelSide;
import de.cau.cs.kieler.kiml.options.PortSide;
import de.cau.cs.kieler.kiml.util.nodespacing.Spacing.Insets;
import de.cau.cs.kieler.kiml.util.nodespacing.Spacing.Margins;

/**
 * @author uru
 */
public interface GraphAdapters {

    /**
     * A generic adapter for graph elements that have a position and dimension. Can be used, for
     * instance, to implement node, port, and label adapters.
     * 
     * <h2>Remark</h2> When using these adapters keep in mind to explicitly use the <emph>set</emph>
     * methods, e.g. for insets. Some API's (e.g. the {@link KVector}) allow to directly set their
     * values, for instance {@code node.getPosition().x = 3}. However, as {@code node.getPosition()}
     * most likely returns an intermediate object, this change will never be applied to the original
     * graph's element.
     * 
     * @param <T>
     *            the type of the underlying graph element.
     */
    public interface GraphElementAdapter<T> {

        /**
         * @return the size of the graph element, the {@code x} value of the {@link KVector} defines
         *         the width, the {@code y} value the height.
         */
        KVector getSize();

        /**
         * @param size
         *            the new size to be set for the graph element.
         */
        void setSize(final KVector size);

        /**
         * @return the current position of the graph element.
         */
        KVector getPosition();

        /**
         * @param pos
         *            the new position of the graph element.
         */
        void setPosition(final KVector pos);

        /**
         * @param prop
         *            the property to retrieve.
         * @param <P>
         *            the contained type of the property.
         * @return the value of the requested property.
         * @see de.cau.cs.kieler.kiml.options.LayoutOptions
         * 
         */
        <P> P getProperty(final IProperty<P> prop);
    }

    /**
     * Adapter for graph element, provides children of the graph.
     */
    public interface GraphAdapter<T> extends GraphElementAdapter<T> {

        /**
         * @return all child nodes of this graph wrapped in an {@link NodeAdapter}.
         */
        Iterable<NodeAdapter<?>> getNodes();
    }

    /**
     * Adapter for a node, provides labels, ports, and insets.
     */
    public interface NodeAdapter<T> extends GraphElementAdapter<T> {

        /**
         * @return the labels of the node wrapped in adapters.
         */
        Iterable<LabelAdapter<?>> getLabels();

        /**
         * @return the ports of the node wrapped in adapter.
         */
        Iterable<PortAdapter<?>> getPorts();
        
        /**
         * @return a collection of the port's incoming edges wrapped in an adapter.
         */
        Iterable<EdgeAdapter<?>> getIncomingEdges();

        /**
         * @return a collection of the port's outgoing edges wrapped in an adapter.
         */
        Iterable<EdgeAdapter<?>> getOutgoingEdges();
 
        /**
         * Whether the node an is a compound node or not, i.e if it has child nodes. This might
         * influence certain layout decisions, such as where to place inside port labels so that
         * they don't overlap edges.
         * 
         * @return {@code true} if it is a compound node.
         */
        boolean isCompoundNode();

        /**
         * Returns the node's insets. The insets describe the area inside the node that is used by
         * ports, port labels, and node labels.
         * 
         * @return the node's insets.
         */
        Insets getInsets();

        /**
         * @param insets
         *            sets the new insets of this node.
         */
        void setInsets(final Insets insets);

        /**
         * Returns the node's margin. The margin is the space around the node that is to be reserved
         * for ports and labels.
         * 
         * @return the node's margin.
         */
        Margins getMargin();

        /**
         * @param margin
         *            the new margin to be set.
         */
        void setMargin(final Margins margin);
    }

    /**
     * Adapter for a port element, provides access to the port's side, margin, and labels.
     */
    public interface PortAdapter<T> extends GraphElementAdapter<T> {

        /**
         * @return the port's side.
         */
        PortSide getSide();

        /**
         * @return the port's labels wrapped in adapters.
         */
        Iterable<LabelAdapter<?>> getLabels();

        /**
         * Returns the port's margin. The margin is the space around the node that is to be reserved
         * for ports and labels.
         * 
         * @return the ports's margin.
         */
        Margins getMargin();

        /**
         * @param margin
         *            the new margin to be set.
         */
        void setMargin(final Margins margin);

        /**
         * @return a collection of the port's incoming edges wrapped in an adapter.
         */
        Iterable<EdgeAdapter<?>> getIncomingEdges();

        /**
         * @return a collection of the port's outgoing edges wrapped in an adapter.
         */
        Iterable<EdgeAdapter<?>> getOutgoingEdges();
    }

    /**
     * Adapter for a label.
     */
    public interface LabelAdapter<T> extends GraphElementAdapter<T> {

        /**
         * @return the side of the label.
         */
        LabelSide getSide();

    }

    /**
     * Adapter for an edge.
     */
    public interface EdgeAdapter<T> {

        /**
         * @return the edge's labels wrapped in an adapter.
         */
        Iterable<LabelAdapter<?>> getLabels();

    }
}
