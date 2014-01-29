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
import de.cau.cs.kieler.kiml.options.Alignment;
import de.cau.cs.kieler.kiml.options.LayoutOptions;

/**
 * A layer in a layered graph. A layer contains a list of nodes, which are
 * drawn in one column.
 *
 * @author msp
 * @kieler.design proposed by msp
 * @kieler.rating yellow 2013-03-22 review KI-35 by chsch, grh
 */
public final class Layer extends LGraphElement implements Iterable<LNode> {

    /** the serial version UID. */
    private static final long serialVersionUID = 5760328884701318753L;

    /** the owning layered graph. */
    private final LGraph owner;

    /** the size of the layer as drawn horizontally. */
    private final KVector size = new KVector();
    /** the nodes of the layer. */
    private final List<LNode> nodes = new LinkedList<LNode>();
    
    /**
     * Creates a layer for the given layered graph. The layer is not added to the
     * given graph yet; the graph is just saved as the layer's owner.
     * 
     * @param graph the owning layered graph
     */
    public Layer(final LGraph graph) {
        super(graph.hashCodeCounter());
        this.owner = graph;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "L_" + getIndex() + nodes.toString();
    }
    
    /**
     * Returns the size of the layer, that is the height of the stacked nodes
     * and the maximal width of the nodes.
     * 
     * @return the size of the layer
     */
    public KVector getSize() {
        return size;
    }

    /**
     * Returns the list of nodes. The order of nodes in this list corresponds to
     * the order in which they are drawn inside the layer: the first node is
     * drawn topmost. This order is affected during crossing minimization.
     * 
     * @return the nodes of the layer
     */
    public List<LNode> getNodes() {
        return nodes;
    }

    /**
     * Returns an iterator over the contained nodes.
     * 
     * @return an iterator for the nodes of this layer
     */
    public Iterator<LNode> iterator() {
        return nodes.iterator();
    }
    
    /**
     * Returns the layered graph that owns this layer.
     * 
     * @return the owner
     */
    public LGraph getGraph() {
        return owner;
    }
    
    /**
     * Returns the index of this layer in the global list of layers. Note that
     * this method has linear running time in the number of layers, so use it
     * with caution.
     * 
     * @return the index of this layer
     */
    public int getIndex() {
        return owner.getLayers().indexOf(this);
    }
    
    /**
     * Determines a horizontal placement for all nodes of this layer. The size
     * of the layer is assumed to be already set to the maximal width of the
     * contained nodes. (usually done during node placement)
     * 
     * @param xoffset horizontal offset for layer placement
     */
    public void placeNodes(final double xoffset) {
        // determine maximal left and right margin
        double maxLeftMargin = 0, maxRightMargin = 0;
        for (LNode node : nodes) {
            maxLeftMargin = Math.max(maxLeftMargin, node.getMargin().left);
            maxRightMargin = Math.max(maxRightMargin, node.getMargin().right);
        }

        // CHECKSTYLEOFF MagicNumber
        for (LNode node : nodes) {
            Alignment alignment = node.getProperty(LayoutOptions.ALIGNMENT);
            double ratio;
            switch (alignment) {
            case LEFT:
                ratio = 0.0;
                break;
            case RIGHT:
                ratio = 1.0;
                break;
            case CENTER:
                ratio = 0.5;
                break;
            default:
                // determine the number of input and output ports for the node
                int inports = 0, outports = 0;
                for (LPort port : node.getPorts()) {
                    if (!port.getIncomingEdges().isEmpty()) {
                        inports++;
                    }
                    
                    if (!port.getOutgoingEdges().isEmpty()) {
                        outports++;
                    }
                }
                
                // calculate node placement based on the port numbers
                if (inports + outports == 0) {
                    ratio = 0.5;
                } else {
                    ratio = (double) outports / (inports + outports);
                }
            }
            
            // align nodes to the layer's maximal margin
            double nodeSize = node.getSize().x;
            double xpos = (size.x - nodeSize) * ratio;
            if (ratio > 0.5) {
                xpos -= maxRightMargin * 2 * (ratio - 0.5);
            } else if (ratio < 0.5) {
                xpos += maxLeftMargin * 2 * (0.5 - ratio);
            }
            
            // consider the node's individual margin
            double leftMargin = node.getMargin().left;
            if (xpos < leftMargin) {
                xpos = leftMargin;
            }
            double rightMargin = node.getMargin().right;
            if (xpos > size.x - rightMargin - nodeSize) {
                xpos = size.x - rightMargin - nodeSize;
            }
            
            node.getPosition().x = xoffset + xpos;
        }
    }

}
