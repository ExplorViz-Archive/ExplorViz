package explorviz.shared.model;

import java.util.List;

public class TestScalingGroupBuilder {

	/**
	 * Creates a {@link ScalingGroup} for test purposes.
	 *
	 * @param name
	 *            name of scalingGroup
	 * @param applications
	 *            list of applications which will belong to the scalinggroup
	 * @return newly created scalinggroup
	 */
	public static ScalingGroup createStandardScalingGroup(final String name,
			final List<Application> applications) {
		ScalingGroup scalingGroup = new ScalingGroup(name, "testfolder", "test-start-script", 100,
				"test-load-receiver", "", true, null);

		for (Application app : applications) {
			scalingGroup.addApplication(app);
			app.setScalinggroup(scalingGroup);
		}

		return scalingGroup;
	}
}