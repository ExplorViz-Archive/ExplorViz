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
package de.cau.cs.kieler.klay.layered.components;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import de.cau.cs.kieler.kiml.options.PortSide;
import de.cau.cs.kieler.klay.layered.graph.LGraph;
import de.cau.cs.kieler.klay.layered.properties.Properties;

/**
 * Represents a group of connected components grouped for layout purposes.
 * 
 * <p>A component group is conceptually divided into nine sectors as such: (the nine sectors are
 * enumerated in the {@link ComponentGroupSector} enumeration)</p>
 * <pre>
 *   +----+----+----+
 *   | nw | n  | ne |
 *   +----+----+----+
 *   | w  | c  | e  |
 *   +----+----+----+
 *   | sw | s  | se |
 *   +----+----+----+
 * </pre>
 * <p>The port sides of external ports a component connects to determines which sector(s) it will
 * occupy. This is best illustrated by some examples:</p>
 * <ul>
 *   <li>Let {@code c} be a component connected to a northern port. Then {@code c} would be placed in
 *       the {@code n} sector.</li>
 *   <li>Let {@code c} be a comopnent connected to a southern port and to an eastern port. Then
 *       {@code c} would be placed in the {@code se} sector.</li>
 *   <li>Let {@code c} be a component connected to no port at all. Then {@code c} would be placed in the
 *       {@code c} sector.</li>
 *   <li>Let {@code c} be a component connected to a western and to an eastern port. Then {@code c}
 *       would be placed in the {@code w}, {@code c}, and {@code e} sectors. If {@code c} would also
 *       connected to a southern port, it would also occupy the {@code sw}, {@code sc}, and {@code se}
 *       sectors.</li>
 * </ul>
 * <p>With this placement comes a bunch of constraints. For example, for a component to occupy the
 * top three sectors, none of them must be occupied by another component yet. If the addition of a
 * component to this group would cause a constraint to be violated, it cannot be added.</p>
 * 
 * <p>This class is not supposed to be public, but needs to be for JUnit tests to find it.</p>
 * 
 * @author cds
 * @kieler.design 2012-08-10 chsch grh
 * @kieler.rating proposed yellow by msp
 */
public final class ComponentGroup {
    
    ///////////////////////////////////////////////////////////////////////////////
    // Constants
    
    // External Port Connection Constants
    
    /** Constant for components with connections to no external port. */
    public static final Set<PortSide> CONN_C = EnumSet.noneOf(PortSide.class);
    /** Constant for components with connections to external port sides: west. */
    public static final Set<PortSide> CONN_W = EnumSet.of(PortSide.WEST);
    /** Constant for components with connections to external port sides: east. */
    public static final Set<PortSide> CONN_E = EnumSet.of(PortSide.EAST);
    /** Constant for components with connections to external port sides: north. */
    public static final Set<PortSide> CONN_N = EnumSet.of(PortSide.NORTH);
    /** Constant for components with connections to external port sides: south. */
    public static final Set<PortSide> CONN_S = EnumSet.of(PortSide.SOUTH);
    /** Constant for components with connections to external port sides: north, south. */
    public static final Set<PortSide> CONN_NS = EnumSet.of(PortSide.NORTH, PortSide.SOUTH);
    /** Constant for components with connections to external port sides: east, west. */
    public static final Set<PortSide> CONN_WE = EnumSet.of(PortSide.EAST, PortSide.WEST);
    /** Constant for components with connections to external port sides: north, west. */
    public static final Set<PortSide> CONN_NW = EnumSet.of(PortSide.NORTH, PortSide.WEST);
    /** Constant for components with connections to external port sides: north, east. */
    public static final Set<PortSide> CONN_NE = EnumSet.of(PortSide.NORTH, PortSide.EAST);
    /** Constant for components with connections to external port sides: south, west. */
    public static final Set<PortSide> CONN_SW = EnumSet.of(PortSide.SOUTH, PortSide.WEST);
    /** Constant for components with connections to external port sides: south, east. */
    public static final Set<PortSide> CONN_SE = EnumSet.of(PortSide.SOUTH, PortSide.EAST);
    /** Constant for components with connections to external port sides: north, west, east. */
    public static final Set<PortSide> CONN_NWE = EnumSet.of(
            PortSide.NORTH, PortSide.WEST, PortSide.EAST);
    /** Constant for components with connections to external port sides: south, west, east. */
    public static final Set<PortSide> CONN_SWE = EnumSet.of(
            PortSide.SOUTH, PortSide.WEST, PortSide.EAST);
    /** Constant for components with connections to external port sides: west, north, south. */
    public static final Set<PortSide> CONN_WNS = EnumSet.of(
            PortSide.WEST, PortSide.NORTH, PortSide.SOUTH);
    /** Constant for components with connections to external port sides: east, north, south. */
    public static final Set<PortSide> CONN_ENS = EnumSet.of(
            PortSide.EAST, PortSide.NORTH, PortSide.SOUTH);
    
    
    // External Port Connection Constraints
    
    /**
     * A map of constraints used to decide whether a component can be placed in this group.
     * 
     * <p>For a new component that is to be placed in this group, the set of external port sides
     * it connects to implies which sets of port sides of other components it is compatible to.
     * For instance, a component connecting to a northern and an eastern port requires that no
     * other component connects to this particular combination of ports. This map maps sets of
     * port sides to a list of port side sets that must not already exist in this group for a
     * component to be added.</p>
     */
    private static final Multimap<Set<PortSide>, Set<PortSide>> CONSTRAINTS = HashMultimap.create();
    
    static {
        // Setup constraints
        CONSTRAINTS.put(CONN_W, CONN_WNS);
        CONSTRAINTS.put(CONN_E, CONN_ENS);
        CONSTRAINTS.put(CONN_N, CONN_NWE);
        CONSTRAINTS.put(CONN_S, CONN_SWE);
        CONSTRAINTS.put(CONN_NS, CONN_WE);
        CONSTRAINTS.put(CONN_NS, CONN_NWE);
        CONSTRAINTS.put(CONN_NS, CONN_SWE);
        CONSTRAINTS.put(CONN_WE, CONN_NS);
        CONSTRAINTS.put(CONN_WE, CONN_WNS);
        CONSTRAINTS.put(CONN_WE, CONN_ENS);
        CONSTRAINTS.put(CONN_NW, CONN_NW);
        CONSTRAINTS.put(CONN_NW, CONN_NWE);
        CONSTRAINTS.put(CONN_NW, CONN_WNS);
        CONSTRAINTS.put(CONN_NE, CONN_NE);
        CONSTRAINTS.put(CONN_NE, CONN_NWE);
        CONSTRAINTS.put(CONN_NE, CONN_ENS);
        CONSTRAINTS.put(CONN_SW, CONN_SW);
        CONSTRAINTS.put(CONN_SW, CONN_SWE);
        CONSTRAINTS.put(CONN_SW, CONN_WNS);
        CONSTRAINTS.put(CONN_SE, CONN_SE);
        CONSTRAINTS.put(CONN_SE, CONN_SWE);
        CONSTRAINTS.put(CONN_SE, CONN_ENS);
        CONSTRAINTS.put(CONN_NWE, CONN_N);
        CONSTRAINTS.put(CONN_NWE, CONN_NS);
        CONSTRAINTS.put(CONN_NWE, CONN_NW);
        CONSTRAINTS.put(CONN_NWE, CONN_NE);
        CONSTRAINTS.put(CONN_NWE, CONN_NWE);
        CONSTRAINTS.put(CONN_NWE, CONN_WNS);
        CONSTRAINTS.put(CONN_NWE, CONN_ENS);
        CONSTRAINTS.put(CONN_SWE, CONN_S);
        CONSTRAINTS.put(CONN_SWE, CONN_NS);
        CONSTRAINTS.put(CONN_SWE, CONN_SW);
        CONSTRAINTS.put(CONN_SWE, CONN_SE);
        CONSTRAINTS.put(CONN_SWE, CONN_SWE);
        CONSTRAINTS.put(CONN_SWE, CONN_WNS);
        CONSTRAINTS.put(CONN_SWE, CONN_ENS);
        CONSTRAINTS.put(CONN_WNS, CONN_W);
        CONSTRAINTS.put(CONN_WNS, CONN_WE);
        CONSTRAINTS.put(CONN_WNS, CONN_NW);
        CONSTRAINTS.put(CONN_WNS, CONN_SW);
        CONSTRAINTS.put(CONN_WNS, CONN_NWE);
        CONSTRAINTS.put(CONN_WNS, CONN_SWE);
        CONSTRAINTS.put(CONN_WNS, CONN_WNS);
        CONSTRAINTS.put(CONN_ENS, CONN_E);
        CONSTRAINTS.put(CONN_ENS, CONN_WE);
        CONSTRAINTS.put(CONN_ENS, CONN_NE);
        CONSTRAINTS.put(CONN_ENS, CONN_SE);
        CONSTRAINTS.put(CONN_ENS, CONN_NWE);
        CONSTRAINTS.put(CONN_ENS, CONN_SWE);
        CONSTRAINTS.put(CONN_ENS, CONN_ENS);
    }
    
    
    ///////////////////////////////////////////////////////////////////////////////
    // Variables
    
    /**
     * A map mapping external port side combinations to components in this group.
     */
    private Multimap<Set<PortSide>, LGraph> components = HashMultimap.create();
    
    
    ///////////////////////////////////////////////////////////////////////////////
    // Constructors
    
    /**
     * Constructs a new, empty component group.
     */
    public ComponentGroup() {
        
    }
    
    /**
     * Constructs a new component group with the given initial component. This is equivalent to
     * constructing an empty component group and then calling {@link #add(LGraph)}.
     * 
     * @param component the component to be added to the group.
     */
    public ComponentGroup(final LGraph component) {
        add(component);
    }

    
    ///////////////////////////////////////////////////////////////////////////////
    // Component Management
    
    /**
     * Tries to add the given component to the group. Before adding the component, a call to
     * {@link #canAdd(LGraph)} determines if the component can actually be added to this
     * group.
     * 
     * @param component the component to be added to this group.
     * @return {@code true} if the component was successfully added, {@code false} otherwise.
     */
    public boolean add(final LGraph component) {
        if (canAdd(component)) {
            components.put(
                    component.getProperty(Properties.EXT_PORT_CONNECTIONS),
                    component);
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Checks whether this group has enough space left to add a given component.
     * 
     * @param component the component to be added to the group.
     * @return {@code true} if the group has enough space left to add the component, {@code false}
     *         otherwise.
     */
    private boolean canAdd(final LGraph component) {
        // Check if we have a component with incompatible external port sides
        Set<PortSide> candidateSides = component.getProperty(Properties.EXT_PORT_CONNECTIONS);
        Collection<Set<PortSide>> constraints = CONSTRAINTS.get(candidateSides);
        
        for (Set<PortSide> constraint : constraints) {
            if (!components.get(constraint).isEmpty()) {
                // A component with a conflicting external port side combination exists
                return false;
            }
        }
        
        // We haven't found any conflicting components
        return true;
    }
    
    /**
     * Returns all components in this component group.
     * 
     * @return the components in this component group.
     */
    public Collection<LGraph> getComponents() {
        return components.values();
    }
    
    /**
     * Returns the components in this component group connected to external ports on the given set
     * of port sides.
     * 
     * @param connections external port sides the returned components are to be connected to.
     * @return the collection of components. If there are no components, an empty collection is
     *         returned.
     */
    public Collection<LGraph> getComponents(final Set<PortSide> connections) {
        return components.get(connections);
    }
}
