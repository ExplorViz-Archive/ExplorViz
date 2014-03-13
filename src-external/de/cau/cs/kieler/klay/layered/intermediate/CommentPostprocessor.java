/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 * 
 * Copyright 2011 by
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
import de.cau.cs.kieler.core.math.KVector;
import de.cau.cs.kieler.klay.layered.ILayoutProcessor;
import de.cau.cs.kieler.klay.layered.graph.LEdge;
import de.cau.cs.kieler.klay.layered.graph.LInsets;
import de.cau.cs.kieler.klay.layered.graph.LNode;
import de.cau.cs.kieler.klay.layered.graph.LPort;
import de.cau.cs.kieler.klay.layered.graph.Layer;
import de.cau.cs.kieler.klay.layered.graph.LGraph;
import de.cau.cs.kieler.klay.layered.properties.InternalProperties;
import de.cau.cs.kieler.klay.layered.properties.Properties;

/**
 * Post-processor for comment boxes. If any comments are found that were removed by the
 * {@link CommentPreprocessor}, they are reinserted and placed above or below their
 * corresponding connected node. This requires the margin around the node to be large
 * enough to hold all comments, which is ensured by the {@link NodeMarginCalculator}.
 * 
 * <dl>
 *   <dt>Precondition:</dt><dd>Comments have been processed by {@link CommentPreprocessor}.
 *     Nodes are organized in layers and have been placed with enough spacing to hold their
 *     connected comment boxes.</dd>
 *   <dt>Postcondition:</dt><dd>Comments that have been removed by pre-processing are
 *     reinserted properly in the graph.</dd>
 *   <dt>Slots:</dt><dd>After phase 5.</dd>
 * </dl>
 *
 * @author msp
 * @kieler.design 2012-08-10 chsch grh
 * @kieler.rating proposed yellow by msp
 */
public final class CommentPostprocessor implements ILayoutProcessor {

    /**
     * {@inheritDoc}
     */
    public void process(final LGraph layeredGraph, final IKielerProgressMonitor monitor) {
        monitor.begin("Comment post-processing", 1);
        double spacing = layeredGraph.getProperty(Properties.OBJ_SPACING);
        
        for (Layer layer : layeredGraph) {
            List<LNode> boxes = new LinkedList<LNode>();
            for (LNode node : layer) {
                List<LNode> topBoxes = node.getProperty(InternalProperties.TOP_COMMENTS);
                List<LNode> bottomBoxes = node.getProperty(InternalProperties.BOTTOM_COMMENTS);
                if (topBoxes != null || bottomBoxes != null) {
                    process(node, topBoxes, bottomBoxes, spacing);
                    if (topBoxes != null) {
                        boxes.addAll(topBoxes);
                    }
                    if (bottomBoxes != null) {
                        boxes.addAll(bottomBoxes);
                    }
                }
            }
            layer.getNodes().addAll(boxes);
        }
        
        monitor.done();
    }
    
    /**
     * Process a node with its connected comment boxes.
     * 
     * @param node a normal node
     * @param topBoxes a list of boxes to be placed on top, or {@code null}
     * @param bottomBoxes a list of boxes to be placed in the bottom, or {@code null}
     * @param spacing the overall spacing value
     */
    private void process(final LNode node, final List<LNode> topBoxes,
            final List<LNode> bottomBoxes, final double spacing) {
        
        KVector nodePos = node.getPosition();
        KVector nodeSize = node.getSize();
        LInsets margin = node.getMargin();
        
        if (topBoxes != null) {
            // determine the total width and maximal height of the top boxes
            double boxesWidth = spacing / 2 * (topBoxes.size() - 1);
            double maxHeight = 0;
            for (LNode box : topBoxes) {
                boxesWidth += box.getSize().x;
                maxHeight = Math.max(maxHeight, box.getSize().y);
            }
            
            // place the boxes on top of the node, horizontally centered around the node itself
            double x = nodePos.x - (boxesWidth - nodeSize.x) / 2;
            double baseLine = nodePos.y - margin.top + maxHeight;
            double anchorInc = nodeSize.x / (topBoxes.size() + 1);
            double anchorX = anchorInc;
            for (LNode box : topBoxes) {
                box.getPosition().x = x;
                box.getPosition().y = baseLine - box.getSize().y;
                x += box.getSize().x + spacing / 2;
                // set source and target point for the connecting edge
                LPort boxPort = getBoxPort(box);
                boxPort.getPosition().x = box.getSize().x / 2 - boxPort.getAnchor().x;
                boxPort.getPosition().y = box.getSize().y;
                LPort nodePort = box.getProperty(InternalProperties.COMMENT_CONN_PORT);
                if (nodePort.getDegree() == 1) {
                    nodePort.getPosition().x = anchorX - nodePort.getAnchor().x;
                    nodePort.getPosition().y = 0;
                    nodePort.setNode(node);
                }
                anchorX += anchorInc;
            }
        }

        if (bottomBoxes != null) {
            // determine the total width and maximal height of the bottom boxes
            double boxesWidth = spacing / 2 * (bottomBoxes.size() - 1);
            double maxHeight = 0;
            for (LNode box : bottomBoxes) {
                boxesWidth += box.getSize().x;
                maxHeight = Math.max(maxHeight, box.getSize().y);
            }
            // place the boxes in the bottom of the node, horizontally centered around the node itself
            double x = nodePos.x - (boxesWidth - nodeSize.x) / 2;
            double baseLine = nodePos.y + nodeSize.y + margin.bottom - maxHeight;
            double anchorInc = nodeSize.x / (bottomBoxes.size() + 1);
            double anchorX = anchorInc;
            for (LNode box : bottomBoxes) {
                box.getPosition().x = x;
                box.getPosition().y = baseLine;
                x += box.getSize().x + spacing / 2;
                // set source and target point for the connecting edge
                LPort boxPort = getBoxPort(box);
                boxPort.getPosition().x = box.getSize().x / 2 - boxPort.getAnchor().x;
                boxPort.getPosition().y = 0;
                LPort nodePort = box.getProperty(InternalProperties.COMMENT_CONN_PORT);
                if (nodePort.getDegree() == 1) {
                    nodePort.getPosition().x = anchorX - nodePort.getAnchor().x;
                    nodePort.getPosition().y = nodeSize.y;
                    nodePort.setNode(node);
                }
                anchorX += anchorInc;
            }
        }
    }
    
    /**
     * Retrieve the port of the given comment box that connects it with the
     * corresponding node. The box is expected to have exactly one connection, which
     * is checked by the {@link CommentPreprocessor}.
     * 
     * @param commentBox a comment box
     * @return the port that connects the box
     */
    private LPort getBoxPort(final LNode commentBox) {
        LPort nodePort = commentBox.getProperty(InternalProperties.COMMENT_CONN_PORT);
        for (LPort port : commentBox.getPorts()) {
            for (LEdge edge : port.getOutgoingEdges()) {
                // reconnect the edge (has been disconnected by pre-processor)
                edge.setTarget(nodePort);
                return port;
            }
            for (LEdge edge : port.getIncomingEdges()) {
                // reconnect the edge (has been disconnected by pre-processor)
                edge.setSource(nodePort);
                return port;
            }
        }
        return null;
    }

}
