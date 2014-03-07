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

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import de.cau.cs.kieler.core.alg.IKielerProgressMonitor;
import de.cau.cs.kieler.kiml.options.PortSide;
import de.cau.cs.kieler.klay.layered.ILayoutProcessor;
import de.cau.cs.kieler.klay.layered.graph.LEdge;
import de.cau.cs.kieler.klay.layered.graph.LGraph;
import de.cau.cs.kieler.klay.layered.graph.LLabel;
import de.cau.cs.kieler.klay.layered.graph.LLabel.LabelSide;
import de.cau.cs.kieler.klay.layered.graph.LNode;
import de.cau.cs.kieler.klay.layered.graph.LPort;
import de.cau.cs.kieler.klay.layered.graph.Layer;
import de.cau.cs.kieler.klay.layered.properties.EdgeLabelSideSelection;
import de.cau.cs.kieler.klay.layered.properties.NodeType;
import de.cau.cs.kieler.klay.layered.properties.Properties;

/**
 * <p>
 * This intermediate processor is used to select the side of port and edge labels. It is chosen
 * between the sides UP and DOWN based on different strategies selected by a layout option.
 * </p>
 * 
 * <dl>
 * <dt>Precondition:</dt>
 * <dd>a properly layered graph with fixed port orders.</dd>
 * <dt>Postcondition:</dt>
 * <dd>the placement side is chosen for each label, and each label is annotated accordingly.</dd>
 * <dt>Slots:</dt>
 * <dd>Before phase 4.</dd>
 * <dt>Same-slot dependencies:</dt>
 * <dd>{@link HyperedgeDummyMerger}</dd>
 * <dd>{@link InLayerConstraintProcessor}</dd>
 * <dd>{@link SubgraphOrderingProcessor}</dd>
 * </dl>
 * 
 * @author jjc
 */
public final class LabelSideSelector implements ILayoutProcessor {

    private static final LabelSide DEFAULT_LABEL_SIDE = LabelSide.BELOW;

    /**
     * {@inheritDoc}
     */
    public void process(final LGraph layeredGraph, final IKielerProgressMonitor monitor) {
        EdgeLabelSideSelection mode = layeredGraph.getProperty(Properties.EDGE_LABEL_SIDE);
        monitor.begin("Label side selection (" + mode + ")", 1);

        Iterable<LNode> nodes = Iterables.concat(layeredGraph);

        switch (mode) {
        case ALWAYS_UP:
            alwaysUp(nodes);
            break;
        case ALWAYS_DOWN:
            alwaysDown(nodes);
            break;
        case DIRECTION_UP:
            directionUp(nodes);
            break;
        case DIRECTION_DOWN:
            directionDown(nodes);
            break;
        case SMART:
            smart(nodes);
            break;
        }

        // iterate over all ports and check that a side is assigned
        // edge-less ports may not get a side
        for (Layer layer : layeredGraph.getLayers()) {
            for (LNode lNode : layer.getNodes()) {
                for (LPort port : lNode.getPorts()) {
                    for (LLabel label : port.getLabels()) {
                        if (label.getSide() == LabelSide.UNKNOWN) {
                            label.setSide(DEFAULT_LABEL_SIDE);
                        }
                    }
                }
            }
        }

        monitor.done();
    }

    /**
     * Strategy which marks all labels for an UP placement.
     * 
     * @param nodes
     *            All nodes of the graph
     */
    private void alwaysUp(final Iterable<LNode> nodes) {
        for (LNode node : nodes) {
            for (LEdge edge : node.getOutgoingEdges()) {
                for (LLabel label : edge.getLabels()) {
                    label.setSide(LabelSide.ABOVE);
                }
                for (LLabel portLabel : edge.getSource().getLabels()) {
                    portLabel.setSide(LabelSide.ABOVE);
                }
                for (LLabel portLabel : edge.getTarget().getLabels()) {
                    portLabel.setSide(LabelSide.ABOVE);
                }
            }
        }
    }

    /**
     * Strategy which marks all labels for an DOWN placement.
     * 
     * @param nodes
     *            All nodes of the graph
     */
    private void alwaysDown(final Iterable<LNode> nodes) {
        for (LNode node : nodes) {
            for (LEdge edge : node.getOutgoingEdges()) {
                for (LLabel label : edge.getLabels()) {
                    label.setSide(LabelSide.BELOW);
                }
                for (LLabel portLabel : edge.getSource().getLabels()) {
                    portLabel.setSide(LabelSide.BELOW);
                }
                for (LLabel portLabel : edge.getTarget().getLabels()) {
                    portLabel.setSide(LabelSide.BELOW);
                }
            }
        }
    }

    /**
     * Strategy that chooses a side depending on the direction of an edge, in this case UP
     * direction-wise.
     * 
     * @param nodes
     *            All nodes of the graph
     */
    private void directionUp(final Iterable<LNode> nodes) {
        for (LNode node : nodes) {
            for (LEdge edge : node.getOutgoingEdges()) {
                LabelSide side = LabelSide.ABOVE;
                LNode target = edge.getTarget().getNode();
                if (target.getProperty(Properties.NODE_TYPE) == NodeType.LONG_EDGE
                        || target.getProperty(Properties.NODE_TYPE) == NodeType.LABEL) {
                    target = target.getProperty(Properties.LONG_EDGE_TARGET).getNode();
                }
                if ((node.getLayer().getIndex() < target.getLayer().getIndex() && !edge
                        .getProperty(Properties.REVERSED))) {
                    side = LabelSide.ABOVE;
                } else {
                    side = LabelSide.BELOW;
                }
                for (LLabel label : edge.getLabels()) {
                    label.setSide(side);
                }
                for (LLabel portLabel : edge.getSource().getLabels()) {
                    portLabel.setSide(side);
                }
                for (LLabel portLabel : edge.getTarget().getLabels()) {
                    portLabel.setSide(side);
                }
            }
        }
    }

    /**
     * Strategy that chooses a side depending on the direction of an edge, in this case DOWN
     * direction-wise.
     * 
     * @param nodes
     *            All nodes of the graph
     */
    private void directionDown(final Iterable<LNode> nodes) {
        for (LNode node : nodes) {
            for (LEdge edge : node.getOutgoingEdges()) {
                LabelSide side = LabelSide.ABOVE;
                LNode target = edge.getTarget().getNode();
                if (target.getProperty(Properties.NODE_TYPE) == NodeType.LONG_EDGE
                        || target.getProperty(Properties.NODE_TYPE) == NodeType.LABEL) {
                    target = target.getProperty(Properties.LONG_EDGE_TARGET).getNode();
                }
                if ((node.getLayer().getIndex() < target.getLayer().getIndex() && !edge
                        .getProperty(Properties.REVERSED))) {
                    side = LabelSide.BELOW;
                } else {
                    side = LabelSide.ABOVE;
                }
                for (LLabel label : edge.getLabels()) {
                    label.setSide(side);
                }
                for (LLabel portLabel : edge.getSource().getLabels()) {
                    portLabel.setSide(side);
                }
                for (LLabel portLabel : edge.getTarget().getLabels()) {
                    portLabel.setSide(side);
                }
            }
        }
    }

    /**
     * Chooses label sides depending on certain patterns.
     * 
     * @param nodes
     *            All nodes of the graph
     */
    private void smart(final Iterable<LNode> nodes) {
        HashMap<LNode, LabelSide> nodeMarkers = Maps.newHashMap();
        for (LNode node : nodes) {
            List<LPort> eastPorts = getPortsBySide(node, PortSide.EAST);
            for (LPort eastPort : eastPorts) {
                for (LEdge edge : eastPort.getOutgoingEdges()) {
                    LabelSide chosenSide = LabelSide.ABOVE;
                    LNode targetNode = edge.getTarget().getNode();
                    if (targetNode.getProperty(Properties.NODE_TYPE) == NodeType.LONG_EDGE
                            || targetNode.getProperty(Properties.NODE_TYPE) == NodeType.LABEL) {
                        targetNode = targetNode.getProperty(Properties.LONG_EDGE_TARGET).getNode();
                    }
                    // Markers make sure that no overlaps will be created
                    if (nodeMarkers.containsKey(targetNode)) {
                        chosenSide = nodeMarkers.get(targetNode);
                    } else {
                        // Patterns are applied, the only current pattern is a node with
                        // two outgoing edges
                        if (eastPorts.size() == 2) {
                            if (eastPort == eastPorts.get(0)) {
                                chosenSide = LabelSide.ABOVE;
                            } else {
                                chosenSide = LabelSide.BELOW;
                            }
                        } else {
                            chosenSide = LabelSide.ABOVE;
                        }
                    }
                    nodeMarkers.put(targetNode, chosenSide);
                    for (LLabel label : edge.getLabels()) {
                        label.setSide(chosenSide);
                    }
                    for (LLabel portLabel : edge.getSource().getLabels()) {
                        portLabel.setSide(chosenSide);
                    }
                    for (LLabel portLabel : edge.getTarget().getLabels()) {
                        portLabel.setSide(chosenSide);
                    }
                }
            }
        }
    }

    /**
     * Get all ports on a certain side of a node. They are sorted descending by their position on
     * the node.
     * 
     * @param node
     *            The node to consider
     * @param portSide
     *            The chosen side
     * @return A list of all ports on the chosen side of the node
     */
    private List<LPort> getPortsBySide(final LNode node, final PortSide portSide) {
        List<LPort> result = new LinkedList<LPort>();
        for (LPort port : node.getPorts(portSide)) {
            result.add(port);
        }
        Collections.sort(result, new Comparator<LPort>() {
            public int compare(final LPort o1, final LPort o2) {
                if (o1.getPosition().y < o2.getPosition().y) {
                    return -1;
                } else if (o1.getPosition().y == o2.getPosition().y) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });
        return result;
    }

}
