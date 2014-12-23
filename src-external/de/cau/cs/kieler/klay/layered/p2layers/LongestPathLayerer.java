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
package de.cau.cs.kieler.klay.layered.p2layers;

import java.util.Collection;
import java.util.List;

import de.cau.cs.kieler.core.alg.IKielerProgressMonitor;
import de.cau.cs.kieler.klay.layered.ILayoutPhase;
import de.cau.cs.kieler.klay.layered.IntermediateProcessingConfiguration;
import de.cau.cs.kieler.klay.layered.graph.LEdge;
import de.cau.cs.kieler.klay.layered.graph.LGraph;
import de.cau.cs.kieler.klay.layered.graph.LNode;
import de.cau.cs.kieler.klay.layered.graph.LPort;
import de.cau.cs.kieler.klay.layered.graph.Layer;
import de.cau.cs.kieler.klay.layered.intermediate.IntermediateProcessorStrategy;
import de.cau.cs.kieler.klay.layered.properties.Properties;
import de.cau.cs.kieler.klay.layered.properties.WideNodesStrategy;

/**
 * The most basic layering algorithm, which assign layers according to the
 * longest path to a sink.
 * 
 * <dl>
 *   <dt>Precondition:</dt><dd>the graph has no cycles</dd>
 *   <dt>Postcondition:</dt><dd>all nodes have been assigned a layer such that
 *     edges connect only nodes from layers with increasing indices</dd>
 * </dl>
 *
 * @author msp
 * @kieler.design 2012-08-10 chsch grh
 * @kieler.rating yellow 2012-11-13 review KI-33 by grh, akoc
 */
public final class LongestPathLayerer implements ILayoutPhase {
    
    /** intermediate processing configuration. */
    private static final IntermediateProcessingConfiguration BASELINE_PROCESSING_CONFIGURATION =
        IntermediateProcessingConfiguration.createEmpty()
            .addBeforePhase1(IntermediateProcessorStrategy.EDGE_AND_LAYER_CONSTRAINT_EDGE_REVERSER)
            .addBeforePhase3(IntermediateProcessorStrategy.LAYER_CONSTRAINT_PROCESSOR);
    
    /** additional processor dependencies for handling big nodes. */
    private static final IntermediateProcessingConfiguration BIG_NODES_PROCESSING_ADDITIONS_AGGRESSIVE =
            IntermediateProcessingConfiguration.createEmpty()
                    .addBeforePhase2(IntermediateProcessorStrategy.BIG_NODES_PREPROCESSOR)
                    .addBeforePhase3(IntermediateProcessorStrategy.BIG_NODES_INTERMEDIATEPROCESSOR)
                    .addAfterPhase5(IntermediateProcessorStrategy.BIG_NODES_POSTPROCESSOR);

    /** additional processor dependencies for handling big nodes after cross min. */
    private static final IntermediateProcessingConfiguration BIG_NODES_PROCESSING_ADDITIONS_CAREFUL =
            IntermediateProcessingConfiguration.createEmpty()
                    .addBeforePhase4(IntermediateProcessorStrategy.BIG_NODES_SPLITTER)
                    .addAfterPhase5(IntermediateProcessorStrategy.BIG_NODES_POSTPROCESSOR);

    /** the layered graph to which layers are added. */
    private LGraph layeredGraph;
    /** map of nodes to their height in the layering. */
    private int[] nodeHeights;
    
    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("deprecation")
    public IntermediateProcessingConfiguration getIntermediateProcessingConfiguration(
            final LGraph graph) {

        // Basic strategy
        IntermediateProcessingConfiguration strategy =
                IntermediateProcessingConfiguration.fromExisting(BASELINE_PROCESSING_CONFIGURATION);

        // Additional dependencies
        if (graph.getProperty(Properties.DISTRIBUTE_NODES)
                || graph.getProperty(Properties.WIDE_NODES_ON_MULTIPLE_LAYERS) 
                        == WideNodesStrategy.AGGRESSIVE) {
            strategy.addAll(BIG_NODES_PROCESSING_ADDITIONS_AGGRESSIVE);
            
        } else if (graph.getProperty(Properties.WIDE_NODES_ON_MULTIPLE_LAYERS) 
                        == WideNodesStrategy.CAREFUL) {
            strategy.addAll(BIG_NODES_PROCESSING_ADDITIONS_CAREFUL);
        }
        
        if (graph.getProperty(Properties.SAUSAGE_FOLDING)) {
            strategy.addBeforePhase4(IntermediateProcessorStrategy.SAUSAGE_COMPACTION);
        }
        
        return strategy;
    }
    
    /**
     * {@inheritDoc}
     */
    public void process(final LGraph thelayeredGraph, final IKielerProgressMonitor monitor) {
        monitor.begin("Longest path layering", 1);
        
        layeredGraph = thelayeredGraph;
        Collection<LNode> nodes = layeredGraph.getLayerlessNodes();
        
        // initialize values required for the computation
        nodeHeights = new int[nodes.size()];
        int index = 0;
        for (LNode node : nodes) {
            // the node id is used as index for the nodeHeights array
            node.id = index;
            nodeHeights[index] = -1;
            index++;
        }
        
        // process all nodes
        for (LNode node : nodes) {
            visit(node);
        }
        
        // empty the list of unlayered nodes
        nodes.clear();
        
        // release the created resources
        this.layeredGraph = null;
        this.nodeHeights = null;
        
        monitor.done();
    }
    

    /**
     * Visit a node: if not already visited, find the longest path to a sink.
     * 
     * @param node node to visit
     * @return height of the given node in the layered graph
     */
    private int visit(final LNode node) {
        int height = nodeHeights[node.id];
        if (height >= 0) {
            // the node was already visited (the case height == 0 should never occur)
            return height;
        } else {
            int maxHeight = 1;
            for (LPort port : node.getPorts()) {
                for (LEdge edge : port.getOutgoingEdges()) {
                    LNode targetNode = edge.getTarget().getNode();
                    
                    // ignore self-loops
                    if (node != targetNode) {
                        int targetHeight = visit(targetNode);
                        maxHeight = Math.max(maxHeight, targetHeight + 1);
                    }
                }
            }
            putNode(node, maxHeight);
            return maxHeight;
        }
    }
    
    /**
     * Puts the given node into the layered graph, adding new layers as necessary.
     * 
     * @param node a node
     * @param height height of the layer where the node shall be added
     *          (height = number of layers - layer index)
     */
    private void putNode(final LNode node, final int height) {
        List<Layer> layers = layeredGraph.getLayers();
        // add layers so as to guarantee that number of layers >= height
        for (int i = layers.size(); i < height; i++) {
            layers.add(0, new Layer(layeredGraph));
        }
        // layer index = number of layers - height
        node.setLayer(layers.get(layers.size() - height));
        nodeHeights[node.id] = height;
    }

}
