package explorviz.plugin_server.capacitymanagement;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import explorviz.plugin_client.capacitymanagement.CapManService;
import explorviz.server.main.PluginManagerServerSide;
import explorviz.shared.model.Landscape;

public class CapManServiceImpl extends RemoteServiceServlet implements CapManService {
	private static final long serialVersionUID = 4562134950630636794L;

	@Override
	public void sendExecutionPlan(final Landscape landscape) {
		PluginManagerServerSide.receivedFinalCapacityAdaptationPlan(landscape);
	}

	@Override
	public void cancelButton(Landscape landscape) {
		PluginManagerServerSide.cancelButton(landscape);

	}
}
