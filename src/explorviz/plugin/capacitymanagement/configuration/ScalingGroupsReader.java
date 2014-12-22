package explorviz.plugin.capacitymanagement.configuration;

import java.io.FileInputStream;
import java.util.Properties;

import explorviz.plugin.capacitymanagement.node.repository.ScalingGroupRepository;

/**
 * @author jgi, dtj Get ScalingGroups from settingsfile and write into
 *         ScalingGroupsRepository.
 */
public class ScalingGroupsReader {

	public static void readInScalingGroups(final ScalingGroupRepository scalingGroupRepository,
			final String filename) throws Exception {
		final Properties settings = new Properties();
		settings.load(new FileInputStream(filename));

		final int scalingGroupCount = Integer.parseInt(settings.getProperty("scalingGroupsCount"));
		for (int i = 1; i <= scalingGroupCount; i++) {
			getScalingGroupFromConfig(i, settings, scalingGroupRepository);
		}
	}

	private static void getScalingGroupFromConfig(final int index, final Properties settings,
			final ScalingGroupRepository scalingGroupRepository) {
		final String scalingGroup = "scalingGroup" + index;

		final String name = settings.getProperty(scalingGroup + "Name");
		final String applicationFolder = settings.getProperty(scalingGroup + "ApplicationFolder");
		final String startApplicationScript = settings.getProperty(scalingGroup
				+ "StartApplicationScript");
		final int waitTimeForApplicationStartInMillis = Integer.parseInt(settings
				.getProperty(scalingGroup + "WaitTimeForApplicationStartInMillis"));
		final String flavor = settings.getProperty(scalingGroup + "Flavor");
		final String image = settings.getProperty(scalingGroup + "Image");
		final String templateHostname = settings.getProperty(scalingGroup + "TemplateHostname");
		final String loadReceiver = settings.getProperty(scalingGroup + "LoadReceiver");
		final String dynamicScalingGroup = settings.getProperty(scalingGroup
				+ "DynamicScalingGroup");
		final boolean enabled = Boolean
				.parseBoolean(settings.getProperty(scalingGroup + "Enabled"));

		scalingGroupRepository.addScalingGroup(name, applicationFolder, startApplicationScript,
				waitTimeForApplicationStartInMillis, flavor, image, templateHostname, loadReceiver,
				dynamicScalingGroup, enabled);
	}
}
