package explorviz.visualization.model

import explorviz.visualization.model.helper.IViewable

import java.util.ArrayList
import java.util.List

class LandscapeClientSide implements IViewable {
	@Property long hash
	
	@Property val List<SystemClientSide> systems = new ArrayList<SystemClientSide>
	@Property val List<CommunicationClientSide> applicationCommunication = new ArrayList<CommunicationClientSide>
	
	def void destroy() {
		systems.forEach [it.destroy()]
		applicationCommunication.forEach [it.destroy()]
	}
	
}