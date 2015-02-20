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
package de.cau.cs.kieler.klay.layered.intermediate;

import de.cau.cs.kieler.core.alg.IKielerProgressMonitor;
import de.cau.cs.kieler.kiml.util.nodespacing.KimlNodeDimensionCalculation;
import de.cau.cs.kieler.klay.layered.ILayoutProcessor;
import de.cau.cs.kieler.klay.layered.graph.LGraph;
import de.cau.cs.kieler.klay.layered.graph.LGraphAdapters;

/**
 * Calculates node sizes, places ports, and places node and port labels.
 * 
 * <p><i>Note:</i> Regarding port placement, this processor now does what the old
 * {@code PortPositionProcessor} did and thus replaces it.</p>
 * 
 * <dl>
 *   <dt>Precondition:</dt>
 *     <dd>The graph is layered.</dd>
 *     <dd>Crossing minimization is finished.</dd>
 *     <dd>Port constraints are at least at {@code FIXED_ORDER}.</dd>
 *     <dd>Port lists are properly sorted going clockwise, starting at the leftmost northern port.</dd>
 *   <dt>Postcondition:</dt>
 *     <dd>Port positions are fixed.</dd>
 *     <dd>Port labels are placed.</dd>
 *     <dd>Node labels are placed.</dd>
 *     <dd>Node sizes are set.</dd>
 *   <dt>Slots:</dt>
 *     <dd>Before phase 4.</dd>
 *   <dt>Same-slot dependencies:</dt>
 *     <dd>{@link LabelSideSelector}</dd>
 * </dl>
 * 
 * @see LabelSideSelector
 * @author cds
 */
public final class LabelAndNodeSizeProcessor implements ILayoutProcessor {
    
    /**
     * {@inheritDoc}
     */
    public void process(final LGraph layeredGraph, final IKielerProgressMonitor monitor) {
        monitor.begin("Node and Port Label Placement and Node Sizing", 1);
        
        KimlNodeDimensionCalculation.calculateLabelAndNodeSizes(LGraphAdapters.adapt(layeredGraph));
        
        monitor.done();
    }
    
}
