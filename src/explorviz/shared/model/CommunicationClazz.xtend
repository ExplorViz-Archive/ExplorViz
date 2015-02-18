package explorviz.shared.model

import java.util.HashMap
import java.util.Map
import com.google.gwt.user.client.rpc.IsSerializable
import org.eclipse.xtend.lib.annotations.Accessors
import explorviz.shared.model.helper.GenericModelElement
import explorviz.plugin_server.rootcausedetection.RanCorrConfiguration
import explorviz.plugin_server.rootcausedetection.model.AnomalyScoreRecord
import java.util.List
import java.util.ArrayList
import explorviz.plugin_client.attributes.TreeMapLongDoubleIValue
import java.util.Map.Entry
import explorviz.plugin_client.attributes.IPluginKeys

class CommunicationClazz extends GenericModelElement implements IsSerializable {
	var requestsCacheCount = 0

	@Accessors String methodName
	@Accessors Map<Long, RuntimeInformation> traceIdToRuntimeMap = new HashMap<Long, RuntimeInformation>

	@Accessors Clazz source
	@Accessors Clazz target
	
	@Accessors boolean hidden = false
	
	@Accessors var double rootCauseRating;

	def void addRuntimeInformation(Long traceId, int calledTimes, int orderIndex, int requests, float averageResponseTime, float overallTraceDuration) {
		var runtime = traceIdToRuntimeMap.get(traceId)
		if (runtime == null) {
			runtime = new RuntimeInformation()
			runtime.calledTimes = calledTimes
			runtime.orderIndexes.add(orderIndex)
			runtime.requests = requests
			runtime.overallTraceDurationInNanoSec = overallTraceDuration
			runtime.averageResponseTimeInNanoSec = averageResponseTime

			traceIdToRuntimeMap.put(traceId, runtime)
			requestsCacheCount += requests
			return
		}

		val beforeSum = runtime.requests * runtime.getAverageResponseTimeInNanoSec
		val currentSum = requests * averageResponseTime;

		runtime.averageResponseTimeInNanoSec = (beforeSum + currentSum) / (runtime.requests + requests)
		runtime.requests = runtime.requests + requests
		runtime.overallTraceDurationInNanoSec = (overallTraceDuration + runtime.getOverallTraceDurationInNanoSec) / 2f
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
	
	/**
	 * Sets the root cause rating of this element to a failure state.
	 */
	def setRootCauseRatingToFailure() {
		rootCauseRating = RanCorrConfiguration.RootCauseRatingFailureState
	}
	
	/**
	 * Returns a list of all available timestamp-anomalyScore pairs for this
	 * operation. All anomaly scores are in [0, 1].
	 *
	 * @return List of {@link AnomalyScoreRecord}s. If there are no anomaly
	 *         scores available, the method will return null.
	 */
	def List<AnomalyScoreRecord> getAnomalyScores() {
		// return null if there are no anomaly scores
		if (!isGenericDataPresent(IPluginKeys.TIMESTAMP_TO_ANOMALY_SCORE)) {
			return null
		}

		// otherwise create list of timestamp-anomalyscore pairs
		// (AnomalyScoreRecord)
		val anomalyScores = getGenericData(IPluginKeys.TIMESTAMP_TO_ANOMALY_SCORE) as TreeMapLongDoubleIValue
		val List<Entry<Long, Double>> mapEntries = new ArrayList<Entry<Long, Double>>(anomalyScores.entrySet())
		val List<AnomalyScoreRecord> outputScores = new ArrayList<AnomalyScoreRecord>()

		for (entry : mapEntries) {
			// note that we use absolute values here
			outputScores.add(new AnomalyScoreRecord(entry.getKey(), Math.abs(entry.getValue())))
		}

		return outputScores
	}
	
	/**
	 * This method returns the latest timestamp-anomalyscore-pair for this
	 * method.
	 *
	 * @return Pair of (Timestamp, Anomaly Score), null if no scores are present
	 */
	def Entry<Long, Double> getLatestAnomalyScorePair() {
		val anomalyScores = getGenericData(IPluginKeys.TIMESTAMP_TO_ANOMALY_SCORE) as TreeMapLongDoubleIValue
		if (anomalyScores == null) {
			return null
		}
		val List<Entry<Long, Double>> mapEntries = new ArrayList<Entry<Long, Double>>(anomalyScores.entrySet())

		var Entry<Long, Double> current = null
		for (entry : mapEntries) {
			if ((current == null) || (entry.getKey() > current.getKey())) {
				current = entry
			}
		}

		return current;
	}
}
