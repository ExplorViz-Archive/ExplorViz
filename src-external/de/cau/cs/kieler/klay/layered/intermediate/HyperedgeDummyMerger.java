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

import java.util.List;
import java.util.ListIterator;

import de.cau.cs.kieler.core.alg.IKielerProgressMonitor;
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
 * Looks for long edge dummy nodes that can be joined together. The aim is to reduce the
 * amount of edges by having edges originating from the same port or going into the same
 * port joined. This should be done after crossing minimization. Only those dummy nodes
 * are joined that the crossing minimizer placed right next to each other.
 * 
 * <dl>
 *   <dt>Precondition:</dt><dd>a layered graph; node orders are fixed; for long edge dummies
 *     to be joined, their {@link de.cau.cs.kieler.klay.layered.properties.Properties#LONG_EDGE_SOURCE}
 *     and {@link de.cau.cs.kieler.klay.layered.properties.Properties#LONG_EDGE_TARGET} properties must
 *     be set.</dd>
 *   <dt>Postcondition:</dt><dd>long edge dummy nodes belonging to the same hyperedge and
 *     being directly next to each other are merged.</dd>
 *   <dt>Slots:</dt><dd>Before phase 4.</dd>
 *   <dt>Same-slot dependencies:</dt><dd>{@link InLayerConstraintProcessor}</dd>
 * </dl>
 *
 * @author cds
 * @kieler.design 2012-08-10 chsch grh
 * @kieler.rating proposed yellow by msp
 */
public final class HyperedgeDummyMerger implements ILayoutProcessor {

    /**
     * {@inheritDoc}
     */
    public void process(final LGraph layeredGraph, final IKielerProgressMonitor monitor) {
        monitor.begin("Hyperedge merging", 1);
        
        // Iterate through the layers
        ListIterator<Layer> layerIter = layeredGraph.getLayers().listIterator();
        while (layerIter.hasNext()) {
            Layer layer = layerIter.next();
            List<LNode> nodes = layer.getNodes();
            
            // If there are no nodes anyway, just move on to the next layer
            if (nodes.isEmpty()) {
                continue;
            }
            
            LNode currentNode = null;
            NodeType currentNodeType = null;
            LNode lastNode = null;
            NodeType lastNodeType = null;
            
            // Iterate through the remaining nodes
            for (int nodeIndex = 0; nodeIndex < nodes.size(); nodeIndex++) {
                // Get the next node
                currentNode = nodes.get(nodeIndex);
                currentNodeType = currentNode.getProperty(Properties.NODE_TYPE);
                
                // We're only interested if the current and last nodes are long edge dummies
                if (currentNodeType == NodeType.LONG_EDGE
                        && lastNodeType == NodeType.LONG_EDGE) {
                    
                    // Get long edge source and target ports
                    LPort currentNodeSource = currentNode.getProperty(Properties.LONG_EDGE_SOURCE);
                    LPort lastNodeSource = lastNode.getProperty(Properties.LONG_EDGE_SOURCE);
                    LPort currentNodeTarget = currentNode.getProperty(Properties.LONG_EDGE_TARGET);
                    LPort lastNodeTarget = lastNode.getProperty(Properties.LONG_EDGE_TARGET);
                    
                    // If at least one of the two nodes doesn't have the properties set, skip it
                    boolean currentNodePropertiesSet =
                        currentNodeSource != null || currentNodeTarget != null;
                    boolean lastNodePropertiesSet =
                        lastNodeSource != null || lastNodeTarget != null;
                    
                    // If the source or the target are identical, merge the current node
                    // into the last
                    if (currentNodePropertiesSet && lastNodePropertiesSet
                            && (currentNodeSource == lastNodeSource
                                    || currentNodeTarget == lastNodeTarget)) {
                        
                        mergeNodes(lastNode, currentNode, currentNodeSource == lastNodeSource,
                                currentNodeTarget == lastNodeTarget
                        );
                        
                        // Remove the current node and make the last node the current node
                        nodes.remove(nodeIndex);
                        nodeIndex--;
                        currentNode = lastNode;
                        currentNodeType = lastNodeType;
                    }
                }
                
                // Remember this node for the next iteration
                lastNode = currentNode;
                lastNodeType = currentNodeType;
            }
        }
        
        monitor.done();
    }
    
    /**
     * Merges the merge source node into the merge target node. All edges that were previously
     * connected to the merge source's ports are rerouted to the merge target. The merge target's
     * long edge source and target ports can be set to null.
     * 
     * @param mergeTarget the merge target node.
     * @param mergeSource the merge source node.
     * @param keepSourcePort if {@code true}, the long edge source property is set to {@code null}.
     * @param keepTargetPort if {@code true}, the long edge target property is set to {@code null}.
     */
    private void mergeNodes(final LNode mergeTarget, final LNode mergeSource,
            final boolean keepSourcePort, final boolean keepTargetPort) {
        
        // We assume that the input port is west, and the output port east
        LPort mergeTargetInputPort = mergeTarget.getPorts(PortSide.WEST).iterator().next();
        LPort mergeTargetOutputPort = mergeTarget.getPorts(PortSide.EAST).iterator().next();
        
        for (LPort port : mergeSource.getPorts()) {
            if (!port.getIncomingEdges().isEmpty()) {
                // Use an array of edges to avoid concurrent modification exceptions
                LEdge[] edgeArray = port.getIncomingEdges().toArray(
                        new LEdge[port.getIncomingEdges().size()]);
                
                for (LEdge edge : edgeArray) {
                    edge.setTarget(mergeTargetInputPort);
                }
            }
            
            if (!port.getOutgoingEdges().isEmpty()) {
                // Use an array of edges to avoid concurrent modification exceptions
                LEdge[] edgeArray = port.getOutgoingEdges().toArray(
                        new LEdge[port.getOutgoingEdges().size()]);
                
                for (LEdge edge : edgeArray) {
                    edge.setSource(mergeTargetOutputPort);
                }
            }
        }
        
        // Possibly reset source and target ports
        if (!keepSourcePort) {
            mergeTarget.setProperty(Properties.LONG_EDGE_SOURCE, null);
        }
        
        if (!keepTargetPort) {
            mergeTarget.setProperty(Properties.LONG_EDGE_TARGET, null);
        }
    }

}
