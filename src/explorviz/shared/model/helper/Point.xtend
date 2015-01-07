package explorviz.shared.model.helper

import com.google.gwt.user.client.rpc.IsSerializable
import org.eclipse.xtend.lib.annotations.Accessors

class Point implements IsSerializable {
	@Accessors float x
	@Accessors float y
	
	private val DELTA = 0.01f
	
	def boolean equals(Point other) {
		Math.abs(other.x - x) <= DELTA && Math.abs(other.y - y) <= DELTA 
	}
}