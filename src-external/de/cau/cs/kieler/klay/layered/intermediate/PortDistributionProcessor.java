/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 * 
 * Copyright 2014 by
 * + Christian-Albrechts-University of Kiel
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 * 
 * This code is provided under the terms of the Eclipse Public License (EPL).
 * See the file epl-v10.html for the license text.
 */
package de.cau.cs.kieler.klay.layered.intermediate;

import java.util.Random;

import de.cau.cs.kieler.core.alg.IKielerProgressMonitor;
import de.cau.cs.kieler.klay.layered.ILayoutProcessor;
import de.cau.cs.kieler.klay.layered.graph.LGraph;
import de.cau.cs.kieler.klay.layered.graph.LNode;
import de.cau.cs.kieler.klay.layered.graph.LPort;
import de.cau.cs.kieler.klay.layered.p3order.AbstractPortDistributor;
import de.cau.cs.kieler.klay.layered.p3order.LayerTotalPortDistributor;
import de.cau.cs.kieler.klay.layered.p3order.NodeRelativePortDistributor;
import de.cau.cs.kieler.klay.layered.properties.InternalProperties;

/**
 * Distributes the ports of nodes without fixed port order and raises the port constraints accordingly.
 * The port order depends on the connected ports in other layers. Also, the actual port distribution
 * mechanism used is determined randomly.
 * 
 * <dl>
 *   <dt>Preconditions:</dt>
 *     <dd>a layered graph.</dd>
 *   <dt>Postconditions:</dt>
 *     <dd>All nodes have port constraints set to at least {@code FIXED_ORDER}.</dd>
 *   <dt>Slots:</dt>
 *     <dd>Before phase 4.</dd>
 *   <dt>Same-slot dependencies:</dt>
 *     <dd>None.</dd>
 * </dl>
 * 
 * @see NodeRelativePortDistributor
 * @see LayerTotalPortDistributor
 * @author cds
 */
public class PortDistributionProcessor implements ILayoutProcessor {

    /**
     * {@inheritDoc}
     */
    public void process(final LGraph layeredGraph, final IKielerProgressMonitor progressMonitor) {
        progressMonitor.begin("Port distribution", 1);
        
        // The port distributor needs an array representation of the graph
        LNode[][] graphArray = layeredGraph.toNodeArray();
        
        // We need to setup port IDs
        int portCount = 0;
        for (LNode[] layer : graphArray) {
            for (LNode node : layer) {
                for (LPort port : node.getPorts()) {
                    port.id = portCount++;
                }
            }
        }
        
        // Randomly determine which port distributor implementation to use
        Random random = layeredGraph.getProperty(InternalProperties.RANDOM);
        AbstractPortDistributor portDistributor = random.nextBoolean()
                ? new NodeRelativePortDistributor(new float[portCount])
                : new LayerTotalPortDistributor(new float[portCount]);
        portDistributor.distributePorts(graphArray);
        
        progressMonitor.done();
    }

}
