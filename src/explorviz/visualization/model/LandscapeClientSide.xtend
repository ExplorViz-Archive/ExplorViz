package explorviz.visualization.model

import explorviz.visualization.model.helper.IViewable

import java.util.ArrayList
import java.util.List

class LandscapeClientSide implements IViewable {
	@Property long hash
	
	@Property val List<NodeGroupClientSide> nodeGroups = new ArrayList<NodeGroupClientSide>
	@Property val List<CommunicationClientSide> applicationCommunication = new ArrayList<CommunicationClientSide>
	
	def void destroy() {
		nodeGroups.forEach [it.destroy()]
		applicationCommunication.forEach [it.destroy()]
	}
	
}