package explorviz.visualization.experiment.tools;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

public class ExperimentSliderJS {

	public static native void showSliderForExp(JsArrayString landscapeNames,
			String jsonExperiment) /*-{
		var height = @explorviz.visualization.engine.main.WebGLStart::viewportHeight;

		//slider.js in war/js/
		$wnd.Slider(height, saveQuestion, landscapeNames, loadlandscape,
				jsonExperiment, loadExperimentToolsPage, true);

		function loadlandscape(timestamp) {
			@explorviz.visualization.experiment.tools.ExperimentSlider::loadLandscape(Ljava/lang/String;)(timestamp)
		}

		function saveQuestion(questionForm) {
			@explorviz.visualization.experiment.tools.ExperimentSlider::saveToServer(Ljava/lang/String;)(questionForm)
		}

		function loadExperimentToolsPage() {
			@explorviz.visualization.experiment.tools.ExperimentToolsPage::loadExpToolsPage()()
		}

	}-*/;

	public static class OverlayJSObj extends JavaScriptObject {
		protected OverlayJSObj() {
		}

		public final native JavaScriptObject test() /*-{
			var obj = {};

			Object.keys(this).forEach(function(key) {
				obj[key] = this[key];
			});

			return obj;
		}-*/;

		public final native String[] getKeys() /*-{
			return Object.keys(this);
		}-*/;

		public final native String[] getValues() /*-{
			var arr = [];

			var length = Object.keys(this).length;

			for ( var key in this) {
				arr.push(this[key]);
			}

			return arr;
		}-*/;

	}

}
