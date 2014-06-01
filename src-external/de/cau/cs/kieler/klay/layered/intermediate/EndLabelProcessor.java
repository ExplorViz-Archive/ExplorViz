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
package de.cau.cs.kieler.klay.layered.intermediate;

import java.util.HashMap;

import de.cau.cs.kieler.core.alg.IKielerProgressMonitor;
import de.cau.cs.kieler.core.math.KVector;
import de.cau.cs.kieler.kiml.options.EdgeLabelPlacement;
import de.cau.cs.kieler.kiml.options.LayoutOptions;
import de.cau.cs.kieler.klay.layered.ILayoutProcessor;
import de.cau.cs.kieler.klay.layered.graph.LEdge;
import de.cau.cs.kieler.klay.layered.graph.LLabel;
import de.cau.cs.kieler.klay.layered.graph.LLabel.LabelSide;
import de.cau.cs.kieler.klay.layered.graph.LInsets;
import de.cau.cs.kieler.klay.layered.graph.LNode;
import de.cau.cs.kieler.klay.layered.graph.LPort;
import de.cau.cs.kieler.klay.layered.graph.Layer;
import de.cau.cs.kieler.klay.layered.graph.LGraph;

/**
 * <p>This intermediate processor does the necessary calculations for an absolute positioning
 * of all end and port labels. It uses the port sides and the side choice made before to find
 * this positioning.</p>
 * 
 * <dl>
 *   <dt>Precondition:</dt>
 *     <dd>a layered graph</dd>
 *     <dd>no dummy nodes</dd>
 *     <dd>labels are marked with a placement side</dd>
 *     <dd>nodes have port sides</dd>
 *   <dt>Postcondition:</dt>
 *     <dd>edge and port labels have absolute coordinates.</dd>
 *   <dt>Slots:</dt>
 *     <dd>After phase 5.</dd>
 *   <dt>Same-slot dependencies:</dt><dd>{@link LongEdgeJoiner}</dd>
 *                                   <dd>{@link NorthSouthPortPostProcessor}</dd>
 *                                   <dd>{@link LabelDummyRemover}</dd>
 *                                   <dd>{@link ReverseEdgeRestorer}</dd>
 * </dl>
 * 
 * @author jjc
 * @kieler.rating proposed yellow cds
 */
public final class EndLabelProcessor implements ILayoutProcessor {

    /**
     * In case of northern ports, labels have to be stacked to avoid overlaps.
     * The necessary offset is stored here.
     */
    private HashMap<LNode, Double> northOffset; 
    
    /** The stacking offset for southern labels is stored here. */
    private HashMap<LNode, Double> southOffset;
    
    /**
     * Port labels have to be stacked on northern or southern ports as well if
     * placed outside. This offset is memorized here.
     */
    private HashMap<LPort, Double> portLabelOffsetHint;
    
    /**
     * {@inheritDoc}
     */
    public void process(final LGraph layeredGraph, final IKielerProgressMonitor monitor) {
        monitor.begin("End label placement", 1);
        
        // Initialize the offset maps
        northOffset = new HashMap<LNode, Double>();
        southOffset = new HashMap<LNode, Double>();
        portLabelOffsetHint = new HashMap<LPort, Double>();
        
        for (Layer layer : layeredGraph.getLayers()) {
            for (LNode node : layer.getNodes()) {
                for (LEdge edge : node.getOutgoingEdges()) {
                    for (LLabel label : edge.getLabels()) {
                        // Only consider end labels
                        if (label.getProperty(LayoutOptions.EDGE_LABEL_PLACEMENT)
                                == EdgeLabelPlacement.TAIL
                                ||
                            label.getProperty(LayoutOptions.EDGE_LABEL_PLACEMENT)
                                == EdgeLabelPlacement.HEAD) {
                            
                            placeEndLabel(node, edge, label);
                        }
                    }
                }
            }
        }
        
        monitor.done();
    }

    /**
     * Places the given end label of the given edge starting at the given node.
     * 
     * @param node source node of the edge.
     * @param edge the edge whose end label to place.
     * @param label the end label to place.
     */
    private void placeEndLabel(final LNode node, final LEdge edge, final LLabel label) {
        // Get the nearest port (source port for tail labels, target port for head labels)
        LPort port = null;
        
        if (label.getProperty(LayoutOptions.EDGE_LABEL_PLACEMENT) == EdgeLabelPlacement.TAIL) {
            port = edge.getSource();
        } else if (label.getProperty(LayoutOptions.EDGE_LABEL_PLACEMENT) == EdgeLabelPlacement.HEAD) {
            port = edge.getTarget();
        }
        
        // Initialize offset with zero if no offset was present
        if (!northOffset.containsKey(port.getNode())) {
            northOffset.put(port.getNode(), 0.0);
        }
        if (!southOffset.containsKey(port.getNode())) {
            southOffset.put(port.getNode(), 0.0);
        }
        if (!portLabelOffsetHint.containsKey(port)) {
            portLabelOffsetHint.put(port, 0.0);
        }
        
        // Calculate end label position based on side choice
        // Port side undefined can be left out, because there would be no reasonable
        // way of handling them
        if (label.getSide() == LabelSide.ABOVE) {
            placeEndLabelUpwards(node, label, port);
        } else {
            placeEndLabelDownwards(node, label, port);
        }
    }

    /**
     * Places the given end label above the edge.
     * 
     * @param node source node of the edge the label belongs to.
     * @param label the label to place.
     * @param port the end port of the edge the label is nearest to.
     */
    private void placeEndLabelDownwards(final LNode node, final LLabel label, final LPort port) {
        // Remember some stuff
        KVector labelPosition = label.getPosition();
        KVector absolutePortPosition = KVector.sum(port.getPosition(), port.getNode().getPosition());
        KVector absolutePortAnchor = port.getAbsoluteAnchor();
        LInsets portMargin = port.getMargin();
        
        // Actually calculate the coordinates
        switch (port.getSide()) {
        case WEST:
            labelPosition.x = Math.min(absolutePortPosition.x, absolutePortAnchor.x)
                              - portMargin.left
                              - label.getSize().x
                              - LabelDummyInserter.LABEL_SPACING;
            labelPosition.y = port.getAbsoluteAnchor().y
                              + LabelDummyInserter.LABEL_SPACING;
            break;
            
        case EAST:
            labelPosition.x = Math.max(absolutePortPosition.x + port.getSize().x, absolutePortAnchor.x)
                              + portMargin.right
                              + LabelDummyInserter.LABEL_SPACING;
            labelPosition.y = port.getAbsoluteAnchor().y
                              + LabelDummyInserter.LABEL_SPACING;
            break;
            
        case NORTH:
            labelPosition.x = port.getAbsoluteAnchor().x
                              + LabelDummyInserter.LABEL_SPACING;
            labelPosition.y = Math.min(absolutePortPosition.y, absolutePortAnchor.y)
                              - portMargin.top
                              - label.getSize().y
                              - LabelDummyInserter.LABEL_SPACING;
            break;
            
        case SOUTH:
            labelPosition.x = port.getAbsoluteAnchor().x
                              + LabelDummyInserter.LABEL_SPACING;
            labelPosition.y = Math.max(absolutePortPosition.y + port.getSize().y, absolutePortAnchor.y)
                              + portMargin.bottom
                              + LabelDummyInserter.LABEL_SPACING;
            break;
        }
    }

    /**
     * Places the given end label below the edge.
     * 
     * @param node source node of the edge the label belongs to.
     * @param label the label to place.
     * @param port the end port of the edge the label is nearest to.
     */
    private void placeEndLabelUpwards(final LNode node, final LLabel label, final LPort port) {
        // Remember some stuff
        KVector labelPosition = label.getPosition();
        KVector absolutePortPosition = KVector.sum(port.getPosition(), port.getNode().getPosition());
        KVector absolutePortAnchor = port.getAbsoluteAnchor();
        LInsets portMargin = port.getMargin();
        
        // Actually calculate the coordinates
        switch (port.getSide()) {
        case WEST:
            labelPosition.x = Math.min(absolutePortPosition.x, absolutePortAnchor.x)
                              - portMargin.left
                              - label.getSize().x
                              - LabelDummyInserter.LABEL_SPACING;
            labelPosition.y = port.getAbsoluteAnchor().y
                              - label.getSize().y
                              - LabelDummyInserter.LABEL_SPACING;
            break;
            
        case EAST:
            labelPosition.x = Math.max(absolutePortPosition.x + port.getSize().x, absolutePortAnchor.x)
                              + portMargin.right
                              + LabelDummyInserter.LABEL_SPACING;
            labelPosition.y = port.getAbsoluteAnchor().y
                              - label.getSize().y
                              - LabelDummyInserter.LABEL_SPACING;
            break;
            
        case NORTH:
            labelPosition.x = port.getAbsoluteAnchor().x
                              + LabelDummyInserter.LABEL_SPACING;
            labelPosition.y = Math.min(absolutePortPosition.y, absolutePortAnchor.y)
                              - portMargin.top
                              - label.getSize().y
                              - LabelDummyInserter.LABEL_SPACING;
            break;
            
        case SOUTH:
            labelPosition.x = port.getAbsoluteAnchor().x
                              + LabelDummyInserter.LABEL_SPACING;
            labelPosition.y = Math.max(absolutePortPosition.y + port.getSize().y, absolutePortAnchor.y)
                              + portMargin.bottom
                              + LabelDummyInserter.LABEL_SPACING;
            break;
        }
    }

}
