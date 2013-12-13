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

import java.util.LinkedList;
import java.util.List;

import de.cau.cs.kieler.core.alg.IKielerProgressMonitor;
import de.cau.cs.kieler.klay.layered.ILayoutProcessor;
import de.cau.cs.kieler.klay.layered.graph.LEdge;
import de.cau.cs.kieler.klay.layered.graph.LNode;
import de.cau.cs.kieler.klay.layered.graph.LPort;
import de.cau.cs.kieler.klay.layered.graph.Layer;
import de.cau.cs.kieler.klay.layered.graph.LGraph;
import de.cau.cs.kieler.klay.layered.properties.EdgeType;
import de.cau.cs.kieler.klay.layered.properties.Properties;

/**
 * Removes dummy edges that were inserted while importing compound graphs to implement constraints
 * for the layering phase (keep dummy nodes representing subgraph borders left resp. right of inner
 * nodes).
 * 
 * <dl>
 *   <dt>Precondition:</dt>
 *     <dd>a layered graph.</dd>
 *   <dt>Postcondition:</dt>
 *     <dd>the graph does not contain compound dummy edges</dd>
 *   <dt>Slots:</dt>
 *     <dd>Before phase 3.</dd>
 *   <dt>Same-slot dependencies:</dt>
 *     <dd>None.</dd>
 * </dl>
 * 
 * @author ima
 * @kieler.design 2012-08-10 chsch grh
 */
public final class CompoundDummyEdgeRemover implements ILayoutProcessor {

    /**
     * {@inheritDoc}
     */
    public void process(final LGraph layeredGraph, final IKielerProgressMonitor monitor) {
        monitor.begin("Removing compound dummy edges", 1);

        List<Layer> layerList = layeredGraph.getLayers();

        LinkedList<LEdge> dummyEdgeList = new LinkedList<LEdge>();

        // Find dummy edges.
        for (Layer layer : layerList) {
            for (LNode lNode : layer) {
                for (LEdge lEdge : lNode.getOutgoingEdges()) {
                    if (lEdge.getProperty(Properties.EDGE_TYPE) == EdgeType.COMPOUND_DUMMY) {
                        dummyEdgeList.add(lEdge);
                    }
                }
            }
        }
        
        // Remove dummy edges.
        for (LEdge lEdge : dummyEdgeList) {
            lEdge.getSource().getOutgoingEdges().remove(lEdge);
            lEdge.getTarget().getIncomingEdges().remove(lEdge);
        }

        // remove unused ports
        for (Layer layer : layeredGraph) {
            for (LNode node : layer) {
                List<LPort> ports = node.getPorts();
                List<LPort> removables = new LinkedList<LPort>();
                for (LPort port : ports) {
                    if (port.getProperty(Properties.LEAVE_DUMMY_PORT)
                            && port.getIncomingEdges().isEmpty()
                            && port.getOutgoingEdges().isEmpty()) {
                        
                        removables.add(port);
                    }
                }
                for (int k = 0; k < removables.size(); k++) {
                    ports.remove(removables.get(k));
                }
            }
        }

        monitor.done();
    }

}
