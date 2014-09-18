package explorviz.visualization.clustering;

import java.util.List;

public class BuildMatrix {

	public static double[][] buildMatrix(final List<ClusterData> clusterdata) {
		final double[][] distanceMatrix = new double[clusterdata.size()][clusterdata.size()];

		for (int i = 0; i < clusterdata.size(); i++) {
			for (int j = 0; j < clusterdata.size(); j++) {

				distanceMatrix[i][j] = distance(clusterdata.get(i), clusterdata.get(j));
			}
		}

		return distanceMatrix;
	}

	public static double distance(final ClusterData class1, final ClusterData class2) {
		final double distance = euclidianDistance(class1, class2)
				+ levenshteinDistance(class1.name, class2.name);

		return distance;
	}

	public static double euclidianDistance(final ClusterData class1, final ClusterData class2) {

		final double euclidianDistance = Math.sqrt(Math.pow((class1.methods - class2.methods), 2)
				+ Math.pow((class1.instances - class2.instances), 2));

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

}
