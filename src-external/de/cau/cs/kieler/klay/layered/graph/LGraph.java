/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 * 
 * Copyright 2010 by
 * + Christian-Albrechts-University of Kiel
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 * 
 * This code is provided under the terms of the Eclipse Public License (EPL).
 * See the file epl-v10.html for the license text.
 */
package de.cau.cs.kieler.klay.layered.graph;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import de.cau.cs.kieler.core.math.KVector;
import de.cau.cs.kieler.klay.layered.properties.Properties;

/**
 * A layered graph has a set of layers that contain the nodes, as well as a
 * list of nodes that are not yet assigned to a layer. Layout algorithms are
 * required to layout the graph from left to right. If another layout direction
 * is desired, it can be obtained by pre-processing and post-processing the graph.
 * In contrast to the KGraph structure, the LGraph is not EMF-based, but plain Java.
 * It is optimized for being processed by a layer-based layout algorithm.
 * 
 * @author msp
 * @kieler.design proposed by msp
 * @kieler.rating yellow 2013-03-22 review KI-35 by chsch, grh
 */
public final class LGraph extends LGraphElement implements Iterable<Layer> {
    
    /** the serial version UID. */
    private static final long serialVersionUID = -8006835373897072852L;
    
    /** the total size of the drawing, without offset. */
    private final KVector size = new KVector();
    /** the graph's insets. */
    private final LInsets insets = new LInsets(0, 0, 0, 0);
    /** the offset to be added to all positions. */
    private final KVector offset = new KVector();
    /** nodes that are not currently part of a layer. */
    private final List<LNode> layerlessNodes = new LinkedList<LNode>();
    /** the layers of the layered graph. */
    private final List<Layer> layers = new LinkedList<Layer>();
    /** the hash code counter used to determine hash codes for new elements. */
    private final HashCodeCounter hashCodeCounter;
    
    /**
     * Create a graph with a new hash code counter. This constructor should only be used when
     * the content does not matter, e.g. when the graph remains empty.
     */
    public LGraph() {
        super(new HashCodeCounter());
        this.hashCodeCounter = new HashCodeCounter();
    }
    
    /**
     * Create an LGraph with given hash code counter.
     * 
     * @param counter the counter used to find a unique but predictable hash code.
     */
    public LGraph(final HashCodeCounter counter) {
        super(counter);
        this.hashCodeCounter = counter;
    }
    
    /**
     * Create an LGraph and copy the hash code counter of an existing one.
     * 
     * @param originalGraph the graph from which to copy the hash code counter
     */
    public LGraph(final LGraph originalGraph) {
        super(originalGraph.hashCodeCounter);
        this.hashCodeCounter = originalGraph.hashCodeCounter;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        if (layers.isEmpty()) {
            return "G-unlayered" + layerlessNodes.toString();
        } else if (layerlessNodes.isEmpty()) {
            return "G-layered" + layers.toString();
        }
        return "G[layerless" + layerlessNodes.toString() + ", layers" + layers.toString() + "]";
    }
    
    /**
     * Returns the hash code counter for all graph elements, only visible by other package members.
     * 
     * @return the counter used to find a unique but predictable hash code.
     */
    HashCodeCounter hashCodeCounter() {
        return hashCodeCounter;
    }

    /**
     * Returns the size of the graph, that is the bounding box that covers the
     * whole drawing. The size does not include insets or anything. Modifying the
     * returned value changes the size of the graph.
     * 
     * @return the size of the layered graph; modify to change the graph size.
     */
    public KVector getSize() {
        return size;
    }
    
    /**
     * Returns the graph's size including any borders. If the graph represents a
     * hierarchical node, the returned size represents the node's size. The returned
     * size can be modified at will without having any influence on the graph's size
     * or the actual size returned on the next method call.
     * 
     * @return the graph's size including borders.
     */
    public KVector getActualSize() {
        float borderSpacing = getProperty(Properties.BORDER_SPACING);
        return new KVector(
                size.x + insets.left + insets.right + (2 * borderSpacing),
                size.y + insets.top + insets.bottom + (2 * borderSpacing));
    }
    
    /**
     * Returns the insets of the graph. The insets determine the amount of space between
     * the content area and the graph's actual border. Modifying the returned value
     * changes the insets.
     * 
     * @return the insets; modify to change the graph's insets.
     */
    public LInsets getInsets() {
        return insets;
    }

    /**
     * Returns the offset for the graph, that is a coordinate vector that has
     * to be added to all position values of nodes and edges. It is usually used
     * to reserve some space in the content area for additional edge routing.
     * 
     * <b>Note:</b> Since many different parts of the algorithm may contribute to
     * the offset, never set the offset to an absolute value! Rather, only add to
     * the offset!
     * 
     * @return the offset of the layered graph
     */
    public KVector getOffset() {
        return offset;
    }
    
    /**
     * Returns the list of nodes that are not currently assigned to a layer.
     * When creating a graph, put the nodes here.
     * 
     * @return the layerless nodes.
     */
    public List<LNode> getLayerlessNodes() {
        return layerlessNodes;
    }

    /**
     * Returns the list of layers of the graph. Layers are created automatically by the layout
     * algorithm, so this list must not be touched when the graph is created.
     * 
     * @return the layers
     */
    public List<Layer> getLayers() {
        return layers;
    }

    /**
     * Returns an iterator over the layers.
     * 
     * @return an iterator for the layers of this layered graph
     */
    public Iterator<Layer> iterator() {
        return layers.iterator();
    }
    
}
