package explorviz.plugin_server.anomalydetection;

import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;

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

	static final Logger LOGGER = Logger.getLogger("AnomalyDetection");
	static final int SIZE = explorviz.server.main.Configuration.TIMESHIFT_INTERVAL_IN_MINUTES * 6;

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
			Landscape landscape = element.getTarget().getParent().getBelongingApplication()
					.getParent().getParent().getParent().getParent();
			element.putGenericBooleanData(IPluginKeys.ERROR_ANOMALY, true);
			if (landscape.isGenericDataPresent(IPluginKeys.ANOMALY_PRESENT)) {
				if (!(landscape.getGenericBooleanData(IPluginKeys.ANOMALY_PRESENT))) {
					landscape.putGenericLongData(IPluginKeys.ANOMALY_PRESENT_ON_TIMESTAMP,
							timestamp);
				}
			} else {
				landscape.putGenericLongData(IPluginKeys.ANOMALY_PRESENT_ON_TIMESTAMP, timestamp);
			}
			landscape.putGenericBooleanData(IPluginKeys.ANOMALY_PRESENT, true);
		} else if (errorWarning[0]) {
			Landscape landscape = element.getTarget().getParent().getBelongingApplication()
					.getParent().getParent().getParent().getParent();
			element.putGenericBooleanData(IPluginKeys.WARNING_ANOMALY, true);
			if (landscape.isGenericDataPresent(IPluginKeys.ANOMALY_PRESENT)) {
				if (!(landscape.getGenericBooleanData(IPluginKeys.ANOMALY_PRESENT))) {
					landscape.putGenericLongData(IPluginKeys.ANOMALY_PRESENT_ON_TIMESTAMP,
							timestamp);
				}
			} else {
				landscape.putGenericLongData(IPluginKeys.ANOMALY_PRESENT_ON_TIMESTAMP, timestamp);
			}
			landscape.putGenericBooleanData(IPluginKeys.ANOMALY_PRESENT, true);
		}

		if ((predictedResponseTimes.size() >= SIZE) && (responseTimes.size() >= SIZE)
				&& (anomalyScores.size() >= SIZE)) {

			TreeMapLongDoubleIValue limitedPredictedResponseTimes = new TreeMapLongDoubleIValue();
			for (int i = 0; i < (SIZE - 1); i++) {
				long key = Collections.max(predictedResponseTimes.keySet());
				limitedPredictedResponseTimes.put(key, predictedResponseTimes.get(key));
				predictedResponseTimes.remove(key);
			}
			predictedResponseTimes = new TreeMapLongDoubleIValue();
			predictedResponseTimes.putAll(limitedPredictedResponseTimes);

			TreeMapLongDoubleIValue limitedResponseTimes = new TreeMapLongDoubleIValue();
			for (int i = 0; i < (SIZE - 1); i++) {
				long key = Collections.max(responseTimes.keySet());
				limitedResponseTimes.put(key, responseTimes.get(key));
				responseTimes.remove(key);
			}
			responseTimes = new TreeMapLongDoubleIValue();
			responseTimes.putAll(limitedResponseTimes);

			TreeMapLongDoubleIValue limitedAnomalyScores = new TreeMapLongDoubleIValue();
			for (int i = 0; i < (SIZE - 1); i++) {
				long key = Collections.max(anomalyScores.keySet());
				limitedAnomalyScores.put(key, anomalyScores.get(key));
				anomalyScores.remove(key);
			}
			anomalyScores = new TreeMapLongDoubleIValue();
			anomalyScores.putAll(limitedAnomalyScores);
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

		TreeMapLongDoubleIValue clazzAnomalyScores = (TreeMapLongDoubleIValue) clazz
				.getGenericData(IPluginKeys.TIMESTAMP_TO_ANOMALY_SCORE);
		TreeMapLongDoubleIValue clazzResponseTimes = (TreeMapLongDoubleIValue) clazz
				.getGenericData(IPluginKeys.TIMESTAMP_TO_RESPONSE_TIME);
		TreeMapLongDoubleIValue clazzPredictedResponseTimes = (TreeMapLongDoubleIValue) clazz
				.getGenericData(IPluginKeys.TIMESTAMP_TO_PREDICTED_RESPONSE_TIME);

		if (clazzAnomalyScores == null) {
			clazzAnomalyScores = new TreeMapLongDoubleIValue();
		}
		if (clazzResponseTimes == null) {
			clazzResponseTimes = new TreeMapLongDoubleIValue();
		}
		if (clazzPredictedResponseTimes == null) {
			clazzPredictedResponseTimes = new TreeMapLongDoubleIValue();
		}

		if (clazzAnomalyScores.get(timestamp) == null) {

			if ((clazzPredictedResponseTimes.size() >= SIZE) && (clazzResponseTimes.size() >= SIZE)
					&& (clazzAnomalyScores.size() >= SIZE)) {

				TreeMapLongDoubleIValue limitedPredictedResponseTimes = new TreeMapLongDoubleIValue();
				for (int i = 0; i < (SIZE - 1); i++) {
					long key = Collections.max(clazzPredictedResponseTimes.keySet());
					limitedPredictedResponseTimes.put(key, clazzPredictedResponseTimes.get(key));
					clazzPredictedResponseTimes.remove(key);
				}
				clazzPredictedResponseTimes = new TreeMapLongDoubleIValue();
				clazzPredictedResponseTimes.putAll(limitedPredictedResponseTimes);

				TreeMapLongDoubleIValue limitedResponseTimes = new TreeMapLongDoubleIValue();
				for (int i = 0; i < (SIZE - 1); i++) {
					long key = Collections.max(clazzResponseTimes.keySet());
					limitedResponseTimes.put(key, clazzResponseTimes.get(key));
					clazzResponseTimes.remove(key);
				}
				clazzResponseTimes = new TreeMapLongDoubleIValue();
				clazzResponseTimes.putAll(limitedResponseTimes);

				TreeMapLongDoubleIValue limitedAnomalyScores = new TreeMapLongDoubleIValue();
				for (int i = 0; i < (SIZE - 1); i++) {
					long key = Collections.max(clazzAnomalyScores.keySet());
					limitedAnomalyScores.put(key, clazzAnomalyScores.get(key));
					clazzAnomalyScores.remove(key);
				}
				clazzAnomalyScores = new TreeMapLongDoubleIValue();
				clazzAnomalyScores.putAll(limitedAnomalyScores);
			}

			clazzAnomalyScores.put(timestamp, anomalyScore);
			clazz.putGenericData(IPluginKeys.TIMESTAMP_TO_ANOMALY_SCORE, clazzAnomalyScores);
			clazzResponseTimes.put(timestamp, responseTime);
			clazz.putGenericData(IPluginKeys.TIMESTAMP_TO_RESPONSE_TIME, clazzResponseTimes);
			clazzPredictedResponseTimes.put(timestamp, predictedResponseTime);
			clazz.putGenericData(IPluginKeys.TIMESTAMP_TO_PREDICTED_RESPONSE_TIME,
					clazzPredictedResponseTimes);
		} else if (Math.abs(clazzAnomalyScores.get(timestamp)) < Math.abs(anomalyScore)) {

			if ((clazzPredictedResponseTimes.size() >= SIZE) && (clazzResponseTimes.size() >= SIZE)
					&& (clazzAnomalyScores.size() >= SIZE)) {

				TreeMapLongDoubleIValue limitedPredictedResponseTimes = new TreeMapLongDoubleIValue();
				for (int i = 0; i < (SIZE - 1); i++) {
					long key = Collections.max(clazzPredictedResponseTimes.keySet());
					limitedPredictedResponseTimes.put(key, clazzPredictedResponseTimes.get(key));
					clazzPredictedResponseTimes.remove(key);
				}
				clazzPredictedResponseTimes = new TreeMapLongDoubleIValue();
				clazzPredictedResponseTimes.putAll(limitedPredictedResponseTimes);

				TreeMapLongDoubleIValue limitedResponseTimes = new TreeMapLongDoubleIValue();
				for (int i = 0; i < (SIZE - 1); i++) {
					long key = Collections.max(clazzResponseTimes.keySet());
					limitedResponseTimes.put(key, clazzResponseTimes.get(key));
					clazzResponseTimes.remove(key);
				}
				clazzResponseTimes = new TreeMapLongDoubleIValue();
				clazzResponseTimes.putAll(limitedResponseTimes);

				TreeMapLongDoubleIValue limitedAnomalyScores = new TreeMapLongDoubleIValue();
				for (int i = 0; i < (SIZE - 1); i++) {
					long key = Collections.max(clazzAnomalyScores.keySet());
					limitedAnomalyScores.put(key, clazzAnomalyScores.get(key));
					clazzAnomalyScores.remove(key);
				}
				clazzAnomalyScores = new TreeMapLongDoubleIValue();
				clazzAnomalyScores.putAll(limitedAnomalyScores);
			}
			clazzAnomalyScores.put(timestamp, anomalyScore);
			clazz.putGenericData(IPluginKeys.TIMESTAMP_TO_ANOMALY_SCORE, clazzAnomalyScores);
			clazzResponseTimes.put(timestamp, responseTime);
			clazz.putGenericData(IPluginKeys.TIMESTAMP_TO_RESPONSE_TIME, clazzResponseTimes);
			clazzPredictedResponseTimes.put(timestamp, predictedResponseTime);
			clazz.putGenericData(IPluginKeys.TIMESTAMP_TO_PREDICTED_RESPONSE_TIME,
					clazzPredictedResponseTimes);
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
		TreeMapLongDoubleIValue componentPredictedResponseTimes = (TreeMapLongDoubleIValue) component
				.getGenericData(IPluginKeys.TIMESTAMP_TO_PREDICTED_RESPONSE_TIME);
		TreeMapLongDoubleIValue componentAnomalyScores = (TreeMapLongDoubleIValue) component
				.getGenericData(IPluginKeys.TIMESTAMP_TO_ANOMALY_SCORE);

		if (componentAnomalyScores == null) {
			componentAnomalyScores = new TreeMapLongDoubleIValue();
		}
		if (componentResponseTimes == null) {
			componentResponseTimes = new TreeMapLongDoubleIValue();
		}
		if (componentPredictedResponseTimes == null) {
			componentPredictedResponseTimes = new TreeMapLongDoubleIValue();
		}

		if (componentAnomalyScores.get(timestamp) == null) {

			if ((componentPredictedResponseTimes.size() >= SIZE)
					&& (componentResponseTimes.size() >= SIZE)
					&& (componentAnomalyScores.size() >= SIZE)) {

				TreeMapLongDoubleIValue limitedPredictedResponseTimes = new TreeMapLongDoubleIValue();
				for (int i = 0; i < (SIZE - 1); i++) {
					long key = Collections.max(componentPredictedResponseTimes.keySet());
					limitedPredictedResponseTimes
							.put(key, componentPredictedResponseTimes.get(key));
					componentPredictedResponseTimes.remove(key);
				}
				componentPredictedResponseTimes = new TreeMapLongDoubleIValue();
				componentPredictedResponseTimes.putAll(limitedPredictedResponseTimes);

				TreeMapLongDoubleIValue limitedResponseTimes = new TreeMapLongDoubleIValue();
				for (int i = 0; i < (SIZE - 1); i++) {
					long key = Collections.max(componentResponseTimes.keySet());
					limitedResponseTimes.put(key, componentResponseTimes.get(key));
					componentResponseTimes.remove(key);
				}
				componentResponseTimes = new TreeMapLongDoubleIValue();
				componentResponseTimes.putAll(limitedResponseTimes);

				TreeMapLongDoubleIValue limitedAnomalyScores = new TreeMapLongDoubleIValue();
				for (int i = 0; i < (SIZE - 1); i++) {
					long key = Collections.max(componentAnomalyScores.keySet());
					limitedAnomalyScores.put(key, componentAnomalyScores.get(key));
					componentAnomalyScores.remove(key);
				}
				componentAnomalyScores = new TreeMapLongDoubleIValue();
				componentAnomalyScores.putAll(limitedAnomalyScores);
			}

			componentAnomalyScores.put(timestamp, anomalyScore);
			component
			.putGenericData(IPluginKeys.TIMESTAMP_TO_ANOMALY_SCORE, componentAnomalyScores);
			componentResponseTimes.put(timestamp, responseTime);
			component
			.putGenericData(IPluginKeys.TIMESTAMP_TO_RESPONSE_TIME, componentResponseTimes);
			componentPredictedResponseTimes.put(timestamp, predictedResponseTime);
			component.putGenericData(IPluginKeys.TIMESTAMP_TO_PREDICTED_RESPONSE_TIME,
					componentPredictedResponseTimes);
		} else if (Math.abs(componentAnomalyScores.get(timestamp)) < Math.abs(anomalyScore)) {

			if ((componentPredictedResponseTimes.size() >= SIZE)
					&& (componentResponseTimes.size() >= SIZE)
					&& (componentAnomalyScores.size() >= SIZE)) {

				TreeMapLongDoubleIValue limitedPredictedResponseTimes = new TreeMapLongDoubleIValue();
				for (int i = 0; i < (SIZE - 1); i++) {
					long key = Collections.max(componentPredictedResponseTimes.keySet());
					limitedPredictedResponseTimes
							.put(key, componentPredictedResponseTimes.get(key));
					componentPredictedResponseTimes.remove(key);
				}
				componentPredictedResponseTimes = new TreeMapLongDoubleIValue();
				componentPredictedResponseTimes.putAll(limitedPredictedResponseTimes);

				TreeMapLongDoubleIValue limitedResponseTimes = new TreeMapLongDoubleIValue();
				for (int i = 0; i < (SIZE - 1); i++) {
					long key = Collections.max(componentResponseTimes.keySet());
					limitedResponseTimes.put(key, componentResponseTimes.get(key));
					componentResponseTimes.remove(key);
				}
				componentResponseTimes = new TreeMapLongDoubleIValue();
				componentResponseTimes.putAll(limitedResponseTimes);

				TreeMapLongDoubleIValue limitedAnomalyScores = new TreeMapLongDoubleIValue();
				for (int i = 0; i < (SIZE - 1); i++) {
					long key = Collections.max(componentAnomalyScores.keySet());
					limitedAnomalyScores.put(key, componentAnomalyScores.get(key));
					componentAnomalyScores.remove(key);
				}
				componentAnomalyScores = new TreeMapLongDoubleIValue();
				componentAnomalyScores.putAll(limitedAnomalyScores);
			}

			componentAnomalyScores.put(timestamp, anomalyScore);
			component
			.putGenericData(IPluginKeys.TIMESTAMP_TO_ANOMALY_SCORE, componentAnomalyScores);
			componentResponseTimes.put(timestamp, responseTime);
			component
			.putGenericData(IPluginKeys.TIMESTAMP_TO_RESPONSE_TIME, componentResponseTimes);
			componentPredictedResponseTimes.put(timestamp, predictedResponseTime);
			component.putGenericData(IPluginKeys.TIMESTAMP_TO_PREDICTED_RESPONSE_TIME,
					componentPredictedResponseTimes);
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
			TreeMapLongDoubleIValue applicationPredictedResponseTimes = (TreeMapLongDoubleIValue) application
					.getGenericData(IPluginKeys.TIMESTAMP_TO_PREDICTED_RESPONSE_TIME);
			TreeMapLongDoubleIValue applicationAnomalyScores = (TreeMapLongDoubleIValue) application
					.getGenericData(IPluginKeys.TIMESTAMP_TO_ANOMALY_SCORE);

			if (applicationAnomalyScores == null) {
				applicationAnomalyScores = new TreeMapLongDoubleIValue();
			}
			if (applicationResponseTimes == null) {
				applicationResponseTimes = new TreeMapLongDoubleIValue();
			}
			if (applicationPredictedResponseTimes == null) {
				applicationPredictedResponseTimes = new TreeMapLongDoubleIValue();
			}
			if (applicationAnomalyScores.get(timestamp) == null) {

				if ((applicationPredictedResponseTimes.size() >= SIZE)
						&& (applicationResponseTimes.size() >= SIZE)
						&& (applicationAnomalyScores.size() >= SIZE)) {

					TreeMapLongDoubleIValue limitedPredictedResponseTimes = new TreeMapLongDoubleIValue();
					for (int i = 0; i < (SIZE - 1); i++) {
						long key = Collections.max(applicationPredictedResponseTimes.keySet());
						limitedPredictedResponseTimes.put(key,
								applicationPredictedResponseTimes.get(key));
						applicationPredictedResponseTimes.remove(key);
					}
					applicationPredictedResponseTimes = new TreeMapLongDoubleIValue();
					applicationPredictedResponseTimes.putAll(limitedPredictedResponseTimes);

					TreeMapLongDoubleIValue limitedResponseTimes = new TreeMapLongDoubleIValue();
					for (int i = 0; i < (SIZE - 1); i++) {
						long key = Collections.max(applicationResponseTimes.keySet());
						limitedResponseTimes.put(key, applicationResponseTimes.get(key));
						applicationResponseTimes.remove(key);
					}
					applicationResponseTimes = new TreeMapLongDoubleIValue();
					applicationResponseTimes.putAll(limitedResponseTimes);

					TreeMapLongDoubleIValue limitedAnomalyScores = new TreeMapLongDoubleIValue();
					for (int i = 0; i < (SIZE - 1); i++) {
						long key = Collections.max(applicationAnomalyScores.keySet());
						limitedAnomalyScores.put(key, applicationAnomalyScores.get(key));
						applicationAnomalyScores.remove(key);
					}
					applicationAnomalyScores = new TreeMapLongDoubleIValue();
					applicationAnomalyScores.putAll(limitedAnomalyScores);
				}

				applicationAnomalyScores.put(timestamp, anomalyScore);
				application.putGenericData(IPluginKeys.TIMESTAMP_TO_ANOMALY_SCORE,
						applicationAnomalyScores);
				applicationResponseTimes.put(timestamp, responseTime);
				application.putGenericData(IPluginKeys.TIMESTAMP_TO_RESPONSE_TIME,
						applicationResponseTimes);
				applicationPredictedResponseTimes.put(timestamp, predictedResponseTime);
				application.putGenericData(IPluginKeys.TIMESTAMP_TO_PREDICTED_RESPONSE_TIME,
						applicationPredictedResponseTimes);
			} else if (Math.abs(applicationAnomalyScores.get(timestamp)) < Math.abs(anomalyScore)) {

				if ((applicationPredictedResponseTimes.size() >= SIZE)
						&& (applicationResponseTimes.size() >= SIZE)
						&& (applicationAnomalyScores.size() >= SIZE)) {

					TreeMapLongDoubleIValue limitedPredictedResponseTimes = new TreeMapLongDoubleIValue();
					for (int i = 0; i < (SIZE - 1); i++) {
						long key = Collections.max(applicationPredictedResponseTimes.keySet());
						limitedPredictedResponseTimes.put(key,
								applicationPredictedResponseTimes.get(key));
						applicationPredictedResponseTimes.remove(key);
					}
					applicationPredictedResponseTimes = new TreeMapLongDoubleIValue();
					applicationPredictedResponseTimes.putAll(limitedPredictedResponseTimes);

					TreeMapLongDoubleIValue limitedResponseTimes = new TreeMapLongDoubleIValue();
					for (int i = 0; i < (SIZE - 1); i++) {
						long key = Collections.max(applicationResponseTimes.keySet());
						limitedResponseTimes.put(key, applicationResponseTimes.get(key));
						applicationResponseTimes.remove(key);
					}
					applicationResponseTimes = new TreeMapLongDoubleIValue();
					applicationResponseTimes.putAll(limitedResponseTimes);

					TreeMapLongDoubleIValue limitedAnomalyScores = new TreeMapLongDoubleIValue();
					for (int i = 0; i < (SIZE - 1); i++) {
						long key = Collections.max(applicationAnomalyScores.keySet());
						limitedAnomalyScores.put(key, applicationAnomalyScores.get(key));
						applicationAnomalyScores.remove(key);
					}
					applicationAnomalyScores = new TreeMapLongDoubleIValue();
					applicationAnomalyScores.putAll(limitedAnomalyScores);
				}

				applicationAnomalyScores.put(timestamp, anomalyScore);
				application.putGenericData(IPluginKeys.TIMESTAMP_TO_ANOMALY_SCORE,
						applicationAnomalyScores);
				applicationResponseTimes.put(timestamp, responseTime);
				application.putGenericData(IPluginKeys.TIMESTAMP_TO_RESPONSE_TIME,
						applicationResponseTimes);
				applicationPredictedResponseTimes.put(timestamp, predictedResponseTime);
				application.putGenericData(IPluginKeys.TIMESTAMP_TO_PREDICTED_RESPONSE_TIME,
						applicationPredictedResponseTimes);
			}

			if (errorWarning[1]) {
				application.putGenericBooleanData(IPluginKeys.ERROR_ANOMALY, true);
			} else if (errorWarning[0]) {
				application.putGenericBooleanData(IPluginKeys.WARNING_ANOMALY, true);
			}
		}
	}
}
