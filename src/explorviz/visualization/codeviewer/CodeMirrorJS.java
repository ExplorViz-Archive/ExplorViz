package explorviz.visualization.codeviewer;

public class CodeMirrorJS {
	public static native void startCodeMirror(String code, String filename) /*-{
		$doc.getElementById("codeview").innerHTML = ""
		var myCodeMirror = $wnd.CodeMirror($doc.getElementById("codeview"), {
			value : code,
			mode : "text/x-java",
			lineNumbers : true,
			readOnly : true,
		});
		globalMyCodeMirror = myCodeMirror;
		$doc.getElementById("codeview-filename").innerHTML = filename

		//		myCodeMirror.focus();
	}-*/;

	public static native void fillCodeTree(String contentAsUL) /*-{
		$doc.getElementById("codetreeview").innerHTML = contentAsUL;
		$wnd
				.jQuery(".treeview li")
				.click(
						function() {
							if (!$wnd.jQuery(this).hasClass("submenu")) {
								var leaf = $wnd.jQuery(this)
								var filepath = ""

								leaf.parents('.submenu').each(
										function(index) {
											filepath = $wnd.jQuery(this).clone()
													.children().remove().end()
													.text().trim()
													+ "/" + filepath
										})

								@explorviz.visualization.codeviewer.CodeViewer::getCode(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)("explorviz", filepath, leaf.text().trim())
							}
						})
		$wnd.ddtreemenu.createTree("codetree", true);
    }-*/;
}