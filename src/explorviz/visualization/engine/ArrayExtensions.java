package explorviz.visualization.engine;

import org.eclipse.xtext.xbase.lib.Inline;

public class ArrayExtensions {
    
    @Inline(value = "$1[$2]")
    public static float getElement(float[] array, int index) {
	return array[index];
    }
    
    @Inline(value = "$1[$2]")
    public static boolean getElement(boolean[] array, int index) {
	return array[index];
    }
    
    @Inline(value = "$1[$2]")
    public static byte getElement(byte[] array, int index) {
	return array[index];
    }
    
    @Inline(value = "$1[$2] = $3")
    public static void setElement(float[] array, int index, float f) {
	array[index] = f;
    }
    
    @Inline(value = "$1[$2] = $3")
    public static void setElement(boolean[] array, int index, boolean b) {
	array[index] = b;
    }
    
    @Inline(value = "new boolean[$1]")
    public static boolean[] createBooleanArray(int length) {
	return new boolean[length];
    }
    
    @Inline(value = "new float[$1]")
    public static float[] createFloatArray(int length) {
	return new float[length];
    }
    
    @Inline(value = "$1.length")
    public static int length(float[] array) {
	return array.length;
    }
    
    @Inline(value = "$1.length")
    public static int length(byte[] array) {
	return array.length;
    }
    
    @Inline(value = "$1++")
    public static int inc(int nr) {
	return nr++;
    }
}
