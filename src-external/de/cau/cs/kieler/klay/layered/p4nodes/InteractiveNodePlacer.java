/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 * 
 * Copyright 2014 by
 * + Christian-Albrechts-University of Kiel
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 * 
 * This code is provided under the terms of the Eclipse Public License (EPL).
 * See the file epl-v10.html for the license text.
 */
package de.cau.cs.kieler.klay.layered.p4nodes;

import de.cau.cs.kieler.core.alg.IKielerProgressMonitor;
import de.cau.cs.kieler.klay.layered.ILayoutPhase;
import de.cau.cs.kieler.klay.layered.IntermediateProcessingConfiguration;
import de.cau.cs.kieler.klay.layered.graph.LGraph;
import de.cau.cs.kieler.klay.layered.graph.LNode;
import de.cau.cs.kieler.klay.layered.graph.LNode.NodeType;
import de.cau.cs.kieler.klay.layered.graph.Layer;
import de.cau.cs.kieler.klay.layered.intermediate.IntermediateProcessorStrategy;
import de.cau.cs.kieler.klay.layered.properties.GraphProperties;
import de.cau.cs.kieler.klay.layered.properties.InternalProperties;
import de.cau.cs.kieler.klay.layered.properties.Properties;

/**
 * A node placer that keeps the pre-existing y coordinates of nodes. As far as dummy nodes are
 * concerned, the interactive node placer tries to compute sensible coordinates for them based
 * on the pre-existing routing of the edges they represent. If nodes overlap, they are moved as
 * far down as necessary to remove the overlaps.
 * 
 * <p>Using this node placer really only makes sense if the interactive implementations of all
 * previous phases are used as well.</p>
 * 
 * <dl>
 *   <dt>Preconditions:</dt>
 *     <dd>The graph layering was produced by the interactive layering algorithm</dd>
 *     <dd>The node ordering was produced by the interactive crossing minimization algorithm</dd>
 *   <dt>Postconditions:</dt>
 *     <dd>Each node is assigned a vertical coordinate such that no two nodes overlap</dd>
 *     <dd>The size of each layer is set according to the area occupied by its nodes</dd>
 *     <dd>The height of the graph is set to the maximal layer height</dd>
 * </dl>
 * 
 * @author cds
 */
public final class InteractiveNodePlacer implements ILayoutPhase {

    /** additional processor dependencies for graphs with hierarchical ports. */
    private static final IntermediateProcessingConfiguration HIERARCHY_PROCESSING_ADDITIONS =
        IntermediateProcessingConfiguration.createEmpty()
            .addBeforePhase5(IntermediateProcessorStrategy.HIERARCHICAL_PORT_POSITION_PROCESSOR);

    /**
     * {@inheritDoc}
     */
    public IntermediateProcessingConfiguration getIntermediateProcessingConfiguration(
            final LGraph graph) {
        
        if (graph.getProperty(InternalProperties.GRAPH_PROPERTIES).contains(
                GraphProperties.EXTERNAL_PORTS)) {
            return HIERARCHY_PROCESSING_ADDITIONS;
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void process(final LGraph layeredGraph, final IKielerProgressMonitor monitor) {
        monitor.begin("Interactive node placement", 1);

        float normalSpacing = layeredGraph.getProperty(InternalProperties.SPACING)
                * layeredGraph.getProperty(Properties.OBJ_SPACING_IN_LAYER_FACTOR);
        float smallSpacing = normalSpacing
                * layeredGraph.getProperty(Properties.EDGE_SPACING_FACTOR);
        
        // Place the nodes in each layer
        for (Layer layer : layeredGraph) {
            placeNodes(layer, normalSpacing, smallSpacing);
        }
        
        // TODO Compute a graph offset?
        
        monitor.done();
    }
    
    /**
     * Places the nodes in the given layer.
     * 
     * @param layer the layer whose nodes to place.
     * @param spacing spacing between regular nodes.
     * @param smallSpacing spacing between dummy nodes.
     */
    private void placeNodes(final Layer layer, final double spacing, final double smallSpacing) {
        // The minimum value for the next valid y coordinate
        double minValidY = Double.NEGATIVE_INFINITY;
        
        // The node type of the last node
        NodeType prevNodeType = NodeType.NORMAL;
        
        for (LNode node : layer) {
            // Check which kind of node it is
            NodeType nodeType = node.getNodeType();
            if (nodeType != NodeType.NORMAL) {
                // While normal nodes have their original position already in them, with dummy nodes
                // it's more complicated. Check if the interactive crossing minimizer has calculated
                // an original position for the dummy node. If not, we compute one.
                Double originalYCoordinate = node.getProperty(
                        InternalProperties.ORIGINAL_DUMMY_NODE_POSITION);
                
                if (originalYCoordinate == null) {
                    // Make sure that the minimum valid Y position is usable
                    minValidY = Math.max(minValidY, 0.0);
                    
                    node.getPosition().y = minValidY
                            + (prevNodeType == NodeType.NORMAL ? spacing : smallSpacing);
                } else {
                    node.getPosition().y = originalYCoordinate;
                }
            }
            
            // If the node extends into nodes we already placed above, we need to move it down
            if (node.getPosition().y < minValidY) {
                node.getPosition().y = minValidY 
                        + (prevNodeType == NodeType.NORMAL || nodeType == NodeType.NORMAL
                           ? spacing : smallSpacing);
            }
            
            // Update minimum valid y coordinate and remember node type
            minValidY = node.getPosition().y + node.getSize().y;
            prevNodeType = nodeType;
        }
    }

}
