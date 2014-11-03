package explorviz.plugin.attributes;

import java.util.*;

import explorviz.shared.model.helper.IValue;

public class TreeMapLongDoubleIValue implements IValue, Map<Long, Double> {
	protected TreeMap<Long, Double> map = new TreeMap<Long, Double>();

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public boolean containsKey(final Object key) {
		return map.containsKey(key);
	}

	@Override
	public boolean containsValue(final Object value) {
		return map.containsValue(value);
	}

	@Override
	public Double get(final Object key) {
		return map.get(key);
	}

	@Override
	public Double put(final Long key, final Double value) {
		return map.put(key, value);
	}

	@Override
	public Double remove(final Object key) {
		return map.remove(key);
	}

	@Override
	public void putAll(final Map<? extends Long, ? extends Double> m) {
		map.putAll(m);
	}

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public Set<Long> keySet() {
		return map.keySet();
	}

	@Override
	public Collection<Double> values() {
		return map.values();
	}

	@Override
	public Set<java.util.Map.Entry<Long, Double>> entrySet() {
		return map.entrySet();
	}

}
