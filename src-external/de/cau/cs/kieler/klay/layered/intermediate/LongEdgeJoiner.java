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

import java.util.List;
import java.util.ListIterator;

import de.cau.cs.kieler.core.alg.IKielerProgressMonitor;
import de.cau.cs.kieler.core.math.KVector;
import de.cau.cs.kieler.core.math.KVectorChain;
import de.cau.cs.kieler.kiml.options.LayoutOptions;
import de.cau.cs.kieler.kiml.options.PortSide;
import de.cau.cs.kieler.klay.layered.ILayoutProcessor;
import de.cau.cs.kieler.klay.layered.graph.LEdge;
import de.cau.cs.kieler.klay.layered.graph.LGraph;
import de.cau.cs.kieler.klay.layered.graph.LLabel;
import de.cau.cs.kieler.klay.layered.graph.LNode;
import de.cau.cs.kieler.klay.layered.graph.LNode.NodeType;
import de.cau.cs.kieler.klay.layered.graph.Layer;
import de.cau.cs.kieler.klay.layered.properties.Properties;

/**
 * Removes dummy nodes due to edge splitting (dummy nodes that have the node type
 * {@link de.cau.cs.kieler.klay.layered.properties.NodeType#LONG_EDGE}). If an edge is split into a
 * chain of edges <i>e1, e2, ..., ek</i>, the first edge <i>e1</i> is retained, while the other
 * edges <i>e2, ..., ek</i> are discarded. This fact should be respected by all processors that
 * create dummy nodes: they should always put the original edge as first edge in the chain of edges,
 * so the original edge is restored.
 * 
 * <p>
 * The actual implementation that joins long edges is provided by this class as a public utility method
 * to be used by other processors.
 * </p>
 * 
 * <dl>
 *   <dt>Preconditions:</dt>
 *     <dd>a layered graph</dd>
 *     <dd>nodes are placed</dd>
 *     <dd>edges are routed.</dd>
 *   <dt>Postconditions:</dt>
 *     <dd>there are no dummy nodes of type
 *     {@link de.cau.cs.kieler.klay.layered.properties.NodeType#LONG_EDGE}.</dd>
 *   <dt>Slots:</dt>
 *     <dd>After phase 5.</dd>
 *   <dt>Same-slot dependencies:</dt>
 *     <dd>{@link HierarchicalPortOrthogonalEdgeRouter}</dd>
 * </dl>
 *
 * @author cds
 * @kieler.design 2012-08-10 chsch grh
 * @kieler.rating proposed yellow by msp
 */
public final class LongEdgeJoiner implements ILayoutProcessor {

    /**
     * {@inheritDoc}
     */
    public void process(final LGraph layeredGraph, final IKielerProgressMonitor monitor) {
        monitor.begin("Edge joining", 1);
        
        final boolean addUnnecessaryBendpoints =
                layeredGraph.getProperty(Properties.ADD_UNNECESSARY_BENDPOINTS);
        
        // Iterate through the layers
        for (Layer layer : layeredGraph) {
            // Get a list iterator for the layer's nodes (since we might be
            // removing dummy nodes from it)
            ListIterator<LNode> nodeIterator = layer.getNodes().listIterator();
            
            while (nodeIterator.hasNext()) {
                LNode node = nodeIterator.next();
                
                // Check if it's a dummy edge we're looking for
                if (node.getNodeType() == NodeType.LONG_EDGE) {
                    joinAt(node, addUnnecessaryBendpoints);
                    
                    // Remove the node
                    nodeIterator.remove();
                }
            }
        }
        
        monitor.done();
    }

    /**
     * Joins the edges connected to the given dummy node. The dummy node is then ready to be removed
     * from the graph.
     * 
     * @param longEdgeDummy
     *            the dummy node whose incident edges to join.
     * @param addUnnecessaryBendpoints
     *            {@code true} if a bend point should be added to the edges at the position of the
     *            dummy node.
     */
    public static void joinAt(final LNode longEdgeDummy, final boolean addUnnecessaryBendpoints) {
        // Get the input and output port (of which we assume to have only one, on the western side and
        // on the eastern side, respectively); the incoming edges are retained, and the outgoing edges
        // are discarded
        List<LEdge> inputPortEdges =
            longEdgeDummy.getPorts(PortSide.WEST).iterator().next().getIncomingEdges();
        List<LEdge> outputPortEdges =
            longEdgeDummy.getPorts(PortSide.EAST).iterator().next().getOutgoingEdges();
        int edgeCount = inputPortEdges.size();
        
        // If we are to add unnecessary bend points, we need to know where. We take the position of the
        // first port we find. (It doesn't really matter which port we're using, so we opt to keep it
        // surprisingly simple.)
        KVector unnecessaryBendpoint = longEdgeDummy.getPorts().get(0).getAbsoluteAnchor();
        
        // The following code assumes that edges with the same indices in the two lists originate from
        // the same long edge, which is true for the current implementation of LongEdgeSplitter and
        // HyperedgeDummyMerger
        while (edgeCount-- > 0) {
            // Get the two edges
            LEdge survivingEdge = inputPortEdges.get(0);
            LEdge droppedEdge = outputPortEdges.get(0);
            
            // Do some edgy stuff
            survivingEdge.setTarget(droppedEdge.getTarget());
            droppedEdge.setSource(null);
            droppedEdge.setTarget(null);
            
            // Join their bend points and add possibly an unnecessary one
            KVectorChain survivingBendPoints = survivingEdge.getBendPoints();
            
            if (addUnnecessaryBendpoints) {
                survivingBendPoints.add(new KVector(unnecessaryBendpoint));
            }
            
            for (KVector bendPoint : droppedEdge.getBendPoints()) {
                survivingBendPoints.add(new KVector(bendPoint));
            }
            
            // Join their labels
            List<LLabel> survivingLabels = survivingEdge.getLabels();
            for (LLabel label: droppedEdge.getLabels()) {
                survivingLabels.add(label);
            }
            
            // Join their junction points
            KVectorChain survivingJunctionPoints = survivingEdge.getProperty(
                    LayoutOptions.JUNCTION_POINTS);
            KVectorChain droppedJunctionsPoints = droppedEdge.getProperty(
                    LayoutOptions.JUNCTION_POINTS);
            if (droppedJunctionsPoints != null) {
                if (survivingJunctionPoints == null) {
                    survivingJunctionPoints = new KVectorChain();
                    survivingEdge.setProperty(LayoutOptions.JUNCTION_POINTS,
                            survivingJunctionPoints);
                }
                for (KVector jp : droppedJunctionsPoints) {
                    survivingJunctionPoints.add(new KVector(jp));
                }
            }
        }
    }

}
