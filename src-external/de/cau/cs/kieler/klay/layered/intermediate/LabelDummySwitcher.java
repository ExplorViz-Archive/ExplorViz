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

import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.Lists;

import de.cau.cs.kieler.core.alg.IKielerProgressMonitor;
import de.cau.cs.kieler.core.util.Pair;
import de.cau.cs.kieler.klay.layered.ILayoutProcessor;
import de.cau.cs.kieler.klay.layered.graph.LEdge;
import de.cau.cs.kieler.klay.layered.graph.LLabel;
import de.cau.cs.kieler.klay.layered.graph.LLabel.LabelSide;
import de.cau.cs.kieler.klay.layered.graph.LNode;
import de.cau.cs.kieler.klay.layered.graph.LPort;
import de.cau.cs.kieler.klay.layered.graph.Layer;
import de.cau.cs.kieler.klay.layered.graph.LGraph;
import de.cau.cs.kieler.klay.layered.properties.NodeType;
import de.cau.cs.kieler.klay.layered.properties.Properties;

/**
 * <p>Processor that switches label dummy nodes in the middle of the list of dummy nodes
 * as good as possible.</p>
 * 
 * <dl>
 *   <dt>Precondition:</dt><dd>a properly layered graph with center labels represented by
 *     center label dummy nodes.</dd>
 *   <dt>Postcondition:</dt><dd>center label dummy nodes are the centermost dummies of a long edge.</dd>
 *   <dt>Slots:</dt><dd>Before phase 3.</dd>
 *   <dt>Same-slot dependencies:</dt><dd>{@link LongEdgeSplitter}, {@link LabelSideSelector}</dd>
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
        List<Pair<LNode, LNode>> nodesToSwap = new LinkedList<Pair<LNode, LNode>>();
        for (Layer layer : layeredGraph) {
            for (LNode node : layer.getNodes()) {
                if (node.getProperty(Properties.NODE_TYPE) == NodeType.LABEL) {
                    // Move to beginning of long edge if necessary
                    LPort source = node.getProperty(Properties.LONG_EDGE_SOURCE);

                    // Collect all nodes of the long edge
                    LNode target = source.getOutgoingEdges().get(0).getTarget()
                            .getNode();
                    List<LNode> longEdge = new LinkedList<LNode>();
                    while (target.getProperty(Properties.NODE_TYPE) == NodeType.LONG_EDGE
                            || target.getProperty(Properties.NODE_TYPE) == NodeType.LABEL) {
                        longEdge.add(target);
                        target = target
                                .getOutgoingEdges().iterator().next()
                                .getTarget()
                                .getNode();
                    }
                    
                    int middle = longEdge.size() / 2;
                    if (longEdge.size() > 0) {
                        nodesToSwap.add(new Pair<LNode, LNode>(node, longEdge.get(middle)));
                    }
                    
                    if (((LLabel) node.getProperty(Properties.ORIGIN)).getSide() == LabelSide.ABOVE) {
                        for (LPort port : node.getPorts()) {
                            port.getPosition().y = node.getSize().y;
                        }
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
     * @param one the first dummy node.
     * @param other the second dummy node.
     */
    private void swapNodes(final LNode one, final LNode other) {
        // Detect incoming and outgoing ports of the nodes
        // Since they are dummy nodes, they can simply be found by looking where
        // there are incoming or outgoing edges
        LPort oneIncomingPort = null;
        LPort oneOutgoingPort = null;
        LPort otherIncomingPort = null;
        LPort otherOutgoingPort = null;
        for (LPort port : one.getPorts()) {
            if (port.getIncomingEdges().size() > 0) {
                oneIncomingPort = port;
            } else if (port.getOutgoingEdges().size() > 0) {
                oneOutgoingPort = port;
            }
        }
        for (LPort port : other.getPorts()) {
            if (port.getIncomingEdges().size() > 0) {
                otherIncomingPort = port;
            } else if (port.getOutgoingEdges().size() > 0) {
                otherOutgoingPort = port;
            }
        }

        // Store information about first node
        Layer oneLayer = one.getLayer();
        int inLayerPosition = one.getIndex();
        List<LEdge> oneIncomingEdges = Lists.newLinkedList(oneIncomingPort.getIncomingEdges());
        List<LEdge> oneOutgoingEdges = Lists.newLinkedList(oneOutgoingPort.getOutgoingEdges());
        List<LEdge> otherIncomingEdges = Lists.newLinkedList(otherIncomingPort.getIncomingEdges());
        List<LEdge> otherOutgoingEdges = Lists.newLinkedList(otherOutgoingPort.getOutgoingEdges());

        // Set values of first node to values from second node
        one.setLayer(other.getIndex(), other.getLayer());
        
        for (LEdge edge : otherIncomingEdges) {
            edge.setTarget(oneIncomingPort);
        }
        for (LEdge edge : otherOutgoingEdges) {
            edge.setSource(oneOutgoingPort);
        }

        // Set values of first node to values from second node
        other.setLayer(inLayerPosition, oneLayer);
        for (LEdge edge : oneIncomingEdges) {
            edge.setTarget(otherIncomingPort);
        }
        for (LEdge edge : oneOutgoingEdges) {
            edge.setSource(otherOutgoingPort);
        }
    }
}
