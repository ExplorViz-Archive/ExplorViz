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
	 * Adds an entry for another Node identified by its IPAdress. Creates an
	 * empty List of Applications.
	 *
	 * @param ipAdress
	 *            IpAdress of the Node which Applications have to be mapped.
	 */
	public static void addNode(String ipAdress) {

		synchronized (nodemap) {
			nodemap.put(ipAdress, new ArrayList<Application>());
		}
	}

	/**
	 * Removes entry for the node identified by its IpAdress.
	 *
	 * @param ipAdress
	 *            IpAdress of node.
	 */
	public static void removeNode(String ipAdress) {
		synchronized (nodemap) {
			nodemap.remove(ipAdress);
		}
	}

	/**
	 * Adds a new application to the entry of the node given by its ipAdress.
	 *
	 * @param ipAdress
	 *            IpAdress of node.
	 * @param app
	 *            Application to add.
	 */
	public static void addApplicationtoNode(String ipAdress, Application app) {
		synchronized (nodemap) {
			ArrayList<Application> appList = nodemap.get(ipAdress);
			appList.add(app);
			nodemap.put(ipAdress, appList);
		}
	}

	/**
	 * Removes single application from node given by its ipAdress.
	 *
	 * @param ipAdress
	 *            IpAdress of node.
	 * @param appName
	 *            Name of application which to remove.
	 */
	public static void removeApplicationFromNode(String ipAdress, String appName) {
		synchronized (nodemap) {
			ArrayList<Application> appList = nodemap.get(ipAdress);
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
			nodemap.put(ipAdress, appList);
		}
	}

	/**
	 * Returns the list of all applications for the node given by its IP.
	 *
	 * @param ipAdress
	 *            IpAdress of node.
	 * @return List of application.
	 */
	public static List<Application> getApplicationsFromNode(String ipAdress) {
		synchronized (nodemap) {
			return nodemap.get(ipAdress);
		}
	}

	/**
	 * Returns an application identified by its name from a node given by its
	 * ipAdress.
	 *
	 * @param ipAdress
	 *            IpAdress of the node
	 * @param appName
	 *            name of the application
	 * @return Application.
	 * @throws Exception
	 *             If no application with given name.
	 */
	public static Application getApplication(String ipAdress, String appName) throws Exception {
		synchronized (nodemap) {
			ArrayList<Application> appList = nodemap.get(ipAdress);
			for (Application app : appList) {
				if (app.getName().equals(appName)) {
					return app;
				}
			}
			throw new Exception("No App with name " + appName + " on Node with IP" + ipAdress);
		}
	}

	/**
	 * Given application will be added to the list of applications of node with
	 * given ipAdress. If application with the same name already exists on this
	 * node , it is removed before.
	 *
	 * @param ipAdress
	 *            ipAdress of node.
	 * @param application
	 *            Application to set eventually with different attributs.
	 */
	public static void setApplication(String ipAdress, Application application) {
		synchronized (nodemap) {
			ArrayList<Application> appList = nodemap.get(ipAdress);
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
			nodemap.put(ipAdress, appList);
		}
	}

}
