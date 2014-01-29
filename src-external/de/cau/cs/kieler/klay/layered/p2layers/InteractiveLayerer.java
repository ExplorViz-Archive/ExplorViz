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
package de.cau.cs.kieler.klay.layered.p2layers;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import de.cau.cs.kieler.core.alg.IKielerProgressMonitor;
import de.cau.cs.kieler.klay.layered.ILayoutPhase;
import de.cau.cs.kieler.klay.layered.IntermediateProcessingConfiguration;
import de.cau.cs.kieler.klay.layered.graph.LEdge;
import de.cau.cs.kieler.klay.layered.graph.LNode;
import de.cau.cs.kieler.klay.layered.graph.LPort;
import de.cau.cs.kieler.klay.layered.graph.Layer;
import de.cau.cs.kieler.klay.layered.graph.LGraph;
import de.cau.cs.kieler.klay.layered.properties.PortType;

/**
 * A node layerer that allows user interaction by respecting previous node positions.
 * These positions could be contrary to edge directions, so the resulting layering must
 * be checked for consistency.
 * 
 * <dl>
 *   <dt>Precondition:</dt><dd>the graph has no cycles</dd>
 *   <dt>Postcondition:</dt><dd>all nodes have been assigned a layer such that
 *     edges connect only nodes from layers with increasing indices</dd>
 * </dl>
 *
 * @author msp
 * @kieler.design 2012-08-10 chsch grh
 * @kieler.rating yellow 2012-11-13 review KI-33 by grh, akoc
 */
public final class InteractiveLayerer implements ILayoutPhase {

    /**
     * {@inheritDoc}
     */
    public IntermediateProcessingConfiguration getIntermediateProcessingConfiguration(
            final LGraph graph) {
        
        return null;
    }
    
    /** Utility class for marking horizontal regions that are already covered by some nodes. */
    private static class LayerSpan {
        private double start;
        private double end;
        private List<LNode> nodes = new LinkedList<LNode>();
    }

    /**
     * {@inheritDoc}
     */
    public void process(final LGraph layeredGraph, final IKielerProgressMonitor monitor) {
        monitor.begin("Interactive node layering", 1);

        // create layers with a start and an end position, merging when they overlap with others
        LinkedList<LayerSpan> currentSpans = new LinkedList<LayerSpan>();
        for (LNode node : layeredGraph.getLayerlessNodes()) {
            double minx = node.getPosition().x;
            double maxx = minx + node.getSize().x;
            // look for a position in the sorted list where the node can be inserted
            ListIterator<LayerSpan> spanIter = currentSpans.listIterator();
            LayerSpan foundSpan = null;
            while (spanIter.hasNext()) {
                LayerSpan span = spanIter.next();
                if (span.start >= maxx) {
                    // the next layer span is further right, so insert the node here
                    spanIter.previous();
                    break;
                } else if (span.end > minx) {
                    // the layer span has an intersection with the node
                    if (foundSpan == null) {
                        // add the node to the current layer span
                        span.nodes.add(node);
                        span.start = Math.min(span.start, minx);
                        span.end = Math.max(span.end, maxx);
                        foundSpan = span;
                    } else {
                        // merge the previously found layer span with the current one
                        foundSpan.nodes.addAll(span.nodes);
                        foundSpan.end = Math.max(foundSpan.end, span.end);
                        spanIter.remove();
                    }
                }
            }
            if (foundSpan == null) {
                // no intersecting span was found, so create a new one
                foundSpan = new LayerSpan();
                foundSpan.start = minx;
                foundSpan.end = maxx;
                spanIter.add(foundSpan);
                foundSpan.nodes.add(node);
            }
        }
        
        // create real layers from the layer spans
        List<Layer> layers = layeredGraph.getLayers();
        int nextIndex = 0;
        for (LayerSpan span : currentSpans) {
            Layer layer = new Layer(layeredGraph);
            layer.id = nextIndex++;
            layers.add(layer);
            for (LNode node : span.nodes) {
                node.setLayer(layer);
                node.id = 0;
            }
        }
        
        // correct the layering respecting the graph topology, so edges point from left to right
        for (LNode node : layeredGraph.getLayerlessNodes()) {
            if (node.id == 0) {
                checkNode(node, layeredGraph);
            }
        }
        
        // remove empty layers, which can happen when the layering has to be corrected
        while (layers.get(0).getNodes().isEmpty()) {
            layers.remove(0);
        }
        
        // clear the list of nodes that have no layer, since now they all have one
        layeredGraph.getLayerlessNodes().clear();
        monitor.done();
    }
    
    /**
     * Check the layering of the given node by comparing the layer index of all successors.
     * 
     * @param node1 a node
     * @param graph the layered graph
     */
    private void checkNode(final LNode node1, final LGraph graph) {
        node1.id = 1;
        Layer layer1 = node1.getLayer();
        for (LPort port : node1.getPorts(PortType.OUTPUT)) {
            for (LEdge edge : port.getOutgoingEdges()) {
                LNode node2 = edge.getTarget().getNode();
                if (node1 != node2) {
                    Layer layer2 = node2.getLayer();
                    if (layer2.id <= layer1.id) {
                        // a violation was detected - move the target node to the next layer
                        int newIndex = layer1.id + 1;
                        if (newIndex == graph.getLayers().size()) {
                            Layer newLayer = new Layer(graph);
                            newLayer.id = newIndex;
                            graph.getLayers().add(newLayer);
                            node2.setLayer(newLayer);
                        } else {
                            Layer newLayer = graph.getLayers().get(newIndex);
                            node2.setLayer(newLayer);
                        }
                        checkNode(node2, graph);
                    }
                }
            }
        }
    }

}
