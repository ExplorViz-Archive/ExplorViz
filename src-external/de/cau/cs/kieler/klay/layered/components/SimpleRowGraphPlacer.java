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
import de.cau.cs.kieler.klay.layered.graph.Layer;
import de.cau.cs.kieler.klay.layered.graph.LGraph;
import de.cau.cs.kieler.klay.layered.properties.Properties;

/**
 * A simple graph placer that places components into rows, trying to make the result fit a configurable
 * aspect ratio. This graph placer does not pay attention to external port connections and should not
 * be used in the presence of such connections.
 * 
 * <p>This was the first algorithm implemented to place the different connected components of a graph,
 * and was formerly the implementation of the {@link ComponentsProcessor#combine(List)} method.</p>
 * 
 * @author msp
 * @author cds
 * @kieler.design 2012-08-10 chsch grh
 * @kieler.rating proposed yellow by msp
 */
final class SimpleRowGraphPlacer extends AbstractGraphPlacer {

    /**
     * {@inheritDoc}
     */
    public LGraph combine(final List<LGraph> components) {
        if (components.size() == 1) {
            LGraph graph = components.get(0);
            // move all nodes away from the layers
            for (Layer layer : graph) {
                graph.getLayerlessNodes().addAll(layer.getNodes());
            }
            graph.getLayers().clear();
            return graph;
        } else if (components.isEmpty()) {
            return new LGraph();
        }
        
        // assign priorities
        for (LGraph graph : components) {
            int priority = 0;
            for (Layer layer : graph) {
                for (LNode node : layer) {
                    priority += node.getProperty(Properties.PRIORITY);
                }
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
        LGraph result = new LGraph(firstComponent);
        result.copyProperties(firstComponent);
        result.getInsets().copy(firstComponent.getInsets());
        
        // determine the maximal row width by the maximal box width and the total area
        double maxRowWidth = 0.0f;
        double totalArea = 0.0f;
        for (LGraph graph : components) {
            KVector size = graph.getSize();
            maxRowWidth = Math.max(maxRowWidth, size.x);
            totalArea += size.x * size.y;
        }
        maxRowWidth = Math.max(maxRowWidth, (float) Math.sqrt(totalArea)
                * result.getProperty(Properties.ASPECT_RATIO));
        double spacing = 2 * result.getProperty(Properties.OBJ_SPACING);

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
            moveGraph(result, graph, xpos, ypos);
            broadestRow = Math.max(broadestRow, xpos + size.x);
            highestBox = Math.max(highestBox, size.y);
            xpos += size.x + spacing;
        }
        
        result.getSize().x = broadestRow;
        result.getSize().y = ypos + highestBox;
        return result;
    }

}
