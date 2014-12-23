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
package de.cau.cs.kieler.klay.layered.p5edges;

import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import com.google.common.collect.Iterables;

import de.cau.cs.kieler.core.alg.IKielerProgressMonitor;
import de.cau.cs.kieler.kiml.options.LayoutOptions;
import de.cau.cs.kieler.klay.layered.ILayoutPhase;
import de.cau.cs.kieler.klay.layered.IntermediateProcessingConfiguration;
import de.cau.cs.kieler.klay.layered.graph.LGraph;
import de.cau.cs.kieler.klay.layered.graph.LGraphUtil;
import de.cau.cs.kieler.klay.layered.graph.LNode;
import de.cau.cs.kieler.klay.layered.graph.Layer;
import de.cau.cs.kieler.klay.layered.intermediate.IntermediateProcessorStrategy;
import de.cau.cs.kieler.klay.layered.properties.GraphProperties;
import de.cau.cs.kieler.klay.layered.properties.InternalProperties;
import de.cau.cs.kieler.klay.layered.properties.Properties;

/**
 * Edge routing implementation that creates orthogonal bend points. Inspired by
 * <ul>
 *   <li>Georg Sander, Layout of directed hypergraphs with orthogonal hyperedges. In
 *     <i>Proceedings of the 11th International Symposium on Graph Drawing (GD '03)</i>,
 *     LNCS vol. 2912, pp. 381-386, Springer, 2004.</li>
 *   <li>Giuseppe di Battista, Peter Eades, Roberto Tamassia, Ioannis G. Tollis,
 *     <i>Graph Drawing: Algorithms for the Visualization of Graphs</i>,
 *     Prentice Hall, New Jersey, 1999 (Section 9.4, for cycle breaking in the
 *     hyperedge segment graph)
 * </ul>
 * 
 * <dl>
 *   <dt>Precondition:</dt><dd>the graph has a proper layering with
 *     assigned node and port positions; the size of each layer is
 *     correctly set; edges connected to ports on strange sides were
 *     processed</dd>
 *   <dt>Postcondition:</dt><dd>each node is assigned a horizontal coordinate;
 *     the bend points of each edge are set; the width of the whole graph is set</dd>
 * </dl>
 *
 * @author msp
 * @author cds
 * @author jjc
 * @kieler.design proposed by msp
 * @kieler.rating proposed yellow by msp
 */
public final class OrthogonalEdgeRouter implements ILayoutPhase {
    
    /* The basic processing strategy for this phase is empty. Depending on the graph features,
     * dependencies on intermediate processors are added dynamically as follows:
     * 
     * Before phase 1:
     *   - None.
     * 
     * Before phase 2:
     *   - For center edge labels:
     *      - LABEL_DUMMY_INSERTER
     * 
     * Before phase 3:
     *   - For non-free ports:
     *     - NORTH_SOUTH_PORT_PREPROCESSOR
     *     - INVERTED_PORT_PROCESSOR
     *   
     *   - For self-loops:
     *     - SELF_LOOP_PROCESSOR
     *   
     *   - For hierarchical ports:
     *     - HIERARCHICAL_PORT_CONSTRAINT_PROCESSOR
     *   
     *   - For center edge labels:
     *     - LABEL_DUMMY_SWITCHER
     * 
     * Before phase 4:
     *   - For hyperedges:
     *     - HYPEREDGE_DUMMY_MERGER
     *   
     *   - For hierarchical ports:
     *     - HIERARCHICAL_PORT_DUMMY_SIZE_PROCESSOR
     *     
     *   - For edge labels:
     *     - LABEL_SIDE_SELECTOR
     * 
     * Before phase 5:
     * 
     * After phase 5:
     *   - For non-free ports:
     *     - NORTH_SOUTH_PORT_POSTPROCESSOR
     *   
     *   - For hierarchical ports:
     *     - HIERARCHICAL_PORT_ORTHOGONAL_EDGE_ROUTER
     *     
     *   - For center edge labels:
     *     - LABEL_DUMMY_REMOVER
     *     
     *   - For end edge labels:
     *     - END_LABEL_PROCESSOR
     */
    
    /** additional processor dependencies for graphs with hyperedges. */
    private static final IntermediateProcessingConfiguration HYPEREDGE_PROCESSING_ADDITIONS =
        IntermediateProcessingConfiguration.createEmpty()
            .addBeforePhase4(IntermediateProcessorStrategy.HYPEREDGE_DUMMY_MERGER);
    
    /** additional processor dependencies for graphs with possible inverted ports. */
    private static final IntermediateProcessingConfiguration INVERTED_PORT_PROCESSING_ADDITIONS =
        IntermediateProcessingConfiguration.createEmpty()
            .addBeforePhase3(IntermediateProcessorStrategy.INVERTED_PORT_PROCESSOR);
    
    /** additional processor dependencies for graphs with northern / southern non-free ports. */
    private static final IntermediateProcessingConfiguration NORTH_SOUTH_PORT_PROCESSING_ADDITIONS =
        IntermediateProcessingConfiguration.createEmpty()
            .addBeforePhase3(IntermediateProcessorStrategy.NORTH_SOUTH_PORT_PREPROCESSOR)
            .addAfterPhase5(IntermediateProcessorStrategy.NORTH_SOUTH_PORT_POSTPROCESSOR);
    
    /** additional processor dependencies for graphs with hierarchical ports. */
    private static final IntermediateProcessingConfiguration HIERARCHICAL_PORT_PROCESSING_ADDITIONS =
        IntermediateProcessingConfiguration.createEmpty()
            .addBeforePhase3(IntermediateProcessorStrategy.HIERARCHICAL_PORT_CONSTRAINT_PROCESSOR)
            .addBeforePhase4(IntermediateProcessorStrategy.HIERARCHICAL_PORT_DUMMY_SIZE_PROCESSOR)
            .addAfterPhase5(IntermediateProcessorStrategy.HIERARCHICAL_PORT_ORTHOGONAL_EDGE_ROUTER);
    
    /** additional processor dependencies for graphs with self-loops. */
    private static final IntermediateProcessingConfiguration SELF_LOOP_PROCESSING_ADDITIONS =
        IntermediateProcessingConfiguration.createEmpty()
            .addBeforePhase3(IntermediateProcessorStrategy.SELF_LOOP_PROCESSOR);
    
    /** additional processor dependencies for graphs with hypernodes. */
    private static final IntermediateProcessingConfiguration HYPERNODE_PROCESSING_ADDITIONS =
        IntermediateProcessingConfiguration.createEmpty()
            .addAfterPhase5(IntermediateProcessorStrategy.HYPERNODE_PROCESSOR);
    
    /** additional processor dependencies for graphs with center edge labels. */
    private static final IntermediateProcessingConfiguration CENTER_EDGE_LABEL_PROCESSING_ADDITIONS =
        IntermediateProcessingConfiguration.createEmpty()
            .addBeforePhase2(IntermediateProcessorStrategy.LABEL_DUMMY_INSERTER)
            .addBeforePhase3(IntermediateProcessorStrategy.LABEL_DUMMY_SWITCHER)
            .addBeforePhase4(IntermediateProcessorStrategy.LABEL_SIDE_SELECTOR)
            .addAfterPhase5(IntermediateProcessorStrategy.LABEL_DUMMY_REMOVER);
    
    /** additional processor dependencies for graphs with head or tail edge labels. */
    private static final IntermediateProcessingConfiguration END_EDGE_LABEL_PROCESSING_ADDITIONS =
        IntermediateProcessingConfiguration.createEmpty()
            .addBeforePhase4(IntermediateProcessorStrategy.LABEL_SIDE_SELECTOR)
            .addAfterPhase5(IntermediateProcessorStrategy.END_LABEL_PROCESSOR);
    
    /**
     * {@inheritDoc}
     */
    public IntermediateProcessingConfiguration getIntermediateProcessingConfiguration(
            final LGraph graph) {
        
        Set<GraphProperties> graphProperties = graph.getProperty(InternalProperties.GRAPH_PROPERTIES);
        
        // Basic configuration
        IntermediateProcessingConfiguration configuration =
                IntermediateProcessingConfiguration.createEmpty();
        
        // Additional dependencies
        if (graphProperties.contains(GraphProperties.HYPEREDGES)) {
            configuration.addAll(HYPEREDGE_PROCESSING_ADDITIONS);
            configuration.addAll(INVERTED_PORT_PROCESSING_ADDITIONS);
        }
        
        if (graphProperties.contains(GraphProperties.NON_FREE_PORTS)
                || graph.getProperty(Properties.FEEDBACK_EDGES)) {
            
            configuration.addAll(INVERTED_PORT_PROCESSING_ADDITIONS);

            if (graphProperties.contains(GraphProperties.NORTH_SOUTH_PORTS)) {
                configuration.addAll(NORTH_SOUTH_PORT_PROCESSING_ADDITIONS);
            }
        }

        if (graphProperties.contains(GraphProperties.EXTERNAL_PORTS)) {
            configuration.addAll(HIERARCHICAL_PORT_PROCESSING_ADDITIONS);
        }

        if (graphProperties.contains(GraphProperties.SELF_LOOPS)) {
            configuration.addAll(SELF_LOOP_PROCESSING_ADDITIONS);
        }
        
        if (graphProperties.contains(GraphProperties.HYPERNODES)) {
            configuration.addAll(HYPERNODE_PROCESSING_ADDITIONS);
        }
        
        if (graphProperties.contains(GraphProperties.CENTER_LABELS)) {
            configuration.addAll(CENTER_EDGE_LABEL_PROCESSING_ADDITIONS);
        }
        
        if (graphProperties.contains(GraphProperties.END_LABELS)) {
            configuration.addAll(END_EDGE_LABEL_PROCESSING_ADDITIONS);
        }
        
        return configuration;
    }
    
    /**
     * {@inheritDoc}
     */
    public void process(final LGraph layeredGraph, final IKielerProgressMonitor monitor) {
        monitor.begin("Orthogonal edge routing", 1);
        
        // Retrieve some generic values
        double nodeSpacing = layeredGraph.getProperty(Properties.OBJ_SPACING).doubleValue();
        double edgeSpacing = nodeSpacing * layeredGraph.getProperty(Properties.EDGE_SPACING_FACTOR);
        boolean debug = layeredGraph.getProperty(LayoutOptions.DEBUG_MODE);
        
        // Prepare for iteration!
        OrthogonalRoutingGenerator routingGenerator = new OrthogonalRoutingGenerator(
                OrthogonalRoutingGenerator.IRoutingDirectionStrategy.Strategy.WEST_TO_EAST,
                edgeSpacing, debug ? "phase5" : null);
        float xpos = 0.0f;
        ListIterator<Layer> layerIter = layeredGraph.getLayers().listIterator();
        Layer leftLayer = null;
        Layer rightLayer = null;
        List<LNode> leftLayerNodes = null;
        List<LNode> rightLayerNodes = null;
        int leftLayerIndex = -1;
        int rightLayerIndex = -1;
        
        // Iterate!
        do {
            int slotsCount;
            
            // Fetch the next layer, if any
            rightLayer = layerIter.hasNext() ? layerIter.next() : null;
            rightLayerNodes = rightLayer == null ? null : rightLayer.getNodes();
            rightLayerIndex = layerIter.previousIndex();
            
            // Place the left layer's nodes, if any
            if (leftLayer != null) {
                LGraphUtil.placeNodes(leftLayer, xpos);
                xpos += leftLayer.getSize().x;
            }
            
            // Route edges between the two layers
            slotsCount = routingGenerator.routeEdges(layeredGraph, leftLayerNodes, leftLayerIndex,
                    rightLayerNodes, leftLayer == null ? xpos : xpos + edgeSpacing);
            
            boolean externalLeftLayer = leftLayer == null || Iterables.all(leftLayerNodes,
                    PolylineEdgeRouter.PRED_EXTERNAL_PORT);
            boolean externalRightLayer = rightLayer == null || Iterables.all(rightLayerNodes,
                    PolylineEdgeRouter.PRED_EXTERNAL_PORT);
            if (slotsCount > 0) {
                // The space between each pair of edge segments, and between nodes and edges
                double increment = slotsCount * edgeSpacing;
                if (rightLayer != null) {
                    increment += edgeSpacing;
                }
                // If we are between two layers, make sure their minimal spacing is preserved
                if (increment < nodeSpacing && !externalLeftLayer && !externalRightLayer) {
                    increment = nodeSpacing;
                }
                xpos += increment;
            } else if (!externalLeftLayer && !externalRightLayer) {
                // If all edges are straight, use the usual spacing 
                xpos += nodeSpacing;
            }
            
            leftLayer = rightLayer;
            leftLayerNodes = rightLayerNodes;
            leftLayerIndex = rightLayerIndex;
        } while (rightLayer != null);
        
        layeredGraph.getSize().x = xpos;
        
        monitor.done();
    }
    
}
