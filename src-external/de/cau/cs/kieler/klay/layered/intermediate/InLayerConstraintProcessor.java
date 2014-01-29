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
import de.cau.cs.kieler.klay.layered.ILayoutProcessor;
import de.cau.cs.kieler.klay.layered.graph.LNode;
import de.cau.cs.kieler.klay.layered.graph.Layer;
import de.cau.cs.kieler.klay.layered.graph.LGraph;
import de.cau.cs.kieler.klay.layered.properties.InLayerConstraint;
import de.cau.cs.kieler.klay.layered.properties.Properties;

/**
 * Makes sure that in-layer constraints are respected. This processor is only necessary
 * if a crossing minimizer doesn't support in-layer constraints anyway. Crossing minimizers
 * that do shouldn't include a dependency on this processor. It would need time without
 * actually doing anything worthwhile.
 * 
 * <p>Please note that, among top- and bottom-placed nodes, in-layer successor constraints
 * are not respected by this processor. It does, however, preserve them if the crossing
 * reduction phase did respect them.</p>
 * 
 * <dl>
 *   <dt>Precondition:</dt><dd>a layered graph; crossing minimization is already finished.</dd>
 *   <dt>Postcondition:</dt><dd>nodes may have been reordered to match in-layer constraints.</dd>
 *   <dt>Slots:</dt><dd>Before phase 4.</dd>
 *   <dt>Same-slot dependencies:</dt><dd>None.</dd>
 * </dl>
 * 
 * @author cds
 * @kieler.design 2012-08-10 chsch grh
 * @kieler.rating proposed yellow by msp
 */
public final class InLayerConstraintProcessor implements ILayoutProcessor {

    /**
     * {@inheritDoc}
     */
    public void process(final LGraph layeredGraph, final IKielerProgressMonitor monitor) {
        monitor.begin("Layer constraint edge reversal", 1);
        
        // Iterate through each layer
        for (Layer layer : layeredGraph) {
            /* We'll go through the layer's nodes, remembering two things:
             *  1. Once we reach the first non-top-constrained node, we remember its
             *     index. Top-constrained nodes encountered afterwards must be inserted
             *     at that point.
             *  2. A list of bottom-constrained nodes we have encountered. They will
             *     afterwards be moved to the end of the list, keeping the order in
             *     which we've encountered them.
             */
            
            int topInsertionIndex = -1;
            List<LNode> bottomConstrainedNodes = new LinkedList<LNode>();
            
            // Iterate through an array of its nodes
            LNode[] nodes = layer.getNodes().toArray(new LNode[layer.getNodes().size()]);
            
            for (int i = 0; i < nodes.length; i++) {
                InLayerConstraint constraint =
                    nodes[i].getProperty(Properties.IN_LAYER_CONSTRAINT);
                
                if (topInsertionIndex == -1) {
                    // See if this node is the first non-top-constrained node
                    if (constraint != InLayerConstraint.TOP) {
                        topInsertionIndex = i;
                    }
                } else {
                    // We have already encountered non-top-constrained nodes before
                    if (constraint == InLayerConstraint.TOP) {
                        // Move the node to the top insertion point
                        nodes[i].setLayer(null);
                        nodes[i].setLayer(topInsertionIndex++, layer);
                    }
                }
                
                // Put BOTTOM-constrained nodes into the corresponding list
                if (constraint == InLayerConstraint.BOTTOM) {
                    bottomConstrainedNodes.add(nodes[i]);
                }
            }
            
            // Append the bottom-constrained nodes
            for (LNode node : bottomConstrainedNodes) {
                node.setLayer(null);
                node.setLayer(layer);
            }
        }
        
        monitor.done();
    }

}
