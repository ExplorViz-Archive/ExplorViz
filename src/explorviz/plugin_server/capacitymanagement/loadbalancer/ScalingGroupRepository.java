package explorviz.plugin_server.capacitymanagement.loadbalancer;

import java.util.ArrayList;
import java.util.List;

import explorviz.shared.model.Application;

/**
 *
 * Repository with a list of all currently existing scalingGroups. <br>
 * Inspired by and partly taken from capacity-manager-project.
 *
 */
public class ScalingGroupRepository {
	private final List<ScalingGroup> scalingGroups = new ArrayList<ScalingGroup>();

	public ScalingGroupRepository() {

	}

	public void addScalingGroup(final String name, final String applicationFolder,
			final String startApplicationScript, final int waitTimeForApplicationActionInMillis) {
		synchronized (scalingGroups) {
			scalingGroups.add(new ScalingGroup(name, applicationFolder, startApplicationScript,
					waitTimeForApplicationActionInMillis));
		}
	}

	public void addScalingGroup(ScalingGroup group) {
		synchronized (scalingGroups) {
			scalingGroups.add(group);
		}
	}

	public void removeScalingGroup(final String name) {
		synchronized (scalingGroups) {
			scalingGroups.remove(getScalingGroupByName(name));
		}
	}

	public ScalingGroup getScalingGroup(final int index) {
		synchronized (scalingGroups) {
			return scalingGroups.get(index);
		}
	}

	public int getScalingGroupsCount() {
		synchronized (scalingGroups) {
			return scalingGroups.size();
		}
	}

	public int getAllAppsCount() {
		synchronized (scalingGroups) {
			int result = 0;
			for (final ScalingGroup scalingGroup : scalingGroups) {
				result += scalingGroup.getAppCount();
			}
			return result;
		}
	}

	public ScalingGroup getScalingGroupByName(final String group) {
		synchronized (scalingGroups) {
			for (final ScalingGroup scalingGroup : scalingGroups) {
				if (scalingGroup.getName().equalsIgnoreCase(group)) {
					return scalingGroup;
				}
			}
			// TODO:jkr/jek: what to do if not existent? Always called in
			// try/catch-Block so better throw Exception?
			return null;
		}
	}

	public Application getAppByID(final int id) {
		synchronized (scalingGroups) {
			for (final ScalingGroup scalingGroup : scalingGroups) {
				for (int i = 0; i < scalingGroup.getAppCount(); i++) {
					if (id == scalingGroup.getApplication(i).getId()) {
						return scalingGroup.getApplication(i);
					}
				}
			}
			return null;
		}
	}
}
