/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 * 
 * Copyright 2015 by
 * + Christian-Albrechts-University of Kiel
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 * 
 * This code is provided under the terms of the Eclipse Public License (EPL).
 * See the file epl-v10.html for the license text.
 */
package de.cau.cs.kieler.klay.layered.intermediate.greedyswitch;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;

import de.cau.cs.kieler.kiml.options.PortSide;
import de.cau.cs.kieler.klay.layered.graph.LNode;
import de.cau.cs.kieler.klay.layered.graph.LPort;

/**
 * Utility class for iterating over ports in any direction.
 * 
 * @author alan
 */
final class PortIterable implements Iterable<LPort> {
    private final PortSide side;
    private final LNode node;
    private final PortOrder order;

    /**
     * Choose the order in which the ports are to be returned in using this enum.
     * 
     * @author alan
     *
     */
    private static enum PortOrder {
        CLOCKWISE, COUNTER_CLOCKWISE, NORTHSOUTH_EASTWEST
    }

    private PortIterable(final LNode node, final PortSide side, final PortOrder order) {
        this.node = node;
        this.side = side;
        this.order = order;
    }

    public static Iterable<LPort> inNorthSouthEastWestOrder(final LNode node, final PortSide side) {
        return new PortIterable(node, side, PortOrder.NORTHSOUTH_EASTWEST);
    }
    public static Iterable<LPort> inClockwiseOrder(final LNode node, final PortSide side) {
        return new PortIterable(node, side, PortOrder.CLOCKWISE);
    }
    public static Iterable<LPort> inCounterClockwiseOrder(final LNode node, final PortSide side) {
        return new PortIterable(node, side, PortOrder.COUNTER_CLOCKWISE);
    }
    
    public Iterator<LPort> iterator() {
        final List<LPort> ports = node.getPorts();
        switch (order) {
        case CLOCKWISE:
            return node.getPorts().iterator();
        case COUNTER_CLOCKWISE:
            return Iterators.filter(getCCWIterator(ports), getPredicate());
        case NORTHSOUTH_EASTWEST:
            switch (side) {
            case EAST:
            case NORTH:
                return Iterators.filter(ports.iterator(), getPredicate());
            case SOUTH:
            case WEST:
                return Iterators.filter(getCCWIterator(ports), getPredicate());
            }
        }
        throw new UnsupportedOperationException("PortOrder not implemented.");
    }

    private Predicate<LPort> getPredicate() {
        switch (side) {
        case NORTH:
            return LPort.NORTH_PREDICATE;
        case EAST:
            return LPort.EAST_PREDICATE;
        case SOUTH:
            return LPort.SOUTH_PREDICATE;
        case WEST:
            return LPort.WEST_PREDICATE;
        }
        throw new UnsupportedOperationException("Can't filter on undefined side");
    }

    private Iterator<LPort> getCCWIterator(final List<LPort> ports) {
        Iterator<LPort> iterator = new Iterator<LPort>() {
            private final ListIterator<LPort> listIterator = ports.listIterator(ports.size());

            public boolean hasNext() {
                return listIterator.hasPrevious();
            }

            public LPort next() {
                return listIterator.previous();
            }
            
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
        return iterator;
    }
}
