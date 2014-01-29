package explorviz.visualization.layout

import de.cau.cs.kieler.core.alg.BasicProgressMonitor

import de.cau.cs.kieler.core.kgraph.KNode

import de.cau.cs.kieler.kiml.util.KimlUtil

import de.cau.cs.kieler.klay.layered.LayeredLayoutProvider

import de.cau.cs.kieler.kiml.klayoutdata.KShapeLayout
import de.cau.cs.kieler.kiml.klayoutdata.KEdgeLayout

import de.cau.cs.kieler.kiml.options.LayoutOptions
import de.cau.cs.kieler.kiml.options.EdgeRouting
import de.cau.cs.kieler.kiml.options.Direction

import explorviz.visualization.layout.exceptions.LayoutException

import explorviz.visualization.model.LandscapeClientSide
import explorviz.visualization.model.CommunicationClientSide
import explorviz.visualization.model.helper.DrawNodeEntity
import explorviz.visualization.model.helper.Point
import explorviz.visualization.model.NodeClientSide
import de.cau.cs.kieler.kiml.options.PortConstraints
import de.cau.cs.kieler.core.kgraph.KPort
import de.cau.cs.kieler.kiml.options.PortSide
import de.cau.cs.kieler.kiml.klayoutdata.KPoint
import java.util.ArrayList
import de.cau.cs.kieler.kiml.klayoutdata.KInsets
import explorviz.visualization.model.ApplicationClientSide
import java.util.Map
import de.cau.cs.kieler.klay.layered.properties.Properties
import de.cau.cs.kieler.klay.layered.p4nodes.NodePlacementStrategy

class LandscapeKielerInterface {
	var static KNode kielerGraph = null

	val static DEFAULT_WIDTH = 1.5f
	val static DEFAULT_HEIGHT = 0.75f

	val static DEFAULT_PORT_WIDTH = 0.01f
	val static DEFAULT_PORT_HEIGHT = 0.01f

	val static DEFAULT_DATABASE_WIDTH = 1.25f
	val static DEFAULT_DATABASE_HEIGHT = 1.25f

	val static SPACING = 0.25f
	val static PADDING = 0.1f

	val static CONVERT_TO_KIELER_FACTOR = 180f

	def static applyLayout(LandscapeClientSide landscape) throws LayoutException {
		setupKieler(landscape, new LayeredLayoutProvider(), new BasicProgressMonitor())

		updateGraphWithResults(landscape)
		landscape
	}

	def private static setupKieler(LandscapeClientSide landscape, LayeredLayoutProvider layouter,
		BasicProgressMonitor monitor) throws LayoutException {
		kielerGraph = KimlUtil::createInitializedNode()
		setLayoutProperties(kielerGraph)

		addNodes(landscape)
		addEdges(landscape)

		for (nodeGroup : landscape.nodeGroups) {
			for (node : nodeGroup.nodes) {
				if (node.visible) {
					layouter.doLayout(node.kielerNodeReference, new BasicProgressMonitor())
				}
			}
			if (nodeGroup.nodes.size() > 1)
				layouter.doLayout(nodeGroup.kielerNodeReference, new BasicProgressMonitor())
		}
		layouter.doLayout(kielerGraph, new BasicProgressMonitor())
	}

	def private static setLayoutProperties(KNode node) {
		val layout = node.getData(typeof(KShapeLayout))
		layout.setProperty(LayoutOptions::EDGE_ROUTING, EdgeRouting::POLYLINE)
		layout.setProperty(LayoutOptions::SPACING, SPACING * CONVERT_TO_KIELER_FACTOR)
		layout.setProperty(LayoutOptions::BORDER_SPACING, SPACING * CONVERT_TO_KIELER_FACTOR)
		layout.setProperty(LayoutOptions::DIRECTION, Direction::RIGHT)
		layout.setProperty(LayoutOptions::PORT_CONSTRAINTS, PortConstraints::FIXED_ORDER)
		layout.setProperty(Properties::NODE_PLACER, NodePlacementStrategy::LINEAR_SEGMENTS)
	}

	def private static void addNodes(LandscapeClientSide landscape) {
		for (nodeGroup : landscape.nodeGroups) {
			nodeGroup.sourcePorts.clear()
			nodeGroup.targetPorts.clear()

			if (nodeGroup.nodes.size() > 1) {
				val nodeGroupKielerNode = KimlUtil::createInitializedNode()
				setLayoutProperties(nodeGroupKielerNode)
				val insets = nodeGroupKielerNode.getData(typeof(KShapeLayout)).insets
				setInsets(insets)

				nodeGroupKielerNode.setParent(kielerGraph)

				nodeGroup.kielerNodeReference = nodeGroupKielerNode

				for (node : nodeGroup.nodes) {
					node.sourcePorts.clear()
					node.targetPorts.clear()
					if (node.visible) {
						createNodeAndItsApplications(nodeGroupKielerNode, node)
					}
				}
			} else {
				for (node : nodeGroup.nodes) {
					node.sourcePorts.clear()
					node.targetPorts.clear()
					if (node.visible) {
						createNodeAndItsApplications(kielerGraph, node)
					}
				}
			}
		}
	}

	def private static setInsets(KInsets insets) {
//		insets.left = PADDING * CONVERT_TO_KIELER_FACTOR
//		insets.right = PADDING * CONVERT_TO_KIELER_FACTOR
//		insets.top = PADDING * CONVERT_TO_KIELER_FACTOR
//		insets.bottom = PADDING * CONVERT_TO_KIELER_FACTOR
	}

	def private static createNodeAndItsApplications(KNode nodeGroupKielerNode, NodeClientSide node) {
		val nodeKielerNode = KimlUtil::createInitializedNode()
		nodeKielerNode.setParent(nodeGroupKielerNode)
		setLayoutProperties(nodeKielerNode)
		val insets = nodeKielerNode.getData(typeof(KShapeLayout)).insets
		insets.left = PADDING * CONVERT_TO_KIELER_FACTOR
		insets.right = 2 * PADDING * CONVERT_TO_KIELER_FACTOR
		insets.top = PADDING * CONVERT_TO_KIELER_FACTOR
		insets.bottom = 6 * PADDING * CONVERT_TO_KIELER_FACTOR

		node.kielerNodeReference = nodeKielerNode

		for (application : node.applications) {
			application.sourcePorts.clear()
			application.targetPorts.clear()

			val applicationKielerNode = KimlUtil::createInitializedNode()
			applicationKielerNode.setParent(nodeKielerNode)

			val applicationLayout = applicationKielerNode.getData(typeof(KShapeLayout))
			if (application.database) {
				applicationLayout.setWidth(DEFAULT_DATABASE_WIDTH * CONVERT_TO_KIELER_FACTOR)
				applicationLayout.setHeight(DEFAULT_DATABASE_HEIGHT * CONVERT_TO_KIELER_FACTOR)
			} else {
				applicationLayout.setWidth(DEFAULT_WIDTH * CONVERT_TO_KIELER_FACTOR)
				applicationLayout.setHeight(DEFAULT_HEIGHT * CONVERT_TO_KIELER_FACTOR)
			}

			application.kielerNodeReference = applicationKielerNode
		}
	}

	def private static addEdges(LandscapeClientSide landscape) {
		for (communication : landscape.applicationCommunication) {
			communication.kielerEdgeReferences.clear()

			var appSource = communication.source
			var appTarget = communication.target

			if (appSource.parent.visible && appTarget.parent.visible) {
				if (appSource.parent == appTarget.parent) {
					communication.kielerEdgeReferences.add(
						createEdgeBetweenSourceTarget(appSource, appTarget, true, appTarget))
				} else {
					val nodeSource = appSource.parent
					val nodeGroupSource = appSource.parent.parent
					val nodeGroupTarget = appTarget.parent.parent
					val nodeTarget = appTarget.parent

					communication.kielerEdgeReferences.add(
						createEdgeBetweenSourceSource(appSource, nodeSource, true, appTarget))

					if (nodeGroupSource.nodes.size() > 1 && nodeGroupTarget.nodes.size() > 1) {
						communication.kielerEdgeReferences.add(
							createEdgeBetweenSourceSource(nodeSource, nodeGroupSource, false, appTarget))
						communication.kielerEdgeReferences.add(
							createEdgeBetweenSourceTarget(nodeGroupSource, nodeGroupTarget, false, appTarget))
						communication.kielerEdgeReferences.add(
							createEdgeBetweenTargetTarget(nodeGroupTarget, nodeTarget, appTarget))
						communication.kielerEdgeReferences.add(
							createEdgeBetweenTargetTarget(nodeTarget, appTarget, appTarget))
					} else if (nodeGroupSource.nodes.size() == 1 && nodeGroupTarget.nodes.size() > 1) {
						communication.kielerEdgeReferences.add(
							createEdgeBetweenSourceTarget(nodeSource, nodeGroupTarget, false, appTarget))
						communication.kielerEdgeReferences.add(
							createEdgeBetweenTargetTarget(nodeGroupTarget, nodeTarget, appTarget))
						communication.kielerEdgeReferences.add(
							createEdgeBetweenTargetTarget(nodeTarget, appTarget, appTarget))
					} else if (nodeGroupSource.nodes.size() > 1 && nodeGroupTarget.nodes.size() == 1) {
						communication.kielerEdgeReferences.add(
							createEdgeBetweenSourceSource(nodeSource, nodeGroupSource, false, appTarget))
						communication.kielerEdgeReferences.add(
							createEdgeBetweenSourceTarget(nodeGroupSource, nodeTarget, false, appTarget))
						communication.kielerEdgeReferences.add(
							createEdgeBetweenTargetTarget(nodeTarget, appTarget, appTarget))
					} else if (nodeGroupSource.nodes.size() == 1 && nodeGroupTarget.nodes.size() == 1) {
						communication.kielerEdgeReferences.add(
							createEdgeBetweenSourceTarget(nodeSource, nodeTarget, false, appTarget))
						communication.kielerEdgeReferences.add(
							createEdgeBetweenTargetTarget(nodeTarget, appTarget, appTarget))
					}
				}
			}

		}
	}

	def private static createEdgeBetweenSourceSource(DrawNodeEntity source, DrawNodeEntity target, boolean beginning,
		ApplicationClientSide appTarget) {
		var KPort port1 = null
		if (beginning) {
			port1 = createSourcePortIfNotExisting(source, appTarget)
		} else {
			port1 = source.sourcePorts.get(appTarget)
		}
		val port2 = createSourcePortIfNotExisting(target, appTarget)

		createEdgeHelper(source, port1, target, port2)
	}

	def private static createEdgeHelper(DrawNodeEntity source, KPort port1, DrawNodeEntity target, KPort port2) {
		val kielerEdge = KimlUtil::createInitializedEdge()
		kielerEdge.setSource(source.kielerNodeReference)
		kielerEdge.setSourcePort(port1)
		kielerEdge.setTarget(target.kielerNodeReference)
		kielerEdge.setTargetPort(port2)
		kielerEdge
	}

	def private static createEdgeBetweenSourceTarget(DrawNodeEntity source, DrawNodeEntity target, boolean beginning,
		ApplicationClientSide appTarget) {
		var KPort port1 = null
		if (beginning) {
			port1 = createSourcePortIfNotExisting(source, appTarget)
		} else {
			port1 = source.sourcePorts.get(appTarget)
		}
		val port2 = createTargetPortIfNotExisting(target, appTarget)

		createEdgeHelper(source, port1, target, port2)
	}

	def private static createEdgeBetweenTargetTarget(DrawNodeEntity source, DrawNodeEntity target,
		ApplicationClientSide appTarget) {
		val port1 = source.targetPorts.get(appTarget)
		val port2 = createTargetPortIfNotExisting(target, appTarget)

		createEdgeHelper(source, port1, target, port2)
	}

	def private static KPort createSourcePortIfNotExisting(DrawNodeEntity entity, ApplicationClientSide appTarget) {
		createPortHelper(entity, entity.sourcePorts, PortSide::EAST, appTarget)
	}
	
	def private static KPort createTargetPortIfNotExisting(DrawNodeEntity entity, ApplicationClientSide appTarget) {
		createPortHelper(entity, entity.targetPorts, PortSide::WEST, appTarget)
	}

	def private static createPortHelper(DrawNodeEntity entity, Map<ApplicationClientSide, KPort> ports, PortSide portSide, ApplicationClientSide appTarget) {
		if (ports.get(appTarget) == null) {
			val port = KimlUtil::createInitializedPort()
			port.setNode(entity.kielerNodeReference)
	
			val layout = port.getData(typeof(KShapeLayout))
			layout.setWidth(DEFAULT_PORT_WIDTH * CONVERT_TO_KIELER_FACTOR)
			layout.setHeight(DEFAULT_PORT_HEIGHT * CONVERT_TO_KIELER_FACTOR)
			layout.setProperty(LayoutOptions::PORT_SIDE, portSide)
		
			ports.put(appTarget, port)
		}
		
		ports.get(appTarget)
	}

	def private static void updateGraphWithResults(LandscapeClientSide landscape) {
		for (nodeGroup : landscape.nodeGroups) {
			if (nodeGroup.nodes.size() > 1)
				updateNodeValues(nodeGroup)
			for (node : nodeGroup.nodes) {
				if (node.visible) {
					updateNodeValues(node)
					if (nodeGroup.nodes.size() > 1)
						setAbsolutePositionForNode(node, nodeGroup)
					for (application : node.applications) {
						updateNodeValues(application)
						setAbsolutePositionForNode(application, node)
					}
				}
			}
		}

		addBendPointsInAbsoluteCoordinates(landscape)

		for (nodeGroup : landscape.nodeGroups) {
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

	def private static updateNodeValues(DrawNodeEntity node) {
		val layout = node.kielerNodeReference.getData(typeof(KShapeLayout))

		node.positionX = layout.getXpos()
		node.positionY = layout.getYpos() * -1 // KIELER has inverted Y coords

		node.width = layout.getWidth()
		node.height = layout.getHeight()
	}

	def private static setAbsolutePositionForNode(DrawNodeEntity node, DrawNodeEntity parent) {
		val insets = parent.kielerNodeReference.getData(typeof(KShapeLayout)).insets
		node.positionX = parent.positionX + node.positionX + insets.left
		node.positionY = parent.positionY + node.positionY - insets.top
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

				communication.kielerEdgeReferences.forEach [ edge, index |
					var parentNode = chooseRightParent(communication, index)
					var edgeLayout = edge.getData(typeof(KEdgeLayout))
					val points = new ArrayList<KPoint>
					points.add(edgeLayout.sourcePoint)
					points.addAll(edgeLayout.getBendPoints())
					points.add(edgeLayout.targetPoint)
					var pOffsetX = 0f
					var pOffsetY = 0f
					if (parentNode != null) {
						val insets = parentNode.kielerNodeReference.getData(typeof(KShapeLayout)).insets
						pOffsetX = parentNode.positionX + insets.left
						pOffsetY = parentNode.positionY - insets.top
					}
					for (point : points) {
						val resultPoint = new Point()
						resultPoint.x = (point.getX() + pOffsetX) / CONVERT_TO_KIELER_FACTOR
						resultPoint.y = (point.getY() * -1 + pOffsetY) / CONVERT_TO_KIELER_FACTOR // KIELER has inverted Y coords
						communication.points.add(resultPoint)
					}
				]
			}
		}
	}

	def private static chooseRightParent(CommunicationClientSide communication, int index) {
		var DrawNodeEntity parentNode = communication.source.parent

		val sourceNodeGroup = communication.source.parent.parent
		val targetNodeGroup = communication.target.parent.parent

		if (sourceNodeGroup.nodes.size() > 1 && targetNodeGroup.nodes.size() > 1) {
			if (index == 1) {
				parentNode = communication.source.parent.parent
			} else if (index == 2) {
				parentNode = null
			} else if (index == 3) {
				parentNode = communication.target.parent.parent
			} else if (index == 4) {
				parentNode = communication.target.parent
			}
		} else if (sourceNodeGroup.nodes.size() == 1 && targetNodeGroup.nodes.size() > 1) {
			if (index == 1) {
				parentNode = null
			} else if (index == 2) {
				parentNode = communication.target.parent.parent
			} else if (index == 3) {
				parentNode = communication.target.parent
			}
		} else if (sourceNodeGroup.nodes.size() > 1 && targetNodeGroup.nodes.size() == 1) {
			if (index == 1) {
				parentNode = communication.source.parent.parent
			} else if (index == 2) {
				parentNode = null
			} else if (index == 3) {
				parentNode = communication.target.parent
			}
		} else if (sourceNodeGroup.nodes.size() == 1 && targetNodeGroup.nodes.size() == 1) {
			if (index == 1) {
				parentNode = null
			} else if (index == 2) {
				parentNode = communication.target.parent
			}
		}
		parentNode
	}
}
