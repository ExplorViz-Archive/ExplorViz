package explorviz.visualization.conformancechecking;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

import explorviz.shared.model.Communication;
import explorviz.shared.model.Landscape;
import explorviz.shared.model.helper.LandscapeEdgeState;
import explorviz.visualization.engine.Logging;
import explorviz.visualization.experiment.Experiment;
import explorviz.visualization.landscapeexchange.LandscapeExchangeServiceAsync;
import explorviz.visualization.modelingexchange.ModelingExchangeService;

/**
 *
 * @author Till Simolka
 *
 */
public class ConformanceChecker {
	private static final boolean ENABLED = true;
	private static Landscape TARGET_MODEL;

	private static long LAST_HASH = -1;

	public static void init() {
		requestTargetModelLandscape();
	}

	private static void requestTargetModelLandscape() {
		final LandscapeExchangeServiceAsync landscapeExchangeService = GWT
				.create(ModelingExchangeService.class);
		final ServiceDefTarget endpoint = (ServiceDefTarget) landscapeExchangeService;
		final String moduleRelativeURL = GWT.getModuleBaseURL() + "modelingexchange";
		endpoint.setServiceEntryPoint(moduleRelativeURL);

		landscapeExchangeService.getCurrentLandscape(new ModelLandscapeCallback<Landscape>());
	}

	public static void setTargetModel(final Landscape targetModel) {
		TARGET_MODEL = targetModel;
	}

	public static void doConformanceChecking(final Landscape landscape) {
		if (ENABLED && (LAST_HASH != landscape.getHash()) && (TARGET_MODEL != null)
				&& (!Experiment.tutorial)) {
			Logging.log("running doConformanceChecking (debug example)");

			for (final Communication commu : landscape.getApplicationCommunication()) {
				// TODO set states on communications according to TARGET_MODEL
				commu.setState(LandscapeEdgeState.NORMAL);

				for (final Communication targetCommu : TARGET_MODEL.getApplicationCommunication()) {
					// ...
				}
			}

			LAST_HASH = landscape.getHash();
		}
	}
}
