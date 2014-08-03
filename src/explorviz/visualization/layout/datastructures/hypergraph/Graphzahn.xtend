package explorviz.visualization.layout.datastructures.hypergraph

import explorviz.shared.model.Application
import explorviz.shared.model.Clazz
import explorviz.shared.model.Component
import explorviz.shared.model.helper.Draw3DNodeEntity

class Graphzahn {
	val Graph<Draw3DNodeEntity> graph = new Graph<Draw3DNodeEntity>()
	
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
	
	/*
	 * TODO: Planarity
	 */
	 def boolean planarityTest() {
	 	return false
	 }
}