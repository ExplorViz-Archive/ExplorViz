package explorviz.visualization.layout.datastructures.hypergraph

import explorviz.shared.model.Component
import explorviz.shared.model.helper.CommunicationAppAccumulator
import explorviz.shared.model.helper.Draw3DNodeEntity
import java.util.ArrayList
import java.util.LinkedHashSet

class Graphzahn {
	@Property val LinkedHashSet<CommunicationAppAccumulator> communicationOfComp = new LinkedHashSet<CommunicationAppAccumulator>()
	@Property val Graph<Draw3DNodeEntity> graph = new Graph<Draw3DNodeEntity>()
	
	
	/** 
     * Hauptprogramm.
     *
     * @param Component component
     * @param ArrayList<CommunicationAppAccumulator>
     */
	new(Component component, ArrayList<CommunicationAppAccumulator> communications) {
		component.children.forEach [
			graph.vertices.add(it)	
			communicationOfComp.addAll(findCommunicationsOfComponent(it, communications))
		]
		
		component.clazzes.forEach [
			graph.vertices.add(it)
			communicationOfComp.addAll(findCommunicationsOfComponent(it, communications))
		]
		
		communicationOfComp.forEach [
			graph.edges.add(new Edge(it.source, it.target))
		]
	}
	
	def insertEdges(Component component, ArrayList<CommunicationAppAccumulator> communications) {
		communications.forEach [
			
		]
	}
	
	/*
	 * Get all CommunicationAppAccumulator entries where Component is source or target
	 * @param Draw3DNodeEntity component
	 * @return ArrayList<CommunicationAppAcculumator>
	 */
	def ArrayList<CommunicationAppAccumulator> findCommunicationsOfComponent(Draw3DNodeEntity component, ArrayList<CommunicationAppAccumulator> communications) {
		var ArrayList<CommunicationAppAccumulator> commuList = new ArrayList<CommunicationAppAccumulator>()
		for(commu : communications) {
			if(commu.source == component || commu.target == component) {
				commuList.add(commu)
			}
		}
		return commuList
	}
	
	/*
	 * Remove all Communications where source or target are leaving the Component
	 */
	def void filterCommunication() {
		communicationOfComp.forEach [
			if(!graph.vertices.contains(it.source) || !graph.vertices.contains(it.target)) {
				communicationOfComp.remove(it)
			}
		]
	}
	
	/*
	 * TODO: Planarity
	 */
	 def boolean planarityTest() {
	 	return false
	 }
}