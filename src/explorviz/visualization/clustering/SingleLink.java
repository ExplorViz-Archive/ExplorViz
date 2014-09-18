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
		final String[] d = new String[clusterdata.size()];
		final Component[] c = new Component[clusterdata.size()];

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
			System.out.println("classes " + cluster1 + " and " + cluster2
					+ " are closest at distance " + minValue + " and will be merged");

			if ((d[cluster1] == "component") && (d[cluster2] == "component")) {
				final List<Component> components = new ArrayList<Component>();
				components.add(c[cluster1]);
				components.add(c[cluster2]);
				c[cluster1] = new Component();
				c[cluster1].setChildren(components);

			} else if (d[cluster1] == "component") {
				final List<Component> components = new ArrayList<Component>();
				final List<Clazz> clazzes = new ArrayList<Clazz>();
				components.add(c[cluster1]);
				clazzes.add(clusterdata.get(cluster2).clazz);
				c[cluster1] = new Component();
				c[cluster1].setChildren(components);
				c[cluster1].setClazzes(clazzes);

			} else if (d[cluster2] == "component") {
				final List<Component> components = new ArrayList<Component>();
				final List<Clazz> clazzes = new ArrayList<Clazz>();
				components.add(c[cluster2]);
				clazzes.add(clusterdata.get(cluster1).clazz);
				c[cluster1] = new Component();
				c[cluster1].setChildren(components);
				c[cluster1].setClazzes(clazzes);

			} else {
				d[cluster1] = "component";
				final List<Clazz> clazzes = new ArrayList<Clazz>();
				clazzes.add(clusterdata.get(cluster1).clazz);
				clazzes.add(clusterdata.get(cluster2).clazz);
				c[cluster1] = new Component();
				c[cluster1].setClazzes(clazzes);
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

		return c[cluster1];
	}

	// public static void main(final String[] args) {
	//
	// final List<ClusterData> clusterdata = new ArrayList<ClusterData>();
	// final ClusterData class0 = new ClusterData();
	// final ClusterData class1 = new ClusterData();
	// final ClusterData class2 = new ClusterData();
	// final ClusterData class3 = new ClusterData();
	// final ClusterData class4 = new ClusterData();
	// final ClusterData class5 = new ClusterData();
	// final ClusterData class6 = new ClusterData();
	// final ClusterData class7 = new ClusterData();
	// final ClusterData class8 = new ClusterData();
	// final ClusterData class9 = new ClusterData();
	// final ClusterData class10 = new ClusterData();
	//
	// class0.name = "ProbeController";
	// class1.name = "JMXController";
	// class2.name = "AbstractController";
	// class3.name = "WriterController";
	// class4.name = "MonitoringController$[Thread]1";
	// class5.name = "MonitoringController";
	// class6.name = "JMXController$JMXImplementation";
	// class7.name = "RegistryController";
	// class8.name = "SamplingController";
	// class9.name = "StateController";
	// class10.name = "TimeSourceController";
	//
	// class0.methods = 6;
	// class1.methods = 5;
	// class2.methods = 1;
	// class3.methods = 7;
	// class4.methods = 1;
	// class5.methods = 14;
	// class6.methods = 1;
	// class7.methods = 6;
	// class8.methods = 5;
	// class9.methods = 12;
	// class10.methods = 6;
	//
	// class0.instances = 1;
	// class1.instances = 1;
	// class2.instances = 1;
	// class3.instances = 1;
	// class4.instances = 1;
	// class5.instances = 2;
	// class6.instances = 2;
	// class7.instances = 1;
	// class8.instances = 1;
	// class9.instances = 1;
	// class10.instances = 1;
	//
	// clusterdata.add(class0);
	// clusterdata.add(class1);
	// clusterdata.add(class2);
	// clusterdata.add(class3);
	// clusterdata.add(class4);
	// clusterdata.add(class5);
	// clusterdata.add(class6);
	// clusterdata.add(class7);
	// clusterdata.add(class8);
	// clusterdata.add(class9);
	// clusterdata.add(class10);
	//
	// doSingleLink(clusterdata);
	// }
}
