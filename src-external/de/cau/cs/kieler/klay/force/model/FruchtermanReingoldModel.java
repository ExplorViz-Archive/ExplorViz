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
package de.cau.cs.kieler.klay.force.model;

import de.cau.cs.kieler.core.math.KVector;
import de.cau.cs.kieler.klay.force.graph.FGraph;
import de.cau.cs.kieler.klay.force.graph.FNode;
import de.cau.cs.kieler.klay.force.graph.FParticle;
import de.cau.cs.kieler.klay.force.properties.Properties;

/**
 * A force model after the Fruchterman-Reingold approach.
 *
 * @author msp
 * @author tmn
 * @author fhol
 * @kieler.design proposed by msp
 * @kieler.rating proposed yellow by msp
 */
public class FruchtermanReingoldModel extends AbstractForceModel {

    /** factor that determines the C constant used for calculation of K. */
    private static final double SPACING_FACTOR = 0.01;
    /** factor used for repulsive force when the distance of two particles is zero. */
    private static final double ZERO_FACTOR = 100;
    
    /** the current temperature of the system. */
    private double temperature = Properties.TEMPERATURE.getDefault();
    /** the temperature threshold for stopping the model. */
    private double threshold;
    /** the main constant used for force calculations. */
    private double k;
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize(final FGraph graph) {
        super.initialize(graph);
        temperature = graph.getProperty(Properties.TEMPERATURE);
        threshold = temperature / graph.getProperty(Properties.ITERATIONS);
        
        // calculate an appropriate value for K
        int n = graph.getNodes().size();
        double totalWidth = 0;
        double totalHeight = 0;
        for (FNode v : graph.getNodes()) {
            totalWidth += v.getSize().x;
            totalHeight += v.getSize().y;
        }
        double area = totalWidth * totalHeight;
        double c = graph.getProperty(Properties.SPACING) * SPACING_FACTOR;
        k = Math.sqrt(area / (2 * n)) * c;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean moreIterations(final int count) {
        return temperature > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected KVector calcDisplacement(final FParticle forcer, final FParticle forcee) {
        avoidSamePosition(getRandom(), forcer, forcee);

        // compute distance (z in the original algorithm)
        KVector displacement = forcee.getPosition().differenceCreate(forcer.getPosition());
        double length = displacement.getLength();
        double d = Math.max(0, length - forcer.getRadius() - forcee.getRadius());
        
        // calculate repulsive force, independent of adjacency
        double force = repulsive(d, k) * forcer.getProperty(Properties.PRIORITY);
        
        // calculate attractive force, depending of adjacency
        int connection = getGraph().getConnection(forcer, forcee);
        if (connection > 0) {
            force -= attractive(d, k) * connection;
        }

        // scale distance vector to the amount of repulsive forces
        displacement.scale(force * temperature / length);

        return displacement;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void iterationDone() {
        super.iterationDone();
        temperature -= threshold;
    }
    
    /**
     * Calculates the amount of repulsive force along a distance.
     * 
     * @param d the distance over which the force exerts
     * @param k the space parameter, depending on the available area
     * @return the amount of the repulsive force
     */
    private static double repulsive(final double d, final double k) {
        if (d > 0) {
            return k * k / d;
        } else {
            return k * k * ZERO_FACTOR;
        }
    }
    
    /**
     * Calculates the amount of attracting force along a distance.
     * 
     * @param d the distance over which the force exerts
     * @param k the space-parameter, depending on the available area
     * @return the amount of the attracting force
     */
    public static double attractive(final double d, final double k) {
        return d * d / k;
    }

}
