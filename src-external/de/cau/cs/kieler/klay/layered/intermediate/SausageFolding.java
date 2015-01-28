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
package de.cau.cs.kieler.klay.layered.intermediate;

import java.util.List;
import java.util.ListIterator;

import com.google.common.collect.Lists;

import de.cau.cs.kieler.core.alg.IKielerProgressMonitor;
import de.cau.cs.kieler.kiml.options.Direction;
import de.cau.cs.kieler.kiml.options.LayoutOptions;
import de.cau.cs.kieler.kiml.options.PortConstraints;
import de.cau.cs.kieler.kiml.options.PortSide;
import de.cau.cs.kieler.klay.layered.ILayoutProcessor;
import de.cau.cs.kieler.klay.layered.graph.LEdge;
import de.cau.cs.kieler.klay.layered.graph.LGraph;
import de.cau.cs.kieler.klay.layered.graph.LNode;
import de.cau.cs.kieler.klay.layered.graph.LPort;
import de.cau.cs.kieler.klay.layered.graph.Layer;
import de.cau.cs.kieler.klay.layered.properties.InternalProperties;
import de.cau.cs.kieler.klay.layered.properties.NodeType;
import de.cau.cs.kieler.klay.layered.properties.Properties;

/**
 * Intermediate processor that splits path-like graphs into 
 * multiple sub-paths to improve the overall aspect ratio of the drawing.
 * 
 * <p>
 * This is a rather problem-specific intermediate processor 
 * that works only on a very small subset of graphs, namely 
 * paths that have very few branches.
 * Also, it only works in conjunction with the 
 * {@link de.cau.cs.kieler.klay.layered.p2layers.LongestPathLayerer}.
 * </p>
 * 
 * <p>
 * What we do is the following. After layering and crossing minimization
 * we determine into how many "rows" we want to split the path.
 * Let n denote the number of nodes of each row. We start moving 
 * the node in layer n+1 into layer 1, node n+2 into layer 2 and so on.
 * While doing this we have to assure that the order within each 
 * layer is correct, have to insert dummy nodes for the edge that now 
 * spans from layer n+1 to layer 1, and have to treat inverted ports
 * specifically.
 * </p>
 * 
 * <dl>
 *   <dt>Precondition:</dt><dd>A layering has been calculated by the longest
 *   path layerer and the crossing minimization has finished.</dd>
 *   <dt>Postcondition:</dt><dd>Long paths are split such that the 
 *   aspect ratio is closer to the currently available area.</dd>
 *   <dt>Slots:</dt><dd>Before phase 4.</dd>
 * </dl>
 * 
 * @author uru
 */
public class SausageFolding implements ILayoutProcessor {

    private double spacing = 0;
    private double inLayerSpacing = 0;

    /**
     * {@inheritDoc}
     */
    public void process(final LGraph graph, final IKielerProgressMonitor progressMonitor) {

        spacing = graph.getProperty(Properties.OBJ_SPACING).doubleValue();
        inLayerSpacing = spacing * graph.getProperty(Properties.OBJ_SPACING_IN_LAYER_FACTOR);

        // determine the maximal dimensions of layers
        double maxHeight = determineMaximalHeight(graph); // sum of heights of nodes in the layer
        int longestPath = graph.getLayers().size();
        double maxWidth = determineMaximalWidth(graph); // maximum node in one layer

        double sumWidth = longestPath * maxWidth;
        
        // since the graph direction may be horizontal (default) or vertical,
        //  the desired aspect ration must be adjusted correspondingly
        //  (Klay internally always assumes left to right, however the
        //  aspect ratio is not adjusted during graph import)
        double desiredAR;
        final Direction dir = graph.getProperty(LayoutOptions.DIRECTION);
        if (dir == Direction.LEFT || dir == Direction.RIGHT || dir == Direction.UNDEFINED) {
            desiredAR = graph.getProperty(Properties.ASPECT_RATIO).doubleValue();
        } else {
            desiredAR = 1 / graph.getProperty(Properties.ASPECT_RATIO);
        }
        
        double currentAR = (sumWidth / maxHeight);
        
        // get the number of rows into which to split the sausage
        double rows = Math.max(1, currentAR / desiredAR - 1); 
        
        // last row will not be as full
        int nodesPerRow = longestPath / (int) Math.ceil(rows);

        // we have to take care not to break at points where there is a long edge dummy
        
        int index = nodesPerRow;
        int newIndex = index;
        boolean wannaRevert = true;
        
        while (index < longestPath) {

            Layer l = graph.getLayers().get(index);

            // if the next node should be moved to a new row, check if it is a dummy
            boolean dummyInvolved = false;
            for (LNode n : l.getNodes()) {
                dummyInvolved |= n.getProperty(InternalProperties.NODE_TYPE) != NodeType.NORMAL;
                for (LEdge e : n.getIncomingEdges()) {
                    dummyInvolved |=
                            e.getSource().getNode()
                            .getProperty(InternalProperties.NODE_TYPE) != NodeType.NORMAL;
                }
                for (LEdge e : n.getOutgoingEdges()) {
                    dummyInvolved |=
                            e.getTarget().getNode()
                            .getProperty(InternalProperties.NODE_TYPE) != NodeType.NORMAL;
                }
            }
            
            
            // stall the revert if we we face a dummy node
            if (wannaRevert && !dummyInvolved) {
                newIndex = 0;
                wannaRevert = false;
            }
            
            if (index != newIndex) {
                Layer newLayer = graph.getLayers().get(newIndex);

                for (LNode n : Lists.newArrayList(l.getNodes())) {
                    n.setLayer(newLayer.getNodes().size(), newLayer);

                    if (newIndex == 0) {
                        for (LEdge e : Lists.newArrayList(n.getIncomingEdges())) {
                            e.reverse(graph, true);
                            graph.setProperty(InternalProperties.CYCLIC, true);

                            // insert proper dummy nodes for the newly created long edge
                            insertDummies(graph, e);
                            
                            // handle the newly created inverted ports
                            List<LNode> foo = Lists.newArrayList();
                            createWestPortSideDummies(graph, e.getSource(), e, foo);
                            for (LNode no : foo) {
                                no.setLayer(newLayer.getNodes().size() - 1, newLayer);
                            }
                        }
                    }
                }
            }

            if (newIndex >= nodesPerRow) {
                wannaRevert = true;
            }
            
            newIndex++;
            index++;
        }

        // remove old layers
        ListIterator<Layer> it = graph.getLayers().listIterator();
        while (it.hasNext()) {
            Layer l = it.next();
            if (l.getNodes().isEmpty()) {
                it.remove();
            }
        }
        
    }

    private double determineMaximalHeight(final LGraph graph) {
        double maxH = 0;

        for (Layer l : graph.getLayers()) {
            double lH = 0;
            for (LNode n : l.getNodes()) {
                lH += n.getSize().y + n.getMargin().bottom + n.getMargin().top + inLayerSpacing;
            }
            // layers cannot be empty
            lH -= inLayerSpacing;

            maxH = Math.max(maxH, lH);
        }

        return maxH;
    }

    private double determineMaximalWidth(final LGraph graph) {
        double maxW = 0;

        for (Layer l : graph.getLayers()) {
            for (LNode n : l.getNodes()) {
                double nW = l.getSize().x + n.getMargin().right + n.getMargin().left + spacing;
                maxW = Math.max(maxW, nW);
            }

        }
        return maxW;
    }

    /**
     * Inserts long edge dummies for the passed edge.
     */
    private void insertDummies(final LGraph layeredGraph, final LEdge originalEdge) {
        
        LEdge edge = originalEdge;
        LPort targetPort = edge.getTarget();
        LNode src = edge.getSource().getNode();
        LNode tgt = edge.getTarget().getNode();
        
        int srcIndex = src.getLayer().getIndex();
        int tgtIndex = tgt.getLayer().getIndex();
        
        for (int i = srcIndex; i < tgtIndex; i++) {
            
            // Create dummy node
            LNode dummyNode = new LNode(layeredGraph);
            dummyNode.setProperty(InternalProperties.ORIGIN, edge);
            dummyNode.setProperty(InternalProperties.NODE_TYPE, NodeType.LONG_EDGE);
            dummyNode.setProperty(LayoutOptions.PORT_CONSTRAINTS,
                    PortConstraints.FIXED_POS);
            Layer nextLayer = layeredGraph.getLayers().get(i + 1);
            dummyNode.setLayer(nextLayer);

            // Set thickness of the edge
            float thickness = edge.getProperty(LayoutOptions.THICKNESS);
            if (thickness < 0) {
                thickness = 0;
                edge.setProperty(LayoutOptions.THICKNESS, thickness);
            }
            dummyNode.getSize().y = thickness;
            double portPos = Math.floor(thickness / 2);

            // Create dummy input and output ports
            LPort dummyInput = new LPort(layeredGraph);
            dummyInput.setSide(PortSide.WEST);
            dummyInput.setNode(dummyNode);
            dummyInput.getPosition().y = portPos;

            LPort dummyOutput = new LPort(layeredGraph);
            dummyOutput.setSide(PortSide.EAST);
            dummyOutput.setNode(dummyNode);
            dummyOutput.getPosition().y = portPos;

            edge.setTarget(dummyInput);

            // Create a dummy edge
            LEdge dummyEdge = new LEdge(layeredGraph);
            dummyEdge.copyProperties(edge);
            dummyEdge.setSource(dummyOutput);
            dummyEdge.setTarget(targetPort);

            setDummyProperties(dummyNode, edge, dummyEdge);
            
            edge = dummyEdge;
        }
        
    }
    
    /**
     * Copied from {@link LongEdgeSplitter}.
     * 
     * Sets the source and target properties on the given dummy node.
     * 
     * @param dummy the dummy node.
     * @param inEdge the edge going into the dummy node.
     * @param outEdge the edge going out of the dummy node.
     */
    private void setDummyProperties(final LNode dummy, final LEdge inEdge, final LEdge outEdge) {
        LNode inEdgeSourceNode = inEdge.getSource().getNode();
        
        if (inEdgeSourceNode.getProperty(InternalProperties.NODE_TYPE) == NodeType.LONG_EDGE) {
            // The incoming edge originates from a long edge dummy node, so we can
            // just copy its properties
            dummy.setProperty(InternalProperties.LONG_EDGE_SOURCE,
                    inEdgeSourceNode.getProperty(InternalProperties.LONG_EDGE_SOURCE));
            dummy.setProperty(InternalProperties.LONG_EDGE_TARGET,
                    inEdgeSourceNode.getProperty(InternalProperties.LONG_EDGE_TARGET));
        } else {
            // The source is the input edge's source port, the target is the output
            // edge's target port
            dummy.setProperty(InternalProperties.LONG_EDGE_SOURCE, inEdge.getSource());
            dummy.setProperty(InternalProperties.LONG_EDGE_TARGET, outEdge.getTarget());
        }
    }
    
    /**
     * Copied from {@link InvertedPortProcessor}.
     * 
     * Creates the necessary dummy nodes for an output port on the west side of a node,
     * provided that the edge connects two different nodes.
     * 
     * @param layeredGraph the layered graph
     * @param westwardPort the offending port.
     * @param edge the edge connected to the port.
     * @param layerNodeList list of unassigned nodes belonging to the layer of the node the
     *                      port belongs to. The new dummy node is added to this list and
     *                      must be assigned to the layer later.
     */
    private void createWestPortSideDummies(final LGraph layeredGraph, final LPort westwardPort,
            final LEdge edge, final List<LNode> layerNodeList) {
        
        if (edge.getTarget().getNode() == westwardPort.getNode()) {
            return;
        }
        
        // Dummy node in the same layer
        LNode dummy = new LNode(layeredGraph);
        dummy.setProperty(InternalProperties.ORIGIN, edge);
        dummy.setProperty(InternalProperties.NODE_TYPE,
                NodeType.LONG_EDGE);
        dummy.setProperty(LayoutOptions.PORT_CONSTRAINTS, PortConstraints.FIXED_POS);
        layerNodeList.add(dummy);
        
        LPort dummyInput = new LPort(layeredGraph);
        dummyInput.setNode(dummy);
        dummyInput.setSide(PortSide.WEST);
        
        LPort dummyOutput = new LPort(layeredGraph);
        dummyOutput.setNode(dummy);
        dummyOutput.setSide(PortSide.EAST);
        
        // Reroute the original edge
        LPort originalTarget = edge.getTarget();
        edge.setTarget(dummyInput);
        
        // Connect the dummy with the original port
        LEdge dummyEdge = new LEdge(layeredGraph);
        dummyEdge.copyProperties(edge);
        dummyEdge.setSource(dummyOutput);
        dummyEdge.setTarget(originalTarget);
        
        // Set LONG_EDGE_SOURCE and LONG_EDGE_TARGET properties on the LONG_EDGE dummy
        setLongEdgeSourceAndTarget(dummy, dummyInput, dummyOutput, westwardPort);
    }
    
    /**
     * Copied from {@link InvertedPortProcessor}.
     * 
     * Properly sets the {@link de.cau.cs.kieler.klay.layered.properties.Properties#LONG_EDGE_SOURCE}
     * and {@link de.cau.cs.kieler.klay.layered.properties.Properties#LONG_EDGE_TARGET} properties for
     * the given long edge dummy. This is required for the
     * {@link de.cau.cs.kieler.klay.layered.intermediate.HyperedgeDummyMerger} to work
     * correctly.
     * 
     * @param longEdgeDummy the long edge dummy whose properties to set.
     * @param dummyInputPort the dummy node's input port.
     * @param dummyOutputPort the dummy node's output port.
     * @param oddPort the odd port that prompted the dummy to be created.
     */
    private void setLongEdgeSourceAndTarget(final LNode longEdgeDummy, final LPort dummyInputPort,
            final LPort dummyOutputPort, final LPort oddPort) {
        
        // There's exactly one edge connected to the input and output port
        LPort sourcePort = dummyInputPort.getIncomingEdges().get(0).getSource();
        LNode sourceNode = sourcePort.getNode();
        NodeType sourceNodeType = sourceNode.getProperty(InternalProperties.NODE_TYPE);
        LPort targetPort = dummyOutputPort.getOutgoingEdges().get(0).getTarget();
        LNode targetNode = targetPort.getNode();
        NodeType targetNodeType = targetNode.getProperty(InternalProperties.NODE_TYPE);
        
        // Set the LONG_EDGE_SOURCE property
        if (sourceNodeType == NodeType.LONG_EDGE) {
            // The source is a LONG_EDGE node; use its LONG_EDGE_SOURCE
            longEdgeDummy.setProperty(InternalProperties.LONG_EDGE_SOURCE,
                    sourceNode.getProperty(InternalProperties.LONG_EDGE_SOURCE));
        } else {
            // The target is the original node; use it
            longEdgeDummy.setProperty(InternalProperties.LONG_EDGE_SOURCE, sourcePort);
        }

        // Set the LONG_EDGE_TARGET property
        if (targetNodeType == NodeType.LONG_EDGE) {
            // The target is a LONG_EDGE node; use its LONG_EDGE_TARGET
            longEdgeDummy.setProperty(InternalProperties.LONG_EDGE_TARGET,
                    targetNode.getProperty(InternalProperties.LONG_EDGE_TARGET));
        } else {
            // The target is the original node; use it
            longEdgeDummy.setProperty(InternalProperties.LONG_EDGE_TARGET, targetPort);
        }
    }
    
}
