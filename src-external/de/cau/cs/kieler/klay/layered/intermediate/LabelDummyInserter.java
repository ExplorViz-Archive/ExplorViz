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
import java.util.ListIterator;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import de.cau.cs.kieler.core.alg.IKielerProgressMonitor;
import de.cau.cs.kieler.core.math.KVector;
import de.cau.cs.kieler.kiml.options.EdgeLabelPlacement;
import de.cau.cs.kieler.kiml.options.LayoutOptions;
import de.cau.cs.kieler.kiml.options.PortConstraints;
import de.cau.cs.kieler.klay.layered.ILayoutProcessor;
import de.cau.cs.kieler.klay.layered.graph.LEdge;
import de.cau.cs.kieler.klay.layered.graph.LGraph;
import de.cau.cs.kieler.klay.layered.graph.LLabel;
import de.cau.cs.kieler.klay.layered.graph.LNode;
import de.cau.cs.kieler.klay.layered.graph.LNode.NodeType;
import de.cau.cs.kieler.klay.layered.graph.LPort;
import de.cau.cs.kieler.klay.layered.properties.InternalProperties;

/**
 * Processor that inserts dummy nodes into edges that have center labels to reserve space for them.
 * At most one dummy node is created for each edge.
 * 
 * <dl>
 *   <dt>Precondition:</dt>
 *     <dd>an unlayered acyclic graph.</dd>
 *   <dt>Postcondition:</dt>
 *     <dd>dummy nodes are inserted that represent center labels.</dd>
 *     <dd>center labels are removed from their respective edges and attached to the dummy nodes.</dd>
 *   <dt>Slots:</dt>
 *     <dd>Before phase 2.</dd>
 *   <dt>Same-slot dependencies:</dt>
 *     <dd>None.</dd>
 * </dl>
 * 
 * @author jjc
 * @kieler.rating yellow proposed cds
 */
public final class LabelDummyInserter implements ILayoutProcessor {
    
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
        
        List<LNode> newDummyNodes = Lists.newArrayList();
        
        double labelSpacing = layeredGraph.getProperty(LayoutOptions.LABEL_SPACING).doubleValue();

        for (LNode node : layeredGraph.getLayerlessNodes()) {
            for (LPort port : node.getPorts()) {
                for (LEdge edge : port.getOutgoingEdges()) {
                    // Ignore self-loops for the moment (see KIPRA-1073)
                    if (edge.getSource().getNode() != edge.getTarget().getNode()
                            && Iterables.any(edge.getLabels(), CENTER_LABEL)) {
                    
                        // Remember the list of edge labels represented by the dummy node
                        List<LLabel> representedLabels = Lists.newArrayListWithCapacity(
                                edge.getLabels().size());
                        
                        // Create dummy node
                        LNode dummyNode = new LNode(layeredGraph);
                        dummyNode.setNodeType(NodeType.LABEL);
                        
                        dummyNode.setProperty(InternalProperties.ORIGIN, edge);
                        dummyNode.setProperty(InternalProperties.REPRESENTED_LABELS, representedLabels);
                        dummyNode.setProperty(LayoutOptions.PORT_CONSTRAINTS,
                                PortConstraints.FIXED_POS);
                        dummyNode.setProperty(InternalProperties.LONG_EDGE_SOURCE, edge.getSource());
                        dummyNode.setProperty(InternalProperties.LONG_EDGE_TARGET, edge.getTarget());
                        
                        newDummyNodes.add(dummyNode);
                        
                        // Actually split the edge
                        LongEdgeSplitter.splitEdge(edge, dummyNode);
                        
                        // Set thickness of the edge
                        float thickness = edge.getProperty(LayoutOptions.THICKNESS);
                        if (thickness < 0) {
                            thickness = 0;
                            edge.setProperty(LayoutOptions.THICKNESS, thickness);
                        }
                        KVector dummySize = dummyNode.getSize();
                        dummySize.y = thickness;
                        double portPos = Math.floor(thickness / 2);

                        // Apply port positions
                        for (LPort dummyPort : dummyNode.getPorts()) {
                            dummyPort.getPosition().y = portPos;
                        }
                        
                        // Determine label size
                        ListIterator<LLabel> iterator = edge.getLabels().listIterator();
                        while (iterator.hasNext()) {
                            LLabel label = iterator.next();
                            
                            if (label.getProperty(LayoutOptions.EDGE_LABEL_PLACEMENT)
                                    == EdgeLabelPlacement.CENTER) {
                                
                                dummySize.x = Math.max(dummySize.x, label.getSize().x);
                                dummySize.y += label.getSize().y + labelSpacing;
                                
                                // Move the label over to the dummy node's REPRESENTED_LABELS property
                                representedLabels.add(label);
                                iterator.remove();
                            }
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
