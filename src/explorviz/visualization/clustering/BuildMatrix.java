package explorviz.visualization.clustering;

import java.util.*;

public class BuildMatrix {

    public static double[][] stringDistances(String[] classnames) {

	// calculate string distances and standardize them
	int arraysize = (((classnames.length - 1) * (classnames.length)) / 2);
	double[] classnameDistances = new double[arraysize];

	for (int i = 0, k = 0; i < (classnames.length - 1); i++) {
	    for (int j = i + 1; j < classnames.length; j++, k++) {

		classnameDistances[k] = Levenshtein.distance(classnames[i], classnames[j]);
	    }
	}
	System.out.println(Arrays.toString(classnameDistances));
	double aMeanOfClassnameDistances = Standardization.arithmeticMean(classnameDistances);
	double sDeviationOfClassnameDistances = Standardization.standardDeviation(classnameDistances);

	// create all string distance pairs
	double[][] sd = new double[classnames.length][classnames.length];

	// if sDeviationOfClassnameDistances = 0 then all pairs of classes have
	// the same string distance which can therefore be ignored in distance
	// calculation and to avoid division by 0
	for (int i = 0; i < classnames.length; i++) {
	    for (int j = 0; j < classnames.length; j++) {
		if (sDeviationOfClassnameDistances == 0) {
		    sd[i][j] = 0;
		} else if (i == j) {
		    sd[i][j] = 0;
		} else {
		    sd[i][j] = ((Levenshtein.distance(classnames[i], classnames[j]) - aMeanOfClassnameDistances) / sDeviationOfClassnameDistances);
		    sd[j][i] = ((Levenshtein.distance(classnames[j], classnames[i]) - aMeanOfClassnameDistances) / sDeviationOfClassnameDistances);
		}

		// display string distance pairs
		System.out.println("sd(" + i + "," + j + ") = " + sd[i][j]);
	    }
	}

	return sd;
    }

    public static double[][] createMatrix(double[] methodCalls, double[] activeInstances, String[] classnames) {

	double[] stMethodCalls = Standardization.standardize(methodCalls);
	double[] stActiveInstances = Standardization.standardize(activeInstances);
	double[][] sd = stringDistances(classnames);

	// create all distance pairs
	double[][] d = new double[stMethodCalls.length][stMethodCalls.length];

	for (int i = 0; i < stMethodCalls.length; i++) {
	    for (int j = 0; j < stMethodCalls.length; j++) {

		if (i == j) {
		    d[i][j] = Double.POSITIVE_INFINITY;
		} else {
		    d[i][j] = Distance.calcDistance(stMethodCalls[i], stMethodCalls[j], stActiveInstances[i], stActiveInstances[j], sd[i][j]);
		    d[j][i] = Distance.calcDistance(stMethodCalls[i], stMethodCalls[j], stActiveInstances[i], stActiveInstances[j], sd[j][i]);
		}

		// display distance pairs
		System.out.println("d(" + i + "," + j + ") = " + d[i][j]);
	    }
	}
	return d;
    }

    public static void main(String[] args) {

	// test with kieker.monitoring.core.controller data
	// double[] methodCalls = { 6, 5, 1, 7, 1, 14, 1, 6, 5, 12, 6 };
	// double[] activeInstances = { 1, 1, 1, 1, 1, 2, 2, 1, 1, 1, 1 };
	// String[] classnames = { "ProbeController", "JMXController",
	// "AbstractController", "WriterController",
	// "MonitoringController$[Thread]1",
	// "MonitoringController", "JMXController$JMXImplementation",
	// "RegistryController", "SamplingController", "StateController",
	// "TimeSourceController" };

	// double[][] distanceMatrix = createMatrix(methodCalls,
	// activeInstances, classnames);

    }
}
