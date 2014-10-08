package explorviz.visualization.clustering;

import explorviz.shared.model.Clazz;
import explorviz.visualization.interaction.ApplicationInteraction;

/**
 *
 * @author Mirco Barzel
 *
 */
public class ClusterData {
	private final String name;
	private final int instances;
	private final int methods;

	private final Clazz clazz;

	public ClusterData() {
		name = "error";
		instances = 0;
		methods = 0;
		clazz = null;
	}

	public ClusterData(final Clazz clazz) {
		name = clazz.getName();
		instances = clazz.getInstanceCount();
		methods = ApplicationInteraction.getCalledMethods(clazz);
		this.clazz = clazz;
	}

	public String getName() {
		return name;
	}

	public int getInstances() {
		return instances;
	}

	public int getMethods() {
		return methods;
	}

	public Clazz getClazz() {
		return clazz;
	}
}
