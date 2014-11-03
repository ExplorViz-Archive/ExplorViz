package explorviz.shared.model.helper

import java.util.Map
import java.util.HashMap
import com.google.gwt.user.client.rpc.IsSerializable

abstract class GenericModelElement implements IsSerializable {
	private var Map<String, IsSerializable> genericData = new HashMap<String, IsSerializable>()

	def boolean isGenericDataPresent(String key) {
		genericData.get(key) != null
	}

	def IsSerializable getGenericData(String key) {
		genericData.get(key)
	}

	def void putGenericData(String key, IValue value) {
		genericData.put(key, value)
	}
	
	def Boolean getGenericBooleanData(String key) {
		val value = genericData.get(key)
		if (value != null && value instanceof BooleanValue) {
			(value as BooleanValue).value
		} else {
			null
		}
	}

	def void putGenericBooleanData(String key, boolean value) {
		genericData.put(key, new BooleanValue(value))
	}
	
	def Double getGenericDoubleData(String key) {
		val value = genericData.get(key)
		if (value != null && value instanceof DoubleValue) {
			(value as DoubleValue).value
		} else {
			null
		}
	}

	def void putGenericDoubleData(String key, Double value) {
		genericData.put(key, new DoubleValue(value))
	}
}

class BooleanValue implements IValue {
	public Boolean value
	
	new() {
	}
	
	new(boolean b) {
		value = b
	}
}

class DoubleValue implements IValue {
	public Double value
	
	new() {
	}
	
	new(double d) {
		value = d
	}
}