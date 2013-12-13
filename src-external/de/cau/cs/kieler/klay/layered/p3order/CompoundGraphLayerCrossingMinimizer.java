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
package de.cau.cs.kieler.klay.layered.p3order;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import de.cau.cs.kieler.core.kgraph.KGraphElement;
import de.cau.cs.kieler.core.kgraph.KNode;
import de.cau.cs.kieler.kiml.options.LayoutOptions;
import de.cau.cs.kieler.klay.layered.Util;
import de.cau.cs.kieler.klay.layered.graph.LGraphElement;
import de.cau.cs.kieler.klay.layered.graph.LNode;
import de.cau.cs.kieler.klay.layered.graph.LGraph;
import de.cau.cs.kieler.klay.layered.properties.Properties;

/**
 * Implements the actual crossing minimization step for a given free layer. In a flat graph, an
 * ICrossingMinimizationHeuristic is directly used to compute the node ordering. For compound graphs
 * it computes orderings for each compound node separately, working its way from the innermost
 * compound node to the outermost, using calculated node orders as atomic sets of nodes for the next
 * calculations. This approach is inspired by
 * <ul>
 * <li>Michael Forster. Applying crossing reduction strategies to layered compound graphs. In
 * <i>Graph Drawing</i>, volume 2528 of LNCS, pp. 115-132. Springer, 2002.</li>
 * </ul> 
 * 
 * @author ima
 * @author cds
 * @author msp
 * @kieler.design proposed by msp
 * @kieler.rating proposed yellow by msp
 */
public final class CompoundGraphLayerCrossingMinimizer {

    /** the layered graph that is processed. */
    private LGraph layeredGraph;
    /** the crossing minimization heuristic. */
    private ICrossingMinimizationHeuristic crossminHeuristic;

    /**
     * Constructs a compound graph layer crossing minimizer.
     * 
     * @param layeredGraph
     *            The layered graph that is to be laid out
     * @param heuristic
     *            the crossing minimization heuristic
     */
    public CompoundGraphLayerCrossingMinimizer(final LGraph layeredGraph,
            final ICrossingMinimizationHeuristic heuristic) {
        this.layeredGraph = layeredGraph;
        this.crossminHeuristic = heuristic;
    }

    /**
     * Uses an ICrossingMinimizationHeuristic to compute a node order for a given layer. If the
     * graph to be laid out is a compound graph, it prepares slices of work for the
     * ICrossingMinimizationHeuristic: Each compound node is ordered separately, from the innermost
     * one to the outermost. After ordering, the nodes of a compound node are treated as an atomic
     * entity with fixed node order, represented by a NodeGroup for the next calculations.
     * 
     * @param layer
     *            the nodes of the layer, whose order is to be determined
     * @param layerIndex
     *            the index of that layer
     * @param forward
     *            whether the fixed layer is located after the free layer
     * @param preOrdered
     *            whether the nodes have been ordered in a previous run
     * @param randomize
     *            whether the layer's node order should just be randomized
     */
    public void compoundMinimizeCrossings(final NodeGroup[] layer, final int layerIndex,
            final boolean forward, final boolean preOrdered, final boolean randomize) {
        // Ignore empty free layers
        if (layer.length == 0) {
            return;
        }

        // determine whether a compound graph is to be laid out
        boolean isCompound = layeredGraph.getProperty(LayoutOptions.LAYOUT_HIERARCHY);
        
        if (!isCompound) {
            List<NodeGroup> nodeGroups = new LinkedList<NodeGroup>();
            for (NodeGroup ng : layer) {
                nodeGroups.add(ng);
            }
            
            // minimize crossings in the given layer
            crossminHeuristic.minimizeCrossings(nodeGroups, layerIndex, preOrdered, randomize, forward);
            // apply the new ordering
            applyNodeGroupOrderingToNodeArray(nodeGroups, layer);
            
        } else {
            // sort the layer's nodes according to their related compound nodes. Find out the
            // maximal depth on the run.
            HashMap<LNode, LinkedList<NodeGroup>> compoundNodesMap 
                = new HashMap<LNode, LinkedList<NodeGroup>>();
            // Remember the order of processing for the compound nodes. This list will contain the
            // same nodes as the keySet of the compoundNodesMap. However, as order matters, the
            // latter can not be used for iteration.
            LinkedList<LNode> compoundNodesMapKeys = new LinkedList<LNode>();

            int maximalDepth = 0;

            // prepare an LNode as key representing the layeredGraph in HashMaps
            LNode graphKey = new LNode(layeredGraph);
            graphKey.setProperty(Properties.ORIGIN, layeredGraph);
            
            for (NodeGroup nodeGroup : layer) {
                // The correlation node/compoundNode is the same as in the SubGraphOrderingProcessor
                LNode key = Util.getRelatedCompoundNode(nodeGroup.getNode(), layeredGraph);
                // If node is contained by the layeredGraph directly, getRelatedCompoundNode has
                // returned null. Use the graphkey in this case.
                if (key == null) {
                    key = graphKey;
                }
                LinkedList<NodeGroup> relatedList = compoundNodesMap.get(key);
                if (relatedList == null) {
                    relatedList = new LinkedList<NodeGroup>();
                    compoundNodesMap.put(key, relatedList);
                    compoundNodesMapKeys.add(key);
                }
                relatedList.add(nodeGroup);
                int keydepth = key.getProperty(Properties.DEPTH);
                if (keydepth > maximalDepth) {
                    maximalDepth = keydepth;
                }
            }
            // Sort the relevant compound nodes into lists sorted by depth. Index 0 means list of
            // nodes with depth 0.
            LinkedList<LinkedList<LNode>> compoundNodesPerDepthLevel 
                = new LinkedList<LinkedList<LNode>>();
            for (int i = 0; i <= maximalDepth; i++) {
                LinkedList<LNode> depthList = new LinkedList<LNode>();
                compoundNodesPerDepthLevel.add(depthList);
            }
            for (LNode compoundNode : compoundNodesMapKeys) {
                compoundNodesPerDepthLevel.get(compoundNode.getProperty(Properties.DEPTH)).add(
                        compoundNode);
            }

            // get the map associating LGraph-elements and the original KGraph-Elements
            HashMap<KGraphElement, LGraphElement> elemMap = layeredGraph
                    .getProperty(Properties.ELEMENT_MAP);

            while (!compoundNodesPerDepthLevel.isEmpty()) {
                // Handle the compound nodes beginning from the highest depth level up to the
                // lowest.
                LinkedList<LNode> actualList = compoundNodesPerDepthLevel.removeLast();
                // Process the compound nodes of the actual depth level.
                for (LNode keyNode : actualList) {
                    LinkedList<NodeGroup> compoundContent = compoundNodesMap.get(keyNode);
                    
                    // Calculate the nodeOrder for this compound node.
                    crossminHeuristic.minimizeCrossings(compoundContent, layerIndex, preOrdered,
                            randomize, forward);
                    
                    // Is outermost level reached? If not, represent the compound node as one entity
                    // for the higher levels. Update compoundNodesMap and compoundNodesPerDepthLevel.
                    if (keyNode != graphKey) {
                        // Create a NodeGroup comprising all Nodes of this compound node, preserving
                        // the order
                        NodeGroup aggregatedNodeGroup;
                        if (compoundContent.size() == 1) {
                            aggregatedNodeGroup = compoundContent.getFirst();
                        } else {
                            aggregatedNodeGroup = new NodeGroup(compoundContent.removeFirst(),
                                    compoundContent.removeLast());
                            while (!compoundContent.isEmpty()) {
                                aggregatedNodeGroup = new NodeGroup(aggregatedNodeGroup,
                                        compoundContent.removeFirst());
                            }
                        }
                        
                        // Store the new nodeGroup representing the compound node in the
                        // compoundNodesMap with the parent of the compoundNode as a key.
                        KNode keyNodeParent = keyNode.getProperty(Properties.K_PARENT);
                        LGraphElement parentRepresentative = elemMap.get(keyNodeParent);
                        LNode parentKey;
                        if (parentRepresentative instanceof LGraph) {
                            parentKey = graphKey;
                        } else {
                            assert (parentRepresentative instanceof LNode);
                            parentKey = (LNode) parentRepresentative;
                        }
                        LinkedList<NodeGroup> parentContents = compoundNodesMap.get(parentKey);
                        if (parentContents == null) {
                            parentContents = new LinkedList<NodeGroup>();
                            compoundNodesMap.put(parentKey, parentContents);
                            compoundNodesMapKeys.add(parentKey);
                        }
                        parentContents.add(aggregatedNodeGroup);

                        // Store the parent of the compoundNode in the
                        // compoundNodesPerDepthLevel-list if not already present
                        LinkedList<LNode> parentList = compoundNodesPerDepthLevel.get(parentKey
                                .getProperty(Properties.DEPTH));
                        if (!parentList.contains(parentKey)) {
                            parentList.add(parentKey);
                        }
                    } else {
                        applyNodeGroupOrderingToNodeArray(compoundContent, layer);
                    }
                }
            }
        }
    }

    /**
     * Apply the node order as determined by the sorted list of vertices to the free layer array.
     * 
     * @param nodeGroups
     *            sorted array of vertices.
     * @param freeLayer
     *            array of nodes to apply the ordering to.
     */
    private void applyNodeGroupOrderingToNodeArray(final List<NodeGroup> nodeGroups,
            final NodeGroup[] freeLayer) {
        int index = 0;

        for (NodeGroup nodeGroup : nodeGroups) {
            for (LNode node : nodeGroup.getNodes()) {
                freeLayer[index++] = node.getProperty(Properties.NODE_GROUP);
            }
        }
    }
    
}
