/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 * 
 * Copyright 2011 by
 * + Christian-Albrechts-University of Kiel
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 * 
 * This code is provided under the terms of the Eclipse Public License (EPL).
 * See the file epl-v10.html for the license text.
 */
package de.cau.cs.kieler.klay.layered.intermediate;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import de.cau.cs.kieler.core.alg.IKielerProgressMonitor;
import de.cau.cs.kieler.kiml.options.LayoutOptions;
import de.cau.cs.kieler.kiml.options.PortConstraints;
import de.cau.cs.kieler.kiml.options.PortSide;
import de.cau.cs.kieler.klay.layered.ILayoutProcessor;
import de.cau.cs.kieler.klay.layered.graph.LEdge;
import de.cau.cs.kieler.klay.layered.graph.LNode;
import de.cau.cs.kieler.klay.layered.graph.LPort;
import de.cau.cs.kieler.klay.layered.graph.Layer;
import de.cau.cs.kieler.klay.layered.graph.LGraph;
import de.cau.cs.kieler.klay.layered.properties.NodeType;
import de.cau.cs.kieler.klay.layered.properties.PortType;
import de.cau.cs.kieler.klay.layered.properties.Properties;

/**
 * Inserts dummy nodes to cope with inverted ports.
 * 
 * <p>The problem is with edges coming from the left of a node being connected to a
 * port that's on its right side, or the other way around. Let a node of that kind
 * be in layer {@code i}. This processor now takes the offending edge and connects
 * it to a new dummy node, also in layer {@code i}. Finally, the dummy is connected
 * with the offending port. This means that once one of these cases occurs in the
 * graph, the layering is not proper anymore.</p>
 * 
 * <p>The dummy nodes are decorated with a
 * {@link de.cau.cs.kieler.klay.layered.properties.Properties#NODE_TYPE} property. They
 * are treated just like ordinary {@link de.cau.cs.kieler.klay.layered.properties.NodeType#LONG_EDGE}
 * dummy nodes</p>
 * 
 * <p>This processor supports self-loops by not doing anything about them. That is, no
 * dummy nodes are created for edges whose source and target node are identical.</p>
 * 
 * <p>Note: the following phases must support in-layer connections for this to work.</p>
 * 
 * <dl>
 *   <dt>Precondition:</dt><dd>a layered graph; nodes have fixed port sides.</dd>
 *   <dt>Postcondition:</dt><dd>dummy nodes have been inserted for edges connected to
 *     ports on odd sides; the graph may contain new in-layer connections.</dd>
 *   <dt>Slots:</dt><dd>Before phase 3.</dd>
 *   <dt>Same-slot dependencies:</dt><dd>{@link PortSideProcessor}</dd>
 * </dl>
 * 
 * @see PortSideProcessor
 * @author cds
 * @kieler.design 2012-08-10 chsch grh
 * @kieler.rating proposed yellow by msp
 */
public final class InvertedPortProcessor implements ILayoutProcessor {

    /**
     * {@inheritDoc}
     */
    public void process(final LGraph layeredGraph, final IKielerProgressMonitor monitor) {
        monitor.begin("Inverted port preprocessing", 1);
        
        // Retrieve the layers in the graph
        List<Layer> layers = layeredGraph.getLayers();
        
        // Iterate through the layers and for each layer create a list of dummy nodes
        // that were created, but not yet assigned to the layer (to avoid concurrent
        // modification exceptions)
        ListIterator<Layer> layerIterator = layers.listIterator();
        Layer currentLayer = null;
        List<LNode> unassignedNodes = new LinkedList<LNode>();
        
        while (layerIterator.hasNext()) {
            // Find the current, previous and next layer. If this layer is the last one,
            // use the postfix layer as the next layer
            Layer previousLayer = currentLayer;
            currentLayer = layerIterator.next();
            
            // If the last layer had unassigned nodes, assign them now and clear the list
            for (LNode node : unassignedNodes) {
                node.setLayer(previousLayer);
            }
            unassignedNodes.clear();
            
            // Iterate through the layer's nodes
            for (LNode node : currentLayer) {
                // Skip dummy nodes
                if (!node.getProperty(Properties.NODE_TYPE).equals(NodeType.NORMAL)
                        || node.getProperty(Properties.NODE_TYPE).equals(NodeType.BIG_NODE)) {
                    continue;
                }
                
                // Skip nodes whose port sides are not fixed (because in that case, the odd
                // port side problem won't appear)
                if (!node.getProperty(LayoutOptions.PORT_CONSTRAINTS).isSideFixed()) {
                    continue;
                }
                
                // Look for input ports on the right side
                for (LPort port : node.getPorts(PortType.INPUT, PortSide.EAST)) {
                    // For every edge going into this port, insert dummy nodes (do this using
                    // a copy of the current list of edges, since the edges are modified when
                    // dummy nodes are created)
                    List<LEdge> edges = port.getIncomingEdges();
                    LEdge[] edgeArray = edges.toArray(new LEdge[edges.size()]);
                    
                    for (LEdge edge : edgeArray) {
                        createEastPortSideDummies(layeredGraph, port, edge, unassignedNodes);
                    }
                }
                
                // Look for ports on the left side connected to edges going to higher layers
                for (LPort port : node.getPorts(PortType.OUTPUT, PortSide.WEST)) {
                    // For every edge going out of this port, insert dummy nodes (do this using
                    // a copy of the current list of edges, since the edges are modified when
                    // dummy nodes are created)
                    List<LEdge> edges = port.getOutgoingEdges();
                    LEdge[] edgeArray = edges.toArray(new LEdge[edges.size()]);
                    
                    for (LEdge edge : edgeArray) {
                        createWestPortSideDummies(layeredGraph, port, edge, unassignedNodes);
                    }
                }
            }
        }
        
        // There may be unassigned nodes left
        for (LNode node : unassignedNodes) {
            node.setLayer(currentLayer);
        }
        
        monitor.done();
    }

    /**
     * Creates the necessary dummy nodes for an input port on the east side of a node,
     * provided that the edge connects two different nodes.
     * 
     * @param layeredGraph the layered graph.
     * @param eastwardPort the offending port.
     * @param edge the edge connected to the port.
     * @param layerNodeList list of unassigned nodes belonging to the layer of the node the
     *                      port belongs to. The new dummy node is added to this list and
     *                      must be assigned to the layer later.
     */
    private void createEastPortSideDummies(final LGraph layeredGraph, final LPort eastwardPort,
            final LEdge edge, final List<LNode> layerNodeList) {
        
        if (edge.getSource().getNode() == eastwardPort.getNode()) {
            return;
        }
        
        // Dummy node in the same layer
        LNode dummy = new LNode(layeredGraph);
        dummy.setProperty(Properties.ORIGIN, edge);
        dummy.setProperty(Properties.NODE_TYPE,
                NodeType.LONG_EDGE);
        dummy.setProperty(LayoutOptions.PORT_CONSTRAINTS, PortConstraints.FIXED_POS);
        layerNodeList.add(dummy);
        
        LPort dummyInput = new LPort(layeredGraph);
        dummyInput.setNode(dummy);
        dummyInput.setSide(PortSide.WEST);
        
        LPort dummyOutput = new LPort(layeredGraph);
        dummyOutput.setNode(dummy);
        dummyOutput.setSide(PortSide.EAST);
        
        // Reroute the original edge
        edge.setTarget(dummyInput);
        
        // Connect the dummy with the original port
        LEdge dummyEdge = new LEdge(layeredGraph);
        dummyEdge.copyProperties(edge);
        dummyEdge.setSource(dummyOutput);
        dummyEdge.setTarget(eastwardPort);
        
        // Set LONG_EDGE_SOURCE and LONG_EDGE_TARGET properties on the LONG_EDGE dummy
        setLongEdgeSourceAndTarget(dummy, dummyInput, dummyOutput, eastwardPort);
    }

    /**
     * Creates the necessary dummy nodes for an output port on the west side of a node,
     * provided that the edge connects two different nodes.
     * 
     * @param layeredGraph the layered graph
     * @param westwardPort the offending port.
     * @param edge the edge connected to the port.
     * @param layerNodeList list of unassigned nodes belonging to the layer of the node the
     *                      port belongs to. The new dummy node is added to this list and
     *                      must be assigned to the layer later.
     */
    private void createWestPortSideDummies(final LGraph layeredGraph, final LPort westwardPort,
            final LEdge edge, final List<LNode> layerNodeList) {
        
        if (edge.getTarget().getNode() == westwardPort.getNode()) {
            return;
        }
        
        // Dummy node in the same layer
        LNode dummy = new LNode(layeredGraph);
        dummy.setProperty(Properties.ORIGIN, edge);
        dummy.setProperty(Properties.NODE_TYPE,
                NodeType.LONG_EDGE);
        dummy.setProperty(LayoutOptions.PORT_CONSTRAINTS, PortConstraints.FIXED_POS);
        layerNodeList.add(dummy);
        
        LPort dummyInput = new LPort(layeredGraph);
        dummyInput.setNode(dummy);
        dummyInput.setSide(PortSide.WEST);
        
        LPort dummyOutput = new LPort(layeredGraph);
        dummyOutput.setNode(dummy);
        dummyOutput.setSide(PortSide.EAST);
        
        // Reroute the original edge
        LPort originalTarget = edge.getTarget();
        edge.setTarget(dummyInput);
        
        // Connect the dummy with the original port
        LEdge dummyEdge = new LEdge(layeredGraph);
        dummyEdge.copyProperties(edge);
        dummyEdge.setSource(dummyOutput);
        dummyEdge.setTarget(originalTarget);
        
        // Set LONG_EDGE_SOURCE and LONG_EDGE_TARGET properties on the LONG_EDGE dummy
        setLongEdgeSourceAndTarget(dummy, dummyInput, dummyOutput, westwardPort);
    }
    
    /**
     * Properly sets the {@link de.cau.cs.kieler.klay.layered.properties.Properties#LONG_EDGE_SOURCE}
     * and {@link de.cau.cs.kieler.klay.layered.properties.Properties#LONG_EDGE_TARGET} properties for
     * the given long edge dummy. This is required for the
     * {@link de.cau.cs.kieler.klay.layered.intermediate.HyperedgeDummyMerger} to work
     * correctly.
     * 
     * @param longEdgeDummy the long edge dummy whose properties to set.
     * @param dummyInputPort the dummy node's input port.
     * @param dummyOutputPort the dummy node's output port.
     * @param oddPort the odd port that prompted the dummy to be created.
     */
    private void setLongEdgeSourceAndTarget(final LNode longEdgeDummy, final LPort dummyInputPort,
            final LPort dummyOutputPort, final LPort oddPort) {
        
        // There's exactly one edge connected to the input and output port
        LPort sourcePort = dummyInputPort.getIncomingEdges().get(0).getSource();
        LNode sourceNode = sourcePort.getNode();
        NodeType sourceNodeType = sourceNode.getProperty(Properties.NODE_TYPE);
        LPort targetPort = dummyOutputPort.getOutgoingEdges().get(0).getTarget();
        LNode targetNode = targetPort.getNode();
        NodeType targetNodeType = targetNode.getProperty(Properties.NODE_TYPE);
        
        // Set the LONG_EDGE_SOURCE property
        if (sourceNodeType == NodeType.LONG_EDGE) {
            // The source is a LONG_EDGE node; use its LONG_EDGE_SOURCE
            longEdgeDummy.setProperty(Properties.LONG_EDGE_SOURCE,
                    sourceNode.getProperty(Properties.LONG_EDGE_SOURCE));
        } else {
            // The target is the original node; use it
            longEdgeDummy.setProperty(Properties.LONG_EDGE_SOURCE, sourcePort);
        }

        // Set the LONG_EDGE_TARGET property
        if (targetNodeType == NodeType.LONG_EDGE) {
            // The target is a LONG_EDGE node; use its LONG_EDGE_TARGET
            longEdgeDummy.setProperty(Properties.LONG_EDGE_TARGET,
                    targetNode.getProperty(Properties.LONG_EDGE_TARGET));
        } else {
            // The target is the original node; use it
            longEdgeDummy.setProperty(Properties.LONG_EDGE_TARGET, targetPort);
        }
    }

}
