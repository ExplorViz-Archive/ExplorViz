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
package de.cau.cs.kieler.klay.layered.properties;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.Multimap;

import de.cau.cs.kieler.core.math.KVector;
import de.cau.cs.kieler.core.math.KVectorChain;
import de.cau.cs.kieler.core.properties.IProperty;
import de.cau.cs.kieler.core.properties.Property;
import de.cau.cs.kieler.kiml.options.LayoutOptions;
import de.cau.cs.kieler.kiml.options.PortSide;
import de.cau.cs.kieler.klay.layered.ILayoutProcessor;
import de.cau.cs.kieler.klay.layered.IntermediateProcessingConfiguration;
import de.cau.cs.kieler.klay.layered.compound.CrossHierarchyEdge;
import de.cau.cs.kieler.klay.layered.graph.LEdge;
import de.cau.cs.kieler.klay.layered.graph.LGraph;
import de.cau.cs.kieler.klay.layered.graph.LLabel;
import de.cau.cs.kieler.klay.layered.graph.LNode;
import de.cau.cs.kieler.klay.layered.graph.LPort;
import de.cau.cs.kieler.klay.layered.p3order.NodeGroup;

/**
 * Container for property definitions for internal use of the algorithm. These properties should
 * not be accessed from outside.
 * 
 * @author msp
 * @author cds
 * @author uru 
 * @kieler.design proposed by msp
 * @kieler.rating proposed yellow by msp
 */
public final class InternalProperties {

    /**
     * The original object from which a graph element was created.
     */
    public static final IProperty<Object> ORIGIN = new Property<Object>("origin");
    
    /**
     * The intermediate processing configuration for an input graph.
     */
    public static final IProperty<IntermediateProcessingConfiguration> CONFIGURATION
            = new Property<IntermediateProcessingConfiguration>("processingConfiguration");
    
    /**
     * The list of layout processors executed for an input graph.
     */
    public static final IProperty<List<ILayoutProcessor>> PROCESSORS
            = new Property<List<ILayoutProcessor>>("processors");
    
    /**
     * Whether the original node an LNode was created from was a compound node or not. This might
     * influence certain layout decisions, such as where to place inside port labels so that they don't
     * overlap edges.
     */
    public static final IProperty<Boolean> COMPOUND_NODE = new Property<Boolean>("compoundNode", false);
    
    /**
     * Whether the original port an LPort was created from was a compound port with connections to or
     * from descendants of its node. This might influence certain layout decisions, such as where to
     * place its inside port label.
     */
    public static final IProperty<Boolean> INSIDE_CONNECTIONS = new Property<Boolean>(
            "insideConnections", false);

    /**
     * An LNode that represents a compound node can hold a reference to a nested LGraph which
     * represents the graph that is contained within the compound node.
     */
    public static final IProperty<LGraph> NESTED_LGRAPH = new Property<LGraph>("nestedLGraph");
    
    /**
     * A nested LGraph has a reference to the LNode that contains it.
     */
    public static final IProperty<LNode> PARENT_LNODE = new Property<LNode>("parentLNode");
    
    /**
     * Node type.
     */
    public static final IProperty<NodeType> NODE_TYPE = new Property<NodeType>("nodeType",
            NodeType.NORMAL);

    /**
     * Offset of port position to the node border. An offset of 0 means that the port touches its
     * parent node on the outside, positive offsets move the port away from the node, and negative
     * offset move the port towards the inside.
     */
    public static final IProperty<Float> OFFSET = new Property<Float>(LayoutOptions.OFFSET, 0.0f);

    /**
     * The original bend points of an edge.
     */
    public static final IProperty<KVectorChain> ORIGINAL_BENDPOINTS = new Property<KVectorChain>(
            "originalBendpoints");
    
    /**
     * In interactive layout settings, this property can be set to indicate where a dummy node that
     * represents an edge in a given layer was probably placed in that layer. This information can
     * be calculated during the crossing minimization phase and later be used by an interactive node
     * placement algorithm.
     */
    public static final IProperty<Double> ORIGINAL_DUMMY_NODE_POSITION = new Property<Double>(
            "originalDummyNodePosition");
    
    /**
     * The edge a label originally belonged to. This property was introduced to remember which
     * cross-hierarchy edge a label originally belonged to.
     */
    public static final IProperty<LEdge> ORIGINAL_LABEL_EDGE = new Property<LEdge>("originalLabelEdge");
    
    /**
     * Flag for reversed edges.
     */
    public static final IProperty<Boolean> REVERSED = new Property<Boolean>("reversed", false);

    /**
     * Random number generator for the algorithm.
     */
    public static final IProperty<Random> RANDOM = new Property<Random>("random");

    /**
     * The source port of a long edge before it was broken into multiple segments.
     */
    public static final IProperty<LPort> LONG_EDGE_SOURCE = new Property<LPort>("longEdgeSource",
            null);
    /**
     * The target port of a long edge before it was broken into multiple segments.
     */
    public static final IProperty<LPort> LONG_EDGE_TARGET = new Property<LPort>("longEdgeTarget",
            null);

    /**
     * Edge constraints for nodes.
     */
    public static final IProperty<EdgeConstraint> EDGE_CONSTRAINT = new Property<EdgeConstraint>(
            "edgeConstraint", EdgeConstraint.NONE);

    /**
     * The layout unit a node belongs to. This property only makes sense for nodes. A layout unit is
     * a set of nodes between which no nodes belonging to other layout units may be placed. Nodes
     * not belonging to any layout unit may be placed arbitrarily between nodes of a layout unit.
     * Layer layout units are identified through one of their nodes.
     */
    public static final IProperty<LNode> IN_LAYER_LAYOUT_UNIT = new Property<LNode>(
            "inLayerLayoutUnit");

    /**
     * The in-layer constraint placed on a node. This indicates whether this node should be handled
     * like any other node, or if it must be placed at the top or bottom of a layer. This is
     * important for external port dummy nodes. Crossing minimizers are not required to respect this
     * constraint. If they don't, however, they must include a dependency on
     * {@link de.cau.cs.kieler.klay.layered.intermediate.InLayerConstraintProcessor}.
     */
    public static final IProperty<InLayerConstraint> IN_LAYER_CONSTRAINT 
           = new Property<InLayerConstraint>(
            "inLayerConstraint", InLayerConstraint.NONE);

    /**
     * Indicates that a node {@code x} may only appear inside a layer before the node {@code y} the
     * property is set to. That is, having {@code x} appear after {@code y} would violate this
     * constraint. This property only makes sense for nodes.
     */
    public static final IProperty<List<LNode>> IN_LAYER_SUCCESSOR_CONSTRAINTS =
            new Property<List<LNode>>("inLayerSuccessorConstraint", new ArrayList<LNode>());
    
    /**
     * A property set on ports indicating a dummy node created for that port. This is not set for all
     * ports that have dummy nodes created for them.
     */
    public static final IProperty<LNode> PORT_DUMMY = new Property<LNode>("portDummy");

    /**
     * The node group of an LNode as used in the crossing minimization phase.
     */
    public static final IProperty<NodeGroup> NODE_GROUP = new Property<NodeGroup>("nodeGroup");

    /**
     * Crossing hint used for in-layer cross counting with northern and southern port dummies. This is
     * effectively the number of different ports a northern or southern port dummy represents.
     */
    public static final IProperty<Integer> CROSSING_HINT = new Property<Integer>("crossingHint", 0);

    /**
     * Flags indicating the properties of a graph.
     */
    public static final IProperty<Set<GraphProperties>> GRAPH_PROPERTIES =
            new Property<Set<GraphProperties>>("graphProperties", EnumSet.noneOf(GraphProperties.class));

    /**
     * The side of an external port a dummy node was created for.
     */
    public static final IProperty<PortSide> EXT_PORT_SIDE = new Property<PortSide>(
            "externalPortSide", PortSide.UNDEFINED);

    /**
     * Original size of the external port a dummy node was created for.
     */
    public static final IProperty<KVector> EXT_PORT_SIZE = new Property<KVector>(
            "externalPortSize", new KVector());

    /**
     * External port dummies that represent northern or southern external ports are replaced by new
     * dummy nodes during layout. In these cases, this property is set to the original dummy node.
     */
    public static final IProperty<LNode> EXT_PORT_REPLACED_DUMMY = new Property<LNode>(
            "externalPortReplacedDummy");

    /**
     * The port sides of external ports a connected component connects to. This property is set on
     * the layered graph that represents a connected component and defaults to no connections. If a
     * connected component connects to an external port on the EAST side and to another external
     * port on the NORTH side, this enumeration will list both sides.
     */
    public static final IProperty<Set<PortSide>> EXT_PORT_CONNECTIONS = new Property<Set<PortSide>>(
            "externalPortConnections", EnumSet.noneOf(PortSide.class));

    /**
     * The original position or position-to-node-size ratio of a port. This property has two use
     * cases:
     * <ol>
     *   <li>For external port dummies. In this use case, the property gives the original position of
     *       the external port (if port constraints are set to {@code FIXED_POS}) or the original
     *       position-to-node-size ratio of the external port ((if port constraints are set to
     *       {@code FIXED_RATIO}).</li>
     *   <li>For ports of regular nodes with port constraints set to {@code FIXED_RATIO}. Since regular
     *       nodes may be resized, the original ratio must be remembered for the new port position
     *       to be determined.</li>
     * </ol>
     * <p>This is a one-dimensional value since the side of the port determines the other dimension.
     * (For eastern and western ports, the x coordinate is determined automatically; for northern and
     * southern ports, the y coordinate is determined automatically)</p>
     */
    public static final IProperty<Double> PORT_RATIO_OR_POSITION = new Property<Double>(
            "portRatioOrPosition", 0.0);

    /**
     * A list of nodes whose barycenters should go into the barycenter calculation of the node this
     * property is set on. Nodes in this list are expected to be in the same layer as the node the
     * property is set on. This is primarily used when edges are rerouted from a node to dummy
     * nodes.
     * <p>
     * This property is currently not declared as one of the layout options offered by KLay Layered
     * and should be considered highly experimental.
     */
    public static final IProperty<List<LNode>> BARYCENTER_ASSOCIATES = new Property<List<LNode>>(
            "barycenterAssociates");

    /**
     * List of comment boxes that are placed on top of a node.
     */
    public static final IProperty<List<LNode>> TOP_COMMENTS = new Property<List<LNode>>(
            "TopSideComments");

    /**
     * List of comment boxes that are placed in the bottom of of a node.
     */
    public static final IProperty<List<LNode>> BOTTOM_COMMENTS = new Property<List<LNode>>(
            "BottomSideComments");

    /**
     * The port of a node that originally connected a comment box with that node.
     */
    public static final IProperty<LPort> COMMENT_CONN_PORT = new Property<LPort>(
            "CommentConnectionPort");

    /**
     * Whether a port is used to collect all incoming edges of a node.
     */
    public static final IProperty<Boolean> INPUT_COLLECT = new Property<Boolean>("inputCollect",
            false);
    /**
     * Whether a port is used to collect all outgoing edges of a node.
     */
    public static final IProperty<Boolean> OUTPUT_COLLECT = new Property<Boolean>("outputCollect",
            false);
    
    /**
     * Property of a LayeredGraph. Whether the graph has been processed by the cycle breaker and the
     * cycle breaker has detected cycles and reverted edges.
     */
    public static final IProperty<Boolean> CYCLIC = new Property<Boolean>("cyclic", false);
        
    /**
     * Determines the original size of a big node.
     */
    public static final IProperty<Float> BIG_NODE_ORIGINAL_SIZE = new Property<Float>(
            "bigNodeOriginalSize", 0f);

    /**
     * Specifies if the corresponding node is the first node in a big node chain.
     */
    public static final IProperty<Boolean> BIG_NODE_INITIAL = new Property<Boolean>(
            "bigNodeInitial", false);
        
    /** 
     * Original labels of a big node. 
     * */
    public static final IProperty<List<LLabel>> BIGNODES_ORIG_LABELS = new Property<List<LLabel>>(
            "de.cau.cs.kieler.klay.layered.bigNodeLabels", new ArrayList<LLabel>());
    
    /** A post processing function that is called during big nodes post processing. */
    public static final IProperty<Function<Void, Void>> BIGNODES_POST_PROCESS =
            new Property<Function<Void, Void>>("de.cau.cs.kieler.klay.layered.postProcess", null);

    /**
     * Map of original hierarchy crossing edges to a set of dummy edges by which the original
     * edge has been replaced.
     */
    public static final IProperty<Multimap<LEdge, CrossHierarchyEdge>> CROSS_HIERARCHY_MAP
            = new Property<Multimap<LEdge, CrossHierarchyEdge>>("crossHierarchyMap");
    
    /**
     * Offset to be added to the target anchor point of an edge when the layout is applied
     * back to the origin.
     */
    public static final IProperty<KVector> TARGET_OFFSET = new Property<KVector>("targetOffset");

    
    // /////////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR

    /**
     * Hidden default constructor.
     */
    private InternalProperties() {
    }
}
