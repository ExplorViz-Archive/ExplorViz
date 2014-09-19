package explorviz.visualization.clustering;

public class ClusteringJS {
	public static native void openDialog() /*-{
		$wnd.jQuery("#syntheticClusteringDialog").show();
		$wnd.jQuery("#syntheticClusteringDialog").dialog({
			closeOnEscape : true,
			modal : true,
			title : 'Choose Clustering Parameters',
			width : '80%',
			resizable : false,
			height : 640,
			position : {
				my : 'center center',
				at : 'center center',
				of : $wnd.jQuery("#view")
			}
		}).focus();
	}-*/;
}
