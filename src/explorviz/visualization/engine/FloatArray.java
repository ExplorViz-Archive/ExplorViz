package explorviz.visualization.engine;

import elemental.html.Float32Array;

public class FloatArray {
	private native static Float32Array createFloat32Array(int length, float... nums) /*-{
		return new Float32Array(nums, 0, length);
	}-*/;

	private native static Float32Array createFloat32Array(int length) /*-{
		return new Float32Array(length);
	}-*/;

	public native static Float32Array set(Float32Array array, float value, int offset) /*-{
		array[offset] = value;
	}-*/;

	public native static Float32Array set(Float32Array array, float[] values, int offset) /*-{
		array.set(values, offset);
	}-*/;

	public native static Float32Array set(Float32Array array, Float32Array values, int offset) /*-{
		array.set(values, offset);
	}-*/;

	public static Float32Array create(final int length, final float... nums) {
		return createFloat32Array(length, nums);
	}

	public static Float32Array create(final int length) {
		return createFloat32Array(length);
	}

	public static Float32Array create(final float... nums) {
		return create(nums.length, nums);
	}
}
