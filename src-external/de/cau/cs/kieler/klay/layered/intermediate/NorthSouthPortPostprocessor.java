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

import java.util.Iterator;

import de.cau.cs.kieler.core.alg.IKielerProgressMonitor;
import de.cau.cs.kieler.core.math.KVector;
import de.cau.cs.kieler.core.math.KVectorChain;
import de.cau.cs.kieler.kiml.options.LayoutOptions;
import de.cau.cs.kieler.kiml.options.PortSide;
import de.cau.cs.kieler.klay.layered.ILayoutProcessor;
import de.cau.cs.kieler.klay.layered.graph.LEdge;
import de.cau.cs.kieler.klay.layered.graph.LNode;
import de.cau.cs.kieler.klay.layered.graph.LPort;
import de.cau.cs.kieler.klay.layered.graph.Layer;
import de.cau.cs.kieler.klay.layered.graph.LGraph;
import de.cau.cs.kieler.klay.layered.properties.NodeType;
import de.cau.cs.kieler.klay.layered.properties.Properties;

/**
 * Removes dummy nodes created by {@link NorthSouthPortPreprocessor} and routes the
 * edges properly.
 * 
 * <dl>
 *   <dt>Precondition:</dt><dd>a layered graph; nodes are placed; edges are routed; port positions
 *     are fixed.</dd>
 *   <dt>Postcondition:</dt><dd>north south port dummy nodes are removed, their edges
 *     properly reconnected and routed.</dd>
 *   <dt>Slots:</dt><dd>After phase 5.</dd>
 *   <dt>Same-slot dependencies:</dt><dd>None.</dd>
 * </dl>
 * 
 * @see NorthSouthPortPreprocessor
 * @author cds
 * @kieler.design 2012-08-10 chsch grh
 * @kieler.rating proposed yellow by msp
 */
public final class NorthSouthPortPostprocessor implements ILayoutProcessor {

    /**
     * {@inheritDoc}
     */
    public void process(final LGraph layeredGraph, final IKielerProgressMonitor monitor) {
        monitor.begin("Odd port side processing", 1);
        
        // Iterate through the layers
        for (Layer layer : layeredGraph) {
            // Iterate through the nodes (use an array to avoid concurrent modification exceptions)
            LNode[] nodeArray = layer.getNodes().toArray(new LNode[layer.getNodes().size()]);
            for (LNode node : nodeArray) {
                // We only care for North/South Port dummy nodes
                if (node.getProperty(Properties.NODE_TYPE) != NodeType.NORTH_SOUTH_PORT) {
                    continue;
                }
                
                if (node.getProperty(Properties.ORIGIN) instanceof LEdge) {
                    // It's a self-loop
                    processSelfLoop(node);
                } else {
                    // Check if all ports were created for the same origin port
                    boolean sameOriginPort;
                    if (node.getPorts().size() >= 2) {
                        // Iterate over the dummy's ports to find out whether the origin is always the
                        // same
                        sameOriginPort = true;
                        
                        Iterator<LPort> portIterator = node.getPorts().iterator();
                        LPort currentPort = portIterator.next();
                        LPort previousPort = null;
                        
                        while (portIterator.hasNext()) {
                            previousPort = currentPort;
                            currentPort = portIterator.next();
                            
                            if (!previousPort.getProperty(Properties.ORIGIN).equals(
                                    currentPort.getProperty(Properties.ORIGIN))) {
                                
                                // The two ports don't have the same origin
                                sameOriginPort = false;
                                break;
                            }
                        }
                    } else {
                        sameOriginPort = false;
                    }
                    
                    // Iterate through the ports
                    for (LPort port : node.getPorts()) {
                        if (!port.getIncomingEdges().isEmpty()) {
                            processInputPort(port, sameOriginPort);
                        }
                        
                        if (!port.getOutgoingEdges().isEmpty()) {
                            processOutputPort(port, sameOriginPort);
                        }
                    }
                }
                
                // Remove the node
                node.setLayer(null);
            }
        }
        
        monitor.done();
    }
    
    /**
     * Reroutes the edges connected to the given input port back to the port it was
     * created for.
     * 
     * @param inputPort the input port whose edges to restore.
     * @param addJunctionPoints if {@code true}, adds a junction point to the edge that equals the
     *                          bend point computed for the edge. This should be {@code true} for ports
     *                          belonging to north south port dummies that only have ports created for
     *                          the same port.
     */
    private void processInputPort(final LPort inputPort, final boolean addJunctionPoints) {
        // Retrieve the port the dummy node was created from
        LPort originPort = (LPort) inputPort.getProperty(Properties.ORIGIN);
        
        // Calculate the bend point
        double x = originPort.getAbsoluteAnchor().x;
        double y = inputPort.getNode().getPosition().y;
        
        // Reroute the edges, inserting a new bend point at the position of the dummy node
        LEdge[] edgeArray = inputPort.getIncomingEdges().toArray(
                new LEdge[inputPort.getIncomingEdges().size()]);
        for (LEdge inEdge : edgeArray) {
            inEdge.setTarget(originPort);
            inEdge.getBendPoints().addLast(x, y);
            
            // Check if a junction point should be added
            if (addJunctionPoints) {
                KVectorChain junctionPoints = inEdge.getProperty(LayoutOptions.JUNCTION_POINTS);
                if (junctionPoints == null) {
                    junctionPoints = new KVectorChain();
                    inEdge.setProperty(LayoutOptions.JUNCTION_POINTS, junctionPoints);
                }
                junctionPoints.add(new KVector(x, y));
            }
        }
    }
    
    /**
     * Reroutes the edges connected to the given output port back to the port it was
     * created for.
     * 
     * @param outputPort the output port whose edges to restore.
     * @param addJunctionPoints if {@code true}, adds a junction point to the edge that equals the
     *                          bend point computed for the edge. This should be {@code true} for ports
     *                          belonging to north south port dummies that only have ports created for
     *                          the same port.
     */
    private void processOutputPort(final LPort outputPort, final boolean addJunctionPoints) {
        // Retrieve the port the dummy node was created from
        LPort originPort = (LPort) outputPort.getProperty(Properties.ORIGIN);
        
        // Calculate the bend point
        double x = originPort.getAbsoluteAnchor().x;
        double y = outputPort.getNode().getPosition().y;
        
        // Reroute the edges, inserting a new bend point at the position of the dummy node
        LEdge[] edgeArray = outputPort.getOutgoingEdges().toArray(
                new LEdge[outputPort.getOutgoingEdges().size()]);
        for (LEdge outEdge : edgeArray) {
            outEdge.setSource(originPort);
            outEdge.getBendPoints().addFirst(x, y);
            
            // Check if a junction point should be added
            if (addJunctionPoints) {
                KVectorChain junctionPoints = outEdge.getProperty(LayoutOptions.JUNCTION_POINTS);
                if (junctionPoints == null) {
                    junctionPoints = new KVectorChain();
                    outEdge.setProperty(LayoutOptions.JUNCTION_POINTS, junctionPoints);
                }
                junctionPoints.add(new KVector(x, y));
            }
        }
    }
    
    /**
     * Reroutes and reconnects the self-loop edge represented by the given dummy.
     * 
     * @param dummy the dummy representing the self-loop edge.
     */
    private void processSelfLoop(final LNode dummy) {
        // Get the edge and the ports it was originally connected to
        LEdge selfLoop = (LEdge) dummy.getProperty(Properties.ORIGIN);
        LPort inputPort = dummy.getPorts(PortSide.WEST).iterator().next();
        LPort outputPort = dummy.getPorts(PortSide.EAST).iterator().next();
        LPort originInputPort = (LPort) inputPort.getProperty(Properties.ORIGIN);
        LPort originOutputPort = (LPort) outputPort.getProperty(Properties.ORIGIN);
        
        // Reconnect the edge
        selfLoop.setSource(originOutputPort);
        selfLoop.setTarget(originInputPort);
        
        // Add two bend points
        KVector bendPoint = new KVector(outputPort.getNode().getPosition());
        bendPoint.x = originOutputPort.getAbsoluteAnchor().x;
        selfLoop.getBendPoints().add(bendPoint);
        
        bendPoint = new KVector(inputPort.getNode().getPosition());
        bendPoint.x = originInputPort.getAbsoluteAnchor().x;
        selfLoop.getBendPoints().add(bendPoint);
    }
}
