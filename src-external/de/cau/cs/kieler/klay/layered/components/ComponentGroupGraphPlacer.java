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

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;

import de.cau.cs.kieler.core.math.KVector;
import de.cau.cs.kieler.core.math.KielerMath;
import de.cau.cs.kieler.klay.layered.graph.LGraph;
import de.cau.cs.kieler.klay.layered.properties.Properties;

/**
 * A graph placer that tries to place the components of a graph with taking connections to external
 * ports into account. This graph placer should only be used if the constraints applying to the
 * external ports are either {@code FREE} or {@code FIXED_SIDES}.
 * 
 * <p>This placer first greedily builds a list of {@link ComponentGroup} instances. It is greedy in
 * that it places a component in the first group it finds that is able to hold it. Afterwards, the
 * components in each group are placed. The different groups are then placed along a diagonal from
 * the top-left to the bottom-right corner.</p>
 * 
 * @author cds
 * @kieler.design 2012-08-10 chsch grh
 * @kieler.rating proposed yellow by msp
 */
final class ComponentGroupGraphPlacer extends AbstractGraphPlacer {
    
    ///////////////////////////////////////////////////////////////////////////////
    // Variables
    
    /**
     * List of component groups holding the different components.
     */
    private final List<ComponentGroup> componentGroups = Lists.newLinkedList();
    
    
    ///////////////////////////////////////////////////////////////////////////////
    // AbstractGraphPlacer

    /**
     * {@inheritDoc}
     */
    public LGraph combine(final List<LGraph> components) {
        // Check if there are any components to be placed
        if (components.isEmpty()) {
            return new LGraph();
        }
        
        // Create a new layered graph
        LGraph firstComponent = components.get(0);
        LGraph result = new LGraph(firstComponent);
        
        // Set the graph properties
        result.copyProperties(firstComponent);
        result.getInsets().copy(firstComponent.getInsets());
        
        // Construct component groups
        for (LGraph component : components) {
            addComponent(component);
        }
        
        // Place components in each group
        KVector offset = new KVector();
        float spacing = 2 * components.get(0).getProperty(Properties.OBJ_SPACING);
        
        for (ComponentGroup group : componentGroups) {
            // Place the components
            KVector groupSize = placeComponents(group, spacing);
            moveGraphs(result, group.getComponents(), offset.x, offset.y);
            
            // Compute the new offset
            offset.x += groupSize.x;
            offset.y += groupSize.y;
        }
        
        // Set the new graph's new size (the component group sizes include additional spacing
        // on the right and bottom sides which we need to subtract at this point)
        result.getSize().x = offset.x - spacing;
        result.getSize().y = offset.y - spacing;
        
        return result;
    }
    
    
    ///////////////////////////////////////////////////////////////////////////////
    // Component Group Building
    
    /**
     * Adds the given component to the first component group that has place for it.
     * 
     * @param component the component to be placed.
     */
    private void addComponent(final LGraph component) {
        // Check if one of the existing component groups has some place left
        for (ComponentGroup group : componentGroups) {
            if (group.add(component)) {
                return;
            }
        }
        
        // Create a new component group for the component
        componentGroups.add(new ComponentGroup(component));
    }
    
    
    ///////////////////////////////////////////////////////////////////////////////
    // Component Placement
    
    /* Component placement works as follows.
     * 
     * We first go through all the possible combinations of external port side connections. For each
     * combination, we compute a placement for the components with the given combination, remembering
     * the amount of space the placement takes up.
     * 
     * We then go through all the combinations again, this time moving the components so as not to
     * overlap other components.
     */
    
    
    /**
     * Computes a placement for the components in the given component group.
     * 
     * @param group the group whose components are to be placed.
     * @param spacing the amount of space to leave between two components.
     * @return the group's size.
     */
    private KVector placeComponents(final ComponentGroup group, final double spacing) {
        
        // Determine the spacing between two components
        // Place the different sector components and remember the amount of space their placement uses.
        // In this phase, we pretend that no other components are in the component group.
        KVector sizeC = placeComponentsInRows(
                group.getComponents(ComponentGroup.CONN_C), spacing);
        KVector sizeN = placeComponentsHorizontally(
                group.getComponents(ComponentGroup.CONN_N), spacing);
        KVector sizeS = placeComponentsHorizontally(
                group.getComponents(ComponentGroup.CONN_S), spacing);
        KVector sizeW = placeComponentsVertically(
                group.getComponents(ComponentGroup.CONN_W), spacing);
        KVector sizeE = placeComponentsVertically(
                group.getComponents(ComponentGroup.CONN_E), spacing);
        KVector sizeNW = placeComponentsHorizontally(
                group.getComponents(ComponentGroup.CONN_NW), spacing);
        KVector sizeNE = placeComponentsHorizontally(
                group.getComponents(ComponentGroup.CONN_NE), spacing);
        KVector sizeSW = placeComponentsHorizontally(
                group.getComponents(ComponentGroup.CONN_SW), spacing);
        KVector sizeSE = placeComponentsHorizontally(
                group.getComponents(ComponentGroup.CONN_SE), spacing);
        KVector sizeWE = placeComponentsVertically(
                group.getComponents(ComponentGroup.CONN_WE), spacing);
        KVector sizeNS = placeComponentsHorizontally(
                group.getComponents(ComponentGroup.CONN_NS), spacing);
        KVector sizeNWE = placeComponentsHorizontally(
                group.getComponents(ComponentGroup.CONN_NWE), spacing);
        KVector sizeSWE = placeComponentsHorizontally(
                group.getComponents(ComponentGroup.CONN_SWE), spacing);
        KVector sizeWNS = placeComponentsVertically(
                group.getComponents(ComponentGroup.CONN_WNS), spacing);
        KVector sizeENS = placeComponentsVertically(
                group.getComponents(ComponentGroup.CONN_ENS), spacing);
        
        // Find the maximum height of the three rows and the maximum width of the three columns the
        // component group is divided into (we're adding a fourth row for WE components and a fourth
        // column for NS components to make the placement easier later)
        double colLeftWidth = KielerMath.maxd(sizeNW.x, sizeW.x, sizeSW.x, sizeWNS.x);
        double colMidWidth = KielerMath.maxd(sizeN.x, sizeC.x, sizeS.x);
        double colNsWidth = sizeNS.x;
        double colRightWidth = KielerMath.maxd(sizeNE.x, sizeE.x, sizeSE.x, sizeENS.x);
        double rowTopHeight = KielerMath.maxd(sizeNW.y, sizeN.y, sizeNE.y, sizeNWE.y);
        double rowMidHeight = KielerMath.maxd(sizeW.y, sizeC.y, sizeE.y);
        double rowWeHeight = sizeWE.y;
        double rowBottomHeight = KielerMath.maxd(sizeSW.y, sizeS.y, sizeSE.y, sizeSWE.y);
        
        // With the individual placements computed, we now move the components to their final place,
        // taking the size of other component placements into account (the NW, NWE, and WNS components
        // stay at coordinates (0,0) and thus don't need to be moved around)
        offsetGraphs(group.getComponents(ComponentGroup.CONN_C),
                colLeftWidth + colNsWidth,
                rowTopHeight + rowWeHeight);
        offsetGraphs(group.getComponents(ComponentGroup.CONN_N),
                colLeftWidth + colNsWidth,
                0.0);
        offsetGraphs(group.getComponents(ComponentGroup.CONN_S),
                colLeftWidth + colNsWidth,
                rowTopHeight + rowWeHeight + rowMidHeight);
        offsetGraphs(group.getComponents(ComponentGroup.CONN_W),
                0.0,
                rowTopHeight + rowWeHeight);
        offsetGraphs(group.getComponents(ComponentGroup.CONN_E),
                colLeftWidth + colNsWidth + colMidWidth,
                rowTopHeight + rowWeHeight);
        offsetGraphs(group.getComponents(ComponentGroup.CONN_NE),
                colLeftWidth + colNsWidth + colMidWidth,
                0.0);
        offsetGraphs(group.getComponents(ComponentGroup.CONN_SW),
                0.0,
                rowTopHeight + rowWeHeight + rowMidHeight);
        offsetGraphs(group.getComponents(ComponentGroup.CONN_SE),
                colLeftWidth + colNsWidth + colMidWidth,
                rowTopHeight + rowWeHeight + rowMidHeight);
        offsetGraphs(group.getComponents(ComponentGroup.CONN_WE),
                0.0,
                rowTopHeight);
        offsetGraphs(group.getComponents(ComponentGroup.CONN_NS),
                colLeftWidth,
                0.0);
        offsetGraphs(group.getComponents(ComponentGroup.CONN_SWE),
                0.0,
                rowTopHeight + rowWeHeight + rowMidHeight);
        offsetGraphs(group.getComponents(ComponentGroup.CONN_ENS),
                colLeftWidth + colNsWidth + colMidWidth,
                0.0);
        
        // Compute this component group's size
        KVector componentSize = new KVector();
        componentSize.x = KielerMath.maxd(
                colLeftWidth + colMidWidth + colNsWidth + colRightWidth,
                sizeWE.x,
                sizeNWE.x,
                sizeSWE.x);
        componentSize.y = KielerMath.maxd(
                rowTopHeight + rowMidHeight + rowWeHeight + rowBottomHeight,
                sizeNS.y,
                sizeWNS.y,
                sizeENS.y);
        
        return componentSize;
    }
    
    /**
     * Places the given collection of components along a horizontal line.
     * 
     * @param components the components to place.
     * @param spacing the amount of space to leave between two components.
     * @return the space used by the component placement, including spacing to the right and to the
     *         bottom of the components.
     */
    private KVector placeComponentsHorizontally(final Collection<LGraph> components,
            final double spacing) {
        
        KVector size = new KVector();
        
        // Iterate over the components and place them
        for (LGraph component : components) {
            offsetGraph(component, size.x, 0.0);
            
            size.x += component.getSize().x + spacing;
            size.y = Math.max(size.y, component.getSize().y);
        }
        
        // Add vertical spacing, if necessary
        if (size.y > 0.0) {
            size.y += spacing;
        }
        
        return size;
    }
    
    /**
     * Places the given collection of components along a vertical line.
     * 
     * @param components the components to place.
     * @param spacing the amount of space to leave between two components.
     * @return the space used by the component placement.
     */
    private KVector placeComponentsVertically(final Collection<LGraph> components,
            final double spacing) {
        
        KVector size = new KVector();
        
        // Iterate over the components and place them
        for (LGraph component : components) {
            offsetGraph(component, 0.0, size.y);
            
            size.y += component.getSize().y + spacing;
            size.x = Math.max(size.x, component.getSize().x);
        }
        
        // Add horizontal spacing, if necessary
        if (size.x > 0.0) {
            size.x += spacing;
        }
        
        return size;
    }
    
    /**
     * Place the given collection of components in multiple rows.
     * 
     * @param components the components to place.
     * @param spacing the amount of space to leave between two components.
     * @return the space used by the component placement.
     */
    private KVector placeComponentsInRows(final Collection<LGraph> components,
            final double spacing) {
        
        /* This code is basically taken from the SimpleRowGraphPlacer. */
        
        // Check if there actually are any components
        if (components.isEmpty()) {
            return new KVector();
        }
        
        // Determine the maximal row width by the maximal box width and the total area
        double maxRowWidth = 0.0f;
        double totalArea = 0.0f;
        for (LGraph component : components) {
            KVector componentSize = component.getSize();
            maxRowWidth = Math.max(maxRowWidth, componentSize.x);
            totalArea += componentSize.x * componentSize.y;
        }
        
        maxRowWidth = Math.max(maxRowWidth, (float) Math.sqrt(totalArea)
                * components.iterator().next().getProperty(Properties.ASPECT_RATIO));
        
        // Place nodes iteratively into rows
        double xpos = 0, ypos = 0, highestBox = 0, broadestRow = spacing;
        for (LGraph graph : components) {
            KVector size = graph.getSize();
            
            if (xpos + size.x > maxRowWidth) {
                // Place the graph into the next row
                xpos = 0;
                ypos += highestBox + spacing;
                highestBox = 0;
            }
            
            offsetGraph(graph, xpos, ypos);
            
            broadestRow = Math.max(broadestRow, xpos + size.x);
            highestBox = Math.max(highestBox, size.y);
            
            xpos += size.x + spacing;
        }
        
        return new KVector(broadestRow + spacing, ypos + highestBox + spacing);
    }

}
