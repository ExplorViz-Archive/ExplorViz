package explorviz.plugin.capacitymanagement.node.repository;

import java.util.Date;
import java.util.LinkedList;

import explorviz.plugin.capacitymanagement.loadbalancer.LoadBalancersFacade;

/**
 * @author jgi, dtj Abstract object that represents "Nodes" in the Landscape.
 */
public class Node {

    private final LinkedList<Double> cpuUtilizationHistory = new LinkedList<Double>();

    private final String hostname;
    private final String privateIp;
    private final String instanceId;
    private final Date creationDate = new Date();

    private boolean enabled = true;

    private final int limitForCPUUtilizationHistory;

    private final ScalingGroup scalingGroup;

    private boolean addedToLoadBalancer = false;

    private String loadBalancerRemoveAfterStartIP;
    private String loadBalancerRemoveAfterStartScalingGroup;

    protected Node(final String privateIp, final String instanceId,
            final String hostname, final int limitForCPUUtilizationHistory,
            final ScalingGroup scalingGroup) {
        this.privateIp = privateIp;
        this.instanceId = instanceId;
        this.hostname = hostname;
        this.limitForCPUUtilizationHistory = limitForCPUUtilizationHistory;
        this.scalingGroup = scalingGroup;
    }

    public String getHostname() {
        return hostname;
    }

    public String getPrivateIP() {
        return privateIp;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void disable() {
        LoadBalancersFacade.removeNode(getPrivateIP(), getScalingGroup()
                .getLoadReceiver());
        enabled = false;
    }

    public ScalingGroup getScalingGroup() {
        return scalingGroup;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void addCPUUtilizationHistoryEntry(final double cpuUtilization) {
        synchronized (cpuUtilizationHistory) {
            if (cpuUtilizationHistory.size() >= limitForCPUUtilizationHistory) {
                cpuUtilizationHistory.removeFirst();
                if (!addedToLoadBalancer) {
                    if (getScalingGroup() != null) {
                        LoadBalancersFacade.addNode(getPrivateIP(),
                                getScalingGroup().getLoadReceiver());
                        if (loadBalancerRemoveAfterStartIP != null
                                && loadBalancerRemoveAfterStartScalingGroup != null) {
                            LoadBalancersFacade.removeNode(
                                    loadBalancerRemoveAfterStartIP,
                                    loadBalancerRemoveAfterStartScalingGroup);
                        }
                    }
                    addedToLoadBalancer = true;
                }
            }
            cpuUtilizationHistory.add(cpuUtilization);
        }
    }

    /**
     * @return -1, if CPU utilization history is empty
     */
    public double getAverageCPUUtilization() {
        synchronized (cpuUtilizationHistory) {
            if (cpuUtilizationHistory.isEmpty()) {
                return -1;
            }

            double sum = 0.0;
            for (final Double cpuUtil : cpuUtilizationHistory) {
                sum += cpuUtil;
            }

            return sum / cpuUtilizationHistory.size();
        }
    }

    public boolean hasSufficientCPUUilizationHistoryEntries() {
        synchronized (cpuUtilizationHistory) {
            return cpuUtilizationHistory.size() == limitForCPUUtilizationHistory;
        }
    }

    public void setLoadBalancerRemoveAfterStart(final String toRemoveIp,
            final String toRemoveScalingGroup) {
        loadBalancerRemoveAfterStartIP = toRemoveIp;
        loadBalancerRemoveAfterStartScalingGroup = toRemoveScalingGroup;
    }
}
