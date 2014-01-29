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
package de.cau.cs.kieler.klay.layered.p1cycles;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import de.cau.cs.kieler.core.alg.IKielerProgressMonitor;
import de.cau.cs.kieler.klay.layered.ILayoutPhase;
import de.cau.cs.kieler.klay.layered.IntermediateProcessingConfiguration;
import de.cau.cs.kieler.klay.layered.graph.LEdge;
import de.cau.cs.kieler.klay.layered.graph.LNode;
import de.cau.cs.kieler.klay.layered.graph.LPort;
import de.cau.cs.kieler.klay.layered.graph.LGraph;
import de.cau.cs.kieler.klay.layered.intermediate.LayoutProcessorStrategy;
import de.cau.cs.kieler.klay.layered.properties.Properties;

/**
 * Cycle breaker implementation that uses a greedy algorithm. Inspired by
 * <ul>
 *   <li>Peter Eades, Xuemin Lin, W. F. Smyth,
 *     A fast and effective heuristic for the feedback arc set problem.
 *     <i>Information Processing Letters</i> 47(6), pp. 319-323, 1993.</li>
 *   <li>Giuseppe di Battista, Peter Eades, Roberto Tamassia, Ioannis G. Tollis,
 *     <i>Graph Drawing: Algorithms for the Visualization of Graphs</i>,
 *     Prentice Hall, New Jersey, 1999 (Section 9.4).</li>
 * </ul>
 * 
 * <p>This cycle breaker doesn't support layer constraints out of the box. If layer
 * constraints should be observed,
 * {@link de.cau.cs.kieler.klay.layered.intermediate.EdgeAndLayerConstraintEdgeReverser} and
 * {@link de.cau.cs.kieler.klay.layered.intermediate.LayerConstraintProcessor} should
 * be used.</p>
 * 
 * <dl>
 *   <dt>Precondition:</dt><dd>none</dd>
 *   <dt>Postcondition:</dt><dd>the graph has no cycles, but possibly
 *     new nodes and edges</dd>
 * </dl>
 * 
 * @see de.cau.cs.kieler.klay.layered.intermediate.EdgeAndLayerConstraintEdgeReverser
 * @see de.cau.cs.kieler.klay.layered.intermediate.LayerConstraintProcessor
 * @author msp
 * @kieler.design 2012-08-10 chsch grh
 * @kieler.rating yellow 2012-11-13 review KI-33 by grh, akoc
 */
public final class GreedyCycleBreaker implements ILayoutPhase {
    
    /** intermediate processing configuration. */
    private static final IntermediateProcessingConfiguration INTERMEDIATE_PROCESSING_CONFIGURATION =
        new IntermediateProcessingConfiguration(
                IntermediateProcessingConfiguration.AFTER_PHASE_5,
                EnumSet.of(LayoutProcessorStrategy.REVERSED_EDGE_RESTORER));

    /** indegree values for the nodes. */
    private int[] indeg;
    /** outdegree values for the nodes. */
    private int[] outdeg;
    /** mark for the nodes, inducing an ordering of the nodes. */
    private int[] mark;
    /** list of source nodes. */
    private final LinkedList<LNode> sources = new LinkedList<LNode>();
    /** list of sink nodes. */
    private final LinkedList<LNode> sinks = new LinkedList<LNode>();
    
    /**
     * {@inheritDoc}
     */
    public IntermediateProcessingConfiguration getIntermediateProcessingConfiguration(
            final LGraph graph) {
        
        return INTERMEDIATE_PROCESSING_CONFIGURATION;
    }

    /**
     * {@inheritDoc}
     */
    public void process(final LGraph layeredGraph, final IKielerProgressMonitor monitor) {
        monitor.begin("Greedy cycle removal", 1);
        
        Collection<LNode> nodes = layeredGraph.getLayerlessNodes();

        // initialize values for the algorithm (sum of priorities of incoming edges and outgoing
        // edges per node, and the ordering calculated for each node)
        int unprocessedNodeCount = nodes.size();
        indeg = new int[unprocessedNodeCount];
        outdeg = new int[unprocessedNodeCount];
        mark = new int[unprocessedNodeCount];
        
        int index = 0;
        for (LNode node : nodes) {
            // the node id is used as index for the indeg, outdeg, and mark arrays
            node.id = index;
            
            for (LPort port : node.getPorts()) {
                // calculate the sum of edge priorities
                for (LEdge edge : port.getIncomingEdges()) {
                    // ignore self-loops
                    if (edge.getSource().getNode() == node) {
                        continue;
                    }
                    
                    int priority = edge.getProperty(Properties.PRIORITY);
                    indeg[index] += priority > 0 ? priority + 1 : 1;
                }
                
                for (LEdge edge : port.getOutgoingEdges()) {
                    // ignore self-loops
                    if (edge.getTarget().getNode() == node) {
                        continue;
                    }
                    
                    int priority = edge.getProperty(Properties.PRIORITY);
                    outdeg[index] += priority > 0 ? priority + 1 : 1;
                }
            }
            
            // collect sources and sinks
            if (outdeg[index] == 0) {
                sinks.add(node);
            } else if (indeg[index] == 0) {
                sources.add(node);
            }
            index++;
        }
        
        // next rank values used for sinks and sources (from right and from left)
        int nextRight = -1, nextLeft = 1;

        // assign marks to all nodes
        List<LNode> maxNodes = new ArrayList<LNode>();
        Random random = layeredGraph.getProperty(Properties.RANDOM);
        
        while (unprocessedNodeCount > 0) {
            // sinks are put to the right --> assign negative rank, which is later shifted to positive
            while (!sinks.isEmpty()) {
                LNode sink = sinks.removeFirst();
                mark[sink.id] = nextRight--;
                updateNeighbors(sink);
                unprocessedNodeCount--;
            }
            
            // sources are put to the left --> assign positive rank
            while (!sources.isEmpty()) {
                LNode source = sources.removeFirst();
                mark[source.id] = nextLeft++;
                updateNeighbors(source);
                unprocessedNodeCount--;
            }
            
            // while there are unprocessed nodes left that are neither sinks nor sources...
            if (unprocessedNodeCount > 0) {
                int maxOutflow = Integer.MIN_VALUE;
                
                // find the set of unprocessed node (=> mark == 0), with the largest out flow
                for (LNode node : nodes) {
                    if (mark[node.id] == 0) {
                        int outflow = outdeg[node.id] - indeg[node.id];
                        if (outflow >= maxOutflow) {
                            if (outflow > maxOutflow) {
                                maxNodes.clear();
                                maxOutflow = outflow;
                            }
                            maxNodes.add(node);
                        }
                    }
                }
                assert maxOutflow > Integer.MIN_VALUE;
                
                // randomly select a node from the ones with maximal outflow and put it left
                LNode maxNode = maxNodes.get(random.nextInt(maxNodes.size()));
                mark[maxNode.id] = nextLeft++;
                updateNeighbors(maxNode);
                unprocessedNodeCount--;
            }
        }

        // shift negative ranks to positive; this applies to sinks of the graph
        int shiftBase = nodes.size() + 1;
        for (index = 0; index < nodes.size(); index++) {
            if (mark[index] < 0) {
                mark[index] += shiftBase;
            }
        }

        // reverse edges that point left
        for (LNode node : nodes) {
            LPort[] ports = node.getPorts().toArray(new LPort[node.getPorts().size()]);
            for (LPort port : ports) {
                LEdge[] outgoingEdges = port.getOutgoingEdges().toArray(
                        new LEdge[port.getOutgoingEdges().size()]);
                
                // look at the node's outgoing edges
                for (LEdge edge : outgoingEdges) {
                    int targetIx = edge.getTarget().getNode().id;
                    if (mark[node.id] > mark[targetIx]) {
                        edge.reverse(layeredGraph, true);
                        layeredGraph.setProperty(Properties.CYCLIC, true);
                    }
                }                
            }
        }

        dispose();
        monitor.done();
    }
    
    /**
     * Release all created resources so the GC can reap them.
     */
    private void dispose() {
        this.indeg = null;
        this.outdeg = null;
        this.mark = null;
        sources.clear();
        sinks.clear();
    }

    /**
     * Updates indegree and outdegree values of the neighbors of the given node,
     * simulating its removal from the graph. the sources and sinks lists are
     * also updated.
     * 
     * @param node node for which neighbors are updated
     */
    private void updateNeighbors(final LNode node) {
        for (LPort port : node.getPorts()) {
            for (LEdge edge : port.getConnectedEdges()) {
                LPort connectedPort = edge.getSource() == port ? edge.getTarget() : edge.getSource();
                LNode endpoint = connectedPort.getNode();
                
                // exclude self-loops
                if (node == endpoint) {
                    continue;
                }
                
                int priority = edge.getProperty(Properties.PRIORITY);
                if (priority < 0) {
                    priority = 0;
                }
                int index = endpoint.id;
                if (mark[index] == 0) {
                    if (edge.getTarget() == connectedPort) {
                        indeg[index] -= priority + 1;
                        if (indeg[index] <= 0 && outdeg[index] > 0) {
                            sources.add(endpoint);
                        }
                    } else {
                        outdeg[index] -= priority + 1;
                        if (outdeg[index] <= 0 && indeg[index] > 0) {
                            sinks.add(endpoint);
                        }
                    }
                }
            }
        }
    }
    
}
