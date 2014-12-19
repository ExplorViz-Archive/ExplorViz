package explorviz.shared.model.helper

import com.google.gwt.user.client.rpc.IsSerializable
import org.eclipse.xtend.lib.annotations.Accessors

class Point implements IsSerializable {
	@Accessors float x
	@Accessors float y
}