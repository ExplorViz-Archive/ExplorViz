package explorviz.visualization.main

import java.util.Map
import java.util.HashMap
import java.util.List
import java.util.ArrayList

class MathHelpers {
	def static Map<Integer, Integer> getCategoriesByQuantiles(List<Integer> list) {
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
		
		val int quart = listWithout0.size / 4

		val q0 = listWithout0.get(0)
		val q25 = if (listWithout0.size > 1) listWithout0.get(quart) else q0
		val q50 = if (listWithout0.size > 2) listWithout0.get(quart * 2) else q25
		val q75 = if (listWithout0.size > 3) listWithout0.get(quart * 3) else q50
		
		list.forEach [
			result.put(it, getCategoryFromQuantiles(it, q0, q25, q50, q75))
		]
		
		result
	}

	def private static int getCategoryFromQuantiles(int requestsPerSecond, int q0, int q25, int q50, int q75) {
		if (requestsPerSecond == 0) {
			return 0
		} else if (requestsPerSecond <= q0) {
			return 1
		} else if (requestsPerSecond <= q25) {
			return 2
		} else if (requestsPerSecond <= q50) {
			return 3
		} else if (requestsPerSecond <= q75) {
			return 4
		} else {
			return 5
		}
	}
}