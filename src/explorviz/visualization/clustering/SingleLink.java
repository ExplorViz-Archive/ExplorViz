package explorviz.visualization.clustering;

import java.util.ArrayList;
import java.util.List;

import explorviz.shared.model.Clazz;
import explorviz.shared.model.Component;

public class SingleLink {

	public static Component doSingleLink(final List<ClusterData> clusterdata) {

		// build distance matrix
		final double[][] distanceMatrix = BuildMatrix.buildMatrix(clusterdata);

		int cluster1 = 0;
		int cluster2 = 0;
		final Component[] c = new Component[clusterdata.size()];
		final List<Clazz> clazz = new ArrayList<Clazz>();

		for (int i = 0; i < clusterdata.size(); i++) {
			clazz.add(clusterdata.get(i).clazz);
			c[i] = new Component();
			c[i].name = clusterdata.get(i).name;
			c[i].parentComponent = clusterdata.get(i).clazz.getParent();
			c[i].belongingApplication = clusterdata.get(i).clazz.getParent().belongingApplication;
			c[i].fullQualifiedName = clusterdata.get(i).clazz.getParent().fullQualifiedName + "."
					+ clusterdata.get(i).name;
			c[i].synthetic = true;
			c[i].setClazzes(clazz);
			clazz.clear();
		}

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

			final List<Component> components = new ArrayList<Component>();
			components.add(c[cluster1]);
			components.add(c[cluster2]);
			final String firstname = c[cluster1].name;
			final String secondname = c[cluster2].name;

			c[cluster1] = new Component();
			c[cluster1].name = firstname + "." + secondname;
			c[cluster1].fullQualifiedName = clusterdata.get(cluster1).clazz.getParent().fullQualifiedName
					+ c[cluster1].name;
			c[cluster1].synthetic = true;
			c[cluster1].setChildren(components);
			components.clear();
		}

		final Component test = new Component();
		final List<Clazz> blubb = new ArrayList<Clazz>();
		for (int i = 0; i < clusterdata.size(); i++) {
			blubb.add(clusterdata.get(i).clazz);
		}
		test.name = "test";
		test.parentComponent = clusterdata.get(1).clazz.getParent();
		test.belongingApplication = clusterdata.get(1).clazz.getParent().belongingApplication;
		test.fullQualifiedName = "parenttest";
		test.synthetic = true;
		test.setClazzes(blubb);
		return test;
		// return c[cluster1];
	}
}
