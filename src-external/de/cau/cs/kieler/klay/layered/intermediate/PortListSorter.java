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

import java.util.Collections;
import java.util.Comparator;

import de.cau.cs.kieler.core.alg.IKielerProgressMonitor;
import de.cau.cs.kieler.kiml.options.LayoutOptions;
import de.cau.cs.kieler.kiml.options.PortConstraints;
import de.cau.cs.kieler.klay.layered.ILayoutProcessor;
import de.cau.cs.kieler.klay.layered.graph.LNode;
import de.cau.cs.kieler.klay.layered.graph.LPort;
import de.cau.cs.kieler.klay.layered.graph.Layer;
import de.cau.cs.kieler.klay.layered.graph.LGraph;

/**
 * Sorts the port lists of nodes with fixed port orders or fixed port positions. 
 * The node's list of ports is sorted beginning at the leftmost northern port, going clockwise.
 * This order of ports may be used during crossing minimization for calculating port ranks.
 * 
 * In case of {@link PortConstraints#FIXED_ORDER FIXED_ORDER} the side and {@link LayoutOptions#PORT_INDEX PORT_INDEX}
 * are used if specified. Otherwise the order is inferred from specified port positions. 
 * For {@link PortConstraints#FIXED_POS FIXED_POS} solely the position of the ports determines the order.
 * 
 * <dl>
 *   <dt>Precondition:</dt><dd>a layered graph.</dd>
 *   <dt>Postcondition:</dt><dd>the port lists of nodes with fixed port orders are sorted.</dd>
 *   <dt>Slots:</dt><dd>Before phase 3. May additionally be used before phase 4 as well.</dd>
 *   <dt>Same-slot dependencies:</dt><dd>None.</dd>
 * </dl>
 * 
 * @see LNode#getPorts()
 * @author cds
 * @author uru
 * @kieler.design 2012-08-10 chsch grh
 * @kieler.rating proposed yellow by msp
 */
public final class PortListSorter implements ILayoutProcessor {
    
    /**
     * A comparer for ports. Ports are sorted by side (north, east, south, west) in
     * clockwise order, beginning at the top left corner.
     */
    public static class PortComparator implements Comparator<LPort> {

        /**
         * {@inheritDoc}
         */
        public int compare(final LPort port1, final LPort port2) {

            // for FIXED_ORDER try the ports' sides and indices first
            //  note that both ports are children of the same node
            if (port1.getNode().getProperty(LayoutOptions.PORT_CONSTRAINTS) == PortConstraints.FIXED_ORDER) {
                int ordinalDifference = port1.getSide().ordinal() - port2.getSide().ordinal();

                // Sort by side first
                if (ordinalDifference != 0) {
                    return ordinalDifference;
                }

                // In case of equal sides, sort by port index property
                Integer index1 = port1.getProperty(LayoutOptions.PORT_INDEX);
                Integer index2 = port2.getProperty(LayoutOptions.PORT_INDEX);
                if (index1 != null && index2 != null) {
                    int indexDifference = index1 - index2;
                    if (indexDifference != 0) {
                        return indexDifference;
                    }
                }
            }
            
            // In case of equal index (or FIXED_POS), sort by position
            switch (port1.getSide()) {
            case NORTH:
                // Compare x coordinates
                return Double.compare(port1.getPosition().x, port2.getPosition().x);
            
            case EAST:
                // Compare y coordinates
                return Double.compare(port1.getPosition().y, port2.getPosition().y);
            
            case SOUTH:
                // Compare x coordinates in reversed order
                return Double.compare(port2.getPosition().x, port1.getPosition().x);
            
            case WEST:
                // Compare y coordinates in reversed order
                return Double.compare(port2.getPosition().y, port1.getPosition().y);
                
            default:
                // Port sides should not be undefined
                throw new IllegalStateException("Port side is undefined");
            }
        }
        
    }
    

    /**
     * {@inheritDoc}
     */
    public void process(final LGraph layeredGraph, final IKielerProgressMonitor monitor) {
        monitor.begin("Port order processing", 1);
        PortComparator portComparator = new PortComparator();
        
        // Iterate through the nodes of all layers
        for (Layer layer : layeredGraph) {
            for (LNode node : layer) {
                if (node.getProperty(LayoutOptions.PORT_CONSTRAINTS).isOrderFixed()) {
                    // We need to sort the port list accordingly
                    Collections.sort(node.getPorts(), portComparator);
                }
            }
        }
        
        monitor.done();
    }

}
