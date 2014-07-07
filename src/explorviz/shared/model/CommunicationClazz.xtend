package explorviz.shared.model

import explorviz.shared.model.helper.Draw3DEdgeEntity
import java.util.HashMap
import java.util.Map

class CommunicationClazz extends Draw3DEdgeEntity {
	var requestsCacheCount = 0

	@Property String methodName
	@Property Map<Long, RuntimeInformation> traceIdToRuntimeMap = new HashMap<Long, RuntimeInformation>

	@Property Clazz source
	@Property Clazz target

	def void addRuntimeInformation(long traceId, int requests, float averageResponseTime) {
		var runtime = traceIdToRuntimeMap.get(traceId)
		if (runtime == null) {
			runtime = new RuntimeInformation()
			runtime.requests = requests
			runtime.averageResponseTime = averageResponseTime

			traceIdToRuntimeMap.put(traceId, runtime)
			requestsCacheCount += requests
			return
		}

		val beforeSum = runtime.requests * runtime.averageResponseTime
		val currentSum = requests * averageResponseTime;

		runtime.averageResponseTime = (beforeSum + currentSum) / (runtime.requests + requests)
		runtime.requests = runtime.requests + requests
		requestsCacheCount += requests
	}

	def void reset() {
		requestsCacheCount = 0
		traceIdToRuntimeMap.clear()
	}

	def int getRequests() {
		requestsCacheCount
	}
}
