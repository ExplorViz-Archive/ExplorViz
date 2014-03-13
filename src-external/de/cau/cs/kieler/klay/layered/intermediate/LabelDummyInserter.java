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

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import de.cau.cs.kieler.core.alg.IKielerProgressMonitor;
import de.cau.cs.kieler.core.math.KVector;
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
import de.cau.cs.kieler.klay.layered.properties.InternalProperties;
import de.cau.cs.kieler.klay.layered.properties.NodeType;

/**
 * Processor that inserts dummy nodes into edges that have center labels to reserve space for them.
 * At most one dummy node is created for each edge.
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
    
    /** The vertical spacing between multiple center labels. */
    public static final double LABEL_SPACING = 1.0;

    /** Predicate that checks for center labels. */
    private static final Predicate<LLabel> CENTER_LABEL = new Predicate<LLabel>() {
        public boolean apply(final LLabel label) {
            return label.getProperty(LayoutOptions.EDGE_LABEL_PLACEMENT)
                    == EdgeLabelPlacement.CENTER;
        }
    };
    
    /**
     * {@inheritDoc}
     */
    public void process(final LGraph layeredGraph, final IKielerProgressMonitor monitor) {
        monitor.begin("Label dummy insertions", 1);
        List<LNode> newDummyNodes = new LinkedList<LNode>();

        for (LNode node : layeredGraph.getLayerlessNodes()) {
            for (LPort port : node.getPorts()) {
                for (LEdge edge : port.getOutgoingEdges()) {
                    // Ignore self-loops for the moment (see KIPRA-1073)
                    if (edge.getSource().getNode() != edge.getTarget().getNode()
                            && Iterables.any(edge.getLabels(), CENTER_LABEL)) {
                    
                        LPort targetPort = edge.getTarget();
                        
                        // Create dummy node
                        LNode dummyNode = new LNode(layeredGraph);
                        dummyNode.setProperty(InternalProperties.ORIGIN, edge);
                        dummyNode.setProperty(InternalProperties.NODE_TYPE, NodeType.LABEL);
                        dummyNode.setProperty(LayoutOptions.PORT_CONSTRAINTS,
                                PortConstraints.FIXED_POS);
                        dummyNode.setProperty(InternalProperties.LONG_EDGE_SOURCE, edge.getSource());
                        dummyNode.setProperty(InternalProperties.LONG_EDGE_TARGET, edge.getTarget());
                        
                        // Set thickness of the edge
                        float thickness = edge.getProperty(LayoutOptions.THICKNESS);
                        if (thickness < 0) {
                            thickness = 0;
                            edge.setProperty(LayoutOptions.THICKNESS, thickness);
                        }
                        KVector dummySize = dummyNode.getSize();
                        dummySize.y = thickness;
                        double portPos = Math.floor(thickness / 2);

                        // Determine label size
                        for (LLabel label : edge.getLabels()) {
                            if (label.getProperty(LayoutOptions.EDGE_LABEL_PLACEMENT)
                                    == EdgeLabelPlacement.CENTER) {
                                dummySize.x = Math.max(dummySize.x, label.getSize().x);
                                dummySize.y += label.getSize().y + LABEL_SPACING;
                            }
                        }
                        dummySize.y -= LABEL_SPACING;
                                
                        // Create dummy ports
                        LPort dummyInput = new LPort(layeredGraph);
                        dummyInput.setSide(PortSide.WEST);
                        dummyInput.setNode(dummyNode);
                        dummyInput.getPosition().y = portPos;
                        
                        LPort dummyOutput = new LPort(layeredGraph);
                        dummyOutput.setSide(PortSide.EAST);
                        dummyOutput.setNode(dummyNode);
                        dummyOutput.getPosition().x = dummySize.x;
                        dummyOutput.getPosition().y = portPos;
                        
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

        // Add created dummies to graph
        layeredGraph.getLayerlessNodes().addAll(newDummyNodes);
        
        monitor.done();
    }

}
