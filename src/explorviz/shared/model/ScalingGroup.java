package explorviz.shared.model;

import java.util.ArrayList;
import java.util.List;

import explorviz.plugin_server.capacitymanagement.loadbalancer.LoadBalancersFacade;

public class ScalingGroup {
	private final String name;
	private final String applicationFolder;
	private final String startApplicationScript;
	private final int waitTimeForApplicationStartInMillis;

	// private final String flavor;
	// private final String image;

	private final NodeGroup parent;
	private final List<Application> apps = new ArrayList<Application>();

	private boolean lockedUntilExecutionActionFinished = false;
	private final boolean enabled;

	// TODO: jkr, jek: diese beiden (Konzepte) behalten?
	private final String dynamicScalingGroup;
	private String loadReceiver;

	public ScalingGroup(final String name, final String applicationFolder,
			final String startApplicationScript, final int waitTimeForApplicationStartInMillis,
			final String loadReceiver, final String dynamicScalingGroup, final boolean enabled,
			final NodeGroup parent) {
		this.name = name;
		this.applicationFolder = applicationFolder;
		this.startApplicationScript = startApplicationScript;
		this.waitTimeForApplicationStartInMillis = waitTimeForApplicationStartInMillis;
		this.loadReceiver = loadReceiver;
		this.dynamicScalingGroup = dynamicScalingGroup;
		this.enabled = enabled;
		this.parent = parent;
	}

	public String getName() {
		return name;
	}

	public String getApplicationFolder() {
		return applicationFolder;
	}

	public String getStartApplicationScript() {
		return startApplicationScript;
	}

	public int getWaitTimeForApplicationStartInMillis() {
		return waitTimeForApplicationStartInMillis;
	}

	public String getLoadReceiver() {
		return loadReceiver;
	}

	public NodeGroup getParent() {
		return parent;
	}

	public void setLoadReceiver(final String loadReceiver) {
		this.loadReceiver = loadReceiver;
	}

	public String getDynamicScalingGroup() {
		return dynamicScalingGroup;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public boolean isLockedUntilExecutionActionFinished() {
		return lockedUntilExecutionActionFinished;
	}

	public void setLockedUntilExecutionActionFinished(
			final boolean lockedUntilExectuionActionFinished) {
		lockedUntilExecutionActionFinished = lockedUntilExectuionActionFinished;
	}

	public boolean addApplication(final Application app) {
		synchronized (apps) {
			if (getApplicationById(app.getId()) == null) {
				LoadBalancersFacade.addApplication(app.getId(), app.getParent().getIpAddress(),
						name);
				return apps.add(app);
			}
		}
		return false;
	}

	public boolean removeApplication(final Application app) {
		synchronized (apps) {
			if (apps.size() > 1) {
				// ensure right object
				final Application appById = getApplicationById(app.getId());
				LoadBalancersFacade.removeApplication(app.getId(), app.getParent().getIpAddress(),
						name);
				return apps.remove(appById);
			}
		}
		return false;
	}

	public Application getApplication(final int index) {
		synchronized (apps) {
			return apps.get(index);
		}
	}

	public int getAppCount() {
		synchronized (apps) {
			return apps.size();
		}
	}

	// public int getActiveNodesCount() {
	// synchronized (nodes) {
	// int result = 0;
	// for (final Node node : nodes) {
	// if (node.isEnabled()) {
	// result++;
	// }
	// }
	// return result;
	// }
	// }

	public Application getApplicationById(final int id) {
		synchronized (apps) {
			for (final Application app : apps) {
				if (id == app.getId()) {
					return app;
				}
			}
		}
		return null;
	}
}
