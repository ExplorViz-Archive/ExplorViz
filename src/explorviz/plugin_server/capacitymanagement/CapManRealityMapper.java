package explorviz.plugin_server.capacitymanagement;

import java.util.*;

import explorviz.shared.model.Application;

/**
 * Maps the applications in the cloud which are created during the initial setup
 * of CapMan with the application from the landscape which the worker creates.
 *
 * @author jek, jkr
 *
 */
public class CapManRealityMapper {
	private static HashMap<String, ArrayList<Application>> nodemap = new HashMap<String, ArrayList<Application>>();

	/**
	 * Adds an entry for another Node identified by its IPAddress. Creates an
	 * empty List of Applications.
	 *
	 * @param ipAddress
	 *            IpAddress of the Node which Applications have to be mapped.
	 */
	public static void addNode(String ipAddress) {

		synchronized (nodemap) {
			nodemap.put(ipAddress, new ArrayList<Application>());
		}
	}

	/**
	 * Removes entry for the node identified by its IpAdress.
	 *
	 * @param ipAddress
	 *            IpAddress of node.
	 */
	public static void removeNode(String ipAddress) {
		synchronized (nodemap) {
			nodemap.remove(ipAddress);
		}
	}

	/**
	 * Adds a new application to the entry of the node given by its ipAdress.
	 *
	 * @param ipAddress
	 *            IpAddress of node.
	 * @param app
	 *            Application to add.
	 */
	public static void addApplicationtoNode(String ipAddress, Application app) {
		synchronized (nodemap) {
			ArrayList<Application> appList = nodemap.get(ipAddress);
			appList.add(app);
			nodemap.put(ipAddress, appList);
		}
	}

	/**
	 * Removes single application from node given by its ipAdress.
	 *
	 * @param ipAddress
	 *            IpAddress of node.
	 * @param appName
	 *            Name of application which to remove.
	 */
	public static void removeApplicationFromNode(String ipAddress, String appName) {
		synchronized (nodemap) {
			ArrayList<Application> appList = nodemap.get(ipAddress);
			int indexToRemove = -1;
			for (Application app : appList) {
				if (app.getName().equals(appName)) {
					indexToRemove = appList.indexOf(app);
					break;
				}
			}
			if ((indexToRemove >= 0) && (indexToRemove < appList.size())) {
				appList.remove(indexToRemove);
			}
			nodemap.put(ipAddress, appList);
		}
	}

	/**
	 * Returns the list of all applications for the node given by its IP.
	 *
	 * @param ipAddress
	 *            IpAddress of node.
	 * @return List of application.
	 */
	public static List<Application> getApplicationsFromNode(String ipAddress) {
		synchronized (nodemap) {
			return nodemap.get(ipAddress);
		}
	}

	/**
	 * Returns an application identified by its name from a node given by its
	 * ipAdress. Returns null if application with name does not exist.
	 *
	 * @param ipAddress
	 *            IpAddress of the node
	 * @param appName
	 *            name of the application
	 * @return Application.
	 */
	public static Application getApplication(String ipAddress, String appName) {
		Application application = null;
		synchronized (nodemap) {
			ArrayList<Application> appList = nodemap.get(ipAddress);
			for (Application app : appList) {
				if (app.getName().equals(appName)) {
					application = app;
					break;
				}
			}
			return application;
		}
	}

	/**
	 * Given application will be added to the list of applications of node with
	 * given ipAddress. If application with the same name already exists on this
	 * node , it is removed before.
	 *
	 * @param ipAddress
	 *            ipAddress of node.
	 * @param application
	 *            Application to set eventually with different attributs.
	 */
	public static void setApplication(String ipAddress, Application application) {
		synchronized (nodemap) {
			ArrayList<Application> appList = nodemap.get(ipAddress);
			int indexToRemove = -1;
			for (Application app : appList) {
				if (app.getName().equals(application.getName())) {
					indexToRemove = appList.indexOf(app);
					break;
				}
			}
			if ((indexToRemove >= 0) && (indexToRemove < appList.size())) {
				appList.remove(indexToRemove);
			}
			appList.add(application);
			nodemap.put(ipAddress, appList);
		}
	}

}
