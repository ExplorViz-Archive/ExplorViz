package explorviz.visualization.databasequeries;

public class DatabaseQueriesJS {
	public static native void openDialog(String tableContent) /*-{
		$wnd.jQuery("#databaseQueriesDialog").show();
		$wnd.jQuery("#databaseQueriesDialog").dialog({
			closeOnEscape : true,
			modal : true,
			title : 'Database Queries',
			width : '80%',
			resizable : false,
			height : Math.max($wnd.jQuery("#view").innerHeight() - 250, 600),
			position : {
				my : 'center center',
				at : 'center center',
				of : $wnd.jQuery("#view")
			}
		}).focus();

		@explorviz.visualization.engine.popover.PopoverService::hidePopover()();
		@explorviz.visualization.engine.navigation.Navigation::cancelTimers()();

		$doc.getElementById("databaseQueriesDialog").innerHTML = '<table id="databaseQueriesInner" class="hover" cellspacing="0" style="width:100%;height:95%">'
				+ tableContent + '</table>';

		$wnd.jQuery("#databaseQueriesInner").DataTable({
			"dom" : '<"top"f>rt<"bottom"ip><"clear">',
			ordering : true,
			"order" : [ [ 2, "desc" ] ],
			paging : true,
		});
	}-*/;
}
