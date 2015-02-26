package explorviz.visualization.codeviewer;

public class CodeMirrorJS {
	public static native void openDialog(String application) /*-{
		$wnd.jQuery("#codeViewerDialog").show();
		$wnd.jQuery("#codeViewerDialog").dialog({
			closeOnEscape : true,
			modal : true,
			resizable : false,
			title : 'Code Viewer for ' + application,
			width : '70%',
			height : Math.max($wnd.jQuery("#view").innerHeight() - 100, 300),
			position : {
				my : 'center top',
				at : 'center top',
				of : $wnd.jQuery("#view")
			}
		}).focus();

		@explorviz.visualization.engine.popover.PopoverService::hidePopover()();

		$doc.getElementById("codeViewerDialog").innerHTML = '<div id="codetreeview-wrapper"><div id="codetreeview"></div></div><div id="codeview-wrapper"><h1 id="codeview-filename"></h1><div id="codeview" style="height:100%"></div></div>'
	}-*/;

	public static native void showCode(String code, String filename) /*-{
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

	public static native void fillCodeTree(String contentAsUL, String currentFilename) /*-{
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
													+ "/" + filepath;
										})

								filepath = filepath.substring(0,filepath.length-1);
								@explorviz.visualization.codeviewer.CodeViewer::getCode(Ljava/lang/String;Ljava/lang/String;)(filepath, leaf.text().trim())
							}
						})

		$wnd.ddtreemenu.createTree("codetree", false);
		selectedElement = $wnd.jQuery('.treeview li:contains("'+currentFilename+'")').not(".submenu").first();
		if (typeof selectedElement != 'undefined' && selectedElement.length > 0)
			$wnd.ddtreemenu.expandSubTree("codetree", selectedElement[0]);
    }-*/;
}
