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
package de.cau.cs.kieler.klay.layered;

/**
 * All layout phase enumerations implement this interface to act as a factory for their layout
 * phases. The actual type of the instance returned depends on the enumeration constant this method
 * is called on. (This is why the {@link #create()} method doesn't take any arguments.)
 * 
 * <p>Note that there is no similar interface to create {@link ILayoutProcessor} instances. This is
 * because intermediate processors are all listed in a single enumeration, while layout phases are
 * distributed over five enumerations.</p>
 * 
 * @author cds
 * @kieler.design proposed by cds
 * @kieler.rating proposed yellow by cds
 */
public interface ILayoutPhaseFactory {
    
    /**
     * Returns an implementation of {@link ILayoutPhase}. The actual implementation returned depends
     * on the actual type that implements this method.
     * 
     * @return layout phase.
     */
    ILayoutPhase create();
    
}
