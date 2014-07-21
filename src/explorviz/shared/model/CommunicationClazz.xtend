package explorviz.shared.model

import java.util.HashMap
import java.util.Map
import com.google.gwt.user.client.rpc.IsSerializable

class CommunicationClazz implements IsSerializable {
	var requestsCacheCount = 0

	@Property String methodName
	@Property Map<Long, RuntimeInformation> traceIdToRuntimeMap = new HashMap<Long, RuntimeInformation>

	@Property Clazz source
	@Property Clazz target

	def void addRuntimeInformation(Long traceId, int calledTimes, int orderIndex, int requests, float averageResponseTime, float overallTraceDuration) {
		var runtime = traceIdToRuntimeMap.get(traceId)
		if (runtime == null) {
			runtime = new RuntimeInformation()
			runtime.calledTimes = calledTimes
			runtime.orderIndexes.add(orderIndex)
			runtime.requests = requests
			runtime.overallTraceDuration = overallTraceDuration
			runtime.averageResponseTime = averageResponseTime

			traceIdToRuntimeMap.put(traceId, runtime)
			requestsCacheCount += requests
			return
		}

		val beforeSum = runtime.requests * runtime.averageResponseTime
		val currentSum = requests * averageResponseTime;

		runtime.averageResponseTime = (beforeSum + currentSum) / (runtime.requests + requests)
		runtime.requests = runtime.requests + requests
		runtime.overallTraceDuration = (overallTraceDuration + runtime.overallTraceDuration) / 2f
		runtime.orderIndexes.add(orderIndex)
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
