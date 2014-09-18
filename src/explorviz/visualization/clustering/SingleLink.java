package explorviz.visualization.clustering;

import java.util.ArrayList;
import java.util.List;

import explorviz.shared.model.Clazz;
import explorviz.shared.model.Component;
import explorviz.visualization.renderer.ColorDefinitions;

public class SingleLink {

	public static Component doSingleLink(final List<ClusterData> clusterdata) {

		// build distance matrix
		final double[][] distanceMatrix = BuildMatrix.buildMatrix(clusterdata);

		int cluster1 = 0;
		int cluster2 = 0;
		final Component[] c = new Component[clusterdata.size()];
		final List<Clazz> clazz = new ArrayList<Clazz>();

		// put every class in cluster(component) in first clustering step
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

			System.out.println("classes " + cluster1 + " and " + cluster2
					+ " are closest at distance: " + minValue + " -> merged");

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

			// create new component out of 2
			final List<Component> components = new ArrayList<Component>();
			components.add(c[cluster1]);
			components.add(c[cluster2]);
			final String firstname = c[cluster1].name;
			final String secondname = c[cluster2].name;

			c[cluster1].getChildren().clear();
			c[cluster1].getClazzes().clear();
			c[cluster1].name = firstname + "." + secondname;
			c[cluster1].fullQualifiedName = clusterdata.get(cluster1).clazz.getParent().fullQualifiedName
					+ c[cluster1].name;
			c[cluster1].synthetic = true;
			c[cluster1].setChildren(components);
			if ((cluster1 % 2) == 1) {
				c[cluster1].setColor(ColorDefinitions.componentSyntheticColor);
			} else {
				c[cluster1].setColor(ColorDefinitions.componentSyntheticSecondColor);
			}
			components.clear();
		}

		// deserialize(c[cluster1]);
		return c[cluster1];
	}

	// public static final void deserialize(final Component component) {
	// final String name = component.name;
	// final String children = component.getChildren().toString();
	// final String clazzes = component.getClazzes().toString();
	// final String fullqualifiedName = component.fullQualifiedName;
	// final String belongingapp = component.belongingApplication.getName();
	//
	// System.out.println("componentname is: " + name);
	// System.out.println("componentchildren are: " + children);
	// System.out.println("componentclazzes are: " + clazzes);
	// System.out.println("componentFQname is: " + fullqualifiedName);
	// System.out.println("component belonging app is: " + belongingapp);
	// }

	// public static void main(final String[] args) {
	// final List<ClusterData> clazzes = new ArrayList<ClusterData>();
	//
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
	// clazzes.add(class0);
	// clazzes.add(class1);
	// clazzes.add(class2);
	// clazzes.add(class3);
	// clazzes.add(class4);
	// clazzes.add(class5);
	// clazzes.add(class6);
	// clazzes.add(class7);
	// clazzes.add(class8);
	// clazzes.add(class9);
	// clazzes.add(class10);
	//
	// doSingleLink(clazzes);
	// }
}
