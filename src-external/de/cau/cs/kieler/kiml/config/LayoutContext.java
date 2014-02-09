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
package de.cau.cs.kieler.kiml.config;

import java.util.Set;

import de.cau.cs.kieler.core.kgraph.KGraphElement;
import de.cau.cs.kieler.core.properties.IProperty;
import de.cau.cs.kieler.core.properties.MapPropertyHolder;
import de.cau.cs.kieler.core.properties.Property;
import de.cau.cs.kieler.kiml.LayoutOptionData;

/**
 * Context information for configuration of layout options. A layout context contains references
 * to a diagram (view model) element, its corresponding domain model element, its corresponding
 * graph element, and other information that is relevant for the layout of that element.
 * Contexts are used by {@link de.cau.cs.kieler.kiml.config.ILayoutConfig ILayoutConfigs}:
 * first a context is enriched with properties that hold required information, then layout options
 * can be queried either one at a time or all at once. The most important properties that should
 * be contained in a layout context are defined here.
 *
 * @author msp
 * @kieler.design proposed by msp
 * @kieler.rating yellow 2013-07-01 review KI-38 by cds, uru
 */
public class LayoutContext extends MapPropertyHolder {
    
    /**
     * Creates and returns a global context.
     * 
     * @return a global context
     */
    public static LayoutContext global() {
        LayoutContext globalContext = new LayoutContext();
        globalContext.setProperty(GLOBAL, true);
        return globalContext;
    }
    
    /** the serial version UID. */
    private static final long serialVersionUID = -7544617305602906672L;
    
    /** whether the context is global, that means it affects options for the whole layout process. */
    public static final IProperty<Boolean> GLOBAL = new Property<Boolean>("context.global", false);

    /** the graph element in the current context. */
    public static final IProperty<KGraphElement> GRAPH_ELEM = new Property<KGraphElement>(
            "context.graphElement");
    
    /** the main domain model element in the current context. */
    public static final IProperty<Object> DOMAIN_MODEL = new Property<Object>(
            "context.domainModelElement");
    
    /** the domain model element of the container of the current graph element. */
    public static final IProperty<Object> CONTAINER_DOMAIN_MODEL = new Property<Object>(
            "context.containerDomainModelElement");
    
    /** the main diagram part in the current context. */
    public static final IProperty<Object> DIAGRAM_PART = new Property<Object>(
            "context.diagramPart");
    
    /** the diagram part for the container of the current graph element. */
    public static final IProperty<Object> CONTAINER_DIAGRAM_PART = new Property<Object>(
            "context.containerDiagramPart");
    
    /** the types of targets for layout options: this determines whether the graph element in
     *  the current context is a node, edge, port, or label. */
    public static final IProperty<Set<LayoutOptionData.Target>> OPT_TARGETS
            = new Property<Set<LayoutOptionData.Target>>("context.optionTargets");

    /**
     * Create an empty layout context.
     */
    public LayoutContext() {
    }
    
    /**
     * Create a new layout context initialized with the content of an existing context.
     * 
     * @param other another layout context
     */
    public LayoutContext(final LayoutContext other) {
        this.copyProperties(other);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return super.getAllProperties().toString();
    }
    
}
