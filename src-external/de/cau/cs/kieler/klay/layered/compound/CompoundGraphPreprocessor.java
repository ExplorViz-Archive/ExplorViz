/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 * 
 * Copyright 2013 by
 * + Christian-Albrechts-University of Kiel
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 * 
 * This code is provided under the terms of the Eclipse Public License (EPL).
 * See the file epl-v10.html for the license text.
 */
package de.cau.cs.kieler.klay.layered.compound;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import de.cau.cs.kieler.core.alg.IKielerProgressMonitor;
import de.cau.cs.kieler.core.math.KVector;
import de.cau.cs.kieler.core.properties.IPropertyHolder;
import de.cau.cs.kieler.core.properties.MapPropertyHolder;
import de.cau.cs.kieler.kiml.options.Direction;
import de.cau.cs.kieler.kiml.options.LayoutOptions;
import de.cau.cs.kieler.kiml.options.PortConstraints;
import de.cau.cs.kieler.kiml.options.PortSide;
import de.cau.cs.kieler.klay.layered.ILayoutProcessor;
import de.cau.cs.kieler.klay.layered.graph.LGraphUtil;
import de.cau.cs.kieler.klay.layered.graph.LEdge;
import de.cau.cs.kieler.klay.layered.graph.LGraph;
import de.cau.cs.kieler.klay.layered.graph.LNode;
import de.cau.cs.kieler.klay.layered.graph.LPort;
import de.cau.cs.kieler.klay.layered.properties.GraphProperties;
import de.cau.cs.kieler.klay.layered.properties.PortType;
import de.cau.cs.kieler.klay.layered.properties.Properties;

/**
 * Preprocess a compound graph by splitting cross-hierarchy edges. The result is stored in
 * {@link Properties#CROSS_HIERARCHY_MAP}, which is attached to the top-level graph.
 * 
 * <dl>
 *   <dt>Precondition:</dt>
 *     <dd>a compound graph with no layers.</dd>
 *   <dt>Postcondition:</dt>
 *     <dd>a compound graph with no layers and no cross-hierarchy edges, but with external ports.</dd>
 * </dl>
 *
 * @author msp
 */
public class CompoundGraphPreprocessor implements ILayoutProcessor {
    
    /** map of generated cross-hierarchy edges. */
    private Multimap<LEdge, CrossHierarchyEdge> crossHierarchyMap;
    /** map of ports to their assigned dummy nodes in the nested graphs. */
    private final Map<LPort, LNode> dummyNodeMap = Maps.newHashMap();

    /**
     * An internal representation for external ports. This class is used to pass information
     * gathered on one hierarchy level to the containing hierarchy level. Instances are created
     * whenever a cross-hierarchy edge crosses the hierarchy bounds of a parent node; the instance
     * represents the split point of the edge.
     */
    private static class ExternalPort {
        /** the original edge for which the port is created. */
        private LEdge origEdge;
        /** the new edge by which the original edge is replaced. */
        private LEdge newEdge;
        /** the node whose outer bounds are crossed by the edge. */
        private LNode parentNode;
        /** the dummy node used by the algorithm as representative for the external port. */
        private LNode dummyNode;
        /** the flow direction: input or output. */
        private PortType type = PortType.UNDEFINED;
        
        /**
         * Create an external port.
         * 
         * @param origEdge the original edge for which the port is created
         * @param newEdge the new edge by which the original edge is replaced
         * @param parentNode the node whose outer bounds are crossed by the edge
         * @param dummyNode the dummy node used by the algorithm as representative for the external port
         * @param portType the flow direction: input or output
         */
        ExternalPort(final LEdge origEdge, final LEdge newEdge, final LNode parentNode,
                final LNode dummyNode, final PortType portType) {
            this.origEdge = origEdge;
            this.newEdge = newEdge;
            this.parentNode = parentNode;
            this.dummyNode = dummyNode;
            this.type = portType;
        }

    }
    
    /**
     * {@inheritDoc}
     */
    public void process(final LGraph graph, final IKielerProgressMonitor monitor) {
        monitor.begin("Compound graph preprocessor", 1);
        crossHierarchyMap = HashMultimap.create();
        
        // create new dummy edges at hierarchy bounds
        transformHierarchyEdges(graph, null);
        
        // remove the original edges from the graph
        for (LEdge origEdge : crossHierarchyMap.keySet()) {
            origEdge.setSource(null);
            origEdge.setTarget(null);
        }
        
        graph.setProperty(Properties.CROSS_HIERARCHY_MAP, crossHierarchyMap);
        crossHierarchyMap = null;
        dummyNodeMap.clear();
        monitor.done();
    }
    
    /**
     * Perform automatic layout recursively in the given graph.
     * 
     * @param graph the layered graph
     * @param parentNode the parent node of the graph, or {@code null} if it is on top-level
     * @param monitor a progress monitor
     * @return the external ports created to split edges that cross the boundary of the parent node
     */
    private Collection<ExternalPort> transformHierarchyEdges(final LGraph graph,
            final LNode parentNode) {
        // process all children and gather their external ports
        List<ExternalPort> containedExternalPorts = new LinkedList<ExternalPort>();
        for (LNode node : graph.getLayerlessNodes()) {
            LGraph nestedGraph = node.getProperty(Properties.NESTED_LGRAPH);
            if (nestedGraph != null) {
                Collection<ExternalPort> childPorts = transformHierarchyEdges(nestedGraph, node);
                containedExternalPorts.addAll(childPorts);
                // create dummy nodes for all ports of the compound node
                if (nestedGraph.getProperty(Properties.GRAPH_PROPERTIES).contains(
                        GraphProperties.EXTERNAL_PORTS)) {
                    for (LPort port : node.getPorts()) {
                        if (dummyNodeMap.get(port) == null) {
                            LNode dummyNode = LGraphUtil.createExternalPortDummy(port,
                                    PortConstraints.FREE, PortSide.UNDEFINED, -port.getNetFlow(),
                                    null, null, port.getSize(),
                                    nestedGraph.getProperty(LayoutOptions.DIRECTION), nestedGraph);
                            dummyNode.setProperty(Properties.ORIGIN, port);
                            dummyNodeMap.put(port, dummyNode);
                            nestedGraph.getLayerlessNodes().add(dummyNode);
                        }
                    }
                }
            }
        }
        
        // process the cross-hierarchy edges connected to the inside of the child nodes
        List<ExternalPort> exportedExternalPorts = new LinkedList<ExternalPort>();
        processInsideEdges(graph, parentNode, exportedExternalPorts, containedExternalPorts);
        
        // process the cross-hierarchy edges connected to the outside of the parent node
        if (parentNode != null) {
            processOutsideEdges(graph, parentNode, exportedExternalPorts);
        }
        
        return exportedExternalPorts;
    }
    
    /**
     * Process edges connected to the inside of compound nodes contained in the given graph.
     * 
     * @param graph the processed graph
     * @param parentNode the parent node of the nested graph, or {@code null} if it is on top-level
     * @param exportedExternalPorts list into which new external ports are put
     * @param containedExternalPorts external ports gathered during the recursive layout of children
     */
    private void processInsideEdges(final LGraph graph, final LNode parentNode,
            final List<ExternalPort> exportedExternalPorts,
            final List<ExternalPort> containedExternalPorts) {
        for (ExternalPort externalPort : containedExternalPorts) {
            LNode sourceNode = externalPort.origEdge.getSource().getNode();
            LNode targetNode = externalPort.origEdge.getTarget().getNode();
            if (externalPort.type == PortType.INPUT && sourceNode.getGraph() != graph
                    && (parentNode == null || LGraphUtil.isDescendant(sourceNode, parentNode))) {
                // the edge comes from the inside of another sibling node,
                // hence we want to process it only once, namely as outgoing edge
                continue;
            }
            
            // create a dummy port matching the external port dummy node
            LPort newPort = createPortForDummy(externalPort.dummyNode, externalPort.parentNode,
                    externalPort.type);
            
            // create a new dummy edge for the next segment of the cross-hierarchy edge
            LEdge newEdge = new LEdge(graph);
            newEdge.copyProperties(externalPort.origEdge);
            newEdge.setProperty(LayoutOptions.JUNCTION_POINTS, null);
            crossHierarchyMap.put(externalPort.origEdge, new CrossHierarchyEdge(newEdge,
                    graph, externalPort.type));
            
            if (externalPort.type == PortType.OUTPUT) {
                newEdge.setSource(newPort);
                LPort targetPort = null;
                if (targetNode.getGraph() == graph) {
                    // the edge goes to a direct child of the parent node
                    targetPort = externalPort.origEdge.getTarget();
                } else if (parentNode == null || LGraphUtil.isDescendant(targetNode, parentNode)) {
                    // the edge goes to the inside of another sibling node
                    ExternalPort targetExtenalPort = null;
                    for (ExternalPort externalPort2 : containedExternalPorts) {
                        if (externalPort2 != externalPort
                                && externalPort2.origEdge == externalPort.origEdge) {
                            targetExtenalPort = externalPort2;
                            break;
                        }
                    }
                    assert targetExtenalPort.type == PortType.INPUT;
                    // create a dummy port matching the other external port dummy node
                    targetPort = createPortForDummy(targetExtenalPort.dummyNode,
                            targetExtenalPort.parentNode, targetExtenalPort.type);
                } else {
                    // the edge goes to the parent node or its outside
                    boolean dummyIsNew = !dummyNodeMap.containsKey(externalPort.origEdge.getTarget());
                    LNode dummyNode = createExternalPortDummy(graph, parentNode, PortType.OUTPUT,
                            externalPort.origEdge);
                    if (dummyIsNew) {
                        graph.getLayerlessNodes().add(dummyNode);
                    }
                    targetPort = dummyNode.getPorts().get(0);
                    if (targetNode != parentNode) {
                        exportedExternalPorts.add(new ExternalPort(externalPort.origEdge, newEdge,
                                parentNode, dummyNode, PortType.OUTPUT));
                    }
                }
                newEdge.setTarget(targetPort);
                
            } else if (externalPort.type == PortType.INPUT) {
                newEdge.setTarget(newPort);
                LPort sourcePort = null;
                if (sourceNode.getGraph() == graph) {
                    // the edge comes from a direct child of the parent node
                    sourcePort = externalPort.origEdge.getSource();
                } else {
                    // the edge comes from the parent node or its outside
                    boolean dummyIsNew = !dummyNodeMap.containsKey(externalPort.origEdge.getSource());
                    LNode dummyNode = createExternalPortDummy(graph, parentNode, PortType.INPUT,
                            externalPort.origEdge);
                    if (dummyIsNew) {
                        graph.getLayerlessNodes().add(dummyNode);
                    }
                    sourcePort = dummyNode.getPorts().get(0);
                    if (sourceNode != parentNode) {
                        exportedExternalPorts.add(new ExternalPort(externalPort.origEdge, newEdge,
                                parentNode, dummyNode, PortType.INPUT));
                    }
                }
                newEdge.setSource(sourcePort);
            }
        }
    }
    
    /**
     * Process edges incident to a node in the given graph and crossing its boundary. These
     * edges are split with instances of {@link ExternalPort}. The resulting edge segments are
     * stored as instances of {@link CrossHierarchyEdge} in the {@link #crossHierarchyMap}.
     * 
     * @param graph the processed graph
     * @param parentNode the parent node of the nested graph
     * @param exportedExternalPorts list into which new external ports are put
     */
    private void processOutsideEdges(final LGraph graph, final LNode parentNode,
            final List<ExternalPort> exportedExternalPorts) {
        List<ExternalPort> externalOutputPorts = new LinkedList<ExternalPort>();
        List<ExternalPort> externalInputPorts = new LinkedList<ExternalPort>();
        for (LNode childNode : graph.getLayerlessNodes()) {
            for (LEdge origEdge : childNode.getOutgoingEdges()) {
                if (!LGraphUtil.isDescendant(origEdge.getTarget().getNode(), parentNode)) {
                    // the edge goes to the outside of the parent node
                    LEdge newEdge = new LEdge(graph);
                    newEdge.copyProperties(origEdge);
                    newEdge.setProperty(LayoutOptions.JUNCTION_POINTS, null);
                    crossHierarchyMap.put(origEdge, new CrossHierarchyEdge(newEdge, graph,
                            PortType.OUTPUT));
                    LNode dummyNode = createExternalPortDummy(graph, parentNode, PortType.OUTPUT,
                            origEdge);
                    newEdge.setTarget(dummyNode.getPorts().get(0));
                    externalOutputPorts.add(new ExternalPort(origEdge, newEdge, parentNode,
                            dummyNode, PortType.OUTPUT));
                }
            }
            
            for (LEdge origEdge : childNode.getIncomingEdges()) {
                if (!LGraphUtil.isDescendant(origEdge.getSource().getNode(), parentNode)) {
                    // the edge comes from the outside of the parent node
                    LEdge newEdge = new LEdge(graph);
                    newEdge.copyProperties(origEdge);
                    newEdge.setProperty(LayoutOptions.JUNCTION_POINTS, null);
                    crossHierarchyMap.put(origEdge, new CrossHierarchyEdge(newEdge, graph,
                            PortType.INPUT));
                    LNode dummyNode = createExternalPortDummy(graph, parentNode, PortType.INPUT,
                            origEdge);
                    newEdge.setSource(dummyNode.getPorts().get(0));
                    externalInputPorts.add(new ExternalPort(origEdge, newEdge, parentNode,
                            dummyNode, PortType.INPUT));
                }
            }
        }
        
        // do some further adaptations outside of the above loop to avoid CMEs
        for (ExternalPort externalPort : externalOutputPorts) {
            if (!graph.getLayerlessNodes().contains(externalPort.dummyNode)) {
                graph.getLayerlessNodes().add(externalPort.dummyNode);
            }
            externalPort.newEdge.setSource(externalPort.origEdge.getSource());
            if (externalPort.origEdge.getTarget().getNode() != parentNode) {
                exportedExternalPorts.add(externalPort);
            }
        }
        for (ExternalPort externalPort : externalInputPorts) {
            if (!graph.getLayerlessNodes().contains(externalPort.dummyNode)) {
                graph.getLayerlessNodes().add(externalPort.dummyNode);
            }
            externalPort.newEdge.setTarget(externalPort.origEdge.getTarget());
            if (externalPort.origEdge.getSource().getNode() != parentNode) {
                exportedExternalPorts.add(externalPort);
            }
        }
    }
    
    /**
     * Create suitable port properties for dummy external ports.
     * 
     * @param graph the graph for which the dummy external port is created
     * @return properties to apply to the dummy port
     */
    private static IPropertyHolder getExternalPortProperties(final LGraph graph) {
        IPropertyHolder propertyHolder = new MapPropertyHolder();
        float offset = graph.getProperty(Properties.OBJ_SPACING)
                * graph.getProperty(Properties.EDGE_SPACING_FACTOR) / 2;
        propertyHolder.setProperty(Properties.OFFSET, offset);
        return propertyHolder;
    }
    
    /**
     * Create a dummy node for an external port.
     * 
     * @param graph the graph in which to create the dummy node
     * @param parentNode the corresponding parent node
     * @param type the type of external port
     * @param origEdge the original edge connected to the external port
     * @return the new dummy node
     */
    private LNode createExternalPortDummy(final LGraph graph, final LNode parentNode,
            final PortType type, final LEdge origEdge) {
        Direction layoutDirection = graph.getProperty(LayoutOptions.DIRECTION);
        LNode dummyNode = null;
        switch (type) {

        case OUTPUT: {
            LPort targetPort = origEdge.getTarget();
            if (targetPort.getNode() == parentNode) {
                dummyNode = dummyNodeMap.get(targetPort);
                if (dummyNode == null) {
                    dummyNode = LGraphUtil.createExternalPortDummy(targetPort,
                            PortConstraints.FREE, PortSide.UNDEFINED, 1, null, null,
                            targetPort.getSize(), layoutDirection, graph);
                    dummyNode.setProperty(Properties.ORIGIN, targetPort);
                    dummyNodeMap.put(targetPort, dummyNode);
                }
            } else {
                dummyNode = LGraphUtil.createExternalPortDummy(
                        getExternalPortProperties(graph), PortConstraints.FREE,
                        PortSide.UNDEFINED, 1, null, null, new KVector(), layoutDirection, graph);
            }
            break;
        }

        case INPUT: {
            LPort sourcePort = origEdge.getSource();
            if (sourcePort.getNode() == parentNode) {
                dummyNode = dummyNodeMap.get(sourcePort);
                if (dummyNode == null) {
                    dummyNode = LGraphUtil.createExternalPortDummy(sourcePort,
                            PortConstraints.FREE, PortSide.UNDEFINED, -1, null, null,
                            sourcePort.getSize(), layoutDirection, graph);
                    dummyNode.setProperty(Properties.ORIGIN, sourcePort);
                    dummyNodeMap.put(sourcePort, dummyNode);
                }
            } else {
                dummyNode = LGraphUtil.createExternalPortDummy(
                        getExternalPortProperties(graph), PortConstraints.FREE,
                        PortSide.UNDEFINED, -1, null, null, new KVector(), layoutDirection, graph);
            }
            break;
        }

        }
        
        graph.getProperty(Properties.GRAPH_PROPERTIES).add(GraphProperties.EXTERNAL_PORTS);
        PortConstraints portConstraints = graph.getProperty(LayoutOptions.PORT_CONSTRAINTS);
        if (portConstraints.isSideFixed()) {
            portConstraints = PortConstraints.FIXED_SIDE;
        } else {
            portConstraints = PortConstraints.FREE;
        }
        graph.setProperty(LayoutOptions.PORT_CONSTRAINTS, portConstraints);
        return dummyNode;
    }
    
    /**
     * Create a port for an existing external port dummy node.
     * 
     * @param dummyNode the dummy node
     * @param parentNode the parent node to which it is attached
     * @param type the port type
     * @return a new port
     */
    private LPort createPortForDummy(final LNode dummyNode, final LNode parentNode,
            final PortType type) {
        LGraph graph = parentNode.getGraph();
        Direction layoutDirection = graph.getProperty(LayoutOptions.DIRECTION);
        LPort port = new LPort(graph);
        port.setNode(parentNode);
        switch (type) {
        case INPUT:
            port.setSide(PortSide.fromDirection(layoutDirection).opposed());
            break;
        case OUTPUT:
            port.setSide(PortSide.fromDirection(layoutDirection));
            break;
        }
        port.setProperty(Properties.OFFSET, dummyNode.getProperty(Properties.OFFSET));
        dummyNode.setProperty(Properties.ORIGIN, port);
        dummyNodeMap.put(port, dummyNode);
        return port;
    }

}
