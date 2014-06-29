package explorviz.shared.model.helper

import com.google.gwt.user.client.rpc.IsSerializable

class Point implements IsSerializable {
	@Property float x
	@Property float y
}