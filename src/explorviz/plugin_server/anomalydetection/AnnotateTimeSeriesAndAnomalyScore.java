package explorviz.plugin_server.anomalydetection;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import explorviz.plugin_client.attributes.IPluginKeys;
import explorviz.plugin_client.attributes.TreeMapLongDoubleIValue;
import explorviz.plugin_server.anomalydetection.aggregation.TraceAggregator;
import explorviz.plugin_server.anomalydetection.anomalyscore.CalculateAnomalyScore;
import explorviz.plugin_server.anomalydetection.anomalyscore.InterpreteAnomalyScore;
import explorviz.plugin_server.anomalydetection.forecast.AbstractForecaster;
import explorviz.plugin_server.anomalydetection.util.ADThreadPool;
import explorviz.plugin_server.anomalydetection.util.IThreadable;
import explorviz.plugin_server.rootcausedetection.exception.RootCauseThreadingException;
import explorviz.shared.model.*;
import explorviz.shared.model.System;

/**
 * Threadable class that does the anomaly detection. The tasks are splitted on
 * CommunicationClazz-level.
 *
 * @author Enno Schwanke
 *
 */
public class AnnotateTimeSeriesAndAnomalyScore implements IThreadable<CommunicationClazz, Long> {

	static final Logger LOGGER = LoggerFactory.getLogger(AnnotateTimeSeriesAndAnomalyScore.class);

	/**
	 * For each CommunicationClazz (Method) that is called an item is added to a
	 * threadpool. Afterwards the threadpool is started and the available
	 * threads take the items in the pool and annotate them.
	 *
	 * @param landscape
	 *            The anomaly detection is done based on a given landscape
	 */
	public void doAnomalyDetection(Landscape landscape) {
		final ADThreadPool<CommunicationClazz, Long> pool = new ADThreadPool<>(this, Runtime
				.getRuntime().availableProcessors(), landscape.getHash());
		LOGGER.info("\ndoAnomalyScore for Timestamp: " + landscape.getHash());
		for (System system : landscape.getSystems()) {
			for (NodeGroup nodeGroup : system.getNodeGroups()) {
				for (Node node : nodeGroup.getNodes()) {
					for (Application application : node.getApplications()) {
						application.putGenericBooleanData(IPluginKeys.WARNING_ANOMALY, false);
						application.putGenericBooleanData(IPluginKeys.ERROR_ANOMALY, false);
						for (Component component : application.getComponents()) {
							component.putGenericBooleanData(IPluginKeys.WARNING_ANOMALY, false);
							component.putGenericBooleanData(IPluginKeys.ERROR_ANOMALY, false);
							recursiveComponentSplitting(component);
						}
						for (CommunicationClazz communication : application.getCommunications()) {
							communication.putGenericBooleanData(IPluginKeys.WARNING_ANOMALY, false);
							communication.putGenericBooleanData(IPluginKeys.ERROR_ANOMALY, false);
							pool.addData(communication);
							// LOGGER.info("\nCommClazz traceidSize: "
							// + communication.getTraceIdToRuntimeMap().size());
							// calculate(communication, landscape.getHash());
						}
					}
				}
			}
		}
		try {
			pool.startThreads();
		} catch (final InterruptedException e) {
			throw new RootCauseThreadingException(
					"AnnotateTimeSeriesAndAnomalyScoreThreaded#calculate(...): Threading interrupted, broken output.");
		}
	}

	private static void recursiveComponentSplitting(Component component) {
		for (Clazz clazz : component.getClazzes()) {
			clazz.putGenericBooleanData(IPluginKeys.WARNING_ANOMALY, false);
			clazz.putGenericBooleanData(IPluginKeys.ERROR_ANOMALY, false);
		}
		for (Component childComponent : component.getChildren()) {
			childComponent.putGenericBooleanData(IPluginKeys.WARNING_ANOMALY, false);
			childComponent.putGenericBooleanData(IPluginKeys.ERROR_ANOMALY, false);
			recursiveComponentSplitting(childComponent);
		}
	}

	@Override
	public void calculate(CommunicationClazz input, Long attr) {
		annotateTimeSeriesAndAnomalyScore(input, attr);
	}

	private static void annotateTimeSeriesAndAnomalyScore(CommunicationClazz element, long timestamp) {

		LOGGER.info("\ndoAnnotation for Timestamp: " + timestamp);
		TreeMapLongDoubleIValue responseTimes = (TreeMapLongDoubleIValue) element
				.getGenericData(IPluginKeys.TIMESTAMP_TO_RESPONSE_TIME);
		if (responseTimes == null) {
			responseTimes = new TreeMapLongDoubleIValue();
		}
		TreeMapLongDoubleIValue predictedResponseTimes = (TreeMapLongDoubleIValue) element
				.getGenericData(IPluginKeys.TIMESTAMP_TO_PREDICTED_RESPONSE_TIME);
		if (predictedResponseTimes == null) {
			predictedResponseTimes = new TreeMapLongDoubleIValue();
		}
		TreeMapLongDoubleIValue anomalyScores = (TreeMapLongDoubleIValue) element
				.getGenericData(IPluginKeys.TIMESTAMP_TO_ANOMALY_SCORE);
		if (anomalyScores == null) {
			anomalyScores = new TreeMapLongDoubleIValue();
		}

		Map<Long, RuntimeInformation> traceIdToRuntimeMap = element.getTraceIdToRuntimeMap();
		if (traceIdToRuntimeMap.size() == 0) {
			return;
		}
		double responseTime = new TraceAggregator().aggregateTraces(traceIdToRuntimeMap);
		responseTimes.put(timestamp, responseTime);

		double predictedResponseTime = AbstractForecaster.forecast(responseTimes,
				predictedResponseTimes);
		predictedResponseTimes.put(timestamp, predictedResponseTime);

		double anomalyScore = new CalculateAnomalyScore().getAnomalyScore(responseTime,
				predictedResponseTime);
		anomalyScores.put(timestamp, anomalyScore);
		boolean[] errorWarning = new InterpreteAnomalyScore().interprete(anomalyScore);
		if (errorWarning[1]) {
			element.putGenericBooleanData(IPluginKeys.ERROR_ANOMALY, true);
			element.getTarget().getParent().getBelongingApplication().getParent().getParent()
					.getParent().getParent()
					.putGenericBooleanData(IPluginKeys.ANOMALY_PRESENT, true);
		} else if (errorWarning[0]) {
			element.putGenericBooleanData(IPluginKeys.WARNING_ANOMALY, true);
			element.getTarget().getParent().getBelongingApplication().getParent().getParent()
					.getParent().getParent()
					.putGenericBooleanData(IPluginKeys.ANOMALY_PRESENT, true);
		}

		LOGGER.info("\nAntwortzeit: "
				+ responseTime
				+ "\nVorhergesagte Antwortzeit: "
				+ predictedResponseTime
				+ "\nAnomalyscore: "
				+ anomalyScore
				+ "\nWarning//Error: "
				+ errorWarning[0]
				+ "//"
				+ errorWarning[1]
				+ "\nhistoryResponseTimesSize//historyPredictedResponseTimesSize//historyAnomalyScoresSize: "
				+ responseTimes.size() + "//" + predictedResponseTimes.size() + "//"
				+ anomalyScores.size());

		element.putGenericData(IPluginKeys.TIMESTAMP_TO_RESPONSE_TIME, responseTimes);
		element.putGenericData(IPluginKeys.TIMESTAMP_TO_PREDICTED_RESPONSE_TIME,
				predictedResponseTimes);
		element.putGenericData(IPluginKeys.TIMESTAMP_TO_ANOMALY_SCORE, anomalyScores);

		annotateParentHierachy(element, errorWarning, responseTime, predictedResponseTime,
				anomalyScore, timestamp);
	}

	private static void annotateParentHierachy(CommunicationClazz element, boolean[] errorWarning,
			double responseTime, double predictedResponseTime, double anomalyScore, long timestamp) {
		Clazz clazz = element.getTarget();
		TreeMapLongDoubleIValue clazzResponseTimes = (TreeMapLongDoubleIValue) clazz
				.getGenericData(IPluginKeys.TIMESTAMP_TO_RESPONSE_TIME);
		if (clazzResponseTimes == null) {
			clazzResponseTimes = new TreeMapLongDoubleIValue();
		}
		if (clazzResponseTimes.get(timestamp) == null) {
			clazzResponseTimes.put(timestamp, responseTime);
			clazz.putGenericData(IPluginKeys.TIMESTAMP_TO_RESPONSE_TIME, clazzResponseTimes);
		} else if (clazzResponseTimes.get(timestamp) < responseTime) {
			clazzResponseTimes.put(timestamp, responseTime);
			clazz.putGenericData(IPluginKeys.TIMESTAMP_TO_RESPONSE_TIME, clazzResponseTimes);

		}
		TreeMapLongDoubleIValue clazzPredictedResponseTimes = (TreeMapLongDoubleIValue) clazz
				.getGenericData(IPluginKeys.TIMESTAMP_TO_PREDICTED_RESPONSE_TIME);
		if (clazzPredictedResponseTimes == null) {
			clazzPredictedResponseTimes = new TreeMapLongDoubleIValue();
		}
		if (clazzPredictedResponseTimes.get(timestamp) == null) {
			clazzPredictedResponseTimes.put(timestamp, predictedResponseTime);
			clazz.putGenericData(IPluginKeys.TIMESTAMP_TO_PREDICTED_RESPONSE_TIME,
					clazzPredictedResponseTimes);
		} else if (clazzPredictedResponseTimes.get(timestamp) < predictedResponseTime) {
			clazzPredictedResponseTimes.put(timestamp, predictedResponseTime);
			clazz.putGenericData(IPluginKeys.TIMESTAMP_TO_PREDICTED_RESPONSE_TIME,
					clazzPredictedResponseTimes);
		}
		TreeMapLongDoubleIValue clazzAnomalyScores = (TreeMapLongDoubleIValue) clazz
				.getGenericData(IPluginKeys.TIMESTAMP_TO_ANOMALY_SCORE);
		if (clazzAnomalyScores == null) {
			clazzAnomalyScores = new TreeMapLongDoubleIValue();
		}
		if (clazzAnomalyScores.get(timestamp) == null) {
			clazzAnomalyScores.put(timestamp, anomalyScore);
			clazz.putGenericData(IPluginKeys.TIMESTAMP_TO_ANOMALY_SCORE, clazzAnomalyScores);
		} else if (clazzAnomalyScores.get(timestamp) < anomalyScore) {
			clazzAnomalyScores.put(timestamp, anomalyScore);
			clazz.putGenericData(IPluginKeys.TIMESTAMP_TO_ANOMALY_SCORE, clazzAnomalyScores);
		}
		if (errorWarning[1]) {
			clazz.putGenericBooleanData(IPluginKeys.ERROR_ANOMALY, true);
		} else if (errorWarning[0]) {
			clazz.putGenericBooleanData(IPluginKeys.WARNING_ANOMALY, true);
		}

		annotateParentComponent(clazz.getParent(), errorWarning, responseTime,
				predictedResponseTime, anomalyScore, timestamp);
	}

	private static void annotateParentComponent(Component component, boolean[] errorWarning,
			double responseTime, double predictedResponseTime, double anomalyScore, long timestamp) {
		Component parentComponent = component.getParentComponent();
		TreeMapLongDoubleIValue componentResponseTimes = (TreeMapLongDoubleIValue) component
				.getGenericData(IPluginKeys.TIMESTAMP_TO_RESPONSE_TIME);
		if (componentResponseTimes == null) {
			componentResponseTimes = new TreeMapLongDoubleIValue();
		}
		if (componentResponseTimes.get(timestamp) == null) {
			componentResponseTimes.put(timestamp, responseTime);
			component
			.putGenericData(IPluginKeys.TIMESTAMP_TO_RESPONSE_TIME, componentResponseTimes);
		} else if (componentResponseTimes.get(timestamp) < responseTime) {
			componentResponseTimes.put(timestamp, responseTime);
			component
			.putGenericData(IPluginKeys.TIMESTAMP_TO_RESPONSE_TIME, componentResponseTimes);
		}
		TreeMapLongDoubleIValue componentPredictedResponseTimes = (TreeMapLongDoubleIValue) component
				.getGenericData(IPluginKeys.TIMESTAMP_TO_PREDICTED_RESPONSE_TIME);
		if (componentPredictedResponseTimes == null) {
			componentPredictedResponseTimes = new TreeMapLongDoubleIValue();
		}
		if (componentPredictedResponseTimes.get(timestamp) == null) {
			componentPredictedResponseTimes.put(timestamp, predictedResponseTime);
			component.putGenericData(IPluginKeys.TIMESTAMP_TO_PREDICTED_RESPONSE_TIME,
					componentPredictedResponseTimes);
		} else if (componentPredictedResponseTimes.get(timestamp) < predictedResponseTime) {
			componentPredictedResponseTimes.put(timestamp, predictedResponseTime);
			component.putGenericData(IPluginKeys.TIMESTAMP_TO_PREDICTED_RESPONSE_TIME,
					componentPredictedResponseTimes);
		}
		TreeMapLongDoubleIValue componentAnomalyScores = (TreeMapLongDoubleIValue) component
				.getGenericData(IPluginKeys.TIMESTAMP_TO_ANOMALY_SCORE);
		if (componentAnomalyScores == null) {
			componentAnomalyScores = new TreeMapLongDoubleIValue();
		}
		if (componentAnomalyScores.get(timestamp) == null) {
			componentAnomalyScores.put(timestamp, anomalyScore);
			component
			.putGenericData(IPluginKeys.TIMESTAMP_TO_ANOMALY_SCORE, componentAnomalyScores);
		} else if (componentAnomalyScores.get(timestamp) < anomalyScore) {
			componentAnomalyScores.put(timestamp, anomalyScore);
			component
			.putGenericData(IPluginKeys.TIMESTAMP_TO_ANOMALY_SCORE, componentAnomalyScores);
		}
		if (errorWarning[1]) {
			component.putGenericBooleanData(IPluginKeys.ERROR_ANOMALY, true);
		} else if (errorWarning[0]) {
			component.putGenericBooleanData(IPluginKeys.WARNING_ANOMALY, true);
		}

		if (parentComponent != null) {
			annotateParentComponent(parentComponent, errorWarning, responseTime,
					predictedResponseTime, anomalyScore, timestamp);
		} else {
			Application application = component.getBelongingApplication();
			TreeMapLongDoubleIValue applicationResponseTimes = (TreeMapLongDoubleIValue) application
					.getGenericData(IPluginKeys.TIMESTAMP_TO_RESPONSE_TIME);
			if (applicationResponseTimes == null) {
				applicationResponseTimes = new TreeMapLongDoubleIValue();
			}
			if (applicationResponseTimes.get(timestamp) == null) {
				applicationResponseTimes.put(timestamp, responseTime);
				application.putGenericData(IPluginKeys.TIMESTAMP_TO_RESPONSE_TIME,
						applicationResponseTimes);
			} else if (applicationResponseTimes.get(timestamp) < responseTime) {
				applicationResponseTimes.put(timestamp, responseTime);
				application.putGenericData(IPluginKeys.TIMESTAMP_TO_RESPONSE_TIME,
						applicationResponseTimes);
			}
			TreeMapLongDoubleIValue applicationPredictedResponseTimes = (TreeMapLongDoubleIValue) application
					.getGenericData(IPluginKeys.TIMESTAMP_TO_PREDICTED_RESPONSE_TIME);
			if (applicationPredictedResponseTimes == null) {
				applicationPredictedResponseTimes = new TreeMapLongDoubleIValue();
			}
			if (applicationPredictedResponseTimes.get(timestamp) == null) {
				applicationPredictedResponseTimes.put(timestamp, predictedResponseTime);
				application.putGenericData(IPluginKeys.TIMESTAMP_TO_PREDICTED_RESPONSE_TIME,
						applicationPredictedResponseTimes);
			} else if (applicationPredictedResponseTimes.get(timestamp) < predictedResponseTime) {
				applicationPredictedResponseTimes.put(timestamp, predictedResponseTime);
				application.putGenericData(IPluginKeys.TIMESTAMP_TO_PREDICTED_RESPONSE_TIME,
						applicationPredictedResponseTimes);
			}
			TreeMapLongDoubleIValue applicationAnomalyScores = (TreeMapLongDoubleIValue) application
					.getGenericData(IPluginKeys.TIMESTAMP_TO_ANOMALY_SCORE);
			if (applicationAnomalyScores == null) {
				applicationAnomalyScores = new TreeMapLongDoubleIValue();
			}
			if (applicationAnomalyScores.get(timestamp) == null) {
				applicationAnomalyScores.put(timestamp, anomalyScore);
				application.putGenericData(IPluginKeys.TIMESTAMP_TO_ANOMALY_SCORE,
						applicationAnomalyScores);
			} else if (applicationAnomalyScores.get(timestamp) < anomalyScore) {
				applicationAnomalyScores.put(timestamp, anomalyScore);
				application.putGenericData(IPluginKeys.TIMESTAMP_TO_ANOMALY_SCORE,
						applicationAnomalyScores);
			}
			if (errorWarning[1]) {
				application.putGenericBooleanData(IPluginKeys.ERROR_ANOMALY, true);
			} else if (errorWarning[0]) {
				application.putGenericBooleanData(IPluginKeys.WARNING_ANOMALY, true);
			}
		}
	}
}
