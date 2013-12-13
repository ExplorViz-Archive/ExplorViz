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
package de.cau.cs.kieler.klay.force.graph;

import java.util.LinkedList;
import java.util.List;

import de.cau.cs.kieler.core.math.KVector;
import de.cau.cs.kieler.core.math.KVectorChain;
import de.cau.cs.kieler.core.properties.MapPropertyHolder;

/**
 * A physico-virtual representation of an edge, including a list of
 * associated bend points.
 * 
 * @author owo
 * @kieler.design proposed by msp
 * @kieler.rating proposed yellow by msp
 */
public class FEdge extends MapPropertyHolder {

    /** the serial version UID. */
    private static final long serialVersionUID = 4387555754824186467L;
    
    /** the bend points of the edge. */
    private List<FBendpoint> bendpoints = new LinkedList<FBendpoint>();
    /** the labels of the edge. */
    private List<FLabel> labels = new LinkedList<FLabel>();
    /** the source node of the edge. */
    private FNode source;
    /** the target node of the edge. */
    private FNode target;

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        if (source != null && target != null) {
            return source.toString() + "->" + target.toString();
        } else {
            return "e_" + hashCode();
        }
    }
    
    /**
     * Returns the source node.
     * 
     * @return the source node
     */
    public FNode getSource() {
        return source;
    }

    /**
     * Returns the target node.
     * 
     * @return the target node
     */
    public FNode getTarget() {
        return target;
    }

    /**
     * Returns the list of bend points associated with this edge.
     * 
     * @return list of bend points
     */
    public List<FBendpoint> getBendpoints() {
        return bendpoints;
    }
    
    /**
     * Returns the list of labels associated with this edge.
     * 
     * @return list of labels
     */
    public List<FLabel> getLabels() {
        return labels;
    }

    /**
     * Returns the docking point at the source node.
     * 
     * @return the source docking point
     */
    public KVector getSourcePoint() {
        KVector v = target.getPosition().differenceCreate(source.getPosition());
        clipVector(v, source.getSize().x, source.getSize().y);
        return v.add(source.getPosition());
    }

    /**
     * Returns the docking point at the target node.
     * 
     * @return the target docking point
     */
    public KVector getTargetPoint() {
        KVector v = source.getPosition().differenceCreate(target.getPosition());
        clipVector(v, target.getSize().x, target.getSize().y);
        return v.add(target.getPosition());
    }
    
    /**
     * Clip the given vector to a rectangular box of given size.
     * 
     * @param v vector relative to the center of the box
     * @param width width of the rectangular box
     * @param height height of the rectangular box
     */
    private static void clipVector(final KVector v, final double width, final double height) {
        double wh = width / 2, hh = height / 2;
        double absx = Math.abs(v.x), absy = Math.abs(v.y);
        double xscale = 1, yscale = 1;
        if (absx > wh) {
            xscale = wh / absx;
        }
        if (absy > hh) {
            yscale = hh / absy;
        }
        v.scale(Math.min(xscale, yscale));
    }

    /**
     * Sets the source vertex.
     * 
     * @param theSource
     *            the source vertex set to
     */
    public final void setSource(final FNode theSource) {
        source = theSource;
    }
    
    /**
     * Sets the target vertex.
     * 
     * @param theTarget
     *            the target vertex
     */
    public final void setTarget(final FNode theTarget) {
        target = theTarget;
    }
    
    /**
     * Returns a vector chain with all bend points and source and target point.
     * 
     * @return a vector chain for the edge
     */
    public KVectorChain toVectorChain() {
        KVectorChain vectorChain = new KVectorChain();
        vectorChain.add(getSourcePoint());
        for (FBendpoint bendPoint : bendpoints) {
            vectorChain.add(bendPoint.getPosition());
        }
        vectorChain.add(getTargetPoint());
        return vectorChain;
    }
    
    /**
     * Distribute the bend points evenly on the edge.
     */
    public void distributeBendpoints() {
        int count = bendpoints.size();
        if (count > 0) {
            KVector sourcePos = source.getPosition();
            KVector targetPos = target.getPosition();
            KVector incr = targetPos.differenceCreate(sourcePos).scale(1 / (double) (count + 1));
            KVector pos = sourcePos.clone();
            for (FBendpoint bendPoint : bendpoints) {
                bendPoint.getPosition().x = pos.x;
                bendPoint.getPosition().y = pos.y;
                pos.add(incr);
            }
        }
    }

}
