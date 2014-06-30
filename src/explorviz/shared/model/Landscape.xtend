package explorviz.shared.model

import java.util.ArrayList
import java.util.List
import com.google.gwt.user.client.rpc.IsSerializable

class Landscape implements IsSerializable {
	@Property long hash
	@Property long activities
	
	@Property List<System> systems = new ArrayList<System>
	@Property List<Communication> applicationCommunication = new ArrayList<Communication>
	
	def void destroy() {
		systems.forEach [it.destroy()]
		applicationCommunication.forEach [it.destroy()]
	}
}