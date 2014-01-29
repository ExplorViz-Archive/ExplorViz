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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;

import de.cau.cs.kieler.core.math.KVector;
import de.cau.cs.kieler.kiml.options.PortSide;
import de.cau.cs.kieler.klay.layered.properties.PortType;
import de.cau.cs.kieler.klay.layered.properties.Properties;

/**
 * A node in a layered graph.
 *
 * @author msp
 * @kieler.design proposed by msp
 * @kieler.rating yellow 2013-03-22 review KI-35 by chsch, grh
 */
public final class LNode extends LShape {
    
    /** the serial version UID. */
    private static final long serialVersionUID = -4272570519129722541L;
    
    /** the owning layer. */
    private Layer owner;
    /** the ports of the node. */
    private final List<LPort> ports = new LinkedList<LPort>();
    /** this node's labels. */
    private final List<LLabel> labels = new LinkedList<LLabel>();
    /** the margin area around this node. */
    private final LInsets margin = new LInsets();
    /** the insets inside this node, usually reserved for port and label placement. */
    private final LInsets insets = new LInsets();
    
    /**
     * Creates a node.
     * 
     * @param graph the graph for which the node is created 
     */
    public LNode(final LGraph graph) {
        super(graph);
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return "n_" + getDesignation();
    }
    
    /**
     * Returns the name of the node. The name is derived from the text of the first label, if any.
     * 
     * @return the name, or {@code null}
     */
    public String getName() {
        if (!labels.isEmpty()) {
            return labels.get(0).getText();
        }
        return null;
    }
    
    /**
     * Returns the node's designation. The designation is either the name if that is not {@code null},
     * or the node's ID otherwise.
     * 
     * @return the node's designation.
     */
    public String getDesignation() {
        String name = getName();
        if (name == null) {
            return Integer.toString(id);
        } else {
            return name;
        }
    }

    /**
     * Returns the layer that owns this node.
     * 
     * @return the owning layer
     */
    public Layer getLayer() {
        return owner;
    }

    /**
     * Sets the owning layer and adds itself to the end of the layer's list of nodes.
     * If the node was previously in another layer, it is removed from
     * that layer's list of nodes. Be careful not to use this method while
     * iterating through the nodes list of the old layer nor of the new layer,
     * since that could lead to {@link java.util.ConcurrentModificationException}s.
     * 
     * @param layer the owner to set
     */
    public void setLayer(final Layer layer) {
        if (owner != null) {
            owner.getNodes().remove(this);
        }
        
        this.owner = layer;
        
        if (owner != null) {
            owner.getNodes().add(this);
        }
    }
    
    /**
     * Sets the owning layer and adds itself to the layer's list of nodes at the
     * specified position. If the node was previously in another layer, it is
     * removed from that layer's list of nodes. Be careful not to use this method
     * while iterating through the nodes list of the old layer nor of the new layer,
     * since that could lead to {@link java.util.ConcurrentModificationException}s.
     * 
     * @param index where the node should be inserted in the layer. Must be {@code >= 0}
     *              and {@code <= layer.getNodes().size()}.
     * @param layer the owner to set.
     */
    public void setLayer(final int index, final Layer layer) {
        if (layer != null && (index < 0 || index > layer.getNodes().size())) {
            throw new IllegalArgumentException("index must be >= 0 and <= layer node count");
        }
        
        if (owner != null) {
            owner.getNodes().remove(this);
        }
        
        this.owner = layer;
        
        if (owner != null) {
            owner.getNodes().add(index, this);
        }
    }

    /**
     * Returns the list of ports of this node. Note that all edges are connected to specific
     * ports, even if the original diagram does not have any ports.
     * Before the crossing minimization phase has passed, the port order in this list is
     * arbitrary. After crossing minimization the order of ports corresponds to the clockwise
     * order in which they are drawn, starting with the north side.
     * Hence the order is
     * <ul>
     *   <li>north ports from left to right,</li>
     *   <li>east ports from top to bottom,</li>
     *   <li>south ports from right to left,</li>
     *   <li>west port from bottom to top.</li>
     * </ul>
     * 
     * @return the ports of this node
     */
    public List<LPort> getPorts() {
        return ports;
    }
    
    /**
     * Returns an iterable for all ports of given type.
     * 
     * @param portType a port type
     * @return an iterable for the ports of given type
     */
    public Iterable<LPort> getPorts(final PortType portType) {
        switch (portType) {
        case INPUT:
            return Iterables.filter(ports, LPort.INPUT_PREDICATE);
        case OUTPUT:
            return Iterables.filter(ports, LPort.OUTPUT_PREDICATE);
        default:
            return Collections.emptyList();
        }
    }
    
    /**
     * Returns an iterable for all ports of given side.
     * 
     * @param side a port side
     * @return an iterable for the ports of given side
     */
    public Iterable<LPort> getPorts(final PortSide side) {
        switch (side) {
        case NORTH:
            return Iterables.filter(ports, LPort.NORTH_PREDICATE);
        case EAST:
            return Iterables.filter(ports, LPort.EAST_PREDICATE);
        case SOUTH:
            return Iterables.filter(ports, LPort.SOUTH_PREDICATE);
        case WEST:
            return Iterables.filter(ports, LPort.WEST_PREDICATE);
        default:
            return Collections.emptyList();
        }
    }
    
    /**
     * Returns an iterable for all ports of a given type and side.
     * 
     * @param portType a port type.
     * @param side a port side.
     * @return an iterable for the ports of the given type and side.
     */
    public Iterable<LPort> getPorts(final PortType portType, final PortSide side) {
        Predicate<LPort> typePredicate = null;
        switch (portType) {
        case INPUT:
            typePredicate = LPort.INPUT_PREDICATE;
            break;
        case OUTPUT:
            typePredicate = LPort.OUTPUT_PREDICATE;
            break;
        }
        
        Predicate<LPort> sidePredicate = null;
        switch (side) {
        case NORTH:
            sidePredicate = LPort.NORTH_PREDICATE;
            break;
        case EAST:
            sidePredicate = LPort.EAST_PREDICATE;
            break;
        case SOUTH:
            sidePredicate = LPort.SOUTH_PREDICATE;
            break;
        case WEST:
            sidePredicate = LPort.WEST_PREDICATE;
            break;
        }
        
        if (typePredicate != null && sidePredicate != null) {
            return Iterables.filter(ports, Predicates.and(typePredicate, sidePredicate));
        } else {
            return Collections.emptyList();
        }
    }
    
    /**
     * Returns an iterable for all inomcing edges.
     * 
     * @return an iterable for all incoming edges.
     */
    public Iterable<LEdge> getIncomingEdges() {
        List<Iterable<LEdge>> iterables = new LinkedList<Iterable<LEdge>>();
        for (LPort port : ports) {
            iterables.add(port.getIncomingEdges());
        }
        
        return Iterables.concat(iterables);
    }
    
    /**
     * Returns an iterable for all outgoing edges.
     * 
     * @return an iterable for all outgoing edges.
     */
    public Iterable<LEdge> getOutgoingEdges() {
        List<Iterable<LEdge>> iterables = new LinkedList<Iterable<LEdge>>();
        for (LPort port : ports) {
            iterables.add(port.getOutgoingEdges());
        }
        
        return Iterables.concat(iterables);
    }
    
    /**
     * Returns an iterable for all connected edges, both incoming and outgoing.
     * 
     * @return an iterable for all connected edges.
     */
    public Iterable<LEdge> getConnectedEdges() {
        List<Iterable<LEdge>> iterables = new LinkedList<Iterable<LEdge>>();
        for (LPort port : ports) {
            iterables.add(port.getConnectedEdges());
        }
        
        return Iterables.concat(iterables);
    }
    
    /**
     * Returns this node's labels.
     * 
     * @return this node's labels.
     */
    public List<LLabel> getLabels() {
        return labels;
    }
    
    /**
     * Returns the node's margin. The margin is the space around the node that is to be reserved
     * for ports and labels.
     * 
     * <p>The margin is not automatically updated. Rather, the margin has to be calculated once
     * the port and label positions are fixed. Usually this is right before the node placement
     * starts.</p>
     *  
     * @return the node's margin. May be modified.
     */
    public LInsets getMargin() {
        return margin;
    }
    
    /**
     * Returns the node's insets. The insets describe the area inside the node that is used by
     * ports, port labels, and node labels.
     * 
     * <p>The insets are not automatically updated. Rather, the insets have to be calculated
     * once the port and label positions are fixed. Usually this is right before node placement
     * starts.</p>
     * 
     * @return the node's insets. May be modified.
     */
    public LInsets getInsets() {
        return insets;
    }
    
    /**
     * Returns the index of the node in the containing layer's list of nodes.
     * Note that this method has linear running time in the number of nodes,
     * so use it with caution.
     * 
     * @return the index of this node, or -1 if the node has no owner
     */
    public int getIndex() {
        if (owner == null) {
            return -1;
        } else {
            return owner.getNodes().indexOf(this);
        }
    }
    
    /**
     * Converts the position of this node from coordinates relative to the parent node's
     * border to coordinates relative to that node's content area. The content area is the
     * parent node border minus insets minus border spacing minus offset.
     * 
     * @param horizontal if {@code true}, the x coordinate will be translated.
     * @param vertical if {@code true}, the y coordinate will be translated.
     * @throws IllegalStateException if the node is not assigned to a layer in a layered graph.
     */
    public void borderToContentAreaCoordinates(final boolean horizontal, final boolean vertical) {
        if (owner == null || owner.getGraph() == null) {
            throw new IllegalStateException("node is not assigned to a layer in a graph.");
        }
        
        LGraph graph = owner.getGraph();
        
        LInsets graphInsets = graph.getInsets();
        float borderSpacing = graph.getProperty(Properties.BORDER_SPACING);
        KVector offset = graph.getOffset();
        KVector pos = getPosition();
        
        if (horizontal) {
            pos.x = pos.x - graphInsets.left - borderSpacing - offset.x;
        }
        
        if (vertical) {
            pos.y = pos.y - graphInsets.top - borderSpacing - offset.y;
        }
    }

    /**
     * Returns the position of this node's interactive reference point. This position depends on the
     * graph's {@link Properties#INTERACTIVE_REFERENCE_POINT} property. It determines on which
     * basis node positions are compared with each other in interactive layout phases.
     * 
     * @param graph the layered graph.
     * @return the node's anchor point position.
     */
    public KVector getInteractiveReferencePoint(final LGraph graph) {
        switch (graph.getProperty(Properties.INTERACTIVE_REFERENCE_POINT)) {
        case CENTER:
            KVector nodePos = getPosition();
            KVector nodeSize = getSize();
            
            return new KVector(nodePos.x + nodeSize.x / 2.0, nodePos.y + nodeSize.y / 2.0);
        
        case TOP_LEFT:
            return new KVector(getPosition());
        
        default:
            // This shouldn't happen.
            return null;
        }
    }

}
