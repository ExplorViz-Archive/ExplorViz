package explorviz.shared.model

import explorviz.shared.model.helper.GenericModelElement
import java.util.ArrayList
import java.util.List
import org.eclipse.xtend.lib.annotations.Accessors
import explorviz.shared.model.helper.CommunicationAccumulator

class Landscape extends GenericModelElement {
	@Accessors long timestamp
	@Accessors long activities

	@Accessors List<System> systems = new ArrayList<System>
	@Accessors List<Communication> applicationCommunication = new ArrayList<Communication>
	
	@Accessors val transient List<CommunicationAccumulator> communicationsAccumulated = new ArrayList<CommunicationAccumulator>(4)
	
	def void destroy() {
		systems.forEach[it.destroy()]
		applicationCommunication.forEach[it.destroy()]
	}
}
