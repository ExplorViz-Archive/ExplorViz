/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 * 
 * Copyright 2013 by
 * + Christian-Albrechts-University of Kiel
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 * 
 * This code is provided under the terms of the Eclipse Public License (EPL).
 * See the file epl-v10.html for the license text.
 */
package de.cau.cs.kieler.klay.layered.compound;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

import de.cau.cs.kieler.core.alg.IKielerProgressMonitor;
import de.cau.cs.kieler.core.math.KVector;
import de.cau.cs.kieler.core.math.KVectorChain;
import de.cau.cs.kieler.kiml.options.LayoutOptions;
import de.cau.cs.kieler.klay.layered.ILayoutProcessor;
import de.cau.cs.kieler.klay.layered.graph.LGraphUtil;
import de.cau.cs.kieler.klay.layered.graph.LEdge;
import de.cau.cs.kieler.klay.layered.graph.LGraph;
import de.cau.cs.kieler.klay.layered.graph.LNode;
import de.cau.cs.kieler.klay.layered.graph.LPort;
import de.cau.cs.kieler.klay.layered.p5edges.OrthogonalRoutingGenerator;
import de.cau.cs.kieler.klay.layered.properties.PortType;
import de.cau.cs.kieler.klay.layered.properties.Properties;

/**
 * Postprocess a compound graph by restoring cross-hierarchy edges that have previously been split
 * by the {@link CompoundGraphPreprocessor}.
 * 
 * <dl>
 *   <dt>Precondition:</dt>
 *     <dd>a compound graph with no layers and no cross-hierarchy edges, but with external ports
 *       and with fully specified layout.</dd>
 *   <dt>Postcondition:</dt>
 *     <dd>a compound graph with no layers and with the original cross-hierarchy edges;
 *       the layout applied to these edges conforms to the rules of the KGraph meta model.</dd>
 * </dl>
 *
 * @author msp
 */
public class CompoundGraphPostprocessor implements ILayoutProcessor {
    
    /**
     * {@inheritDoc}
     */
    public void process(final LGraph graph, final IKielerProgressMonitor monitor) {
        monitor.begin("Compound graph postprocessor", 1);
        Multimap<LEdge, CrossHierarchyEdge> crossHierarchyMap = graph.getProperty(
                Properties.CROSS_HIERARCHY_MAP);
        
        for (LEdge origEdge : crossHierarchyMap.keySet()) {
            List<CrossHierarchyEdge> crossHierarchyEdges = new ArrayList<CrossHierarchyEdge>(
                    crossHierarchyMap.get(origEdge));
           
            // put the cross-hierarchy edges in proper order from source to target
            Collections.sort(crossHierarchyEdges, new Comparator<CrossHierarchyEdge>() {
                public int compare(final CrossHierarchyEdge edge1, final CrossHierarchyEdge edge2) {
                    if (edge1.getType() == PortType.OUTPUT
                            && edge2.getType() == PortType.INPUT) {
                        return -1;
                    } else if (edge1.getType() == PortType.INPUT
                            && edge2.getType() == PortType.OUTPUT) {
                        return 1;
                    }
                    int level1 = hierarchyLevel(edge1.getGraph(), graph);
                    int level2 = hierarchyLevel(edge2.getGraph(), graph);
                    if (edge1.getType() == PortType.OUTPUT) {
                        // from deeper level to higher level
                        return level2 - level1;
                    } else {
                        // from higher level to deeper level
                        return level1 - level2;
                    }
                }
            });
            LPort sourcePort = crossHierarchyEdges.get(0).getActualSource();
            LPort targetPort = crossHierarchyEdges.get(crossHierarchyEdges.size() - 1)
                    .getActualTarget();
            origEdge.getBendPoints().clear();
            
            // determine the reference graph for all bend points
            LNode referenceNode = sourcePort.getNode();
            LGraph referenceGraph;
            if (LGraphUtil.isDescendant(targetPort.getNode(), referenceNode)) {
                referenceGraph = referenceNode.getProperty(Properties.NESTED_LGRAPH);
            } else {
                referenceGraph = referenceNode.getGraph();
            }

            // check whether there are any junction points
            KVectorChain junctionPoints = origEdge.getProperty(LayoutOptions.JUNCTION_POINTS);
            if (Iterables.any(crossHierarchyEdges, new Predicate<CrossHierarchyEdge>() {
                public boolean apply(final CrossHierarchyEdge chEdge) {
                    KVectorChain jps = chEdge.getEdge().getProperty(LayoutOptions.JUNCTION_POINTS);
                    return jps != null && !jps.isEmpty();
                }
            })) {
                if (junctionPoints == null) {
                    junctionPoints = new KVectorChain();
                    origEdge.setProperty(LayoutOptions.JUNCTION_POINTS, junctionPoints);
                } else {
                    junctionPoints.clear();
                }
            } else if (junctionPoints != null) {
                origEdge.setProperty(LayoutOptions.JUNCTION_POINTS, null);
            }
            
            // apply the computed layouts to the cross-hierarchy edge
            KVector lastPoint = null;
            for (CrossHierarchyEdge chEdge : crossHierarchyEdges) {
                
                // transform all coordinates from the graph of the dummy edge to the reference graph
                KVector offset = new KVector();
                LGraphUtil.changeCoordSystem(offset, chEdge.getGraph(), referenceGraph);
                
                LEdge ledge = chEdge.getEdge();
                KVectorChain bendPoints = ledge.getBendPoints().translate(offset);
                // Note: if an NPE occurs here, that means KLay Layered has replaced the original edge
                KVector sourcePoint = ledge.getSource().getAbsoluteAnchor().add(offset);
                KVector targetPoint = ledge.getTarget().getAbsoluteAnchor().add(offset);

                if (lastPoint != null) {
                    KVector nextPoint;
                    if (bendPoints.isEmpty()) {
                        nextPoint = targetPoint;
                    } else {
                        nextPoint = bendPoints.getFirst();
                    }
                    if (Math.abs(lastPoint.x - nextPoint.x) > OrthogonalRoutingGenerator.TOLERANCE
                        && Math.abs(lastPoint.y - nextPoint.y) > OrthogonalRoutingGenerator.TOLERANCE) {
                        // add the source point as bend point to properly connect the hierarchy levels
                        origEdge.getBendPoints().add(sourcePoint);
                    }
                }

                origEdge.getBendPoints().addAll(bendPoints);
                
                if (bendPoints.isEmpty()) {
                    lastPoint = sourcePoint;
                } else {
                    lastPoint = bendPoints.getLast();
                }
                
                // copy junction points
                KVectorChain ledgeJPs = ledge.getProperty(LayoutOptions.JUNCTION_POINTS);
                if (ledgeJPs != null) {
                    junctionPoints.addAll(ledgeJPs.translate(offset));
                }
                
                // add offset to target port with a special property
                if (chEdge.getActualTarget() == targetPort) {
                    if (targetPort.getNode().getGraph() != chEdge.getGraph()) {
                        // the target port is in a different coordinate system -- recompute the offset
                        offset = new KVector();
                        LGraphUtil.changeCoordSystem(offset, targetPort.getNode().getGraph(),
                                referenceGraph);
                    }
                    origEdge.setProperty(Properties.TARGET_OFFSET, offset);
                }
                
                // remove the dummy edge from the graph (dummy ports and dummy nodes are retained)
                ledge.setSource(null);
                ledge.setTarget(null);
            }
            
            // restore the original source port and target port
            origEdge.setSource(sourcePort);
            origEdge.setTarget(targetPort);
        }

        monitor.done();
    }
    
    /**
     * Compute the hierarchy level of the given nested graph.
     * 
     * @param nestedGraph a nested graph
     * @param topLevelGraph the top-level graph
     * @return the hierarchy level (higher number means the node is nested deeper)
     */
    private static int hierarchyLevel(final LGraph nestedGraph, final LGraph topLevelGraph) {
        LGraph currentGraph = nestedGraph;
        int level = 0;
        do {
            if (currentGraph == topLevelGraph) {
                return level;
            }
            LNode currentNode = currentGraph.getProperty(Properties.PARENT_LNODE);
            if (currentNode == null) {
                // the given node is not an ancestor of the graph node
                throw new IllegalArgumentException();
            }
            currentGraph = currentNode.getGraph();
            level++;
        } while (true);
    }

}
