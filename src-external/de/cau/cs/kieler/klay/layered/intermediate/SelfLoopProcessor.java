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
package de.cau.cs.kieler.klay.layered.intermediate;

import java.util.LinkedList;
import java.util.List;

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
import de.cau.cs.kieler.klay.layered.properties.Properties;

/**
 * This processor does some work to ensure that other phases and processors can handle
 * self-loops correctly. In detail, it handles the following cases:
 * 
 * <dl>
 *   <dt>North / South to West</dt>
 *     <dd>The edge is reversed to maintain a left-to-right data flow.</dd>
 *   <dt>East to North / South</dt>
 *     <dd>The edge is reversed to maintain a left-to-right data flow.</dd>
 *   <dt>East to West</dt>
 *     <dd>The edge is reversed to maintain a left-to-right data flow. A long-edge dummy
 *       node is placed in between.</dd>
 *   <dt>West to East</dt>
 *     <dd>A long-edge dummy is placed in between.</dd>
 *   <dt>North to South</dt>
 *     <dd>Nothing is done.</dd>
 *   <dt>South to North</dt>
 *     <dd>The edge is reversed to maintain a top-down data flow.</dd>
 * </dl>
 * 
 * <p>The processor thus reduces the number of different cases of self-loops.</p>
 * 
 * <dl>
 *   <dt>Precondition:</dt><dd>a layered graph.</dd>
 *   <dt>Postcondition:</dt><dd>long edge dummies were inserted for special kinds of self
 *     loops.</dd>
 *   <dt>Slots:</dt><dd>Before phase 3.</dd>
 *   <dt>Same-slot dependencies:</dt><dd>{@link InvertedPortProcessor}</dd>
 * </dl>
 *
 * @author cds
 * @kieler.design 2012-08-10 chsch grh
 * @kieler.rating proposed yellow by msp
 */
public final class SelfLoopProcessor implements ILayoutProcessor {

    /**
     * {@inheritDoc}
     */
    public void process(final LGraph layeredGraph, final IKielerProgressMonitor monitor) {
        monitor.begin("Self-loop processing", 1);
        
        // Iterate through all nodes
        List<LNode> createdDummies = new LinkedList<LNode>();
        
        for (Layer layer : layeredGraph) {
            createdDummies.clear();
            
            for (LNode node : layer) {
                for (LPort port : node.getPorts()) {
                    // Go through the port's outgoing edges
                    LEdge[] edges = port.getOutgoingEdges().toArray(
                            new LEdge[port.getOutgoingEdges().size()]);
                    
                    for (LEdge edge : edges) {
                        // We're only interested in edges whose source and target node are identical
                        if (edge.getSource().getNode() != edge.getTarget().getNode()) {
                            continue;
                        }
                        
                        LPort sourcePort = edge.getSource();
                        LPort targetPort = edge.getTarget();
                        PortSide sourcePortSide = sourcePort.getSide();
                        PortSide targetPortSide = targetPort.getSide();
                        
                        /* We have to take care of the following scenarios:
                         *  1. North or South -> West
                         *     Reverse the edge.
                         *  2. East -> North or South
                         *     Reverse the edge.
                         *  3. East -> West
                         *     Reverse the edge, insert dummy.
                         *  4. West -> East
                         *     Insert dummy.
                         *  5. North -> South
                         *     Nothing has to be done.
                         *  6. South -> North
                         *     Reverse the edge.
                         */
                        
                        // First, let's deal with the cases where edges have to be reversed
                        if ((sourcePortSide == PortSide.NORTH || sourcePortSide == PortSide.SOUTH)
                                && targetPortSide == PortSide.WEST) {
                            
                            edge.reverse(layeredGraph, false);
                        } else if (sourcePortSide == PortSide.SOUTH
                                && targetPortSide == PortSide.NORTH) {
                            
                            edge.reverse(layeredGraph, false);
                        } else if (sourcePortSide == PortSide.EAST
                                && targetPortSide != PortSide.EAST) {
                            
                            edge.reverse(layeredGraph, false);
                        }
                        
                        // Now, let's see if a dummy has to be inserted
                        if (sourcePortSide == PortSide.EAST && targetPortSide == PortSide.WEST) {
                            // Note that the edge was reversed, so source and target port have switched
                            createdDummies.add(createDummy(layeredGraph, edge, targetPort, sourcePort));
                        } else if (sourcePortSide == PortSide.WEST && targetPortSide == PortSide.EAST) {
                            createdDummies.add(createDummy(layeredGraph, edge, sourcePort, targetPort));
                        }
                    }
                }
            }
            
            // Add the dummies, if any
            for (LNode dummy : createdDummies) {
                dummy.setLayer(layer);
            }
        }
        
        monitor.done();
    }
    
    /**
     * Creates a dummy for the self-loop edge connecting the two given ports. The dummy is not
     * added to the layer yet.
     * 
     * @param layeredGraph the layered graph.
     * @param edge the self-looping edge.
     * @param sourcePort the source port.
     * @param targetPort the target port.
     * @return the dummy node created.
     */
    private LNode createDummy(final LGraph layeredGraph, final LEdge edge, final LPort sourcePort,
            final LPort targetPort) {
        
        // Create a dummy node with an input port and an output port
        LNode dummyNode = new LNode(layeredGraph);
        dummyNode.setProperty(Properties.ORIGIN, edge);
        dummyNode.setProperty(Properties.NODE_TYPE, NodeType.LONG_EDGE);
        dummyNode.setProperty(LayoutOptions.PORT_CONSTRAINTS, PortConstraints.FIXED_POS);
        dummyNode.setProperty(Properties.LONG_EDGE_SOURCE, sourcePort);
        dummyNode.setProperty(Properties.LONG_EDGE_TARGET, targetPort);
        
        LPort dummyInput = new LPort(layeredGraph);
        dummyInput.setSide(PortSide.WEST);
        dummyInput.setNode(dummyNode);
        
        LPort dummyOutput = new LPort(layeredGraph);
        dummyOutput.setSide(PortSide.EAST);
        dummyOutput.setNode(dummyNode);
        
        edge.setTarget(dummyInput);
        
        // Create a dummy edge
        LEdge dummyEdge = new LEdge(layeredGraph);
        dummyEdge.copyProperties(edge);
        dummyEdge.setSource(dummyOutput);
        dummyEdge.setTarget(targetPort);
        
        return dummyNode;
    }
    
}
