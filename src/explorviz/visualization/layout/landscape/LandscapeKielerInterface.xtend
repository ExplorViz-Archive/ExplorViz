package explorviz.visualization.layout.landscape

import de.cau.cs.kieler.core.alg.BasicProgressMonitor
import de.cau.cs.kieler.core.math.KVector
import de.cau.cs.kieler.kiml.options.Direction
import de.cau.cs.kieler.kiml.options.EdgeRouting
import de.cau.cs.kieler.kiml.options.LayoutOptions
import de.cau.cs.kieler.kiml.options.PortConstraints
import de.cau.cs.kieler.kiml.options.PortSide
import de.cau.cs.kieler.klay.layered.KlayLayered
import de.cau.cs.kieler.klay.layered.graph.LEdge
import de.cau.cs.kieler.klay.layered.graph.LGraph
import de.cau.cs.kieler.klay.layered.graph.LGraphElement.HashCodeCounter
import de.cau.cs.kieler.klay.layered.graph.LNode
import de.cau.cs.kieler.klay.layered.graph.LPort
import de.cau.cs.kieler.klay.layered.p4nodes.NodePlacementStrategy
import de.cau.cs.kieler.klay.layered.properties.GraphProperties
import de.cau.cs.kieler.klay.layered.properties.Properties
import explorviz.visualization.layout.exceptions.LayoutException
import explorviz.visualization.model.ApplicationClientSide
import explorviz.visualization.model.LandscapeClientSide
import explorviz.visualization.model.NodeClientSide
import explorviz.visualization.model.NodeGroupClientSide
import explorviz.visualization.model.SystemClientSide
import explorviz.visualization.model.helper.DrawNodeEntity
import explorviz.visualization.model.helper.Point
import java.util.EnumSet
import java.util.Map
import de.cau.cs.kieler.klay.layered.graph.LGraphUtil
import de.cau.cs.kieler.klay.layered.p3order.CrossingMinimizationStrategy

class LandscapeKielerInterface {
	var static LGraph topLevelKielerGraph = null

	val static DEFAULT_WIDTH = 1.5f
	val static DEFAULT_HEIGHT = 0.75f

	val static DEFAULT_PORT_WIDTH = 0.001f
	val static DEFAULT_PORT_HEIGHT = 0.001f

	val static SPACING = 0.25f
	val static PADDING = 0.1f

	val static CONVERT_TO_KIELER_FACTOR = 180f

	val static hashCodeCounter = new HashCodeCounter()

	def static applyLayout(LandscapeClientSide landscape) throws LayoutException {
		setupKieler(landscape, new KlayLayered(), new BasicProgressMonitor())

		updateGraphWithResults(landscape)
		landscape
	}

	def private static setupKieler(LandscapeClientSide landscape, KlayLayered layouter, BasicProgressMonitor monitor) throws LayoutException {
		topLevelKielerGraph = new LGraph(hashCodeCounter)

		setLayoutPropertiesGraph(topLevelKielerGraph)

		addNodes(landscape)
		addEdges(landscape)

		layouter.doCompoundLayout(topLevelKielerGraph, new BasicProgressMonitor())
	}

	def private static setLayoutPropertiesGraph(LGraph graph) {
		graph.setProperty(LayoutOptions::EDGE_ROUTING, EdgeRouting::POLYLINE)
		graph.setProperty(LayoutOptions::SPACING, SPACING * CONVERT_TO_KIELER_FACTOR)
		graph.setProperty(LayoutOptions::BORDER_SPACING, SPACING * CONVERT_TO_KIELER_FACTOR)
		graph.setProperty(LayoutOptions::DIRECTION, Direction::RIGHT)
		graph.setProperty(LayoutOptions::PORT_CONSTRAINTS, PortConstraints::FIXED_ORDER)

		graph.setProperty(Properties::NODE_PLACER, NodePlacementStrategy::LINEAR_SEGMENTS)
		graph.setProperty(Properties::EDGE_SPACING_FACTOR, 1.5f)
		graph.setProperty(Properties::GRAPH_PROPERTIES, EnumSet::noneOf(typeof(GraphProperties)))
	}

	def private static void addNodes(LandscapeClientSide landscape) {
		for (system : landscape.systems) {
			system.sourcePorts.clear()
			system.targetPorts.clear()

			if (!system.opened) { // TODO not working
				val systemKielerNode = new LNode(topLevelKielerGraph)
				topLevelKielerGraph.layerlessNodes.add(systemKielerNode)

				val sizeVector = systemKielerNode.size
				sizeVector.x = 2 * DEFAULT_WIDTH * CONVERT_TO_KIELER_FACTOR
				sizeVector.y = 2 * DEFAULT_HEIGHT * CONVERT_TO_KIELER_FACTOR

				system.kielerNodeReference = systemKielerNode
			} else {
				if (system.nodeGroups.size() > 1) {
					val systemKielerNode = new LNode(topLevelKielerGraph)
					topLevelKielerGraph.layerlessNodes.add(systemKielerNode)
					system.kielerNodeReference = systemKielerNode

					val systemKielerGraph = new LGraph(hashCodeCounter)
					system.kielerGraphReference = systemKielerGraph
					system.kielerGraphReference.setProperty(Properties::PARENT_LNODE, systemKielerNode)
					system.kielerNodeReference.setProperty(Properties::NESTED_LGRAPH, systemKielerGraph)
					setLayoutPropertiesGraph(systemKielerGraph)

					val insets = systemKielerGraph.insets
					insets.left = PADDING * CONVERT_TO_KIELER_FACTOR
					insets.right = 2 * PADDING * CONVERT_TO_KIELER_FACTOR
					insets.top = 9 * PADDING * CONVERT_TO_KIELER_FACTOR
					insets.bottom = PADDING * CONVERT_TO_KIELER_FACTOR

					// TODO init graph properties
					for (nodeGroup : system.nodeGroups) {
						nodeGroup.sourcePorts.clear()
						nodeGroup.targetPorts.clear()
						if (nodeGroup.visible) {
							createNodeGroup(systemKielerGraph, nodeGroup)
						}
					}
				} else {
					for (nodeGroup : system.nodeGroups) {
						nodeGroup.sourcePorts.clear()
						nodeGroup.targetPorts.clear()
						if (nodeGroup.visible) {
							createNodeGroup(topLevelKielerGraph, nodeGroup)
						}
					}
				}
			}
		}
	}

	def private static createNodeGroup(LGraph parentGraph, NodeGroupClientSide nodeGroup) {
		if (nodeGroup.nodes.size() > 1) {
			val nodeGroupKielerNode = new LNode(parentGraph)
			parentGraph.layerlessNodes.add(nodeGroupKielerNode)

			nodeGroup.kielerNodeReference = nodeGroupKielerNode

			val nodeGroupKielerGraph = new LGraph(hashCodeCounter)
			nodeGroup.kielerGraphReference = nodeGroupKielerGraph
			nodeGroup.kielerGraphReference.setProperty(Properties::PARENT_LNODE, nodeGroupKielerNode)
			nodeGroup.kielerNodeReference.setProperty(Properties::NESTED_LGRAPH, nodeGroupKielerGraph)
			setLayoutPropertiesGraph(nodeGroupKielerGraph)

			nodeGroupKielerGraph.setProperty(Properties::CROSS_MIN, CrossingMinimizationStrategy::INTERACTIVE)

			val insets = nodeGroupKielerGraph.insets
			insets.left = PADDING * CONVERT_TO_KIELER_FACTOR
			insets.right = PADDING * CONVERT_TO_KIELER_FACTOR
			insets.top = PADDING * CONVERT_TO_KIELER_FACTOR
			insets.bottom = PADDING * CONVERT_TO_KIELER_FACTOR

			nodeGroup.nodes.sortInplaceBy[it.ipAddress]

			var yCoord = 0d

			for (node : nodeGroup.nodes) {
				node.sourcePorts.clear()
				node.targetPorts.clear()
				if (node.visible) {
					createNodeAndItsApplications(nodeGroupKielerGraph, node)
					val position = node.kielerNodeReference.position
					position.x = 0
					position.y = yCoord
					yCoord = yCoord + 3000d
				}
			}
		} else {
			for (node : nodeGroup.nodes) {
				node.sourcePorts.clear()
				node.targetPorts.clear()
				if (node.visible) {
					createNodeAndItsApplications(parentGraph, node)
				}
			}
		}
	}

	def private static createNodeAndItsApplications(LGraph parentGraph, NodeClientSide node) {
		val nodeKielerNode = new LNode(parentGraph)
		parentGraph.layerlessNodes.add(nodeKielerNode)

		node.kielerNodeReference = nodeKielerNode

		val nodeKielerGraph = new LGraph(hashCodeCounter)
		node.kielerGraphReference = nodeKielerGraph
		node.kielerGraphReference.setProperty(Properties::PARENT_LNODE, nodeKielerNode)
		node.kielerNodeReference.setProperty(Properties::NESTED_LGRAPH, nodeKielerGraph)
		setLayoutPropertiesGraph(nodeKielerGraph)

		val insets = nodeKielerGraph.insets
		insets.left = PADDING * CONVERT_TO_KIELER_FACTOR
		insets.right = 2 * PADDING * CONVERT_TO_KIELER_FACTOR
		insets.top = PADDING * CONVERT_TO_KIELER_FACTOR
		insets.bottom = 8 * PADDING * CONVERT_TO_KIELER_FACTOR

		for (application : node.applications) {
			application.sourcePorts.clear()
			application.targetPorts.clear()

			val applicationKielerNode = new LNode(nodeKielerGraph)
			nodeKielerGraph.layerlessNodes.add(applicationKielerNode)
			val applicationLayout = applicationKielerNode.size
			applicationLayout.x = DEFAULT_WIDTH * CONVERT_TO_KIELER_FACTOR
			applicationLayout.y = DEFAULT_HEIGHT * CONVERT_TO_KIELER_FACTOR

			application.kielerNodeReference = applicationKielerNode
		}

		node
	}

	def private static addEdges(LandscapeClientSide landscape) {
		for (communication : landscape.applicationCommunication) {
			communication.kielerEdgeReferences.clear()

			var appSource = communication.source
			var appTarget = communication.target

			if (appSource.parent.visible && appTarget.parent.visible) {
				communication.kielerEdgeReferences.add(createEdgeBetweenSourceTarget(appSource, appTarget))
			}
		}
	}

	def private static createEdgeBetweenSourceTarget(ApplicationClientSide source, ApplicationClientSide target) {
		var LPort port1 = createSourcePortIfNotExisting(source)
		val LPort port2 = createTargetPortIfNotExisting(target)

		createEdgeHelper(source, port1, target, port2)
	}

	def private static createEdgeHelper(ApplicationClientSide source, LPort port1, ApplicationClientSide target,
		LPort port2) {
		var LGraph parentGraph = findGraphFromParent(source)

		val kielerEdge = new LEdge(parentGraph)
		kielerEdge.setSource(port1)
		kielerEdge.setTarget(port2)
		kielerEdge
	}

	def private static LGraph findGraphFromParent(DrawNodeEntity source) {
		var LGraph parentGraph = null
		if (source instanceof SystemClientSide) {
			parentGraph = topLevelKielerGraph
		} else if (source instanceof NodeGroupClientSide) {
			val systemClientSide = (source as NodeGroupClientSide).parent
			if (systemClientSide.kielerGraphReference != null) {
				parentGraph = systemClientSide.kielerGraphReference
			} else {
				parentGraph = findGraphFromParent(systemClientSide)
			}
		} else if (source instanceof NodeClientSide) {
			val nodeGroupClientSide = (source as NodeClientSide).parent
			if (nodeGroupClientSide.kielerGraphReference != null) {
				parentGraph = nodeGroupClientSide.kielerGraphReference
			} else {
				parentGraph = findGraphFromParent(nodeGroupClientSide)
			}
		} else if (source instanceof ApplicationClientSide) {
			val nodeClientSide = (source as ApplicationClientSide).parent
			if (nodeClientSide.kielerGraphReference != null) {
				parentGraph = nodeClientSide.kielerGraphReference
			} else {
				parentGraph = findGraphFromParent(nodeClientSide)
			}
		}

		return parentGraph
	}

	def private static LPort createSourcePortIfNotExisting(ApplicationClientSide source) {
		createPortHelper(source, source.sourcePorts, PortSide::EAST)
	}

	def private static LPort createTargetPortIfNotExisting(ApplicationClientSide target) {
		createPortHelper(target, target.targetPorts, PortSide::WEST)
	}

	def private static createPortHelper(ApplicationClientSide entity, Map<ApplicationClientSide, LPort> ports,
		PortSide portSide) {
		if (ports.get(entity) == null) {
			var LGraph parentGraph = findGraphFromParent(entity)
			val port = new LPort(parentGraph)
			port.setNode(entity.kielerNodeReference)

			val layout = port.size
			layout.x = DEFAULT_PORT_WIDTH * CONVERT_TO_KIELER_FACTOR
			layout.y = DEFAULT_PORT_HEIGHT * CONVERT_TO_KIELER_FACTOR
			port.side = portSide

			ports.put(entity, port)
		}
		ports.get(entity)
	}

	def private static void updateGraphWithResults(LandscapeClientSide landscape) {
		for (system : landscape.systems) {
			if (system.nodeGroups.size() > 1)
				updateNodeValues(system)
			for (nodeGroup : system.nodeGroups) {
				if (nodeGroup.visible) {
					if (nodeGroup.nodes.size() > 1)
						updateNodeValues(nodeGroup)

					if (system.nodeGroups.size() > 1)
						setAbsolutePositionForNode(nodeGroup, system)

					for (node : nodeGroup.nodes) {
						if (node.visible) {
							updateNodeValues(node)
							if (nodeGroup.nodes.size() > 1)
								setAbsolutePositionForNode(node, nodeGroup)
							if (system.nodeGroups.size() > 1 && nodeGroup.nodes.size() == 1)
								setAbsolutePositionForNode(node, system)
							for (application : node.applications) {
								updateNodeValues(application)
								setAbsolutePositionForNode(application, node)
							}
						}
					}
				}
			}
		}

		addBendPointsInAbsoluteCoordinates(landscape)

		for (system : landscape.systems) {
			for (nodeGroup : system.nodeGroups) {
				if (nodeGroup.visible) {
					for (node : nodeGroup.nodes) {
						if (node.visible) {
							for (application : node.applications) {
								convertToExplorVizCoords(application)
							}
							convertToExplorVizCoords(node)
						}
					}
					if (nodeGroup.nodes.size() > 1)
						convertToExplorVizCoords(nodeGroup)
				}
			}
			if (system.nodeGroups.size() > 1)
				convertToExplorVizCoords(system)
		}
	}

	def private static updateNodeValues(DrawNodeEntity node) {
		val layout = node.kielerNodeReference

		node.positionX = layout.position.x as float
		node.positionY = (layout.position.y as float) * -1 // KIELER has inverted Y coords

		node.width = layout.size.x as float
		node.height = layout.size.y as float
	}

	def private static setAbsolutePositionForNode(DrawNodeEntity node, DrawNodeEntity parent) {
		val insets = parent.kielerGraphReference.insets
		val offset = parent.kielerGraphReference.offset
		node.positionX = parent.positionX + node.positionX + insets.left as float + offset.x as float
		node.positionY = parent.positionY + node.positionY - insets.top as float - offset.y as float
	}

	def private static convertToExplorVizCoords(DrawNodeEntity node) {
		node.positionX = node.positionX / CONVERT_TO_KIELER_FACTOR
		node.positionY = node.positionY / CONVERT_TO_KIELER_FACTOR

		node.width = node.width / CONVERT_TO_KIELER_FACTOR
		node.height = node.height / CONVERT_TO_KIELER_FACTOR
	}

	def private static void addBendPointsInAbsoluteCoordinates(LandscapeClientSide landscape) {
		for (communication : landscape.applicationCommunication) {
			if (communication.source.parent.visible && communication.target.parent.visible) {
				communication.points.clear()

				communication.kielerEdgeReferences.forEach [ LEdge edge, index |
					if (edge != null) {
						var parentNode = communication.source.parent
						val points = edge.getBendPoints()

						//						points.add(edge.source.position)
						var edgeOffset = parentNode.kielerGraphReference.offset
						var KVector sourcePoint = null

						if (LGraphUtil::isDescendant(edge.getTarget().getNode(), edge.getSource().getNode())) {
							var LPort sourcePort = edge.getSource();
							sourcePoint = KVector.sum(sourcePort.getPosition(), sourcePort.getAnchor());
							var sourceInsets = sourcePort.getNode().getInsets();
							sourcePoint.translate(-sourceInsets.left, -sourceInsets.top);
							var nestedGraph = sourcePort.getNode().getProperty(Properties.NESTED_LGRAPH);
							if (nestedGraph != null) {
								edgeOffset = nestedGraph.getOffset();
							}
							sourcePoint.sub(edgeOffset);
						} else {
							sourcePoint = edge.getSource().getAbsoluteAnchor();
						}

						points.addFirst(sourcePoint)

						var targetPoint = edge.getTarget().getAbsoluteAnchor();
						if (edge.getProperty(Properties.TARGET_OFFSET) != null) {
							targetPoint.add(edge.getProperty(Properties.TARGET_OFFSET));
						}
						points.addLast(targetPoint)
						points.translate(edgeOffset)

						//						points.scale(1,-1)
						var pOffsetX = 0f
						var pOffsetY = 0f
						if (parentNode != null && parentNode.kielerGraphReference != null) {
							val insets = parentNode.kielerGraphReference.insets
							pOffsetX = parentNode.positionX + insets.left as float
							pOffsetY = parentNode.positionY - insets.top as float
						}
						for (point : points) {
							val resultPoint = new Point()
							resultPoint.x = (point.x as float + pOffsetX) / CONVERT_TO_KIELER_FACTOR
							resultPoint.y = (point.y as float * -1 + pOffsetY) / CONVERT_TO_KIELER_FACTOR // KIELER has inverted Y coords
							communication.points.add(resultPoint)
						}
					}
				]
			}
		}
	}
}
