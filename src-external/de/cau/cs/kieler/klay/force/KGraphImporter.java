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
package de.cau.cs.kieler.klay.force;

import java.util.HashMap;
import java.util.Map;

import de.cau.cs.kieler.core.kgraph.KEdge;
import de.cau.cs.kieler.core.kgraph.KLabel;
import de.cau.cs.kieler.core.kgraph.KNode;
import de.cau.cs.kieler.core.math.KVector;
import de.cau.cs.kieler.kiml.klayoutdata.KEdgeLayout;
import de.cau.cs.kieler.kiml.klayoutdata.KInsets;
import de.cau.cs.kieler.kiml.klayoutdata.KShapeLayout;
import de.cau.cs.kieler.kiml.options.LayoutOptions;
import de.cau.cs.kieler.kiml.options.PortConstraints;
import de.cau.cs.kieler.kiml.util.KimlUtil;
import de.cau.cs.kieler.klay.force.graph.FEdge;
import de.cau.cs.kieler.klay.force.graph.FGraph;
import de.cau.cs.kieler.klay.force.graph.FLabel;
import de.cau.cs.kieler.klay.force.graph.FNode;
import de.cau.cs.kieler.klay.force.properties.Properties;

/**
 * Manages the transformation of KGraphs to FGraphs.
 *
 * @author msp
 * @kieler.design proposed by msp
 * @kieler.rating proposed yellow by msp
 */
public class KGraphImporter implements IGraphImporter<KNode> {
    
    ///////////////////////////////////////////////////////////////////////////////
    // Transformation KGraph -> FGraph

    /**
     * {@inheritDoc}
     */
    public FGraph importGraph(final KNode kgraph) {
        FGraph fgraph = new FGraph();
        fgraph.setProperty(Properties.ORIGIN, kgraph);
        
        // copy the properties of the KGraph to the force graph
        KShapeLayout sourceShapeLayout = kgraph.getData(KShapeLayout.class);
        fgraph.copyProperties(sourceShapeLayout);
        fgraph.checkProperties(Properties.SPACING, Properties.ASPECT_RATIO, Properties.TEMPERATURE,
                Properties.ITERATIONS, Properties.REPULSION);
                
        // keep a list of created nodes in the force graph
        Map<KNode, FNode> elemMap = new HashMap<KNode, FNode>();
        
        // transform everything
        transformNodes(kgraph, fgraph, elemMap);
        transformEdges(kgraph, fgraph, elemMap);
                
        return fgraph;
    }
    
    /**
     * Transforms the nodes and ports defined by the given layout node.
     * 
     * @param parentNode the layout node whose edges to transform.
     * @param fgraph the force graph.
     * @param elemMap the element map that maps the original {@code KGraph} elements to the
     *                transformed {@code FGraph} elements.
     */
    private void transformNodes(final KNode parentNode, final FGraph fgraph,
            final Map<KNode, FNode> elemMap) {
        int index = 0;
        for (KNode knode : parentNode.getChildren()) {
            // add a new node to the force graph, copying its size
            KShapeLayout nodeLayout = knode.getData(KShapeLayout.class);
            
            String label = "";
            if (!knode.getLabels().isEmpty()) {
                label = knode.getLabels().get(0).getText();
            }
            FNode newNode = new FNode(label);
            newNode.id = index++;
            newNode.setProperty(Properties.ORIGIN, knode);
            newNode.getPosition().x = nodeLayout.getXpos() + nodeLayout.getWidth() / 2;
            newNode.getPosition().y = nodeLayout.getYpos() + nodeLayout.getHeight() / 2;
            newNode.getSize().x = Math.max(nodeLayout.getWidth(), 1);
            newNode.getSize().y = Math.max(nodeLayout.getHeight(), 1);
            fgraph.getNodes().add(newNode);
            
            elemMap.put(knode, newNode);
            
            // port constraints cannot be undefined
            PortConstraints portConstraints = nodeLayout.getProperty(LayoutOptions.PORT_CONSTRAINTS);
            if (portConstraints == PortConstraints.UNDEFINED) {
                portConstraints = PortConstraints.FREE;
            }
            
            // TODO consider ports
            
            // set properties of the new node
            newNode.copyProperties(nodeLayout);
        }
    }
    
    /**
     * Transforms the edges defined by the given layout node.
     * 
     * @param parentNode the layout node whose edges to transform.
     * @param fgraph the force graph.
     * @param elemMap the element map that maps the original {@code KGraph} elements to the
     *                transformed {@code FGraph} elements.
     */
    private void transformEdges(final KNode parentNode, final FGraph fgraph,
            final Map<KNode, FNode> elemMap) {
        for (KNode knode : parentNode.getChildren()) {
            for (KEdge kedge : knode.getOutgoingEdges()) {
                // exclude edges that pass hierarchy bounds as well as self-loops
                if (kedge.getTarget().getParent() == parentNode && knode != kedge.getTarget()) {
                    KEdgeLayout edgeLayout = kedge.getData(KEdgeLayout.class);
                    
                    // create a force edge
                    FEdge newEdge = new FEdge();
                    newEdge.setProperty(Properties.ORIGIN, kedge);
                    newEdge.setSource(elemMap.get(knode));
                    newEdge.setTarget(elemMap.get(kedge.getTarget()));
                    fgraph.getEdges().add(newEdge);
                    
                    // transform the edge's labels
                    for (KLabel klabel : kedge.getLabels()) {
                        KShapeLayout labelLayout = klabel.getData(KShapeLayout.class);
                        FLabel newLabel = new FLabel(newEdge, klabel.getText());
                        newLabel.getSize().x = Math.max(labelLayout.getWidth(), 1);
                        newLabel.getSize().y = Math.max(labelLayout.getHeight(), 1);
                        newLabel.setProperty(Properties.ORIGIN, klabel);
                        newLabel.refreshPosition();
                        fgraph.getLabels().add(newLabel);
                    }
                    
                    // set properties of the new edge
                    newEdge.copyProperties(edgeLayout);
                    newEdge.checkProperties(Properties.LABEL_SPACING, Properties.EDGE_REP);
                }
            }
        }
    }    
    
    ///////////////////////////////////////////////////////////////////////////////
    // Apply Layout Results
    


    /**
     * {@inheritDoc}
     */
    public void applyLayout(final FGraph fgraph) {
        KNode kgraph = (KNode) fgraph.getProperty(Properties.ORIGIN);
        
        // determine the border spacing, which influences the offset
        KShapeLayout parentLayout = kgraph.getData(KShapeLayout.class);
        float borderSpacing = fgraph.getProperty(LayoutOptions.BORDER_SPACING);
        if (borderSpacing < 0) {
            borderSpacing = Properties.SPACING.getDefault();
        }
        fgraph.setProperty(LayoutOptions.BORDER_SPACING, borderSpacing);
        
        // calculate the offset from border spacing and node distribution
        double minXPos = Integer.MAX_VALUE, minYPos = Integer.MAX_VALUE,
                maxXPos = Integer.MIN_VALUE, maxYPos = Integer.MIN_VALUE;
        for (FNode node : fgraph.getNodes()) {
            KVector pos = node.getPosition();
            KVector size = node.getSize();
            minXPos = Math.min(minXPos, pos.x - size.x / 2);
            minYPos = Math.min(minYPos, pos.y - size.y / 2);
            maxXPos = Math.max(maxXPos, pos.x + size.x / 2);
            maxYPos = Math.max(maxYPos, pos.y + size.y / 2);
        }
        KVector offset = new KVector(borderSpacing - minXPos, borderSpacing - minYPos);
        
        // process the nodes
        for (FNode fnode : fgraph.getNodes()) {
            Object object = fnode.getProperty(Properties.ORIGIN);
            
            if (object instanceof KNode) {
                // set the node position
                KNode knode = (KNode) object;
                KShapeLayout nodeLayout = knode.getData(KShapeLayout.class);
                KVector nodePos = fnode.getPosition().add(offset);
                nodeLayout.setXpos((float) nodePos.x - nodeLayout.getWidth() / 2);
                nodeLayout.setYpos((float) nodePos.y - nodeLayout.getHeight() / 2);
            }
        }
        
        // process the edges
        for (FEdge fedge : fgraph.getEdges()) {
            KEdge kedge = (KEdge) fedge.getProperty(Properties.ORIGIN);
            KEdgeLayout edgeLayout = kedge.getData(KEdgeLayout.class);
            edgeLayout.getBendPoints().clear();
            edgeLayout.getSourcePoint().applyVector(fedge.getSourcePoint());
            edgeLayout.getTargetPoint().applyVector(fedge.getTargetPoint());
        }
        
        // process the labels
        for (FLabel flabel : fgraph.getLabels()) {
            KLabel klabel = (KLabel) flabel.getProperty(Properties.ORIGIN);
            KShapeLayout klabelLayout = klabel.getData(KShapeLayout.class);
            KVector labelPos = flabel.getPosition().add(offset);
            klabelLayout.applyVector(labelPos);
        }
        
        // set up the parent node
        KInsets insets = parentLayout.getInsets();
        float width = (float) (maxXPos - minXPos) + 2 * borderSpacing
                + insets.getLeft() + insets.getRight();
        float height = (float) (maxYPos - minYPos) + 2 * borderSpacing
                + insets.getTop() + insets.getBottom();
        KimlUtil.resizeNode(kgraph, width, height, false, true);
    }
    
}
