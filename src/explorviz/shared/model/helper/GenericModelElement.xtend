package explorviz.shared.model.helper

import java.util.Map
import java.util.HashMap
import com.google.gwt.user.client.rpc.IsSerializable

abstract class GenericModelElement implements IsSerializable {
	Map<IKey, IValue> genericData = new HashMap<IKey, IValue>()
	
	def Object getGenericData(IKey key) {
		genericData.get(key)
	}
	
	def void putGenericData(IKey key, IValue value) {
		genericData.put(key, value)
	}
	
	def void isGenericDataPresent(IKey key) {
		genericData.get(key) != null
	}
}
