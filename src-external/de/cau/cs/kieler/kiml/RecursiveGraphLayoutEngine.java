/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 * 
 * Copyright 2008 by
 * + Christian-Albrechts-University of Kiel
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 * 
 * This code is provided under the terms of the Eclipse Public License (EPL).
 * See the file epl-v10.html for the license text.
 */
package de.cau.cs.kieler.kiml;

import de.cau.cs.kieler.core.alg.IKielerProgressMonitor;
import de.cau.cs.kieler.core.kgraph.KNode;
import de.cau.cs.kieler.kiml.config.DefaultLayoutConfig;
import de.cau.cs.kieler.kiml.klayoutdata.KShapeLayout;
import de.cau.cs.kieler.kiml.options.GraphFeature;
import de.cau.cs.kieler.kiml.options.LayoutOptions;

/**
 * Performs layout on a graph with hierarchy by executing a layout algorithm on each level of the
 * hierarchy. This is done recursively from the leafs to the root of the nodes in the graph, using
 * size information from lower levels in the levels above.
 * 
 * @kieler.design 2011-03-14 reviewed by cmot, cds
 * @kieler.rating yellow 2012-08-10 review KI-23 by cds, sgu
 * @author ars
 * @author msp
 */
public class RecursiveGraphLayoutEngine implements IGraphLayoutEngine {

    /**
     * Performs recursive layout on the given layout graph.
     * 
     * @param layoutGraph top-level node of the graph to be laid out
     * @param progressMonitor monitor to which progress of the layout algorithms is reported
     */
    public void layout(final KNode layoutGraph, final IKielerProgressMonitor progressMonitor) {
        int nodeCount = countNodesRecursively(layoutGraph, true);
        progressMonitor.begin("Recursive Graph Layout", nodeCount);
        
        // perform recursive layout of the whole substructure of the given node
        layoutRecursively(layoutGraph, progressMonitor);
        
        progressMonitor.done();
    }

    /**
     * Recursive function to enable layout of hierarchy. The leafs are laid out
     * first to use their layout information in the levels above.
     * 
     * @param layoutNode the node with children to be laid out
     * @param progressMonitor monitor used to keep track of progress
     */
    private void layoutRecursively(final KNode layoutNode,
            final IKielerProgressMonitor progressMonitor) {
        
        KShapeLayout layoutNodeShapeLayout = layoutNode.getData(KShapeLayout.class);
        
        if (!layoutNode.getChildren().isEmpty()
                && !layoutNodeShapeLayout.getProperty(LayoutOptions.NO_LAYOUT)) {
            
            // this node has children and is thus a compound node;
            // fetch the layout algorithm that should be used to compute a layout for its content
            LayoutAlgorithmData algorithmData = getAlgorithm(layoutNode);
            AbstractLayoutProvider layoutProvider = algorithmData.getInstancePool().fetch();
            
            // if the layout provider supports hierarchy, it is expected to layout the node's compound
            // node children as well
            int nodeCount;
            if (layoutNodeShapeLayout.getProperty(LayoutOptions.LAYOUT_HIERARCHY)
                    && (algorithmData.getFeatureSupport(GraphFeature.COMPOUND)
                            > LayoutAlgorithmData.MIN_PRIORITY
                        || algorithmData.getFeatureSupport(GraphFeature.CLUSTERS)
                            > LayoutAlgorithmData.MIN_PRIORITY)) {
                
                // the layout algorithm will compute a layout for all levels of hierarchy under the
                // current one
                nodeCount = countNodesRecursively(layoutNode, false);
            } else {
                // layout each compound node contained in this node separately
                nodeCount = layoutNode.getChildren().size();
                for (KNode child : layoutNode.getChildren()) {
                    layoutRecursively(child, progressMonitor);
                    if (progressMonitor.isCanceled()) {
                        return;
                    }
                }
            }

            // perform layout on the current hierarchy level
            layoutProvider.doLayout(layoutNode, progressMonitor.subTask(nodeCount));
            algorithmData.getInstancePool().release(layoutProvider);
        }
    }

    /**
     * Returns the most appropriate layout provider for the given node.
     * 
     * @param layoutNode node for which a layout provider is requested
     * @return a layout provider instance that fits the layout hints for the given node
     */
    private LayoutAlgorithmData getAlgorithm(final KNode layoutNode) {
        KShapeLayout nodeLayout = layoutNode.getData(KShapeLayout.class);
        String layoutHint = nodeLayout.getProperty(LayoutOptions.ALGORITHM);
        String diagramType = nodeLayout.getProperty(LayoutOptions.DIAGRAM_TYPE);
        LayoutAlgorithmData algorithmData = DefaultLayoutConfig.getLayouterData(
                layoutHint, diagramType);
        if (algorithmData == null) {
            throw new IllegalStateException("No registered layout algorithm is available.");
        }
        return algorithmData;
    }

    /**
     * Determines the total number of layout nodes in the given layout graph.
     * 
     * @param layoutNode parent layout node to examine
     * @param countAncestors if true, the nodes on the ancestors path are also counted
     * @return total number of child layout nodes
     */
    private int countNodesRecursively(final KNode layoutNode, final boolean countAncestors) {
        // count the content of the given node
        int count = layoutNode.getChildren().size();
        for (KNode childNode : layoutNode.getChildren()) {
            if (!childNode.getChildren().isEmpty()) {
                count += countNodesRecursively(childNode, false);
            }
        }
        // count the ancestors path
        if (countAncestors) {
            KNode parent = layoutNode.getParent();
            while (parent != null) {
                count += parent.getChildren().size();
                parent = parent.getParent();
            }
        }
        return count;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isActive() {
        return true;
    }

}
