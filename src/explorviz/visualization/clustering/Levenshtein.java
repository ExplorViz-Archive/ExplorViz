package explorviz.visualization.clustering;

public class Levenshtein {
    public static int distance(String a, String b) {
	a = a.toLowerCase();
	b = b.toLowerCase();
	// i == 0
	int[] costs = new int[b.length() + 1];
	for (int j = 0; j < costs.length; j++)
	    costs[j] = j;
	for (int i = 1; i <= a.length(); i++) {
	    // j == 0; nw = lev(i - 1, j)
	    costs[0] = i;
	    int nw = i - 1;
	    for (int j = 1; j <= b.length(); j++) {
		int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]), a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
		nw = costs[j];
		costs[j] = cj;
	    }
	}
	return costs[b.length()];
    }

    public static void main(String[] args) {

	// test with kieker.monitoring.core.controller data
	String[] classnames = { "ProbeController", "JMXController", "AbstractController", "WriterController", "MonitoringController$[Thread]1",
		"MonitoringController", "JMXController$JMXImplementation", "RegistryController", "SamplingController", "StateController",
		"TimeSourceController" };

	for (int i = 0; i < classnames.length; i++) {
	    for (int j = i + 1; j < classnames.length; j++) {
		System.out.println("distance between " + classnames[i] + " and " + classnames[j] + " is " + distance(classnames[i], classnames[j]));
	    }
	}
    }

}