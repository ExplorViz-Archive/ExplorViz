package explorviz.visualization.clustering;

import java.util.List;

/**
 *
 * @author Mirco Barzel
 *
 */
public class BuildMatrix {

	// Weights for distance measurement
	// Change value to 0 to deactivate a specific parameter
	static final int METHOD_WEIGHT = 1;
	static final int INSTANCE_WEIGHT = 1;
	static final int CLASSNAME_WEIGHT = 1;

	public static double[][] buildMatrix(final List<ClusterData> clusterdata) {
		final double[][] distanceMatrix = new double[clusterdata.size()][clusterdata.size()];

		for (int i = 0; i < clusterdata.size(); i++) {
			for (int j = 0; j < clusterdata.size(); j++) {

				if (i == j) {
					distanceMatrix[i][j] = Double.POSITIVE_INFINITY;
				} else {
					distanceMatrix[i][j] = distance(clusterdata.get(i), clusterdata.get(j));
				}
			}
		}

		return distanceMatrix;
	}

	public static double distance(final ClusterData class1, final ClusterData class2) {
		final double distance = euclidianDistance(class1, class2)
				+ (levenshteinDistance(class1.getName(), class2.getName()) * CLASSNAME_WEIGHT);

		return distance;
	}

	public static double euclidianDistance(final ClusterData class1, final ClusterData class2) {
		final double euclidianDistance = Math
				.sqrt((Math.pow((class1.getMethods() - class2.getMethods()), 2) * METHOD_WEIGHT)
						+ (Math.pow((class1.getInstances() - class2.getInstances()), 2) * INSTANCE_WEIGHT));

		return euclidianDistance;
	}

	public static int levenshteinDistance(String classname1, String classname2) {
		classname1 = classname1.toLowerCase();
		classname2 = classname2.toLowerCase();

		final int[] costs = new int[classname2.length() + 1];
		for (int j = 0; j < costs.length; j++) {
			costs[j] = j;
		}
		for (int i = 1; i <= classname1.length(); i++) {
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

}
