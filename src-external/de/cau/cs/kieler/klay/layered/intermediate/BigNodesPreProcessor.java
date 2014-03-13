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
package de.cau.cs.kieler.klay.layered.intermediate;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.Lists;

import de.cau.cs.kieler.core.alg.IKielerProgressMonitor;
import de.cau.cs.kieler.kiml.options.LayoutOptions;
import de.cau.cs.kieler.kiml.options.PortConstraints;
import de.cau.cs.kieler.kiml.options.PortSide;
import de.cau.cs.kieler.klay.layered.ILayoutProcessor;
import de.cau.cs.kieler.klay.layered.graph.LEdge;
import de.cau.cs.kieler.klay.layered.graph.LGraph;
import de.cau.cs.kieler.klay.layered.graph.LNode;
import de.cau.cs.kieler.klay.layered.graph.LPort;
import de.cau.cs.kieler.klay.layered.properties.InternalProperties;
import de.cau.cs.kieler.klay.layered.properties.NodeType;
import de.cau.cs.kieler.klay.layered.properties.Properties;

/**
 * Nodes that exceed a certain width are considered <emph>Big Nodes</emph>. Currently either a
 * minimal width of 50, or, if wider, the smallest node are used as criterion. Big nodes are then
 * replaced by <code>n</code> dummy nodes, where n equals the width of a specific node divided by
 * the previously calculated minimal width. Each dummy node gets assigned the height of the original
 * node, as well, as the {@link PortConstraints} value and is connected to its predecessor by
 * exactly one edge. EAST ports of the original node are moved to the lastly created dummy node.
 * 
 * 
 * <h2>Remarks</h2>
 * <ul>
 *   <li>During minimal width calculation, any type of dummy node is ignored.</li>
 *   <li>Only handle nodes with {@link PortConstraints} <= {@link PortConstraints#FIXED_ORDER}, or for
 *       greater port constraints we demand that the node has no NORTH and SOUTH ports.</li>
 *   <li>Big nodes with incoming edges on EAST ports are ignored.</li>
 * </ul>
 * 
 * 
 * <dl>
 *   <dt>Precondition:</dt>
 *     <dd>an acyclic graph, where every port has an assigned side.</dd>
 *   <dt>Postcondition:</dt>
 *     <dd>big nodes are split into a series of dummy nodes.</dd>
 *   <dt>Slots:</dt>
 *     <dd>Before phase 2.</dd>
 *   <dt>Same-slot dependencies:</dt>
 *     <dd>{@link PortSideProcessor}</dd>
 * </dl>
 * 
 * @see BigNodesIntermediateProcessor
 * @see BigNodesPostProcessor
 * 
 * @author uru
 */
public class BigNodesPreProcessor implements ILayoutProcessor {

    /**
     * Minimal width into which nodes are split, the smaller this value, the more big node dummy
     * nodes are possibly introduced.
     */
    private static final double MIN_WIDTH = 50;
    /** The current graph. */
    private LGraph layeredGraph;
    /** Used to assign ids to newly created dummy nodes. */
    private int dummyID = 0;

    /**
     * {@inheritDoc}
     */
    public void process(final LGraph theLayeredGraph, final IKielerProgressMonitor monitor) {
        monitor.begin("Big nodes pre-processing", 1);

        this.layeredGraph = theLayeredGraph;
        List<LNode> nodes = layeredGraph.getLayerlessNodes();
        // re-index nodes
        int counter = 0;
        for (LNode node : nodes) {
            node.id = counter++;
        }

        // the object spacing in the drawn graph
        double minSpacing = layeredGraph.getProperty(Properties.OBJ_SPACING);
        // the ID for the most recently created dummy node
        dummyID = nodes.size();

        // Compute width of narrowest node
        double minWidth = Float.MAX_VALUE;
        for (LNode node : nodes) {
            // ignore all dummy nodes
            if ((node.getProperty(InternalProperties.NODE_TYPE) == NodeType.NORMAL)
                    && (node.getSize().x < minWidth)) {
                minWidth = node.getSize().x;
            }
        }

        // assure a capped minimal width
        minWidth = Math.max(MIN_WIDTH, minWidth);

        // initialize width array
        int[] width = new int[nodes.size()];
        Arrays.fill(width, 1);

        // collect all nodes that are considered "big"
        List<LNode> bigNodes = Lists.newLinkedList();
        double threshold = (minWidth + minSpacing);
        for (LNode node : nodes) {
            if ((node.getProperty(InternalProperties.NODE_TYPE) == NodeType.NORMAL)
                    && (node.getSize().x > threshold)) {
                Double parts = Math.ceil(node.getSize().x / minWidth);
                width[node.id] = parts.intValue();
                bigNodes.add(node);
            }
        }

        // handle each big node
        for (LNode node : bigNodes) {
            // is this big node ok?
            if (!isProcessorApplicable(node)) {
                continue;
            }

            // remember east ports
            LinkedList<LPort> eastPorts = new LinkedList<LPort>();
            for (LPort port : node.getPorts()) {
                if (port.getSide() == PortSide.EAST) {
                    eastPorts.add(port);
                }
            }
            
            // remember original width for later step
            double originalWidth = node.getSize().x; 

            // shrink the big node and mark it
            node.setProperty(InternalProperties.BIG_NODE_ORIGINAL_SIZE, (float) node.getSize().x);
            node.getSize().x = minWidth;
            node.setProperty(InternalProperties.BIG_NODE_INITIAL, true);

            // introduce dummy nodes
            LNode start = node;
            // the original node represents 1*minWidth
            originalWidth -= minWidth;
            
            while (width[node.id] > 1) {
                // create it and add dummy to the graph
                double dummyWidth = Math.min(originalWidth, minWidth);
                start = introduceDummyNode(start, dummyWidth);
                layeredGraph.getLayerlessNodes().add(start);

                width[node.id]--;
                originalWidth -= minWidth;
            }

            // add the east ports to the final dummy
            for (LPort port : eastPorts) {
                node.getPorts().remove(port);
                port.setNode(start);
            }
        }

        monitor.done();
    }

    /**
     * Only handle nodes with {@link PortConstraints} <= {@link PortConstraints#FIXED_ORDER}, or for
     * greater port constraints we demand that the node has no NORTH and SOUTH ports.
     * 
     * @param node
     * @return true if we can apply big nodes processing
     */
    private boolean isProcessorApplicable(final LNode node) {

        if (node.getProperty(LayoutOptions.PORT_CONSTRAINTS) == PortConstraints.FIXED_RATIO
                || node.getProperty(LayoutOptions.PORT_CONSTRAINTS) == PortConstraints.FIXED_POS) {
            for (LPort port : node.getPorts()) {
                if (port.getSide() == PortSide.NORTH || port.getSide() == PortSide.SOUTH) {
                    return false;
                }
            }
        }

        return true;
    }

    private LNode introduceDummyNode(final LNode src, final double width) {
        // create new dummy node
        LNode dummy = new LNode(layeredGraph);
        dummy.setProperty(InternalProperties.NODE_TYPE, NodeType.BIG_NODE);
        dummy.setProperty(LayoutOptions.PORT_CONSTRAINTS,
                src.getProperty(LayoutOptions.PORT_CONSTRAINTS));
        dummy.id = dummyID++;

        // set same height as original
        dummy.getSize().y = src.getSize().y;
        // the first n-1 nodes (initial+dummies) are assigned a width of 'minWidth'
        // while the last node (right most) is assigned the remaining
        // width of the bignode, i.e.
        //      overallWidth - (n-1) * minWidth
        dummy.getSize().x = width;

        // add ports to connect it with the previous node
        LPort outPort = new LPort(layeredGraph);
        outPort.setSide(PortSide.EAST);
        outPort.setNode(src);
        // assign reasonable positions to the port in case of FIXES_POS
        outPort.getPosition().x = dummy.getSize().x;
        outPort.getPosition().y = dummy.getSize().y / 2;

        LPort inPort = new LPort(layeredGraph);
        inPort.setSide(PortSide.WEST);
        inPort.setNode(dummy);
        inPort.getPosition().y = dummy.getSize().y / 2;
        inPort.getPosition().x = -inPort.getSize().x;

        // add edge to connect it with the previous node
        LEdge edge = new LEdge(layeredGraph);
        edge.setSource(outPort);
        edge.setTarget(inPort);

        return dummy;
    }
}
