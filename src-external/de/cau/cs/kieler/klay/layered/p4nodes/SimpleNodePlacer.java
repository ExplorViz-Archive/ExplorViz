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
package de.cau.cs.kieler.klay.layered.p4nodes;

import de.cau.cs.kieler.core.alg.IKielerProgressMonitor;
import de.cau.cs.kieler.core.math.KVector;
import de.cau.cs.kieler.klay.layered.ILayoutPhase;
import de.cau.cs.kieler.klay.layered.IntermediateProcessingConfiguration;
import de.cau.cs.kieler.klay.layered.graph.LNode;
import de.cau.cs.kieler.klay.layered.graph.Layer;
import de.cau.cs.kieler.klay.layered.graph.LGraph;
import de.cau.cs.kieler.klay.layered.intermediate.IntermediateProcessorStrategy;
import de.cau.cs.kieler.klay.layered.properties.GraphProperties;
import de.cau.cs.kieler.klay.layered.properties.InternalProperties;
import de.cau.cs.kieler.klay.layered.properties.NodeType;
import de.cau.cs.kieler.klay.layered.properties.Properties;

/**
 * Node placement implementation that centers all nodes vertically.
 * 
 * <dl>
 *   <dt>Preconditions:</dt>
 *     <dd>The graph has a proper layering with optimized nodes ordering</dd>
 *     <dd>Ports are properly arranged</dd>
 *   <dt>Postconditions:</dt>
 *     <dd>Each node is assigned a vertical coordinate such that no two nodes overlap</dd>
 *     <dd>The size of each layer is set according to the area occupied by its nodes</dd>
 * </dl>
 * 
 * @author msp
 * @kieler.design proposed by msp
 * @kieler.rating proposed yellow by msp
 */
public final class SimpleNodePlacer implements ILayoutPhase {

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
        monitor.begin("Simple node placement", 1);

        float normalSpacing = layeredGraph.getProperty(Properties.OBJ_SPACING)
                * layeredGraph.getProperty(Properties.OBJ_SPACING_IN_LAYER_FACTOR);
        float smallSpacing = normalSpacing
                * layeredGraph.getProperty(Properties.EDGE_SPACING_FACTOR);

        // first iteration: determine the height of each layer
        double maxHeight = 0;
        for (Layer layer : layeredGraph.getLayers()) {
            KVector layerSize = layer.getSize();
            layerSize.y = 0;
            LNode lastNode = null;
            for (LNode node : layer.getNodes()) {
                if (lastNode != null) {
                    if (lastNode.getProperty(InternalProperties.NODE_TYPE) == NodeType.NORMAL) {
                        layerSize.y += normalSpacing;
                    } else {
                        layerSize.y += smallSpacing;
                    }
                }
                layerSize.y += node.getMargin().top + node.getSize().y + node.getMargin().bottom;
                lastNode = node;
            }
            maxHeight = Math.max(maxHeight, layerSize.y);
        }
        
        // second iteration: center the nodes of each layer around the tallest layer
        for (Layer layer : layeredGraph.getLayers()) {
            KVector layerSize = layer.getSize();
            double pos = (maxHeight - layerSize.y) / 2;
            LNode lastNode = null;
            for (LNode node : layer.getNodes()) {
                if (lastNode != null) {
                    if (lastNode.getProperty(InternalProperties.NODE_TYPE) == NodeType.NORMAL) {
                        pos += normalSpacing;
                    } else {
                        pos += smallSpacing;
                    }
                }
                pos += node.getMargin().top;
                node.getPosition().y = pos;
                pos += node.getSize().y + node.getMargin().bottom;
                lastNode = node;
            }
        }
        
        monitor.done();
    }

}
