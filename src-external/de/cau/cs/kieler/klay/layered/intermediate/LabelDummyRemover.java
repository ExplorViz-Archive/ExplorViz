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

import java.util.ListIterator;

import de.cau.cs.kieler.core.alg.IKielerProgressMonitor;
import de.cau.cs.kieler.kiml.options.LayoutOptions;
import de.cau.cs.kieler.kiml.util.nodespacing.LabelSide;
import de.cau.cs.kieler.klay.layered.ILayoutProcessor;
import de.cau.cs.kieler.klay.layered.graph.LEdge;
import de.cau.cs.kieler.klay.layered.graph.LGraph;
import de.cau.cs.kieler.klay.layered.graph.LLabel;
import de.cau.cs.kieler.klay.layered.graph.LNode;
import de.cau.cs.kieler.klay.layered.graph.LNode.NodeType;
import de.cau.cs.kieler.klay.layered.graph.Layer;
import de.cau.cs.kieler.klay.layered.properties.InternalProperties;

/**
 * <p>Processor that removes the inserted center label dummies and places the labels on their
 * position.</p>
 * 
 * <dl>
 *   <dt>Precondition:</dt>
 *     <dd>a layered graph<dd>
 *     <dd>nodes are placed</dd>
 *     <dd>edges are routed</dd>
 *     <dd>center labels are represented by and attached to center label dummy nodes.</dd>
 *   <dt>Postcondition:</dt>
 *     <dd>labels are placed</dd>
 *     <dd>there are no dummy nodes of type
 *       {@link de.cau.cs.kieler.klay.layered.properties.NodeType#LABEL}.</dd>
 *     <dd>center labels are attached to their original edges again.</dd>
 *   <dt>Slots:</dt>
 *     <dd>After phase 5.</dd>
 *   <dt>Same-slot dependencies:</dt>
 *     <dd>{@link HierarchicalPortOrthogonalEdgeRouter}</dd>
 * </dl>
 *
 * @author jjc
 * @kieler.rating yellow proposed cds
 */
public final class LabelDummyRemover implements ILayoutProcessor {

    /**
     * {@inheritDoc}
     */
    public void process(final LGraph layeredGraph, final IKielerProgressMonitor monitor) {
        monitor.begin("Label dummy removal", 1);
        
        double labelSpacing = layeredGraph.getProperty(LayoutOptions.LABEL_SPACING).doubleValue();
        
        for (Layer layer : layeredGraph.getLayers()) {
            // An iterator is necessary for traversing nodes, since
            // dummy nodes might be removed
            ListIterator<LNode> nodeIterator = layer.getNodes().listIterator();
            
            while (nodeIterator.hasNext()) {
                LNode node = nodeIterator.next();
                
                if (node.getNodeType() == NodeType.LABEL) {
                    // First, place labels on position of dummy node 
                    LEdge originEdge = (LEdge) node.getProperty(InternalProperties.ORIGIN);
                    double ypos = node.getPosition().y;
                    
                    if (node.getProperty(InternalProperties.LABEL_SIDE) == LabelSide.BELOW) {
                        ypos += originEdge.getProperty(LayoutOptions.THICKNESS)
                                + labelSpacing;
                    }
                    
                    for (LLabel label : node.getProperty(InternalProperties.REPRESENTED_LABELS)) {
                        label.getPosition().x = node.getPosition().x
                                + (node.getSize().x - label.getSize().x) / 2;
                        label.getPosition().y = ypos;
                        ypos += label.getSize().y + labelSpacing;
                        
                        originEdge.getLabels().add(label);
                    }
                    
                    // Join the edges without adding unnecessary bend points
                    LongEdgeJoiner.joinAt(node, false);
                    
                    // Remove the node
                    nodeIterator.remove();
                }
            }
        }
        monitor.done();
    }

}
