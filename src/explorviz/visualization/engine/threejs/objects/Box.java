package explorviz.visualization.engine.threejs.objects;

import explorviz.visualization.engine.math.Vector3f;

public class Box {

	public Vector3f center;
	public Vector3f extensionInEachDirection;

	public Box(final Vector3f center, final Vector3f extensionInEachDirection) {
		this.center = center;
		this.extensionInEachDirection = extensionInEachDirection;
	}

	public native void getCenter() /*-{
		var centerVector = this.@explorviz.visualization.engine.threejs.objects.Box::center;
		var center = centerVector.@explorviz.visualization.engine.math.Vector3f::getVector()();

		return center;
	}-*/;

	public native void getExtensions() /*-{
		var extensionVector = this.@explorviz.visualization.engine.threejs.objects.Box::extensionInEachDirection;
		var extension = extensionVector.@explorviz.visualization.engine.math.Vector3f::getVector()();

		return extension;
	}-*/;

}
