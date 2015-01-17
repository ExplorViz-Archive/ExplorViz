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

	public static void addNode(final String ip, final String nodeGroupName) {
		LOG.info("Adding node '" + ip + "' to loadbalancers");
		try {
			synchronized (loadBalancerUrlHostPorts) {
				for (final String hostPort : loadBalancerUrlHostPorts) {
					new URL(hostPort + "add&group=" + nodeGroupName + "&node=" + ip).openStream()
					.close();
				}
			}
		} catch (final MalformedURLException e) {
			LOG.error(e.getMessage(), e);
		} catch (final IOException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	public static void removeNode(final String ip, final String nodeGroupName) {
		LOG.info("Removing node '" + ip + "' from loadbalancers");
		try {
			synchronized (loadBalancerUrlHostPorts) {
				for (final String hostPort : loadBalancerUrlHostPorts) {
					new URL(hostPort + "remove&group=" + nodeGroupName + "&node=" + ip)
							.openStream().close();
				}
			}
		} catch (final MalformedURLException e) {
			LOG.error(e.getMessage(), e);
		} catch (final IOException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	public static void reset() {
		LOG.info("Resetting loadbalancers");
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

	public static int getNodeCount() {
		synchronized (loadBalancerUrlHostPorts) {
			return loadBalancerUrlHostPorts.size();
		}
	}
}
