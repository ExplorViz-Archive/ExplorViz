package explorviz.plugin_client.capacitymanagement.configuration;

import java.io.*;
import java.util.*;

import explorviz.plugin_server.capacitymanagement.execution.ExecutionAction;
import explorviz.plugin_server.capacitymanagement.execution.NodeStartAction;
import explorviz.plugin_server.capacitymanagement.loadbalancer.ScalingGroup;
import explorviz.shared.model.Application;
import explorviz.shared.model.NodeGroup;

public class InitialSetupReader {

	private static List<ScalingGroup> scalingGroups;
	private static ArrayList<ExecutionAction> nodesToStart;

	// TODO: throws specified Exception?
	public static ArrayList<ExecutionAction> readInitialSetup(final String filename)
			throws FileNotFoundException, IOException, InvalidConfigurationException {
		final Properties settings = new Properties();
		settings.load(new FileInputStream(filename));

		final int scalingGroupCount = Integer.parseInt(settings.getProperty("scalingGroupsCount"));
		scalingGroups = new ArrayList<ScalingGroup>();
		for (int i = 1; i <= scalingGroupCount; i++) {
			scalingGroups.add(getScalingGroupFromConfig(i, settings));
		}

		final int nodeCount = Integer.parseInt(settings.getProperty("nodesCount"));
		nodesToStart = new ArrayList<ExecutionAction>();
		for (int i = 1; i <= nodeCount; i++) {
			NodeStartAction newNodeAction = getNodeFromConfig(i, settings);
			if (newNodeAction != null) {
				nodesToStart.add(newNodeAction);
			}
		}

		return nodesToStart;
	}

	private static NodeStartAction getNodeFromConfig(int index, Properties settings)
			throws InvalidConfigurationException {
		final String node = "node" + index;

		final String hostname = settings.getProperty(node + "Hostname");
		final String flavor = settings.getProperty(node + "Flavor");
		final String image = settings.getProperty(node + "Image");
		// final String loadReceiver = settings.getProperty(scalingGroup +
		// "LoadReceiver");
		final boolean enabled = Boolean.parseBoolean(settings.getProperty(node + "Enabled"));
		if (enabled == false) {
			return null;
		}

		final int appCount = Integer.parseInt(settings.getProperty(node + "ApplicationCount"));
		List<Application> apps = new ArrayList<Application>();
		for (int i = 1; i <= appCount; i++) {
			final String scalingGroupName = settings.getProperty(node + "Application" + i
					+ "Scalinggroup");
			Application app = new Application();
			ScalingGroup sg = getScalingGroupByName(scalingGroupName);
			if (sg == null) {
				throw new InvalidConfigurationException("ScalingGroup with name "
						+ scalingGroupName + " is undefined!");
			}

			app.setDummyScalinggroupName("sg");
			final String name = settings.getProperty(node + "Application" + i + "Name");
			app.setName(name);
			apps.add(app);
		}

		NodeGroup parent = new NodeGroup();
		parent.setName("DefaultParent for " + hostname);

		return new NodeStartAction(hostname, flavor, image, apps, parent);
	}

	private static ScalingGroup getScalingGroupByName(String scalingGroupName) {
		for (ScalingGroup sg : scalingGroups) {
			if (sg.getName().equals(scalingGroupName)) {
				return sg;
			}
		}
		return null;
	}

	private static ScalingGroup getScalingGroupFromConfig(final int index, final Properties settings)
			throws InvalidConfigurationException {
		final String scalingGroup = "scalingGroup" + index;

		final String name = settings.getProperty(scalingGroup + "Name");

		if (getScalingGroupByName(name) != null) {
			throw new InvalidConfigurationException("ScalingGroup with name " + name
					+ " is multiple defined!");
		}
		final String applicationFolder = settings.getProperty(scalingGroup + "ApplicationFolder");
		final String startApplicationScript = settings.getProperty(scalingGroup
				+ "StartApplicationScript");

		final int waitTimeForApplicationStartInMillis = Integer.parseInt(settings
				.getProperty(scalingGroup + "WaitTimeForApplicationStartInMillis"));

		// final String dynamicScalingGroup = settings.getProperty(scalingGroup
		// + "DynamicScalingGroup");

		ScalingGroup newScalingGroup = new ScalingGroup(name, applicationFolder,
				startApplicationScript, waitTimeForApplicationStartInMillis, (String) null,
				(String) null);

		return newScalingGroup;
	}
}
