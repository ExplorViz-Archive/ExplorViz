package explorviz.visualization.main;

public class JSHelpers {

	public static native void showElementById(String id) /*-{
		$wnd.jQuery("#" + id).show();
	}-*/;

	public static native void hideElementById(String id) /*-{
		$wnd.jQuery("#" + id).hide();
	}-*/;

	public static native void downloadAsFile(String filename, String content) /*-{
		var a = $doc.createElement('a');
		a.href = 'data:attachment/plain,' + encodeURIComponent(content);
		a.target = '_blank';
		a.download = filename;

		$doc.body.appendChild(a);
		a.click();
		$doc.body.removeChild(a);
	}-*/;

	public static native void resizeHandler() /*-{
		$wnd.jQuery($wnd).on("debouncedresize", function(event) {
			alert("hi");
		});
	}-*/;
}
