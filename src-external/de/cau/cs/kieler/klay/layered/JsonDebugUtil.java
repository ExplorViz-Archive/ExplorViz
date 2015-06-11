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
package de.cau.cs.kieler.klay.layered;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import de.cau.cs.kieler.core.math.KVector;
import de.cau.cs.kieler.core.math.KVectorChain;
import de.cau.cs.kieler.core.properties.IProperty;
import de.cau.cs.kieler.klay.layered.graph.*;
import de.cau.cs.kieler.klay.layered.graph.LNode.NodeType;
import de.cau.cs.kieler.klay.layered.p4nodes.LinearSegmentsNodePlacer.LinearSegment;
import de.cau.cs.kieler.klay.layered.p5edges.OrthogonalRoutingGenerator.Dependency;
import de.cau.cs.kieler.klay.layered.p5edges.OrthogonalRoutingGenerator.HyperNode;
import de.cau.cs.kieler.klay.layered.properties.InternalProperties;

/**
 * A utility class for debugging of KLay Layered.
 *
 * @author msp
 * @author cds
 * @author csp
 */
public final class JsonDebugUtil {

	private static final String INDENT = "  ";

	/**
	 * Hidden constructor to avoid instantiation.
	 */
	private JsonDebugUtil() {
	}

	/**
	 * Output a representation of the given graph in JSON format.
	 *
	 * @param lgraph
	 *            the layered graph
	 * @param slotIndex
	 *            the slot before whose execution the graph is written.
	 * @param name
	 *            the name the slot before whose execution the graph is written.
	 */
	public static void writeDebugGraph(final LGraph lgraph, final int slotIndex, final String name) {
		try {
			final ConsoleWriter ConsoleWriter = createConsoleWriter(lgraph, slotIndex, name);

			final List<LEdge> edges = Lists.newLinkedList();

			beginGraph(ConsoleWriter, lgraph);

			// Write layerless nodes and collect edges
			edges.addAll(writeLayer(ConsoleWriter, -1, lgraph.getLayerlessNodes(), 2));

			// Go through the layers
			int layerNumber = -1;
			final Iterator<Layer> layersIterator = lgraph.iterator();
			if (!lgraph.getLayerlessNodes().isEmpty() && layersIterator.hasNext()) {
				ConsoleWriter.write(",");
			}
			while (layersIterator.hasNext()) {
				final Layer layer = layersIterator.next();
				layerNumber++;

				// Write the nodes and collect edges
				edges.addAll(writeLayer(ConsoleWriter, layerNumber, layer.getNodes(), 2));

				if (layersIterator.hasNext()) {
					ConsoleWriter.write(",");
				}
			}

			endChildNodeList(ConsoleWriter);

			writeEdges(ConsoleWriter, edges, 1);

			// Close the graph and the ConsoleWriter.
			endGraph(ConsoleWriter);
		} catch (final IOException exception) {
			exception.printStackTrace();
		}
	}

	/**
	 * Writes a debug graph for the given linear segments and their
	 * dependencies.
	 *
	 * @param layeredGraph
	 *            the layered graph.
	 * @param segmentList
	 *            the list of linear segments.
	 * @param outgoingList
	 *            the list of successors for each linear segment.
	 */
	public static void writeDebugGraph(final LGraph layeredGraph,
			final List<LinearSegment> segmentList, final List<List<LinearSegment>> outgoingList) {

		try {
			final ConsoleWriter ConsoleWriter = createConsoleWriter(layeredGraph);

			beginGraph(ConsoleWriter, layeredGraph);

			final String indent1 = Strings.repeat(INDENT, 2);
			final String indent2 = Strings.repeat(INDENT, 3); // SUPPRESS
			// CHECKSTYLE
			// MagicNumber
			int edgeId = 0;

			final Iterator<LinearSegment> segmentIterator = segmentList.iterator();
			final Iterator<List<LinearSegment>> successorsIterator = outgoingList.iterator();

			final StringBuffer edges = new StringBuffer();

			while (segmentIterator.hasNext()) {
				final LinearSegment segment = segmentIterator.next();
				ConsoleWriter.write("\n" + indent1 + "{\n" + indent2 + "\"id\": \"n"
						+ segment.hashCode() + "\",\n" + indent2 + "\"labels\": [ { \"text\": \""
						+ segment + "\" } ],\n" + indent2 + "\"width\": 50,\n" + indent2
						+ "\"height\": 25\n" + indent1 + "}");
				if (segmentIterator.hasNext()) {
					ConsoleWriter.write(",");
				}

				final Iterator<LinearSegment> succIterator = successorsIterator.next().iterator();

				while (succIterator.hasNext()) {
					final LinearSegment successor = succIterator.next();
					edges.append("\n" + indent1 + "{\n" + indent2 + "\"id\": \"e" + edgeId++
							+ "\",\n" + indent2 + "\"source\": \"n" + segment.hashCode() + "\",\n"
							+ indent2 + "\"target\": \"n" + successor.hashCode() + "\",\n"
							+ indent1 + "},");
				}
			}

			endChildNodeList(ConsoleWriter);

			if (edges.length() > 0) {
				edges.deleteCharAt(edges.length() - 1);
			}
			ConsoleWriter.write(INDENT + "\"edges\": [" + edges + "\n" + indent1 + "]");

			endGraph(ConsoleWriter);
		} catch (final Exception exception) {
			exception.printStackTrace();
		}
	}

	/**
	 * Writes a debug graph for the given list of hypernodes.
	 *
	 * @param layeredGraph
	 *            the layered graph
	 * @param layerIndex
	 *            the currently processed layer's index
	 * @param hypernodes
	 *            a list of hypernodes
	 * @param debugPrefix
	 *            prefix of debug output files
	 * @param label
	 *            a label to append to the output files
	 */
	public static void writeDebugGraph(final LGraph layeredGraph, final int layerIndex,
			final List<HyperNode> hypernodes, final String debugPrefix, final String label) {

		try {
			final ConsoleWriter ConsoleWriter = createConsoleWriter(layeredGraph, layerIndex,
					debugPrefix, label);
			beginGraph(ConsoleWriter, layeredGraph);

			final String indent1 = Strings.repeat(INDENT, 2);
			final String indent2 = Strings.repeat(INDENT, 3); // SUPPRESS
			// CHECKSTYLE
			// MagicNumber
			int edgeId = 0;

			final Iterator<HyperNode> hypernodeIterator = hypernodes.iterator();

			final StringBuffer edges = new StringBuffer();

			while (hypernodeIterator.hasNext()) {
				final HyperNode hypernode = hypernodeIterator.next();
				ConsoleWriter.write("\n" + indent1 + "{\n" + indent2 + "\"id\": \"n"
						+ System.identityHashCode(hypernode) + "\",\n" + indent2
						+ "\"labels\": [ { \"text\": \"" + hypernode.toString() + "\" } ],\n"
						+ indent2 + "\"width\": 50,\n" + indent2 + "\"height\": 25\n" + indent1
						+ "}");
				if (hypernodeIterator.hasNext()) {
					ConsoleWriter.write(",");
				}

				final Iterator<Dependency> dependencyIterator = hypernode.getOutgoing().iterator();

				while (dependencyIterator.hasNext()) {
					final Dependency dependency = dependencyIterator.next();
					edges.append("\n" + indent1 + "{\n" + indent2 + "\"id\": \"e" + edgeId++
							+ "\",\n" + indent2 + "\"source\": \"n"
							+ System.identityHashCode(hypernode) + "\",\n" + indent2
							+ "\"target\": \"n" + System.identityHashCode(dependency.getTarget())
							+ "\"\n" + indent1 + "},");
				}
			}

			endChildNodeList(ConsoleWriter);

			if (edges.length() > 0) {
				edges.deleteCharAt(edges.length() - 1);
			}
			ConsoleWriter.write(INDENT + "\"edges\": [" + edges + "\n" + INDENT + "]");

			endGraph(ConsoleWriter);
		} catch (final Exception exception) {
			exception.printStackTrace();
		}
	}

	/**
	 * Begins a new graph by writing the root node, the graphs properties and
	 * the start of the child node list to the ConsoleWriter.
	 *
	 * @param ConsoleWriter
	 *            the ConsoleWriter to write to.
	 * @param lgraph
	 *            the graph to begin
	 * @throws IOException
	 *             if anything goes wrong with the ConsoleWriter.
	 */
	private static void beginGraph(final ConsoleWriter ConsoleWriter, final LGraph lgraph)
			throws IOException {
		final KVectorChain graphSize = calculateGraphSize(lgraph);
		ConsoleWriter.write("{\n" + INDENT + "\"id\": \"root\",\n" + INDENT + "\"x\": "
				+ graphSize.getFirst().x + ",\n" + INDENT + "\"y\": " + graphSize.getFirst().y
				+ ",\n" + INDENT + "\"width\": " + graphSize.getLast().x + ",\n" + INDENT
				+ "\"height\": " + graphSize.getLast().y + ",\n");
		writeProperties(ConsoleWriter, lgraph.getAllProperties(), 1);
		ConsoleWriter.write(",\n" + INDENT + "\"children\": [");
	}

	/**
	 * Calculate the overall size of the given graph. Returns a
	 * {@link KVectorChain} with two elements where the first one represents the
	 * minimal coordinates used by any node, edge or port contained in the graph
	 * and the second one represents the maximal coordinates.
	 *
	 * @param lgraph
	 *            the graph to calculate the size for.
	 * @return a {@link KVectorChain} with two elements, the minimal and the
	 *         maximal coordinates.
	 */
	private static KVectorChain calculateGraphSize(final LGraph lgraph) {
		final KVector min = new KVector();
		final KVector max = new KVector();
		for (final LNode node : lgraph.getLayerlessNodes()) {
			calculateMinMaxPositions(min, max, node);
		}
		for (final Layer layer : lgraph) {
			for (final LNode node : layer) {
				calculateMinMaxPositions(min, max, node);
			}
		}
		max.x -= min.x;
		max.y -= min.y;
		return new KVectorChain(min, max);
	}

	/**
	 * Inspects the given node, its outgoing edges and its ports for minimal and
	 * maximal coordinates and adjusts the given vectors {@code min} and
	 * {@code max} if necessary.
	 *
	 * @param min
	 *            the minimal coordinates used by the graph.
	 * @param max
	 *            the maximal coordinates used by the graph.
	 * @param node
	 *            the current node to inspect.
	 */
	private static void calculateMinMaxPositions(final KVector min, final KVector max,
			final LNode node) {
		min.x = Math.min(min.x, node.getPosition().x - node.getMargin().left);
		max.x = Math.max(max.x, node.getPosition().x + node.getSize().x + node.getMargin().right);
		min.y = Math.min(min.y, node.getPosition().y - node.getMargin().top);
		max.y = Math.max(max.y, node.getPosition().y + node.getSize().y + node.getMargin().bottom);
		for (final LPort port : node.getPorts()) {
			min.x = Math.min(min.x,
					(node.getPosition().x + port.getPosition().x) - port.getMargin().left);
			max.x = Math.max(max.x, node.getPosition().x + port.getPosition().x + port.getSize().x
					+ port.getMargin().right);
			min.y = Math.min(min.y,
					(node.getPosition().y + port.getPosition().y) - port.getMargin().top);
			max.y = Math.max(max.y, node.getPosition().y + port.getPosition().y + port.getSize().y
					+ port.getMargin().bottom);
		}
		for (final LEdge edge : node.getOutgoingEdges()) {
			for (final KVector bendpoint : edge.getBendPoints()) {
				min.x = Math.min(min.x, bendpoint.x);
				max.x = Math.max(max.x, bendpoint.x);
				min.y = Math.min(min.y, bendpoint.y);
				max.y = Math.max(max.y, bendpoint.y);
			}
		}
	}

	/**
	 * Ends the list of child nodes.
	 *
	 * @param ConsoleWriter
	 *            the ConsoleWriter to write to.
	 * @throws IOException
	 *             if anything goes wrong with the ConsoleWriter.
	 */
	private static void endChildNodeList(final ConsoleWriter ConsoleWriter) throws IOException {
		ConsoleWriter.write("\n" + INDENT + "],\n");
	}

	/**
	 * Ends the graph.
	 *
	 * @param ConsoleWriter
	 *            the ConsoleWriter to write to.
	 * @throws IOException
	 *             if anything goes wrong with the ConsoleWriter.
	 */
	private static void endGraph(final ConsoleWriter ConsoleWriter) throws IOException {
		ConsoleWriter.write("\n}\n");

		ConsoleWriter.close();
	}

	/**
	 * Writes the given list of nodes and collects their edges.
	 *
	 * @param ConsoleWriter
	 *            ConsoleWriter to write to.
	 * @param layerNumber
	 *            the layer number. {@code -1} for layerless nodes.
	 * @param nodes
	 *            the nodes in the layer.
	 * @param indentation
	 *            the indentation level to use.
	 * @return list of edges that need to be added to the graph.
	 * @throws IOException
	 *             if anything goes wrong with the ConsoleWriter.
	 */
	private static List<LEdge> writeLayer(final ConsoleWriter ConsoleWriter, final int layerNumber,
			final List<LNode> nodes, final int indentation) throws IOException {

		if (nodes.isEmpty()) {
			return Lists.newLinkedList();
		}

		writeNodes(ConsoleWriter, nodes, indentation, layerNumber);

		final List<LEdge> edges = Lists.newLinkedList();

		// Collect the edges
		for (final LNode node : nodes) {
			// Go through all edges and collect those that have this node as
			// their source
			for (final LPort port : node.getPorts()) {
				edges.addAll(port.getOutgoingEdges());
			}
		}

		return edges;
	}

	/**
	 * Writes the nodes edges as JSON formatted string to the given
	 * ConsoleWriter.
	 *
	 * @param ConsoleWriter
	 *            the ConsoleWriter to write to.
	 * @param nodes
	 *            the nodes to write.
	 * @param indentation
	 *            the indentation level to use.
	 * @param layerNumber
	 *            the layer number. {@code -1} for layerless nodes.
	 * @throws IOException
	 *             if anything goes wrong with the ConsoleWriter.
	 */
	private static void writeNodes(final ConsoleWriter ConsoleWriter, final List<LNode> nodes,
			final int indentation, final int layerNumber) throws IOException {

		final String indent0 = Strings.repeat(INDENT, indentation);
		final String indent1 = Strings.repeat(INDENT, indentation + 1);
		int nodeNumber = -1;
		final Iterator<LNode> nodesIterator = nodes.iterator();
		while (nodesIterator.hasNext()) {
			nodeNumber++;
			final LNode node = nodesIterator.next();
			ConsoleWriter.write("\n" + indent0 + "{\n" + indent1 + "\"id\": \"n" + node.hashCode()
					+ "\",\n" + indent1 + "\"labels\": [ { \"text\": \""
					+ getNodeName(node, layerNumber, nodeNumber) + "\" } ],\n" + indent1
					+ "\"width\": " + node.getSize().x + ",\n" + indent1 + "\"height\": "
					+ node.getSize().y + ",\n" + indent1 + "\"x\": " + node.getPosition().x + ",\n"
					+ indent1 + "\"y\": " + node.getPosition().y + ",\n");
			writeProperties(ConsoleWriter, node.getAllProperties(), indentation + 1);
			ConsoleWriter.write(",\n");
			writePorts(ConsoleWriter, node.getPorts(), indentation + 1);
			ConsoleWriter.write("\n" + indent0 + "}");
			if (nodesIterator.hasNext()) {
				ConsoleWriter.write(",");
			}
		}
	}

	/**
	 * Writes the given edges as JSON formatted string to the given
	 * ConsoleWriter.
	 *
	 * @param ConsoleWriter
	 *            the ConsoleWriter to write to.
	 * @param edges
	 *            the edges to write.
	 * @param indentation
	 *            the indentation level to use.
	 * @throws IOException
	 *             if anything goes wrong with the ConsoleWriter.
	 */
	private static void writeEdges(final ConsoleWriter ConsoleWriter, final List<LEdge> edges,
			final int indentation) throws IOException {

		final String indent0 = Strings.repeat(INDENT, indentation);
		final String indent1 = Strings.repeat(INDENT, indentation + 1);
		final String indent2 = Strings.repeat(INDENT, indentation + 2);
		ConsoleWriter.write(indent0 + "\"edges\": [");
		final Iterator<LEdge> edgesIterator = edges.iterator();
		while (edgesIterator.hasNext()) {
			final LEdge edge = edgesIterator.next();
			ConsoleWriter.write("\n" + indent1 + "{\n" + indent2 + "\"id\": \"e" + edge.hashCode()
					+ "\",\n" + indent2 + "\"source\": \"n" + edge.getSource().getNode().hashCode()
					+ "\",\n" + indent2 + "\"target\": \"n" + edge.getTarget().getNode().hashCode()
					+ "\",\n" + indent2 + "\"sourcePort\": \"p" + edge.getSource().hashCode()
					+ "\",\n" + indent2 + "\"targetPort\": \"p" + edge.getTarget().hashCode()
					+ "\",\n" + indent2 + "\"sourcePoint\": { \"x\": "
					+ edge.getSource().getAbsoluteAnchor().x + ", \"y\": "
					+ edge.getSource().getAbsoluteAnchor().y + " },\n" + indent2
					+ "\"targetPoint\": { \"x\": " + edge.getTarget().getAbsoluteAnchor().x
					+ ", \"y\": " + edge.getTarget().getAbsoluteAnchor().y + " },\n");
			// + indent2 + "\"bendPoints\": \"" +
			// edge.getBendPoints().toString() + "\",\n");
			writeBendPoints(ConsoleWriter, edge.getBendPoints(), indentation + 2);
			ConsoleWriter.write(",\n");
			writeProperties(ConsoleWriter, edge.getAllProperties(), indentation + 2);
			ConsoleWriter.write("\n" + indent1 + "}");
			if (edgesIterator.hasNext()) {
				ConsoleWriter.write(",");
			}
		}
		ConsoleWriter.write("\n" + indent0 + "]");
	}

	/**
	 * Writes the given bendpoints as JSON formatted string to the given
	 * ConsoleWriter.
	 *
	 * @param ConsoleWriter
	 *            the ConsoleWriter to write to.
	 * @param bendPoints
	 *            the bendpoints to write.
	 * @param indentation
	 *            the indentation level to use.
	 * @throws IOException
	 *             if anything goes wrong with the ConsoleWriter.
	 */
	private static void writeBendPoints(final ConsoleWriter ConsoleWriter,
			final KVectorChain bendPoints, final int indentation) throws IOException {

		final String indent0 = Strings.repeat(INDENT, indentation);
		final String indent1 = Strings.repeat(INDENT, indentation + 1);
		ConsoleWriter.write(indent0 + "\"bendPoints\": [");
		final Iterator<KVector> pointsIterator = bendPoints.iterator();
		while (pointsIterator.hasNext()) {
			final KVector point = pointsIterator.next();
			ConsoleWriter.write("\n" + indent1 + "{ \"x\": " + point.x + ", \"y\": " + point.y
					+ "}");
			if (pointsIterator.hasNext()) {
				ConsoleWriter.write(",");
			}
		}
		ConsoleWriter.write("\n" + indent0 + "]");
	}

	/**
	 * Writes the given ports as JSON formatted string to the given
	 * ConsoleWriter.
	 *
	 * @param ConsoleWriter
	 *            the ConsoleWriter to write to.
	 * @param ports
	 *            the ports to write.
	 * @param indentation
	 *            the indentation level to use.
	 * @throws IOException
	 *             if anything goes wrong with the ConsoleWriter.
	 */
	private static void writePorts(final ConsoleWriter ConsoleWriter, final List<LPort> ports,
			final int indentation) throws IOException {

		final String indent0 = Strings.repeat(INDENT, indentation);
		final String indent1 = Strings.repeat(INDENT, indentation + 1);
		final String indent2 = Strings.repeat(INDENT, indentation + 2);
		ConsoleWriter.write(indent0 + "\"ports\": [");
		final Iterator<LPort> portsIterator = ports.iterator();
		while (portsIterator.hasNext()) {
			final LPort port = portsIterator.next();
			ConsoleWriter.write("\n" + indent1 + "{\n" + indent2 + "\"id\": \"p" + port.hashCode()
					+ "\",\n" + indent2 + "\"width\": " + port.getSize().x + ",\n" + indent2
					+ "\"height\": " + port.getSize().y + ",\n" + indent2 + "\"x\": "
					+ port.getPosition().x + ",\n" + indent2 + "\"y\": " + port.getPosition().y
					+ ",\n");
			writeProperties(ConsoleWriter, port.getAllProperties(), indentation + 2);
			ConsoleWriter.write("\n" + indent1 + "}");
			if (portsIterator.hasNext()) {
				ConsoleWriter.write(",");
			}
		}
		ConsoleWriter.write("\n" + indent0 + "]");
	}

	/**
	 * Writes the given properties as JSON formatted string to the given
	 * ConsoleWriter.
	 *
	 * @param ConsoleWriter
	 *            the ConsoleWriter to write to.
	 * @param properties
	 *            the properties to write.
	 * @param indentation
	 *            the indentation level to use.
	 * @throws IOException
	 *             if anything goes wrong with the ConsoleWriter.
	 */
	private static void writeProperties(final ConsoleWriter ConsoleWriter,
			final Map<IProperty<?>, Object> properties, final int indentation) throws IOException {

		final String indent0 = Strings.repeat(INDENT, indentation);
		final String indent1 = Strings.repeat(INDENT, indentation + 1);
		ConsoleWriter.write(indent0 + "\"properties\": {");
		final Iterator<Entry<IProperty<?>, Object>> propertiesIterator = properties.entrySet()
				.iterator();
		while (propertiesIterator.hasNext()) {
			final Entry<IProperty<?>, Object> property = propertiesIterator.next();
			final String id = property.getKey().getId();
			// Test whether the given id is a registered layout option. If not,
			// prefix the id with
			// DEBUG_ID_PREFIX
			final String val = getValueRepresentation(property.getKey(), property.getValue());
			ConsoleWriter.write("\n" + indent1 + "\"" + id + "\": \"" + val.replace("\"", "\\\"")
					+ "\"");
			if (propertiesIterator.hasNext()) {
				ConsoleWriter.write(",");
			}
		}
		ConsoleWriter.write("\n" + indent0 + "}");
	}

	/**
	 * Returns the value of the given property formatted for debug output. For
	 * example for layout processors, only the class' simple name instead of the
	 * qualified name is returned.
	 *
	 * @param property
	 *            the property whose value to get.
	 * @param value
	 *            the value to format.
	 * @return the formatted value.
	 */
	@SuppressWarnings("unchecked")
	private static String getValueRepresentation(final IProperty<?> property, final Object value) {
		if (property.getId().equals(InternalProperties.PROCESSORS.getId())) {
			final Iterator<ILayoutProcessor> processors = ((List<ILayoutProcessor>) value)
					.iterator();
			final StringBuffer result = new StringBuffer("[");
			while (processors.hasNext()) {
				final ILayoutProcessor processor = processors.next();
				result.append(processor.getClass().getSimpleName());
				if (processors.hasNext()) {
					result.append(", ");
				}
			}
			result.append("]");
			return result.toString();
		}
		return value.toString();
	}

	/**
	 * Returns the name of the node. The layer and index in the layer is added
	 * in parentheses. If its a dummy node, it get a suffix
	 * {@code DUMMY: <NodeType>}.
	 *
	 * @param node
	 *            the node whose name to get.
	 * @param layer
	 *            the layer of the node.
	 * @param index
	 *            the index inside the layer.
	 * @return the nodes name.
	 */
	private static String getNodeName(final LNode node, final int layer, final int index) {
		String name = "";
		if (node.getNodeType() == NodeType.NORMAL) {
			// Normal nodes display their name, if any
			if (node.getName() != null) {
				name = node.getName().replace("\"", "\\\"");
			}
			name += " (" + layer + "," + index + ")";
		} else {
			// Dummy nodes show their name (if set), or their node ID
			if (node.getName() != null) {
				name = node.getName().replace("\"", "\\\"");
			} else {
				name = "n_" + node.id;
			}
			if (node.getNodeType() == NodeType.NORTH_SOUTH_PORT) {
				final Object origin = node.getProperty(InternalProperties.ORIGIN);
				if (origin instanceof LNode) {
					name += "(" + ((LNode) origin).toString() + ")";
				}
			}
			name += " (" + layer + "," + index + ")";
			name += "\\n DUMMY: " + node.getNodeType().name();
		}
		return name;
	}

	/**
	 * Creates a ConsoleWriter for debug output.
	 *
	 * @param layeredGraph
	 *            the layered graph.
	 * @return a file ConsoleWriter for debug output.
	 * @throws IOException
	 *             if creating the output file fails.
	 */
	private static ConsoleWriter createConsoleWriter(final LGraph layeredGraph) throws IOException {
		final String debugFileName = getDebugOutputFileBaseName(layeredGraph) + "linseg-dep";
		return new ConsoleWriter(debugFileName + ".dot");
	}

	/**
	 * Creates a ConsoleWriter for the given graph. The file name to be written
	 * to is assembled from the graph's hash code and the slot index.
	 *
	 * @param graph
	 *            the graph to be written.
	 * @param slotIndex
	 *            the slot before whose execution the graph is written.
	 * @param name
	 *            the name the slot before whose execution the graph is written.
	 * @return file ConsoleWriter.
	 * @throws IOException
	 *             if anything goes wrong.
	 */
	private static ConsoleWriter createConsoleWriter(final LGraph graph, final int slotIndex,
			final String name) throws IOException {

		final String debugFileName = getDebugOutputFileBaseName(graph) + "fulldebug-slot"
				+ slotIndex + "-" + name;
		return new ConsoleWriter(debugFileName + ".json");
	}

	/**
	 * Create a ConsoleWriter for debug output.
	 *
	 * @param layeredGraph
	 *            the layered graph
	 * @param layerIndex
	 *            the currently processed layer's index
	 * @param debugPrefix
	 *            prefix of debug output files
	 * @param label
	 *            a label to append to the output files
	 * @return a file ConsoleWriter for debug output
	 * @throws IOException
	 *             if creating the output file fails
	 */
	private static ConsoleWriter createConsoleWriter(final LGraph layeredGraph,
			final int layerIndex, final String debugPrefix, final String label) throws IOException {
		final String debugFileName = getDebugOutputFileBaseName(layeredGraph) + debugPrefix + "-l"
				+ layerIndex + "-" + label;
		return new ConsoleWriter(debugFileName + ".json");
	}

	/**
	 * Returns the beginning of the file name used for debug output graphs while
	 * layouting the given layered graph. This will look something like
	 * {@code "143293-"}.
	 *
	 * @param graph
	 *            the graph to return the base debug file name for.
	 * @return the base debug file name for the given graph.
	 */
	private static String getDebugOutputFileBaseName(final LGraph graph) {
		return "graph-";
	}

}
