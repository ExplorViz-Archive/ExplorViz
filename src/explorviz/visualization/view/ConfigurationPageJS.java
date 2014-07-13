package explorviz.visualization.view;

public class ConfigurationPageJS {
	public static native void init() /*-{
		$wnd
				.jQuery("#saveAdminConfig")
				.on(
						"click touchstart",
						function() {
							var result = $wnd.jQuery("#adminConfigurationForm")
									.serialize();
							@explorviz.visualization.view.ConfigurationPage::saveConfiguration(Ljava/lang/String;)(result);
						});
	}-*/;

	public static native void destroy() /*-{
		$wnd.jQuery("#saveAdminConfig").off("click touchstart");
	}-*/;
}
