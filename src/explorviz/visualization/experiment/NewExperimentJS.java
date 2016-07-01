package explorviz.visualization.experiment;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

public class NewExperimentJS {

	public static native void init(JsArrayString jsArrayString) /*-{
		var height = @explorviz.visualization.engine.main.WebGLStart::viewportHeight;

		//slider.js in war/js/
		$wnd.Slider("Question Interface", height, saveQuestion, jsArrayString);

		function saveQuestion(questionForm) {
			@explorviz.visualization.experiment.NewExperiment::saveToServer2(Ljava/lang/String;)(questionForm)
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
