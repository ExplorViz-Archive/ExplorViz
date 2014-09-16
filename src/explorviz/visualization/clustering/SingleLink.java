package explorviz.visualization.clustering;

public class SingleLink {

    public static void doSingleLink(double[] methodCalls, double[] activeInstances, String[] classnames) {

	// build distance matrix
	double[][] distanceMatrix = BuildMatrix.createMatrix(methodCalls, activeInstances, classnames);

	int cluster1 = 0;
	int cluster2 = 0;

	// create cluster for every class to overwrite later
	String[] cluster = new String[classnames.length];
	for (int i = 0; i < classnames.length; i++) {
	    cluster[i] = "(" + classnames[i] + ")";
	}

	// algorithm always does n-1 iterations where n represents the number of
	// classes
	System.out.println("begin singleLink for clusters:");
	for (int i = 0; i < classnames.length; i++) {
	    System.out.println("(" + classnames[i] + ")");
	}
	System.out.println();

	for (int n = 0; n < (methodCalls.length - 1); n++) {

	    System.out.println("step " + (n + 1) + ":");

	    // reset minValue
	    double minValue = Double.MAX_VALUE;

	    // find nearest clusters to form a new cluster: (cluster1, cluster2)
	    for (int i = 0; i < methodCalls.length; i++) {
		for (int j = 0; j < methodCalls.length; j++) {

		    if (i == j) {
			continue;
		    } else if (distanceMatrix[i][j] < minValue) {
			minValue = distanceMatrix[i][j];
			cluster1 = i;
			cluster2 = j;
		    }
		}
	    }

	    System.out.println("clusters " + cluster[cluster1] + " and " + cluster[cluster2] + " are closest clusters at distance " + minValue);
	    System.out.println("Added " + cluster[cluster2] + " to Cluster " + cluster[cluster1]);

	    cluster[cluster1] = "(" + cluster[cluster1] + ", " + cluster[cluster2] + ")";
	    cluster[cluster2] = "";

	    System.out.println();
	    System.out.println("current clusters are: ");
	    for (int i = 0; i < cluster.length; i++) {
		if (cluster[i] == "") {
		    continue;
		} else {
		    System.out.println(cluster[i]);
		}
	    }

	    System.out.println();
	    if (n < (methodCalls.length - 2)) {
		System.out.println("building new matrix");
	    }
	    System.out.println();

	    // build new matrix
	    // step 1:
	    // row and column of one merged cluster serve as new place for
	    // values of new cluster
	    for (int j = 0; j < methodCalls.length; j++) {

		if (cluster1 == j) {
		    distanceMatrix[cluster1][j] = Double.POSITIVE_INFINITY;

		} else {
		    distanceMatrix[cluster1][j] = Math.min(distanceMatrix[cluster1][j], distanceMatrix[cluster2][j]);
		    distanceMatrix[j][cluster1] = Math.min(distanceMatrix[cluster1][j], distanceMatrix[cluster2][j]);

		}

	    }

	    // step 2:
	    // set row and column values of the other merged cluster to INFINITY
	    // to simulate deletion of said cluster from matrix
	    for (int j = 0; j < methodCalls.length; j++) {
		distanceMatrix[cluster2][j] = Double.POSITIVE_INFINITY;
		distanceMatrix[j][cluster2] = Double.POSITIVE_INFINITY;
	    }
	}

	System.out.println("all clusters have been merged");

	return;

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

	// random test
	double[] methodCalls = { 5, 1, 7, 3, 7, 2, 2, 6, 9, 4 };
	double[] activeInstances = { 2, 5, 5, 7, 1, 9, 4, 1, 7, 1 };
	String[] classnames = { "Auto", "Wurst", "Mortadella", "Zettel", "Tastatur", "Boxen", "Mousepad", "Taschentuch", "Energydrink", "Handy" };

	doSingleLink(methodCalls, activeInstances, classnames);

    }

}
