package explorviz.shared.model

import java.util.ArrayList
import java.util.List
import com.google.gwt.user.client.rpc.IsSerializable

class Landscape implements IsSerializable {
	@Property long hash
	@Property long activities
	
	@Property List<NodeGroup> nodeGroups = new ArrayList<NodeGroup>
	
	@Property List<Communication> applicationCommunication = new ArrayList<Communication>
}