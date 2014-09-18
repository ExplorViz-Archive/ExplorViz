package explorviz.visualization.clustering;

import java.util.List;

import explorviz.shared.model.Component;

public class SingleLink {

	public static Component doSingleLink(final List<ClusterData> clusterdata) {

		final Component clusteredComponent = new Component();

		// build distance matrix
		final double[][] distanceMatrix = BuildMatrix.buildMatrix(clusterdata);

		int cluster1 = 0;
		int cluster2 = 0;

		// algorithm always does n-1 iterations where n represents the number of
		// classes
		for (int n = 0; n < (clusterdata.size() - 1); n++) {

			// reset minValue
			double minValue = Double.MAX_VALUE;

			// find nearest clusters to form a new cluster:
			for (int i = 0; i < clusterdata.size(); i++) {
				for (int j = 0; j < clusterdata.size(); j++) {

					if (distanceMatrix[i][j] == 0) {
						continue;
					} else if (distanceMatrix[i][j] < minValue) {
						minValue = distanceMatrix[i][j];
						cluster1 = i;
						cluster2 = j;

						// createComponent(clusterdata.get(cluster1).clazz,
						// clusterdata.get(cluster2).clazz)
					}
				}
			}

			// build new matrix
			// step 1:
			// row and column of one merged cluster serve as new place for
			// values of new cluster
			for (int j = 0; j < clusterdata.size(); j++) {

				if (cluster1 == j) {
					distanceMatrix[cluster1][j] = 0;

				} else {
					distanceMatrix[cluster1][j] = Math.min(distanceMatrix[cluster1][j],
							distanceMatrix[cluster2][j]);
					distanceMatrix[j][cluster1] = Math.min(distanceMatrix[cluster1][j],
							distanceMatrix[cluster2][j]);

				}

			}

			// step 2:
			// set row and column values of the other merged cluster to 0
			// to simulate deletion of said cluster from matrix
			for (int j = 0; j < clusterdata.size(); j++) {
				distanceMatrix[cluster2][j] = 0;
				distanceMatrix[j][cluster2] = 0;
			}
		}

		return clusteredComponent;

	}
}
