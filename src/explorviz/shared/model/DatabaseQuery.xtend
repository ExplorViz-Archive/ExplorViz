package explorviz.shared.model

import com.google.gwt.user.client.rpc.IsSerializable
import org.eclipse.xtend.lib.annotations.Accessors

class DatabaseQuery implements IsSerializable {
	@Accessors String SQLStatement
	@Accessors String returnValue
	
	@Accessors long timeInNanos
}
