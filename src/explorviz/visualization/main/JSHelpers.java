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
		var evt = $doc.createEvent("HTMLEvents");
		evt.initEvent("click");
		$wnd.jQuery("<a>", {
			download : filename,
			href : webkitURL.createObjectURL(blob)
		}).get(0).dispatchEvent(evt);
	}-*/;

	// TODO ...
	public static native void registerResizeHandler() /*-{
		$wnd.jQuery($wnd).on("debouncedresize", function(event) {
			//@explorviz.visualization.main.ExplorViz::resizeHandler()()
		});
	}-*/;
}
