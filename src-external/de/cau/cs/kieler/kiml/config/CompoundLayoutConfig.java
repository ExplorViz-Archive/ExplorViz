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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Set;

import de.cau.cs.kieler.core.properties.IProperty;
import de.cau.cs.kieler.kiml.LayoutOptionData;

/**
 * A layout configurator that is composed of multiple other configurators.
 * This is used to handle a collection of layout configurators during layout option management.
 *
 * @author msp
 * @kieler.design proposed by msp
 * @kieler.rating yellow 2012-07-05 review KI-18 by cmot, sgu
 */
public class CompoundLayoutConfig implements IMutableLayoutConfig {
    
    /**
     * Create a compound layout configurator from the given configurators.
     * 
     * @param confs one or more configurators to include in the new compound configurator
     * @return a new compound configurator
     */
    public static CompoundLayoutConfig of(final ILayoutConfig... confs) {
        CompoundLayoutConfig instance = new CompoundLayoutConfig();
        for (ILayoutConfig conf : confs) {
            instance.add(conf);
        }
        return instance;
    }

    /** the contained layout configurations. */
    private final LinkedList<ILayoutConfig> configs = new LinkedList<ILayoutConfig>();
    
    /**
     * Create an empty compound layout configurator.
     */
    public CompoundLayoutConfig() {
    }
    
    /**
     * Create a compound layout configurator based on the given configurators.
     * 
     * @param confs a collection of configurators
     */
    public CompoundLayoutConfig(final Collection<ILayoutConfig> confs) {
        for (ILayoutConfig conf : confs) {
            add(conf);
        }
    }
    
    /**
     * Create a compound layout configurator with the same content as the given one.
     * 
     * @param compConf a compound configurator to copy
     */
    public CompoundLayoutConfig(final CompoundLayoutConfig compConf) {
        for (ILayoutConfig conf : compConf.configs) {
            add(conf);
        }
    }
    
    /**
     * Insert the given layout configuration into this compound configuration according
     * to its priority value.
     * 
     * @param conf a layout configuration
     */
    public void add(final ILayoutConfig conf) {
        ListIterator<ILayoutConfig> configIter = configs.listIterator();
        int prio = conf.getPriority();
        while (configIter.hasNext()) {
            ILayoutConfig nextConf = configIter.next();
            if (nextConf.getPriority() <= prio) {
                configIter.previous();
                break;
            }
        }
        configIter.add(conf);
    }
    
    /**
     * Insert all given layout configurations into this compound configuration.
     * 
     * @param confs a collection of layout configurations
     */
    public void addAll(final Collection<ILayoutConfig> confs) {
        for (ILayoutConfig conf : confs) {
            add(conf);
        }
    }
    
    /**
     * Remove the given layout configuration.
     * 
     * @param conf a layout configuration
     */
    public void remove(final ILayoutConfig conf) {
        configs.remove(conf);
    }
    
    /**
     * Remove all given layout configurations.
     * 
     * @param confs a collection of layout configurations
     */
    public void removeAll(final Collection<ILayoutConfig> confs) {
        configs.removeAll(confs);
    }
    
    /**
     * {@inheritDoc}
     */
    public int getPriority() {
        if (configs.isEmpty()) {
            return 0;
        }
        return configs.getFirst().getPriority();
    }

    /**
     * {@inheritDoc}
     */
    public void enrich(final LayoutContext context) {
        for (ILayoutConfig conf : configs) {
            conf.enrich(context);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Object getValue(final LayoutOptionData optionData, final LayoutContext context) {
        for (ILayoutConfig conf : configs) {
            Object value = conf.getValue(optionData, context);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Collection<IProperty<?>> getAffectedOptions(final LayoutContext context) {
        if (configs.size() == 1) {
            return configs.getFirst().getAffectedOptions(context);
        } else if (configs.size() > 1) {
            Set<IProperty<?>> collectedOptions = new HashSet<IProperty<?>>();
            for (ILayoutConfig conf : configs) {
                collectedOptions.addAll(conf.getAffectedOptions(context));
            }
            return collectedOptions;
        }
        return Collections.emptyList();
    }

    /**
     * {@inheritDoc}
     */
    public void clearValues(final LayoutContext context) {
        for (ILayoutConfig conf : configs) {
            if (conf instanceof IMutableLayoutConfig) {
                IMutableLayoutConfig mlc = (IMutableLayoutConfig) conf;
                mlc.clearValues(context);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setValue(final LayoutOptionData optionData, final LayoutContext context,
            final Object value) {
        for (ILayoutConfig conf : configs) {
            if (conf instanceof IMutableLayoutConfig) {
                IMutableLayoutConfig mlc = (IMutableLayoutConfig) conf;
                mlc.setValue(optionData, context, value);
                if (value != null && mlc.isSet(optionData, context)) {
                    // the value has been set successfully on the configurator with highest priority
                    return;
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isSet(final LayoutOptionData optionData, final LayoutContext context) {
        for (ILayoutConfig conf : configs) {
            if (conf instanceof IMutableLayoutConfig) {
                IMutableLayoutConfig mlc = (IMutableLayoutConfig) conf;
                if (mlc.isSet(optionData, context)) {
                    return true;
                }
            }
        }
        return false;
    }

}
