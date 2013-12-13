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
package de.cau.cs.kieler.klay.layered.p5edges;

import java.io.*;
import java.util.*;

import de.cau.cs.kieler.core.math.KVector;
import de.cau.cs.kieler.core.math.KVectorChain;
import de.cau.cs.kieler.kiml.options.LayoutOptions;
import de.cau.cs.kieler.kiml.options.PortSide;
import de.cau.cs.kieler.klay.layered.Util;
import de.cau.cs.kieler.klay.layered.graph.*;
import de.cau.cs.kieler.klay.layered.properties.PortType;
import de.cau.cs.kieler.klay.layered.properties.Properties;

/**
 * Edge routing implementation that creates orthogonal bend points. Inspired by
 * <ul>
 * <li>Georg Sander. Layout of directed hypergraphs with orthogonal hyperedges. In <i>Proceedings of
 * the 11th International Symposium on Graph Drawing (GD '03)</i>, volume 2912 of LNCS, pp. 381-386.
 * Springer, 2004.</li>
 * <li>Giuseppe di Battista, Peter Eades, Roberto Tamassia, Ioannis G. Tollis, <i>Graph Drawing:
 * Algorithms for the Visualization of Graphs</i>, Prentice Hall, New Jersey, 1999 (Section 9.4, for
 * cycle breaking in the hyperedge segment graph)
 * </ul>
 * <p>
 * This is a generic implementation that can be applied to all four routing directions. Usually,
 * edges will be routed from west to east. However, with northern and southern external ports, this
 * changes: edges are routed from south to north and north to south, respectively. To support these
 * different requirements, the routing direction-related code is factored out into
 * {@link IRoutingDirectionStrategy routing strategies}.
 * </p>
 * <p>
 * When instantiating a new routing generator, the concrete directional strategy must be specified.
 * Once that is done, {@link #routeEdges(LGraph, List, int, List, double)} is called repeatedly to
 * route edges between given lists of nodes.
 * </p>
 * 
 * @author msp
 * @author cds
 * @kieler.design proposed by msp
 * @kieler.rating proposed yellow by msp
 */
public final class OrthogonalRoutingGenerator {
    
    // /////////////////////////////////////////////////////////////////////////////
    // Routing Strategies
    
    /**
     * A routing direction strategy adapts the {@link OrthogonalRoutingGenerator} to different
     * routing directions. Usually, but not always, edges will be routes from west to east. However,
     * with northern and southern external ports, this changes. Routing strategies support that.
     * 
     * @author cds
     */
    public interface IRoutingDirectionStrategy {
	/**
	 * Returns the port's position on a hyper edge axis. In the west-to-east routing case, this
	 * would be the port's exact y coordinate.
	 * 
	 * @param port
	 *            the port.
	 * @return the port's coordinate on the hyper edge axis.
	 */
	double getPortPositionOnHyperNode(final LPort port);
	
	/**
	 * Returns the side of ports that should be considered on a source layer. For a west-to-east
	 * routing, this would probably be the eastern ports of each western layer.
	 * 
	 * @return the side of ports to be considered in the source layer.
	 */
	PortSide getSourcePortSide();
	
	/**
	 * Returns the side of ports that should be considered on a target layer. For a west-to-east
	 * routing, this would probably be the western ports of each eastern layer.
	 * 
	 * @return the side of ports to be considered in the target layer.
	 */
	PortSide getTargetPortSide();
	
	/**
	 * Calculates and assigns bend points for edges incident to the ports belonging to the given
	 * hyper edge.
	 * 
	 * @param hyperNode
	 *            the hyper edge.
	 * @param startPos
	 *            the position of the trunk of the first hyper edge between the layers. This
	 *            position, together with the current hyper node's rank allows the calculation
	 *            of the hyper node's trunk's position.
	 * @param edgeSpacing
	 *            the space between two edges.
	 */
	void calculateBendPoints(final HyperNode hyperNode, final double startPos,
		final double edgeSpacing);
    }
    
    /**
     * Routing strategy for routing layers from west to east.
     * 
     * @author cds
     */
    public static class WestToEastRoutingStrategy implements IRoutingDirectionStrategy {
	
	/**
	 * {@inheritDoc}
	 */
	public double getPortPositionOnHyperNode(final LPort port) {
	    return port.getNode().getPosition().y + port.getPosition().y + port.getAnchor().y;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public PortSide getSourcePortSide() {
	    return PortSide.EAST;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public PortSide getTargetPortSide() {
	    return PortSide.WEST;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void calculateBendPoints(final HyperNode hyperNode, final double startPos,
		final double edgeSpacing) {
	    
	    // Calculate coordinates for each port's bend points
	    final double x = startPos + (hyperNode.rank * edgeSpacing);
	    
	    for (final LPort port : hyperNode.ports) {
		final double sourcey = port.getAbsoluteAnchor().y;
		
		for (final LEdge edge : port.getOutgoingEdges()) {
		    final LPort target = edge.getTarget();
		    final double targety = target.getAbsoluteAnchor().y;
		    if (Math.abs(sourcey - targety) > TOLERANCE) {
			final KVector point1 = new KVector(x, sourcey);
			edge.getBendPoints().add(point1);
			addJunctionPointIfNecessary(edge, hyperNode, point1, true);
			
			final KVector point2 = new KVector(x, targety);
			edge.getBendPoints().add(point2);
			addJunctionPointIfNecessary(edge, hyperNode, point2, true);
		    }
		}
	    }
	}
	
    }
    
    /**
     * Routing strategy for routing layers from north to south.
     * 
     * @author cds
     */
    public static class NorthToSouthRoutingStrategy implements IRoutingDirectionStrategy {
	
	/**
	 * {@inheritDoc}
	 */
	public double getPortPositionOnHyperNode(final LPort port) {
	    return port.getNode().getPosition().x + port.getPosition().x + port.getAnchor().x;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public PortSide getSourcePortSide() {
	    return PortSide.SOUTH;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public PortSide getTargetPortSide() {
	    return PortSide.NORTH;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void calculateBendPoints(final HyperNode hyperNode, final double startPos,
		final double edgeSpacing) {
	    
	    // Calculate coordinates for each port's bend points
	    final double y = startPos + (hyperNode.rank * edgeSpacing);
	    
	    for (final LPort port : hyperNode.ports) {
		final double sourcex = port.getAbsoluteAnchor().x;
		
		for (final LEdge edge : port.getOutgoingEdges()) {
		    final LPort target = edge.getTarget();
		    final double targetx = target.getAbsoluteAnchor().x;
		    if (Math.abs(sourcex - targetx) > TOLERANCE) {
			final KVector point1 = new KVector(sourcex, y);
			edge.getBendPoints().add(point1);
			addJunctionPointIfNecessary(edge, hyperNode, point1, false);
			
			final KVector point2 = new KVector(targetx, y);
			edge.getBendPoints().add(point2);
			addJunctionPointIfNecessary(edge, hyperNode, point2, false);
		    }
		}
	    }
	}
	
    }
    
    /**
     * Routing strategy for routing layers from south to north.
     * 
     * @author cds
     */
    public static class SouthToNorthRoutingStrategy implements IRoutingDirectionStrategy {
	
	/**
	 * {@inheritDoc}
	 */
	public double getPortPositionOnHyperNode(final LPort port) {
	    return port.getNode().getPosition().x + port.getPosition().x + port.getAnchor().x;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public PortSide getSourcePortSide() {
	    return PortSide.NORTH;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public PortSide getTargetPortSide() {
	    return PortSide.SOUTH;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void calculateBendPoints(final HyperNode hyperNode, final double startPos,
		final double edgeSpacing) {
	    
	    // Calculate coordinates for each port's bend points
	    final double y = startPos - (hyperNode.rank * edgeSpacing);
	    
	    for (final LPort port : hyperNode.ports) {
		final double sourcex = port.getAbsoluteAnchor().x;
		
		for (final LEdge edge : port.getOutgoingEdges()) {
		    final LPort target = edge.getTarget();
		    final double targetx = target.getAbsoluteAnchor().x;
		    if (Math.abs(sourcex - targetx) > TOLERANCE) {
			final KVector point1 = new KVector(sourcex, y);
			edge.getBendPoints().add(point1);
			addJunctionPointIfNecessary(edge, hyperNode, point1, false);
			
			final KVector point2 = new KVector(targetx, y);
			edge.getBendPoints().add(point2);
			addJunctionPointIfNecessary(edge, hyperNode, point2, false);
		    }
		}
	    }
	}
	
    }
    
    // /////////////////////////////////////////////////////////////////////////////
    // Hyper Node Graph Structures
    
    /**
     * A hypernode used for routing a hyperedge.
     */
    private class HyperNode implements Comparable<HyperNode> {
	/** ports represented by this hypernode. */
	private final List<LPort>	ports       = new LinkedList<LPort>();
	/** mark value used for cycle breaking. */
	private int		      mark;
	/** the rank determines the horizontal distance to the preceding layer. */
	private int		      rank;
	/** vertical starting position of this hypernode. */
	private double		   start       = Double.NaN;
	/** vertical ending position of this hypernode. */
	private double		   end	 = Double.NaN;
	/** positions of line segments going to the preceding layer. */
	private final LinkedList<Double> sourcePosis = new LinkedList<Double>();
	/** positions of line segments going to the next layer. */
	private final LinkedList<Double> targetPosis = new LinkedList<Double>();
	/** list of outgoing dependencies. */
	private final List<Dependency>   outgoing    = new LinkedList<Dependency>();
	/** sum of the weights of outgoing dependencies. */
	private int		      outweight;
	/** list of incoming dependencies. */
	private final List<Dependency>   incoming    = new LinkedList<Dependency>();
	/** sum of the weights of incoming depencencies. */
	private int		      inweight;
	
	/**
	 * Adds the positions of the given port and all connected ports.
	 * 
	 * @param port
	 *            a port
	 * @param hyperNodeMap
	 *            map of ports to existing hypernodes
	 */
	void addPortPosis(final LPort port, final Map<LPort, HyperNode> hyperNodeMap) {
	    hyperNodeMap.put(port, this);
	    ports.add(port);
	    final double pos = routingStrategy.getPortPositionOnHyperNode(port);
	    
	    // set new start position
	    if (Double.isNaN(start)) {
		start = pos;
	    } else {
		start = Math.min(start, pos);
	    }
	    
	    // set new end position
	    if (Double.isNaN(end)) {
		end = pos;
	    } else {
		end = Math.max(end, pos);
	    }
	    
	    // add the new port position to the respective list
	    if (port.getSide() == routingStrategy.getSourcePortSide()) {
		insertSorted(sourcePosis, pos);
	    } else {
		insertSorted(targetPosis, pos);
	    }
	    
	    // add connected ports
	    for (final LPort otherPort : port.getConnectedPorts()) {
		if (!hyperNodeMap.containsKey(otherPort)) {
		    addPortPosis(otherPort, hyperNodeMap);
		}
	    }
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
	    final StringBuilder builder = new StringBuilder("{");
	    final Iterator<LPort> portIter = ports.iterator();
	    while (portIter.hasNext()) {
		final LPort port = portIter.next();
		String name = port.getNode().getName();
		if (name == null) {
		    name = "n" + port.getNode().getIndex();
		}
		builder.append(name);
		if (portIter.hasNext()) {
		    builder.append(',');
		}
	    }
	    builder.append('}');
	    return builder.toString();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public int compareTo(final HyperNode other) {
	    return mark - other.mark;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(final Object object) {
	    if (object instanceof HyperNode) {
		final HyperNode other = (HyperNode) object;
		return mark == other.mark;
	    }
	    return false;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
	    return mark;
	}
	
    }
    
    /**
     * A dependency between two hypernodes.
     */
    private static final class Dependency {
	/** the source hypernode of this dependency. */
	private HyperNode source;
	/** the target hypernode of this dependency. */
	private HyperNode target;
	/** the weight of this dependency. */
	private final int weight;
	
	/**
	 * Creates a dependency from the given source to the given target.
	 * 
	 * @param thesource
	 *            the dependency source
	 * @param thetarget
	 *            the dependency target
	 * @param theweight
	 *            weight of the dependency
	 */
	private Dependency(final HyperNode thesource, final HyperNode thetarget, final int theweight) {
	    
	    target = thetarget;
	    source = thesource;
	    weight = theweight;
	    source.outgoing.add(this);
	    target.incoming.add(this);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
	    return source + "->" + target;
	}
    }
    
    // /////////////////////////////////////////////////////////////////////////////
    // Constants and Variables
    
    /** differences below this tolerance value are treated as zero. */
    private static final double	     TOLERANCE	   = 1e-3;
    /** factor for edge spacing used to determine the conflict threshold. */
    private static final double	     CONFL_THRESH_FACTOR = 0.2;
    /** weight penalty for conflicts of horizontal line segments. */
    private static final int		CONFLICT_PENALTY    = 16;
    
    /** routing direction strategy. */
    private final IRoutingDirectionStrategy routingStrategy;
    /** spacing between edges. */
    private final double		    edgeSpacing;
    /** threshold at which conflicts of horizontal line segments are detected. */
    private final double		    conflictThreshold;
    
    private final String		    debugPrefix;
    
    // /////////////////////////////////////////////////////////////////////////////
    // Constructor
    
    /**
     * Constructs a new instance.
     * 
     * @param routingStrategy
     *            the routing strategy to use. This will usually be one of the strategies defined by
     *            this class.
     * @param edgeSpacing
     *            the space between edges.
     * @param debugPrefix
     *            prefix of debug output files, or {@code null} if no debug output should be
     *            generated.
     */
    public OrthogonalRoutingGenerator(final IRoutingDirectionStrategy routingStrategy,
	    final double edgeSpacing, final String debugPrefix) {
	
	this.routingStrategy = routingStrategy;
	this.edgeSpacing = edgeSpacing;
	conflictThreshold = CONFL_THRESH_FACTOR * edgeSpacing;
	this.debugPrefix = debugPrefix;
    }
    
    // /////////////////////////////////////////////////////////////////////////////
    // Edge Routing
    
    /**
     * Route edges between the given layers.
     * 
     * @param layeredGraph
     *            the layered graph.
     * @param sourceLayerNodes
     *            the left layer. May be {@code null}.
     * @param sourceLayerIndex
     *            the source layer's index. Ignored if there is no source layer.
     * @param targetLayerNodes
     *            the right layer. May be {@code null}.
     * @param startPos
     *            horizontal position of the first routing slot
     * @return the number of routing slots for this layer
     */
    public int routeEdges(final LGraph layeredGraph, final Iterable<LNode> sourceLayerNodes,
	    final int sourceLayerIndex, final Iterable<LNode> targetLayerNodes,
	    final double startPos) {
	
	final Map<LPort, HyperNode> portToHyperNodeMap = new HashMap<LPort, HyperNode>();
	final List<HyperNode> hyperNodes = new LinkedList<HyperNode>();
	
	// create hypernodes for eastern output ports of the left layer and for western
	// output ports of the right layer
	createHyperNodes(sourceLayerNodes, routingStrategy.getSourcePortSide(), hyperNodes,
		portToHyperNodeMap);
	createHyperNodes(targetLayerNodes, routingStrategy.getTargetPortSide(), hyperNodes,
		portToHyperNodeMap);
	
	// create dependencies for the hypernode ordering graph
	final ListIterator<HyperNode> iter1 = hyperNodes.listIterator();
	while (iter1.hasNext()) {
	    final HyperNode hyperNode1 = iter1.next();
	    final ListIterator<HyperNode> iter2 = hyperNodes.listIterator(iter1.nextIndex());
	    while (iter2.hasNext()) {
		final HyperNode hyperNode2 = iter2.next();
		createDependency(hyperNode1, hyperNode2, conflictThreshold);
	    }
	}
	
	// write the full dependency graph to an output file
	if (debugPrefix != null) {
	    writeDebugGraph(layeredGraph, sourceLayerNodes == null ? 0 : sourceLayerIndex + 1,
		    hyperNodes, "full");
	}
	
	// break cycles
	breakCycles(hyperNodes, layeredGraph.getProperty(Properties.RANDOM));
	
	// write the acyclic dependency graph to an output file
	if (debugPrefix != null) {
	    writeDebugGraph(layeredGraph, sourceLayerNodes == null ? 0 : sourceLayerIndex + 1,
		    hyperNodes, "acyclic");
	}
	
	// assign ranks to the hypernodes
	topologicalNumbering(hyperNodes);
	
	// set bend points with appropriate coordinates
	int rankCount = -1;
	for (final HyperNode node : hyperNodes) {
	    // Hypernodes that are just straight lines don't take up a slot and don't need bend
	    // points
	    if (Math.abs(node.start - node.end) < TOLERANCE) {
		continue;
	    }
	    
	    rankCount = Math.max(rankCount, node.rank);
	    
	    routingStrategy.calculateBendPoints(node, startPos, edgeSpacing);
	}
	
	return rankCount + 1;
    }
    
    // /////////////////////////////////////////////////////////////////////////////
    // Hyper Node Graph Creation
    
    /**
     * Creates hypernodes for the given layer.
     * 
     * @param nodes
     *            the layer. May be {@code null}, in which case nothing happens.
     * @param portSide
     *            side of the output ports for whose outgoing edges hypernodes should be created.
     * @param hyperNodes
     *            list the created hypernodes should be added to.
     * @param portToHyperNodeMap
     *            map from ports to hypernodes that should be filled.
     */
    private void createHyperNodes(final Iterable<LNode> nodes, final PortSide portSide,
	    final List<HyperNode> hyperNodes, final Map<LPort, HyperNode> portToHyperNodeMap) {
	
	if (nodes != null) {
	    for (final LNode node : nodes) {
		for (final LPort port : node.getPorts(PortType.OUTPUT, portSide)) {
		    HyperNode hyperNode = portToHyperNodeMap.get(port);
		    if (hyperNode == null) {
			hyperNode = new HyperNode();
			hyperNodes.add(hyperNode);
			hyperNode.addPortPosis(port, portToHyperNodeMap);
		    }
		}
	    }
	}
    }
    
    /**
     * Create a dependency between the two given hypernodes, if one is needed.
     * 
     * @param hn1
     *            first hypernode
     * @param hn2
     *            second hypernode
     * @param minDiff
     *            the minimal difference between horizontal line segments to avoid a conflict
     */
    private static void createDependency(final HyperNode hn1, final HyperNode hn2,
	    final double minDiff) {
	
	// check if at least one of the two nodes is just a straight line; those don't
	// create dependencies since they don't take up a slot
	if ((Math.abs(hn1.start - hn1.end) < TOLERANCE)
		|| (Math.abs(hn2.start - hn2.end) < TOLERANCE)) {
	    return;
	}
	
	// compare number of conflicts for both variants
	final int conflicts1 = countConflicts(hn1.targetPosis, hn2.sourcePosis, minDiff);
	final int conflicts2 = countConflicts(hn2.targetPosis, hn1.sourcePosis, minDiff);
	
	// compare number of crossings for both variants
	final int crossings1 = countCrossings(hn1.targetPosis, hn2.start, hn2.end)
		+ countCrossings(hn2.sourcePosis, hn1.start, hn1.end);
	final int crossings2 = countCrossings(hn2.targetPosis, hn1.start, hn1.end)
		+ countCrossings(hn1.sourcePosis, hn2.start, hn2.end);
	
	final int depValue1 = (CONFLICT_PENALTY * conflicts1) + crossings1;
	final int depValue2 = (CONFLICT_PENALTY * conflicts2) + crossings2;
	
	if (depValue1 < depValue2) {
	    // create dependency from first hypernode to second one
	    new Dependency(hn1, hn2, depValue2 - depValue1);
	} else if (depValue1 > depValue2) {
	    // create dependency from second hypernode to first one
	    new Dependency(hn2, hn1, depValue1 - depValue2);
	} else if ((depValue1 > 0) && (depValue2 > 0)) {
	    // create two dependencies with zero weight
	    new Dependency(hn1, hn2, 0);
	    new Dependency(hn2, hn1, 0);
	}
    }
    
    /**
     * Counts the number of conflicts for the given lists of positions.
     * 
     * @param posis1
     *            sorted list of positions
     * @param posis2
     *            sorted list of positions
     * @param minDiff
     *            minimal difference between two positions
     * @return number of positions that overlap
     */
    private static int countConflicts(final List<Double> posis1, final List<Double> posis2,
	    final double minDiff) {
	
	int conflicts = 0;
	
	if (!posis1.isEmpty() && !posis2.isEmpty()) {
	    final Iterator<Double> iter1 = posis1.iterator();
	    final Iterator<Double> iter2 = posis2.iterator();
	    double pos1 = iter1.next();
	    double pos2 = iter2.next();
	    boolean hasMore = true;
	    
	    do {
		if ((pos1 > (pos2 - minDiff)) && (pos1 < (pos2 + minDiff))) {
		    conflicts++;
		}
		
		if ((pos1 <= pos2) && iter1.hasNext()) {
		    pos1 = iter1.next();
		} else if ((pos2 <= pos1) && iter2.hasNext()) {
		    pos2 = iter2.next();
		} else {
		    hasMore = false;
		}
	    } while (hasMore);
	}
	
	return conflicts;
    }
    
    /**
     * Counts the number of crossings for a given list of positions.
     * 
     * @param posis
     *            sorted list of positions
     * @param start
     *            start of the critical area
     * @param end
     *            end of the critical area
     * @return number of positions in the critical area
     */
    private static int countCrossings(final List<Double> posis, final double start, final double end) {
	int crossings = 0;
	for (final double pos : posis) {
	    if (pos > end) {
		break;
	    } else if (pos >= start) {
		crossings++;
	    }
	}
	return crossings;
    }
    
    // /////////////////////////////////////////////////////////////////////////////
    // Cycle Breaking
    
    /**
     * Breaks all cycles in the given hypernode structure by reversing or removing some
     * dependencies. This implementation assumes that the dependencies of zero weight are exactly
     * the two-cycles of the hypernode structure.
     * 
     * @param nodes
     *            list of hypernodes
     * @param random
     *            random number generator
     */
    private static void breakCycles(final List<HyperNode> nodes, final Random random) {
	final LinkedList<HyperNode> sources = new LinkedList<HyperNode>();
	final LinkedList<HyperNode> sinks = new LinkedList<HyperNode>();
	
	// initialize values for the algorithm
	int nextMark = -1;
	for (final HyperNode node : nodes) {
	    node.mark = nextMark--;
	    int inweight = 0, outweight = 0;
	    
	    for (final Dependency dependency : node.outgoing) {
		outweight += dependency.weight;
	    }
	    
	    for (final Dependency dependency : node.incoming) {
		inweight += dependency.weight;
	    }
	    
	    node.inweight = inweight;
	    node.outweight = outweight;
	    
	    if (outweight == 0) {
		sinks.add(node);
	    } else if (inweight == 0) {
		sources.add(node);
	    }
	}
	
	// assign marks to all nodes, ignore dependencies of weight zero
	final Set<HyperNode> unprocessed = new TreeSet<HyperNode>(nodes);
	final int markBase = nodes.size();
	int nextRight = markBase - 1, nextLeft = markBase + 1;
	final List<HyperNode> maxNodes = new ArrayList<HyperNode>();
	
	while (!unprocessed.isEmpty()) {
	    while (!sinks.isEmpty()) {
		final HyperNode sink = sinks.removeFirst();
		unprocessed.remove(sink);
		sink.mark = nextRight--;
		updateNeighbors(sink, sources, sinks);
	    }
	    
	    while (!sources.isEmpty()) {
		final HyperNode source = sources.removeFirst();
		unprocessed.remove(source);
		source.mark = nextLeft++;
		updateNeighbors(source, sources, sinks);
	    }
	    
	    int maxOutflow = Integer.MIN_VALUE;
	    for (final HyperNode node : unprocessed) {
		final int outflow = node.outweight - node.inweight;
		if (outflow >= maxOutflow) {
		    if (outflow > maxOutflow) {
			maxNodes.clear();
			maxOutflow = outflow;
		    }
		    maxNodes.add(node);
		}
	    }
	    
	    if (!maxNodes.isEmpty()) {
		// if there are multiple hypernodes with maximal outflow, select one randomly
		final HyperNode maxNode = maxNodes.get(random.nextInt(maxNodes.size()));
		unprocessed.remove(maxNode);
		maxNode.mark = nextLeft++;
		updateNeighbors(maxNode, sources, sinks);
		maxNodes.clear();
	    }
	}
	
	// shift ranks that are left of the mark base
	final int shiftBase = nodes.size() + 1;
	for (final HyperNode node : nodes) {
	    if (node.mark < markBase) {
		node.mark += shiftBase;
	    }
	}
	
	// process edges that point left: remove those of zero weight, reverse the others
	for (final HyperNode source : nodes) {
	    final ListIterator<Dependency> depIter = source.outgoing.listIterator();
	    while (depIter.hasNext()) {
		final Dependency dependency = depIter.next();
		final HyperNode target = dependency.target;
		
		if (source.mark > target.mark) {
		    depIter.remove();
		    target.incoming.remove(dependency);
		    
		    if (dependency.weight > 0) {
			dependency.source = target;
			target.outgoing.add(dependency);
			dependency.target = source;
			source.incoming.add(dependency);
		    }
		}
	    }
	}
    }
    
    /**
     * Updates in-weight and out-weight values of the neighbors of the given node, simulating its
     * removal from the graph. The sources and sinks lists are also updated.
     * 
     * @param node
     *            node for which neighbors are updated
     * @param sources
     *            list of sources
     * @param sinks
     *            list of sinks
     */
    private static void updateNeighbors(final HyperNode node, final LinkedList<HyperNode> sources,
	    final LinkedList<HyperNode> sinks) {
	// process following nodes
	for (final Dependency dep : node.outgoing) {
	    if ((dep.target.mark < 0) && (dep.weight > 0)) {
		dep.target.inweight -= dep.weight;
		if ((dep.target.inweight <= 0) && (dep.target.outweight > 0)) {
		    sources.add(dep.target);
		}
	    }
	}
	
	// process preceding nodes
	for (final Dependency dep : node.incoming) {
	    if ((dep.source.mark < 0) && (dep.weight > 0)) {
		dep.source.outweight -= dep.weight;
		if ((dep.source.outweight <= 0) && (dep.source.inweight > 0)) {
		    sinks.add(dep.source);
		}
	    }
	}
    }
    
    // /////////////////////////////////////////////////////////////////////////////
    // Topological Ordering
    
    /**
     * Perform a topological numbering of the given hypernodes.
     * 
     * @param nodes
     *            list of hypernodes
     */
    private static void topologicalNumbering(final List<HyperNode> nodes) {
	// determine sources, targets, incoming count and outgoing count; targets are only
	// added to the list if they only connect westward ports (that is, if all their
	// horizontal segments point to the right)
	final List<HyperNode> sources = new LinkedList<HyperNode>();
	final List<HyperNode> rightwardTargets = new LinkedList<HyperNode>();
	for (final HyperNode node : nodes) {
	    node.inweight = node.incoming.size();
	    node.outweight = node.outgoing.size();
	    
	    if (node.inweight == 0) {
		sources.add(node);
	    }
	    
	    if ((node.outweight == 0) && (node.sourcePosis.size() == 0)) {
		rightwardTargets.add(node);
	    }
	}
	
	int maxRank = -1;
	
	// assign ranks using topological numbering
	while (!sources.isEmpty()) {
	    final HyperNode node = sources.remove(0);
	    for (final Dependency dep : node.outgoing) {
		final HyperNode target = dep.target;
		target.rank = Math.max(target.rank, node.rank + 1);
		maxRank = Math.max(maxRank, target.rank);
		
		target.inweight--;
		if (target.inweight == 0) {
		    sources.add(target);
		}
	    }
	}
	
	/*
	 * If we stopped here, hyper nodes that don't have any horizontal segments pointing leftward
	 * would be ranked just like every other hyper node. This would move back edges too far away
	 * from their target node. To remedy that, we move all hyper nodes with horizontal segments
	 * only pointing rightwards as far right as possible.
	 */
	if (maxRank > -1) {
	    // assign all target nodes with horzizontal segments pointing to the right the
	    // rightmost rank
	    for (final HyperNode node : rightwardTargets) {
		node.rank = maxRank;
	    }
	    
	    // let all other segments with horizontal segments pointing rightwards move as
	    // far right as possible
	    while (!rightwardTargets.isEmpty()) {
		final HyperNode node = rightwardTargets.remove(0);
		
		// The node only has connections to western ports
		for (final Dependency dep : node.incoming) {
		    final HyperNode source = dep.source;
		    if (source.sourcePosis.size() > 0) {
			continue;
		    }
		    
		    source.rank = Math.min(source.rank, node.rank - 1);
		    
		    source.outweight--;
		    if (source.outweight == 0) {
			rightwardTargets.add(source);
		    }
		}
	    }
	}
    }
    
    // /////////////////////////////////////////////////////////////////////////////
    // Utilities
    
    /**
     * Inserts a given value into a sorted list.
     * 
     * @param list
     *            sorted list
     * @param value
     *            value to insert
     */
    private static void insertSorted(final List<Double> list, final double value) {
	final ListIterator<Double> listIter = list.listIterator();
	while (listIter.hasNext()) {
	    final double next = listIter.next().floatValue();
	    if (next == value) {
		// an exactly equal value is already present in the list
		return;
	    } else if (next > value) {
		listIter.previous();
		break;
	    }
	}
	listIter.add(Double.valueOf(value));
    }
    
    /**
     * Add a junction point to the given edge if necessary. It is necessary to add a junction point
     * if the bend point is not at one of the two end positions of the hypernode.
     * 
     * @param edge
     *            an edge
     * @param hyperNode
     *            the corresponding hypernode
     * @param pos
     *            the bend point position
     * @param vertical
     *            {@code true} if the connecting segment is vertical, {@code false} if it is
     *            horizontal
     */
    private static void addJunctionPointIfNecessary(final LEdge edge, final HyperNode hyperNode,
	    final KVector pos, final boolean vertical) {
	
	final double p = vertical ? pos.y : pos.x;
	
	// check if the given bend point is somewhere between the start and end position of the
	// hypernode
	if (((p > hyperNode.start) && (p < hyperNode.end))
		|| (!hyperNode.sourcePosis.isEmpty() && !hyperNode.targetPosis.isEmpty()
		// the bend point is at the start and joins another edge at the same position
		&& (((Math.abs(p - hyperNode.sourcePosis.getFirst()) < TOLERANCE) && (Math.abs(p
			- hyperNode.targetPosis.getFirst()) < TOLERANCE))
		// the bend point is at the end and joins another edge at the same position
		|| ((Math.abs(p - hyperNode.sourcePosis.getLast()) < TOLERANCE) && (Math.abs(p
			- hyperNode.targetPosis.getLast()) < TOLERANCE))))) {
	    
	    // it is, so create a new junction point for the edge at the bend point's position
	    KVectorChain junctionPoints = edge.getProperty(LayoutOptions.JUNCTION_POINTS);
	    if (junctionPoints == null) {
		junctionPoints = new KVectorChain();
		edge.setProperty(LayoutOptions.JUNCTION_POINTS, junctionPoints);
	    }
	    junctionPoints.add(new KVector(pos));
	}
    }
    
    // /////////////////////////////////////////////////////////////////////////////
    // Debugging
    
    /**
     * Writes a debug graph for the given list of hypernodes.
     * 
     * @param layeredGraph
     *            the layered graph
     * @param layerIndex
     *            the currently processed layer's index
     * @param hypernodes
     *            a list of hypernodes
     * @param label
     *            a label to append to the output files
     */
    private void writeDebugGraph(final LGraph layeredGraph, final int layerIndex,
	    final List<HyperNode> hypernodes, final String label) {
	
	try {
	    final Writer writer = createWriter(layeredGraph, layerIndex, label);
	    writer.write("digraph {\n");
	    
	    // Write hypernode information
	    for (final HyperNode hypernode : hypernodes) {
		writer.write("  " + hypernode.hashCode() + "[label=\"" + hypernode.toString()
			+ "\"]\n");
	    }
	    
	    // Write dependency information
	    for (final HyperNode hypernode : hypernodes) {
		for (final Dependency dependency : hypernode.outgoing) {
		    writer.write("  " + hypernode.hashCode() + "->" + dependency.target.hashCode()
			    + "[label=\"" + dependency.weight + "\"]\n");
		}
	    }
	    
	    writer.write("}\n");
	    writer.close();
	} catch (final Exception exception) {
	    exception.printStackTrace();
	}
    }
    
    /**
     * Create a writer for debug output.
     * 
     * @param layeredGraph
     *            the layered graph
     * @param layerIndex
     *            the currently processed layer's index
     * @param label
     *            a label to append to the output files
     * @return a file writer for debug output
     * @throws IOException
     *             if creating the output file fails
     */
    private Writer createWriter(final LGraph layeredGraph, final int layerIndex, final String label)
	    throws IOException {
	
	final String path = Util.getDebugOutputPath();
	new FileFake(path).mkdirs();
	
	final String debugFileName = Util.getDebugOutputFileBaseName(layeredGraph) + debugPrefix
		+ "-l" + layerIndex + "-" + label;
	return new FileWriter(new FileFake(path + FileFake.separator + debugFileName + ".dot"));
    }
    
}
