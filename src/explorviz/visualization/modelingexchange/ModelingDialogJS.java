package explorviz.visualization.modelingexchange;

import explorviz.shared.model.*;
import explorviz.shared.model.helper.CommunicationTileAccumulator;
import explorviz.visualization.engine.main.SceneDrawer;
import explorviz.visualization.landscapeexchange.LandscapeExchangeManager;

public class ModelingDialogJS {
	public static void configureSystem(final explorviz.shared.model.System system) {
		showDialog("Configuration of " + system.getName(), system.getParent());
	}

	public static void configureNode(final Node node) {
		showDialog("Configuration of " + node.getName(), node.getParent().getParent().getParent());
	}

	public static void configureApplication(final Application application) {
		showDialog("Configuration of " + application.getName(), application.getParent().getParent()
				.getParent().getParent());
	}

	public static void addNewCommunication(final Application source) {
		showDialog("Add communication to source application " + source.getName(), source
				.getParent().getParent().getParent().getParent());
	}

	public static void configureCommunication(final CommunicationTileAccumulator commu) {
		showDialog("Configuration of " + commu.getCommunications().get(0).getSource().getName()
				+ " - " + commu.getCommunications().get(0).getTarget().getName(), commu
				.getCommunications().get(0).getSource().getParent().getParent().getParent()
				.getParent());
	}

	public static void updatePerspective(final Landscape landscape) {
		LandscapeExchangeManager.saveTargetModelIfInModelingMode(landscape);

		SceneDrawer.createObjectsFromLandscape(landscape, true);
	}

	public static native void showDialog(final String title, final Landscape landscape) /*-{
		$wnd.jQuery("#configurationModelingDialog").show();
		$wnd
				.jQuery("#configurationModelingDialog")
				.dialog(
						{
							closeOnEscape : true,
							modal : true,
							resizable : false,
							title : title,
							width : 400,
							height : 300,
							position : {
								my : 'center center',
								at : 'center center',
								of : $wnd.jQuery("#view")
							},
							close : function() {
								@explorviz.visualization.modelingexchange.ModelingDialogJS::updatePerspective(Lexplorviz/shared/model/Landscape;)(landscape);
							}
						}).focus();

		@explorviz.visualization.engine.popover.PopoverService::hidePopover()();

		$wnd.jQuery("#configurationModelingDialog").html("hi");
	}-*/;
}