package explorviz.shared.model

import java.util.ArrayList
import java.util.List
import com.google.gwt.user.client.rpc.IsSerializable

class Node implements IsSerializable {
	@Property String name
	@Property String ipAddress
	
	@Property float cpuUtilization
	@Property float freeRAM
	@Property float usedRAM
	
	@Property List<Application> applications = new ArrayList<Application>
}