package explorviz.shared.model

import java.util.HashSet
import java.util.Set
import explorviz.shared.model.helper.Draw3DNodeEntity
import explorviz.visualization.renderer.ColorDefinitions
import org.eclipse.xtend.lib.annotations.Accessors
import explorviz.plugin_server.rootcausedetection.RanCorrConfiguration
import java.util.List
import explorviz.plugin_server.rootcausedetection.model.AnomalyScoreRecord
import explorviz.plugin_server.rootcausedetection.model.RanCorrLandscape
import java.util.ArrayList
import java.util.Map.Entry

class Clazz extends Draw3DNodeEntity {
	@Accessors var int instanceCount = 0
	@Accessors val transient Set<Integer> objectIds = new HashSet<Integer>()

	@Accessors Component parent
	@Accessors var boolean visible = false

	@Accessors var double rootCauseRating;
	@Accessors var double temporaryRating = -1;

	override void destroy() {
		super.destroy()
	}

	def void clearAllPrimitiveObjects() {
		this.primitiveObjects.clear()
	}

	override void highlight() {
		this.primitiveObjects.forEach [
			it.highlight(ColorDefinitions::highlightColor)
		]
		highlighted = true
	}

	override unhighlight() {
		if (highlighted) {
			this.primitiveObjects.forEach [
				it.unhighlight()
			]
			highlighted = false
		}
	}

	/**
	 * Sets the root cause rating of this element to a failure state.
	 */
	def void setRootCauseRatingToFailure() {
		rootCauseRating = RanCorrConfiguration.RootCauseRatingFailureState;
	}

	/**
	 * Returns a list of all available timestamp-anomalyScore pairs for all
	 * operations in this class. All anomaly scores are in [0, 1].
	 *
	 * @param lscp
	 *            landscape we want to look for operations in
	 * @return list of timestamp-anomalyScore pairs
	 */
	def List<AnomalyScoreRecord> getAnomalyScores(RanCorrLandscape lscp) {
		val List<AnomalyScoreRecord> outputScores = new ArrayList<AnomalyScoreRecord>();

		// add all anomaly scores from operations that are placed inside this
		// class
		for (operation : lscp.getOperations()) {
			if (operation.getTarget() == this) {
				outputScores.addAll(operation.getAnomalyScores());
			}
		}

		return outputScores;
	}

	/**
	 * This method returns if the Root Cause Rating of this class is positive.
	 * This information is directly derived from Anomaly Scores. This is done as
	 * follows: We get from every method in this class the latest anomaly score.
	 * From these we choose the AS which has the most recent timestamp and of
	 * these the highest absolute value. Then we check if this AS is >= 0.
	 *
	 * @param lscp
	 *            Landscape we want to look for operations in
	 * @return Is the Root Cause Ranking of this class positive?
	 */
	def boolean isRankingPositive(RanCorrLandscape lscp) {
		var long latest = 0;
		var double valueOfLatest = 0;

		for (operation : lscp.getOperations()) {
			if (operation.getTarget() == this) {
				val Entry<Long, Double> entry = operation.getLatestAnomalyScorePair();
				if (entry != null) {
					if (entry.getKey() > latest) {

						// more recent value has been found
						latest = entry.getKey();
						valueOfLatest = entry.getValue();
					} else if ((entry.getKey() == latest) && (Math.abs(entry.getValue()) > Math.abs(valueOfLatest))) {

						// higher absolute value has been found
						// new value has the same timestamp as the one from before
						valueOfLatest = entry.getValue();
					}
				}
			}
		}

		return valueOfLatest >= 0;
	}

}
