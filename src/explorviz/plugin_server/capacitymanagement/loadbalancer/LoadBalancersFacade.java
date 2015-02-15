package explorviz.plugin_server.capacitymanagement.loadbalancer;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Facade to add/remove nodes from the load balancer or to reset it
 *
 * @author jgi, dtj
 *
 */
public class LoadBalancersFacade {

	private static final Logger LOG = LoggerFactory.getLogger(LoadBalancersFacade.class);

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
		} catch (final MalformedURLException e) {
			LOG.error(e.getMessage(), e);
		} catch (final IOException e) {
			LOG.error(e.getMessage(), e);
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
			LOG.error(e.getMessage(), e);
		} catch (final IOException e) {
			LOG.error(e.getMessage(), e);
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
			LOG.error(e.getMessage(), e);
		} catch (final IOException e) {
			LOG.error(e.getMessage(), e);
		}
	}

}
