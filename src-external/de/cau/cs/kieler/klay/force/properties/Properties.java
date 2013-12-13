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
package de.cau.cs.kieler.klay.force.properties;

import java.util.Random;

import de.cau.cs.kieler.core.math.KVector;
import de.cau.cs.kieler.core.properties.IProperty;
import de.cau.cs.kieler.core.properties.Property;
import de.cau.cs.kieler.kiml.options.LayoutOptions;
import de.cau.cs.kieler.klay.force.model.ForceModelStrategy;

/**
 * Property definitions for the force layouter.
 * 
 * @author msp
 * @kieler.design proposed by msp
 * @kieler.rating proposed yellow by msp
 */
public final class Properties {
    
    /** the original object from which a graph element was created. */
    public static final IProperty<Object> ORIGIN = new Property<Object>("origin");
    
    /** random number generator for the algorithm. */
    public static final IProperty<Random> RANDOM = new Property<Random>("random");
    
    /** upper left corner of the graph's bounding box. */
    public static final IProperty<KVector> BB_UPLEFT = new Property<KVector>("boundingBox.upLeft");
    /** lower right corner of the graph's bounding box. */
    public static final IProperty<KVector> BB_LOWRIGHT = new Property<KVector>("boundingBox.lowRight");
    
    ///////////////////////////////////////////////////////////////////////////////
    // USER INTERFACE OPTIONS

    /** force model property. */
    public static final Property<ForceModelStrategy> FORCE_MODEL = new Property<ForceModelStrategy>(
            "de.cau.cs.kieler.klay.force.model", ForceModelStrategy.FRUCHTERMAN_REINGOLD);
    
    /** minimal spacing between objects. */
    public static final Property<Float> SPACING = new Property<Float>(
            LayoutOptions.SPACING, 80.0f);
    
    /** the aspect ratio for packing connected components. */
    public static final Property<Float> ASPECT_RATIO = new Property<Float>(
            LayoutOptions.ASPECT_RATIO, 1.6f, 0.0f);

    /** priority of nodes or edges. */
    public static final Property<Integer> PRIORITY = new Property<Integer>(LayoutOptions.PRIORITY, 1);
    
    /** label spacing property. */
    public static final Property<Float> LABEL_SPACING = new Property<Float>(
            LayoutOptions.LABEL_SPACING, 5.0f, 0.0f);
    
    /** temperature property. */
    public static final Property<Float> TEMPERATURE = new Property<Float>(
            "de.cau.cs.kieler.klay.force.temperature", 0.001f, 0.0f);
    
    /** iterations property. */
    public static final Property<Integer> ITERATIONS = new Property<Integer>(
            "de.cau.cs.kieler.klay.force.iterations", 100, 1);
    
    /** edge repulsive power property. */
    public static final Property<Integer> EDGE_REP = new Property<Integer>(
            "de.cau.cs.kieler.klay.force.repulsivePower", 0, 0);
    
    /** repulsion factor property. */
    public static final Property<Float> REPULSION = new Property<Float>(
            "de.cau.cs.kieler.klay.force.repulsion", 5.0f, 0.0f);
    
    ///////////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    
    /**
     * Hidden default constructor.
     */
    private Properties() {
    }

}
