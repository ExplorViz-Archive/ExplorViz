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
package de.cau.cs.kieler.klay.layered.components;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.cau.cs.kieler.core.util.Pair;
import de.cau.cs.kieler.kiml.options.LayoutOptions;
import de.cau.cs.kieler.kiml.options.PortConstraints;
import de.cau.cs.kieler.kiml.options.PortSide;
import de.cau.cs.kieler.klay.layered.graph.LNode;
import de.cau.cs.kieler.klay.layered.graph.LPort;
import de.cau.cs.kieler.klay.layered.graph.LGraph;
import de.cau.cs.kieler.klay.layered.properties.GraphProperties;
import de.cau.cs.kieler.klay.layered.properties.NodeType;
import de.cau.cs.kieler.klay.layered.properties.Properties;

/**
 * A processor that is able to split an input graph into connected components and to pack those
 * components after layout.
 * 
 * <p>If the graph has no external ports, splitting it into components is straightforward and always
 * works. If on the other hand it does have external ports, splitting the graph into connected
 * components is problematic because the port positions might introduce constraints on the placement
 * of the different components. More or less simple solutions have only been implemented for the cases
 * of port constraints set to {@link de.cau.cs.kieler.kiml.options.PortConstraints#FREE FREE} or
 * {@link de.cau.cs.kieler.kiml.options.PortConstraints#FIXED_SIDE FIXED_SIDE}. If the graph contains
 * external ports with port constraints other than these, connected components processing is disabled
 * even if requested by the user.</p>
 * 
 * <p>Splitting into components
 * <dl>
 *   <dt>Precondition:</dt>
 *     <dd>an unlayered graph.</dd>
 *   <dt>Postcondition:</dt><dd>a list of graphs that represent the connected components of
 *     the input graph.</dd>
 * </dl>
 * </p>
 * 
 * <p>Packing components
 * <dl>
 *   <dt>Precondition:</dt><dd>a list of unlayered graphs with complete layout.</dd>
 *   <dt>Postcondition:</dt><dd>a single unlayered graph.</dd>
 * </dl>
 * </p>
 *
 * @author msp
 * @author cds
 * @kieler.design 2012-08-10 chsch grh
 * @kieler.rating proposed yellow by msp
 */
public final class ComponentsProcessor {
    
    /**
     * Graph placer to be used to combine the different components back into a single graph.
     */
    private AbstractGraphPlacer graphPlacer;
    

    /**
     * Split the given graph into its connected components.
     * 
     * @param graph an input graph with layerless nodes
     * @return a list of components that can be processed one by one
     */
    public List<LGraph> split(final LGraph graph) {
        List<LGraph> result;
        
        // Whether separate components processing is requested
        Boolean separateProperty = graph.getProperty(LayoutOptions.SEPARATE_CC);
        boolean separate = separateProperty == null || separateProperty.booleanValue();
        
        // Whether the graph contains external ports
        boolean extPorts =
                graph.getProperty(Properties.GRAPH_PROPERTIES).contains(GraphProperties.EXTERNAL_PORTS);
        
        // The graph's external port constraints
        PortConstraints extPortConstraints = graph.getProperty(LayoutOptions.PORT_CONSTRAINTS);
        boolean compatiblePortConstraints = extPortConstraints == PortConstraints.FREE
                || extPortConstraints == PortConstraints.FIXED_SIDE;
        
        // The graph may only be separated 
        //  1. Separation was requested.
        //  2. If the graph contains external ports, port constraints are set to either
        //     FREE or FIXED_SIDES.
        if (separate && (compatiblePortConstraints || !extPorts)) {
            // Set id of all nodes to 0
            for (LNode node : graph.getLayerlessNodes()) {
                node.id = 0;
            }
            
            // Perform DFS starting on each node, collecting connected components
            result = new LinkedList<LGraph>();
            for (LNode node : graph.getLayerlessNodes()) {
                Pair<List<LNode>, Set<PortSide>> componentData = dfs(node, null);
                
                if (componentData != null) {
                    LGraph newGraph = new LGraph(graph);
                    
                    newGraph.copyProperties(graph);
                    newGraph.setProperty(Properties.EXT_PORT_CONNECTIONS, componentData.getSecond());
                    newGraph.getInsets().copy(graph.getInsets());
                    for (LNode n : componentData.getFirst()) {
                        newGraph.getLayerlessNodes().add(n);
                        n.setGraph(newGraph);
                    }
                    
                    result.add(newGraph);
                }
            }
            
            if (extPorts) {
                // With external ports connections, we want to use the more complex components
                // placement algorithm
                if (!(graphPlacer instanceof ComponentGroupGraphPlacer)) {
                    graphPlacer = new ComponentGroupGraphPlacer();
                }
            } else {
                // If there are no connections to external ports, default to the simpler components
                // placement algorithm
                if (!(graphPlacer instanceof SimpleRowGraphPlacer)) {
                    graphPlacer = new SimpleRowGraphPlacer();
                }
            }
        } else {
            result = Arrays.asList(graph);
            
            if (!(graphPlacer instanceof SimpleRowGraphPlacer)) {
                graphPlacer = new SimpleRowGraphPlacer();
            }
        }
        
        return result;
    }
    
    /**
     * Perform a DFS starting on the given node, collect all nodes that are found in the corresponding
     * connected component and return the set of external port sides the component connects to.
     * 
     * @param node a node.
     * @param data pair of nodes in the component and external port sides used to produce the result
     *             during recursive calls. Should be {@code null} when this method is called.
     * @return a pairing of the connected component and the set of port sides of external ports it
     *         connects to, or {@code null} if the node was already visited
     */
    private Pair<List<LNode>, Set<PortSide>> dfs(final LNode node,
            final Pair<List<LNode>, Set<PortSide>> data) {
        
        if (node.id == 0) {
            // Mark the node as visited
            node.id = 1;
            
            // Check if we already have a list of nodes for the connected component
            Pair<List<LNode>, Set<PortSide>> mutableData = data;
            if (mutableData == null) {
                List<LNode> component = new LinkedList<LNode>();
                Set<PortSide> extPortSides = EnumSet.noneOf(PortSide.class);
                
                mutableData = new Pair<List<LNode>, Set<PortSide>>(component, extPortSides);
            }
            
            // Add this node to the component
            mutableData.getFirst().add(node);
            
            // Check if this node is an external port dummy and, if so, add its side
            if (node.getProperty(Properties.NODE_TYPE) == NodeType.EXTERNAL_PORT) {
                mutableData.getSecond().add(node.getProperty(Properties.EXT_PORT_SIDE));
            }
            
            // DFS
            for (LPort port1 : node.getPorts()) {
                for (LPort port2 : port1.getConnectedPorts()) {
                    dfs(port2.getNode(), mutableData);
                }
            }
            
            return mutableData;
        }
        
        // The node was already visited
        return null;
    }
    
    /**
     * Combine the given components into a single graph by moving them around such that they are
     * placed next and beneath to each other instead of overlapping.
     * 
     * @param components a list of components
     * @param target the target graph into which the others shall be combined
     */
    public void combine(final List<LGraph> components, final LGraph target) {
        graphPlacer.combine(components, target);
    }
    
}
