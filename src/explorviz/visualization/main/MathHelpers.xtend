package explorviz.visualization.main

import java.util.Map
import java.util.HashMap
import java.util.List
import java.util.ArrayList

class MathHelpers {
	def static Map<Integer, Integer> getCategoriesForMapping(List<Integer> list) {
		val result = new HashMap<Integer, Integer>()
		
		if (list.empty) {
			return result
		}

		list.sortInplace
		val listWithout0 = new ArrayList<Integer>()
		list.forEach [
			if (it != 0) {
				listWithout0.add(it)
			}
		]
		
		if (listWithout0.empty) {
			result.put(0, list.get(0))
			return result
		}
		
		useThreshholds(listWithout0, list, result)
//		useQuartiles(listWithout0, list, result)
		
		result
	}
	
	def private static void useThreshholds(List<Integer> listWithout0, List<Integer> list, Map<Integer, Integer> result) {
		var max = 1
		for (value : listWithout0) {
			if (value > max) {
				max = value
			}
		}
		
		val oneStep = max / 5f
		
		val t1 = oneStep
		val t2 = oneStep * 2
		val t3 = oneStep * 3
		val t4 = oneStep * 4
		
		list.forEach [
			result.put(it, getCategoryFromValues(it, t1, t2, t3, t4))
		]
	}
	
//	def private static void useQuartiles(List<Integer> listWithout0, List<Integer> list, Map<Integer, Integer> result) {
//			val int quart = listWithout0.size / 4
//
//		val q0 = listWithout0.get(0)
//		val q25 = if (listWithout0.size > 1) listWithout0.get(quart) else q0
//		val q50 = if (listWithout0.size > 2) listWithout0.get(quart * 2) else q25
//		val q75 = if (listWithout0.size > 3) listWithout0.get(quart * 3) else q50
//		
//		list.forEach [
//			result.put(it, getCategoryFromValues(it, q0, q25, q50, q75))
//		]
//	}

	def private static int getCategoryFromValues(int requestsPerSecond, float t1, float t2, float t3, float t4) {
		if (requestsPerSecond == 0) {
			return 0
		} else if (requestsPerSecond <= t1) {
			return 1
		} else if (requestsPerSecond <= t2) {
			return 2
		} else if (requestsPerSecond <= t3) {
			return 3
		} else if (requestsPerSecond <= t4) {
			return 4
		} else {
			return 5
		}
	}
}