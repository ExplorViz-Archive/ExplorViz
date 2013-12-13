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

import org.eclipse.emf.ecore.EObject;

import de.cau.cs.kieler.core.properties.IProperty;
import de.cau.cs.kieler.kiml.LayoutContext;
import de.cau.cs.kieler.kiml.LayoutDataService;
import de.cau.cs.kieler.kiml.LayoutOptionData;
import de.cau.cs.kieler.kiml.klayoutdata.KLayoutData;
import de.cau.cs.kieler.kiml.options.LayoutOptions;

/**
 * An abstract layout configuration that is able to consider semantic model properties.
 *
 * @author msp
 * @kieler.design proposed by msp
 * @kieler.rating yellow 2013-07-01 review KI-38 by cds, uru
 */
public abstract class SemanticLayoutConfig implements IMutableLayoutConfig {
    
    /** the default priority for semantic layout configurations. */
    public static final int PRIORITY = 5;
    
    /**
     * {@inheritDoc}
     */
    public int getPriority() {
        return PRIORITY;
    }
    
    /**
     * Returns the options that are affected by this layout configuration.
     * 
     * @param semanticElem a semantic model element
     * @return the affected options, or {@code null} if there are none
     */
    protected abstract IProperty<?>[] getAffectedOptions(EObject semanticElem);
    
    /**
     * Determine the value of the given layout option from the semantic element.
     * 
     * @param semanticElem a semantic model element
     * @param layoutOption a layout option
     * @return the corresponding value, or {@code null} if no specific value is determined
     */
    protected abstract Object getSemanticValue(EObject semanticElem,
            LayoutOptionData<?> layoutOption);
    
    /**
     * Set a layout option value for the semantic element. This feature is optional, so
     * subclasses may leave the implementation empty.
     * 
     * @param semanticElem a semantic model element
     * @param layoutOption a layout option
     * @param value a value for the layout option, or {@code null} if the currently set
     *     value shall be deleted
     */
    protected abstract void setSemanticValue(EObject semanticElem,
            LayoutOptionData<?> layoutOption, Object value);

    /**
     * {@inheritDoc}
     */
    public void enrich(final LayoutContext context) {
        LayoutDataService layoutDataService = LayoutDataService.getInstance();
        LayoutOptionData<?> algorithmData = layoutDataService.getOptionData(
                LayoutOptions.ALGORITHM.getId());
        LayoutOptionData<?> diagTypeData = layoutDataService.getOptionData(
                LayoutOptions.DIAGRAM_TYPE.getId());
        
        EObject element = context.getProperty(LayoutContext.DOMAIN_MODEL);
        if (element != null) {
            // set layout algorithm hint for the content of the selected element
            if (context.getProperty(DefaultLayoutConfig.CONTENT_HINT) == null
                    && algorithmData != null) {
                String hint = (String) getSemanticValue(element, algorithmData);
                if (hint != null) {
                    context.setProperty(DefaultLayoutConfig.CONTENT_HINT, hint);
                }
            }
            // set diagram type for the content of the selected element
            if (context.getProperty(DefaultLayoutConfig.CONTENT_DIAGT) == null
                    && diagTypeData != null) {
                String diagType = (String) getSemanticValue(element, diagTypeData);
                if (diagType != null) {
                    context.setProperty(DefaultLayoutConfig.CONTENT_DIAGT, diagType);
                }
            }
        }
        
        EObject containerElem = context.getProperty(LayoutContext.CONTAINER_DOMAIN_MODEL);
        if (containerElem != null) {
            // set layout algorithm hint for the container of the selected element
            if (context.getProperty(DefaultLayoutConfig.CONTAINER_HINT) == null
                    && algorithmData != null) {
                String hint = (String) getSemanticValue(containerElem, algorithmData);
                if (hint != null) {
                    context.setProperty(DefaultLayoutConfig.CONTAINER_HINT, hint);
                }
            }
            // set diagram type for the container of the selected element
            if (context.getProperty(DefaultLayoutConfig.CONTAINER_DIAGT) == null
                    && diagTypeData != null) {
                String diagType = (String) getSemanticValue(containerElem, diagTypeData);
                if (diagType != null) {
                    context.setProperty(DefaultLayoutConfig.CONTAINER_DIAGT, diagType);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public final Object getValue(final LayoutOptionData<?> optionData, final LayoutContext context) {
        EObject element = context.getProperty(LayoutContext.DOMAIN_MODEL);
        if (element != null) {
            return getSemanticValue(element, optionData);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public final void transferValues(final KLayoutData graphData, final LayoutContext context) {
        EObject element = context.getProperty(LayoutContext.DOMAIN_MODEL);
        if (element != null) {
            LayoutDataService layoutDataService = LayoutDataService.getInstance();
            IProperty<?>[] affectedOptions = getAffectedOptions(element);
            if (affectedOptions != null) {
                for (IProperty<?> property : affectedOptions) {
                    LayoutOptionData<?> optionData;
                    if (property instanceof LayoutOptionData<?>) {
                        optionData = (LayoutOptionData<?>) property;
                    } else {
                        optionData = layoutDataService.getOptionData(property.getId());
                    }
                    
                    if (optionData != null) {
                        Object value = getSemanticValue(element, optionData);
                        graphData.setProperty(property, value);
                    }
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void clearValues(final LayoutContext context) {
        EObject element = context.getProperty(LayoutContext.DOMAIN_MODEL);
        if (element != null) {
            LayoutDataService layoutDataService = LayoutDataService.getInstance();
            IProperty<?>[] affectedOptions = getAffectedOptions(element);
            if (affectedOptions != null) {
                for (IProperty<?> property : affectedOptions) {
                    LayoutOptionData<?> optionData;
                    if (property instanceof LayoutOptionData<?>) {
                        optionData = (LayoutOptionData<?>) property;
                    } else {
                        optionData = layoutDataService.getOptionData(property.getId());
                    }
                    
                    if (optionData != null) {
                        setSemanticValue(element, optionData, null);
                    }
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public final void setValue(final LayoutOptionData<?> optionData, final LayoutContext context,
            final Object value) {
        EObject element = context.getProperty(LayoutContext.DOMAIN_MODEL);
        if (element != null) {
            setSemanticValue(element, optionData, value);
        }
    }

    /**
     * {@inheritDoc}
     */
    public final boolean isSet(final LayoutOptionData<?> optionData, final LayoutContext context) {
        EObject element = context.getProperty(LayoutContext.DOMAIN_MODEL);
        if (element != null) {
            return getSemanticValue(element, optionData) != null;
        }
        return false;
    }

}
