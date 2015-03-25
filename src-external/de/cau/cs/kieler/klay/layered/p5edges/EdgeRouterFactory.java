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
package de.cau.cs.kieler.klay.layered.p5edges;

import de.cau.cs.kieler.kiml.options.EdgeRouting;
import de.cau.cs.kieler.klay.layered.ILayoutPhase;
import de.cau.cs.kieler.klay.layered.ILayoutPhaseFactory;
import de.cau.cs.kieler.klay.layered.p5edges.splines.SplineEdgeRouter;

/**
 * Factory for edge routers. This factory is necessary since the {@link EdgeRouting} enumeration is
 * defined outside of KLay Layered and can thus not be made into a factory.
 * 
 * @author cds
 * @kieler.design proposed by cds
 * @kieler.rating proposed yellow by cds
 */
public final class EdgeRouterFactory implements ILayoutPhaseFactory {
    
    /** the edge routing this factory uses to decide which implementation to return. */
    private EdgeRouting edgeRoutingStrategy;
    
    
    /**
     * Creates a new factory for the given edge routing strategy. The strategy decides which edge router
     * implementation the factory returns.
     * 
     * @param edgeRoutingStrategy the edge routing strategy.
     * @return the edge router factory.
     */
    public static EdgeRouterFactory factoryFor(final EdgeRouting edgeRoutingStrategy) {
        EdgeRouterFactory factory = new EdgeRouterFactory();
        factory.edgeRoutingStrategy = edgeRoutingStrategy;
        return factory;
    }
    
    /**
     * {@inheritDoc}
     */
    public ILayoutPhase create() {
        switch (edgeRoutingStrategy) {
        case POLYLINE:
            return new PolylineEdgeRouter();
            
        case SPLINES:
            return new SplineEdgeRouter();
            
        default:
            return new OrthogonalEdgeRouter();
        }
    }

}
