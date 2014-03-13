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
import de.cau.cs.kieler.kiml.options.Alignment;
import de.cau.cs.kieler.kiml.options.LayoutOptions;
import de.cau.cs.kieler.kiml.options.PortSide;
import de.cau.cs.kieler.klay.layered.ILayoutProcessor;
import de.cau.cs.kieler.klay.layered.graph.LNode;
import de.cau.cs.kieler.klay.layered.graph.LPort;
import de.cau.cs.kieler.klay.layered.graph.Layer;
import de.cau.cs.kieler.klay.layered.graph.LGraph;
import de.cau.cs.kieler.klay.layered.properties.InternalProperties;
import de.cau.cs.kieler.klay.layered.properties.NodeType;
import de.cau.cs.kieler.klay.layered.properties.Properties;

/**
 * Sets the width of hierarchical port dummies and sets the layer alignment of North/South port dummies
 * to Center.
 * 
 * <p>To see why this is necessary, please refer to the processor's Wiki documentation.</p>
 * 
 * <dl>
 *   <dt>Precondition:</dt>
 *     <dd>A layered graph with finished node placement; node order respects in-layer constraints.</dd>
 *   <dt>Postcondition:</dt>
 *     <dd>External port dummies are assigned a width.</dd>
 *     <dd>Layer alignment of North/South external port dummies is set to Center.</dd> 
 *   <dt>Slots:</dt>
 *     <dd>Before phase 5.</dd>
 *   <dt>Same-slot dependencies:</dt>
 *     <dd>None.</dd>
 * </dl>
 * 
 * @see HierarchicalPortConstraintProcessor
 * @see HierarchicalPortOrthogonalEdgeRouter
 * @see HierarchicalPortPositionProcessor
 * @author cds
 * @kieler.design 2012-08-10 chsch grh
 * @kieler.rating proposed yellow by msp
 */
public final class HierarchicalPortDummySizeProcessor implements ILayoutProcessor {

    /**
     * {@inheritDoc}
     */
    public void process(final LGraph layeredGraph, final IKielerProgressMonitor monitor) {
        monitor.begin("Hierarchical port dummy size processing", 1);
        
        List<LNode> northernDummies = new LinkedList<LNode>();
        List<LNode> southernDummies = new LinkedList<LNode>();
        
        // Calculate the width difference (this assumes CENTER node alignment)
        double normalSpacing = layeredGraph.getProperty(Properties.OBJ_SPACING);
        double smallSpacing = normalSpacing * layeredGraph.getProperty(Properties.EDGE_SPACING_FACTOR);
        double delta = smallSpacing * 2;
        
        // Iterate through the layers
        for (Layer layer : layeredGraph) {
            northernDummies.clear();
            southernDummies.clear();
            
            // Collect northern and southern hierarchical port dummies
            for (LNode node : layer) {
                if (node.getProperty(InternalProperties.NODE_TYPE) == NodeType.EXTERNAL_PORT) {
                    PortSide side = node.getProperty(InternalProperties.EXT_PORT_SIDE);
                    
                    if (side == PortSide.NORTH) {
                        northernDummies.add(node);
                    } else if (side == PortSide.SOUTH) {
                        southernDummies.add(node);
                    }
                }
            }
            
            // Set widths
            setWidths(northernDummies, true, delta);
            setWidths(southernDummies, false, delta);
        }
        
        monitor.done();
    }
    
    /**
     * Sets the widths of the given list of nodes and sets their layer alignment properly.
     * 
     * @param nodes the list of nodes.
     * @param topDown {@code true} if the nodes should widen with increasing index, {@code false}
     *                if it should be the other way round.
     * @param delta the width difference from one node to the next.
     */
    private void setWidths(final List<LNode> nodes, final boolean topDown, final double delta) {
        double currentWidth = 0.0;
        double step = delta;
        
        if (!topDown) {
            // Start with the widest node, decreasing node size
            currentWidth = delta * (nodes.size() - 1);
            step *= -1.0;
        }
        
        for (LNode node : nodes) {
            node.setProperty(LayoutOptions.ALIGNMENT, Alignment.CENTER);
            node.getSize().x = currentWidth;
            
            // Move eastern ports to the node's right border
            for (LPort port : node.getPorts(PortSide.EAST)) {
                port.getPosition().x = currentWidth;
            }
            
            currentWidth += step;
        }
    }
    
}
