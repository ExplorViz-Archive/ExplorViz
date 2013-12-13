package explorviz.shared.model

import java.util.ArrayList
import java.util.List
import com.google.gwt.user.client.rpc.IsSerializable

class NodeGroup implements IsSerializable {
	@Property List<Node> nodes = new ArrayList<Node>
}