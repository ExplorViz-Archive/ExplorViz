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

		if ($wnd.jQuery('#adaptiveMonitoringDialog').parents(
				'.ui-dialog:visible').length) {
			$wnd.jQuery("#adaptiveMonitoringDialog").dialog('close');
		}

		if ($wnd.jQuery('#errorDialog').parents('.ui-dialog:visible').length) {
			$wnd.jQuery("#errorDialog").dialog('close');
		}

		if ($wnd.jQuery('#traceReplayerDialog').parents('.ui-dialog:visible').length) {
			$wnd.jQuery("#traceReplayerDialog").dialog('close');
		}

		if ($wnd.jQuery('#traceHighlighterDialog')
				.parents('.ui-dialog:visible').length) {
			$wnd.jQuery("#traceHighlighterDialog").dialog('close');
		}
		if ($wnd.jQuery('#syntheticClusteringDialog').parents(
				'.ui-dialog:visible').length) {
			$wnd.jQuery("#syntheticClusteringDialog").dialog('close');
		}

		if ($wnd.jQuery('#codeViewerDialog').parents('.ui-dialog:visible').length) {
			$wnd.jQuery("#codeViewerDialog").dialog('close');
		}

		if ($wnd.jQuery('#tutorialDialog').parents('.ui-dialog:visible').length) {
			$wnd.jQuery("#tutorialDialog").dialog('close');
		}

		if ($wnd.jQuery('#questionDialog').parents('.ui-dialog:visible').length) {
			$wnd.jQuery("#questionDialog").dialog('close');
		}

		$wnd.jQuery("#tutorialArrowLeft").hide();
		$wnd.jQuery("#tutorialArrowDown").hide();
		$wnd.jQuery("#timer").hide();
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

	public static native void downloadAsZip(String filename, String content) /*-{
		var decodedBytes = window.atob(content)
		var buffer = new ArrayBuffer(decodedBytes.length);
		var array = new Uint8Array(buffer);
		for (var i = 0; i < decodedBytes.length; i++) {
			array[i] = decodedBytes.charCodeAt(i);
		}
		//var blob = new Blob([ decoded ]);

		var blob = new Blob([ array ], {
			type : "application/zip"
		});

		function createObjectURL(file) {
			if ($wnd.webkitURL) {
				return $wnd.webkitURL.createObjectURL(file);
			} else if ($wnd.URL && $wnd.URL.createObjectURL) {
				return $wnd.URL.createObjectURL(file);
			} else {
				return 'data:application/zip,' + encodeURIComponent(content);
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
			//			@explorviz.visualization.main.ExplorViz::resizeHandler()();
		});
	}-*/;

	public static native void centerSpinner() /*-{
		$wnd.jQuery("#spinner").center();
	}-*/;
}
