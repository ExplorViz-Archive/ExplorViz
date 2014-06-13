package explorviz.visualization.main;

public class JSHelpers {

	public static native void showElementById(String id) /*-{
		$wnd.jQuery("#" + id).show();
	}-*/;

	public static native void hideElementById(String id) /*-{
		$wnd.jQuery("#" + id).hide();
	}-*/;

	public static native void downloadAsFile(String filename, String content) /*-{
		var blob = new Blob([ content ]);

		function createObjectURL(file) {
			if (window.webkitURL) {
				return window.webkitURL.createObjectURL(file);
			} else if (window.URL && window.URL.createObjectURL) {
				return window.URL.createObjectURL(file);
			} else {
				return null;
			}
		}

		$wnd.jQuery("<a>", {
			download : filename,
			href : createObjectURL(blob)
		}).get(0).click();
	}-*/;

	// TODO ...
	public static native void registerResizeHandler() /*-{
		$wnd.jQuery($wnd).on("debouncedresize", function(event) {
			//@explorviz.visualization.main.ExplorViz::resizeHandler()()
		});
	}-*/;
}
