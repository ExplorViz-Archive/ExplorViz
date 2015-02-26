package explorviz.plugin_server.capacitymanagement.configuration;

import java.io.FileInputStream;
import java.util.Properties;

import explorviz.plugin_server.capacitymanagement.loadbalancer.LoadBalancersFacade;

public class LoadBalancersReader {

	public static void readInLoadBalancers(final String filename) throws Exception {
		final Properties settings = new Properties();
		settings.load(new FileInputStream(filename));

		final int loadBalancersCount = Integer.parseInt(settings.getProperty("loadBalancersCount"));
		for (int i = 1; i <= loadBalancersCount; i++) {
			getLoadBalancersFromConfig(i, settings);
		}
	}

	private static void getLoadBalancersFromConfig(final int index, final Properties settings) {
		final String loadBalancer = "loadBalancer" + index;

		final String host = settings.getProperty(loadBalancer + "Host");
		final String port = settings.getProperty(loadBalancer + "Port");

		LoadBalancersFacade.addLoadBalancerUrl(host, port);
	}
}
