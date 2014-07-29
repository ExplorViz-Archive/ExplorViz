package explorviz.visualization.layout.datastructures.hypergraph

import explorviz.shared.model.Component
import explorviz.shared.model.helper.CommunicationAppAccumulator
import explorviz.shared.model.helper.Draw3DNodeEntity
import java.util.ArrayList
import java.util.LinkedHashSet
import edu.uci.ics.jung.graph.SetHypergraph
import edu.uci.ics.jung.graph.Hypergraph
import java.util.Collection

class Graphzahn {
	@Property val Hypergraph<Draw3DNodeEntity, CommunicationAppAccumulator> graph = new SetHypergraph<Draw3DNodeEntity, CommunicationAppAccumulator>()
	@Property val LinkedHashSet<CommunicationAppAccumulator> communicationOfComp = new LinkedHashSet<CommunicationAppAccumulator>()
	
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
			var ArrayList<Draw3DNodeEntity> collection = new ArrayList<Draw3DNodeEntity>()
			collection.add(it.source)
			collection.add(it.target)
			graph.addEdge(it, collection)
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
	
	def void adjacencyMatrix() {
		
	}
}