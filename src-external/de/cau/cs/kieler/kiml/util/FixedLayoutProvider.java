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
package de.cau.cs.kieler.kiml.util;

import de.cau.cs.kieler.core.alg.IKielerProgressMonitor;
import de.cau.cs.kieler.core.kgraph.KEdge;
import de.cau.cs.kieler.core.kgraph.KLabel;
import de.cau.cs.kieler.core.kgraph.KNode;
import de.cau.cs.kieler.core.kgraph.KPort;
import de.cau.cs.kieler.core.math.KVector;
import de.cau.cs.kieler.core.math.KVectorChain;
import de.cau.cs.kieler.kiml.AbstractLayoutProvider;
import de.cau.cs.kieler.kiml.klayoutdata.KEdgeLayout;
import de.cau.cs.kieler.kiml.klayoutdata.KInsets;
import de.cau.cs.kieler.kiml.klayoutdata.KPoint;
import de.cau.cs.kieler.kiml.klayoutdata.KShapeLayout;
import de.cau.cs.kieler.kiml.options.EdgeRouting;
import de.cau.cs.kieler.kiml.options.LayoutOptions;
import de.cau.cs.kieler.kiml.options.SizeConstraint;

/**
 * A layout provider that sets fixed positions for all elements. These positions are taken
 * from the {@link LayoutOptions#POSITION} and {@link LayoutOptions#BEND_POINTS} options.
 * Elements that have no position option attached just stay where they are.
 * This is useful for at least two things:
 * <ul>
 *   <li>Fix the layout of a part of the diagram so it won't be affected by automatic layout.</li>
 *   <li>Apply a layout imported from somewhere else, e.g. the original layout that was
 *     manually created in another modeling tool.</li>
 * </ul>
 *
 * @kieler.rating yellow 2012-08-10 review KI-23 by cds, sgu
 * @kieler.design proposed by msp
 * @author msp
 */
public class FixedLayoutProvider extends AbstractLayoutProvider {

    /** the layout provider id. */
    public static final String ID = "de.cau.cs.kieler.fixed";
    
    /** default value for border spacing. */
    private static final float DEF_BORDER_SPACING = 15.0f;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void doLayout(final KNode layoutNode, final IKielerProgressMonitor progressMonitor) {
        progressMonitor.begin("Fixed Layout", 1);
        KShapeLayout parentLayout = layoutNode.getData(KShapeLayout.class);
        EdgeRouting edgeRouting = parentLayout.getProperty(LayoutOptions.EDGE_ROUTING);
        float maxx = 0, maxy = 0;
        
        for (KNode node : layoutNode.getChildren()) {
            KShapeLayout nodeLayout = node.getData(KShapeLayout.class);
            // set the fixed position of the node, or leave it as it is
            KVector pos = nodeLayout.getProperty(LayoutOptions.POSITION);
            if (pos != null) {
                nodeLayout.applyVector(pos);
                // set the fixed size of the node
                // TODO Think about whether this makes sense with the new size constraint options.
                if (nodeLayout.getProperty(LayoutOptions.SIZE_CONSTRAINT).contains(
                        SizeConstraint.MINIMUM_SIZE)) {
                    
                    float width = nodeLayout.getProperty(LayoutOptions.MIN_WIDTH);
                    float height = nodeLayout.getProperty(LayoutOptions.MIN_HEIGHT);
                    if (width > 0 && height > 0) {
                        KimlUtil.resizeNode(node, width, height, true, true);
                    }
                }
            }
            maxx = Math.max(maxx, nodeLayout.getXpos() + nodeLayout.getWidth());
            maxy = Math.max(maxy, nodeLayout.getYpos() + nodeLayout.getHeight());
            
            // set the fixed position of the node labels, or leave them as they are
            for (KLabel label : node.getLabels()) {
                KShapeLayout labelLayout = label.getData(KShapeLayout.class);
                pos = labelLayout.getProperty(LayoutOptions.POSITION);
                if (pos != null) {
                    labelLayout.applyVector(pos);
                }
                maxx = Math.max(maxx, nodeLayout.getXpos() + labelLayout.getXpos()
                        + labelLayout.getWidth());
                maxy = Math.max(maxy, nodeLayout.getYpos() + labelLayout.getYpos()
                        + labelLayout.getHeight());
            }
                
            // set the fixed position of the ports, or leave them as they are
            for (KPort port : node.getPorts()) {
                KShapeLayout portLayout = port.getData(KShapeLayout.class);
                pos = portLayout.getProperty(LayoutOptions.POSITION);
                if (pos != null) {
                    portLayout.applyVector(pos);
                }
                float portx = nodeLayout.getXpos() + portLayout.getXpos();
                float porty = nodeLayout.getYpos() + portLayout.getYpos();
                maxx = Math.max(maxx, portx + portLayout.getWidth());
                maxy = Math.max(maxy, porty + portLayout.getHeight());
                
                // set the fixed position of the port labels, or leave them as they are
                for (KLabel label : port.getLabels()) {
                    KShapeLayout labelLayout = label.getData(KShapeLayout.class);
                    pos = labelLayout.getProperty(LayoutOptions.POSITION);
                    if (pos != null) {
                        labelLayout.applyVector(pos);
                    }
                    maxx = Math.max(maxx, portx + labelLayout.getXpos() + labelLayout.getWidth());
                    maxy = Math.max(maxy, porty + labelLayout.getYpos() + labelLayout.getHeight());
                }
            }
            
            // set fixed routing for the connected edges, or leave them as they are
            for (KEdge edge : node.getOutgoingEdges()) {
                KVector maxv = processEdge(edge, edgeRouting);
                maxx = Math.max(maxx, (float) maxv.x);
                maxy = Math.max(maxy, (float) maxv.y);
            }
            for (KEdge edge : node.getIncomingEdges()) {
                if (edge.getSource().getParent() != layoutNode) {
                    KVector maxv = processEdge(edge, edgeRouting);
                    maxx = Math.max(maxx, (float) maxv.x);
                    maxy = Math.max(maxy, (float) maxv.y);
                }
            }
        }
        
        // set size of the parent node
        float borderSpacing = parentLayout.getProperty(LayoutOptions.BORDER_SPACING);
        if (borderSpacing < 0) {
            borderSpacing = DEF_BORDER_SPACING;
        }
        KInsets insets = parentLayout.getInsets();
        float newWidth = maxx + borderSpacing + insets.getLeft() + insets.getRight();
        float newHeight = maxy + borderSpacing + insets.getTop() + insets.getBottom();
        KimlUtil.resizeNode(layoutNode, newWidth, newHeight, true, true);
        progressMonitor.done();
    }
    
    /**
     * Process an edge and its labels.
     * 
     * @param edge an edge
     * @param edgeRouting the global edge routing setting
     */
    private KVector processEdge(final KEdge edge, final EdgeRouting edgeRouting) {
        KEdgeLayout edgeLayout = edge.getData(KEdgeLayout.class);
        boolean sameHierarchy = edge.getSource().getParent() == edge.getTarget().getParent();
        KVector maxv = new KVector();
        KVectorChain bendPoints = edgeLayout.getProperty(LayoutOptions.BEND_POINTS);
        // we need at least two bend points, since the source point and target point must be included
        if (bendPoints != null && bendPoints.size() >= 2) {
            edgeLayout.applyVectorChain(bendPoints);
        }
        
        // determine maximal coordinates
        if (sameHierarchy) {
            for (KPoint point : edgeLayout.getBendPoints()) {
                maxv.x = Math.max(maxv.x, point.getX());
                maxv.y = Math.max(maxv.y, point.getY());
            }
        }
        
        // set the fixed position of the edge labels, or leave them as they are
        for (KLabel label : edge.getLabels()) {
            KShapeLayout labelLayout = label.getData(KShapeLayout.class);
            KVector pos = labelLayout.getProperty(LayoutOptions.POSITION);
            if (pos != null) {
                labelLayout.applyVector(pos);
            }
            if (sameHierarchy) {
                maxv.x = Math.max(maxv.x, labelLayout.getXpos() + labelLayout.getWidth());
                maxv.y = Math.max(maxv.y, labelLayout.getYpos() + labelLayout.getHeight());
            }
        }
        
        // if orthogonal routing is selected, determine the junction points
        // Note: if the edge coordinates are not modified, the junction points are also ignored
        if (edgeRouting == EdgeRouting.ORTHOGONAL) {
            KVectorChain junctionPoints = KimlUtil.determineJunctionPoints(edge);
            if (junctionPoints.isEmpty()) {
                edgeLayout.setProperty(LayoutOptions.JUNCTION_POINTS, null);
            } else {
                edgeLayout.setProperty(LayoutOptions.JUNCTION_POINTS, junctionPoints);
            }
        }
        
        return maxv;
    }

}
