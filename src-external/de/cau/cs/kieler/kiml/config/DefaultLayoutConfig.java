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
package de.cau.cs.kieler.kiml.config;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.cau.cs.kieler.core.kgraph.KEdge;
import de.cau.cs.kieler.core.kgraph.KGraphElement;
import de.cau.cs.kieler.core.kgraph.KLabel;
import de.cau.cs.kieler.core.kgraph.KNode;
import de.cau.cs.kieler.core.kgraph.KPort;
import de.cau.cs.kieler.core.kgraph.util.KGraphSwitch;
import de.cau.cs.kieler.core.properties.IProperty;
import de.cau.cs.kieler.core.properties.Property;
import de.cau.cs.kieler.kiml.LayoutAlgorithmData;
import de.cau.cs.kieler.kiml.LayoutDataService;
import de.cau.cs.kieler.kiml.LayoutOptionData;

/**
 * Default implementation of the layout configuration interface. This configuration handles the
 * default values of layout algorithms and layout options.
 *
 * @author msp
 * @kieler.design proposed by msp
 * @kieler.rating yellow 2012-07-05 review KI-18 by cmot, sgu
 */
public class DefaultLayoutConfig implements ILayoutConfig {
    
    /** the priority for the default layout configuration. */
    public static final int PRIORITY = 0;
    
    /** option for layout context: whether the {@link OPTIONS} list shall be created. */
    public static final IProperty<Boolean> OPT_MAKE_OPTIONS = new Property<Boolean>(
            "context.makeOptions", false);
    
    /** the layout options that are supported by the active layout algorithm. */
    public static final IProperty<List<LayoutOptionData>> OPTIONS
            = new Property<List<LayoutOptionData>>("context.options");
    
    /** the layout algorithm or type identifier for the content of the current graph element. */
    public static final IProperty<String> CONTENT_HINT = new Property<String>(
            "context.contentHint");
    
    /** the diagram type identifier for the content of the current graph element. */
    public static final IProperty<String> CONTENT_DIAGT = new Property<String>(
            "context.contentDiagramType");
    
    /** the layout algorithm that is responsible for the content of the current graph element. */
    public static final IProperty<LayoutAlgorithmData> CONTENT_ALGO
            = new Property<LayoutAlgorithmData>("context.contentAlgorithm");
    
    /** the layout algorithm or type identifier for the container of the current graph element. */
    public static final IProperty<String> CONTAINER_HINT = new Property<String>(
            "context.containerHint");
    
    /** the diagram type identifier for the container of the current graph element. */
    public static final IProperty<String> CONTAINER_DIAGT = new Property<String>(
            "context.containerDiagramType");
    
    /** the layout algorithm that is assigned to the container of the current graph element. */ 
    public static final IProperty<LayoutAlgorithmData> CONTAINER_ALGO
            = new Property<LayoutAlgorithmData>("context.containerAlgorithm");
    
    /** whether the node in the current context contains any ports. */
    public static final IProperty<Boolean> HAS_PORTS = new Property<Boolean>(
            "context.hasPorts", false);

    /**
     * {@inheritDoc}
     */
    public int getPriority() {
        return PRIORITY;
    }
    
    /**
     * A switch class for KGraph elements that determines the layout option targets.
     */
    public static class OptionTargetSwitch extends KGraphSwitch<Set<LayoutOptionData.Target>> {
        
        @Override
        public Set<LayoutOptionData.Target> caseKNode(final KNode node) {
            Set<LayoutOptionData.Target> targets = EnumSet.noneOf(LayoutOptionData.Target.class);
            if (node.getParent() != null) {
                targets.add(LayoutOptionData.Target.NODES);
            }
            if (node.getChildren().size() != 0) {
                targets.add(LayoutOptionData.Target.PARENTS);
            }
            return targets;
        }
        
        @Override
        public Set<LayoutOptionData.Target> caseKEdge(final KEdge edge) {
            return EnumSet.of(LayoutOptionData.Target.EDGES);
        }
        
        @Override
        public Set<LayoutOptionData.Target> caseKPort(final KPort port) {
            return EnumSet.of(LayoutOptionData.Target.PORTS);
        }
        
        @Override
        public Set<LayoutOptionData.Target> caseKLabel(final KLabel label) {
            return EnumSet.of(LayoutOptionData.Target.LABELS);
        }
        
    };
    
    /** the cached switch for computing layout option targets. */
    private OptionTargetSwitch kgraphSwitch = new OptionTargetSwitch();

    /**
     * {@inheritDoc}
     */
    public void enrich(final LayoutContext context) {
        // adapt ports information in the context
        KGraphElement graphElement = context.getProperty(LayoutContext.GRAPH_ELEM);
        if (context.getProperty(HAS_PORTS) == null && graphElement instanceof KNode) {
            context.setProperty(HAS_PORTS, !((KNode) graphElement).getPorts().isEmpty());
        }
        
        // add layout option target types
        Set<LayoutOptionData.Target> optionTargets = context.getProperty(LayoutContext.OPT_TARGETS);
        if (optionTargets == null && graphElement != null) {
            optionTargets = kgraphSwitch.doSwitch(graphElement);
            context.setProperty(LayoutContext.OPT_TARGETS, optionTargets);
        }
        
        if (context.getProperty(OPT_MAKE_OPTIONS)) {
            if (optionTargets == null) {
                optionTargets = Collections.emptySet();
            }
            LayoutDataService layoutDataService = LayoutDataService.getInstance();
            List<LayoutOptionData> optionData = new LinkedList<LayoutOptionData>();
    
            for (LayoutOptionData.Target target : optionTargets) {
                LayoutAlgorithmData algoData;
                switch (target) {
                case PARENTS:
                    // add algorithm data for the content of the current element
                    algoData = getLayouterData(context.getProperty(CONTENT_HINT),
                            context.getProperty(CONTENT_DIAGT));
                    context.setProperty(CONTENT_ALGO, algoData);
                    break;
                default:
                    // add algorithm data for the container of the current element
                    algoData = getLayouterData(context.getProperty(CONTAINER_HINT),
                            context.getProperty(CONTAINER_DIAGT));
                    context.setProperty(CONTAINER_ALGO, algoData);
                }
                optionData.addAll(layoutDataService.getOptionData(algoData, target));
            }
            
            // add layout options that are available for the current element
            context.setProperty(OPTIONS, optionData);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Object getValue(final LayoutOptionData optionData, final LayoutContext context) {
        Object result = null;
        
        // check default value of the content layout algorithm
        LayoutAlgorithmData contentAlgoData = context.getProperty(CONTENT_ALGO);
        if (contentAlgoData != null && optionData.getTargets().contains(
                LayoutOptionData.Target.PARENTS)) {
            result = contentAlgoData.getDefaultValue(optionData);
            if (result != null) {
                return result;
            }
        }

        // check default value of the container layout provider
        LayoutAlgorithmData containerAlgoData = context.getProperty(CONTAINER_ALGO);
        if (containerAlgoData != null) {
            result = containerAlgoData.getDefaultValue(optionData);
            if (result != null) {
                return result;
            }
        }
        
        // fall back to default value of the option itself
        result = optionData.getDefault();
        if (result != null) {
            return result;
        }
        
        // fall back to default-default value
        return optionData.getDefaultDefault();
    }
    
    /**
     * Determine the most appropriate layout algorithm for the given layout hint and diagram type.
     * If no layout algorithms are registered, this returns {@code null}.
     * 
     * @param theLayoutHint either a layout algorithm identifier,
     *          or a layout type identifier, or {@code null}
     * @param diagramType a diagram type identifier
     * @return the most appropriate layout algorithm
     */
    public static LayoutAlgorithmData getLayouterData(final String theLayoutHint,
            final String diagramType) {
        String chDiagType = (diagramType == null || diagramType.length() == 0)
                ? LayoutDataService.DIAGRAM_TYPE_GENERAL : diagramType;
        LayoutDataService layoutServices = LayoutDataService.getInstance();
        String layoutHint = theLayoutHint;
        
        // try to get a specific provider for the given hint
        LayoutAlgorithmData directHitData = layoutServices.getAlgorithmData(layoutHint);
        if (directHitData != null) {
            return directHitData;
        }

        // look for the provider with highest priority, interpreting the hint as layout type
        LayoutAlgorithmData bestAlgo = null;
        int bestPrio = LayoutAlgorithmData.MIN_PRIORITY;
        boolean matchesLayoutType = false, matchesDiagramType = false, matchesGeneralDiagram = false;
        for (LayoutAlgorithmData currentAlgo : layoutServices.getAlgorithmData()) {
            int currentPrio = currentAlgo.getDiagramSupport(chDiagType);
            String layoutType = currentAlgo.getType();
            if (matchesLayoutType) {
                if (layoutType.length() > 0 && layoutType.equals(layoutHint)) {
                    if (matchesDiagramType) {
                        if (currentPrio > bestPrio) {
                            // the algorithm matches the layout type hint and has higher priority for
                            // the given diagram type than the previous one
                            bestAlgo = currentAlgo;
                            bestPrio = currentPrio;
                        }
                    } else {
                        if (currentPrio > LayoutAlgorithmData.MIN_PRIORITY) {
                            // the algorithm matches the layout type hint and has higher priority
                            // for the given diagram type than the previous one
                            bestAlgo = currentAlgo;
                            bestPrio = currentPrio;
                            matchesDiagramType = true;
                            matchesGeneralDiagram = false;
                        } else {
                            currentPrio = currentAlgo.getDiagramSupport(
                                    LayoutDataService.DIAGRAM_TYPE_GENERAL);
                            if (matchesGeneralDiagram) {
                                if (currentPrio > bestPrio) {
                                    // the algorithm matches the layout type hint and has higher
                                    // priority for general diagrams than the previous one
                                    bestAlgo = currentAlgo;
                                    bestPrio = currentPrio;
                                }
                            } else {
                                if (currentPrio > LayoutAlgorithmData.MIN_PRIORITY) {
                                    // the algorithm has a priority for general diagrams,
                                    // while the previous one did not have it
                                    bestAlgo = currentAlgo;
                                    bestPrio = currentPrio;
                                    matchesGeneralDiagram = true;
                                } else if (bestAlgo == null) {
                                    bestAlgo = currentAlgo;
                                }
                            }
                        }
                    }
                }
            } else {
                if (layoutType.length() > 0 && layoutType.equals(layoutHint)) {
                    // the first algorithm that matches the layout type hint is found
                    bestAlgo = currentAlgo;
                    matchesLayoutType = true;
                    if (currentPrio > LayoutAlgorithmData.MIN_PRIORITY) {
                        // the algorithm supports the given diagram type
                        bestPrio = currentPrio;
                        matchesDiagramType = true;
                        matchesGeneralDiagram = false;
                    } else {
                        matchesDiagramType = false;
                        currentPrio = currentAlgo.getDiagramSupport(
                                LayoutDataService.DIAGRAM_TYPE_GENERAL);
                        if (currentPrio > LayoutAlgorithmData.MIN_PRIORITY) {
                            // the algorithm does not support the given diagram type, but
                            // has a priority for general diagrams
                            bestPrio = currentPrio;
                            matchesGeneralDiagram = true;
                        } else {
                            matchesGeneralDiagram = false;
                        }
                    }
                } else {
                    if (matchesDiagramType) {
                        if (currentPrio > bestPrio) {
                            // the algorithm has higher priority for the given diagram type
                            // than the previous one
                            bestAlgo = currentAlgo;
                            bestPrio = currentPrio;
                        }
                    } else {
                        if (currentPrio > LayoutAlgorithmData.MIN_PRIORITY) {
                            // the first algorithm that matches the given diagram type is found
                            bestAlgo = currentAlgo;
                            bestPrio = currentPrio;
                            matchesDiagramType = true;
                            matchesGeneralDiagram = false;
                        } else {
                            currentPrio = currentAlgo.getDiagramSupport(
                                    LayoutDataService.DIAGRAM_TYPE_GENERAL);
                            if (matchesGeneralDiagram) {
                                if (currentPrio > bestPrio) {
                                    // the algorithm has higher priority for general diagrams
                                    // than the previous one
                                    bestAlgo = currentAlgo;
                                    bestPrio = currentPrio;
                                }
                            } else {
                                if (currentPrio > LayoutAlgorithmData.MIN_PRIORITY) {
                                    // the first algorithm with a priority for general diagrams is found
                                    bestAlgo = currentAlgo;
                                    bestPrio = currentPrio;
                                    matchesGeneralDiagram = true;
                                } else if (bestAlgo == null) {
                                    // if no match is found, the first algorithm in the list is returned
                                    bestAlgo = currentAlgo;
                                }
                            }
                        }
                    }
                }
            }
        }
        return bestAlgo;
    }

    /**
     * {@inheritDoc}
     */
    public Collection<IProperty<?>> getAffectedOptions(final LayoutContext context) {
        return Collections.emptyList();
    }

}
