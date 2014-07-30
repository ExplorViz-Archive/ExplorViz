package explorviz.visualization.layout.datastructures.hypergraph

import explorviz.shared.model.Component
import explorviz.shared.model.helper.CommunicationAppAccumulator
import explorviz.shared.model.helper.Draw3DNodeEntity
import java.util.ArrayList
import java.util.LinkedHashSet

class Graphzahn {
//	@Property val Graph<Draw3DNodeEntity, CommunicationAppAccumulator> graph = new SparseGraph<Draw3DNodeEntity, CommunicationAppAccumulator>()
	@Property val LinkedHashSet<CommunicationAppAccumulator> communicationOfComp = new LinkedHashSet<CommunicationAppAccumulator>()
	@Property val Graph<Draw3DNodeEntity> graph = new Graph<Draw3DNodeEntity>()
	
	new(Component component, ArrayList<CommunicationAppAccumulator> communications) {
		component.children.forEach [
			graph.addVertex(it)	
			communicationOfComp.addAll(findCommunicationsOfComponent(it, communications))
		]
		
		component.clazzes.forEach [
			graph.addVertex(it)
			communicationOfComp.addAll(findCommunicationsOfComponent(it, communications))
		]
		
		filterCommunication()
		
		communicationOfComp.forEach [
			graph.addEdge(new Edge(it.source, it.target))
		]
	}
	
	def insertEdges(Component component, ArrayList<CommunicationAppAccumulator> communications) {
		communications.forEach [
			
		]
	}
	
	def ArrayList<CommunicationAppAccumulator> findCommunicationsOfComponent(Draw3DNodeEntity component, ArrayList<CommunicationAppAccumulator> communications) {
		var ArrayList<CommunicationAppAccumulator> commuList = new ArrayList<CommunicationAppAccumulator>()
		for(commu : communications) {
			if(commu.source == component || commu.target == component) {
				commuList.add(commu)
			}
		}
		return commuList
	}
	
	def void filterCommunication() {
		communicationOfComp.forEach [
			if(!graph.containsVertex(it.source) || !graph.containsVertex(it.target)) {
				communicationOfComp.remove(it)
			}
		]
	}
}