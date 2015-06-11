/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 * 
 * Copyright 2012 by
 * + Christian-Albrechts-University of Kiel
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 * 
 * This code is provided under the terms of the Eclipse Public License (EPL).
 * See the file epl-v10.html for the license text.
 */
package de.cau.cs.kieler.klay.layered.intermediate;

import java.util.List;

import com.google.common.collect.Lists;

import de.cau.cs.kieler.core.alg.IKielerProgressMonitor;
import de.cau.cs.kieler.core.util.Pair;
import de.cau.cs.kieler.klay.layered.ILayoutProcessor;
import de.cau.cs.kieler.klay.layered.graph.LEdge;
import de.cau.cs.kieler.klay.layered.graph.LGraph;
import de.cau.cs.kieler.klay.layered.graph.LNode;
import de.cau.cs.kieler.klay.layered.graph.LNode.NodeType;
import de.cau.cs.kieler.klay.layered.graph.LPort;
import de.cau.cs.kieler.klay.layered.graph.Layer;
import de.cau.cs.kieler.klay.layered.properties.PortType;

/**
 * <p>Processor that switches label dummy nodes in the middle of the list of dummy nodes
 * as good as possible.</p>
 * 
 * <dl>
 *   <dt>Precondition:</dt><dd>a properly layered graph with center labels represented by
 *     center label dummy nodes.</dd>
 *   <dt>Postcondition:</dt><dd>center label dummy nodes are the centermost dummies of a long edge.</dd>
 *   <dt>Slots:</dt><dd>Before phase 3.</dd>
 *   <dt>Same-slot dependencies:</dt><dd>{@link LongEdgeSplitter}</dd>
 * </dl>
 * 
 * @author jjc
 * @kieler.rating yellow proposed cds
 */
public final class LabelDummySwitcher implements ILayoutProcessor {

    /**
     * {@inheritDoc}
     */
    public void process(final LGraph layeredGraph, final IKielerProgressMonitor monitor) {
        monitor.begin("Label dummy switching", 1);
        
        // Mark all label nodes which can be swapped to the middle of a long edge
        List<Pair<LNode, LNode>> nodesToSwap = Lists.newArrayList();
        for (Layer layer : layeredGraph) {
            for (LNode node : layer.getNodes()) {
                if (node.getNodeType() == NodeType.LABEL) {
                    // Gather long edge dummies left of the label dummy
                    List<LNode> leftLongEdge = Lists.newArrayList();
                    LNode source = node;
                    do {
                        source = source.getIncomingEdges().iterator().next().getSource().getNode();
                        if (source.getNodeType() == NodeType.LONG_EDGE) {
                            leftLongEdge.add(source);
                        }
                    } while (source.getNodeType() == NodeType.LONG_EDGE);
                    
                    // Gather long edge dummies right of the label dummy
                    List<LNode> rightLongEdge = Lists.newArrayList();
                    LNode target = node;
                    do {
                        target = target.getOutgoingEdges().iterator().next().getTarget().getNode();
                        if (target.getNodeType() == NodeType.LONG_EDGE) {
                            rightLongEdge.add(target);
                        }
                    } while (target.getNodeType() == NodeType.LONG_EDGE);
                    
                    // Check whether the label dummy should be switched
                    int leftSize = leftLongEdge.size();
                    int rightSize = rightLongEdge.size();
                    if (leftSize > rightSize + 1) {
                        int pos = (leftSize + rightSize) / 2;
                        nodesToSwap.add(new Pair<LNode, LNode>(node, leftLongEdge.get(pos)));
                    } else if (rightSize > leftSize + 1) {
                        int pos = (rightSize - leftSize) / 2 - 1;
                        nodesToSwap.add(new Pair<LNode, LNode>(node, rightLongEdge.get(pos)));
                    }
                }
            }
        }

        // Execute the swapping
        for (Pair<LNode, LNode> swapPair : nodesToSwap) {
            swapNodes(swapPair.getFirst(), swapPair.getSecond());
        }
        
        monitor.done();
    }

    /**
     * Swaps the two given dummy nodes.
     * 
     * @param dummy1 the first dummy node.
     * @param dummy2 the second dummy node.
     */
    private void swapNodes(final LNode dummy1, final LNode dummy2) {
        Layer layer1 = dummy1.getLayer();
        Layer layer2 = dummy2.getLayer();
        
        // Detect incoming and outgoing ports of the nodes
        LPort inputPort1 = dummy1.getPorts(PortType.INPUT).iterator().next();
        LPort outputPort1 = dummy1.getPorts(PortType.OUTPUT).iterator().next();
        LPort inputPort2 = dummy2.getPorts(PortType.INPUT).iterator().next();
        LPort outputPort2 = dummy2.getPorts(PortType.OUTPUT).iterator().next();
        
        // Store incoming and outgoing edges
        LEdge[] incomingEdges1 = inputPort1.getIncomingEdges().toArray(new LEdge[1]);
        LEdge[] outgoingEdges1 = outputPort1.getOutgoingEdges().toArray(new LEdge[1]);
        LEdge[] incomingEdges2 = inputPort2.getIncomingEdges().toArray(new LEdge[1]);
        LEdge[] outgoingEdges2 = outputPort2.getOutgoingEdges().toArray(new LEdge[1]);

        // Set values of first node to values from second node
        dummy1.setLayer(layer2);
        for (LEdge edge : incomingEdges2) {
            edge.setTarget(inputPort1);
        }
        for (LEdge edge : outgoingEdges2) {
            edge.setSource(outputPort1);
        }

        // Set values of first node to values from second node
        dummy2.setLayer(layer1);
        for (LEdge edge : incomingEdges1) {
            edge.setTarget(inputPort2);
        }
        for (LEdge edge : outgoingEdges1) {
            edge.setSource(outputPort2);
        }
    }
}
