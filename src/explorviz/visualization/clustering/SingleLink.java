package explorviz.visualization.clustering;

/**
 *
 * @author Mirco Barzel
 *
 */
public class SingleLink extends GenericClusterLink {

	@Override
	void applyMetric(final double[][] distanceMatrix, final int cluster1, final int cluster2,
			final int j) {
		distanceMatrix[cluster1][j] = Math.min(distanceMatrix[cluster1][j],
				distanceMatrix[cluster2][j]);
		distanceMatrix[j][cluster1] = Math.min(distanceMatrix[cluster1][j],
				distanceMatrix[cluster2][j]);
	}
}
