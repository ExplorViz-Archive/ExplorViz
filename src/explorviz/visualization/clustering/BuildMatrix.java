package explorviz.visualization.clustering;

import java.util.ArrayList;
import java.util.List;

public class BuildMatrix {

	// Weights for distance measurement
	// Change value to 0 to deactivate a specific parameter
	static int methodWeight = 1;
	static int instanceWeight = 1;
	static int classnameWeight = 1;

	public static double[][] buildMatrix(final List<ClusterData> clusterdata) {
		final double[][] distanceMatrix = new double[clusterdata.size()][clusterdata.size()];

		for (int i = 0; i < clusterdata.size(); i++) {
			for (int j = 0; j < clusterdata.size(); j++) {

				if (i == j) {
					distanceMatrix[i][j] = Double.POSITIVE_INFINITY;
				} else {
					distanceMatrix[i][j] = distance(clusterdata.get(i), clusterdata.get(j));
				}
				// System.out.println("d[" + i + "][" + j + "] = " +
				// distanceMatrix[i][j]);
			}
		}

		return distanceMatrix;
	}

	public static double distance(final ClusterData class1, final ClusterData class2) {
		final double distance = euclidianDistance(class1, class2)
				+ (levenshteinDistance(class1.name, class2.name) * classnameWeight);

		return distance;
	}

	public static double euclidianDistance(final ClusterData class1, final ClusterData class2) {

		final double euclidianDistance = Math
				.sqrt((Math.pow((class1.methods - class2.methods), 2) * methodWeight)
						+ (Math.pow((class1.instances - class2.instances), 2) * instanceWeight));

		return euclidianDistance;
	}

	public static int levenshteinDistance(String classname1, String classname2) {
		classname1 = classname1.toLowerCase();
		classname2 = classname2.toLowerCase();
		// i == 0
		final int[] costs = new int[classname2.length() + 1];
		for (int j = 0; j < costs.length; j++) {
			costs[j] = j;
		}
		for (int i = 1; i <= classname1.length(); i++) {
			// j == 0; nw = lev(i - 1, j)
			costs[0] = i;
			int nw = i - 1;
			for (int j = 1; j <= classname2.length(); j++) {
				final int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]),
						classname1.charAt(i - 1) == classname2.charAt(j - 1) ? nw : nw + 1);
				nw = costs[j];
				costs[j] = cj;
			}
		}
		return costs[classname2.length()];
	}

	public static void main(final String[] args) {
		final List<ClusterData> clazzes = new ArrayList<ClusterData>();

		final ClusterData class0 = new ClusterData();
		final ClusterData class1 = new ClusterData();
		final ClusterData class2 = new ClusterData();
		final ClusterData class3 = new ClusterData();
		final ClusterData class4 = new ClusterData();
		final ClusterData class5 = new ClusterData();
		final ClusterData class6 = new ClusterData();
		final ClusterData class7 = new ClusterData();
		final ClusterData class8 = new ClusterData();
		final ClusterData class9 = new ClusterData();
		final ClusterData class10 = new ClusterData();

		class0.name = "ProbeController";
		class1.name = "JMXController";
		class2.name = "AbstractController";
		class3.name = "WriterController";
		class4.name = "MonitoringController$[Thread]1";
		class5.name = "MonitoringController";
		class6.name = "JMXController$JMXImplementation";
		class7.name = "RegistryController";
		class8.name = "SamplingController";
		class9.name = "StateController";
		class10.name = "TimeSourceController";

		class0.methods = 6;
		class1.methods = 5;
		class2.methods = 1;
		class3.methods = 7;
		class4.methods = 1;
		class5.methods = 14;
		class6.methods = 1;
		class7.methods = 6;
		class8.methods = 5;
		class9.methods = 12;
		class10.methods = 6;

		class0.instances = 1;
		class1.instances = 1;
		class2.instances = 1;
		class3.instances = 1;
		class4.instances = 1;
		class5.instances = 2;
		class6.instances = 2;
		class7.instances = 1;
		class8.instances = 1;
		class9.instances = 1;
		class10.instances = 1;

		clazzes.add(class0);
		clazzes.add(class1);
		clazzes.add(class2);
		clazzes.add(class3);
		clazzes.add(class4);
		clazzes.add(class5);
		clazzes.add(class6);
		clazzes.add(class7);
		clazzes.add(class8);
		clazzes.add(class9);
		clazzes.add(class10);

		buildMatrix(clazzes);
	}
}
