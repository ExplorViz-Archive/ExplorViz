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

import java.util.LinkedList;
import java.util.List;

/**
 * Data type used to store information for a layout type. A layout type defines the basic
 * graph drawing approach employed by a layout algorithm, for instance the layer-based
 * approach ({@link #TYPE_LAYERED}) or the force-based approach ({@link #TYPE_FORCE}).
 * The type of an algorithm can be left empty, in which case it is displayed as "Other"
 * in the user interface.
 *
 * @kieler.design 2011-02-01 reviewed by cmot, soh
 * @kieler.rating yellow 2012-10-09 review KI-25 by chsch, bdu
 * @author msp
 */
public class LayoutTypeData implements ILayoutData {
    
    /** default name for layout types for which no name is given. */
    public static final String DEFAULT_TYPE_NAME = "<Unnamed Type>";
    
    /** type identifier for layer based algorithms. */
    public static final String TYPE_LAYERED = "de.cau.cs.kieler.type.layered";
    /** type identifier for orthogonalization algorithms. */
    public static final String TYPE_ORTHOGONAL = "de.cau.cs.kieler.type.orthogonal";
    /** type identifier for force based algorithms. */
    public static final String TYPE_FORCE = "de.cau.cs.kieler.type.force";
    /** type identifier for circular algorithms. */
    public static final String TYPE_CIRCLE = "de.cau.cs.kieler.type.circle";
    /** type identifier for tree algorithms. */
    public static final String TYPE_TREE = "de.cau.cs.kieler.type.tree";

    
    /** identifier of the layout type. */
    private String id = "";
    /** user friendly name of the layout type. */
    private String name = "";
    /** detail description. */
    private String description = "";
    /** the list of layout algorithms that are registered for this type. */
    private final List<LayoutAlgorithmData> layouters = new LinkedList<LayoutAlgorithmData>();
    
    /**
     * {@inheritDoc}
     */
    public boolean equals(final Object obj) {
        if (obj instanceof LayoutTypeData) {
            return this.id.equals(((LayoutTypeData) obj).id);
        }
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return id.hashCode();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        if (name != null && name.length() > 0) {
            if (name.endsWith(">")) {
                return name;
            } else {
                return name + " Type";
            }
        } else {
            return "Other";
        }
    }
    
    /**
     * Returns the list of layout algorithms that are registered for this type.
     * 
     * @return the layouters
     */
    public List<LayoutAlgorithmData> getLayouters() {
        return layouters;
    }

    /**
     * Returns the layout type identifier.
     * 
     * @return the layout type identifier
     */
    public String getId() {
        return id;
    }
    
    /**
     * Sets the layout type identifier.
     * 
     * @param theid the identifier to set
     */
    public void setId(final String theid) {
        assert theid != null;
        this.id = theid;
    }
    
    /**
     * Returns the name of the layout type.
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets the name of the layout type.
     * 
     * @param thename the name to set
     */
    public void setName(final String thename) {
        if (thename == null || thename.length() == 0) {
            this.name = DEFAULT_TYPE_NAME;
        } else {
            this.name = thename;
        }
    }
    
    /**
     * Returns the description.
     * 
     * @return the description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Sets the description.
     * 
     * @param thedescription the description to set
     */
    public void setDescription(final String thedescription) {
        if (thedescription == null) {
            this.description = "";
        } else {
            this.description = thedescription;
        }
    }

}
