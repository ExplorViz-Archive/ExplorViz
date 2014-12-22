package explorviz.plugin.capacitymanagement.node.repository;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jgi, dtj Arraylist of Nodes. Abstract object that represents
 *         "ScalingGroups" in the Landscape.
 */
public class ScalingGroup {
    private final String name;
    private final String applicationFolder;
    private final String startApplicationScript;
    private final int waitTimeForApplicationStartInMillis;
    private final String flavor;
    private final String image;

    private final String templateHostname;
    private int hostnameCounter = 1;

    private final int limitForCPUUtilizationHistory;

    private final List<Node> nodes = new ArrayList<Node>();

    private boolean lockedUntilInstanceBootFinished = false;
    private boolean lockedUntilInstanceShutdownFinished = false;
    private final boolean enabled;
    private final String dynamicScalingGroup;
    private String loadReceiver;

    public ScalingGroup(final String name, final String applicationFolder,
            final String startApplicationScript,
            final int waitTimeForApplicationStartInMillis, final String flavor,
            final String image, final String templateHostname,
            final String loadReceiver, final String dynamicScalingGroup,
            final boolean enabled, final int limitForCPUUtilizationHistory) {
        this.name = name;
        this.applicationFolder = applicationFolder;
        this.startApplicationScript = startApplicationScript;
        this.waitTimeForApplicationStartInMillis = waitTimeForApplicationStartInMillis;
        this.flavor = flavor;
        this.image = image;
        this.templateHostname = templateHostname;
        this.loadReceiver = loadReceiver;
        this.dynamicScalingGroup = dynamicScalingGroup;
        this.enabled = enabled;
        this.limitForCPUUtilizationHistory = limitForCPUUtilizationHistory;
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

    public String getFlavor() {
        return flavor;
    }

    public String getImage() {
        return image;
    }

    public String generateNewUniqueHostname() {
        return templateHostname + String.valueOf(hostnameCounter++);
    }

    public String getTemplateHostname() {
        return templateHostname;
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

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isLockedUntilInstanceBootFinished() {
        return lockedUntilInstanceBootFinished;
    }

    public void setLockedUntilInstanceBootFinished(
            final boolean lockedUntilInstanceBootFinished) {
        this.lockedUntilInstanceBootFinished = lockedUntilInstanceBootFinished;
    }

    public boolean isLockedUntilInstanceShutdownFinished() {
        return lockedUntilInstanceShutdownFinished;
    }

    public void setLockedUntilInstanceShutdownFinished(
            final boolean lockedUntilInstanceShutdownFinished) {
        this.lockedUntilInstanceShutdownFinished = lockedUntilInstanceShutdownFinished;
    }

    public boolean addNode(final String privateIp, final String instanceId,
            final String hostname) {
        synchronized (nodes) {
            if (getNodeByHostname(hostname) == null) {
                return nodes.add(new Node(privateIp, instanceId, hostname,
                        limitForCPUUtilizationHistory, this));
            }
        }
        return false;
    }

    public boolean removeNode(final Node node) {
        synchronized (nodes) {
            if (nodes.size() > 1) {
                // ensure right object
                final Node nodeByHostname = getNodeByHostname(node
                        .getHostname());
                return nodes.remove(nodeByHostname);
            }
        }
        return false;
    }

    public Node getNode(final int index) {
        synchronized (nodes) {
            return nodes.get(index);
        }
    }

    public int getNodesCount() {
        synchronized (nodes) {
            return nodes.size();
        }
    }

    public int getActiveNodesCount() {
        synchronized (nodes) {
            int result = 0;
            for (final Node node : nodes) {
                if (node.isEnabled()) {
                    result++;
                }
            }
            return result;
        }
    }

    public Node getNodeByHostname(final String hostname) {
        synchronized (nodes) {
            for (final Node node : nodes) {
                if (hostname.equalsIgnoreCase(node.getHostname())) {
                    return node;
                }
            }
        }
        return null;
    }
}
