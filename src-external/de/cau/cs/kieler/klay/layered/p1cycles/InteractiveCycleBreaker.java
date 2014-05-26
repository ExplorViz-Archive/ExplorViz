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
package de.cau.cs.kieler.klay.layered.p1cycles;

import java.util.LinkedList;
import java.util.List;

import de.cau.cs.kieler.core.alg.IKielerProgressMonitor;
import de.cau.cs.kieler.klay.layered.ILayoutPhase;
import de.cau.cs.kieler.klay.layered.IntermediateProcessingConfiguration;
import de.cau.cs.kieler.klay.layered.graph.LEdge;
import de.cau.cs.kieler.klay.layered.graph.LGraph;
import de.cau.cs.kieler.klay.layered.graph.LNode;
import de.cau.cs.kieler.klay.layered.graph.LPort;
import de.cau.cs.kieler.klay.layered.intermediate.IntermediateProcessorStrategy;
import de.cau.cs.kieler.klay.layered.properties.PortType;

/**
 * A cycle breaker that responds to user interaction by respecting the direction of
 * edges as given in the original drawing.
 * 
 * <dl>
 *   <dt>Precondition:</dt><dd>none</dd>
 *   <dt>Postcondition:</dt><dd>the graph has no cycles</dd>
 * </dl>
 * 
 * @author msp
 * @kieler.design 2012-08-10 chsch grh
 * @kieler.rating yellow 2012-11-13 review KI-33 by grh, akoc
 */
public final class InteractiveCycleBreaker implements ILayoutPhase {

    /** intermediate processing configuration. */
    private static final IntermediateProcessingConfiguration INTERMEDIATE_PROCESSING_CONFIGURATION =
        IntermediateProcessingConfiguration.createEmpty()
            .addAfterPhase5(IntermediateProcessorStrategy.REVERSED_EDGE_RESTORER);

    /**
     * {@inheritDoc}
     */
    public IntermediateProcessingConfiguration getIntermediateProcessingConfiguration(
            final LGraph graph) {
        
        return INTERMEDIATE_PROCESSING_CONFIGURATION;
    }

    /**
     * {@inheritDoc}
     */
    public void process(final LGraph layeredGraph, final IKielerProgressMonitor monitor) {
        monitor.begin("Interactive cycle breaking", 1);
        
        // gather edges that point to the wrong direction
        LinkedList<LEdge> revEdges = new LinkedList<LEdge>();
        for (LNode source : layeredGraph.getLayerlessNodes()) {
            source.id = 1;
            double sourcex = source.getInteractiveReferencePoint().x;
            for (LPort port : source.getPorts(PortType.OUTPUT)) {
                for (LEdge edge : port.getOutgoingEdges()) {
                    LNode target = edge.getTarget().getNode();
                    if (target != source) {
                        double targetx = target.getInteractiveReferencePoint().x;
                        if (targetx < sourcex) {
                            revEdges.add(edge);
                        }
                    }
                }
            }
        }
        // reverse the gathered edges
        for (LEdge edge : revEdges) {
            edge.reverse(layeredGraph, true);
        }
        
        // perform an additional check for cycles - maybe we missed something
        // (could happen if some nodes have the same horizontal position)
        revEdges.clear();
        for (LNode node : layeredGraph.getLayerlessNodes()) {
            // unvisited nodes have id = 1
            if (node.id > 0) {
                findCycles(node, revEdges);
            }
        }
        // again, reverse the edges that were marked
        for (LEdge edge : revEdges) {
            edge.reverse(layeredGraph, true);
        }
        
        revEdges.clear();
        monitor.done();
    }
    
    /**
     * Perform a DFS starting on the given node and mark back edges in order to break cycles.
     * 
     * @param node1 a node
     * @param revEdges list of edges that will be reversed
     */
    private void findCycles(final LNode node1, final List<LEdge> revEdges) {
        // nodes with negative id are part of the currently inspected path
        node1.id = -1;
        for (LPort port : node1.getPorts(PortType.OUTPUT)) {
            for (LEdge edge : port.getOutgoingEdges()) {
                LNode node2 = edge.getTarget().getNode();
                if (node1 != node2) {
                    if (node2.id < 0) {
                        // a node of the current path is found --> cycle
                        revEdges.add(edge);
                    } else if (node2.id > 0) {
                        // the node has not been visited yet --> expand the current path
                        findCycles(node2, revEdges);
                    }
                }
            }
        }
        // nodes with id = 0 have been already visited and are ignored if encountered again
        node1.id = 0;
    }

}
