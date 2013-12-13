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
package de.cau.cs.kieler.klay.force.graph;

import de.cau.cs.kieler.core.math.KVector;
import de.cau.cs.kieler.core.properties.MapPropertyHolder;

/**
 * A particle in the force graph, that is an object that can attract or repulse other particles.
 * 
 * @author owo
 * @author msp
 * @kieler.design proposed by msp
 * @kieler.rating proposed yellow by msp
 */
public abstract class FParticle extends MapPropertyHolder {
    
    /** the serial version UID. */
    private static final long serialVersionUID = -6264474302326798066L;
    
    /** Position of this particle. */
    private KVector position = new KVector();
    /** Width and height of graphical representation. */
    private KVector size = new KVector();

    /**
     * Returns the position vector of this particle.
     * 
     * @return the position vector
     */
    public final KVector getPosition() {
        return position;
    }

    /**
     * Returns the size of this particle.
     * 
     * @return the dimension vector
     */
    public final KVector getSize() {
        return size;
    }
    
    /**
     * Calculate radius for this particle.
     * 
     * @return radius of smallest circle surrounding shape of p
     */
    public double getRadius() {
        return size.getLength() / 2;
    }

}
