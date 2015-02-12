package explorviz.visualization.layout.landscape

import de.cau.cs.kieler.core.alg.BasicProgressMonitor
import de.cau.cs.kieler.core.math.KVector
import de.cau.cs.kieler.kiml.options.Direction
import de.cau.cs.kieler.kiml.options.EdgeRouting
import de.cau.cs.kieler.kiml.options.LayoutOptions
import de.cau.cs.kieler.kiml.options.PortSide
import de.cau.cs.kieler.kiml.options.SizeConstraint
import de.cau.cs.kieler.klay.layered.KlayLayered
import de.cau.cs.kieler.klay.layered.graph.LEdge
import de.cau.cs.kieler.klay.layered.graph.LGraph
import de.cau.cs.kieler.klay.layered.graph.LGraphElement.HashCodeCounter
import de.cau.cs.kieler.klay.layered.graph.LGraphUtil
import de.cau.cs.kieler.klay.layered.graph.LNode
import de.cau.cs.kieler.klay.layered.graph.LPort
import de.cau.cs.kieler.klay.layered.p3order.CrossingMinimizationStrategy
import de.cau.cs.kieler.klay.layered.p4nodes.NodePlacementStrategy
import de.cau.cs.kieler.klay.layered.properties.GraphProperties
import de.cau.cs.kieler.klay.layered.properties.InternalProperties
import de.cau.cs.kieler.klay.layered.properties.Properties
import explorviz.shared.model.Application
import explorviz.shared.model.Landscape
import explorviz.shared.model.Node
import explorviz.shared.model.NodeGroup
import explorviz.shared.model.System
import explorviz.shared.model.helper.DrawNodeEntity
import explorviz.shared.model.helper.Point
import explorviz.visualization.engine.primitives.Label
import explorviz.visualization.layout.exceptions.LayoutException
import explorviz.visualization.renderer.LandscapeRenderer
import java.util.EnumSet
import java.util.Map
import de.cau.cs.kieler.klay.layered.properties.ContentAlignment

class LandscapeKielerInterface {
	var static LGraph topLevelKielerGraph = null

	val static DEFAULT_WIDTH = 1.5f
	val static DEFAULT_HEIGHT = 0.75f

	val static DEFAULT_PORT_WIDTH = 0.000001f
	val static DEFAULT_PORT_HEIGHT = 0.000001f

	val static SPACING = 0.2f
	val static PADDING = 0.1f

	val static CONVERT_TO_KIELER_FACTOR = 180f

	val static hashCodeCounter = new HashCodeCounter()

	def static applyLayout(Landscape landscape) throws LayoutException {
		setupKieler(landscape, new KlayLayered(), new BasicProgressMonitor())

		updateGraphWithResults(landscape)
		landscape
	}

	def private static setupKieler(Landscape landscape, KlayLayered layouter, BasicProgressMonitor monitor) throws LayoutException {
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
		graph.setProperty(LayoutOptions::INTERACTIVE, true)

		graph.setProperty(Properties::NODE_PLACER, NodePlacementStrategy::LINEAR_SEGMENTS)
		graph.setProperty(Properties::ADD_UNNECESSARY_BENDPOINTS, true)

		graph.setProperty(Properties::EDGE_SPACING_FACTOR, 1.0f)
		graph.setProperty(InternalProperties::GRAPH_PROPERTIES, EnumSet::noneOf(typeof(GraphProperties)))
	}

	def private static void addNodes(Landscape landscape) {
		for (system : landscape.systems) {
			system.sourcePorts.clear()
			system.targetPorts.clear()

			if (!system.opened) {
				val systemKielerNode = new LNode(topLevelKielerGraph)
				topLevelKielerGraph.layerlessNodes.add(systemKielerNode)
				system.kielerNodeReference = systemKielerNode
				system.kielerGraphReference = null

				val sizeVector = systemKielerNode.size
				sizeVector.x = Math.max(2.5 * DEFAULT_WIDTH * CONVERT_TO_KIELER_FACTOR,
					(Label::calculateRequiredLength(system.name, LandscapeRenderer::SYSTEM_LABEL_HEIGHT) +
						PADDING * 6f) * CONVERT_TO_KIELER_FACTOR)
				sizeVector.y = 2.5 * DEFAULT_HEIGHT * CONVERT_TO_KIELER_FACTOR
			} else {
				val systemKielerNode = new LNode(topLevelKielerGraph)
				topLevelKielerGraph.layerlessNodes.add(systemKielerNode)
				system.kielerNodeReference = systemKielerNode

				val systemKielerGraph = new LGraph(hashCodeCounter)
				system.kielerGraphReference = systemKielerGraph
				system.kielerGraphReference.setProperty(InternalProperties::PARENT_LNODE, systemKielerNode)
				system.kielerNodeReference.setProperty(InternalProperties::NESTED_LGRAPH, systemKielerGraph)
				setLayoutPropertiesGraph(systemKielerGraph)

				val insets = systemKielerGraph.insets
				insets.left = PADDING * CONVERT_TO_KIELER_FACTOR
				insets.right = PADDING * CONVERT_TO_KIELER_FACTOR
				insets.top = 8 * PADDING * CONVERT_TO_KIELER_FACTOR
				insets.bottom = PADDING * CONVERT_TO_KIELER_FACTOR

				systemKielerGraph.setProperty(LayoutOptions::SIZE_CONSTRAINT, SizeConstraint::minimumSize)
				systemKielerGraph.setProperty(LayoutOptions::MIN_WIDTH,
					Math.max(2.5 * DEFAULT_WIDTH * CONVERT_TO_KIELER_FACTOR,
						(Label::calculateRequiredLength(system.name, LandscapeRenderer::SYSTEM_LABEL_HEIGHT) +
							PADDING * 6f) * CONVERT_TO_KIELER_FACTOR) as float)
				systemKielerGraph.setProperty(LayoutOptions::MIN_HEIGHT,
					(2.5 * DEFAULT_HEIGHT * CONVERT_TO_KIELER_FACTOR) as float)
				systemKielerGraph.setProperty(Properties::CONTENT_ALIGNMENT, ContentAlignment::centerCenter)

				for (nodeGroup : system.nodeGroups) {
					nodeGroup.sourcePorts.clear()
					nodeGroup.targetPorts.clear()
					if (nodeGroup.visible) {
						createNodeGroup(systemKielerGraph, nodeGroup)
					}
				}
			}
		}
	}

	def private static createNodeGroup(LGraph parentGraph, NodeGroup nodeGroup) {
		if (nodeGroup.nodes.size() > 1) {
			val nodeGroupKielerNode = new LNode(parentGraph)
			parentGraph.layerlessNodes.add(nodeGroupKielerNode)

			nodeGroup.kielerNodeReference = nodeGroupKielerNode

			val nodeGroupKielerGraph = new LGraph(hashCodeCounter)
			nodeGroup.kielerGraphReference = nodeGroupKielerGraph
			nodeGroup.kielerGraphReference.setProperty(InternalProperties::PARENT_LNODE, nodeGroupKielerNode)
			nodeGroup.kielerNodeReference.setProperty(InternalProperties::NESTED_LGRAPH, nodeGroupKielerGraph)
			setLayoutPropertiesGraph(nodeGroupKielerGraph)

			nodeGroupKielerGraph.setProperty(Properties::CROSS_MIN, CrossingMinimizationStrategy::LAYER_SWEEP)

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
					yCoord = yCoord + CONVERT_TO_KIELER_FACTOR
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

	def private static createNodeAndItsApplications(LGraph parentGraph, Node node) {
		val nodeKielerNode = new LNode(parentGraph)
		parentGraph.layerlessNodes.add(nodeKielerNode)
		node.kielerNodeReference = nodeKielerNode

		val nodeKielerGraph = new LGraph(hashCodeCounter)
		node.kielerGraphReference = nodeKielerGraph
		node.kielerGraphReference.setProperty(InternalProperties::PARENT_LNODE, nodeKielerNode)
		node.kielerNodeReference.setProperty(InternalProperties::NESTED_LGRAPH, nodeKielerGraph)
		setLayoutPropertiesGraph(nodeKielerGraph)

		val insets = nodeKielerGraph.insets
		insets.left = PADDING * CONVERT_TO_KIELER_FACTOR
		insets.right = PADDING * CONVERT_TO_KIELER_FACTOR
		insets.top = PADDING * CONVERT_TO_KIELER_FACTOR
		insets.bottom = 6 * PADDING * CONVERT_TO_KIELER_FACTOR

		nodeKielerGraph.setProperty(LayoutOptions::SIZE_CONSTRAINT, SizeConstraint::minimumSize)
		nodeKielerGraph.setProperty(LayoutOptions::MIN_WIDTH,
			Math.max(DEFAULT_WIDTH * CONVERT_TO_KIELER_FACTOR,
				(Label::calculateRequiredLength(node.displayName, LandscapeRenderer::NODE_LABEL_HEIGHT) +
					PADDING * 2f) * CONVERT_TO_KIELER_FACTOR) as float)
		nodeKielerGraph.setProperty(LayoutOptions::MIN_HEIGHT, (DEFAULT_HEIGHT * CONVERT_TO_KIELER_FACTOR) as float)
		nodeKielerGraph.setProperty(Properties::CONTENT_ALIGNMENT, ContentAlignment::centerCenter)

		for (application : node.applications) {
			application.sourcePorts.clear()
			application.targetPorts.clear()

			val applicationKielerNode = new LNode(nodeKielerGraph)
			nodeKielerGraph.layerlessNodes.add(applicationKielerNode)
			val applicationLayout = applicationKielerNode.size
			applicationLayout.x = Math.max(DEFAULT_WIDTH * CONVERT_TO_KIELER_FACTOR,
				(Label::calculateRequiredLength(application.name, LandscapeRenderer::APPLICATION_LABEL_HEIGHT) +
					LandscapeRenderer::APPLICATION_PIC_PADDING_SIZE + LandscapeRenderer::APPLICATION_PIC_SIZE +
					PADDING * 3f) * CONVERT_TO_KIELER_FACTOR)
			applicationLayout.y = DEFAULT_HEIGHT * CONVERT_TO_KIELER_FACTOR

			application.kielerNodeReference = applicationKielerNode
		}

		node
	}

	def private static addEdges(Landscape landscape) {
		for (communication : landscape.applicationCommunication) {
			communication.kielerEdgeReferences.clear()
			communication.points.clear()

			var appSource = communication.source
			var appTarget = communication.target

			if (appSource.parent.visible && appTarget.parent.visible) {
				communication.kielerEdgeReferences.add(createEdgeBetweenSourceTarget(appSource, appTarget))
			} else if (appSource.parent.visible && !appTarget.parent.visible) {
				if (appTarget.parent.parent.parent.opened) {
					val representativeApplication = seekRepresentativeApplication(appTarget)
					communication.kielerEdgeReferences.add(
						createEdgeBetweenSourceTarget(
							appSource,
							representativeApplication
						))
				} else {

					// System is closed
					communication.kielerEdgeReferences.add(
						createEdgeBetweenSourceTarget(
							appSource,
							appTarget.parent.parent.parent
						))
				}
			} else if (!appSource.parent.visible && appTarget.parent.visible) {
				if (appSource.parent.parent.parent.opened) {
					val representativeApplication = seekRepresentativeApplication(appSource)
					communication.kielerEdgeReferences.add(
						createEdgeBetweenSourceTarget(
							representativeApplication,
							appTarget
						))
				} else {

					// System is closed
					communication.kielerEdgeReferences.add(
						createEdgeBetweenSourceTarget(
							appSource.parent.parent.parent,
							appTarget
						))
				}
			} else {
				if (appSource.parent.parent.parent.opened) {
					val representativeSourceApplication = seekRepresentativeApplication(appSource)

					if (appTarget.parent.parent.parent.opened) {
						val representativeTargetApplication = seekRepresentativeApplication(appTarget)
						communication.kielerEdgeReferences.add(
							createEdgeBetweenSourceTarget(representativeSourceApplication,
								representativeTargetApplication))
					} else {

						// Target System is closed
						communication.kielerEdgeReferences.add(
							createEdgeBetweenSourceTarget(representativeSourceApplication,
								appTarget.parent.parent.parent))
					}
				} else {

					// Source System is closed
					if (appTarget.parent.parent.parent.opened) {
						val representativeTargetApplication = seekRepresentativeApplication(appTarget)
						communication.kielerEdgeReferences.add(
							createEdgeBetweenSourceTarget(appSource.parent.parent.parent,
								representativeTargetApplication))
					} else {

						// Target System is closed
						communication.kielerEdgeReferences.add(
							createEdgeBetweenSourceTarget(
								appSource.parent.parent.parent,
								appTarget.parent.parent.parent
							))
					}
				}
			}
		}
	}

	private def static Application seekRepresentativeApplication(Application app) {
		for (node : app.parent.parent.nodes) {
			if (node.visible) {
				for (representiveApplication : node.applications) {
					if (representiveApplication.name == app.name) {
						return representiveApplication
					}
				}
			}
		}

		null
	}

	def private static createEdgeBetweenSourceTarget(DrawNodeEntity source, DrawNodeEntity target) {
		var LPort port1 = createSourcePortIfNotExisting(source)
		val LPort port2 = createTargetPortIfNotExisting(target)

		createEdgeHelper(source, port1, target, port2)
	}

	def private static createEdgeHelper(DrawNodeEntity source, LPort port1, DrawNodeEntity target, LPort port2) {
		var LGraph parentGraph = findGraphFromParent(source)

		val kielerEdge = new LEdge(parentGraph)
		kielerEdge.setSource(port1)
		kielerEdge.setTarget(port2)

		setEdgeLayoutProperties(kielerEdge)

		kielerEdge
	}

	def private static setEdgeLayoutProperties(LEdge edge) {
		val lineThickness = 0.06f * 4 + 0.01f
		val oldThickness = edge.getProperty(LayoutOptions.THICKNESS)
		edge.setProperty(LayoutOptions.THICKNESS, Math.max(lineThickness * CONVERT_TO_KIELER_FACTOR, oldThickness))
	}

	def private static LGraph findGraphFromParent(DrawNodeEntity source) {
		var LGraph parentGraph = null
		if (source instanceof System) {
			parentGraph = topLevelKielerGraph
		} else if (source instanceof NodeGroup) {
			val system = source.parent
			if (system.kielerGraphReference != null) {
				parentGraph = system.kielerGraphReference
			} else {
				parentGraph = findGraphFromParent(system)
			}
		} else if (source instanceof Node) {
			val nodeGroup = source.parent
			if (nodeGroup.kielerGraphReference != null) {
				parentGraph = nodeGroup.kielerGraphReference
			} else {
				parentGraph = findGraphFromParent(nodeGroup)
			}
		} else if (source instanceof Application) {
			val node = source.parent
			if (node.kielerGraphReference != null) {
				parentGraph = node.kielerGraphReference
			} else {
				parentGraph = findGraphFromParent(node)
			}
		}

		return parentGraph
	}

	def private static LPort createSourcePortIfNotExisting(DrawNodeEntity source) {
		createPortHelper(source, source.sourcePorts, PortSide::EAST)
	}

	def private static LPort createTargetPortIfNotExisting(DrawNodeEntity target) {
		createPortHelper(target, target.targetPorts, PortSide::WEST)
	}

	def private static createPortHelper(DrawNodeEntity entity, Map<DrawNodeEntity, LPort> ports, PortSide portSide) {
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

	def private static void updateGraphWithResults(Landscape landscape) {
		for (system : landscape.systems) {
			updateNodeValues(system)

			for (nodeGroup : system.nodeGroups) {
				if (nodeGroup.visible) {
					if (nodeGroup.nodes.size() > 1)
						updateNodeValues(nodeGroup)

					setAbsolutePositionForNode(nodeGroup, system)

					for (node : nodeGroup.nodes) {
						if (node.visible) {
							updateNodeValues(node)
							if (nodeGroup.nodes.size() > 1) {
								setAbsolutePositionForNode(node, nodeGroup)
							} else if (nodeGroup.nodes.size() == 1) {
								setAbsolutePositionForNode(node, system)
							}
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

	def private static void addBendPointsInAbsoluteCoordinates(Landscape landscape) {
		for (communication : landscape.applicationCommunication) {
			for (edge : communication.kielerEdgeReferences) {
				if (edge != null) {
					var DrawNodeEntity parentNode = getRightParent(communication.source, communication.target)
					if (parentNode != null) {
						val points = edge.getBendPoints()

						var edgeOffset = new KVector()
						if (parentNode.kielerGraphReference != null) {
							edgeOffset = parentNode.kielerGraphReference.offset
						}

						var KVector sourcePoint = null

						if (LGraphUtil::isDescendant(edge.getTarget().getNode(), edge.getSource().getNode())) {

							// self edges..
							var LPort sourcePort = edge.getSource();
							sourcePoint = KVector.sum(sourcePort.getPosition(), sourcePort.getAnchor());
							var sourceInsets = sourcePort.getNode().getInsets();
							sourcePoint.add(-sourceInsets.left, -sourceInsets.top);
							var nestedGraph = sourcePort.getNode().getProperty(InternalProperties.NESTED_LGRAPH);
							if (nestedGraph != null) {
								edgeOffset = nestedGraph.getOffset();
							}
							sourcePoint.sub(edgeOffset);
						} else {
							sourcePoint = edge.getSource().getAbsoluteAnchor();
						}

						points.addFirst(sourcePoint)

						var targetPoint = edge.getTarget().getAbsoluteAnchor();
						if (edge.getProperty(InternalProperties.TARGET_OFFSET) != null) {
							targetPoint.add(edge.getProperty(InternalProperties.TARGET_OFFSET));
						}
						points.addLast(targetPoint)
						for (point : points) {
							point.add(edgeOffset)
						}

						var pOffsetX = 0f
						var pOffsetY = 0f
						if (parentNode != null) {
							var insetLeft = 0f
							var insetTop = 0f

							if (parentNode.kielerGraphReference != null) {
								insetLeft = parentNode.kielerGraphReference.insets.left as float
								insetTop = parentNode.kielerGraphReference.insets.top as float
							}

							if (parentNode instanceof System) {
								pOffsetX = insetLeft
								pOffsetY = insetTop * -1
							} else {
								pOffsetX = parentNode.positionX + insetLeft
								pOffsetY = parentNode.positionY - insetTop

							}
						}

						for (point : points) {
							val resultPoint = new Point()
							resultPoint.x = (point.x as float + pOffsetX) / CONVERT_TO_KIELER_FACTOR
							resultPoint.y = (point.y as float * -1 + pOffsetY) / CONVERT_TO_KIELER_FACTOR // KIELER has inverted Y coords

							communication.points.add(resultPoint)
						}
					}
				}
			}
		}
	}

	private def static getRightParent(Application source, Application target) {
		var DrawNodeEntity result = source.parent
		if (!source.parent.visible) {
			if (!source.parent.parent.parent.opened) {
				if (source.parent.parent.parent != target.parent.parent.parent) {
					result = source.parent.parent.parent
				} else {
					result = null // means don't draw
				}
			} else {
				result = seekRepresentativeApplication(source)
				if (result != null) {
					result = (result as Application).parent
				}
			}
		}
		result
	}
}
