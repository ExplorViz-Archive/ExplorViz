/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 * 
 * Copyright 2015 by
 * + Christian-Albrechts-University of Kiel
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 * 
 * This code is provided under the terms of the Eclipse Public License (EPL).
 * See the file epl-v10.html for the license text.
 */
package de.cau.cs.kieler.klay.layered;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import de.cau.cs.kieler.kiml.options.Direction;
import de.cau.cs.kieler.kiml.options.LayoutOptions;
import de.cau.cs.kieler.klay.layered.graph.LGraph;
import de.cau.cs.kieler.klay.layered.graph.LGraphUtil;
import de.cau.cs.kieler.klay.layered.intermediate.IntermediateProcessorStrategy;
import de.cau.cs.kieler.klay.layered.p5edges.EdgeRouterFactory;
import de.cau.cs.kieler.klay.layered.properties.GraphProperties;
import de.cau.cs.kieler.klay.layered.properties.InternalProperties;
import de.cau.cs.kieler.klay.layered.properties.Properties;

/**
 * The configurator configures a graph in preparation for layout. This includes making sure that a
 * given graph has sensible properties set as well as assembling the phases and processors required
 * to layout the graph. That list is attached to the graph in its
 * {@link InternalProperties#PROCESSORS} property.
 * 
 * <p>Each phase and processor is cached, so a given instance of this class can safely (and usually
 * should) be reused.</p>
 * 
 * @author cds
 */
final class GraphConfigurator {

    ////////////////////////////////////////////////////////////////////////////////
    // Constants
    
    /** initial size of the list that will contain the processors that make up the algorithm. */
    private static final int INITIAL_CONFIGURATION_SIZE = 30;
    
    /** intermediate processing configuration for basic graphs. */
    private static final IntermediateProcessingConfiguration BASELINE_PROCESSING_CONFIGURATION =
        IntermediateProcessingConfiguration.createEmpty()
            .addBeforePhase4(IntermediateProcessorStrategy.NODE_MARGIN_CALCULATOR)
            .addBeforePhase4(IntermediateProcessorStrategy.LABEL_AND_NODE_SIZE_PROCESSOR)
            .addBeforePhase5(IntermediateProcessorStrategy.LAYER_SIZE_AND_GRAPH_HEIGHT_CALCULATOR);

    
    ////////////////////////////////////////////////////////////////////////////////
    // Processor Caching
    
    /** cache of instantiated layout phases, from enumeration values to phase instances. */
    private final Map<Object, ILayoutPhase> phaseCache = Maps.newHashMap();
    /** cache of instantiated intermediate processors. */
    private final Map<IntermediateProcessorStrategy, ILayoutProcessor> intermediateProcessorCache =
            Maps.newHashMap();
    
    
    ////////////////////////////////////////////////////////////////////////////////
    // Graph Preprocessing (Property Configuration)

    /** the minimal spacing between edges, so edges won't overlap. */
    private static final float MIN_EDGE_SPACING = 2.0f;

    /**
     * Set special layout options for the layered graph.
     * 
     * @param lgraph a new layered graph
     */
    private void configureGraphProperties(final LGraph lgraph) {
        // check the bounds of some layout options
        lgraph.checkProperties(Properties.OBJ_SPACING, Properties.BORDER_SPACING,
                Properties.THOROUGHNESS, Properties.ASPECT_RATIO);
        
        float spacing = lgraph.getProperty(Properties.OBJ_SPACING);
        if (lgraph.getProperty(Properties.EDGE_SPACING_FACTOR) * spacing < MIN_EDGE_SPACING) {
            // Edge spacing is determined by the product of object spacing and edge spacing factor.
            // Make sure the resulting edge spacing is at least 2 in order to avoid overlapping edges.
            lgraph.setProperty(Properties.EDGE_SPACING_FACTOR, MIN_EDGE_SPACING / spacing);
        }
        
        Direction direction = lgraph.getProperty(LayoutOptions.DIRECTION);
        if (direction == Direction.UNDEFINED) {
            lgraph.setProperty(LayoutOptions.DIRECTION, LGraphUtil.getDirection(lgraph));
        }
        
        // set the random number generator based on the random seed option
        Integer randomSeed = lgraph.getProperty(Properties.RANDOM_SEED);
        if (randomSeed == 0) {
            lgraph.setProperty(InternalProperties.RANDOM, new Random());
        } else {
            lgraph.setProperty(InternalProperties.RANDOM, new Random(randomSeed));
        }
    }
    
    
    ////////////////////////////////////////////////////////////////////////////////
    // Updating the Configuration
    
    /**
     * Rebuilds the configuration to include all processors required to layout the given graph. The list
     * of processors is attached to the graph in the {@link InternalProperties#PROCESSORS} property.
     * 
     * @param lgraph the graph to layout.
     */
    public void prepareGraphForLayout(final LGraph lgraph) {
        // Make sure the graph properties are sensible
        configureGraphProperties(lgraph);
        
        // get instances for the different phases of our algorithm
        ILayoutPhase cycleBreaker = cachedLayoutPhase(lgraph.getProperty(Properties.CYCLE_BREAKING));
        ILayoutPhase layerer = cachedLayoutPhase(lgraph.getProperty(Properties.NODE_LAYERING));
        ILayoutPhase crossingMinimizer = cachedLayoutPhase(lgraph.getProperty(Properties.CROSS_MIN));
        ILayoutPhase nodePlacer = cachedLayoutPhase(lgraph.getProperty(Properties.NODE_PLACER));
        ILayoutPhase edgeRouter = cachedLayoutPhase(
                EdgeRouterFactory.factoryFor(lgraph.getProperty(LayoutOptions.EDGE_ROUTING)));

        // determine intermediate processor configuration
        IntermediateProcessingConfiguration intermediateProcessingConfiguration =
                IntermediateProcessingConfiguration.createEmpty();
        lgraph.setProperty(InternalProperties.CONFIGURATION, intermediateProcessingConfiguration);
        intermediateProcessingConfiguration
                .addAll(cycleBreaker.getIntermediateProcessingConfiguration(lgraph))
                .addAll(layerer.getIntermediateProcessingConfiguration(lgraph))
                .addAll(crossingMinimizer.getIntermediateProcessingConfiguration(lgraph))
                .addAll(nodePlacer.getIntermediateProcessingConfiguration(lgraph))
                .addAll(edgeRouter.getIntermediateProcessingConfiguration(lgraph))
                .addAll(this.getPhaseIndependentIntermediateProcessingConfiguration(lgraph));

        // construct the list of processors that make up the algorithm
        List<ILayoutProcessor> algorithm = Lists.newArrayListWithCapacity(INITIAL_CONFIGURATION_SIZE);
        lgraph.setProperty(InternalProperties.PROCESSORS, algorithm);
        algorithm.addAll(getIntermediateProcessorList(intermediateProcessingConfiguration,
                        IntermediateProcessingConfiguration.Slot.BEFORE_PHASE_1));
        algorithm.add(cycleBreaker);
        algorithm.addAll(getIntermediateProcessorList(intermediateProcessingConfiguration,
                        IntermediateProcessingConfiguration.Slot.BEFORE_PHASE_2));
        algorithm.add(layerer);
        algorithm.addAll(getIntermediateProcessorList(intermediateProcessingConfiguration,
                        IntermediateProcessingConfiguration.Slot.BEFORE_PHASE_3));
        algorithm.add(crossingMinimizer);
        algorithm.addAll(getIntermediateProcessorList(intermediateProcessingConfiguration,
                        IntermediateProcessingConfiguration.Slot.BEFORE_PHASE_4));
        algorithm.add(nodePlacer);
        algorithm.addAll(getIntermediateProcessorList(intermediateProcessingConfiguration,
                        IntermediateProcessingConfiguration.Slot.BEFORE_PHASE_5));
        algorithm.add(edgeRouter);
        algorithm.addAll(getIntermediateProcessorList(intermediateProcessingConfiguration,
                        IntermediateProcessingConfiguration.Slot.AFTER_PHASE_5));
    }
    
    /**
     * Returns the cycle breaker to use for the given graph depending on the property settings.
     * 
     * <p>If an instance of the requested implementation is already in the phase cache, that instance is
     * used. Otherwise, a new instance is created and put in the phase cache.</p>
     * 
     * @param lgraph the graph to return the cycle breaker for.
     * @return the cycle breaker to use.
     */
    private ILayoutPhase cachedLayoutPhase(final ILayoutPhaseFactory factory) {
        ILayoutPhase layoutPhase = phaseCache.get(factory);
        
        if (layoutPhase == null) {
            layoutPhase = factory.create();
            phaseCache.put(factory, layoutPhase);
        }
        
        return layoutPhase;
    }

    /**
     * Returns a list of layout processor instances for the given intermediate layout processing slot.
     * 
     * @param configuration the intermediate processing configuration
     * @param slot the intermediate processing slot whose list of processors to return.
     * @return list of layout processors.
     */
    private List<ILayoutProcessor> getIntermediateProcessorList(
            final IntermediateProcessingConfiguration configuration,
            final IntermediateProcessingConfiguration.Slot slot) {
        
        // fetch the set of layout processors configured for the given slot
        Set<IntermediateProcessorStrategy> processors = configuration.getProcessors(slot);
        List<ILayoutProcessor> result = new ArrayList<ILayoutProcessor>(processors.size());

        // iterate through the layout processors and add them to the result list; the processors set
        // guarantees that we iterate over the processors in the order in which they occur in
        // the LayoutProcessorStrategy, thereby satisfying all of their runtime order
        // dependencies without having to sort them in any way
        for (IntermediateProcessorStrategy processor : processors) {
            // check if an instance of the given layout processor is already in the cache
            ILayoutProcessor processorImpl = intermediateProcessorCache.get(processor);

            if (processorImpl == null) {
                // It's not in the cache, so create it and put it in the cache
                processorImpl = processor.create();
                intermediateProcessorCache.put(processor, processorImpl);
            }

            // add the layout processor to the list of processors for this slot
            result.add(processorImpl);
        }

        return result;
    }
    
    /**
     * Returns an intermediate processing configuration with processors not tied to specific phases.
     * 
     * @param lgraph the layered graph to be processed. The configuration may vary depending on certain
     *               properties of the graph.
     * @return intermediate processing configuration. May be {@code null}.
     */
    private IntermediateProcessingConfiguration getPhaseIndependentIntermediateProcessingConfiguration(
            final LGraph lgraph) {

        Set<GraphProperties> graphProperties = lgraph.getProperty(InternalProperties.GRAPH_PROPERTIES);

        // Basic configuration
        IntermediateProcessingConfiguration configuration =
                IntermediateProcessingConfiguration.fromExisting(BASELINE_PROCESSING_CONFIGURATION);

        // port side processor, put to first slot only if requested and routing is orthogonal
        if (lgraph.getProperty(Properties.FEEDBACK_EDGES)) {
            configuration.addBeforePhase1(IntermediateProcessorStrategy.PORT_SIDE_PROCESSOR);
        } else {
            configuration.addBeforePhase3(IntermediateProcessorStrategy.PORT_SIDE_PROCESSOR);
        }

        // graph transformations for unusual layout directions
        switch (lgraph.getProperty(LayoutOptions.DIRECTION)) {
        case LEFT:
            configuration
                .addBeforePhase1(IntermediateProcessorStrategy.LEFT_DIR_PREPROCESSOR)
                .addAfterPhase5(IntermediateProcessorStrategy.LEFT_DIR_POSTPROCESSOR);
            break;
        case DOWN:
            configuration
                .addBeforePhase1(IntermediateProcessorStrategy.DOWN_DIR_PREPROCESSOR)
                .addAfterPhase5(IntermediateProcessorStrategy.DOWN_DIR_POSTPROCESSOR);
            break;
        case UP:
            configuration
                .addBeforePhase1(IntermediateProcessorStrategy.UP_DIR_PREPROCESSOR)
                .addAfterPhase5(IntermediateProcessorStrategy.UP_DIR_POSTPROCESSOR);
            break;
        default:
            // This is either RIGHT or UNDEFINED, which is just mapped to RIGHT. Either way, we
            // don't need any processors here
            break;
        }

        // Additional dependencies
        if (graphProperties.contains(GraphProperties.COMMENTS)) {
            configuration
                .addBeforePhase1(IntermediateProcessorStrategy.COMMENT_PREPROCESSOR)
                .addAfterPhase5(IntermediateProcessorStrategy.COMMENT_POSTPROCESSOR);
        }

        return configuration;
    }
    
}
