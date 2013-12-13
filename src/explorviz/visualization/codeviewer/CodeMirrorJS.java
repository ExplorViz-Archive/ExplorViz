package explorviz.visualization.codeviewer;

public class CodeMirrorJS {
    public static native void startCodeMirror(String code, String filename) /*-{
		$doc.getElementById("codeview").innerHTML = ""
		var myCodeMirror = $wnd.CodeMirror($doc.getElementById("codeview"), {
			value : code,
			mode : "text/x-java",
			lineNumbers : true,
			onChange : function() {
				updateCodeMirror();
			}
		});
		globalMyCodeMirror = myCodeMirror;
		$doc.getElementById("codeview-filename").innerHTML = filename

		//		myCodeMirror.focus();

		function updateCodeMirror() {
			if (myCodeMirror.historySize().undo > 0) {
				$doc.getElementById("undo_codeviewer").children[0].src = "images/ribbon/back_enabled_hover.png";
			} else {
				$doc.getElementById("undo_codeviewer").children[0].src = "images/ribbon/back_disabled.png";
			}

			if (myCodeMirror.historySize().redo > 0) {
				$doc.getElementById("redo_codeviewer").children[0].src = "images/ribbon/forward_enabled_hover.png";
			} else {
				$doc.getElementById("redo_codeviewer").children[0].src = "images/ribbon/forward_disabled.png";
			}
		}
		updateCodeMirror();
    }-*/;
    
    public static native void undoCodeMirror() /*-{
		globalMyCodeMirror.undo();
    }-*/;
    
    public static native void redoCodeMirror() /*-{
		globalMyCodeMirror.redo();
    }-*/;
    
    public static native void updateHistoryDisplayCodeMirror() /*-{
		if (globalMyCodeMirror.historySize().undo > 0) {
			$doc.getElementById("undo_codeviewer").children[0].src = "images/ribbon/back_enabled_hover.png";
		} else {
			$doc.getElementById("undo_codeviewer").children[0].src = "images/ribbon/back_disabled.png";
		}

		if (globalMyCodeMirror.historySize().redo > 0) {
			$doc.getElementById("redo_codeviewer").children[0].src = "images/ribbon/forward_enabled_hover.png";
		} else {
			$doc.getElementById("redo_codeviewer").children[0].src = "images/ribbon/forward_disabled.png";
		}
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
