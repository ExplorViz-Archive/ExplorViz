/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 * 
 * Copyright 2012 by
 * + Christian-Albrechts-University of Kiel
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 * 
 * This code is provided under the terms of the Eclipse Public License (EPL).
 * See the file epl-v10.html for the license text.
 */
package de.cau.cs.kieler.klay.layered.components;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.cau.cs.kieler.core.math.KVector;
import de.cau.cs.kieler.klay.layered.graph.LNode;
import de.cau.cs.kieler.klay.layered.graph.LGraph;
import de.cau.cs.kieler.klay.layered.properties.Properties;

/**
 * A simple graph placer that places components into rows, trying to make the result fit a configurable
 * aspect ratio. This graph placer does not pay attention to external port connections and should not
 * be used in the presence of such connections.
 * 
 * <p>This is the first algorithm implemented to place the different connected components of a graph,
 * and was formerly the implementation of the {@link ComponentsProcessor#combine(List)} method.</p>
 * 
 * <p>The target graph must not be contained in the list of components, except if there is only
 * one component.</p>
 * 
 * @author msp
 * @author cds
 * @kieler.design 2012-08-10 chsch grh
 * @kieler.rating yellow 2014-04-22 review KI-48 by uru, tit, csp
 */
final class SimpleRowGraphPlacer extends AbstractGraphPlacer {
    
    /** Factor for spacing between components. */
    private static final float SPACING_FACTOR = 1.6f;

    /**
     * {@inheritDoc}
     */
    public void combine(final List<LGraph> components, final LGraph target) {
        if (components.size() == 1) {
            LGraph source = components.get(0);
            if (source != target) {
                target.getLayerlessNodes().clear();
                moveGraph(target, source, 0, 0);
                target.copyProperties(source);
                target.getInsets().copy(source.getInsets());
                target.getSize().x = source.getSize().x;
                target.getSize().y = source.getSize().y;
            }
            return;
        } else if (components.isEmpty()) {
            target.getLayerlessNodes().clear();
            target.getSize().x = 0;
            target.getSize().y = 0;
            return;
        }
        assert !components.contains(target);
        
        // assign priorities
        for (LGraph graph : components) {
            int priority = 0;
            for (LNode node : graph.getLayerlessNodes()) {
                priority += node.getProperty(Properties.PRIORITY);
            }
            graph.id = priority;
        }

        // sort the components by their priority and size
        Collections.sort(components, new Comparator<LGraph>() {
            public int compare(final LGraph graph1, final LGraph graph2) {
                int prio = graph2.id - graph1.id;
                if (prio == 0) {
                    double size1 = graph1.getSize().x * graph1.getSize().y;
                    double size2 = graph2.getSize().x * graph2.getSize().y;
                    return Double.compare(size1, size2);
                }
                return prio;
            }
        });
        
        LGraph firstComponent = components.get(0);
        target.getLayerlessNodes().clear();
        target.copyProperties(firstComponent);
        
        // determine the maximal row width by the maximal box width and the total area
        double maxRowWidth = 0.0f;
        double totalArea = 0.0f;
        for (LGraph graph : components) {
            KVector size = graph.getSize();
            maxRowWidth = Math.max(maxRowWidth, size.x);
            totalArea += size.x * size.y;
        }
        maxRowWidth = Math.max(maxRowWidth, (float) Math.sqrt(totalArea)
                * target.getProperty(Properties.ASPECT_RATIO));
        double spacing = SPACING_FACTOR * target.getProperty(Properties.OBJ_SPACING);

        // place nodes iteratively into rows
        double xpos = 0, ypos = 0, highestBox = 0, broadestRow = spacing;
        for (LGraph graph : components) {
            KVector size = graph.getSize();
            if (xpos + size.x > maxRowWidth) {
                // place the graph into the next row
                xpos = 0;
                ypos += highestBox + spacing;
                highestBox = 0;
            }
            moveGraph(target, graph, xpos, ypos);
            broadestRow = Math.max(broadestRow, xpos + size.x);
            highestBox = Math.max(highestBox, size.y);
            xpos += size.x + spacing;
        }
        
        target.getSize().x = broadestRow;
        target.getSize().y = ypos + highestBox;
    }

}
