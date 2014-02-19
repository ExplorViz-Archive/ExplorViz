package explorviz.shared.model

import java.util.List
import com.google.gwt.user.client.rpc.IsSerializable
import java.util.ArrayList

class System implements IsSerializable {
	@Property List<NodeGroup> nodeGroups = new ArrayList<NodeGroup>
	@Property String name
}