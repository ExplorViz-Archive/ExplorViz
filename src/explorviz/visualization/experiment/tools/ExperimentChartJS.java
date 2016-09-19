package explorviz.visualization.experiment.tools;

public class ExperimentChartJS {

	public static native void showExpChart(int finished, int remaining) /*-{

		var canvas = $doc.getElementById("expChart");

		$wnd.ExpChart(canvas, finished, remaining);

	}-*/;

}