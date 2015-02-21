package explorviz.plugin_server.capacitymanagement.loadbalancer;

import java.util.ArrayList;
import java.util.List;

import explorviz.shared.model.Application;

/**
 *
 * Group of applications of the same type. <br>
 * The LoadBalancerFacade is informed about Scaling groups so that a load
 * balancer can manage the requests.
 *
 */
public class ScalingGroup {
	private final String name;
	private final String applicationFolder;
	private final String startApplicationScript;
	private final int waitTimeForApplicationStartInMillis;

	private final List<Application> apps = new ArrayList<Application>();

	private boolean lockedUntilExecutionActionFinished = false;

	// TODO: jkr, jek: diese beiden (Konzepte) behalten?
	private final String dynamicScalingGroup;
	private String loadReceiver;

	public ScalingGroup(final String name, final String applicationFolder,
			final String startApplicationScript, final int waitTimeForApplicationStartInMillis,
			final String loadReceiver, final String dynamicScalingGroup) {
		this.name = name;
		this.applicationFolder = applicationFolder;
		this.startApplicationScript = startApplicationScript;
		this.waitTimeForApplicationStartInMillis = waitTimeForApplicationStartInMillis;
		this.loadReceiver = loadReceiver;
		this.dynamicScalingGroup = dynamicScalingGroup;

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

	public void setLoadReceiver(final String loadReceiver) {
		this.loadReceiver = loadReceiver;
	}

	public String getDynamicScalingGroup() {
		return dynamicScalingGroup;
	}

	public boolean isLockedUntilExecutionActionFinished() {
		return lockedUntilExecutionActionFinished;
	}

	public void setLockedUntilExecutionActionFinished(
			final boolean lockedUntilExectuionActionFinished) {
		lockedUntilExecutionActionFinished = lockedUntilExectuionActionFinished;
	}

	public void addApplication(final Application app) {
		synchronized (apps) {
			if (getApplicationById(app.getId()) == null) {
				LoadBalancersFacade.addApplication(app.getId(), app.getParent().getIpAddress(),
						name);

				app.setScalinggroup(this);

				apps.add(app);
			}
		}

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
