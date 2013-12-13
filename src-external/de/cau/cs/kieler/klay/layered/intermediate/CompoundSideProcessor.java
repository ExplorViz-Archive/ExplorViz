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
import de.cau.cs.kieler.core.kgraph.KNode;
import de.cau.cs.kieler.kiml.klayoutdata.KInsets;
import de.cau.cs.kieler.kiml.options.Alignment;
import de.cau.cs.kieler.kiml.options.LayoutOptions;
import de.cau.cs.kieler.kiml.options.PortSide;
import de.cau.cs.kieler.kiml.util.KimlUtil;
import de.cau.cs.kieler.klay.layered.ILayoutProcessor;
import de.cau.cs.kieler.klay.layered.Util;
import de.cau.cs.kieler.klay.layered.graph.LEdge;
import de.cau.cs.kieler.klay.layered.graph.LNode;
import de.cau.cs.kieler.klay.layered.graph.LPort;
import de.cau.cs.kieler.klay.layered.graph.Layer;
import de.cau.cs.kieler.klay.layered.graph.LGraph;
import de.cau.cs.kieler.klay.layered.importexport.CompoundKGraphImporter;
import de.cau.cs.kieler.klay.layered.properties.EdgeType;
import de.cau.cs.kieler.klay.layered.properties.NodeType;
import de.cau.cs.kieler.klay.layered.properties.Properties;

/**
 * Sets up dummy nodes at the sides of a compound node, connects these nodes with dummy edges. The
 * linear segments node placer will place the dummy nodes in a line, such allowing for the compound
 * node borders to be drawn. This approach is inspired by Georg Sander,
 * "Layout of Compound Directed Graphs", Technical Report A/03/96, Universität des Saarlandes, 1996
 * 
 * <dl>
 * <dt>Precondition:</dt>
 * <dd>a layered graph with fixed node ordering.</dd>
 * <dt>Postcondition:</dt>
 * <dd>two dummy nodes of type
 * {@link de.cau.cs.kieler.klay.layered.properties.NodeType#COMPOUND_SIDE are inserted for each
 * compound node for each layer it spans, one above all nodes belonging to that subgraph, one
 * below.}.</dd>
 * <dt>Slots:</dt>
 * <dd>After phase 3.</dd>
 * <dt>Same-slot dependencies:</dt>
 * <dd>None.</dd>
 * </dl>
 * 
 * @author ima
 * @kieler.design 2012-08-10 chsch grh
 */
public final class CompoundSideProcessor implements ILayoutProcessor {

    /**
     * {@inheritDoc}
     */
    public void process(final LGraph layeredGraph, final IKielerProgressMonitor monitor) {
        monitor.begin("Set Compound Side nodes", 1);
        List<Layer> layers = layeredGraph.getLayers();
        List<LNode> openingBorders = new LinkedList<LNode>();

        // Iterate layers to find the UPPER_COMPOUND_BORDER-Nodes indicating the beginning of a
        // compound node
        for (Layer layer : layers) {
            for (LNode lnode : layer) {
                if (lnode.getProperty(Properties.NODE_TYPE) == NodeType.UPPER_COMPOUND_BORDER) {
                    openingBorders.add(lnode);
                }
            }
        }
        // Insert dummy node lines at the side of every subgraph node
        for (LNode lnode : openingBorders) {
            Layer spanEnd = findSpanEnd(lnode, layers);
            int startIndex = lnode.getLayer().getIndex();
            int endIndex = spanEnd.getIndex();
            insertSideDummies(startIndex, endIndex, layers, lnode, null, null, layeredGraph);
        }
        monitor.done();
    }

    /**
     * Recursively inserts two compound side dummy nodes to given layer, one above all nodes of the
     * subgraph the given LNode is upper border dummy of, one below them, for each layer between
     * startIndex and endIndex (inclusive).
     * 
     * @param startIndex
     *            layerIndex to start with.
     * @param endIndex
     *            layerIndex to end with. are to be inserted.
     * @param layers
     *            the complete list of Layers.
     * @param openingBorder
     *            the upper border dummy node determining the subgraph.
     * @param lowerConnector
     *            the edge to connect the side dummy node on top of the layer to.
     * @param upperConnector
     *            the edge to connect the side dummy node at the bottom of the layer to.
     * @param layeredGraph
     */
    private void insertSideDummies(final int startIndex, final int endIndex,
            final List<Layer> layers, final LNode openingBorder, final LEdge lowerConnector,
            final LEdge upperConnector, final LGraph layeredGraph) {     

        // get the insets for origin of openingBorder
        KInsets insets = openingBorder.getProperty(Properties.ORIGINAL_INSETS);

        // get the border spacing for the compound node
        float borderSpacing = openingBorder.getProperty(Properties.BORDER_SPACING);

        Layer layer = layers.get(startIndex);
        List<LNode> layerNodes = layer.getNodes();

        int lowerIndex = findUltimateIndex(layer, openingBorder, true, layeredGraph);
        int upperIndex = findUltimateIndex(layer, openingBorder, false, layeredGraph);

        // create lower side node (higher layer index)
        LNode lowerSideDummy = new LNode(layeredGraph);
        lowerSideDummy.setProperty(LayoutOptions.ALIGNMENT, Alignment.LEFT);
        lowerSideDummy.getSize().y = insets.getBottom() + borderSpacing;
        lowerSideDummy.setProperty(Properties.NODE_TYPE, NodeType.COMPOUND_SIDE);
        lowerSideDummy.setProperty(Properties.SIDE_OWNER, openingBorder);

        // avoid index-out-of-bounds-exception
        if (layerNodes.size() == lowerIndex) {
            lowerSideDummy.setLayer(layer);
        } else {
            lowerSideDummy.setLayer(lowerIndex + 1, layer);
        }

        // create upper side node (lower layer index)
        LNode upperSideDummy = new LNode(layeredGraph);
        upperSideDummy.setProperty(LayoutOptions.ALIGNMENT, Alignment.LEFT);
        upperSideDummy.getSize().y = insets.getTop() + borderSpacing;
        upperSideDummy.setProperty(Properties.NODE_TYPE, NodeType.COMPOUND_SIDE);
        upperSideDummy.setProperty(Properties.SIDE_OWNER, openingBorder);
        upperSideDummy.setLayer(upperIndex, layer);

        // create ports for connection-edges
        LPort lowPortWest = new LPort(layeredGraph);
        lowPortWest.setSide(PortSide.WEST);
        lowPortWest.setNode(lowerSideDummy);

        LPort lowPortEast = new LPort(layeredGraph);
        lowPortEast.setSide(PortSide.EAST);
        lowPortEast.setNode(lowerSideDummy);

        LPort highPortWest = new LPort(layeredGraph);
        highPortWest.setSide(PortSide.WEST);
        highPortWest.setNode(upperSideDummy);

        LPort highPortEast = new LPort(layeredGraph);
        highPortEast.setSide(PortSide.EAST);
        highPortEast.setNode(upperSideDummy);

        // connect to connection-edges from predecessor, if existent.
        if (lowerConnector != null) {
            lowerConnector.setTarget(lowPortWest);
        }
        if (upperConnector != null) {
            upperConnector.setTarget(highPortWest);
        }

        if (startIndex < endIndex) {
            // create connection-edges to successor, if not last index.
            LEdge lowerEdge = new LEdge(layeredGraph);
            LEdge upperEdge = new LEdge(layeredGraph);

            lowerEdge.setProperty(Properties.EDGE_TYPE, EdgeType.COMPOUND_SIDE);
            upperEdge.setProperty(Properties.EDGE_TYPE, EdgeType.COMPOUND_SIDE);

            lowerEdge.setSource(lowPortEast);
            upperEdge.setSource(highPortEast);

            // handle next layer
            insertSideDummies(startIndex + 1, endIndex, layers, openingBorder, lowerEdge,
                    upperEdge, layeredGraph);
        }
    }

    /**
     * Finds the layer, in which the closing Compound Dummy Nodes for upperBorder are placed. Make
     * sure the LNode you pass as a parameter is of the type UPPER_COMPOUND_BORDER.
     * 
     * @param upperBorder
     *            upper compound border dummy node for which the partner lower compound border dummy
     *            node is to be found.
     * @param layers
     *            list of all layers.
     * @return returns layer, in which the closing compound dummy nodes for the given border node
     *         are placed.
     */
    public static Layer findSpanEnd(final LNode upperBorder, final List<Layer> layers) {
        LNode spanEndNode = null;
        // Find the corresponding lower compound border for upperBorder
        for (Layer layer : layers) {
            for (LNode lnode : layer) {
                if ((lnode.getProperty(Properties.NODE_TYPE) == NodeType.LOWER_COMPOUND_BORDER)
                        && (lnode.getProperty(Properties.ORIGIN) == upperBorder
                                .getProperty(Properties.ORIGIN))) {
                    spanEndNode = lnode;
                    break;
                }
            }
        }
        Layer ret = spanEndNode.getLayer();
        return ret;
    }

    /**
     * Finds the Node highest resp. lowest node in the ordering of the given layer that is
     * representing the given compoundNode or any of its descendants.
     * 
     * @param layer
     *            the layering, whose order is to be searched.
     * @param upperBorder
     *            the compound node whose descendants are to be respected.
     * @param lowerSide
     *            flag indicating, whether the highest (true) or lowest (false) ordered node is to
     *            be found.
     * @param layeredGraph
     * @return returns the Node highest in the ordering of the layer among those representing the
     *         compoundNode or any of its descendants.
     */
    private int findUltimateIndex(final Layer layer, final LNode upperBorder,
            final boolean lowerSide, final LGraph layeredGraph) {
        List<LNode> nodes = layer.getNodes();
        KNode upperBorderOrigin = (KNode) upperBorder.getProperty(Properties.ORIGIN);
        int ret = 0;
        // to find the minimum, initialize with highest index
        if (!lowerSide) {
            ret = nodes.size() - 1;
        }
        // find the minimum resp. maximum index of the layer that is inhabited by a node
        // representing an original node, dummy nodes are not considered
        for (LNode lnode : nodes) {
            if (lnode.getProperty(Properties.ORIGIN) instanceof KNode) {
                KNode origin = (KNode) lnode.getProperty(Properties.ORIGIN);
                if (CompoundKGraphImporter.isDescendant(upperBorderOrigin, origin)) {
                    ret = compareIndex(lnode, ret, lowerSide);
                }
            }
            if (lnode.getProperty(Properties.NODE_TYPE) == NodeType.LONG_EDGE) {
                LNode sourceNode = lnode.getProperty(Properties.LONG_EDGE_SOURCE).getNode();
                LNode targetNode = lnode.getProperty(Properties.LONG_EDGE_TARGET).getNode();
                if (sourceNode.getProperty(Properties.PARENT) == targetNode
                        .getProperty(Properties.PARENT)) {
                    if ((Util.isDescendant(sourceNode, upperBorder))
                            || (Util.isDescendant(targetNode, upperBorder))) {
                        ret = compareIndex(lnode, ret, lowerSide);
                    }
                } else {
                    if ((Util.isDescendant(targetNode, upperBorder)) 
                            || (Util.isDescendant(sourceNode, upperBorder))) {
                        ret = compareIndex(lnode, ret, lowerSide);
                    }
                }
            }
            // keep north-south-port dummies and their nodes together
            if (lnode.getProperty(Properties.NODE_TYPE) == NodeType.NORTH_SOUTH_PORT) {
                LNode portNode = lnode.getProperty(Properties.IN_LAYER_LAYOUT_UNIT);
                KNode originPortNode = (KNode) portNode.getProperty(Properties.ORIGIN);
                if (KimlUtil.isDescendant(originPortNode, upperBorderOrigin)) {
                    ret = compareIndex(lnode, ret, lowerSide);
                }
            }
        }
        return ret;
    }

    /**
     * Compares a node's index to an actual index to update a maximum/minimum value.
     * 
     * @param lnode
     *            node whose index is to be compared.
     * @param actualValue
     *            the actual maximum/minimum value.
     * @param lowerSide
     *            if true, the maximum is to be updated, if false, the minimum.
     * @return returns the updated max or min value.
     */
    private int compareIndex(final LNode lnode, final int actualValue, final boolean lowerSide) {

        int test = lnode.getIndex();
        int ret = 0;
        ret += actualValue;
        if (lowerSide) {
            if (test > ret) {
                ret = test;
            }
        } else {
            if (test < ret) {
                ret = test;
            }
        }
        return ret;
    }

}
