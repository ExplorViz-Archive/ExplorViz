package explorviz.visualization.experiment;

import com.google.gwt.core.client.JavaScriptObject;

public class NewExperimentJS {

	public static native void init() /*-{
		var height = @explorviz.visualization.engine.main.WebGLStart::viewportHeight;

		//slider.js in war/js/
		$wnd.Slider("Question Interface", height, saveQuestion);

		function saveQuestion(questionForm) {
			console.log(questionForm);
			@explorviz.visualization.experiment.NewExperiment::saveToServer(Lexplorviz/visualization/experiment/NewExperimentJS$OverlayJSObj;)(questionForm)
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
