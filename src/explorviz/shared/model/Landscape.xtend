package explorviz.shared.model

import java.util.ArrayList
import java.util.List
import com.google.gwt.user.client.rpc.IsSerializable
import org.eclipse.xtend.lib.annotations.Accessors
import explorviz.shared.model.helper.CommunicationAccumulator

class Landscape implements IsSerializable {
	@Accessors long hash
	@Accessors long activities
	
	@Accessors List<System> systems = new ArrayList<System>
	@Accessors List<Communication> applicationCommunication = new ArrayList<Communication>
	
	@Accessors val transient List<CommunicationAccumulator> communicationsAccumulated = new ArrayList<CommunicationAccumulator>(4)
	
	def void destroy() {
		systems.forEach [it.destroy()]
		applicationCommunication.forEach [it.destroy()]
	}
}