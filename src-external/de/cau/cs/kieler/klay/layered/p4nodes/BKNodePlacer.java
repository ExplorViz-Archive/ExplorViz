/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 * 
 * Copyright 2012 by
 * + Christian-Albrechts-University of Kiel
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 * 
 * This code is provided under the terms of the Eclipse Public License (EPL).
 * See the file epl-v10.html for the license text.
 */
package de.cau.cs.kieler.klay.layered.p4nodes;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import de.cau.cs.kieler.core.alg.IKielerProgressMonitor;
import de.cau.cs.kieler.klay.layered.ILayoutPhase;
import de.cau.cs.kieler.klay.layered.IntermediateProcessingConfiguration;
import de.cau.cs.kieler.klay.layered.graph.LEdge;
import de.cau.cs.kieler.klay.layered.graph.LNode;
import de.cau.cs.kieler.klay.layered.graph.Layer;
import de.cau.cs.kieler.klay.layered.graph.LGraph;
import de.cau.cs.kieler.klay.layered.intermediate.IntermediateProcessorStrategy;
import de.cau.cs.kieler.klay.layered.properties.FixedAlignment;
import de.cau.cs.kieler.klay.layered.properties.GraphProperties;
import de.cau.cs.kieler.klay.layered.properties.InternalProperties;
import de.cau.cs.kieler.klay.layered.properties.NodeType;
import de.cau.cs.kieler.klay.layered.properties.Properties;

/**
 * <p>This algorithm is an implementation for solving the node placement problem
 * which is posed in phase 4 of the KLay Layered algorithm. Inspired by</p>
 * <ul>
 *   <li> Ulrik Brandes and Boris K&ouml;pf, Fast and simple horizontal coordinate assignment.
 *     In <i>Proceedings of the 9th International Symposium on Graph Drawing (GD'01)</i>,
 *     LNCS vol. 2265, pp. 33-36, Springer, 2002. </li>
 * </ul>
 * 
 * <p>The original algorithm was extended to be able to cope with ports, node sizes, and
 * node margins, and was made more stable in general.
 * The algorithm is structured in five steps, which include two new steps which were
 * not included in the original algorithm by Brandes and Koepf. The middle three steps
 * are executed four times, traversing the graph in all combinations of TOP or BOTTOM
 * and LEFT or RIGHT.</p>
 * 
 * <p>Although we have, in KLay Layered, the general idea of layouting from left to right
 * and transforming in the desired direction later, we decided to keep the terminology
 * from the original algorithm which thinks of a layout from top to bottom. When placing
 * coordinates, we have to differ from the original algorithm, since node placement in
 * KLay Layered has to assign y-coordinates and not x-coordinates.</p>
 * 
 * <h4>The algorithm:</h4>
 * 
 * <p>The first step checks the graphs' edges and marks short edges which cross long edges
 * (called type 1 conflict). The algorithm indents to draw long edges as straight
 * as possible, thus trying to solve the marked conflicts in a way which keep the
 * long edge straight.</p>
 * 
 * <p>============ TOP, BOTTOM x LEFT, RIGHT ============</p>
 * 
 * <p>The second step traverses the graph in the given directions and tries to group
 * connected nodes into (horizontal) blocks. These blocks, respectively the contained
 * nodes, will be drawn straight when the algorithm is finished. Here, type 1 conflicts
 * are resolved, so that the dummy nodes of a long edge share the same block if possible,
 * such that the long edge is drawn straightly.</p>
 * 
 * <p>The third step contains the addition of node size and port positions to the original
 * algorithm. Each block is investigated from TOP to BOTTOM. Nodes are moved inside the
 * blocks, such that the port of the edge going to the next node is on the same level as
 * that next node. Furthermore, the size of the block is calculated, regarding node sizes
 * and new space needed due to node movement.</p>
 * 
 * <p>In the fourth step, actual y-coordinates are determined. The blocks are placed, start
 * block and direction determined by the directions of the current iteration. 
 * It is tried to place the blocks as compact as possible by grouping blocks.</p>
 *  
 * <p>======================= END =======================</p>
 * 
 * <p>The action of the last step depends on a layout option. If "Less Edge Bends" is set to
 * true, one of the four calculated layouts is selected and applied, choosing the layout 
 * which uses the least space. If it is false, a balanced layout is chosen by calculating
 * a median layout of all four layouts.</p>
 * 
 * <p>In rare cases, it is possible that one or more layouts is not correct, e.g. having nodes
 * which overlap each other or violating the layer ordering constraint. If the algorithm
 * detects that, the respective layout is discarded and another one is chosen.</p>
 * 
 * <dl>
 * <dt>Precondition:</dt>
 * <dd>The graph has a proper layering with optimized nodes ordering; Ports are properly arranged</dd>
 * <dt>Postcondition:</dt>
 * <dd>Each node is assigned a vertical coordinate such that no two nodes overlap; The size of each
 * layer is set according to the area occupied by contained nodes; The height of the graph is set to
 * the maximal layer height</dd>
 * </dl>
 * 
 * @author jjc
 * @kieler.design 2012-08-10 chsch grh
 * @kieler.rating yellow 2012-08-10 chsch grh KI-19
 */
public final class BKNodePlacer implements ILayoutPhase {
    
    /** In the compaction step, nodes connected with north south dummies
     *  are compacted in a way which doesn't leave enough space for e.g., arrowheads.
     *  Thus, a small offset is added to give north south dummies enough space. */
    private static final double NORTH_SOUTH_SPACING = 10.0;

    /** Additional processor dependencies for graphs with hierarchical ports. */
    private static final IntermediateProcessingConfiguration HIERARCHY_PROCESSING_ADDITIONS =
        IntermediateProcessingConfiguration.createEmpty()
            .addBeforePhase5(IntermediateProcessorStrategy.HIERARCHICAL_PORT_POSITION_PROCESSOR);

    /** List of edges involved in type 1 conflicts (see above). */
    private final List<LEdge> markedEdges = new LinkedList<LEdge>();

    /** Basic spacing between nodes, determined by layout options. */
    private float normalSpacing;

    /** Spacing between dummy nodes, determined by layout options. */
    private float smallSpacing;

    /** Flag which switches debug output of the algorithm on or off. */
    private boolean debug = false;

    /** Whether to produce a balanced layout or not. */
    private boolean produceBalancedLayout = false;
    

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
    public void process(final LGraph layeredGraph, final IKielerProgressMonitor monitor) {
        monitor.begin("Brandes & Koepf node placement", 1);

        // Determine overall node count for an optimal initialization of maps.
        int nodeCount = 0;
        for (Layer layer : layeredGraph) {
            nodeCount += layer.getNodes().size();
        }

        // Initialize four layouts which result from the two possible directions respectively.
        BKAlignedLayout lefttop = new BKAlignedLayout(
                nodeCount, VDirection.LEFT, HDirection.TOP);
        BKAlignedLayout righttop = new BKAlignedLayout(
                nodeCount, VDirection.RIGHT, HDirection.TOP);
        BKAlignedLayout leftbottom = new BKAlignedLayout(
                nodeCount, VDirection.LEFT, HDirection.BOTTOM);
        BKAlignedLayout rightbottom = new BKAlignedLayout(
                nodeCount, VDirection.RIGHT, HDirection.BOTTOM);

        // Initialize spacing value from layout options.
        normalSpacing = layeredGraph.getProperty(Properties.OBJ_SPACING) 
                * layeredGraph.getProperty(Properties.OBJ_SPACING_IN_LAYER_FACTOR);
        smallSpacing = normalSpacing * layeredGraph.getProperty(Properties.EDGE_SPACING_FACTOR);

        // Regard possible other layout options.
        debug = layeredGraph.getProperty(Properties.DEBUG_MODE);
        produceBalancedLayout =
                layeredGraph.getProperty(Properties.FIXED_ALIGNMENT) == FixedAlignment.BALANCED;

        // Phase which marks type 1 conflicts, no difference between the directions so only
        // one run is required.
        markConflicts(layeredGraph);

        // Phase which determines the nodes' memberships in blocks. This happens in four different
        // ways, either from processing the nodes from the first layer to the last or vice versa.
        verticalAlignment(layeredGraph, lefttop);
        verticalAlignment(layeredGraph, righttop);
        verticalAlignment(layeredGraph, leftbottom);
        verticalAlignment(layeredGraph, rightbottom);

        // Additional phase which is not included in the original Brandes-Koepf Algorithm.
        // It makes sure that the connected ports within a block are aligned to avoid unnecessary
        // bend points.
        // Also, the required size of each block is determined.
        insideBlockShift(layeredGraph, lefttop);
        insideBlockShift(layeredGraph, righttop);
        insideBlockShift(layeredGraph, leftbottom);
        insideBlockShift(layeredGraph, rightbottom);

        // This phase determines the y coordinates of the blocks and thus the vertical coordinates
        // of all nodes.
        horizontalCompaction(layeredGraph, lefttop);
        horizontalCompaction(layeredGraph, righttop);
        horizontalCompaction(layeredGraph, leftbottom);
        horizontalCompaction(layeredGraph, rightbottom);

        // Debug output
        if (debug) {
            System.out.println("lefttop size is " + lefttop.layoutSize());
            System.out.println("righttop size is " + righttop.layoutSize());
            System.out.println("leftbottom size is " + leftbottom.layoutSize());
            System.out.println("rightbottom size is " + rightbottom.layoutSize());
        }

        // Choose a layout from the four calculated layouts. Layouts that contain errors are skipped.
        // The layout with the smallest size is selected. If more than one smallest layout exists,
        // the first one of the competing layouts is selected.
        BKAlignedLayout chosenLayout = null;
        LinkedList<BKAlignedLayout> layouts = new LinkedList<BKAlignedLayout>();
        switch (layeredGraph.getProperty(Properties.FIXED_ALIGNMENT)) {
        case LEFTDOWN:
            layouts.add(lefttop);
            break;
        case LEFTUP:
            layouts.add(leftbottom);
            break;
        case RIGHTDOWN:
            layouts.add(righttop);
            break;
        case RIGHTUP:
            layouts.add(rightbottom); 
            break;
        default:
            layouts.add(lefttop);
            layouts.add(righttop);
            layouts.add(leftbottom);
            layouts.add(rightbottom); 
        }
        
        BKAlignedLayout balanced = new BKAlignedLayout(nodeCount, null, null);

        // If layout options chose to use the balanced layout, it is calculated and added here.
        // If it is broken for any reason, one of the four other layouts is selected by the
        // given criteria.
        if (produceBalancedLayout) {
            balanced = createBalancedLayout(layouts, nodeCount);
            chosenLayout = balanced;
        }
        
        // Note that this condition can be true even if a balanced layout was produced, so don't you
        // dare adding an "else" here...
        if (!produceBalancedLayout || !checkOrderConstraint(layeredGraph, balanced)) {
            chosenLayout = null;
            for (BKAlignedLayout bal : layouts) {
                if (checkOrderConstraint(layeredGraph, bal)) {
                    if (chosenLayout == null) {
                        chosenLayout = bal;
                    } else {
                        if (bal.layoutSize() < chosenLayout.layoutSize()) {
                            chosenLayout = bal;
                        }
                    }
                }
            }
        }
        
        // If no layout is correct (which should never happen but is not provable to never happen),
        // the lefttop layout is chosen by default.
        if (chosenLayout == null) {
            chosenLayout = lefttop;
        }
        
        // Apply calculated positions to nodes.
        for (Layer layer : layeredGraph.getLayers()) {
            for (LNode node : layer.getNodes()) {
                node.getPosition().y = chosenLayout.y.get(node)
                        + chosenLayout.innerShift.get(node);
            }
        }

        // Debug output
        if (debug) {
            System.out.println(getBlocks(chosenLayout));
            System.out.println(chosenLayout);
            System.out.println(markedEdges);
        }

        markedEdges.clear();
        monitor.done();
    }

    /**
     * <p>This phase of the node placer marks all type 1 and type 2 conflicts.</p>
     * 
     * <p>The conflict types base on the distinction of inner segments and non-inner segments of edges.
     * A inner segment is present if an edge is drawn between two dummy nodes and thus is part of
     * a long edge. A non-inner segment is present if one of the connected nodes is not a dummy
     * node.</p>
     * 
     * <p>Type 0 conflicts occur if two non-inner segments cross each other. Type 1 conflicts happen 
     * when a non-inner segment and a inner segment cross. Type 2 conflicts are present if two
     * inner segments cross.</p>
     * 
     * <p>The markers are later used to solve conflicts in favor of long edges. In case of type 2
     * conflicts, the marker favors the earlier node in layout order.</p>
     * 
     * @param layeredGraph The layered graph to be layouted
     */
    private void markConflicts(final LGraph layeredGraph) {
        for (int i = 1; i <= layeredGraph.getLayers().size() - 2; i++) {
            // The variable naming here follows the notation of the corresponding paper
            // Normally, underscores are not allowed in local variable names, but since there
            // is no way of properly writing indices beside underscores, Checkstyle will be
            // disabled here and in future methods containing indexed variables
            // CHECKSTYLEOFF Local Variable Names
            int k_0 = 0;
            int l = 0;
            for (int l_1 = 0; l_1 < layerSize(layeredGraph, i + 1); l_1++) {
                // In the paper, l and i are indices for the layer and the position in the layer
                LNode v_l_i = nodeByPosition(layeredGraph, i + 1, l_1);
                
                if (l_1 == ((layerSize(layeredGraph, i + 1)) - 1)
                        || incidentToInnerSegment(v_l_i, i + 1, i)) {
                    
                    int k_1 = layerSize(layeredGraph, i) - 1;
                    if (incidentToInnerSegment(v_l_i, i + 1, i)) {
                        k_1 = allUpperNeighbors(v_l_i).get(0).getIndex();
                    }
                    
                    while (l <= l_1) {
                        LNode v_l = nodeByPosition(layeredGraph, i + 1, l);
                        
                        if (!incidentToInnerSegment(v_l, i + 1, i)) {
                            for (LNode upperNeighbor : allUpperNeighbors(v_l)) {
                                int k = upperNeighbor.getIndex();
                                
                                if (k < k_0 || k > k_1) {
                                    // Marked edge can't return null here, because the upper neighbor
                                    // relationship between v_l and upperNeighbor enforces the existence
                                    // of at least one edge between the two nodes
                                    markedEdges.add(getEdge(upperNeighbor, v_l));
                                }
                            }
                        }
                        
                        l++;
                    }
                    
                    k_0 = k_1;
                }
            }
            // CHECKSTYLEON Local Variable Names
        }
    }

    /**
     * <p>The graph is traversed in the given directions and nodes a grouped into blocks.</p>
     * 
     * <p>These blocks, respectively the contained nodes, will be drawn straight when the
     * algorithm is finished.</p>
     * 
     * <p>Type 1 conflicts are resolved, so that the dummy nodes of a long edge share the
     * same block if possible, such that the long edge is drawn straightly.</p>
     * 
     * @param layeredGraph The layered graph to be layouted
     * @param bal One of the four layouts which shall be used in this step 
     */
    private void verticalAlignment(final LGraph layeredGraph, final BKAlignedLayout bal) {
        // Initialize root and align maps
        for (Layer layer : layeredGraph.getLayers()) {
            for (LNode v : layer.getNodes()) {
                bal.root.put(v, v);
                bal.align.put(v, v);
                bal.innerShift.put(v, 0.0);
                
                if (v.getProperty(InternalProperties.NODE_TYPE) == NodeType.NORTH_SOUTH_PORT) {
                    bal.blockContainsNorthSouth.put(v, true);
                } else {
                    bal.blockContainsNorthSouth.put(v, false);
                }
                
                if (v.getProperty(InternalProperties.NODE_TYPE) == NodeType.NORMAL) {
                    bal.blockContainsRegularNode.put(v, true);
                } else {
                    bal.blockContainsRegularNode.put(v, false);
                }
            }
        }

        List<Layer> layers = layeredGraph.getLayers();
        
        // If the horizontal direction is bottom, the layers are traversed from
        // right to left, thus a reverse iterator is needed
        if (bal.hdir == HDirection.BOTTOM) {
            layers = Lists.reverse(layers);
        }

        for (Layer layer : layers) {
            // r denotes the position in layer order where the last block was found
            // It is initialized with -1, since nothing is found and the ordering starts with 0
            int r = -1;
            List<LNode> nodes = layer.getNodes();
            
            if (bal.vdir == VDirection.RIGHT) {
                // If the alignment direction is RIGHT, the nodes in a layer are traversed
                // reversely, thus we start at INT_MAX and with the reversed list of nodes.
                r = Integer.MAX_VALUE;
                nodes = Lists.reverse(nodes);
            }
            
            // Variable names here are again taken from the paper mentioned above.
            // i denotes the index of the layer and k the position of the node within the layer.
            // m denotes the position of a neighbor in the neighbor list of a node.
            // CHECKSTYLEOFF Local Variable Names
            for (LNode v_i_k : nodes) {
                List<LNode> neighbors = null;
                if (bal.hdir == HDirection.BOTTOM) {
                    neighbors = allLowerNeighbors(v_i_k);
                } else {
                    neighbors = allUpperNeighbors(v_i_k);
                }

                if (neighbors.size() > 0) {

                    // When a node has many upper neighbors, consider only the (two) nodes in the
                    // middle.
                    int d = neighbors.size();
                    int low = ((int) Math.floor(((d + 1.0) / 2.0))) - 1;
                    int high = ((int) Math.ceil(((d + 1.0) / 2.0))) - 1;

                    if (bal.vdir == VDirection.RIGHT) {
                        // Check, whether v_i_k can be added to a block of its upper/lower neighbor(s)
                        for (int m = high; m >= low; m--) {
                            if (bal.align.get(v_i_k).equals(v_i_k)) {
                                LNode u_m = neighbors.get(m);
                                // Again, getEdge won't return null because the neighbor relationship
                                // ensures that at least one edge exists
                                if (!markedEdges.contains(getEdge(u_m, v_i_k)) && r > u_m.getIndex()) {
                                    bal.align.put(u_m, v_i_k);
                                    bal.root.put(v_i_k, bal.root.get(u_m));
                                    bal.align.put(v_i_k, bal.root.get(v_i_k));
                                    
                                    if (bal.blockContainsNorthSouth.get(v_i_k)) {
                                        bal.blockContainsNorthSouth.put(bal.root.get(v_i_k), true);
                                    }
                                    
                                    if (bal.blockContainsRegularNode.get(v_i_k)) {
                                        bal.blockContainsRegularNode.put(bal.root.get(v_i_k), true);
                                    }
                                    
                                    r = u_m.getIndex();
                                }
                            }
                        }
                    } else {
                        // Check, whether vik can be added to a block of its upper/lower neighbor(s)
                        for (int m = low; m <= high; m++) {
                            if (bal.align.get(v_i_k).equals(v_i_k)) {
                                LNode um = neighbors.get(m);
                                if (!markedEdges.contains(getEdge(um, v_i_k)) && r < um.getIndex()) {
                                    bal.align.put(um, v_i_k);
                                    bal.root.put(v_i_k, bal.root.get(um));
                                    bal.align.put(v_i_k, bal.root.get(v_i_k));
                                    
                                    if (bal.blockContainsNorthSouth.get(v_i_k)) {
                                        bal.blockContainsNorthSouth.put(bal.root.get(v_i_k), true);
                                    }
                                    
                                    if (bal.blockContainsRegularNode.get(v_i_k)) {
                                        bal.blockContainsRegularNode.put(bal.root.get(v_i_k), true);
                                    }
                                    
                                    r = um.getIndex();
                                }
                            }
                        }
                    }
                }
            }
            // CHECKSTYLEOFF Local Variable Names
        }
    }

    /**
     * <p>This phase moves the nodes inside a block, ensuring that all edges inside a block
     * can be drawn straightly.</p>
     * 
     * <p>This phase is not included in the original algorithm and adds port and node size
     * handling.</p>
     * 
     * @param layeredGraph The layered graph to be layouted
     * @param bal One of the four layouts which shall be used in this step
     */
    private void insideBlockShift(final LGraph layeredGraph, final BKAlignedLayout bal) {
        HashMap<LNode, List<LNode>> blocks = getBlocks(bal);
        for (LNode root : blocks.keySet()) {
            /* For each block, we place the top left corner of the root node at coordinate (0,0). We
             * then calculate the space required above the top left corner (due to other nodes placed
             * above and to top margins of nodes, including the root node) and the space required below
             * the top left corner. The sum of both becomes the block size, and the y coordinate of each
             * node relative to the block's top border becomes the inner shift of that node.
             */
            
            double spaceAbove = 0.0;
            double spaceBelow = 0.0;
            
            // Reserve space for the root node
            spaceAbove = root.getMargin().top;
            spaceBelow = root.getSize().y + root.getMargin().bottom;
            bal.innerShift.put(root, 0.0);
            
            // Iterate over all other nodes of the block
            LNode current = root;
            LNode next;
            while ((next = bal.align.get(current)) != root) {
                // Find the edge between the current and the next node
                LEdge edge = getEdge(current, next);
                
                // Calculate the y coordinate difference between the two nodes required to straighten
                // the edge
                double difference = 0.0;
                if (bal.hdir == HDirection.BOTTOM) {
                    difference = edge.getTarget().getPosition().y + edge.getTarget().getAnchor().y
                            - edge.getSource().getPosition().y - edge.getSource().getAnchor().y;
                } else {
                    difference = edge.getSource().getPosition().y + edge.getSource().getAnchor().y
                            - edge.getTarget().getPosition().y - edge.getTarget().getAnchor().y;
                }
                
                // The current node already has an inner shift value that we need to use as the basis
                // to calculate the next node's inner shift
                double currentInnerShift = bal.innerShift.get(current) + difference;
                bal.innerShift.put(next, currentInnerShift);
                
                // Update the space required above and below the root node's top left corner
                spaceAbove = Math.max(spaceAbove,
                        next.getMargin().top - currentInnerShift);
                spaceBelow = Math.max(spaceBelow,
                        currentInnerShift + next.getSize().y + next.getMargin().bottom);
                                
                // The next node is the current node in the next iteration
                current = next;
            }
            
            // Adjust each node's inner shift by the space required above the root node's top left
            // corner (which the inner shifts are relative to at the moment)
            current = root;
            do {
                bal.innerShift.put(current, bal.innerShift.get(current) + spaceAbove);
                current = bal.align.get(current);
            } while (current  != root);
            
            // Remember the block size
            bal.blockSize.put(root, spaceAbove + spaceBelow);
        }
    }

    /**
     * <p>In this step, actual coordinates are calculated for blocks and its nodes.</p>
     * 
     * <p>First, all blocks are placed, trying to avoid any crossing of the blocks.
     * Then, the blocks are shifted towards each other if there is any space for 
     * compaction.</p>
     * 
     * @param layeredGraph The layered graph to be layouted
     * @param bal One of the four layouts which shall be used in this step
     */
    private void horizontalCompaction(final LGraph layeredGraph, final BKAlignedLayout bal) {
        // Initialize fields with basic values, partially depending on the direction
        for (Layer layer : layeredGraph.getLayers()) {
            for (LNode node : layer.getNodes()) {
                bal.sink.put(node, node);
                if (bal.vdir == VDirection.RIGHT) {
                    bal.shift.put(node, Double.NEGATIVE_INFINITY);
                } else {
                    bal.shift.put(node, Double.POSITIVE_INFINITY);
                }
            }
        }

        List<Layer> layers = layeredGraph.getLayers();
        // If the horizontal direction is bottom, the layers are traversed from
        // right to left, thus a reverse iterator is needed
        if (bal.hdir == HDirection.BOTTOM) {
            layers = Lists.reverse(layers);
        }

        for (Layer layer : layers) {
            // As with layers, we need a reversed iterator for blocks for different directions
            List<LNode> nodes = layer.getNodes();
            if (bal.vdir == VDirection.RIGHT) {
                nodes = Lists.reverse(nodes);
            }
            
            // Do a initial placement for all blocks
            for (LNode v : nodes) {
                if (bal.root.get(v).equals(v)) {
                    placeBlock(v, bal);
                }
            }
        }

        // Try to compact blocks by shifting them towards each other if there is space
        // between them. It's important to traverse top-bottom or bottom-top here too
        for (Layer layer : layers) {
            for (LNode v : layer.getNodes()) {
                bal.y.put(v, bal.y.get(bal.root.get(v)));
                if (bal.vdir == VDirection.RIGHT) {
                    if (v.equals(bal.root.get(v))
                            && bal.shift.get(bal.sink.get(v)) > Double.NEGATIVE_INFINITY) {
                        
                        bal.y.put(v,
                                bal.y.get(v) + bal.shift.get(bal.sink.get(v)));
                    }
                } else {
                    if (v.equals(bal.root.get(v))
                            && bal.shift.get(bal.sink.get(v)) < Double.POSITIVE_INFINITY) {
                        
                        bal.y.put(v,
                                bal.y.get(v) + bal.shift.get(bal.sink.get(v)));
                    }
                }
            }
        }

    }

    /**
     * <p>Blocks are placed based on their root node.</p>
     * 
     * <p>This is done by watching all layers which are crossed by this block and
     * moving the whole block up/downwards if there are blocks which already occupy
     * the chosen position.</p>
     * 
     * @param v The root node of the block
     * @param bal One of the four layouts which shall be used in this step
     */
    private void placeBlock(final LNode v, final BKAlignedLayout bal) {
        // Only place block if it does not have a placement already
        if (!bal.y.containsKey(v)) {
            bal.y.put(v, 0.0);
            LNode w = v;
            
            // Iterate through block and determine, where the block can be placed
            do {
                // If the node is the top or bottom node of it's layer, it can be, depending
                // on the current direction, placed safely since it is the first to be placed
                if ((bal.vdir == VDirection.LEFT && w.getIndex() > 0)
                        || (bal.vdir == VDirection.RIGHT && w.getIndex() < (w.getLayer()
                                .getNodes().size() - 1))) {

                    // Get the node which is top/bottom to the node to be placed to check,
                    // whether the current node conflicts with it
                    LNode u = null;
                    LNode x = null;
                    if (bal.vdir == VDirection.RIGHT) {
                        x = w.getLayer().getNodes().get(w.getIndex() + 1);
                        u = bal.root.get(x);
                    } else {
                        x = w.getLayer().getNodes().get(w.getIndex() - 1);
                        u = bal.root.get(x);
                    }

                    // Check whether the comparison node is already placed, place it if not
                    placeBlock(u, bal);
                    
                    // Note that the two nodes and their blocks form a unit called class in the
                    // original algorithm. These are combinations of blocks which play a role
                    // in the final compaction
                    if (bal.sink.get(v).equals(v)) {
                        bal.sink.put(v, bal.sink.get(u));
                    }
                    
                    // If two nodes aren't member of the same class, calculate how the two classes
                    // might be compacted later on
                    if (!bal.sink.get(v).equals(bal.sink.get(u))) {
                        double spacing = normalSpacing;
                        if (bal.vdir == VDirection.RIGHT) {
                            bal.shift.put(
                                    bal.sink.get(u),
                                    Math.max(bal.shift.get(bal.sink.get(u)), bal.y.get(v)
                                            - bal.y.get(u)
                                            + bal.blockSize.get(v)
                                            + spacing));
                        } else {
                            bal.shift.put(
                                    bal.sink.get(u),
                                    Math.min(bal.shift.get(bal.sink.get(u)), bal.y.get(v)
                                            - bal.y.get(u)
                                            - bal.blockSize.get(u)
                                            - spacing));
                        }
                    } else {
                        // If they are on the class, calculate a y position for the current block,
                        // using the information from the comparison node
                        
                        // Determine the available space in the current layer, by taking node sizes,
                        // and special node types into account
                        double spacing = normalSpacing;
                        double wSize = w.getSize().y + w.getMargin().bottom
                                + bal.innerShift.get(w);
                        double xSize = x.getSize().y + x.getMargin().bottom;
                        
                        if (w.getProperty(InternalProperties.NODE_TYPE) == NodeType.NORTH_SOUTH_PORT) {
                            wSize += NORTH_SOUTH_SPACING;
                        }
                        
                        if (x.getProperty(InternalProperties.NODE_TYPE) == NodeType.NORTH_SOUTH_PORT) {
                            xSize += NORTH_SOUTH_SPACING;
                        }
                        
                        // Check if we may use small spacing
                        if ((!(blockContainsNorthSouthDummy(bal, v) && blockContainsRegularNode(bal, u))
                          && !(blockContainsNorthSouthDummy(bal, u) && blockContainsRegularNode(bal, v))
                          && (bal.blockSize.get(v) == 0.0 || bal.blockSize.get(u) == 0.0))) {
                            
                            spacing = smallSpacing;
                        }
                        
                        // Determine the block's final position
                        if (bal.vdir == VDirection.RIGHT) {
                            bal.y.put(
                                    v,
                                    Math.min(bal.y.get(v),
                                             (bal.y.get(u)
                                                     + bal.innerShift.get(x)
                                                     - x.getMargin().top)
                                              - spacing - wSize));

                        } else {
                            bal.y.put(
                                    v,
                                    Math.max(bal.y.get(v),
                                            (bal.y.get(u) 
                                                    + bal.innerShift.get(x)
                                                    + x.getMargin().top)
                                             + spacing + xSize));
                        }
                    }
                }
                // Get the next node in the block
                w = bal.align.get(w);
            } while (w != v);
        }
    }

    /**
     * <p>A balanced layout is calculated by determining the median layout of the
     * four layouts.</p>
     * 
     * <p>First, the layout with the smallest height, meaning the difference between the highest and the
     * lowest y-coordinate placement, is used as a starting point.
     * Then, the median position of each of the four layouts is used for determining
     * the final position.</p>
     * 
     * @param layouts The four calculated layouts
     * @param nodeCount The number of nodes in the graph
     * @return A balanced layout, the median of the four layouts
     */
    private BKAlignedLayout createBalancedLayout(final List<BKAlignedLayout> layouts,
            final int nodeCount) {
        
        final int noOfLayouts = layouts.size();
        BKAlignedLayout balanced = new BKAlignedLayout(nodeCount, null, null);
        double[] width = new double[noOfLayouts];
        double[] min = new double[noOfLayouts];
        double[] max = new double[noOfLayouts];
        int minWidthLayout = 0;

        // Find the smallest layout
        for (int i = 0; i < noOfLayouts; i++) {
            min[i] = Integer.MAX_VALUE;
            max[i] = Integer.MIN_VALUE;
        }
        
        for (int i = 0; i < noOfLayouts; i++) {
            BKAlignedLayout current = layouts.get(i);
            for (double y : current.y.values()) {
                if (min[i] > y) {
                    min[i] = y;
                }
                
                if (max[i] < y) {
                    max[i] = y;
                }
            }
            
            width[i] = max[i] - min[i];
            if (width[minWidthLayout] > width[i]) {
                minWidthLayout = i;
            }
        }

        // Find the shift between the smallest and the four layouts
        double[] shift = new double[noOfLayouts];
        for (int i = 0; i < noOfLayouts; i++) {
            if (layouts.get(i).vdir == VDirection.LEFT) {
                shift[i] = min[minWidthLayout] - min[i];
            } else {
                shift[i] = max[minWidthLayout] - max[i];
            }
        }

        // Calculated y-coordinates for a balanced placement
        double[] calculatedYs = new double[noOfLayouts];
        for (LNode node : layouts.get(0).y.keySet()) {
            for (int i = 0; i < noOfLayouts; i++) {
                calculatedYs[i] = layouts.get(i).y.get(node) + shift[i];
            }
            
            Arrays.sort(calculatedYs);
            balanced.y.put(node, (calculatedYs[1] + calculatedYs[2]) / 2.0);
            balanced.innerShift.put(node,
                    layouts.get(minWidthLayout).innerShift.get(node));
        }

        return balanced;
    }

    /**
     * Auxiliary method for getting the size of a layer.
     * 
     * @param layeredGraph The containing layered graph
     * @param layer The respective layer
     * @return The size of the given layer
     */
    private int layerSize(final LGraph layeredGraph, final int layer) {
        return layeredGraph.getLayers().get(layer).getNodes().size();
    }

    /**
     * Auxiliary method for getting the node on a certain position of a layer.
     * 
     * @param layeredGraph The containing layered graph
     * @param layer The containing layer
     * @param position The node's position, with 0 <= position <= layer.size - 1
     * @return The node which is on the given position of the given layer or an exception, if there is
     *         no node on the given position
     */
    private LNode nodeByPosition(final LGraph layeredGraph, final int layer,
            final int position) {
        
        return layeredGraph.getLayers().get(layer).getNodes().get(position);
    }

    /**
     * Checks whether the given node is part of a long edge between the two given layers.
     * 
     * @param node Possible long edge node
     * @param layer1 The first layer, the layer of the node
     * @param layer2 The second layer
     * @return True if the node is part of a long edge between the layers, false else
     */
    private boolean incidentToInnerSegment(final LNode node, final int layer1, final int layer2) {
        
        // consider that big nodes include their respective start and end node.
        if (node.getProperty(InternalProperties.NODE_TYPE) == NodeType.BIG_NODE) {
            // all nodes should be placed straightly
            return true;
        }
        
        if (node.getProperty(InternalProperties.NODE_TYPE) == NodeType.LONG_EDGE) {
            for (LEdge edge : node.getIncomingEdges()) {
                if (edge.getSource().getNode().getProperty(InternalProperties.NODE_TYPE)
                            == NodeType.LONG_EDGE
                        && edge.getSource().getNode().getLayer().getIndex() == layer2
                        && node.getLayer().getIndex() == layer1) {
                    
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Gives all upper neighbors of a given node.
     * 
     * An upper neighbor is a node in a previous layer which has an edge pointing to the given node.
     * 
     * @param node The node which might have neighbors
     * @return A list containing all upper neighbors
     */
    private List<LNode> allUpperNeighbors(final LNode node) {
        List<LNode> result = new LinkedList<LNode>();
        int maxPriority = 0;
        
        for (LEdge edge : node.getIncomingEdges()) {
            if (edge.getProperty(Properties.PRIORITY) > maxPriority) {
                maxPriority = edge.getProperty(Properties.PRIORITY);
            }
        }
        
        for (LEdge edge : node.getIncomingEdges()) {
            if (node.getLayer() != edge.getSource().getNode().getLayer()
                    && edge.getProperty(Properties.PRIORITY) == maxPriority) {
                result.add(edge.getSource().getNode());
            }
        }
        
        Collections.sort(result, new NeighborComparator());
        return result;
    }

    /**
     * Give all lower neighbors of a given node.
     * 
     * A lower neighbor is a node in a following layer which has an edge coming from the given node.
     * 
     * @param node The node which might have neighbors
     * @return A list containing all lower neighbors
     */
    private List<LNode> allLowerNeighbors(final LNode node) {
        List<LNode> result = new LinkedList<LNode>();
        int maxPriority = 0;
        
        for (LEdge edge : node.getOutgoingEdges()) {
            if (edge.getProperty(Properties.PRIORITY) > maxPriority) {
                maxPriority = edge.getProperty(Properties.PRIORITY);
            }
        }
        
        for (LEdge edge : node.getOutgoingEdges()) {
            if (node.getLayer() != edge.getTarget().getNode().getLayer()
                    && edge.getProperty(Properties.PRIORITY) == maxPriority) {
                result.add(edge.getTarget().getNode());
            }
        }
        
        Collections.sort(result, new NeighborComparator());
        return result;
    }

    /**
     * An auxiliary method to find an edge between two given nodes.
     * 
     * @param source The source node of the edge
     * @param target The target node of the edge
     * @return The edge between source and target, or null if there is none
     */
    private LEdge getEdge(final LNode source, final LNode target) {
        for (LEdge edge : source.getConnectedEdges()) {
            if (edge.getTarget().getNode().equals(target)
                    || edge.getSource().getNode().equals(target)) {
                
                return edge;
            }
        }
        return null;
    }

    /**
     * An auxiliary method for finding all blocks of a given layout.
     * 
     * @param bal The layout of which the blocks shall be found
     * @return The blocks of the given layout
     */
    private HashMap<LNode, List<LNode>> getBlocks(final BKAlignedLayout bal) {
        HashMap<LNode, List<LNode>> blocks = new HashMap<LNode, List<LNode>>();
        for (LNode key : bal.root.keySet()) {
            if (!blocks.containsKey(bal.root.get(key))) {
                blocks.put(bal.root.get(key), new LinkedList<LNode>());
            }
            blocks.get(bal.root.get(key)).add(key);
        }
        return blocks;
    }
    
    /**
     * Checks whether any north-south port dummies are included in the block
     * given by the root node.
     * 
     * @param bal The layout of which the blocks shall be found
     * @param root The root of the block to investigate
     * @return True, if the block contains a north-south dummy, false else
     */
    private boolean blockContainsNorthSouthDummy(final BKAlignedLayout bal, final LNode root) {
        return bal.blockContainsRegularNode.get(root);
    }
    
    /**
     * Checks whether any regular nodes are included in the block
     * given by the root node.
     * 
     * @param bal The layout of which the blocks shall be found
     * @param root The root of the block to investigate
     * @return True, if the block contains a regular node, false else
     */
    private boolean blockContainsRegularNode(final BKAlignedLayout bal, final LNode root) {
        return bal.blockContainsRegularNode.get(root);
    }

    /**
     * It is checked whether all nodes are placed in the correct order in their layers
     * and do not overlap each other.
     * 
     * @param layeredGraph the containing layered graph.
     * @param bal the layout which shall be checked.
     * @return {@code true} if the order is preserved and no nodes overlap, {@code false} otherwise.
     */
    private boolean checkOrderConstraint(final LGraph layeredGraph, final BKAlignedLayout bal) {
        // Check if the layout contains Y coordinate information
        if (bal.y.isEmpty()) {
            return false;
        }
        
        // Flag indicating if any problems were found
        boolean layoutIsSane = true;
        
        // Iterate over the layers
        for (Layer layer : layeredGraph.getLayers()) {
            // Current Y position in the layer
            double pos = Double.NEGATIVE_INFINITY;
            
            // We remember the previous node for debug output
            LNode previous = null;
            
            // Iterate through the layer's nodes
            for (LNode node : layer.getNodes()) {
                // For the layout to be correct, both the node's top border and its bottom border must
                // be beyond the current position in the layer
                double top = bal.y.get(node) + bal.innerShift.get(node) - node.getMargin().top;
                double bottom = bal.y.get(node) + bal.innerShift.get(node) + node.getSize().y
                        + node.getMargin().bottom;
                
                if (top > pos && bottom > pos) {
                    previous = node;
                    
                    // Update the position inside the layer
                    pos = bal.y.get(node) + bal.innerShift.get(node) + node.getSize().y
                            + node.getMargin().bottom;
                } else {
                    // We've found an overlap
                    layoutIsSane = false;
                    if (debug) {
                        System.out.println("breaks on " + node
                                + " which should have been after " + previous);
                    }
                    break;
                }
            }
        }
        
        if (debug) {
            System.out.println(bal + " is correct: " + layoutIsSane);
        }
        
        return layoutIsSane;
    }

    /**
     * Comparator which determines the order of nodes in a layer.
     */
    private static class NeighborComparator implements Comparator<LNode>, Serializable {
        /** The serial version UID. */
        private static final long serialVersionUID = 7540379553811800233L;

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
     * Class which holds all information about a layout in one of the four direction
     * combinations.
     */
    private static class BKAlignedLayout {

        /** The root node of each node in a block. */
        private HashMap<LNode, LNode> root;

        /** The size of a block. */
        private HashMap<LNode, Double> blockSize;

        /** The next node in a block, or the first if the current node is the last,
         * forming a ring. */
        private HashMap<LNode, LNode> align;

        /** The value by which a node must be shifted to stay straight inside a block. */
        private HashMap<LNode, Double> innerShift;
        
        /** The root node of a class, mapped from block root nodes to class root nodes. */
        private HashMap<LNode, LNode> sink;

        /** The value by which a block must be shifted for a more compact placement. */
        private HashMap<LNode, Double> shift;

        /** The y-coordinate of every node, forming the final layout. */
        private HashMap<LNode, Double> y;
        
        /** Stores, whether a block contains a NORTH SOUTH port dummy. */
        private HashMap<LNode, Boolean> blockContainsNorthSouth;
        
        /** Stores, whether a block contains a regular node. */
        private HashMap<LNode, Boolean> blockContainsRegularNode;

        /** The vertical direction of the current layout. */
        private VDirection vdir;

        /** The horizontal direction of the current layout. */
        private HDirection hdir;

        /**
         * Basic constructor for a layout.
         */
        public BKAlignedLayout(final int nodeCount, final VDirection vdir, final HDirection hdir) {
            root = Maps.newHashMapWithExpectedSize(nodeCount);
            blockSize = Maps.newHashMapWithExpectedSize(nodeCount);
            align = Maps.newHashMapWithExpectedSize(nodeCount);
            innerShift = Maps.newHashMapWithExpectedSize(nodeCount);
            sink = Maps.newHashMapWithExpectedSize(nodeCount);
            shift = Maps.newHashMapWithExpectedSize(nodeCount);
            y = Maps.newHashMapWithExpectedSize(nodeCount);
            blockContainsNorthSouth = Maps.newHashMapWithExpectedSize(nodeCount);
            blockContainsRegularNode = Maps.newHashMapWithExpectedSize(nodeCount);
            this.vdir = vdir;
            this.hdir = hdir;
        }

        /**
         * Calculate the layout size for comparison.
         * 
         * @return The size of the layout
         */
        public double layoutSize() {
            double min = Double.POSITIVE_INFINITY;
            double max = Double.NEGATIVE_INFINITY;
            for (double i : y.values()) {
                if (i < min) {
                    min = i;
                }
                if (i > max) {
                    max = i;
                }
            }
            min = Math.abs(min);
            return max + min;
        }

        @Override
        public String toString() {
            String result = "";
            if (vdir == VDirection.LEFT) {
                result += "LEFT";
            } else if (vdir == VDirection.RIGHT) {
                result += "RIGHT";
            } else {
                result += "BALANCED";
            }
            if (hdir == HDirection.TOP) {
                result += "TOP";
            } else if (hdir == HDirection.BOTTOM) {
                result += "BOTTOM";
            }
            return result;
        }

    }

    /**
     * Vertical direction enumeration.
     */
    private enum VDirection {
        LEFT, RIGHT;
    }

    /**
     * Horizontal direction enumeration.
     */
    private enum HDirection {
        TOP, BOTTOM;
    }
}
