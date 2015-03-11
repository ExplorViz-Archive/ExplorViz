package explorviz.plugin_server.capacitymanagement.loadbalancer;

import java.util.ArrayList;
import java.util.List;

import explorviz.shared.model.Application;

/**
 *
 * Group of applications of the same type. <br>
 * The LoadBalancerFacade is informed about Scaling groups so that a load
 * balancer can manage the requests. <br>
 * The name of the ScalingGroup has to be unique. Partly taken from the
 * capacity-manager-project.
 *
 */
public class ScalingGroup {
	// private static final Logger LOGGER =
	// LoggerFactory.getLogger(ScalingGroup.class);
	private final String name; // needs to be unique
	private final String applicationFolder;
	private final String startApplicationScript;
	private final int waitTimeForApplicationActionInMillis;

	private final List<Application> apps = new ArrayList<Application>();

	private boolean lockedUntilExecutionActionFinished = false;

	public ScalingGroup(final String name, final String applicationFolder,
			final String startApplicationScript, final int waitTimeForApplicationStartInMillis) {
		this.name = name; // name is unique which is ensured in the
		// InitialSetupReader where the ScalingGroups are
		// defined.
		this.applicationFolder = applicationFolder;
		this.startApplicationScript = startApplicationScript;
		waitTimeForApplicationActionInMillis = waitTimeForApplicationStartInMillis;
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

	public int getWaitTimeForApplicationActionInMillis() {
		return waitTimeForApplicationActionInMillis;
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

				app.setScalinggroupName(getName());

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
