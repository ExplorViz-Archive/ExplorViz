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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
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
import de.cau.cs.kieler.klay.layered.graph.LLabel;
import de.cau.cs.kieler.klay.layered.graph.LNode;
import de.cau.cs.kieler.klay.layered.graph.LPort;
import de.cau.cs.kieler.klay.layered.properties.GraphProperties;
import de.cau.cs.kieler.klay.layered.properties.InternalProperties;
import de.cau.cs.kieler.klay.layered.properties.PortType;
import de.cau.cs.kieler.klay.layered.properties.Properties;

/**
 * Preprocess a compound graph by splitting cross-hierarchy edges. The result is stored in
 * {@link Properties#CROSS_HIERARCHY_MAP}, which is attached to the top-level graph.
 * 
 * <p>This processor assumes that external port dummy nodes only occur on the uppermost level of
 * hierarchy in the input graph. In all deeper levels, it is the job of this processor to create
 * external ports and associated dummy nodes.</p>
 * 
 * <strong>Implementation Notes</strong>
 * 
 * <p>Basically, the algorithm replaces cross-hierarchy edges by hierarchy-local edge segments. It
 * distinguishes between two types of segments: <em>outer segments</em> and <em>inner segments</em>.
 * Outer segments are those two segments that connect to the original source or target port of a
 * hierarchical edge. Inner segments are the remaining segments between the two outer segments.</p>
 * 
 * <p>To split cross-hierarchy edges, the algorithm dives depth-first into the graph's hierarchy tree
 * and begins working its way from the deepest levels of hierarchy upwards. For each contained graph,
 * it looks for cross-hierarchy edges beginning or ending there and starts by splitting those up and
 * thus creating the first outer segments. This will result in external ports being created and added
 * to the graph's parent node. Those are published to the upper level of hierarchy.</p>
 * 
 * </p>All external ports thus published by child nodes are then processed further. With all outer
 * segments created, the algorithm then creates required inner segments. To this end, it continues
 * adding external ports and publishes them to the upper level of hierarchy.</p>
 * 
 * <p>Remember when graph layout was easy? Pepperidge Farm remembers...</p>
 * 
 * <dl>
 *   <dt>Precondition:</dt>
 *     <dd>a compound graph with no layers.</dd>
 *     <dd>no external port dummy nodes except on the uppermost hierarchy level.</dd>
 *   <dt>Postcondition:</dt>
 *     <dd>a compound graph with no layers and no cross-hierarchy edges, but with external ports.</dd>
 * </dl>
 *
 * @author msp
 * @author cds
 * @kieler.design proposed by cds
 * @kieler.rating proposed yellow by cds
 */
public class CompoundGraphPreprocessor implements ILayoutProcessor {
    
    /** map of original edges to generated cross-hierarchy edges. */
    private Multimap<LEdge, CrossHierarchyEdge> crossHierarchyMap;
    /** map of ports to their assigned dummy nodes in the nested graphs. */
    private final BiMap<LPort, LNode> dummyNodeMap = HashBiMap.create();

    /**
     * An internal representation for external ports. This class is used to pass information
     * gathered on one hierarchy level to the containing hierarchy level. Instances are created
     * whenever a cross-hierarchy edge crosses the hierarchy bounds of a parent node; the instance
     * represents the split point of the edge.
     */
    private static class ExternalPort {
        /** the list of original edges for which the port is created. */
        private List<LEdge> origEdges = Lists.newLinkedList();
        /** the new edge by which the original edge is replaced. */
        private LEdge newEdge;
        /** the dummy node used by the algorithm as representative for the external port. */
        private LNode dummyNode;
        /** the dummy port used by the algorithm as representative for the external port. */
        private LPort dummyPort;
        /** the flow direction: input or output. */
        private PortType type = PortType.UNDEFINED;
        /**
         * whether the external port will be exported to the outside or not. (it will not be exported
         * if the port was introduced for connections from an inside node to its parent)
         */
        private boolean exported;
        
        /**
         * Create an external port.
         * 
         * @param origEdge the original edge for which the port is created
         * @param newEdge the new edge by which the original edge is replaced
         * @param dummyNode the dummy node used by the algorithm as representative for the external port
         * @param portType the flow direction: input or output
         * @param exported whether the external port is to be exported by its parent.
         */
        ExternalPort(final LEdge origEdge, final LEdge newEdge, final LNode dummyNode,
                final LPort dummyPort, final PortType portType, final boolean exported) {
            
            this.origEdges.add(origEdge);
            this.newEdge = newEdge;
            this.dummyNode = dummyNode;
            this.dummyPort = dummyPort;
            this.type = portType;
            this.exported = exported;
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
        
        // move all labels of the original edges to the appropriate dummy edges and remove the original
        // edges from the graph
        for (LEdge origEdge : crossHierarchyMap.keySet()) {
            // if the original edge had any labels, we need to move them to the newly introduced edge
            // segments
            if (origEdge.getLabels().size() > 0) {
                // retrieve and sort the edge segments introduced for the original edge
                List<CrossHierarchyEdge> edgeSegments = new ArrayList<CrossHierarchyEdge>(
                        crossHierarchyMap.get(origEdge));
                Collections.sort(edgeSegments, new CrossHierarchyEdgeComparator(graph));
                
                // iterate over the labels and move them to the edge segments
                Iterator<LLabel> labelIterator = origEdge.getLabels().listIterator();
                while (labelIterator.hasNext()) {
                    LLabel currLabel = labelIterator.next();
                    
                    // find the index of the dummy edge we will move the label to
                    int targetDummyEdgeIndex = -1;
                    switch (currLabel.getProperty(LayoutOptions.EDGE_LABEL_PLACEMENT)) {
                    case HEAD:
                        targetDummyEdgeIndex = edgeSegments.size() - 1;
                        break;
                    
                    case CENTER:
                        targetDummyEdgeIndex = edgeSegments.size() / 2;
                        break;
                        
                    case TAIL:
                        targetDummyEdgeIndex = 0;
                        break;
                        
                    default:
                        // we have no idea what to do with the label, so ignore it    
                    }
                    
                    // move the label if we were lucky enough to find a new home for it
                    if (targetDummyEdgeIndex != -1) {
                        CrossHierarchyEdge targetSegment = edgeSegments.get(targetDummyEdgeIndex);
                        targetSegment.getEdge().getLabels().add(currLabel);
                        targetSegment.getEdge().getSource().getNode().getGraph().getProperty(
                                InternalProperties.GRAPH_PROPERTIES).add(GraphProperties.END_LABELS);
                        targetSegment.getEdge().getSource().getNode().getGraph().getProperty(
                                InternalProperties.GRAPH_PROPERTIES).add(GraphProperties.CENTER_LABELS);
                        
                        labelIterator.remove();
                        currLabel.setProperty(InternalProperties.ORIGINAL_LABEL_EDGE, origEdge);
                    }
                }
            }
            
            // remove original edge
            origEdge.setSource(null);
            origEdge.setTarget(null);
        }
        
        graph.setProperty(InternalProperties.CROSS_HIERARCHY_MAP, crossHierarchyMap);
        crossHierarchyMap = null;
        dummyNodeMap.clear();
        monitor.done();
    }
    
    /**
     * Recursively transform cross-hierarchy edges into sequences of dummy ports and dummy edges.
     * 
     * @param graph the layered graph
     * @param parentNode the parent node of the graph, or {@code null} if it is on top-level
     * @return the external ports created to split edges that cross the boundary of the parent node
     */
    private Collection<ExternalPort> transformHierarchyEdges(final LGraph graph,
            final LNode parentNode) {
        
        // process all children and gather their external ports
        List<ExternalPort> containedExternalPorts = new LinkedList<ExternalPort>();
        
        for (LNode node : graph.getLayerlessNodes()) {
            LGraph nestedGraph = node.getProperty(InternalProperties.NESTED_LGRAPH);
            if (nestedGraph != null) {
                // recursively process the child graph
                Collection<ExternalPort> childPorts = transformHierarchyEdges(nestedGraph, node);
                containedExternalPorts.addAll(childPorts);
                
                // make sure that all hierarchical ports have had dummy nodes created for them (some
                // will already have been created, but perhaps not all)
                if (nestedGraph.getProperty(InternalProperties.GRAPH_PROPERTIES).contains(
                        GraphProperties.EXTERNAL_PORTS)) {
                    
                    for (LPort port : node.getPorts()) {
                        if (dummyNodeMap.get(port) == null) {
                            LNode dummyNode = LGraphUtil.createExternalPortDummy(port,
                                    PortConstraints.FREE, PortSide.UNDEFINED, -port.getNetFlow(),
                                    null, null, port.getSize(),
                                    nestedGraph.getProperty(LayoutOptions.DIRECTION), nestedGraph);
                            dummyNode.setProperty(InternalProperties.ORIGIN, port);
                            dummyNodeMap.put(port, dummyNode);
                            nestedGraph.getLayerlessNodes().add(dummyNode);
                        }
                    }
                }
            }
        }
        
        // process the cross-hierarchy edges connected to the inside of the child nodes
        List<ExternalPort> exportedExternalPorts = new LinkedList<ExternalPort>();
        processInnerHierarchicalEdgeSegments(graph, parentNode, exportedExternalPorts,
                containedExternalPorts);
        
        // process the cross-hierarchy edges connected to the outside of the parent node
        if (parentNode != null) {
            processOuterHierarchicalEdgeSegments(graph, parentNode, exportedExternalPorts);
        }
        
        return exportedExternalPorts;
    }
    
    /**
     * Deals with the inner segments of hierarchical edges by breaking them between external ports.
     * For each external port contained in child nodes, this method adds appropriate new external ports
     * and / or dummy edges.
     * 
     * @param graph the graph whose child nodes have exposed external ports.
     * @param parentNode the graph's parent node, or {@code null} if the graph is at the top level.
     * @param exportedExternalPorts list that will be filled with the external ports this method
     *                              creates.
     * @param containedExternalPorts list of external ports exposed by the graph's child nodes.
     */
    private void processInnerHierarchicalEdgeSegments(final LGraph graph, final LNode parentNode,
            final List<ExternalPort> exportedExternalPorts,
            final List<ExternalPort> containedExternalPorts) {
        
        // we remember the ports and the dummy nodes we create to add them to the graph afterwards
        // (this is not strictly necessary, but allows us to reuse methods we also use for outer
        // hierarchy edge segments)
        List<ExternalPort> externalPorts = Lists.newLinkedList();
        
        // iterate over the list of contained external ports
        for (ExternalPort externalPort : containedExternalPorts) {
            ExternalPort currentExternalPort = null;
            
            // we process output ports and input ports a bit differently
            if (externalPort.type == PortType.OUTPUT) {
                // iterate over the port's original edges
                for (LEdge outEdge : externalPort.origEdges) {
                    /* at this point, we distinguish three cases:
                     *  1. The edge connects to a direct child of the parent node.
                     *  2. connects two external ports of direct children of the parent node.
                     *  3. The edge comes from a child node and either connects directly to the parent
                     *     node or leaves it.
                     */
                    LNode targetNode = outEdge.getTarget().getNode();
                    if (targetNode.getGraph() == graph) {
                        // case 1: edge connects to a direct chlid
                        connectChild(graph, externalPort, outEdge, externalPort.dummyPort,
                                outEdge.getTarget());
                    } else if (parentNode == null || LGraphUtil.isDescendant(targetNode, parentNode)) {
                        // case 2: edge connects two direct children
                        connectSiblings(graph, externalPort, containedExternalPorts, outEdge);
                    } else {
                        // case 3: edge connects to parent node or to the outside world
                        ExternalPort newExternalPort = introduceHierarchicalEdgeSegment(
                                graph,
                                parentNode,
                                outEdge,
                                externalPort.dummyPort,
                                PortType.OUTPUT,
                                currentExternalPort);
                        if (newExternalPort != currentExternalPort) {
                            externalPorts.add(newExternalPort);
                        }
                        
                        // the port is our new current external port if it is exported
                        if (newExternalPort.exported) {
                            currentExternalPort = newExternalPort;
                        }
                    }
                }
            } else {
                // iterate over the port's original edges
                for (LEdge inEdge : externalPort.origEdges) {
                    /* at this point, we distinguish three cases:
                     *  1. The edge comes from a direct child of the parent node.
                     *  2. connects two external ports of direct children of the parent node. (we don't
                     *     deal with that case here; the code that handles output ports above does that)
                     *  3. The edge comes from the parent node or from the outside.
                     */
                    LNode sourceNode = inEdge.getSource().getNode();
                    if (sourceNode.getGraph() == graph) {
                        // case 1: edge comes from a direct child
                        connectChild(graph, externalPort, inEdge, inEdge.getSource(),
                                externalPort.dummyPort);
                    } else if (parentNode == null || LGraphUtil.isDescendant(sourceNode, parentNode)) {
                        // case 2: edge connects two direct children; this case is handled in the code
                        //         for output ports above, so there's nothing to do here
                        continue;
                    } else {
                        // case 3: edge comes from the parent node or from the outside
                        ExternalPort newExternalPort = introduceHierarchicalEdgeSegment(
                                graph,
                                parentNode,
                                inEdge,
                                externalPort.dummyPort,
                                PortType.INPUT,
                                currentExternalPort);
                        if (newExternalPort != currentExternalPort) {
                            externalPorts.add(newExternalPort);
                        }
                        
                        // the port is our new current external port if it is exported
                        if (newExternalPort.exported) {
                            currentExternalPort = newExternalPort;
                        }
                    }
                }
            }
        }
        
        // add dummy nodes and exported external ports
        for (ExternalPort externalPort : externalPorts) {
            if (!graph.getLayerlessNodes().contains(externalPort.dummyNode)) {
                graph.getLayerlessNodes().add(externalPort.dummyNode);
            }
            
            if (externalPort.exported) {
                exportedExternalPorts.add(externalPort);
            }
        }
    }

    /**
     * Connects an external port with a child node of the given graph. To this end, a new dummy edge
     * is inserted and associated with the original hierarchy-crossing edge in the cross hierarchy map.
     * 
     * @param graph the graph whose child to connect.
     * @param externalPort the external port that provides the other end of the connection.
     * @param origEdge the original hierarchy-crossing edge.
     * @param sourcePort the source port the edge shall be connected to.
     * @param targetPort the target port the edge shall be connected to.
     */
    private void connectChild(final LGraph graph, final ExternalPort externalPort, final LEdge origEdge,
            final LPort sourcePort, final LPort targetPort) {
        
        // add new dummy edge and connect properly
        LEdge dummyEdge = createDummyEdge(graph, origEdge);
        dummyEdge.setSource(sourcePort);
        dummyEdge.setTarget(targetPort);
        
        crossHierarchyMap.put(origEdge,
                new CrossHierarchyEdge(dummyEdge, graph, externalPort.type));
    }

    /**
     * Connects external ports of two child nodes of the given graph. To this end, the provided list of
     * external ports is searched for the counterpart of the provided external output port, and a new
     * dummy edge is created to connect the two. The dummy edge is associated with the original
     * hierarchy-crossing edge in the cross hierarchy map.
     * 
     * @param graph the graph whose child nodes to connect.
     * @param externalOutputPort the external output port. 
     * @param containedExternalPorts list of external ports exposed by children of the graph. This list
     *                               is searched for the external target port.
     * @param origEdge the original edge that is being broken.
     */
    private void connectSiblings(final LGraph graph, final ExternalPort externalOutputPort,
            final List<ExternalPort> containedExternalPorts, final LEdge origEdge) {
        
        // find the opposite external port
        ExternalPort targetExternalPort = null;
        for (ExternalPort externalPort2 : containedExternalPorts) {
            if (externalPort2 != externalOutputPort && externalPort2.origEdges.contains(origEdge)) {
                targetExternalPort = externalPort2;
                break;
            }
        }
        assert targetExternalPort.type == PortType.INPUT;
        
        // add new dummy edge and connect properly
        LEdge dummyEdge = createDummyEdge(graph, origEdge);
        dummyEdge.setSource(externalOutputPort.dummyPort);
        dummyEdge.setTarget(targetExternalPort.dummyPort);
        
        crossHierarchyMap.put(origEdge,
                new CrossHierarchyEdge(dummyEdge, graph, externalOutputPort.type));
    }
    
    /**
     * Deals with the outer segments of hierarchical edges by breaking them at their source or target.
     * For each hierarchical edge that starts or ends at one of the graph's children, this method adds
     * appropriate new external ports and / or dummy edges.
     * 
     * @param graph the graph whose child nodes have exposed external ports.
     * @param parentNode the graph's parent node, or {@code null} if the graph is at the top level.
     * @param exportedExternalPorts list that will be filled with the external ports this method
     *                              creates.
     */
    private void processOuterHierarchicalEdgeSegments(final LGraph graph, final LNode parentNode,
            final List<ExternalPort> exportedExternalPorts) {
        
        // we need to remember the ports and the dummy nodes we create to add them to the graph
        // afterwards (to avoid concurrent modification exceptions)
        List<ExternalPort> externalPorts = Lists.newLinkedList();
        
        // iterate over all ports of the graph's child nodes
        for (LNode childNode : graph.getLayerlessNodes()) {
            for (LPort childPort : childNode.getPorts()) {
                // we treat outgoing and incoming edges separately
                ExternalPort currentExternalOutputPort = null;
                for (LEdge outEdge : childPort.getOutgoingEdges().toArray(new LEdge[0])) {
                    if (!LGraphUtil.isDescendant(outEdge.getTarget().getNode(), parentNode)) {
                        // the edge goes to the outside or to the parent node itself, so create an
                        // external port if necessary and introduce a new dummy edge
                        ExternalPort newExternalPort = introduceHierarchicalEdgeSegment(
                                graph,
                                parentNode,
                                outEdge,
                                outEdge.getSource(),
                                PortType.OUTPUT,
                                currentExternalOutputPort);
                        if (newExternalPort != currentExternalOutputPort) {
                            externalPorts.add(newExternalPort);
                        }
                        
                        // the port is our new current external port if it is exported
                        if (newExternalPort.exported) {
                            currentExternalOutputPort = newExternalPort;
                        }
                    }
                }

                ExternalPort currentExternalInputPort = null;
                for (LEdge inEdge : childPort.getIncomingEdges().toArray(new LEdge[0])) {
                    if (!LGraphUtil.isDescendant(inEdge.getSource().getNode(), parentNode)) {
                        // the edge comes from the outside or from the parent node itself, so create an
                        // external port if necessary and introduce a new dummy edge
                        ExternalPort newExternalPort = introduceHierarchicalEdgeSegment(
                                graph,
                                parentNode,
                                inEdge,
                                inEdge.getTarget(),
                                PortType.INPUT,
                                currentExternalInputPort);
                        if (newExternalPort != currentExternalInputPort) {
                            externalPorts.add(newExternalPort);
                        }
                        
                        // the port is our new current external port if it is exported
                        if (newExternalPort.exported) {
                            currentExternalInputPort = newExternalPort;
                        }
                    }
                }
            }
        }
        
        // add dummy nodes and exported external ports
        for (ExternalPort externalPort : externalPorts) {
            if (!graph.getLayerlessNodes().contains(externalPort.dummyNode)) {
                graph.getLayerlessNodes().add(externalPort.dummyNode);
            }
            
            if (externalPort.exported) {
                exportedExternalPorts.add(externalPort);
            }
        }
    }
    
    /**
     * Does the actual work of creating a new hierarchical edge segment between an external port and a
     * given opposite port. The external port used for the segment is returned. This method does not
     * put any created edges into the cross hierarchy map!
     * 
     * <p>The method first decides on an external port to use for the segment. If the default external
     * port passed to the method is not {@code null} and if external ports are to be merged in the
     * current graph, the default external port is reused. An exception are segments that start or end
     * in the parent node; each such segments gets its own external port.</p>
     * 
     * <p>If a new external port is created, the method also creates a dummy node for it as well as an
     * actual port on the parent node, if no such port already exists, as well as a dummy edge for the
     * connection. Thus, the newly created external port has everything it needs to be properly
     * represented and initialized.</p>
     * 
     * <p>The original edge is added to the list of original edges in the external port used for the
     * segment. The dummy edge is associated with the original hierarchy-crossing edge in the cross
     * hierarchy map.</p>
     * 
     * @param graph the layered graph.
     * @param parentNode the graph's parent node, or {@code null} if the graph is at the top level.
     * @param origEdge the hierarchy-crossing edge that is being broken.
     * @param oppositePort the port that will be one of the two end points of the new segment.
     * @param portType the type of the port to create as one of the segment's edge points.
     * @param defaultExternalPort a default external port we can reuse if external ports should be
     *                            merged. If this is {@code null}, a new external port is always created.
     *                            If this port is reused, it is returned by this method.
     * @return the external port (created or reused) and used as one endpoint of the connection.
     */
    private ExternalPort introduceHierarchicalEdgeSegment(final LGraph graph, final LNode parentNode,
            final LEdge origEdge, final LPort oppositePort, final PortType portType,
            final ExternalPort defaultExternalPort) {
        
        // check if external ports are to be merged
        boolean mergeExternalPorts = graph.getProperty(Properties.MERGE_HIERARCHICAL_PORTS);
        
        // check if the edge connects to the parent node instead of to the outside world
        boolean connectsToParent = false;
        if (portType == PortType.INPUT) {
            connectsToParent = origEdge.getSource().getNode() == parentNode;
        } else {
            connectsToParent = origEdge.getTarget().getNode() == parentNode;
        }
        
        // only create a new external port if the current one is null or if ports are not to be merged
        // or if the connection actually ends at the parent node
        ExternalPort externalPort = defaultExternalPort;
        if (externalPort == null || !mergeExternalPorts || connectsToParent) {
            // create a dummy node that will represent the external port
            LNode dummyNode = createExternalPortDummy(graph, parentNode, portType, origEdge);
            
            // create a dummy edge to be connected to the port
            LEdge dummyEdge = createDummyEdge(parentNode.getGraph(), origEdge);

            if (portType == PortType.INPUT) {
                // if the external port is an input port, the source of the edge must be connected to
                // the new dummy node
                dummyEdge.setSource(dummyNode.getPorts().get(0));
                dummyEdge.setTarget(oppositePort);
            } else {
                // if the external port is an output port, the target of the edge must be connected to
                // the new dummy node
                dummyEdge.setSource(oppositePort);
                dummyEdge.setTarget(dummyNode.getPorts().get(0));
            }
            
            // create the external port (the port is to be exported if the connection is not just to the
            // parent node)
            externalPort = new ExternalPort(origEdge, dummyEdge, dummyNode,
                    (LPort) dummyNode.getProperty(InternalProperties.ORIGIN), portType,
                    !connectsToParent);
        } else {
            // we use an existing external port, so simply add the original edge to its list of
            // original edges
            externalPort.origEdges.add(origEdge);
            
            // merge the properties of the original edges
            float thickness = Math.max(externalPort.newEdge.getProperty(LayoutOptions.THICKNESS),
                    origEdge.getProperty(LayoutOptions.THICKNESS));
            externalPort.newEdge.setProperty(LayoutOptions.THICKNESS, thickness);
        }

        crossHierarchyMap.put(origEdge,
                new CrossHierarchyEdge(externalPort.newEdge, graph, portType));
        
        return externalPort;
    }
    
    /**
     * Creates and initializes a new dummy edge for the given original hierarchy-crossing edge. All that
     * remains to be done afterwards is to properly connect the edge. Nice!
     * 
     * @param graph the graph the edge will be placed in.
     * @param origEdge the original hierarchy-crossing edge.
     * @return a new dummy edge.
     */
    private LEdge createDummyEdge(final LGraph graph, final LEdge origEdge) {
        LEdge dummyEdge = new LEdge(graph);
        dummyEdge.copyProperties(origEdge);
        dummyEdge.setProperty(LayoutOptions.JUNCTION_POINTS, null);
        return dummyEdge;
    }
    
    /**
     * Retrieves a dummy node to be used to represent a new external port of the parent node and to
     * connect a new segment of the given hierarchical edge to. A proper dummy node might already have
     * been created; if so, that one is returned.
     * 
     * @param graph the graph.
     * @param parentNode the graph's parent node.
     * @param portType the type of the new external port.
     * @param edge the edge that will be connected to the external port.
     * @return an appropriate external port dummy.
     */
    private LNode createExternalPortDummy(final LGraph graph, final LNode parentNode,
            final PortType portType, final LEdge edge) {
        
        LNode dummyNode = null;
        
        // find the port on the outside of its parent node that the edge connects to
        LPort outsidePort = portType == PortType.INPUT ? edge.getSource() : edge.getTarget();
        Direction layoutDirection = LGraphUtil.getDirection(graph);
        
        // check if the edge connects to the parent node or to something way outside...
        if (outsidePort.getNode() == parentNode) {
            // we need to check if a dummy node has already been created for the port
            dummyNode = dummyNodeMap.get(outsidePort);
            if (dummyNode == null) {
                dummyNode = LGraphUtil.createExternalPortDummy(
                        outsidePort,
                        PortConstraints.FREE,
                        PortSide.UNDEFINED,
                        portType == PortType.INPUT ? -1 : 1,
                        null,
                        null,
                        outsidePort.getSize(),
                        layoutDirection,
                        graph
                );
                dummyNode.setProperty(InternalProperties.ORIGIN, outsidePort);
                dummyNodeMap.put(outsidePort, dummyNode);
            }
        } else {
            // we create a new dummy node in any case, and since there is no port yet we have to
            // create one as well
            float thickness = edge.getProperty(LayoutOptions.THICKNESS);
            dummyNode = LGraphUtil.createExternalPortDummy(
                    getExternalPortProperties(graph),
                    PortConstraints.FREE,
                    PortSide.UNDEFINED,
                    portType == PortType.INPUT ? -1 : 1,
                    null,
                    null,
                    new KVector(thickness, thickness),
                    layoutDirection,
                    graph
            );
            LPort dummyPort = createPortForDummy(dummyNode, parentNode, portType);
            dummyNode.setProperty(InternalProperties.ORIGIN, dummyPort);
            dummyNodeMap.put(dummyPort, dummyNode);
        }
        
        // set a few graph properties
        graph.getProperty(InternalProperties.GRAPH_PROPERTIES).add(GraphProperties.EXTERNAL_PORTS);
        if (graph.getProperty(LayoutOptions.PORT_CONSTRAINTS).isSideFixed()) {
            graph.setProperty(LayoutOptions.PORT_CONSTRAINTS, PortConstraints.FIXED_SIDE);
        } else {
            graph.setProperty(LayoutOptions.PORT_CONSTRAINTS, PortConstraints.FREE);
        }
        
        return dummyNode;
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
        propertyHolder.setProperty(InternalProperties.OFFSET, offset);
        return propertyHolder;
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
        Direction layoutDirection = LGraphUtil.getDirection(graph);
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
        port.setProperty(InternalProperties.OFFSET, dummyNode.getProperty(InternalProperties.OFFSET));
        dummyNode.setProperty(InternalProperties.ORIGIN, port);
        dummyNodeMap.put(port, dummyNode);
        return port;
    }

}
