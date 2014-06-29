package explorviz.shared.model

import com.google.gwt.user.client.rpc.IsSerializable

class Communication implements IsSerializable {
	@Property int requests
	
	@Property Application source
	@Property Application target
	
	@Property Clazz sourceClazz
	@Property Clazz targetClazz
}