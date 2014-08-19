package explorviz.visualization.layout.datastructures.hypergraph

import explorviz.shared.model.Application
import explorviz.shared.model.Clazz
import explorviz.shared.model.Component
import explorviz.shared.model.helper.Draw3DNodeEntity
import java.util.ArrayList
import java.util.HashSet
import java.util.List
import java.util.Set

class Graphzahn {
	@Property val Graph<Draw3DNodeEntity> graph = new Graph<Draw3DNodeEntity>()
	
	def public void clear() {
		graph.clear
	}
	def public void fillGraph(Component component, Application app) {
		component.children.forEach [
			fillGraph(it, app)
			graph.addVertex(it)
		]
		
		component.clazzes.forEach [
			graph.addVertex(it)
		]
		
		app.communications.forEach [
			graph.addEdge(new Edge<Draw3DNodeEntity>(it.source, it.target))
			if(it.source.parent != null && it.target.parent != null) {
				if(it.source.parent != it.target.parent && (!isGreatParent(it.source.parent, it.target.parent) && !isGreatParent(it.target.parent, it.source.parent))) {
					graph.addEdge(new Edge<Draw3DNodeEntity>(it.source.parent , it.target.parent))
				}
			} else if(it.source.parent == null && it.target.parent != null) {
				graph.addEdge(new Edge<Draw3DNodeEntity>(it.source, it.target.parent))
			} else if(it.source.parent != null && it.target.parent == null) {
				graph.addEdge(new Edge<Draw3DNodeEntity>(it.source.parent, it.target))
			}
		]
	}
	
	def public boolean isGreatParent(Component parent, Component component) {
		var boolean isGP = false
		
		if(component.parentComponent != null) {
			if(parent.equals(component.parentComponent)) {
				isGP = true
			} else {
				isGP = isGreatParent(parent, component.parentComponent)
			}
		}
		
		return isGP
	}
	
	def public int getWeights(Draw3DNodeEntity component) {
		var int weights = 0
		if(component instanceof Component) {

			for(Component comp : component.children) {
				weights = weights + getWeights(comp)
			}
			
			for(Clazz clazz : component.clazzes) {
				weights = weights + graph.getWeight(clazz as Draw3DNodeEntity)
			}
			
		} else {
			weights = graph.getWeight(component)
		}
			
		return weights
	}
	
	def int getRank(Draw3DNodeEntity vertex) {
		var int fullRank = 0
		
		if(vertex instanceof Component) {
			for(Component comp : vertex.children) {
				fullRank = fullRank + getRank(comp)
			}
			
			for(Clazz clazz : vertex.clazzes) {
				for(Draw3DNodeEntity vert : graph.adjMatrix.get(clazz)) {
					if(graph.adjMatrix.get(vert) != null) {
						fullRank = fullRank + getWeights(vert)
					}
				}
			}
		} else {
			for(Draw3DNodeEntity vert : graph.adjMatrix.get(vertex)) {
				if(graph.adjMatrix.get(vert) != null) {
					fullRank = fullRank + getWeights(vert)
				}
			}
		}
		return fullRank
	}
	
	def void createAdjacencyMatrix() {
		graph.createAdjacencyMatrix
	}
	
	def List<Edge<Component>> edgesOfComponent(Component component) {
		val List<Edge<Component>> componentEdges = new ArrayList<Edge<Component>>()
		
		graph.edges.forEach [
			if(it.source.equals(component)) {
				if(it.target instanceof Component) {
					componentEdges.add(new Edge<Component>(it.source as Component, it.target as Component))
				} 
			} else if(it.target.equals(component)) {
				if(it.source instanceof Component) {
					componentEdges.add(new Edge<Component>(it.source as Component, it.target as Component))
				}
			}
		]
		
		return componentEdges
	}
	
	def List<Component> orderComponents(Component component) {
		val Graph<Component> componentGraph = new Graph<Component>(component.children)
		val Set<Component> orderList = new HashSet<Component>()
		
		component.children.forEach [
			var List<Edge<Component>> componentEdges = edgesOfComponent(it)
			componentEdges.forEach [
				if(componentGraph.vertices.contains(it.source) && componentGraph.vertices.contains(it.target)) {
					componentGraph.addEdge(it)
				}
			]
		]
		
		if(component.children.size > 4) {
			componentGraph.createAdjacencyMatrix
			
			component.children.forEach [
				orderList.addAll(componentGraph.getMNeighborsByWeights(it, 4))	
			]
		}
		
		return orderList as List<Component>
	}
}