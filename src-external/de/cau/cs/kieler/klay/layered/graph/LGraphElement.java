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
package de.cau.cs.kieler.klay.layered.graph;

import de.cau.cs.kieler.core.properties.MapPropertyHolder;

/**
 * Abstract superclass for the layers, nodes, ports, and edges of a layered graph
 * (and the graph itself).
 * 
 * <p>The hash code of each graph element is computed from a counter object {@link HashCodeCounter}.
 * Its {@code count} field is incremented each time a graph element is created. Provided that the
 * same counter object is used for all graph elements, the generated hash codes are unique.
 * The {@code count} field is not static, because that would cause problems when multiple graphs
 * are processed in parallel. The default hash code inherited from {@link Object} cannot be used,
 * because it is not deterministic, hence different hash codes would be generated in two consecutive
 * runs on the same graph. As a consequence, hash tables and hash sets would store their content
 * in different order, which can lead to different layouts in some cases. The deterministic hash
 * code implemented here guarantees that such effects will not occur.</p>
 *
 * @author msp
 * @kieler.design proposed by msp
 * @kieler.rating yellow 2013-03-22 review KI-35 by chsch, grh
 */
public abstract class LGraphElement extends MapPropertyHolder implements Comparable<LGraphElement> {

    /** the serial version UID. */
    private static final long serialVersionUID = 5480383439314459124L;
    
    // CHECKSTYLEOFF VisibilityModifier

    /** Identifier value, may be arbitrarily used by algorithms. */
    public int id;
    
    // CHECKSTYLEON VisibilityModifier
    
    /** the hash code for this graph element. */
    private final int hashCode;

    /**
     * Create a graph element with given hash code counter.
     * 
     * @param counter the counter used to find a unique but predictable hash code
     */
    public LGraphElement(final HashCodeCounter counter) {
        hashCode = ++counter.count;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean equals(final Object object) {
        if (object instanceof LGraphElement) {
            LGraphElement other = (LGraphElement) object;
            return this.hashCode == other.hashCode;
        }
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final int hashCode() {
        return hashCode;
    }
    
    /**
     * {@inheritDoc}
     */
    public final int compareTo(final LGraphElement other) {
        return this.hashCode - other.hashCode;
    }
    
    /**
     * A counter for hash codes. The same counter must be used for all elements created in
     * an algorithm run. This guarantees that the hash codes of all graph elements are unique,
     * but predictable independently of their object instance (see {@link LGraphElement}).
     */
    public static class HashCodeCounter {
        private int count = 0;
    }
    
}
