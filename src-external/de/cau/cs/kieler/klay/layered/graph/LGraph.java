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

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import de.cau.cs.kieler.core.math.KVector;
import de.cau.cs.kieler.klay.layered.properties.NodeType;
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
     * 
     * @return the layerless nodes.
     */
    public List<LNode> getLayerlessNodes() {
        return layerlessNodes;
    }

    /**
     * Returns the list of layers of the graph.
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
    
    /**
     * Resizes the graph such that the next call to {@link #getActualSize()} will return the size
     * given as a parameter here.
     * 
     * @param actualSize
     *            the graph's new actual size.
     * @throws IllegalArgumentException
     *             if the new actual size is lower than the insets and border spacing required.
     */
    public void applyActualSize(final KVector actualSize) {
        float borderSpacing = getProperty(Properties.BORDER_SPACING);
        
        // error checking
        if (actualSize.x < insets.left + insets.right + 2 * borderSpacing) {
            throw new IllegalArgumentException("width lower than insets and border spacing.");
        }

        if (actualSize.y < insets.top + insets.bottom + 2 * borderSpacing) {
            throw new IllegalArgumentException("height lower than insets and border spacing.");
        }
        
        // apply new size
        size.x = actualSize.x - insets.left - insets.right - 2 * borderSpacing;
        size.y = actualSize.y - insets.top - insets.bottom - 2 * borderSpacing;
    }

    // /////////////////////////////////////////////////////////////////////////////
    // Debug
    
    /**
     * Outputs a representation of this graph in dot format to the given writer. The
     * following conventions are used:
     * <ul>
     *   <li>Standard nodes are drawn as rectangles.</li>
     *   <li>Dummy nodes are drawn as ellipses.</li>
     *   <li>Nodes have a color that depends on their node type.
     *     (yellow for {@code LONG_EDGE}, turquoise for {@code ODD_PORT_SIDE},
     *     dark blue for {@code NORTH_SOUTH_PORT})</li>
     * </ul>
     * 
     * @param writer the writer to output the graph to. An attempt is made to close the
     *               writer when finished.
     * @throws IOException if anything goes wrong with the writer.
     */
    public void writeDotGraph(final Writer writer) throws IOException {
        // Begin the digraph
        writer.write("digraph {\n");
        
        // Digraph options
        writer.write("    rankdir=LR;\n");
        
        // Write layerless nodes and edges
        writeLayer(writer, -1, layerlessNodes);
        
        // Go through the layers
        int layerNumber = -1;
        for (Layer layer : layers) {
            layerNumber++;
            
            // Write the nodes and edges
            writeLayer(writer, layerNumber, layer.getNodes());
        }
        
        // Close the digraph. And the writer.
        writer.write("}\n");
        writer.close();
    }
    
    /**
     * Writes the given list of nodes and their edges.
     * 
     * @param writer writer to write to.
     * @param layerNumber the layer number. {@code -1} for layerless nodes.
     * @param nodes the nodes in the layer.
     * @throws IOException if anything goes wrong with the writer.
     */
    private void writeLayer(final Writer writer, final int layerNumber, final List<LNode> nodes)
            throws IOException {
        
        if (nodes.isEmpty()) {
            return;
        }
        
        // Go through the layer's nodes
        int nodeNumber = -1;
        for (LNode node : nodes) {
            nodeNumber++;
            
            // The node's name in the output is its hash code (unique!)
            writer.write("        " + node.hashCode());
            
            // Options time!
            StringBuffer options = new StringBuffer();
            
            // Label
            options.append("label=\"");
            if (node.getProperty(Properties.NODE_TYPE) == NodeType.NORMAL) {
                // Normal nodes display their name, if any
                if (node.getName() != null) {
                    options.append(node.getName().replace("\"", "\\\"") + " ");
                }
            } else {
                // Dummy nodes show their name (if set), or their node ID
                if (node.getName() != null) {
                    options.append(node.getName().replace("\"", "\\\"") + " ");
                } else {
                    options.append("n_" + node.id + " ");
                }
                if (node.getProperty(Properties.NODE_TYPE) == NodeType.NORTH_SOUTH_PORT) {
                    Object origin = node.getProperty(Properties.ORIGIN);
                    if (origin instanceof LNode) {
                        options.append("(" + ((LNode) origin).toString() + ")");
                    }
                }
            }
            options.append("(" + layerNumber + "," + nodeNumber + ")\",");
            
            // Node type
            if (node.getProperty(Properties.NODE_TYPE).equals(NodeType.NORMAL)) {
                options.append("shape=box,");
            } else {
                options.append("style=\"rounded,filled\",");
                
                String color = node.getProperty(Properties.NODE_TYPE).getColor();
                if (color != null) {
                    options.append("color=\"" + color + "\",");
                }
            }
            
            // Print options, if any
            options.deleteCharAt(options.length() - 1);
            if (options.length() > 0) {
                writer.write("[" + options + "]");
            }
            
            // End the node line
            writer.write(";\n");
        }
        
        // Write the edges
        for (LNode node : nodes) {
            // Go through all edges and output those that have this node as their source
            for (LPort port : node.getPorts()) {
                for (LEdge edge : port.getOutgoingEdges()) {
                    writer.write("    " + node.hashCode() + " -> "
                            + edge.getTarget().getNode().hashCode());
                    writer.write(";\n");
                }
            }
        }
    }
}
