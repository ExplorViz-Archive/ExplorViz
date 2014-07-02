package explorviz.visualization.main;

public class JSHelpers {

	public static native void showElementById(String id) /*-{
		$wnd.jQuery("#" + id).show();
	}-*/;

	public static native void hideElementById(String id) /*-{
		$wnd.jQuery("#" + id).hide();
	}-*/;

	public static native void hideAllButtonsAndDialogs() /*-{
		$wnd.jQuery(".btn-default").hide();
		$wnd.jQuery("#startStopLabel").hide();
		$wnd.jQuery("#adaptiveMonitoringDialog").hide();
		$wnd.jQuery("#tutorialDialog").hide();
		$wnd.jQuery("#questionDialog").hide();
		$wnd.jQuery("#tutorialArrowLeft").hide();
		$wnd.jQuery("#tutorialArrowDown").hide();
		$wnd.jQuery("#genericPopover").hide();
	}-*/;

	public static native void downloadAsFile(String filename, String content) /*-{
		var blob = new Blob([ content ]);

		function createObjectURL(file) {
			if ($wnd.webkitURL) {
				return $wnd.webkitURL.createObjectURL(file);
			} else if ($wnd.URL && $wnd.URL.createObjectURL) {
				return $wnd.URL.createObjectURL(file);
			} else {
				return 'data:attachment/plain,' + encodeURIComponent(content);
			}
		}

		var a = $doc.createElement('a');
		a.href = createObjectURL(blob);
		a.target = '_blank';
		a.download = filename;

		$doc.body.appendChild(a);
		a.click();
		$doc.body.removeChild(a);
	}-*/;

	public static native void registerResizeHandler() /*-{
		$wnd.jQuery($wnd).on("debouncedresize", function(event) {
			@explorviz.visualization.main.ExplorViz::resizeHandler()();
		});
	}-*/;

	public static native void saveConfiguration() /*-{
		var res = $wnd.jQuery("#adminConfigurationForm").serialize();
		@explorviz.visualization.view.ConfigurationPage::saveConfiguration(Ljava/lang/String;)(res);
	}-*/;
}
