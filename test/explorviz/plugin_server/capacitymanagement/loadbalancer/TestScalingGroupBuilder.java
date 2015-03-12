package explorviz.plugin_server.capacitymanagement.loadbalancer;

public class TestScalingGroupBuilder {

	public static ScalingGroup createStandardScalingGroup() {
		ScalingGroup sg = new ScalingGroup("Test-Scaling-Group-Name", "TestFolder", 3000);
		return sg;
	}

	public static ScalingGroup createScalingGroup(String name) {
		ScalingGroup sg = new ScalingGroup(name, "TestFolder", 3000);
		return sg;
	}

}
