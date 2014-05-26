/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 *
 * Copyright 2014 by
 * + Christian-Albrechts-University of Kiel
 * + Department of Computer Science
 * + Real-Time and Embedded Systems Group
 *
 * This code is provided under the terms of the Eclipse Public License (EPL).
 * See the file epl-v10.html for the license text.
 */
package de.cau.cs.kieler.klay.layered.p4nodes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
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
import de.cau.cs.kieler.klay.layered.properties.GraphProperties;
import de.cau.cs.kieler.klay.layered.properties.InternalProperties;
import de.cau.cs.kieler.klay.layered.properties.NodeType;
import de.cau.cs.kieler.klay.layered.properties.Properties;

/**
 * 
 * <p>
 * Node placement implementation inspired by
 * </p>
 * <ul>
 * <li>Christoph Buchheim, Michael Jünger and Sebastian Leipert. A Fast Layout Algorithm for
 * k-Level Graphs. In <i>Graph Drawing(GD'01)</i>, LNCS vol. 1984, pp. 86-89, Springer, 2001.</li>
 * </ul>
 * <p>
 * The original algorithm was extended to be able to cope with ports, node sizes, node margins and
 * to handle inner segment crossings. The algorithm places the dummy nodes first and in a second
 * step the regular nodes.
 * </p>
 *  * <dl>
 * <dt>Precondition:</dt>
 * <dd>the graph has a proper layering with optimized nodes ordering; ports are properly arranged</dd>
 * <dt>Postcondition:</dt>
 * <dd>each node is assigned a vertical coordinate such that no two nodes overlap; the size of each
 * layer is set according to the area occupied by contained nodes; the height of the graph is set to
 * the maximal layer height</dd>
 * </dl>
 * 
 * @author kpe
 */
public final class BJLNodePlacer implements ILayoutPhase {
    /** the current Graph. */
    private LGraph layeredGraph;
    /** total number of nodes of the current graph. */
    private int numberOfNodes;
    /** list of all linear segments of the current graph. */
    private List<LinearSegment> linearSegments;
    /** Map from node IDs to siblings, segments, and classes. */
    private LNodeExtensions[] nodeExtensions;
    /** top classes computed by traversing the graph from left to right and each level downwards. */
    private List<List<LinearSegment>> topClasses;
    /** bottom classes computed by traversing the graph left to right and each level upwards. */
    private List<List<LinearSegment>> bottomClasses;
    /** top layout, topmost y-coordinates computed by placeDummy. */
    private double[] nodePositionsTop;
    /** bottom layout, bottommost y-coordinates computed by placeDummy. */
    private double[] nodePositionsBottom;
    /** distance between regular nodes. */
    private float normalSpacing;
    /** distance between dummy nodes. */
    private float smallSpacing;
    /** minimum y-Coordinate of the graph. */
    private double minY;

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
    public void process(final LGraph lGraph, final IKielerProgressMonitor monitor) {
        monitor.begin("Buchheim, Jünger and Leipert Node Placement", 1);

        this.layeredGraph = lGraph;

        normalSpacing = layeredGraph.getProperty(Properties.OBJ_SPACING)
                * layeredGraph.getProperty(Properties.OBJ_SPACING_IN_LAYER_FACTOR);
        smallSpacing = normalSpacing * layeredGraph.getProperty(Properties.EDGE_SPACING_FACTOR);

        //preparation
        initializeIds();
        initializeNodeExtensions();

        //start algorithm
        placeDummy();
        placeRegular();

        //additional post processing to refine the layout
        postProcess();

        //clear all
        layeredGraph = null;
        linearSegments.clear();
        nodeExtensions = null;
        topClasses.clear();
        bottomClasses.clear();
        nodePositionsTop = null;
        nodePositionsBottom = null;

        monitor.done();
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////
    // begin initialization

    /** initialize node id, layer id and number of nodes. */
    private void initializeIds() {
        numberOfNodes = 0;
        int idOfLayer = 0;

        for (Layer layer : layeredGraph) {
            layer.id = idOfLayer;
            idOfLayer++;

            for (LNode node : layer.getNodes()) {
                node.id = numberOfNodes;
                numberOfNodes++;
            }
        }
    }

    /** initializes node extensions, compute all left and right siblings. */
    private void initializeNodeExtensions() {
        // map for all direct siblings
        nodeExtensions = new LNodeExtensions[numberOfNodes];

        for (Layer layer : layeredGraph) {
            // if there is only a single node in the layer
            if (layer.getNodes().size() == 1) {
                // create node extensions and set siblings to null
                LNodeExtensions extendedNode = new LNodeExtensions(null, null, -1, -1);
                nodeExtensions[layer.getNodes().get(0).id] = extendedNode;
            } else {
                LNode topSibling = null;
                LNode currentNode = null;
                LNode bottomSibling = null;
                // mark the first node of a layer
                boolean firstNode = true;
                // the current node is always considered as the bottom sibling
                for (LNode node : layer.getNodes()) {
                    // check if the node is the first node in the layer
                    if (firstNode) {
                        currentNode = node;
                        firstNode = false;
                    } else {
                        bottomSibling = node;
                        // create node extensions and save current node with siblings
                        LNodeExtensions extendedNode =
                                new LNodeExtensions(topSibling, bottomSibling, -1, -1);
                        nodeExtensions[currentNode.id] = extendedNode;
                        topSibling = currentNode;
                        currentNode = bottomSibling;
                    }
                }
                // save node extensions of the last node in the current layer
                LNodeExtensions extendedNode = new LNodeExtensions(topSibling, null, -1, -1);
                nodeExtensions[currentNode.id] = extendedNode;
            }
        }
    }

    // end initialization
    // /////////////////////////////////////////////////////////////////////////////////////////////
    // begin placeDummy
    /**
     * places the dummy nodes as close to each other as possible respecting the minimal distance
     * between direct siblings, the given order of the nodes and the straightness of all inner
     * segments of long edges.
     */
    private void placeDummy() {
        computeLinearSegments();
        computePositionTop();
        computePositionBottom();

        // compute the minimum y-coordinate of the average node placement
        minY = 0;
        for (Layer layer : layeredGraph) {
            for (LNode lNode : layer) {
                if (((nodePositionsTop[lNode.id] + nodePositionsBottom[lNode.id]) / 2) < minY) {
                    minY = (nodePositionsTop[lNode.id] + nodePositionsBottom[lNode.id]) / 2;
                }
            }
        }
        
        //compute the average y-coordinate from both layouts, top and bottom, and adjust it to 0
        for (Layer layer : layeredGraph) {
            for (LNode lNode : layer) {
                lNode.getPosition().y =
                        ((nodePositionsTop[lNode.id] + nodePositionsBottom[lNode.id]) / 2)
                                + Math.abs(minY);
            }
        }
    }

    /**
     * compute the lowermost positions of all nodes of the graph.
     */
    private void computePositionBottom() {
        // initialize the bottom positions
        nodePositionsBottom = new double[numberOfNodes];

        // assign all nodes not placed
        for (int i = 0; i < nodeExtensions.length - 1; i++) {
            nodeExtensions[i].isPlaced = false;
        }

        computeBottomClasses();

        // place all nodes of each class to the position as low as possible
        for (int i = 0; i < bottomClasses.size(); i++) {
            if (!bottomClasses.get(i).isEmpty()) {
                int classId = i;
                for (LinearSegment seg : bottomClasses.get(classId)) {
                    for (LNode node : seg.nodes) {
                        // check if the current node is already placed
                        if (!nodeExtensions[node.id].isPlaced) {
                            placeBottom(node, classId);
                        }
                    }
                }
                // adjust the current class to the bottom classes already placed
                adjustBottomClass(classId);
            }
        }
    }

    /**
     * places all nodes of the current node segment to the lowermost position.
     * 
     * @param node
     *            current node to be placed to the lowermost position
     * @param classId
     *            identifier value of the current class
     */
    private void placeBottom(final LNode lNode, final int classId) {
        // resulting position of all nodes which belong to the current node linear segment
        double p = Double.POSITIVE_INFINITY;

        for (LNode node : linearSegments.get(nodeExtensions[lNode.id].segId).nodes) {
            // get the bottom sibling of the current node
            LNode bottomSibling = nodeExtensions[node.id].bottomSibling;
            // check if the bottom sibling exists and appears in the same class as the current node
            if ((bottomSibling != null) && classId == nodeExtensions[bottomSibling.id].classId) {
                // check if the bottom sibling is already placed
                if (!nodeExtensions[bottomSibling.id].isPlaced) {
                    placeBottom(bottomSibling, classId);
                }
                // compute the minimum distance between the current node and his bottom sibling
                double m = 0;
                if (!isDummy(node)) {
                    m = bottomSibling.getMargin().top + normalSpacing + node.getMargin().bottom
                                    + node.getSize().y;
                } else {
                    m = bottomSibling.getMargin().top + smallSpacing + node.getMargin().bottom
                                    + node.getSize().y;
                }
                p = Math.min(p, (nodePositionsBottom[bottomSibling.id] - m));
            }
        }
        
        // if no bottom sibling of the same class found, set p to 0
        if (p == Double.POSITIVE_INFINITY) {
            p = 0;
        }
        
        // set all nodes of the linear segment to the same position p
        for (LNode node2 : linearSegments.get(nodeExtensions[lNode.id].segId).nodes) {
            nodePositionsBottom[node2.id] = p;
            nodeExtensions[node2.id].isPlaced = true;
        }
    }

    /**
     * align the current class to the previously placed classes.
     * 
     * @param classId
     *            identifier value of the current class
     */
    private void adjustBottomClass(final int classId) {
        // computed shifting value for all nodes of the current class
        double d = Double.POSITIVE_INFINITY;
        // minimum distance between two nodes
        double m;

        for (LinearSegment seg : bottomClasses.get(classId)) {
            for (LNode node : seg.nodes) {
                // get top sibling of the current node
                LNode topSibling = nodeExtensions[node.id].topSibling;
                // check if the top sibling exists and appears in another class than the current
                // node
                if ((topSibling != null) && (nodeExtensions[topSibling.id].classId != classId)) {
                    // minimum distance between the current node and his top sibling
                    m = 0;
                    if (!isDummy(node)) {
                        m = node.getMargin().top + normalSpacing
                                        + topSibling.getMargin().bottom + topSibling.getSize().y;
                    } else {
                        m = node.getMargin().top + smallSpacing + topSibling.getMargin().bottom
                                        + topSibling.getSize().y;
                    }
                    d = Math.min(d, ((nodePositionsBottom[node.id] 
                                        - nodePositionsBottom[topSibling.id]) - m));
                }
            }
        }

        if (d == Double.POSITIVE_INFINITY) {
            // list to save the difference between the y-coordinate of the current node
            // and the connected nodes in the previous layer for computing the median
            ArrayList<Double> distancesToNeighbors = new ArrayList<Double>();

            for (LinearSegment seg : bottomClasses.get(classId)) {
                for (LNode node : seg.nodes) {
                    for (LEdge edge : node.getIncomingEdges()) {
                        LNode neighbor = edge.getSource().getNode();
                        LPort neighborPort = edge.getSource();
                        LPort currentNodePort = edge.getTarget();
                        // check if the connected neighbor belongs to a lower class,
                        // that has already been placed before
                        if (nodeExtensions[neighbor.id].classId < classId) {
                            distancesToNeighbors.add((nodePositionsBottom[node.id] 
                                    - currentNodePort.getPosition().y)
                                        - (nodePositionsBottom[neighbor.id] 
                                             - neighborPort.getPosition().y));
                        }
                    }
                }
            }
            // check if there are no neighbors
            if (distancesToNeighbors.isEmpty()) {
                d = 0;
            } else {
                // compute the median of distances to the neighbors
                Collections.sort(distancesToNeighbors);
                d = distancesToNeighbors.get(distancesToNeighbors.size() / 2);
            }
        }
        
        // shift all nodes of the current class by the value d
        for (LinearSegment seg : bottomClasses.get(classId)) {
            for (LNode node : seg.nodes) {
                nodePositionsBottom[node.id] = nodePositionsBottom[node.id] - d;
            }
        }
    }

    /**
     * places all nodes of the current class to the topmost position.
     */
    private void computePositionTop() {
        // initialize the top layout
        nodePositionsTop = new double[numberOfNodes];
       
          // assign all nodes not placed
          for (int i = 0; i < nodeExtensions.length; i++) {
            nodeExtensions[i].isPlaced = false;
        }

        computeTopClasses();
 
        // place all nodes of each class to the topmost position
        for (int i = 0; i < topClasses.size(); i++) {
            if (!topClasses.get(i).isEmpty()) {
                int classId = i;
                for (LinearSegment seg : topClasses.get(classId)) {
                    for (LNode node : seg.nodes) {
                        // check if the current node is placed
                        if (!nodeExtensions[node.id].isPlaced) {
                            placeTop(node, classId);
                        }
                    }
                }
                // adjust the current class to the top classes already placed
                adjustTopClass(classId);
            }
        }
    }

    /**
     * places all nodes of the current node segment to the topmost position.
     * 
     * @param node
     *            current node to be placed to the topmost position
     * @param classId
     *            identifier value of the current class
     */
    private void placeTop(final LNode lNode, final int classId) {
        // resulting position of all nodes that belong to the current node linear segment
        double p = Double.NEGATIVE_INFINITY;
        // get the current node segment
        LinearSegment seg = linearSegments.get(nodeExtensions[lNode.id].segId);

        for (LNode node : seg.nodes) {
            LNode topSibling = nodeExtensions[node.id].topSibling;
            // check if the top sibling exists and appears in the same class as the current node
            if ((topSibling != null) && classId == nodeExtensions[topSibling.id].classId) {
                // check if the top sibling is already placed
                if (!nodeExtensions[topSibling.id].isPlaced) {
                    placeTop(topSibling, classId);
                }
                // compute the minimum distance between the current node and his top sibling
                double m = 0;
                if (!isDummy(node)) {
                    m = topSibling.getSize().y + topSibling.getMargin().bottom + normalSpacing
                                    + node.getMargin().top;
                } else {
                    m = topSibling.getSize().y + topSibling.getMargin().bottom + smallSpacing
                                    + node.getMargin().top;
                }
                p = Math.max(p, (nodePositionsTop[topSibling.id] + m));
            }
        }
        
        // if no top sibling of the same class found, set p to the initial value 0
        if (p == Double.NEGATIVE_INFINITY) {
            p = 0;
        }
        
        // set all nodes of the current segment to the same y-coordinate
        for (LNode node2 : seg.nodes) {
            nodePositionsTop[node2.id] = p;
            nodeExtensions[node2.id].isPlaced = true;
        }
    }

    /**
     * align the current class to the previously placed class.
     * 
     * @param classID
     *            identifier value of the current class
     */
    private void adjustTopClass(final int classId) {
        double d = Double.POSITIVE_INFINITY;
        double m;

        for (LinearSegment seg : topClasses.get(classId)) {
            for (LNode node : seg.nodes) {
                LNode bottomSibling = nodeExtensions[node.id].bottomSibling;
                // check if the bottom sibling exists
                // and appears in another class than the current node
                if ((bottomSibling != null)
                        && (nodeExtensions[bottomSibling.id].classId != classId)) {
                    // compute the minimum distance between the current node and his bottom sibling
                    m = 0;
                    if (!isDummy(node)) {
                        m = node.getSize().y + node.getMargin().bottom + normalSpacing
                                        + bottomSibling.getMargin().top;
                    } else {
                        m = node.getSize().y + node.getMargin().bottom + smallSpacing
                                        + bottomSibling.getMargin().top;
                    }
                    d = Math.min(d, nodePositionsTop[bottomSibling.id]
                                        - nodePositionsTop[node.id] - m);
                }
            }
        }

        // check if there's no bottom sibling that belongs to another class
        if (d == Double.POSITIVE_INFINITY) {
            // list to save the positions of the connected nodes in the previous layer for computing
            // the median
            ArrayList<Double> distancesToNeighbors = new ArrayList<Double>();

            for (LinearSegment seg : topClasses.get(classId)) {
                for (LNode node : seg.nodes) {
                    for (LEdge edge : node.getIncomingEdges()) {
                        LNode neighbour = edge.getSource().getNode();
                        LPort neighborPort = edge.getSource();
                        LPort currentNodePort = edge.getTarget();
                        // check if the connected neighbor belongs to a lower class,
                        // which are always already placed at this moment
                        if (nodeExtensions[neighbour.id].classId < classId) {
                            distancesToNeighbors.add((nodePositionsTop[neighbour.id]
                                    - neighborPort.getPosition().y)
                                        - (nodePositionsTop[node.id] - currentNodePort.getPosition().y));
                        }
                    }
                }
            }
            // check if there are no neighbors
            if (distancesToNeighbors.isEmpty()) {
                d = 0;
            } else {
                // compute the median of distances to the neighbors
                Collections.sort(distancesToNeighbors);
                d = distancesToNeighbors.get((distancesToNeighbors.size() / 2));
            }
        }
      
        // shift all nodes of the current class by value d
        for (LinearSegment seg : topClasses.get(classId)) {
            for (LNode node2 : seg.nodes) {
                nodePositionsTop[node2.id] = nodePositionsTop[node2.id] + d;
            }
        }
    }

    /**
     * computes the linear segments of the layered graph by getting the current segment id of each
     * node out of the nodeExtensions array.
     * 
     */
    private void computeLinearSegments() {
        // maximum linear segment id
        int maxSegId = computeSegmentIds();
        // initialize list of all linear segments
        linearSegments = new ArrayList<LinearSegment>(maxSegId);
        
        for (int i = 0; i < maxSegId; i++) {
            linearSegments.add(new LinearSegment(i));
        }
      
        for (Layer layer : layeredGraph) {
            for (LNode node : layer) {
                linearSegments.get(nodeExtensions[node.id].segId).addNode(node);
            }
        }
    }

    /**
     * computes the segmentId of each node and save it in the nodeExtensions array.
     * 
     * @return the number of linear segments.
     */
    private int computeSegmentIds() {
        int segId = 0;
        // helper for inner segment crossings,
        // list of dummy nodes of the previous layer that has dummy successors in the current layer
        List<Integer> lastLayerSegments;
        // list of segment identifier of dummy nodes whose successors are also dummy nodes
        List<Integer> currentLayerSegments = new LinkedList<Integer>();

        for (Layer layer : layeredGraph) {
            lastLayerSegments = currentLayerSegments;
            currentLayerSegments = new LinkedList<Integer>();
            for (LNode node : layer) {
                // create a new segment for all regular nodes
                if (!isDummy(node)) {
                    nodeExtensions[node.id].segId = segId;
                    segId++;
                    } else {
                    // predecessor in the previous layer
                    LNode pred = getFirstPredeccessor(node);
                    // successor in the next layer
                    LNode succ = getFirstsuccessor(node);
                    // check if the list of dummy nodes that has successors in the current layer
                    // is empty
                    if (!lastLayerSegments.isEmpty()) {
                        if (isDummy(pred) && (pred.getLayer().id < node.getLayer().id)) {
                            if (nodeExtensions[pred.id].segId == lastLayerSegments.get(0)) {
                                // the current dummy node belongs to a long edge, so it gets the
                                // segment id of his predecessor
                                nodeExtensions[node.id].segId = nodeExtensions[pred.id].segId;
                                lastLayerSegments.remove(0);
                            } else {
                                lastLayerSegments.remove(0);
                                // the current dummy node belongs to a long edge with inner
                                // crossings, so it gets a new segment id
                                nodeExtensions[node.id].segId = segId;
                                segId++;
                            }
                        } else {
                            nodeExtensions[node.id].segId = segId;
                            segId++;
                        }
                    } else {
                        nodeExtensions[node.id].segId = segId;
                        segId++;
                    }
                    if (isDummy(succ) && (succ.getLayer().id > node.getLayer().id)) {
                        currentLayerSegments.add(nodeExtensions[node.id].segId);
                    }
                }
            }
        }
        return segId;
    }

    /**
     * @param node
     *            current node
     * @return the first predecessor of the current node in the previous layer
     */
    private LNode getFirstPredeccessor(final LNode node) {
        LNode predecessor = null;
      
        for (LEdge edge : node.getIncomingEdges()) {
            if (edge.getSource().getNode().getLayer().id < node.getLayer().id) {
                predecessor = edge.getSource().getNode();
            }
        }
        return predecessor;
    }

    /**
     * 
     * @param node
     *            current node
     * @return the first successor of the current node
     */
    private LNode getFirstsuccessor(final LNode node) {
        LNode successor = null;
      
        for (LEdge edge : node.getOutgoingEdges()) {
            if (edge.getTarget().getNode().getLayer().id == (node.getLayer().id + 1)) {
                successor = edge.getTarget().getNode();
            }
        }
        return successor;
    }

    /**
     * @param node
     * @return true if node is a dummy node
     */
    private boolean isDummy(final LNode node) {
        if (node != null) {
            return node.getProperty(InternalProperties.NODE_TYPE) != NodeType.NORMAL;
        } else {
            return false;
        }
    }

    /**
     * divide the graph into classes by traversing each layer downwards.
     */
    private void computeTopClasses() {
        // save the computed topmost classes in the order they are inserted
        topClasses = new ArrayList<List<LinearSegment>>(layeredGraph.getLayers().size());
     
        for (int i = 0; i < layeredGraph.getLayers().size(); i++) {
            topClasses.add(new LinkedList<LinearSegment>());
        }

        for (Layer layer : layeredGraph) {
            int c = layer.id;
            for (LNode lNode : layer) {
                // is the current node already part of a class ?
                if (nodeExtensions[lNode.id].classId == -1) {
                    linearSegments.get(nodeExtensions[lNode.id].segId).computedClassId = c;
                    for (LNode lNode2 : linearSegments.get(nodeExtensions[lNode.id].segId).nodes) {
                        nodeExtensions[lNode2.id].classId = c;
                    }
                    // insert segment into the current class c
                    topClasses.get(c).add(linearSegments.get(nodeExtensions[lNode.id].segId));
                // node already part of a class
                } else {
                    c = nodeExtensions[lNode.id].classId;
                }
            }
        }
    }

    /**
     * divides the graph into classes by traversing each layer upwards.
     */
    private void computeBottomClasses() {
        // save the computed lowermost classes in the order they are inserted
        bottomClasses = new ArrayList<List<LinearSegment>>(layeredGraph.getLayers().size());
     
        for (int i = 0; i < layeredGraph.getLayers().size(); i++) {
            bottomClasses.add(new LinkedList<LinearSegment>());
        }

        // initialize all classesIds
        for (int i = 0; i < nodeExtensions.length; i++) {
            nodeExtensions[i].classId = -1;
        }
      
        for (LinearSegment seg : linearSegments) {
            seg.computedClassId = -1;
        }

        for (Layer layer : layeredGraph) {
            int c = layer.id;

            // for computing the bottommost classes
            // iterate over the nodes in reverse order.
            for (int i = layer.getNodes().size() - 1; i >= 0; i--) {

                // is node already part of a class ?
                if (nodeExtensions[layer.getNodes().get(i).id].classId == -1) {

                    // get segment of the current node
                    int segmentId = nodeExtensions[layer.getNodes().get(i).id].segId;
                    linearSegments.get(segmentId).computedClassId = c;

                    for (LNode lNode2 : linearSegments.get(segmentId).nodes) {
                        nodeExtensions[lNode2.id].classId = c;
                    }

                    // insert segment into the current class c
                    bottomClasses.get(c).add(linearSegments.get(segmentId));

                    // node already part of a class
                } else {
                    c = nodeExtensions[layer.getNodes().get(i).id].classId;
                }
            }
        }
    }

    // end placeDummy
    // ////////////////////////////////////////////////////////////////////////////////////////
    // begin placeRegular
    
    /**
     * Threshold for two doubles to still be considered equal. This could probably be done better with
     * a relative threshold, but it will suffice for the moment.
     */
    private static final double EQUALITY_PRECISION = 2.0;
    
    /**
     * placeRegular minimizes the total length of all outer segments. The positions of dummy nodes
     * are regarded as fixed. Thus the regular sequences can be placed independently, if a dummy
     * node exists between the sequences.
     */
    private void placeRegular() {
        // Preparations/////////////////////
        // dummy that border a regular sequence below
        LNode bottomDummy = null;
        // dummy that border a regular sequence above
        LNode topDummy = null;
        // all dummy nodes of the layered graph
        List<LNode> dummies = new ArrayList<LNode>();
        // sequence of regular nodes
        List<LNode> sequenceOfRegulars = new ArrayList<LNode>();
        // array saves the current direction of each node
        int[] directions = new int[numberOfNodes];
        // array saves for each node if it's already placed
        boolean[] placed = new boolean[numberOfNodes];

        // list of all dummy nodes of the current graph
        for (Layer layer : layeredGraph) {
            for (LNode node : layer) {
                if (isDummy(node)) {
                    dummies.add(node);
                }
                // compute minimum distance between all nodes and their bottom siblings
                LNode sib = nodeExtensions[node.id].bottomSibling;
                if (sib != null) {
                    if (!isDummy(node)) {
                        nodeExtensions[node.id].minDistanceToBottomSibling =
                                node.getSize().y + node.getMargin().bottom + normalSpacing
                                        + sib.getMargin().top;
                    } else {
                        nodeExtensions[node.id].minDistanceToBottomSibling =
                                node.getSize().y + node.getMargin().bottom + smallSpacing
                                        + sib.getMargin().top;
                    }
                }
            }
        }
        // end of preparation/////////////////

        // at first the algorithm finds the regular sequences that can be regarded as placed.
        // It is the case if the distance between the surrounding dummy nodes is already the
        // minimum distance.
        for (int i = 0; i < dummies.size(); i++) {
            sequenceOfRegulars.clear();
            topDummy = dummies.get(i);
            bottomDummy = findNextVirtual(topDummy, sequenceOfRegulars);
            if (bottomDummy != null) {
                directions[topDummy.id] = 0;
                if (Math.abs((bottomDummy.getPosition().y - topDummy.getPosition().y)
                        - minimumDistance(topDummy, bottomDummy)) < EQUALITY_PRECISION) {
                    
                    placed[topDummy.id] = true;
                } else {
                    placed[topDummy.id] = false;
                }
            }
        }
        // traversing the graph from left to right
        for (Layer layer : layeredGraph) {
            sequenceOfRegulars.clear();
            traverseByDirection(sequenceOfRegulars, directions, placed, 1, layer);
            if (layer.id < layeredGraph.getLayers().size() - 1) {
                adjustDirections(layer, 1, directions, placed);
            }
        }
        // traversing the graph in the reversed order from right to left
        for (int j = layeredGraph.getLayers().size() - 1; j >= 0; j--) {
            sequenceOfRegulars.clear();
            traverseByDirection(sequenceOfRegulars, directions, placed, -1, layeredGraph
                    .getLayers().get(j));
            if (layeredGraph.getLayers().get(j).id > 0) {
                adjustDirections(layeredGraph.getLayers().get(j), -1, directions, placed);
            }
        }
    }

    /**
     * places all regular sequences by traversing the graph in direction d.
     * 
     * @param sequence
     *            chain of regular nodes
     * @param directions
     *            array of all traversing directions
     * @param placed
     *            assign nodes placed or not
     * @param d
     *            current direction, 1 defines top down and -1 defines the reversed direction
     * @param layer
     *            current layer
     */
    private void traverseByDirection(final List<LNode> sequence, final int[] directions,
            final boolean[] placed, final int d, final Layer layer) {
        // empty sequence
        sequence.clear();
        // initialize the next bottom dummy node
        LNode bottomDummy = null;
        // initialize top dummy node
        LNode topDummy = null;
 
        // find the topmost dummy node and insert all regular nodes above in the sequence list
        // consider that this sequence is above the top dummy
        for (LNode node : layer) {
            if (!isDummy(node)) {
                sequence.add(node);
            } else {
                topDummy = node;
                break;
            }
        }
        
        // place the above computed sequence
        placeSequence(null, topDummy, d, sequence);

        // save computed minimum distance
        for (LNode node : sequence) {
            LNode bottomSibling = nodeExtensions[node.id].bottomSibling;
            if (bottomSibling != null) {
                nodeExtensions[node.id].minDistanceToBottomSibling =
                        Math.max((bottomSibling.getPosition().y - node.getPosition().y),
                                (bottomSibling.getMargin().top + normalSpacing
                                        + node.getMargin().bottom + node.getSize().y));
            }
        }
        
        // if a top dummy exists, save the minimum distance between this dummy and the 
        // last regular node of the sequence above
        if (topDummy != null && !sequence.isEmpty()) {
            nodeExtensions[(sequence.get(sequence.size() - 1).id)].minDistanceToBottomSibling =
                    Math.max(
                            (topDummy.getPosition().y - sequence.get(sequence.size() - 1)
                                    .getPosition().y),
                            (topDummy.getMargin().top + normalSpacing
                                    + sequence.get(sequence.size() - 1).getMargin().bottom + sequence
                                    .get(sequence.size() - 1).getSize().y));
        }
        
        while (topDummy != null) {
            // find the next dummy node in the current layer
            // and save regular nodes in sequence
            sequence.clear();
            bottomDummy = findNextVirtual(topDummy, sequence);
            if (bottomDummy == null) {
                placeSequence(topDummy, null, d, sequence);
                // set the distances computed by placeSequence as the new minimum distances from
                // each node of the sequence to his bottom sibling
                for (LNode node : sequence) {
                    LNode bottomSibling = nodeExtensions[node.id].bottomSibling;
                    if (bottomSibling != null) {
                        nodeExtensions[node.id].minDistanceToBottomSibling =
                                Math.max(
                                        (bottomSibling.getPosition().y - node.getPosition().y),
                                        (bottomSibling.getMargin().top + normalSpacing
                                                + node.getMargin().bottom + node.getSize().y));
                    }
                }
                if (!sequence.isEmpty()) {
                    nodeExtensions[topDummy.id].minDistanceToBottomSibling =
                            Math.max(
                                    (sequence.get(0).getPosition().y - topDummy.getPosition().y),
                                    (sequence.get(0).getMargin().top + normalSpacing
                                            + topDummy.getMargin().bottom + topDummy.getSize().y));
                }
            } else if ((bottomDummy != null) && (directions[topDummy.id] == d)) {
                placeSequence(topDummy, bottomDummy, d, sequence);
                placed[topDummy.id] = true;
            }
            topDummy = bottomDummy;
        }
    }

    /**
     * computes the values of the array directions for the next layer. If it sets
     * directions[topDummy] to d, this forces the method placeRegular to place the regular sequence
     * while processing the next layer.
     * 
     * @param layer
     *            current layer
     * @param d
     *            current direction, 1 defines top down and -1 defines the reversed direction
     * @param directions
     *            array of all traversing directions
     * @param placed
     *            assign nodes placed or not
     */
    private void adjustDirections(final Layer layer, final int d, final int[] directions,
            final boolean[] placed) {
        // dummy in the next layer refers to direction d
        LNode vTop = null;
        // dummy in the current layer
        LNode wTop = null;
        // dummy in the next layer refers to direction d
        LNode vBottom = null;
        // dummy in the current layer
        LNode wBottom = null;
        // p saves if a node is already placed or not
        boolean p = false;
        // get the next layer referring to direction d
        Layer nextLayer = layeredGraph.getLayers().get(layer.id + d);

        // find all dummy nodes in the next layer l+d
        for (LNode node : nextLayer) {
            if (isDummy(node)) {
                vBottom = node;
                // find all dummy neighbors in layer l of dummy nodes v in layer l+d
                for (LEdge edge : vBottom.getIncomingEdges()) {
                    if ((edge.getSource().getNode().getLayer().id == layer.id)
                            && (isDummy(edge.getSource().getNode()))) {
                        wBottom = edge.getSource().getNode();
                          if (vTop != null) {
                            p = placed[wTop.id];
                            // sequence between wTop and wBottom
                            for (int i = wTop.id; i < wBottom.id - 1; i++) {
                                if (nodeExtensions[i].bottomSibling != null) {
                                    LNode w = nodeExtensions[i].bottomSibling;
                                    p = (p && placed[w.id]);
                                }
                            }
                            if (p) {
                                directions[vTop.id] = d;
                                // sequence between vTop and vBottom
                                for (int i = vTop.id; i < vBottom.id - 1; i++) {
                                    if (nodeExtensions[i].bottomSibling != null) {
                                        LNode v = nodeExtensions[i].bottomSibling;
                                        directions[v.id] = d;
                                    }
                                }
                            }
                        }
                        // save the new border dummies for the next sequence
                        vTop = vBottom;
                        wTop = wBottom;
                    }
                }
            }
        }
    }

    /**
     * places the sequence of regular nodes using a divide & conquer method.
     * 
     * @param bMinus
     *            next dummy node above the regular sequence
     * @param bPlus
     *            next dummy node below the regular sequence
     * @param d
     *            current direction, 1 defines top down and -1 defines the reversed direction
     * @param sequence
     *            consecutive chain of regular nodes
     */
    private void placeSequence(final LNode bMinus, final LNode bPlus, final int d,
            final List<LNode> sequence) {
        if (sequence.size() == 1) {
            placeSingle(bMinus, bPlus, d, sequence.get(0));
        } else if (sequence.size() > 1) {
            int t = sequence.size() / 2;
            // place the sublists
            placeSequence(bMinus, bPlus, d, sequence.subList(0, t));
            placeSequence(bMinus, bPlus, d, sequence.subList(t, sequence.size()));
            combineSequences(bMinus, bPlus, d, sequence);
        }
    }

    /**
     * places a single regular node.
     * 
     * @param topDummy
     *            next dummy node above the regular node
     * @param bottomDummy
     *            next dummy node below the regular node
     * @param node
     *            current regular node
     */
    private void placeSingle(final LNode topDummy, final LNode bottomDummy, final int d,
            final LNode node) {
        // saves all neighbors in the previous layer
        List<LNode> neighbors = new ArrayList<LNode>();
        LNode neighbor = null;

        for (LEdge edge : node.getIncomingEdges()) {
            neighbor = edge.getSource().getNode();
            if (neighbor.getLayer().id == node.getLayer().id - d) {
                neighbors.add(neighbor);
            }
        }
        
        // compute the median of all neighbors in the next layer and set the current node to this
        // position
        if (!neighbors.isEmpty()) {
            // sorted list of neighbors
            Collections.sort(neighbors, new NeighborComparator());
            // median neighbor node
            LNode neighbor2 = neighbors.get(neighbors.size() / 2);
            double neighbor2Position = 0;

            for (LPort port : neighbor2.getPorts()) {
                for (LPort port2 : port.getConnectedPorts()) {
                    if (port2.getNode().equals(node)) {
                        neighbor2Position = neighbor2.getPosition().y - port.getPosition().y
                                                - port.getAnchor().y;
                        // set the position of the current node to the median
                        node.getPosition().y = neighbor2Position + port2.getPosition().y 
                                                + port2.getAnchor().y;
                    }
                }
            }
            // attend to the minimum distances to the border dummies
            if (topDummy != null) {
                node.getPosition().y = Math.max(node.getPosition().y,
                                topDummy.getPosition().y + minimumDistance(topDummy, node));
            }
            if (bottomDummy != null) {
                node.getPosition().y = Math.min(node.getPosition().y, 
                                bottomDummy.getPosition().y - minimumDistance(node, bottomDummy));
            }
        }
    }

    /**
     * combines the regular sequences and align their position by attending the minimum distances.
     * 
     * @param topDummy
     *            dummy node above the regular sequence
     * @param bottomDummy
     *            dummy node below the regular sequence
     * @param d
     *            current direction, 1 defines top down and -1 defines the reversed direction
     * @param sequence
     *            consecutive chain of regular nodes
     */
    private void combineSequences(final LNode topDummy, final LNode bottomDummy, final int d,
            final List<LNode> sequence) {
        List<Resistance> minusResistance = new LinkedList<Resistance>();
        List<Resistance> plusResistance = new LinkedList<Resistance>();
        // compute the middle of the sequence
        int t = sequence.size() / 2;
        // divide the sequence
        List<LNode> s1 = sequence.subList(0, t);
        List<LNode> s2 = sequence.subList(t, sequence.size());
        // get the middle elements of the sequence
        LNode vt1 = s1.get(s1.size() - 1);
        LNode vt2 = s2.get(0);
        // initialize resistance value for vt1 moving upwards
        int rMinus = 0;
        // initialize resistance value for vt2 moving downwards
        int rPlus = 0;

        collectTopChanges(topDummy, bottomDummy, minusResistance, s1, d);
        // sorted list of Resistances
        Collections.sort(minusResistance);

        collectBottomChanges(topDummy, bottomDummy, plusResistance, s2, d);
        // sorted list of Resistances
        Collections.sort(plusResistance);
     
        // if the distance between vt1 and vt2 is too small, they have to be moved
        while ((vt2.getPosition().y - vt1.getPosition().y) < minimumDistance(vt1, vt2)) {
            // check by the resistance values if better moving vt1 or vt2
            if (rMinus < rPlus) {
                if (minusResistance.isEmpty()) {
                    vt1.getPosition().y = vt2.getPosition().y - minimumDistance(vt1, vt2);
                } else {
                    Resistance resitance = minusResistance.get(0);
                    rMinus = rMinus + resitance.c;
                    vt1.getPosition().y = resitance.position;
                    vt1.getPosition().y =
                            Math.max(vt1.getPosition().y,
                                    vt2.getPosition().y - minimumDistance(vt1, vt2));
                    minusResistance.remove(0);
                }
            } else {
                if (plusResistance.isEmpty()) {
                    vt2.getPosition().y = vt1.getPosition().y + minimumDistance(vt1, vt2);
                } else {
                    Resistance resistance = plusResistance.get(plusResistance.size() - 1);
                    rPlus = rPlus + resistance.c;
                    vt2.getPosition().y = resistance.position;
                    vt2.getPosition().y =
                            Math.min(vt2.getPosition().y,
                                    vt1.getPosition().y + minimumDistance(vt1, vt2));
                    plusResistance.remove(plusResistance.size() - 1);
                }
            }
        }
      
        // now position of vt1 is fix, so adjust the other nodes of the sequence
        for (int i = t - 1; i > 0; i--) {
            LNode vi = s1.get(i);
            vi.getPosition().y =
                    Math.min(vi.getPosition().y, (vt1.getPosition().y - minimumDistance(vi, vt1)));
        }
        // now the position of vt1 is fix, so adjust the other nodes of the sequence
        for (int i = 1; i < s2.size(); i++) {
            LNode vi = s2.get(i);
            vi.getPosition().y =
                    Math.max(vi.getPosition().y, (vt2.getPosition().y + minimumDistance(vt2, vi)));
        }
    }

    /**
     * computes the results of all rPlus. rPlus is the number of all segments getting longer by
     * increasing the position of the current node minus the number of all segments getting shorter.
     * 
     * @param topDummy
     *            dummy node above the regular sequence
     * @param bottomDummy
     *            dummy node below the regular sequence
     * @param plusResistance
     *            saves the potential positions of all nodes of s2 and the respective resistances.
     * @param d
     *            current direction, 1 defines top down and -1 defines the reversed direction
     * @param s2
     *            the bottom part of the sequence of regular nodes
     */
    private void collectBottomChanges(final LNode topDummy, final LNode bottomDummy,
            final List<Resistance> plusResistance, final List<LNode> s2, final int d) {
        // first element of sequence s2
        LNode vt2 = s2.get(0);

        for (LNode node : s2) {
            // saves the value of resistance
            int c = 0;
            List<LNode> neighbours = new ArrayList<LNode>();
            LNode neighbour = null;
            for (LEdge edge : node.getIncomingEdges()) {
                neighbour = edge.getSource().getNode();
                if (d == 1) {
                    if ((neighbour.getLayer().id == node.getLayer().id - 1)
                            && !neighbour.equals(node)) {
                        neighbours.add(neighbour);
                    }
                } else if (d == -1) {
                    if ((neighbour.getLayer().id == node.getLayer().id + 1)
                            && !neighbour.equals(node)) {
                        neighbours.add(neighbour);
                    }
                }
            }
            for (LNode nNode : neighbours) {
                if (nNode.getPosition().y <= node.getPosition().y) {
                    c++;
                } else {
                    c--;
                    plusResistance.add(new Resistance(2, nNode.getPosition().y
                            - minimumDistance(vt2, node)));
                }
            }
            plusResistance
                    .add(new Resistance(c, node.getPosition().y - minimumDistance(vt2, node)));
        }
      
        // check if a bottom dummy exists and ensure that this node position is fix by setting the
        // resistance to infinity
        if (bottomDummy != null) {
            plusResistance.add(new Resistance(Integer.MAX_VALUE, bottomDummy.getPosition().y
                    - minimumDistance(vt2, bottomDummy)));
        }
    }

    /**
     * computes the results of all rMinus. rMinus is the number of all segments getting longer by
     * decreasing the position of the current node minus the number of all segments getting shorter.
     * 
     * @param topDummy
     *            dummy node above the regular sequence
     * @param bottomDummy
     *            dummy node below the regular sequence
     * @param minusResistance
     *            saves the potential positions of all nodes of s1 and the respective resistances.
     * @param d
     *            current direction
     * @param s1
     *            the top part of the sequence of regular nodes
     */
    private void collectTopChanges(final LNode topDummy, final LNode bottomDummy,
            final List<Resistance> minusResistance, final List<LNode> s1, final int d) {
        // last element of sequence s1
        LNode vt1 = s1.get(s1.size() - 1);

        for (LNode node : s1) {
            // saves the value of resistance
            int c = 0;
            List<LNode> neighbors = new ArrayList<LNode>();
            LNode neighbour = null;
            for (LEdge edge : node.getConnectedEdges()) {
                neighbour = edge.getSource().getNode();
                if (d == 1) {
                    if ((neighbour.getLayer().id == node.getLayer().id - 1)
                            && !neighbour.equals(node)) {
                        neighbors.add(neighbour);
                    }
                } else if (d == -1) {
                    if ((neighbour.getLayer().id == node.getLayer().id + 1)
                            && !neighbour.equals(node)) {
                        neighbors.add(neighbour);
                    }
                }
            }
            for (LNode nNode : neighbors) {
                if (nNode.getPosition().y >= node.getPosition().y) {
                    c++;
                } else {
                    c--;
                    minusResistance.add(new Resistance(2, nNode.getPosition().y
                            + minimumDistance(node, vt1)));
                }
            }
            minusResistance.add(new Resistance(c, node.getPosition().y + minimumDistance(node, vt1)));
        }
     
        // check if a top dummy exists and ensure that this node position is fix by setting the
        // resistance to infinity
        if (topDummy != null) {
            minusResistance.add(new Resistance(Integer.MAX_VALUE, topDummy.getPosition().y
                    + minimumDistance(topDummy, vt1)));
        }
    }

    // //////////////////////////////////////////////////////////////////////////////////////////
    // helper placeRegular
    /**
     * finds the next dummy node and saves the above consecutive chain of regular nodes.
     * 
     * @param node
     *            node to find the following dummy 
     * @param sequence
     *            consecutive chain of regular nodes
     * @return the next dummy node
     */
    private LNode findNextVirtual(final LNode node, final List<LNode> sequence) {
        sequence.clear();
        LNode nextNode = nodeExtensions[node.id].bottomSibling;
       
        while ((nextNode != null) && (!isDummy(nextNode))) {
            // sequence of regular nodes
            sequence.add(nextNode);
            nextNode = nodeExtensions[nextNode.id].bottomSibling;
        }
        return nextNode;
    }

    /**
     * computes the minimum distance between two nodes.
     * 
     * @param node1
     * @param node2
     * @return the minimum distance between node1 and node2
     */
    private double minimumDistance(final LNode node1, final LNode node2) {
        assert node1.getLayer().id == node2.getLayer().id;
        double m = 0;
        if (node1 != null && node2 != null) {
            float spacing;
            if (node1.id < node2.id) {
                LNode node = node1;
                for (int i = node1.id; i < node2.id; i++) {
                    if (isDummy(node) && isDummy(nodeExtensions[i].bottomSibling)) {
                        spacing = smallSpacing;
                    } else {
                        spacing = normalSpacing;
                    }
                    m += Math.max((nodeExtensions[i].minDistanceToBottomSibling),
                                    (node.getSize().y + node.getMargin().bottom 
                                          + spacing + nodeExtensions[i].bottomSibling.getMargin().top));
                    node = nodeExtensions[i].bottomSibling;
                }
            } else {
                LNode node = node2;
                for (int i = node2.id; i < node1.id; i++) {
                    if (isDummy(node) && isDummy(nodeExtensions[i].bottomSibling)) {
                        spacing = smallSpacing;
                    } else {
                        spacing = normalSpacing;
                    }
                    m += Math.max((nodeExtensions[i].minDistanceToBottomSibling),
                                    (node.getSize().y + node.getMargin().bottom 
                                          + spacing + nodeExtensions[i].bottomSibling.getMargin().top));
                    node = nodeExtensions[i].bottomSibling;
                }
            }
        }
        return m;
    }

    // end placeRegular
    // //////////////////////////////////////////////////////////////////////////////////////////////
    // helper classes
    /**
     * LNodeExtensions contains additional informations about a node that are used in several methods.
     */
    private static final class LNodeExtensions {
        /** previous node in the same layer. */
        private LNode topSibling;
        /** successive node in the same layer. */
        private LNode bottomSibling;
        /** identifier value of the current node linear segment. */
        private int segId;
        /** identifier value of the current node class. */
        private int classId;
        /** flag value to assign the node placed or not. */
        private boolean isPlaced;
        /** computed minimum distance to the next bottom node in the same layer. */
        private double minDistanceToBottomSibling;
       
        /**
         * @param topSibling
         *            previous node in the same layer.
         * @param bottomSibling
         *            successive node in the same layer
         * @param segId
         *            identifier value of the current node linear segment.
         * @param classId
         *            identifier value of the current node class.
         */
        public LNodeExtensions(final LNode topSibling, final LNode bottomSibling, final int segId,
                final int classId) {
            super();
            this.topSibling = topSibling;
            this.bottomSibling = bottomSibling;
            this.segId = segId;
            this.classId = classId;
            this.isPlaced = false;
            this.minDistanceToBottomSibling = 0;
        }
    }

    /**
     * a linear segment contains a single regular node or all dummy nodes of a long edge.
     */
    private final class LinearSegment {
        /** identifier value, used as index in the segments array. */
        private int id;
        /** identifier value of the computed classes. */
        @SuppressWarnings("unused")
        private int computedClassId;
        /** nodes of the linear segment. */
        private List<LNode> nodes;
   
        /**
         * @param segId
         *            identifier value, used as index in the segments array.
         * @param layerId
         *            identifier value of the layer of the first node in a linear segment.
         */
        private LinearSegment(final int segId) {
            this.id = segId;
            this.computedClassId = -1;
            this.nodes = new LinkedList<LNode>();
        }
      
        /**
         * add a node to the end of the node list of this segment and set the segment id in the node
         * extension.
         * 
         * @param node
         *            node to add
         */
        public void addNode(final LNode node) {
            this.nodes.add(node);
            nodeExtensions[node.id].segId = this.id;
        }
    }

    /**
     * resistance contains a node position and the corresponding resistance.
     * 
     */
    private static final class Resistance implements Comparable<Resistance> {
        /** saves the value of resistance. */
        private int c;
       /** saves the node position to the corresponding resistance c. */
        private double position;
        Resistance(final int c, final double position) {
            this.c = c;
            this.position = position;
        }

        /**
         * {@inheritDoc}
         */
        public int compareTo(final Resistance o) {
            return (int) (this.position - o.position);
        }
    }

    /**
     * comparator that determines the order of nodes in a layer.
     */
    private static class NeighborComparator implements Comparator<LNode>, Serializable {

        private static final long serialVersionUID = 8508550732869672955L;

        /**
         * {@inheritDoc}
         */
        public int compare(final LNode o1, final LNode o2) {
            int result = 0;
            if (o1.getIndex() < o2.getIndex()) {
                result = -1;
            } else if (o1.getIndex() > o2.getIndex()) {
                result = 1;
            }
            return result;
        }
    }

    /**
     * post processing method to adjust the linear segments to minimize edge bends, inspired by the
     * linear segment node placer.
     */
    private void postProcess() {
        // traverse all linear segments
        for (LinearSegment segment : linearSegments) {
            double minSpaceAbove = Integer.MAX_VALUE, minSpaceBelow = Integer.MAX_VALUE;
            for (LNode node : segment.nodes) {
                double spaceAbove, spaceBelow;
                 // compute the minimum space between the linear segment and all direct siblings above
                LNode topSibling = nodeExtensions[node.id].topSibling;
                if (topSibling != null) {
                    float spacing =
                            !isDummy(node) && !isDummy(topSibling) ? normalSpacing : smallSpacing;
                    spaceAbove = node.getPosition().y - node.getMargin().top
                                    - (topSibling.getPosition().y + topSibling.getSize().y
                                            + topSibling.getMargin().bottom + spacing);
                } else {
                    spaceAbove = node.getPosition().y - node.getMargin().top;
                }
                minSpaceAbove = Math.min(spaceAbove, minSpaceAbove);
                // compute the minimum space between the linear segment and all direct siblings below
                LNode bottomSibling = nodeExtensions[node.id].bottomSibling;
                if (bottomSibling != null) {
                    float spacing =
                            !isDummy(node) && !isDummy(bottomSibling) ? normalSpacing
                                    : smallSpacing;
                    spaceBelow =
                            bottomSibling.getPosition().y
                                    - bottomSibling.getMargin().top
                                    - (node.getPosition().y + node.getSize().y
                                            + node.getMargin().bottom + spacing);
                } else {
                    spaceBelow = node.getPosition().y;
                }
                minSpaceBelow = Math.min(spaceBelow, minSpaceBelow);
            }
            double minDisplaceSegment = Integer.MAX_VALUE;
            boolean foundPosition = false;
            // determine the minimal displacement that would make one incoming edge straight
            LNode firstNode = segment.nodes.get(0);
            for (LPort target : firstNode.getPorts()) {
                double pos =
                        firstNode.getPosition().y + target.getPosition().y + target.getAnchor().y;
                for (LEdge edge : target.getIncomingEdges()) {
                    LPort source = edge.getSource();
                    double d =
                            source.getNode().getPosition().y + source.getPosition().y
                                    + source.getAnchor().y - pos;
                    if (Math.abs(d) < Math.abs(minDisplaceSegment)
                            && Math.abs(d) < (d < 0 ? minSpaceAbove : minSpaceBelow)) {
                        minDisplaceSegment = d;
                        foundPosition = true;
                    }
                }
            }
            // determine the minimal displacement that would make one outgoing edge straight
            LNode lastNode = segment.nodes.get(segment.nodes.size() - 1);
            for (LPort source : lastNode.getPorts()) {
                double pos =
                        lastNode.getPosition().y + source.getPosition().y + source.getAnchor().y;
                for (LEdge edge : source.getOutgoingEdges()) {
                    LPort target = edge.getTarget();
                    double d =
                            target.getNode().getPosition().y + target.getPosition().y
                                    + target.getAnchor().y - pos;
                    if (Math.abs(d) < Math.abs(minDisplaceSegment)
                            && Math.abs(d) < (d < 0 ? minSpaceAbove : minSpaceBelow)) {
                        minDisplaceSegment = d;
                        foundPosition = true;
                    }
                }
            }
            // if such a displacement could be found, apply it to the whole linear segment
            if (foundPosition && minDisplaceSegment != 0) {
                for (LNode node : segment.nodes) {
                    node.getPosition().y += minDisplaceSegment;
                }
            }
        }
    }
}