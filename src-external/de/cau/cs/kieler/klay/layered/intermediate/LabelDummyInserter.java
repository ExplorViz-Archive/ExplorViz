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

import de.cau.cs.kieler.core.alg.IKielerProgressMonitor;
import de.cau.cs.kieler.kiml.options.EdgeLabelPlacement;
import de.cau.cs.kieler.kiml.options.LayoutOptions;
import de.cau.cs.kieler.kiml.options.PortConstraints;
import de.cau.cs.kieler.kiml.options.PortSide;
import de.cau.cs.kieler.klay.layered.ILayoutProcessor;
import de.cau.cs.kieler.klay.layered.graph.LEdge;
import de.cau.cs.kieler.klay.layered.graph.LLabel;
import de.cau.cs.kieler.klay.layered.graph.LNode;
import de.cau.cs.kieler.klay.layered.graph.LPort;
import de.cau.cs.kieler.klay.layered.graph.LGraph;
import de.cau.cs.kieler.klay.layered.properties.NodeType;
import de.cau.cs.kieler.klay.layered.properties.Properties;

/**
 * <p>Processor that inserts dummy nodes into edges that have a center label to reserve space
 * for them.</p>
 * 
 * <dl>
 *   <dt>Precondition:</dt><dd>an unlayered acyclic graph.</dd>
 *   <dt>Postcondition:</dt><dd>dummy nodes are inserted that represent center labels.</dd>
 *   <dt>Slots:</dt><dd>Before phase 2.</dd>
 *   <dt>Same-slot dependencies:</dt><dd>None.</dd>
 * </dl>
 * 
 * @author jjc
 * @kieler.rating yellow proposed cds
 */
public final class LabelDummyInserter implements ILayoutProcessor {

    /**
     * {@inheritDoc}
     */
    public void process(final LGraph layeredGraph, final IKielerProgressMonitor monitor) {
        monitor.begin("Label dummy insertions", 1);
        List<LNode> newDummyNodes = new LinkedList<LNode>();

        for (LNode node : layeredGraph.getLayerlessNodes()) {
            for (LPort port : node.getPorts()) {
                for (LEdge edge : port.getOutgoingEdges()) {
                    // Ignore self-loops for the moment (KIELER-2136 is, amongst other things, about
                    // fixing this limitation)
                    if (edge.getSource().getNode().equals(edge.getTarget().getNode())) {
                        continue;
                    }
                    
                    for (LLabel label : edge.getLabels()) {
                        if (label.getProperty(LayoutOptions.EDGE_LABEL_PLACEMENT)
                                == EdgeLabelPlacement.CENTER) {
                            
                            LPort targetPort = edge.getTarget();
                            
                            // Create dummy node
                            LNode dummyNode = new LNode(layeredGraph);
                            dummyNode.setProperty(Properties.ORIGIN, label);
                            dummyNode.setProperty(Properties.NODE_TYPE, NodeType.LABEL);
                            dummyNode.setProperty(LayoutOptions.PORT_CONSTRAINTS,
                                    PortConstraints.FIXED_POS);
                            dummyNode.setProperty(Properties.LONG_EDGE_SOURCE, edge.getSource());
                            dummyNode.setProperty(Properties.LONG_EDGE_TARGET, edge.getTarget());
                            
                            // Set values of dummy node
                            dummyNode.getSize().x = label.getSize().x;                           
                            dummyNode.getSize().y = label.getSize().y;
                            
                            // Create dummy ports
                            LPort dummyInput = new LPort(layeredGraph);
                            dummyInput.setSide(PortSide.WEST);
                            dummyInput.setNode(dummyNode);
                            
                            LPort dummyOutput = new LPort(layeredGraph);
                            dummyOutput.setSide(PortSide.EAST);
                            dummyOutput.setNode(dummyNode);
                            
                            edge.setTarget(dummyInput);
                            
                            // Create dummy edge
                            LEdge dummyEdge = new LEdge(layeredGraph);
                            dummyEdge.copyProperties(edge);
                            dummyEdge.setSource(dummyOutput);
                            dummyEdge.setTarget(targetPort);
                            
                            // Remember created dummies
                            newDummyNodes.add(dummyNode);
                        }
                    }
                }
            }
        }

        // Add created dummies to graph
        layeredGraph.getLayerlessNodes().addAll(newDummyNodes);
        
        monitor.done();
    }

}
