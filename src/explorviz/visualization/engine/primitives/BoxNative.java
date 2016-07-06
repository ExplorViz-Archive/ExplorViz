package explorviz.visualization.engine.primitives;

import com.google.gwt.core.client.JavaScriptObject;

public class BoxNative {

	public static native JavaScriptObject getCenter(Box box)
	/*-{
		var centerVector = box.@explorviz.visualization.engine.primitives.Box::center;
		var center = centerVector.@explorviz.visualization.engine.math.Vector3f::getVector()();

		return center;
	}-*/;

	public static native JavaScriptObject getExtensions(Box box) /*-{
		var extensionVector = box.@explorviz.visualization.engine.primitives.Box::extensionInEachDirection;
		var extension = extensionVector.@explorviz.visualization.engine.math.Vector3f::getVector()();

		return extension;
	}-*/;

	public static native JavaScriptObject getColor(Box box) /*-{
		var colorVector = box.@explorviz.visualization.engine.primitives.Box::color;
		var color = colorVector.@explorviz.visualization.engine.math.Vector4f::getVector()();

		return color;
	}-*/;

}
