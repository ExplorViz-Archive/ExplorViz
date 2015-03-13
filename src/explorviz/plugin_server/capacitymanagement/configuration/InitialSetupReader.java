package explorviz.plugin_server.capacitymanagement.configuration;

import java.io.*;
import java.util.*;

import explorviz.plugin_server.capacitymanagement.execution.ExecutionAction;
import explorviz.plugin_server.capacitymanagement.execution.NodeStartAction;
import explorviz.plugin_server.capacitymanagement.loadbalancer.ScalingGroup;
import explorviz.plugin_server.capacitymanagement.loadbalancer.ScalingGroupRepository;
import explorviz.shared.model.Application;
import explorviz.shared.model.NodeGroup;

/**
 * Reads the setup of nodes and applications. <br>
 * Inspired by capacity-manager-project.
 */
public class InitialSetupReader {

	private static ScalingGroupRepository repository = new ScalingGroupRepository();
	private static ArrayList<ExecutionAction> nodesToStart;

	// ensures unique hostnames
	private static ArrayList<String> hostnames = new ArrayList<String>();

	// absolute path on ExplorViz-Server with the application-folders
	static String appsFolder;

	/**
	 * Reads the initial setup from the configuration and returns a list of
	 * {@link NodeStartAction}
	 *
	 * @param filename
	 *            setup configuration
	 * @return list of NodeStartActions
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws InvalidConfigurationException
	 */
	public static ArrayList<ExecutionAction> readInitialSetup(final String filename)
			throws FileNotFoundException, IOException, InvalidConfigurationException {
		final Properties settings = new Properties();
		settings.load(new FileInputStream(filename));

		final int scalingGroupCount = Integer.parseInt(settings.getProperty("scalingGroupsCount"));
		for (int i = 1; i <= scalingGroupCount; i++) {
			ScalingGroup sg = getScalingGroupFromConfig(i, settings);
			repository.addScalingGroup(sg);
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
		if (hostnames.contains(hostname)) {
			throw new InvalidConfigurationException("Hostname " + hostname + " is not unique!");
		} else {
			hostnames.add(hostname);
		}
		final String flavor = settings.getProperty(node + "Flavor");
		final String image = settings.getProperty(node + "Image");

		final boolean enabled = Boolean.parseBoolean(settings.getProperty(node + "Enabled"));
		if (enabled == false) {
			return null;
		}

		final int appCount = Integer.parseInt(settings.getProperty(node + "ApplicationCount"));
		List<Application> apps = new ArrayList<Application>();
		if (appCount > 0) {
			List<String> appNames = new ArrayList<String>();
			for (int i = 1; i <= appCount; i++) {
				Application a = createApplicationFromConfig(node, i, settings);
				if (appNames.contains(a.getName())) {
					throw new InvalidConfigurationException("Node " + hostname
							+ " contains more than one application with name " + a.getName());
				} else {
					apps.add(createApplicationFromConfig(node, i, settings));
					appNames.add(a.getName());
				}
			}
		}
		NodeGroup parent = new NodeGroup();
		parent.setName("DefaultParent for " + hostname);

		return new NodeStartAction(hostname, flavor, image, apps, parent);
	}

	private static Application createApplicationFromConfig(String node, int i, Properties settings)
			throws InvalidConfigurationException {
		final String scalingGroupName = settings.getProperty(node + "Application" + i
				+ "Scalinggroup");
		Application app = new Application();
		ScalingGroup sg = repository.getScalingGroupByName(scalingGroupName);
		if (sg == null) {
			throw new InvalidConfigurationException("ScalingGroup with name " + scalingGroupName
					+ " is undefined!");
		}

		app.setScalinggroupName(scalingGroupName);
		final String name = settings.getProperty(node + "Application" + i + "Name");
		app.setName(name);
		app.getArguments().add(name);
		final String dependendOn = settings.getProperty(node + "Application" + i + "DependendOnIP");
		if ((dependendOn != null) && !dependendOn.equals("")) {
			String[] dependents = dependendOn.split(",");
			app.setDependendOn(Arrays.asList(dependents));
		}
		return app;
	}

	private static ScalingGroup getScalingGroupFromConfig(final int index, final Properties settings)
			throws InvalidConfigurationException {
		final String scalingGroup = "scalingGroup" + index;

		final String name = settings.getProperty(scalingGroup + "Name");

		if (repository.getScalingGroupByName(name) != null) {
			throw new InvalidConfigurationException("ScalingGroup with name " + name
					+ " is multiple defined!");
		}
		appsFolder = settings.getProperty("applicationFolder");
		String applicationFolder = settings.getProperty(scalingGroup + "ApplicationFolder");
		if (!applicationFolder.endsWith("/")) {
			applicationFolder += "/";
		}

		final int waitTimeForApplicationActionInMillis = Integer.parseInt(settings
				.getProperty(scalingGroup + "WaitTimeForApplicationStartInMillis"));

		ScalingGroup newScalingGroup = new ScalingGroup(name, applicationFolder,
				waitTimeForApplicationActionInMillis);

		return newScalingGroup;
	}

	/**
	 * Repository is only "filled" if readInitialSetup() was executed.
	 *
	 * @return repository
	 */
	public static ScalingGroupRepository getScalingGroupRepository() {
		return repository;
	}

	public static String getApplicationsFolderPath() {
		return appsFolder;
	}
}
