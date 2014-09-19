package explorviz.visualization.clustering;

import java.util.ArrayList;
import java.util.List;

import explorviz.shared.model.*;
import explorviz.visualization.renderer.ColorDefinitions;

public class CompleteLink {

	public static Component doCompleteLink(final List<ClusterData> clusterdata,
			final Application application) {

		// build distance matrix
		final double[][] distanceMatrix = BuildMatrix.buildMatrix(clusterdata);

		int cluster1 = 0;
		int cluster2 = 0;
		final Component[] c = new Component[clusterdata.size()];

		// put every class in cluster(component) in first clustering step
		for (int i = 0; i < clusterdata.size(); i++) {
			final ClusterData currentData = clusterdata.get(i);
			final List<Clazz> clazzes = new ArrayList<Clazz>();
			clazzes.add(currentData.clazz);
			c[i] = new Component();
			c[i].setName(currentData.name);
			c[i].setParentComponent(currentData.clazz.getParent());
			c[i].setBelongingApplication(application);
			c[i].setFullQualifiedName(currentData.clazz.getParent().getFullQualifiedName() + "."
					+ c[i].getName());
			currentData.clazz.setParent(c[i]);
			c[i].setSynthetic(true);
			c[i].setClazzes(clazzes);
		}

		// algorithm always does n-1 iterations where n represents the number of
		// classes
		for (int n = 0; n < (clusterdata.size() - 1); n++) {

			// reset minValue
			double maxValue = Double.MIN_VALUE;

			// find nearest clusters to form a new cluster:
			for (int i = 0; i < clusterdata.size(); i++) {
				for (int j = 0; j < clusterdata.size(); j++) {

					if (distanceMatrix[i][j] == 0) {
						continue;
					} else if (distanceMatrix[i][j] > maxValue) {
						maxValue = distanceMatrix[i][j];
						cluster1 = i;
						cluster2 = j;
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
					distanceMatrix[cluster1][j] = Math.max(distanceMatrix[cluster1][j],
							distanceMatrix[cluster2][j]);
					distanceMatrix[j][cluster1] = Math.max(distanceMatrix[cluster1][j],
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

			// create new component out of 2
			final Component mergedCluster = new Component();
			mergedCluster.setParentComponent(c[cluster1].getParentComponent());
			mergedCluster.setBelongingApplication(application);
			mergedCluster.setName("clusterstep" + (n + 1));
			mergedCluster.setFullQualifiedName(c[cluster1].getParentComponent()
					.getFullQualifiedName() + "." + mergedCluster.getName());

			c[cluster1].setParentComponent(mergedCluster);
			c[cluster1].setFullQualifiedName(mergedCluster.getFullQualifiedName() + "."
					+ c[cluster1].getName());
			c[cluster2].setParentComponent(mergedCluster);
			c[cluster2].setFullQualifiedName(mergedCluster.getFullQualifiedName() + "."
					+ c[cluster2].getName());

			final List<Component> components = new ArrayList<Component>();
			components.add(c[cluster1]);
			components.add(c[cluster2]);
			mergedCluster.setChildren(components);

			mergedCluster.setOpened(false);

			c[cluster1] = mergedCluster;
		}

		setColors(c[cluster1], 0);

		return c[cluster1];
	}

	private static void setColors(final Component component, final int i) {
		for (final Component child : component.getChildren()) {
			setColors(child, i + 1);
		}

		if ((i % 2) == 1) {
			component.setColor(ColorDefinitions.componentSyntheticColor);
		} else {
			component.setColor(ColorDefinitions.componentSyntheticSecondColor);
		}
	}

}
