package explorviz.shared.model

import java.util.ArrayList
import java.util.List
import com.google.gwt.user.client.rpc.IsSerializable
import org.eclipse.xtend.lib.annotations.Accessors

class Landscape implements IsSerializable {
	@Accessors long timestamp
	@Accessors long activities
	
	@Accessors List<System> systems = new ArrayList<System>
	@Accessors List<Communication> applicationCommunication = new ArrayList<Communication>
	
	def void destroy() {
		systems.forEach [it.destroy()]
		applicationCommunication.forEach [it.destroy()]
	}
}