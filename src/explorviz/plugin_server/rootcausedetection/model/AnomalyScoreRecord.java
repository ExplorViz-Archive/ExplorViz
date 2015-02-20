package explorviz.plugin_server.rootcausedetection.model;

/**
 * This class represents an anomaly score with its corresponding timestamp.
 *
 * @author Christian Claus Wiechmann
 *
 */
public class AnomalyScoreRecord {

	private final Long timestamp;
	private final Double anomaly_score;

	/**
	 * Create new AnomalyScoreRecord.
	 *
	 * @param timestamp
	 *            Timestamp of anomaly score
	 * @param anomaly_score
	 *            Anomaly score in [-1, 1]
	 */
	public AnomalyScoreRecord(final Long timestamp, final Double anomaly_score) {
		this.timestamp = timestamp;
		this.anomaly_score = anomaly_score;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public Double getAnomaly_score() {
		return anomaly_score;
	}
}
