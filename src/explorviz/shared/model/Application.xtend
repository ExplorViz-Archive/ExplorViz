package explorviz.shared.model

import com.google.gwt.user.client.rpc.IsSerializable
import java.util.ArrayList

class Application implements IsSerializable {
    @Property int id
    
	@Property boolean database
	
	@Property String name
	@Property String image
	
	@Property long lastUsage
	
	@Property var components = new ArrayList<Component>
	
	@Property var communcations = new ArrayList<CommunicationClazz>
}