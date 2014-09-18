package explorviz.visualization.clustering;

import explorviz.shared.model.Clazz;

public class ClusterData {

	String name;
	int instances;
	int methods;
	Clazz clazz;

	public ClusterData() {
		name = "error";
		instances = 0;
		methods = 0;
	}

	public ClusterData(final Clazz clazz) {
		name = clazz.getName();
		instances = clazz.getInstanceCount();
		methods = 0;// TODO Florian fragen, wie genau ich die calledMethods
					// kriege (addition von incoming communications und outgoing
					// communications????)
		this.clazz = clazz;
	}

}
