package explorviz.visualization.experiment.tools;

public class ExperimentChartJS {

	public static native void showExpChart() /*-{

		var canvas = $doc.getElementById("expChart");

		$wnd.ExpChart(canvas);

	}-*/;

}