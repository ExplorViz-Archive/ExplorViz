package explorviz.plugin.capacitymanagement.node.repository;

import java.util.ArrayList;
import java.util.List;

import explorviz.plugin.capacitymanagement.configuration.Configuration;

/**
 * @author jgi, dtj Arraylist containing all ScalingGroups.
 */
public class ScalingGroupRepository {
    private final List<ScalingGroup> scalingGroups = new ArrayList<ScalingGroup>();
    private final int limitForCPUUtilizationHistory;

    public ScalingGroupRepository(final Configuration configuration) {
        limitForCPUUtilizationHistory = configuration
                .getCpuUtilizationHistoryLimit();
    }

    /**
     * @param name
     *            Name of ScalingGroup.
     * @param applicationFolder
     *            Folder containing application.
     * @param startApplicationScript
     *            Start application-scripts.
     * @param waitTimeForApplicationStartInMillis
     *            Wait time in milliseconds for the application to start.
     * @param flavor
     *            explorviz.worker?
     * @param image
     *            Symbol shown in Explorviz-perspective.
     * @param templateHostname
     *            Hostname of template.
     * @param loadReceiver
     *            ?
     * @param dynamicScalingGroup
     *            ?
     * @param enabled
     *            Is the ScalingGroup running?
     */
    public void addScalingGroup(final String name,
            final String applicationFolder,
            final String startApplicationScript,
            final int waitTimeForApplicationStartInMillis, final String flavor,
            final String image, final String templateHostname,
            final String loadReceiver, final String dynamicScalingGroup,
            final boolean enabled) {
        synchronized (scalingGroups) {
            scalingGroups.add(new ScalingGroup(name, applicationFolder,
                    startApplicationScript,
                    waitTimeForApplicationStartInMillis, flavor, image,
                    templateHostname, loadReceiver, dynamicScalingGroup,
                    enabled, limitForCPUUtilizationHistory));
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

    public int getAllNodesCount() {
        synchronized (scalingGroups) {
            int result = 0;
            for (final ScalingGroup scalingGroup : scalingGroups) {
                result += scalingGroup.getNodesCount();
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
            return null;
        }
    }

    public Node getNodeByHostname(final String hostname) {
        synchronized (scalingGroups) {
            for (final ScalingGroup scalingGroup : scalingGroups) {
                for (int i = 0; i < scalingGroup.getNodesCount(); i++) {
                    if (hostname.equalsIgnoreCase(scalingGroup.getNode(i)
                            .getHostname())) {
                        return scalingGroup.getNode(i);
                    }
                }
            }
            return null;
        }
    }
}
