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
package de.cau.cs.kieler.kiml;

/**
 * Interface for data holder classes that describe the layout infrastructure.
 * Implementing classes are used to store meta information and are accessed using
 * {@link LayoutDataService}.
 *
 * @kieler.design 2011-02-01 reviewed by cmot, soh
 * @kieler.rating yellow 2012-10-09 review KI-25 by chsch, bdu
 * @author msp
 */
public interface ILayoutData {
    
    /**
     * Returns the identifier, that is a unique string used to look up the layout data instance.
     * 
     *  @return the identifier
     */
    String getId();
    
    /**
     * Sets the identifier. This is usually done while reading data from the respective
     * extension point.
     *
     * @param id the identifier to set
     */
    void setId(String id);
    
    /**
     * Returns the name, that is a user-friendly title that can be displayed in user interfaces.
     * While the name should generally be short, longer text can be written in the description.
     * In contrast to the identifier, the name can be ambiguous.
     *
     * @return the name
     */
    String getName();
    
    /**
     * Sets the name. This is usually done while reading data from the respective
     * extension point.
     *
     * @param name the name to set
     */
    void setName(String name);
    
    /**
     * Returns the description for display in user interfaces.
     *
     * @return the description
     */
    String getDescription();
    
    /**
     * Sets the description. This is usually done while reading data from the respective
     * extension point.
     *
     * @param description the description to set
     */
    void setDescription(String description);

}
