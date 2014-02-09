/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 * 
 * Copyright 2014 by
 * + Christian-Albrechts-University of Kiel
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 * 
 * This code is provided under the terms of the Eclipse Public License (EPL).
 * See the file epl-v10.html for the license text.
 */
package de.cau.cs.kieler.klay.layered.importexport;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;

import de.cau.cs.kieler.core.math.KVector;
import de.cau.cs.kieler.core.properties.IPropertyHolder;
import de.cau.cs.kieler.kiml.options.Direction;
import de.cau.cs.kieler.kiml.options.LayoutOptions;
import de.cau.cs.kieler.kiml.options.PortConstraints;
import de.cau.cs.kieler.kiml.options.PortSide;
import de.cau.cs.kieler.klay.layered.graph.LEdge;
import de.cau.cs.kieler.klay.layered.graph.LGraph;
import de.cau.cs.kieler.klay.layered.graph.LInsets;
import de.cau.cs.kieler.klay.layered.graph.LLabel;
import de.cau.cs.kieler.klay.layered.graph.LNode;
import de.cau.cs.kieler.klay.layered.graph.LPort;
import de.cau.cs.kieler.klay.layered.properties.EdgeConstraint;
import de.cau.cs.kieler.klay.layered.properties.GraphProperties;
import de.cau.cs.kieler.klay.layered.properties.InLayerConstraint;
import de.cau.cs.kieler.klay.layered.properties.LayerConstraint;
import de.cau.cs.kieler.klay.layered.properties.NodeType;
import de.cau.cs.kieler.klay.layered.properties.PortType;
import de.cau.cs.kieler.klay.layered.properties.Properties;

/**
 * Utility class for importing graphs into the {@link LGraph} format.
 * 
 * @author msp
 */
public final class ImportUtil {
    
    /**
     * Hidden constructor to avoid instantiation.
     */
    private ImportUtil() { }

    
    ///////////////////////////////////////////////////////////////////////////////
    // Graph Properties
    
    /**
     * Compute the graph properties of the given layered graph. These properties are important
     * to determine which intermediate processors are included in the layout run.
     * Ideally the properties are computed during the import of the source format into {@link LGraph},
     * e.g. as done in {@code KGraphImporter}. This method is offered only for convenience.
     * <p>
     * The nodes are expected to be in the {@link LGraph#getLayerlessNodes()} list.
     * </p>
     * 
     * @param layeredGraph a layered graph
     */
    public static void computeGraphProperties(final LGraph layeredGraph) {
        Set<GraphProperties> props = layeredGraph.getProperty(Properties.GRAPH_PROPERTIES);
        if (!props.isEmpty()) {
            props.clear();
        }
        
        Direction direction = layeredGraph.getProperty(LayoutOptions.DIRECTION);
        for (LNode node : layeredGraph.getLayerlessNodes()) {
            if (node.getProperty(LayoutOptions.COMMENT_BOX)) {
                props.add(GraphProperties.COMMENTS);
            } else if (node.getProperty(LayoutOptions.HYPERNODE)) {
                props.add(GraphProperties.HYPERNODES);
                props.add(GraphProperties.HYPEREDGES);
            } else if (node.getProperty(Properties.NODE_TYPE) == NodeType.EXTERNAL_PORT) {
                props.add(GraphProperties.EXTERNAL_PORTS);
            }
            
            PortConstraints portConstraints = node.getProperty(LayoutOptions.PORT_CONSTRAINTS);
            if (portConstraints == PortConstraints.UNDEFINED) {
                // correct the port constraints value
                node.setProperty(LayoutOptions.PORT_CONSTRAINTS, PortConstraints.FREE);
            } else if (portConstraints != PortConstraints.FREE) {
                props.add(GraphProperties.NON_FREE_PORTS);
            }
            
            for (LPort port : node.getPorts()) {
                if (port.getIncomingEdges().size() + port.getOutgoingEdges().size() > 1) {
                    props.add(GraphProperties.HYPEREDGES);
                }
                
                PortSide portSide = port.getSide();
                switch (direction) {
                case UP:
                case DOWN:
                    if (portSide == PortSide.EAST || portSide == PortSide.WEST) {
                        props.add(GraphProperties.NORTH_SOUTH_PORTS);
                    }
                    break;
                default:
                    if (portSide == PortSide.NORTH || portSide == PortSide.SOUTH) {
                        props.add(GraphProperties.NORTH_SOUTH_PORTS);
                    }
                }
                
                for (LEdge edge : port.getOutgoingEdges()) {
                    if (edge.getTarget().getNode() == node) {
                        props.add(GraphProperties.SELF_LOOPS);
                    }
                    
                    for (LLabel label : edge.getLabels()) {
                        switch (label.getProperty(LayoutOptions.EDGE_LABEL_PLACEMENT)) {
                        case CENTER:
                            props.add(GraphProperties.CENTER_LABELS);
                            break;
                        case HEAD:
                        case TAIL:
                            props.add(GraphProperties.END_LABELS);
                            break;
                        }
                    }
                }
            }
        }
    }
    
    
    ///////////////////////////////////////////////////////////////////////////////
    // Handling of Ports

    /**
     * Create a port for an edge that is not connected to a port. This is necessary because KLay
     * Layered wants all edges to have a source port and a target port. The port side is computed
     * from the given absolute end point position of the edge.
     * 
     * @param node
     *            the node at which the edge is incident
     * @param endPoint
     *            the absolute point where the edge ends, or {@code null} if unknown
     * @param type
     *            the port type
     * @param layeredGraph
     *            the layered graph
     * @return a new port
     */
    public static LPort createPort(final LNode node, final KVector endPoint, final PortType type,
            final LGraph layeredGraph) {
        LPort port;
        Direction direction = layeredGraph.getProperty(LayoutOptions.DIRECTION);
        if (direction == Direction.UNDEFINED) {
            // The default direction is right
            direction = Direction.RIGHT;
        }
        boolean mergePorts = layeredGraph.getProperty(Properties.MERGE_PORTS);
        
        if ((mergePorts || node.getProperty(LayoutOptions.HYPERNODE))
                && !node.getProperty(LayoutOptions.PORT_CONSTRAINTS).isSideFixed()) {
            
            // Hypernodes have one output port and one input port
            final PortSide defaultSide = PortSide.fromDirection(direction);
            port = provideCollectorPort(layeredGraph, node, type,
                    type == PortType.OUTPUT ? defaultSide : defaultSide.opposed());
        } else {
            port = new LPort(layeredGraph);
            port.setNode(node);
            
            if (endPoint != null) {
                KVector pos = port.getPosition();
                pos.x = endPoint.x - node.getPosition().x;
                pos.y = endPoint.y - node.getPosition().y;
                pos.applyBounds(0, 0, node.getSize().x, node.getSize().y);
            }
            
            PortSide portSide = calcPortSide(port, direction);
            port.setSide(portSide);
            Set<GraphProperties> graphProperties = layeredGraph.getProperty(
                    Properties.GRAPH_PROPERTIES);
            switch (direction) {
            case LEFT:
            case RIGHT:
                if (portSide == PortSide.NORTH || portSide == PortSide.SOUTH) {
                    graphProperties.add(GraphProperties.NORTH_SOUTH_PORTS);
                }
                break;
            case UP:
            case DOWN:
                if (portSide == PortSide.EAST || portSide == PortSide.WEST) {
                    graphProperties.add(GraphProperties.NORTH_SOUTH_PORTS);
                }
                break;
            }
        }
        
        return port;
    }
    
    /**
     * Determine the port side for the given port from its relative position at
     * its corresponding node.
     * 
     * @param port port to analyze
     * @param direction the overall layout direction
     * @return the port side relative to its containing node
     */
    static PortSide calcPortSide(final LPort port, final Direction direction) {
        LNode node = port.getNode();
        // if the node has zero size, we cannot decide anything
        double nodeWidth = node.getSize().x;
        double nodeHeight = node.getSize().y;
        if (nodeWidth <= 0 && nodeHeight <= 0) {
            return PortSide.UNDEFINED;
        }

        // check direction-dependent criterion
        double xpos = port.getPosition().x;
        double ypos = port.getPosition().y;
        double width = port.getSize().x;
        double height = port.getSize().y;
        switch (direction) {
        case LEFT:
        case RIGHT:
            if (xpos < 0) {
                return PortSide.WEST;
            } else if (xpos + width > nodeWidth) {
                return PortSide.EAST;
            }
            break;
        case UP:
        case DOWN:
            if (ypos < 0) {
                return PortSide.NORTH;
            } else if (ypos + height > nodeHeight) {
                return PortSide.SOUTH;
            }
        }
        
        // check general criterion
        double widthPercent = (xpos + width / 2) / nodeWidth;
        double heightPercent = (ypos + height / 2) / nodeHeight;
        if (widthPercent + heightPercent <= 1
                && widthPercent - heightPercent <= 0) {
            // port is on the left
            return PortSide.WEST;
        } else if (widthPercent + heightPercent >= 1
                && widthPercent - heightPercent >= 0) {
            // port is on the right
            return PortSide.EAST;
        } else if (heightPercent < 1.0f / 2) {
            // port is on the top
            return PortSide.NORTH;
        } else {
            // port is on the bottom
            return PortSide.SOUTH;
        }
    }
    
    /**
     * Compute the offset for a port, that is the amount by which it is moved outside of the node.
     * An offset value of 0 means the port has no intersection with the node and touches the outside
     * border of the node.
     * 
     * @param port a port
     * @param side the side on the node for the given port
     * @return the offset on the side
     */
    static float calcPortOffset(final LPort port, final PortSide side) {
        LNode node = port.getNode();
        switch (side) {
        case NORTH:
            return (float) -(port.getPosition().y + port.getSize().y);
        case EAST:
            return (float) (port.getPosition().x - node.getSize().x);
        case SOUTH:
            return (float) (port.getPosition().y - node.getSize().y);
        case WEST:
            return (float) -(port.getPosition().x + port.getSize().x);
        }
        return 0;
    }

    /**
     * Center the given point on one side of a boundary.
     * 
     * @param point
     *            a point to change
     * @param boundary
     *            the boundary to use for centering
     * @param side
     *            the side of the boundary
     */
    static void centerPoint(final KVector point, final KVector boundary,
            final PortSide side) {
        
        switch (side) {
        case NORTH:
            point.x = boundary.x / 2;
            point.y = 0;
            break;
        case EAST:
            point.x = boundary.x;
            point.y = boundary.y / 2;
            break;
        case SOUTH:
            point.x = boundary.x / 2;
            point.y = boundary.y;
            break;
        case WEST:
            point.x = 0;
            point.y = boundary.y / 2;
            break;
        }
    }

    /**
     * Return a collector port of given type, creating it if necessary. A collector port is used to
     * merge all incident edges that originally had no ports.
     * 
     * @param layeredGraph
     *            the layered graph
     * @param node
     *            a node
     * @param type
     *            if {@code INPUT}, an input collector port is returned; if {@code OUTPUT}, an
     *            output collector port is returned
     * @param side
     *            the side to set for a newly created port
     * @return a collector port
     */
    public static LPort provideCollectorPort(final LGraph layeredGraph,
            final LNode node, final PortType type, final PortSide side) {
        
        LPort port = null;
        switch (type) {
        case INPUT:
            for (LPort inport : node.getPorts()) {
                if (inport.getProperty(Properties.INPUT_COLLECT)) {
                    return inport;
                }
            }
            port = new LPort(layeredGraph);
            port.setProperty(Properties.INPUT_COLLECT, true);
            break;
        case OUTPUT:
            for (LPort outport : node.getPorts()) {
                if (outport.getProperty(Properties.OUTPUT_COLLECT)) {
                    return outport;
                }
            }
            port = new LPort(layeredGraph);
            port.setProperty(Properties.OUTPUT_COLLECT, true);
            break;
        }
        if (port != null) {
            port.setNode(node);
            port.setSide(side);
            centerPoint(port.getPosition(), node.getSize(), side);
        }
        return port;
    }
    
    /**
     * Initialize the side, offset, and anchor point of the given port. The port is assumed to
     * be also present in the original graph structure. The port's current position and the size
     * of the corresponding node are used to determine missing values.
     * 
     * @param port a port
     * @param portConstraints the port constraints of the containing node
     * @param direction the overall layout direction
     * @param anchorPos the anchor position, or {@code null} if it shall be determined automatically
     */
    public static void initializePort(final LPort port, final PortConstraints portConstraints,
            final Direction direction, final KVector anchorPos) {
        PortSide portSide = port.getSide();
        
        if (portSide == PortSide.UNDEFINED && portConstraints.isSideFixed()) {
            // calculate the port side and offset from the port's current position
            portSide = calcPortSide(port, direction);
            port.setSide(portSide);
            
            // if port coordinates are (0,0), we default to port offset 0 to make the common case
            // frustration-free
            if (port.getProperty(LayoutOptions.OFFSET) == null && portSide != PortSide.UNDEFINED
                    && (port.getPosition().x != 0 || port.getPosition().y != 0)) {
                port.setProperty(LayoutOptions.OFFSET, calcPortOffset(port, portSide));
            }
        }
        
        // if the port constraints are set to fixed ratio, remember the current ratio
        if (portConstraints.isRatioFixed()) {
            double ratio = 0.0;
            
            switch (portSide) {
            case NORTH:
            case SOUTH:
                double nodeWidth = port.getNode().getSize().x;
                if (nodeWidth > 0) {
                    ratio = port.getPosition().x / nodeWidth;
                }
                break;
            case EAST:
            case WEST:
                double nodeHeight = port.getNode().getSize().y;
                if (nodeHeight > 0) {
                    ratio = port.getPosition().y / nodeHeight;
                }
                break;
            }
            
            port.setProperty(Properties.PORT_RATIO_OR_POSITION, ratio);
        }

        KVector portSize = port.getSize();
        // if the port anchor property is set, use it as anchor point
        if (anchorPos != null) {
            port.getAnchor().x = anchorPos.x;
            port.getAnchor().y = anchorPos.y;
        } else if (portConstraints.isSideFixed() && portSide != PortSide.UNDEFINED) {
            // set the anchor point according to the port side
            switch (portSide) {
            case NORTH:
                port.getAnchor().x = portSize.x / 2;
                break;
            case EAST:
                port.getAnchor().x = portSize.x;
                port.getAnchor().y = portSize.y / 2;
                break;
            case SOUTH:
                port.getAnchor().x = portSize.x / 2;
                port.getAnchor().y = portSize.y;
                break;
            case WEST:
                port.getAnchor().y = portSize.y / 2;
                break;
            }
        } else {
            // the port side will be decided later, so set the anchor to the center point
            port.getAnchor().x = portSize.x / 2;
            port.getAnchor().y = portSize.y / 2;
        }
    }
    
    
    ///////////////////////////////////////////////////////////////////////////////
    // External Ports  (Ports on the boundary of the parent node)
    
    /**
     * For free port constraints, this map maps port types and layout directions to the port side we
     * will use for an external port.
     */
    private static final Map<PortType, Map<Direction, PortSide>> EXTERNAL_PORT_SIDE_MAP =
            Maps.newEnumMap(PortType.class);
    
    // Initialize the external port side map.
    static {
        Map<Direction, PortSide> inputPortMap = Maps.newEnumMap(Direction.class);
        inputPortMap.put(Direction.RIGHT, PortSide.WEST);
        inputPortMap.put(Direction.LEFT, PortSide.EAST);
        inputPortMap.put(Direction.DOWN, PortSide.NORTH);
        inputPortMap.put(Direction.UP, PortSide.SOUTH);
        EXTERNAL_PORT_SIDE_MAP.put(PortType.INPUT, inputPortMap);
        
        Map<Direction, PortSide> outputPortMap = Maps.newEnumMap(Direction.class);
        outputPortMap.put(Direction.RIGHT, PortSide.EAST);
        outputPortMap.put(Direction.LEFT, PortSide.WEST);
        outputPortMap.put(Direction.DOWN, PortSide.SOUTH);
        outputPortMap.put(Direction.UP, PortSide.NORTH);
        EXTERNAL_PORT_SIDE_MAP.put(PortType.OUTPUT, outputPortMap);
    }
    
    /**
     * Creates a dummy for an external port. The dummy will have just one port. The port is on
     * the eastern side for western external ports, on the western side for eastern external ports,
     * on the southern side for northern external ports, and on the northern side for southern
     * external ports.
     * 
     * <p>The returned dummy node is decorated with some properties:</p>
     * <ul>
     *   <li>Its {@link Properties#NODE_TYPE} is set to {@link NodeType#EXTERNAL_PORT}.</li>
     *   <li>Its {@link Properties#ORIGIN} is set to the external port object.</li>
     *   <li>The {@link LayoutOptions#PORT_CONSTRAINTS} are set to
     *     {@link PortConstraints#FIXED_POS}.</li>
     *   <li>For western and eastern port dummies, the {@link Properties#LAYER_CONSTRAINT} is set to
     *     {@link LayerConstraint#FIRST_SEPARATE} and {@link LayerConstraint#LAST_SEPARATE},
     *     respectively.</li>
     *   <li>For northern and southern port dummies, the {@link Properties#IN_LAYER_CONSTRAINT} is set to
     *     {@link InLayerConstraint#TOP} and {@link InLayerConstraint#BOTTOM}, respectively.</li>
     *   <li>For eastern dummies, the {@link Properties#EDGE_CONSTRAINT} is set to
     *     {@link EdgeConstraint#OUTGOING_ONLY}; for western dummies, it is set to
     *     {@link EdgeConstraint#INCOMING_ONLY}; for all other dummies, it is left unset.</li>
     *   <li>{@link Properties#EXT_PORT_SIDE} is set to the side of the external port represented.</li>
     *   <li>If the port constraints of the original port's node are set to
     *     {@link PortConstraints#FIXED_RATIO} or {@link PortConstraints#FIXED_POS}, the dummy node's
     *     {@link Properties#PORT_RATIO_OR_POSITION} property is set to the port's original position,
     *     defined relative to the original node's origin. (as opposed to relative to the node's content
     *     area)</li>
     *   <li>The {@link Properties#EXT_PORT_SIZE} property is set to the size of the external port the
     *     the dummy represents, while the size of the dummy itself is set to {@code (0, 0)}.</li>
     * </ul>
     * 
     * <p>The layout direction of a graph has implications on the side external ports are placed at.
     * If port constraints imply fixed sides for ports, the {@link Properties#EXT_PORT_SIDE} property is
     * set to whatever the external port's port side is. If the port side needs to be determined, it
     * depends on the port type (input port or output port; determined by the number of incoming and
     * outgoing edges) and on the layout direction as follows:</p>
     * <table>
     *   <tr><th></th>     <th>Input port</th><th>Output port</th></tr>
     *   <tr><th>Right</th><td>WEST</td>      <td>EAST</td></tr>
     *   <tr><th>Left</th> <td>EAST</td>      <td>WEST</td></tr>
     *   <tr><th>Down</th> <td>NORTH</td>     <td>SOUTH</td></tr>
     *   <tr><th>Up</th>   <td>SOUTH</td>     <td>NORTH</td></tr>
     * </table>
     * 
     * @param propertyHolder property holder for layout options that are set on the original port
     * @param portConstraints constraints for external ports.
     * @param portSide the side of the external port.
     * @param netFlow the number of incoming minus the number of outgoing edges.
     * @param portNodeSize the size of the node the port belongs to. Only relevant if the port
     *                     constraints are {@code FIXED_RATIO}.
     * @param portPosition the current port position. Only relevant if the port constraints are
     *                     {@code FIXED_ORDER}, {@code FIXED_RATIO} or {@code FIXED_POSITION}.
     * @param portSize size of the port. Depending on the port's side, the created dummy will
     *                 have the same width or height as the port, with the other dimension set
     *                 to zero.
     * @param layoutDirection layout direction of the node that owns the port.
     * @param layeredGraph the layered graph
     * @return a dummy node representing the external port.
     */
    public static LNode createExternalPortDummy(final IPropertyHolder propertyHolder,
            final PortConstraints portConstraints, final PortSide portSide, final int netFlow,
            final KVector portNodeSize, final KVector portPosition, final KVector portSize,
            final Direction layoutDirection, final LGraph layeredGraph) {
        
        PortSide finalExternalPortSide = portSide;
        
        // Create the dummy with one port
        LNode dummy = new LNode(layeredGraph);
        dummy.setProperty(Properties.NODE_TYPE, NodeType.EXTERNAL_PORT);
        dummy.setProperty(Properties.EXT_PORT_SIZE, portSize);
        dummy.setProperty(LayoutOptions.PORT_CONSTRAINTS, PortConstraints.FIXED_POS);
        dummy.setProperty(Properties.OFFSET, propertyHolder.getProperty(LayoutOptions.OFFSET));
        
        // set the anchor point
        KVector anchor = propertyHolder.getProperty(Properties.PORT_ANCHOR);
        if (anchor == null) {
            anchor = new KVector(portSize.x / 2, portSize.y / 2);
        }
        dummy.setProperty(Properties.PORT_ANCHOR, anchor);
        
        LPort dummyPort = new LPort(layeredGraph);
        dummyPort.setNode(dummy);
        
        // If the port constraints are free, we need to determine where to put the dummy (and its port)
        if (!portConstraints.isSideFixed()) {
            if (netFlow > 0) {
                finalExternalPortSide = EXTERNAL_PORT_SIDE_MAP.get(PortType.OUTPUT).get(layoutDirection);
            } else {
                finalExternalPortSide = EXTERNAL_PORT_SIDE_MAP.get(PortType.INPUT).get(layoutDirection);
            }
            propertyHolder.setProperty(LayoutOptions.PORT_SIDE, finalExternalPortSide);
        }
        
        // With the port side at hand, set the necessary properties and place the dummy's port
        // at the dummy's center
        switch (finalExternalPortSide) {
        case WEST:
            dummy.setProperty(Properties.LAYER_CONSTRAINT, LayerConstraint.FIRST_SEPARATE);
            dummy.setProperty(Properties.EDGE_CONSTRAINT, EdgeConstraint.OUTGOING_ONLY);
            dummy.getSize().y = portSize.y;
            dummyPort.setSide(PortSide.EAST);
            dummyPort.getPosition().y = anchor.y;
            break;
        
        case EAST:
            dummy.setProperty(Properties.LAYER_CONSTRAINT, LayerConstraint.LAST_SEPARATE);
            dummy.setProperty(Properties.EDGE_CONSTRAINT, EdgeConstraint.INCOMING_ONLY);
            dummy.getSize().y = portSize.y;
            dummyPort.setSide(PortSide.WEST);
            dummyPort.getPosition().y = anchor.y;
            break;
        
        case NORTH:
            dummy.setProperty(Properties.IN_LAYER_CONSTRAINT, InLayerConstraint.TOP);
            dummy.getSize().x = portSize.x;
            dummyPort.setSide(PortSide.SOUTH);
            dummyPort.getPosition().x = anchor.x;
            break;
        
        case SOUTH:
            dummy.setProperty(Properties.IN_LAYER_CONSTRAINT, InLayerConstraint.BOTTOM);
            dummy.getSize().x = portSize.x;
            dummyPort.setSide(PortSide.NORTH);
            dummyPort.getPosition().x = anchor.x;
            break;
        
        default:
            // Should never happen!
            assert false : finalExternalPortSide;
        }
        
        // From FIXED_ORDER onwards, we need to save the port position or ratio
        if (portConstraints.isOrderFixed()) {
            double positionOrRatio = 0;
            
            switch (finalExternalPortSide) {
            case WEST:
            case EAST:
                positionOrRatio = portPosition.y;
                if (portConstraints.isRatioFixed()) {
                    positionOrRatio /= portNodeSize.y;
                }
                
                break;
                
            case NORTH:
            case SOUTH:
                positionOrRatio = portPosition.x;
                if (portConstraints.isRatioFixed()) {
                    positionOrRatio /= portNodeSize.x;
                }
                
                break;
            }
            
            dummy.setProperty(Properties.PORT_RATIO_OR_POSITION, positionOrRatio);
        }
        
        // Set the port side of the dummy
        dummy.setProperty(Properties.EXT_PORT_SIDE, finalExternalPortSide);
        
        return dummy;
    }
    
    /**
     * Calculates the position of the external port's top left corner from the position of the
     * given dummy node that represents the port. The position is relative to the graph node's
     * top left corner.
     * 
     * @param graph the graph for which ports shall be placed
     * @param portDummy the dummy node representing the external port.
     * @param portWidth the external port's width.
     * @param portHeight the external port's height.
     * @return the external port's position.
     */
    public static KVector getExternalPortPosition(final LGraph graph, final LNode portDummy,
            final double portWidth, final double portHeight) {
        
        KVector portPosition = new KVector(portDummy.getPosition());
        portPosition.x += portDummy.getSize().x / 2.0;
        portPosition.y += portDummy.getSize().y / 2.0;
        float portOffset = portDummy.getProperty(Properties.OFFSET);
        
        // Get some properties of the graph
        KVector graphSize = graph.getSize();
        LInsets insets = graph.getInsets();
        KVector graphOffset = graph.getOffset();
        
        // The exact coordinates depend on the port's side... (often enough, these calculations will
        // give the same results as the port coordinates already computed, but depending on the kind of
        // connected components processing, the computed coordinates might be wrong now)
        switch (portDummy.getProperty(Properties.EXT_PORT_SIDE)) {
        case NORTH:
            portPosition.x += insets.left + graphOffset.x - (portWidth / 2.0);
            portPosition.y = -portHeight - portOffset;
            portDummy.getPosition().y = -(insets.top + portOffset + graphOffset.y);
            break;
        
        case EAST:
            portPosition.x = graphSize.x + insets.left + insets.right + portOffset;
            portPosition.y += insets.top + graphOffset.y - (portHeight / 2.0);
            portDummy.getPosition().x = graphSize.x + insets.right + portOffset - graphOffset.x;
            break;
        
        case SOUTH:
            portPosition.x += insets.left + graphOffset.x - (portWidth / 2.0);
            portPosition.y = graphSize.y + insets.top + insets.bottom + portOffset;
            portDummy.getPosition().y = graphSize.y + insets.bottom + portOffset - graphOffset.y;
            break;
        
        case WEST:
            portPosition.x = -portWidth - portOffset;
            portPosition.y += insets.top + graphOffset.y - (portHeight / 2.0);
            portDummy.getPosition().x = -(insets.left + portOffset + graphOffset.x);
            break;
        }
        
        return portPosition;
    }
    
}
