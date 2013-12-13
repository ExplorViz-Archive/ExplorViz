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
package de.cau.cs.kieler.kiml.config;

import de.cau.cs.kieler.kiml.LayoutContext;
import de.cau.cs.kieler.kiml.LayoutOptionData;
import de.cau.cs.kieler.kiml.klayoutdata.KLayoutData;

/**
 * Layout option configurator interface. Implementations are used to determine the
 * <em>abstract layout</em>, which consists of a mapping of layout options to specific values for
 * each graph element. The available layout configurators are managed by
 * {@link de.cau.cs.kieler.kiml.ui.service.LayoutOptionManager}. There the available configurators
 * are first used to <em>enrich</em> the context of a graph element with required information,
 * then the actual layout option values are transferred to the graph element data holder.
 *
 * @author msp
 * @kieler.design proposed by msp
 * @kieler.rating yellow 2012-07-05 review KI-18 by cmot, sgu
 */
public interface ILayoutConfig {
    
    /**
     * Return the priority of this layout configurator, which is relevant when multiple configurators
     * are applied. A greater number means higher priority.
     * 
     * @return the priority
     */
    int getPriority();
    
    /**
     * Enrich the given context with additional information that can be derived from what is already
     * contained. This information can be specific to the configurator or it can be reused by other
     * configurators to find out more about the current context. This method should be called once to
     * prepare a context before any values are queried. The configurator can use the context
     * object as a cache to store results of more elaborate computations.
     * 
     * @param context a context for layout configuration
     */
    void enrich(LayoutContext context);
    
    /**
     * Get the current value for a layout option in the given context.
     * 
     * @param optionData a layout option descriptor
     * @param context a context for layout configuration
     * @return the layout option value, or {@code null} if the option has no value in this context
     */
    Object getValue(LayoutOptionData<?> optionData, LayoutContext context);
    
    /**
     * Transfer all non-default layout option values that are managed by this layout configurator
     * to the given layout data instance.
     * 
     * @param layoutData a layout data instance that can hold layout options
     * @param context a context for layout configuration
     */
    void transferValues(KLayoutData layoutData, LayoutContext context);

}
