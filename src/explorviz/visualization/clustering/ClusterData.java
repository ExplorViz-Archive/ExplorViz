package explorviz.visualization.clustering;

import explorviz.shared.model.Clazz;
import explorviz.visualization.interaction.ApplicationInteraction;

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
		methods = ApplicationInteraction.getCalledMethods(clazz);
		this.clazz = clazz;
	}
}
