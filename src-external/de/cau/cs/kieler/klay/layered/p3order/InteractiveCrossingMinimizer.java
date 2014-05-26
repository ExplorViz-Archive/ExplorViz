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
package de.cau.cs.kieler.klay.layered.p3order;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import de.cau.cs.kieler.core.alg.IKielerProgressMonitor;
import de.cau.cs.kieler.core.math.KVector;
import de.cau.cs.kieler.core.math.KVectorChain;
import de.cau.cs.kieler.klay.layered.ILayoutPhase;
import de.cau.cs.kieler.klay.layered.IntermediateProcessingConfiguration;
import de.cau.cs.kieler.klay.layered.graph.LEdge;
import de.cau.cs.kieler.klay.layered.graph.LGraph;
import de.cau.cs.kieler.klay.layered.graph.LNode;
import de.cau.cs.kieler.klay.layered.graph.LPort;
import de.cau.cs.kieler.klay.layered.graph.Layer;
import de.cau.cs.kieler.klay.layered.intermediate.IntermediateProcessorStrategy;
import de.cau.cs.kieler.klay.layered.properties.GraphProperties;
import de.cau.cs.kieler.klay.layered.properties.InternalProperties;

/**
 * A crossing minimizer that allows user interaction by respecting previous node positions.
 * 
 * <dl>
 *   <dt>Precondition:</dt><dd>The graph has a proper layering, i.e. all
 *     long edges have been splitted; all nodes have at least fixed port
 *     sides.</dd>
 *   <dt>Postcondition:</dt><dd>The order of nodes in each layer is rearranged
 *     according to previous positions given by the input graph.</dd>
 * </dl>
 *
 * @author msp
 * @kieler.design 2012-08-10 chsch grh
 * @kieler.rating proposed yellow by msp
 */
public final class InteractiveCrossingMinimizer implements ILayoutPhase {

    /** intermediate processing configuration. */
    private static final IntermediateProcessingConfiguration INTERMEDIATE_PROCESSING_CONFIGURATION =
        IntermediateProcessingConfiguration.createEmpty()
            .addBeforePhase3(IntermediateProcessorStrategy.LONG_EDGE_SPLITTER)
            .addBeforePhase4(IntermediateProcessorStrategy.IN_LAYER_CONSTRAINT_PROCESSOR)
            .addAfterPhase5(IntermediateProcessorStrategy.LONG_EDGE_JOINER);
    
    /**
     * {@inheritDoc}
     */
    public IntermediateProcessingConfiguration getIntermediateProcessingConfiguration(
            final LGraph graph) {
        IntermediateProcessingConfiguration configuration =
                IntermediateProcessingConfiguration.fromExisting(INTERMEDIATE_PROCESSING_CONFIGURATION);
        
        if (graph.getProperty(InternalProperties.GRAPH_PROPERTIES).contains(
                GraphProperties.NON_FREE_PORTS)) {
            
            configuration.addBeforePhase3(IntermediateProcessorStrategy.PORT_LIST_SORTER);
        }
        
        return configuration;
    }

    /**
     * {@inheritDoc}
     */
    public void process(final LGraph layeredGraph, final IKielerProgressMonitor monitor) {
        monitor.begin("Interactive crossing minimization", 1);
        
        int portCount = 0;
        for (Layer layer : layeredGraph) {
            // determine a horizontal position for edge bend points comparison
            double horizPos = 0;
            int nodeCount = 0;
            for (LNode node : layer.getNodes()) {
                if (node.getPosition().x > 0) {
                    horizPos += node.getPosition().x + node.getSize().x / 2;
                    nodeCount++;
                }
                for (LPort port : node.getPorts()) {
                    port.id = portCount++;
                }
            }
            horizPos /= nodeCount;
            
            // create an array of vertical node positions
            final double[] pos = new double[layer.getNodes().size()];
            int nextIndex = 0;
            for (LNode node : layer) {
                node.id = nextIndex++;
                pos[node.id] = getPos(node, horizPos);
            }
            
            // sort the nodes using the position array
            Collections.sort(layer.getNodes(), new Comparator<LNode>() {
                public int compare(final LNode node1, final LNode node2) {
                    int compare = Double.compare(pos[node1.id], pos[node2.id]);
                    
                    if (compare == 0) {
                        // The two nodes have the same y coordinate. Check for node successor
                        // constraints
                        List<LNode> node1Successors =
                                node1.getProperty(InternalProperties.IN_LAYER_SUCCESSOR_CONSTRAINTS);
                        List<LNode> node2Successors =
                                node2.getProperty(InternalProperties.IN_LAYER_SUCCESSOR_CONSTRAINTS);
                        
                        if (node1Successors.contains(node2)) {
                            return -1;
                        } else if (node2Successors.contains(node1)) {
                            return 1;
                        }
                    }
                    
                    return compare;
                }
            });
        }

        // Initialize the position arrays and layered graph array
        LNode[][] lgraphArray = new LNode[layeredGraph.getLayers().size()][];
        ListIterator<Layer> layerIter = layeredGraph.getLayers().listIterator();
        while (layerIter.hasNext()) {
            Layer layer = layerIter.next();
            int layerIndex = layerIter.previousIndex();
            lgraphArray[layerIndex] = layer.getNodes().toArray(new LNode[layer.getNodes().size()]);
        }
        
        // Distribute the ports of all nodes with free port constraints
        AbstractPortDistributor portDistributor = new NodeRelativePortDistributor(new float[portCount]);
        portDistributor.distributePorts(lgraphArray);
        
        monitor.done();
    }
    
    /**
     * Determine a vertical position for the given node.
     * 
     * @param node a node
     * @param horizPos the horizontal position at which to measure (relevant for edges)
     * @return the vertical position used for sorting
     */
    private double getPos(final LNode node, final double horizPos) {
        switch (node.getProperty(InternalProperties.NODE_TYPE)) {
        case LONG_EDGE:
            LEdge edge = (LEdge) node.getProperty(InternalProperties.ORIGIN);
            // reconstruct the original bend points from the node annotations
            KVectorChain bendpoints = edge.getProperty(InternalProperties.ORIGINAL_BENDPOINTS);
            if (bendpoints == null) {
                bendpoints = new KVectorChain();
            } else if (edge.getProperty(InternalProperties.REVERSED)) {
                bendpoints = KVectorChain.reverse(bendpoints);
            }
            LPort source = node.getProperty(InternalProperties.LONG_EDGE_SOURCE);
            KVector sourcePoint = source.getAbsoluteAnchor();
            if (horizPos <= sourcePoint.x) {
                return sourcePoint.y;
            }
            bendpoints.addFirst(sourcePoint);
            LPort target = node.getProperty(InternalProperties.LONG_EDGE_TARGET);
            KVector targetPoint = target.getAbsoluteAnchor();
            if (targetPoint.x <= horizPos) {
                return targetPoint.y;
            }
            bendpoints.addLast(targetPoint);
            
            Iterator<KVector> pointIter = bendpoints.iterator();
            KVector point1 = pointIter.next();
            KVector point2 = pointIter.next();
            while (point2.x < horizPos && pointIter.hasNext()) {
                point1 = point2;
                point2 = pointIter.next();
            }
            return point1.y + (horizPos - point1.x) / (point2.x - point1.x) * (point2.y - point1.y);
            
        case NORTH_SOUTH_PORT:
            // Get one of the ports the dummy node was created for, and its original node
            LPort originPort = (LPort) node.getPorts().get(0).getProperty(InternalProperties.ORIGIN);
            LNode originNode = originPort.getNode();
            
            switch (originPort.getSide()) {
            case NORTH:
                // Use the position of the node's northern side. This causes northern dummies to
                // have the same y coordinate as the node they were created from if TOP_LEFT is
                // used as the anchor point. We solve this when sorting the nodes by y coordinate
                // by respecting node successor constraints.
                return originNode.getPosition().y;
            
            case SOUTH:
                // Use the position of the node's southern side
                return originNode.getPosition().y + originNode.getSize().y;
            }
            
            break;

        // FIXME What about the other node types?
        }
        
        // the fallback solution is to take the previous position of the node's anchor point
        return node.getInteractiveReferencePoint().y;
    }

}
