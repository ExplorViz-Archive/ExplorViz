package explorviz.plugin_server.anomalydetection;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import explorviz.plugin_server.anomalydetection.aggregation.TraceAggregatorTest;
import explorviz.plugin_server.anomalydetection.anomalyscore.*;
import explorviz.plugin_server.anomalydetection.forecast.*;

@RunWith(Suite.class)
@SuiteClasses({ TraceAggregatorTest.class, CalculateAnomalyScoreTest.class,
	InterpreteAnomalyScoreTest.class, NormalizeAnomalyScoreTest.class,
	AbstractForecasterTest.class, MovingAverageForecasterTest.class, NaiveForecasterTest.class,
	WeightedForecasterTest.class, OPADxTest.class })
public class AnomalyDetectionTestSuite {

}
