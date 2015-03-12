package explorviz.plugin_server.capacitymanagement.loadbalancer;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Facade to add/remove nodes from the load balancer or to reset it Taken from
 * the capacity-manager-project.
 *
 */
public class LoadBalancersFacade {

	private static final Logger LOG = Logger.getLogger("LoadBalancer");

	private static final List<String> loadBalancerUrlHostPorts = new ArrayList<String>();

	public static void addLoadBalancerUrl(final String hostname, final String port) {
		synchronized (loadBalancerUrlHostPorts) {
			loadBalancerUrlHostPorts.add("http://" + hostname + ":" + port + "?action=");
		}
	}

	public static void addApplication(final int id, final String nodeIP,
			final String scalingGroupName) {
		LOG.info("Adding application '" + id + " on node " + nodeIP + "' to loadbalancer");
		try {
			synchronized (loadBalancerUrlHostPorts) {
				for (final String hostPort : loadBalancerUrlHostPorts) {

					new URL(hostPort + "add&group=" + scalingGroupName + "&ip=" + nodeIP + "&app="
							+ id).openStream().close();
				}
			}
		} catch (java.net.ConnectException ce) {
			LOG.info("connection Error im Loadbalancer: " + ce.getMessage());

		} catch (final MalformedURLException e) {
			LOG.severe(e.getMessage());
			e.printStackTrace();
		} catch (final IOException e) {
			LOG.severe(e.getMessage());
			e.printStackTrace();
		}
	}

	public static void removeApplication(final int id, final String nodeIP,
			final String scalingGroupName) {
		LOG.info("Removing Application " + id + " from node '" + nodeIP + "' from loadbalancers");
		try {
			synchronized (loadBalancerUrlHostPorts) {
				for (final String hostPort : loadBalancerUrlHostPorts) {
					new URL(hostPort + "remove&group=" + scalingGroupName + "&ip=" + nodeIP
							+ "&app=" + id).openStream().close();
				}
			}
		} catch (final MalformedURLException e) {
			LOG.severe(e.getMessage());
			e.printStackTrace();
		} catch (final IOException e) {
			LOG.severe(e.getMessage());
			e.printStackTrace();
		}
	}

	public static void reset() {
		LOG.info("Resetting loadbalancer");
		try {
			synchronized (loadBalancerUrlHostPorts) {
				for (final String hostPort : loadBalancerUrlHostPorts) {
					new URL(hostPort + "reset").openStream().close();
				}
			}
		} catch (final MalformedURLException e) {
			LOG.severe(e.getMessage());
			e.printStackTrace();
		} catch (final IOException e) {
			LOG.severe(e.getMessage());
			e.printStackTrace();
		}
	}

}
