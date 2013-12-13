package explorviz.visualization.engine.math;

import static org.junit.Assert.*;

import org.junit.Test;

public class Matrix44fTest {
    
    private static final float DELTA = 0.001f;
    
    @Test
    public void testMatrix44d() {
	final Matrix44f ident = new Matrix44f();
	
	assertEquals(1.0f, ident.entries[0], DELTA);
	assertEquals(0.0f, ident.entries[1], DELTA);
	assertEquals(0.0f, ident.entries[2], DELTA);
	assertEquals(0.0f, ident.entries[3], DELTA);
	assertEquals(0.0f, ident.entries[4], DELTA);
	assertEquals(1.0f, ident.entries[5], DELTA);
	assertEquals(0.0f, ident.entries[6], DELTA);
	assertEquals(0.0f, ident.entries[7], DELTA);
	assertEquals(0.0f, ident.entries[8], DELTA);
	assertEquals(0.0f, ident.entries[9], DELTA);
	assertEquals(1.0f, ident.entries[10], DELTA);
	assertEquals(0.0f, ident.entries[11], DELTA);
	assertEquals(0.0f, ident.entries[12], DELTA);
	assertEquals(0.0f, ident.entries[13], DELTA);
	assertEquals(0.0f, ident.entries[14], DELTA);
	assertEquals(1.0f, ident.entries[15], DELTA);
    }
    
    @Test
    public void testMatrix44dMatrix44d() {
	final Matrix44f src = new Matrix44f(1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f, 8.0f, 9.0f,
		10.0f, 11.0f, 12.0f, 13.0f, 14.0f, 15.0f, 16.0f);
	final Matrix44f base = new Matrix44f(src);
	
	assertEquals(1.0f, base.entries[0], DELTA);
	assertEquals(2.0f, base.entries[1], DELTA);
	assertEquals(3.0f, base.entries[2], DELTA);
	assertEquals(4.0f, base.entries[3], DELTA);
	assertEquals(5.0f, base.entries[4], DELTA);
	assertEquals(6.0f, base.entries[5], DELTA);
	assertEquals(7.0f, base.entries[6], DELTA);
	assertEquals(8.0f, base.entries[7], DELTA);
	assertEquals(9.0f, base.entries[8], DELTA);
	assertEquals(10.0f, base.entries[9], DELTA);
	assertEquals(11.0f, base.entries[10], DELTA);
	assertEquals(12.0f, base.entries[11], DELTA);
	assertEquals(13.0f, base.entries[12], DELTA);
	assertEquals(14.0f, base.entries[13], DELTA);
	assertEquals(15.0f, base.entries[14], DELTA);
	assertEquals(16.0f, base.entries[15], DELTA);
    }
    
    @Test
    public void testMatrix44dFloatFloatFloatFloatFloatFloatFloatFloatFloatFloatFloatFloatFloatFloatFloatFloat() {
	final Matrix44f base = new Matrix44f(1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f, 8.0f, 9.0f,
		10.0f, 11.0f, 12.0f, 13.0f, 14.0f, 15.0f, 16.0f);
	
	assertEquals(1.0f, base.entries[0], DELTA);
	assertEquals(2.0f, base.entries[1], DELTA);
	assertEquals(3.0f, base.entries[2], DELTA);
	assertEquals(4.0f, base.entries[3], DELTA);
	assertEquals(5.0f, base.entries[4], DELTA);
	assertEquals(6.0f, base.entries[5], DELTA);
	assertEquals(7.0f, base.entries[6], DELTA);
	assertEquals(8.0f, base.entries[7], DELTA);
	assertEquals(9.0f, base.entries[8], DELTA);
	assertEquals(10.0f, base.entries[9], DELTA);
	assertEquals(11.0f, base.entries[10], DELTA);
	assertEquals(12.0f, base.entries[11], DELTA);
	assertEquals(13.0f, base.entries[12], DELTA);
	assertEquals(14.0f, base.entries[13], DELTA);
	assertEquals(15.0f, base.entries[14], DELTA);
	assertEquals(16.0f, base.entries[15], DELTA);
    }
    
    @Test
    public void testMultVector4f() {
	final Matrix44f base = new Matrix44f(1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f, 8.0f, 9.0f,
		10.0f, 11.0f, 12.0f, 13.0f, 14.0f, 15.0f, 16.0f);
	
	final Vector4f v = new Vector4f(1.0f, 2.0f, 3.0f, 4.0f);
	
	final Vector4f multResult = base.mult(v);
	
	assertEquals(90.0f, multResult.x, DELTA);
	assertEquals(100.0f, multResult.y, DELTA);
	assertEquals(110.0f, multResult.z, DELTA);
	assertEquals(120.0f, multResult.w, DELTA);
    }
    
    @Test
    public void testMultMatrix44f() {
	final Matrix44f base = new Matrix44f(1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f, 8.0f, 9.0f,
		10.0f, 11.0f, 12.0f, 13.0f, 14.0f, 15.0f, 16.0f);
	
	final Matrix44f multResult = base.mult(base);
	
	assertEquals(90.0f, multResult.entries[0], DELTA);
	assertEquals(100.0f, multResult.entries[1], DELTA);
	assertEquals(110.0f, multResult.entries[2], DELTA);
	assertEquals(120.0f, multResult.entries[3], DELTA);
	assertEquals(202.0f, multResult.entries[4], DELTA);
	assertEquals(228.0f, multResult.entries[5], DELTA);
	assertEquals(254.0f, multResult.entries[6], DELTA);
	assertEquals(280.0f, multResult.entries[7], DELTA);
	assertEquals(314.0f, multResult.entries[8], DELTA);
	assertEquals(356.0f, multResult.entries[9], DELTA);
	assertEquals(398.0f, multResult.entries[10], DELTA);
	assertEquals(440.0f, multResult.entries[11], DELTA);
	assertEquals(426.0f, multResult.entries[12], DELTA);
	assertEquals(484.0f, multResult.entries[13], DELTA);
	assertEquals(542.0f, multResult.entries[14], DELTA);
	assertEquals(600.0f, multResult.entries[15], DELTA);
    }
    
    @Test
    public void testTranspose() {
	final Matrix44f base = new Matrix44f(1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f, 8.0f, 9.0f,
		10.0f, 11.0f, 12.0f, 13.0f, 14.0f, 15.0f, 16.0f).transpose();
	
	assertEquals(1.0f, base.entries[0], DELTA);
	assertEquals(5.0f, base.entries[1], DELTA);
	assertEquals(9.0f, base.entries[2], DELTA);
	assertEquals(13.0f, base.entries[3], DELTA);
	assertEquals(2.0f, base.entries[4], DELTA);
	assertEquals(6.0f, base.entries[5], DELTA);
	assertEquals(10.0f, base.entries[6], DELTA);
	assertEquals(14.0f, base.entries[7], DELTA);
	assertEquals(3.0f, base.entries[8], DELTA);
	assertEquals(7.0f, base.entries[9], DELTA);
	assertEquals(11.0f, base.entries[10], DELTA);
	assertEquals(15.0f, base.entries[11], DELTA);
	assertEquals(4.0f, base.entries[12], DELTA);
	assertEquals(8.0f, base.entries[13], DELTA);
	assertEquals(12.0f, base.entries[14], DELTA);
	assertEquals(16.0f, base.entries[15], DELTA);
    }
    
    @Test
    public void testNormalMatrix() {
	final Matrix33f base = new Matrix44f().normalMatrix();
	
	assertEquals(1.0f, base.entries[0], DELTA);
	assertEquals(0.0f, base.entries[1], DELTA);
	assertEquals(0.0f, base.entries[2], DELTA);
	assertEquals(0.0f, base.entries[3], DELTA);
	assertEquals(1.0f, base.entries[4], DELTA);
	assertEquals(0.0f, base.entries[5], DELTA);
	assertEquals(0.0f, base.entries[6], DELTA);
	assertEquals(0.0f, base.entries[7], DELTA);
	assertEquals(1.0f, base.entries[8], DELTA);
    }
    
    @Test
    public void testRotationMatrix() {
	final Matrix33f base = new Matrix44f(1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f, 8.0f, 9.0f,
		10.0f, 11.0f, 12.0f, 13.0f, 14.0f, 15.0f, 16.0f).rotationMatrix();
	
	assertEquals(1.0f, base.entries[0], DELTA);
	assertEquals(2.0f, base.entries[1], DELTA);
	assertEquals(3.0f, base.entries[2], DELTA);
	assertEquals(5.0f, base.entries[3], DELTA);
	assertEquals(6.0f, base.entries[4], DELTA);
	assertEquals(7.0f, base.entries[5], DELTA);
	assertEquals(9.0f, base.entries[6], DELTA);
	assertEquals(10.0f, base.entries[7], DELTA);
	assertEquals(11.0f, base.entries[8], DELTA);
    }
    
    @Test
    public void testToString() {
	final Matrix44f ident = new Matrix44f();
	assertEquals(
		"(1.0, 0.0, 0.0, 0.0\n0.0, 1.0, 0.0, 0.0\n0.0, 0.0, 1.0, 0.0\n0.0, 0.0, 0.0, 1.0)",
		ident.toString());
    }
    
    @Test
    public void testRotationX() {
	final Matrix44f rotationX0 = Matrix44f.rotationX(0);
	
	assertEquals(1.0f, rotationX0.entries[0], DELTA);
	assertEquals(0.0f, rotationX0.entries[1], DELTA);
	assertEquals(0.0f, rotationX0.entries[2], DELTA);
	assertEquals(0.0f, rotationX0.entries[3], DELTA);
	assertEquals(0.0f, rotationX0.entries[4], DELTA);
	assertEquals(1.0f, rotationX0.entries[5], DELTA);
	assertEquals(0.0f, rotationX0.entries[6], DELTA);
	assertEquals(0.0f, rotationX0.entries[7], DELTA);
	assertEquals(0.0f, rotationX0.entries[8], DELTA);
	assertEquals(0.0f, rotationX0.entries[9], DELTA);
	assertEquals(1.0f, rotationX0.entries[10], DELTA);
	assertEquals(0.0f, rotationX0.entries[11], DELTA);
	assertEquals(0.0f, rotationX0.entries[12], DELTA);
	assertEquals(0.0f, rotationX0.entries[13], DELTA);
	assertEquals(0.0f, rotationX0.entries[14], DELTA);
	assertEquals(1.0f, rotationX0.entries[15], DELTA);
	
	final Matrix44f rotationX90 = Matrix44f.rotationX(90);
	
	assertEquals(1.0f, rotationX90.entries[0], DELTA);
	assertEquals(0.0f, rotationX90.entries[1], DELTA);
	assertEquals(0.0f, rotationX90.entries[2], DELTA);
	assertEquals(0.0f, rotationX90.entries[3], DELTA);
	assertEquals(0.0f, rotationX90.entries[4], DELTA);
	assertEquals(0.0f, rotationX90.entries[5], DELTA);
	assertEquals(1.0f, rotationX90.entries[6], DELTA);
	assertEquals(0.0f, rotationX90.entries[7], DELTA);
	assertEquals(0.0f, rotationX90.entries[8], DELTA);
	assertEquals(-1.0f, rotationX90.entries[9], DELTA);
	assertEquals(0.0f, rotationX90.entries[10], DELTA);
	assertEquals(0.0f, rotationX90.entries[11], DELTA);
	assertEquals(0.0f, rotationX90.entries[12], DELTA);
	assertEquals(0.0f, rotationX90.entries[13], DELTA);
	assertEquals(0.0f, rotationX90.entries[14], DELTA);
	assertEquals(1.0f, rotationX90.entries[15], DELTA);
	
	final Matrix44f rotationX180 = Matrix44f.rotationX(180);
	
	assertEquals(1.0f, rotationX180.entries[0], DELTA);
	assertEquals(0.0f, rotationX180.entries[1], DELTA);
	assertEquals(0.0f, rotationX180.entries[2], DELTA);
	assertEquals(0.0f, rotationX180.entries[3], DELTA);
	assertEquals(0.0f, rotationX180.entries[4], DELTA);
	assertEquals(-1.0f, rotationX180.entries[5], DELTA);
	assertEquals(0.0f, rotationX180.entries[6], DELTA);
	assertEquals(0.0f, rotationX180.entries[7], DELTA);
	assertEquals(0.0f, rotationX180.entries[8], DELTA);
	assertEquals(0.0f, rotationX180.entries[9], DELTA);
	assertEquals(-1.0f, rotationX180.entries[10], DELTA);
	assertEquals(0.0f, rotationX180.entries[11], DELTA);
	assertEquals(0.0f, rotationX180.entries[12], DELTA);
	assertEquals(0.0f, rotationX180.entries[13], DELTA);
	assertEquals(0.0f, rotationX180.entries[14], DELTA);
	assertEquals(1.0f, rotationX180.entries[15], DELTA);
    }
    
    @Test
    public void testRotationY() {
	final Matrix44f rotationY0 = Matrix44f.rotationY(0);
	
	assertEquals(1.0f, rotationY0.entries[0], DELTA);
	assertEquals(0.0f, rotationY0.entries[1], DELTA);
	assertEquals(0.0f, rotationY0.entries[2], DELTA);
	assertEquals(0.0f, rotationY0.entries[3], DELTA);
	assertEquals(0.0f, rotationY0.entries[4], DELTA);
	assertEquals(1.0f, rotationY0.entries[5], DELTA);
	assertEquals(0.0f, rotationY0.entries[6], DELTA);
	assertEquals(0.0f, rotationY0.entries[7], DELTA);
	assertEquals(0.0f, rotationY0.entries[8], DELTA);
	assertEquals(0.0f, rotationY0.entries[9], DELTA);
	assertEquals(1.0f, rotationY0.entries[10], DELTA);
	assertEquals(0.0f, rotationY0.entries[11], DELTA);
	assertEquals(0.0f, rotationY0.entries[12], DELTA);
	assertEquals(0.0f, rotationY0.entries[13], DELTA);
	assertEquals(0.0f, rotationY0.entries[14], DELTA);
	assertEquals(1.0f, rotationY0.entries[15], DELTA);
	
	final Matrix44f rotationY90 = Matrix44f.rotationY(90);
	
	assertEquals(0.0f, rotationY90.entries[0], DELTA);
	assertEquals(0.0f, rotationY90.entries[1], DELTA);
	assertEquals(-1.0f, rotationY90.entries[2], DELTA);
	assertEquals(0.0f, rotationY90.entries[3], DELTA);
	assertEquals(0.0f, rotationY90.entries[4], DELTA);
	assertEquals(1.0f, rotationY90.entries[5], DELTA);
	assertEquals(0.0f, rotationY90.entries[6], DELTA);
	assertEquals(0.0f, rotationY90.entries[7], DELTA);
	assertEquals(1.0f, rotationY90.entries[8], DELTA);
	assertEquals(0.0f, rotationY90.entries[9], DELTA);
	assertEquals(0.0f, rotationY90.entries[10], DELTA);
	assertEquals(0.0f, rotationY90.entries[11], DELTA);
	assertEquals(0.0f, rotationY90.entries[12], DELTA);
	assertEquals(0.0f, rotationY90.entries[13], DELTA);
	assertEquals(0.0f, rotationY90.entries[14], DELTA);
	assertEquals(1.0f, rotationY90.entries[15], DELTA);
	
	final Matrix44f rotationY180 = Matrix44f.rotationY(180);
	
	assertEquals(-1.0f, rotationY180.entries[0], DELTA);
	assertEquals(0.0f, rotationY180.entries[1], DELTA);
	assertEquals(0.0f, rotationY180.entries[2], DELTA);
	assertEquals(0.0f, rotationY180.entries[3], DELTA);
	assertEquals(0.0f, rotationY180.entries[4], DELTA);
	assertEquals(1.0f, rotationY180.entries[5], DELTA);
	assertEquals(0.0f, rotationY180.entries[6], DELTA);
	assertEquals(0.0f, rotationY180.entries[7], DELTA);
	assertEquals(0.0f, rotationY180.entries[8], DELTA);
	assertEquals(0.0f, rotationY180.entries[9], DELTA);
	assertEquals(-1.0f, rotationY180.entries[10], DELTA);
	assertEquals(0.0f, rotationY180.entries[11], DELTA);
	assertEquals(0.0f, rotationY180.entries[12], DELTA);
	assertEquals(0.0f, rotationY180.entries[13], DELTA);
	assertEquals(0.0f, rotationY180.entries[14], DELTA);
	assertEquals(1.0f, rotationY180.entries[15], DELTA);
    }
    
    @Test
    public void testRotationZ() {
	final Matrix44f rotationZ0 = Matrix44f.rotationZ(0);
	
	assertEquals(1.0f, rotationZ0.entries[0], DELTA);
	assertEquals(0.0f, rotationZ0.entries[1], DELTA);
	assertEquals(0.0f, rotationZ0.entries[2], DELTA);
	assertEquals(0.0f, rotationZ0.entries[3], DELTA);
	assertEquals(0.0f, rotationZ0.entries[4], DELTA);
	assertEquals(1.0f, rotationZ0.entries[5], DELTA);
	assertEquals(0.0f, rotationZ0.entries[6], DELTA);
	assertEquals(0.0f, rotationZ0.entries[7], DELTA);
	assertEquals(0.0f, rotationZ0.entries[8], DELTA);
	assertEquals(0.0f, rotationZ0.entries[9], DELTA);
	assertEquals(1.0f, rotationZ0.entries[10], DELTA);
	assertEquals(0.0f, rotationZ0.entries[11], DELTA);
	assertEquals(0.0f, rotationZ0.entries[12], DELTA);
	assertEquals(0.0f, rotationZ0.entries[13], DELTA);
	assertEquals(0.0f, rotationZ0.entries[14], DELTA);
	assertEquals(1.0f, rotationZ0.entries[15], DELTA);
	
	final Matrix44f rotationZ90 = Matrix44f.rotationZ(90);
	
	assertEquals(0.0f, rotationZ90.entries[0], DELTA);
	assertEquals(1.0f, rotationZ90.entries[1], DELTA);
	assertEquals(0.0f, rotationZ90.entries[2], DELTA);
	assertEquals(0.0f, rotationZ90.entries[3], DELTA);
	assertEquals(-1.0f, rotationZ90.entries[4], DELTA);
	assertEquals(0.0f, rotationZ90.entries[5], DELTA);
	assertEquals(0.0f, rotationZ90.entries[6], DELTA);
	assertEquals(0.0f, rotationZ90.entries[7], DELTA);
	assertEquals(0.0f, rotationZ90.entries[8], DELTA);
	assertEquals(0.0f, rotationZ90.entries[9], DELTA);
	assertEquals(1.0f, rotationZ90.entries[10], DELTA);
	assertEquals(0.0f, rotationZ90.entries[11], DELTA);
	assertEquals(0.0f, rotationZ90.entries[12], DELTA);
	assertEquals(0.0f, rotationZ90.entries[13], DELTA);
	assertEquals(0.0f, rotationZ90.entries[14], DELTA);
	assertEquals(1.0f, rotationZ90.entries[15], DELTA);
	
	final Matrix44f rotationZ180 = Matrix44f.rotationZ(180);
	
	assertEquals(-1.0f, rotationZ180.entries[0], DELTA);
	assertEquals(0.0f, rotationZ180.entries[1], DELTA);
	assertEquals(0.0f, rotationZ180.entries[2], DELTA);
	assertEquals(0.0f, rotationZ180.entries[3], DELTA);
	assertEquals(0.0f, rotationZ180.entries[4], DELTA);
	assertEquals(-1.0f, rotationZ180.entries[5], DELTA);
	assertEquals(0.0f, rotationZ180.entries[6], DELTA);
	assertEquals(0.0f, rotationZ180.entries[7], DELTA);
	assertEquals(0.0f, rotationZ180.entries[8], DELTA);
	assertEquals(0.0f, rotationZ180.entries[9], DELTA);
	assertEquals(1.0f, rotationZ180.entries[10], DELTA);
	assertEquals(0.0f, rotationZ180.entries[11], DELTA);
	assertEquals(0.0f, rotationZ180.entries[12], DELTA);
	assertEquals(0.0f, rotationZ180.entries[13], DELTA);
	assertEquals(0.0f, rotationZ180.entries[14], DELTA);
	assertEquals(1.0f, rotationZ180.entries[15], DELTA);
    }
    
    @Test
    public void testRotationAxis() {
	final Vector3f axis = new Vector3f(1, 1, 1);
	final Matrix44f rotationAxis0 = Matrix44f.rotationAxis(axis, 0);
	
	assertEquals(1.0f, rotationAxis0.entries[0], DELTA);
	assertEquals(0.0f, rotationAxis0.entries[1], DELTA);
	assertEquals(0.0f, rotationAxis0.entries[2], DELTA);
	assertEquals(0.0f, rotationAxis0.entries[3], DELTA);
	assertEquals(0.0f, rotationAxis0.entries[4], DELTA);
	assertEquals(1.0f, rotationAxis0.entries[5], DELTA);
	assertEquals(0.0f, rotationAxis0.entries[6], DELTA);
	assertEquals(0.0f, rotationAxis0.entries[7], DELTA);
	assertEquals(0.0f, rotationAxis0.entries[8], DELTA);
	assertEquals(0.0f, rotationAxis0.entries[9], DELTA);
	assertEquals(1.0f, rotationAxis0.entries[10], DELTA);
	assertEquals(0.0f, rotationAxis0.entries[11], DELTA);
	assertEquals(0.0f, rotationAxis0.entries[12], DELTA);
	assertEquals(0.0f, rotationAxis0.entries[13], DELTA);
	assertEquals(0.0f, rotationAxis0.entries[14], DELTA);
	assertEquals(1.0f, rotationAxis0.entries[15], DELTA);
	
	final Matrix44f rotationAxis90 = Matrix44f.rotationAxis(axis, 90);
	
	assertEquals(0.3333f, rotationAxis90.entries[0], DELTA);
	assertEquals(-0.2440f, rotationAxis90.entries[1], DELTA);
	assertEquals(0.91068f, rotationAxis90.entries[2], DELTA);
	assertEquals(0.0f, rotationAxis90.entries[3], DELTA);
	assertEquals(0.91068f, rotationAxis90.entries[4], DELTA);
	assertEquals(0.3333f, rotationAxis90.entries[5], DELTA);
	assertEquals(-0.2440f, rotationAxis90.entries[6], DELTA);
	assertEquals(0.0f, rotationAxis90.entries[7], DELTA);
	assertEquals(-0.2440f, rotationAxis90.entries[8], DELTA);
	assertEquals(0.9106f, rotationAxis90.entries[9], DELTA);
	assertEquals(0.3333f, rotationAxis90.entries[10], DELTA);
	assertEquals(0.0f, rotationAxis90.entries[11], DELTA);
	assertEquals(0.0f, rotationAxis90.entries[12], DELTA);
	assertEquals(0.0f, rotationAxis90.entries[13], DELTA);
	assertEquals(0.0f, rotationAxis90.entries[14], DELTA);
	assertEquals(1.0f, rotationAxis90.entries[15], DELTA);
	
	final Matrix44f rotationAxis180 = Matrix44f.rotationAxis(axis, 180);
	
	assertEquals(-0.3333f, rotationAxis180.entries[0], DELTA);
	assertEquals(0.6666f, rotationAxis180.entries[1], DELTA);
	assertEquals(0.6666f, rotationAxis180.entries[2], DELTA);
	assertEquals(0.0f, rotationAxis180.entries[3], DELTA);
	assertEquals(0.6666f, rotationAxis180.entries[4], DELTA);
	assertEquals(-0.3333f, rotationAxis180.entries[5], DELTA);
	assertEquals(0.66666f, rotationAxis180.entries[6], DELTA);
	assertEquals(0.0f, rotationAxis180.entries[7], DELTA);
	assertEquals(0.6666f, rotationAxis180.entries[8], DELTA);
	assertEquals(0.6666f, rotationAxis180.entries[9], DELTA);
	assertEquals(-0.3333f, rotationAxis180.entries[10], DELTA);
	assertEquals(0.0f, rotationAxis180.entries[11], DELTA);
	assertEquals(0.0f, rotationAxis180.entries[12], DELTA);
	assertEquals(0.0f, rotationAxis180.entries[13], DELTA);
	assertEquals(0.0f, rotationAxis180.entries[14], DELTA);
	assertEquals(1.0f, rotationAxis180.entries[15], DELTA);
    }
    
    @Test
    public void testScale() {
	final Matrix44f scaled0 = Matrix44f.scale(0);
	
	assertEquals(0.0f, scaled0.entries[0], DELTA);
	assertEquals(0.0f, scaled0.entries[1], DELTA);
	assertEquals(0.0f, scaled0.entries[2], DELTA);
	assertEquals(0.0f, scaled0.entries[3], DELTA);
	assertEquals(0.0f, scaled0.entries[4], DELTA);
	assertEquals(0.0f, scaled0.entries[5], DELTA);
	assertEquals(0.0f, scaled0.entries[6], DELTA);
	assertEquals(0.0f, scaled0.entries[7], DELTA);
	assertEquals(0.0f, scaled0.entries[8], DELTA);
	assertEquals(0.0f, scaled0.entries[9], DELTA);
	assertEquals(0.0f, scaled0.entries[10], DELTA);
	assertEquals(0.0f, scaled0.entries[11], DELTA);
	assertEquals(0.0f, scaled0.entries[12], DELTA);
	assertEquals(0.0f, scaled0.entries[13], DELTA);
	assertEquals(0.0f, scaled0.entries[14], DELTA);
	assertEquals(1.0f, scaled0.entries[15], DELTA);
	
	final Matrix44f scaled5 = Matrix44f.scale(5);
	
	assertEquals(5.0f, scaled5.entries[0], DELTA);
	assertEquals(0.0f, scaled5.entries[1], DELTA);
	assertEquals(0.0f, scaled5.entries[2], DELTA);
	assertEquals(0.0f, scaled5.entries[3], DELTA);
	assertEquals(0.0f, scaled5.entries[4], DELTA);
	assertEquals(5.0f, scaled5.entries[5], DELTA);
	assertEquals(0.0f, scaled5.entries[6], DELTA);
	assertEquals(0.0f, scaled5.entries[7], DELTA);
	assertEquals(0.0f, scaled5.entries[8], DELTA);
	assertEquals(0.0f, scaled5.entries[9], DELTA);
	assertEquals(5.0f, scaled5.entries[10], DELTA);
	assertEquals(0.0f, scaled5.entries[11], DELTA);
	assertEquals(0.0f, scaled5.entries[12], DELTA);
	assertEquals(0.0f, scaled5.entries[13], DELTA);
	assertEquals(0.0f, scaled5.entries[14], DELTA);
	assertEquals(1.0f, scaled5.entries[15], DELTA);
	
	final Matrix44f scaledM5 = Matrix44f.scale(-5);
	
	assertEquals(-5.0f, scaledM5.entries[0], DELTA);
	assertEquals(0.0f, scaledM5.entries[1], DELTA);
	assertEquals(0.0f, scaledM5.entries[2], DELTA);
	assertEquals(0.0f, scaledM5.entries[3], DELTA);
	assertEquals(0.0f, scaledM5.entries[4], DELTA);
	assertEquals(-5.0f, scaledM5.entries[5], DELTA);
	assertEquals(0.0f, scaledM5.entries[6], DELTA);
	assertEquals(0.0f, scaledM5.entries[7], DELTA);
	assertEquals(0.0f, scaledM5.entries[8], DELTA);
	assertEquals(0.0f, scaledM5.entries[9], DELTA);
	assertEquals(-5.0f, scaledM5.entries[10], DELTA);
	assertEquals(0.0f, scaledM5.entries[11], DELTA);
	assertEquals(0.0f, scaledM5.entries[12], DELTA);
	assertEquals(0.0f, scaledM5.entries[13], DELTA);
	assertEquals(0.0f, scaledM5.entries[14], DELTA);
	assertEquals(1.0f, scaledM5.entries[15], DELTA);
    }
    
    @Test
    public void testTranslationVector3f() {
	final Matrix44f base = Matrix44f.translation(new Vector3f(1.0f, 2.0f, -1.0f));
	
	assertEquals(1.0f, base.entries[0], DELTA);
	assertEquals(0.0f, base.entries[1], DELTA);
	assertEquals(0.0f, base.entries[2], DELTA);
	assertEquals(0.0f, base.entries[3], DELTA);
	assertEquals(0.0f, base.entries[4], DELTA);
	assertEquals(1.0f, base.entries[5], DELTA);
	assertEquals(0.0f, base.entries[6], DELTA);
	assertEquals(0.0f, base.entries[7], DELTA);
	assertEquals(0.0f, base.entries[8], DELTA);
	assertEquals(0.0f, base.entries[9], DELTA);
	assertEquals(1.0f, base.entries[10], DELTA);
	assertEquals(0.0f, base.entries[11], DELTA);
	assertEquals(1.0f, base.entries[12], DELTA);
	assertEquals(2.0f, base.entries[13], DELTA);
	assertEquals(-1.0f, base.entries[14], DELTA);
	assertEquals(1.0f, base.entries[15], DELTA);
    }
    
    @Test
    public void testTranslationFloatFloatFloat() {
	final Matrix44f base = Matrix44f.translation(0.0f, 3.0f, -2.0f);
	
	assertEquals(1.0f, base.entries[0], DELTA);
	assertEquals(0.0f, base.entries[1], DELTA);
	assertEquals(0.0f, base.entries[2], DELTA);
	assertEquals(0.0f, base.entries[3], DELTA);
	assertEquals(0.0f, base.entries[4], DELTA);
	assertEquals(1.0f, base.entries[5], DELTA);
	assertEquals(0.0f, base.entries[6], DELTA);
	assertEquals(0.0f, base.entries[7], DELTA);
	assertEquals(0.0f, base.entries[8], DELTA);
	assertEquals(0.0f, base.entries[9], DELTA);
	assertEquals(1.0f, base.entries[10], DELTA);
	assertEquals(0.0f, base.entries[11], DELTA);
	assertEquals(0.0f, base.entries[12], DELTA);
	assertEquals(3.0f, base.entries[13], DELTA);
	assertEquals(-2.0f, base.entries[14], DELTA);
	assertEquals(1.0f, base.entries[15], DELTA);
    }
    
    @Test
    public void testPerspective() {
	final Matrix44f base = Matrix44f.perspective(45.0f, 768 / (float) 1024, 0.1f, 1000.0f);
	
	assertEquals(3.218f, base.entries[0], DELTA);
	assertEquals(0.0f, base.entries[1], DELTA);
	assertEquals(0.0f, base.entries[2], DELTA);
	assertEquals(0.0f, base.entries[3], DELTA);
	assertEquals(0.0f, base.entries[4], DELTA);
	assertEquals(2.414f, base.entries[5], DELTA);
	assertEquals(0.0f, base.entries[6], DELTA);
	assertEquals(0.0f, base.entries[7], DELTA);
	assertEquals(0.0f, base.entries[8], DELTA);
	assertEquals(0.0f, base.entries[9], DELTA);
	assertEquals(-1.0f, base.entries[10], DELTA);
	assertEquals(-1.0f, base.entries[11], DELTA);
	assertEquals(0.0f, base.entries[12], DELTA);
	assertEquals(0.0f, base.entries[13], DELTA);
	assertEquals(-0.2f, base.entries[14], DELTA);
	assertEquals(0.0f, base.entries[15], DELTA);
    }
    
    @Test
    public void testLookAt() {
	final Matrix44f base = Matrix44f.lookAt(new Vector3f(0f, 0f, 1f), new Vector3f(),
		new Vector3f(0f, 1f, 0f));
	
	assertEquals(1.0f, base.entries[0], DELTA);
	assertEquals(0.0f, base.entries[1], DELTA);
	assertEquals(0.0f, base.entries[2], DELTA);
	assertEquals(0.0f, base.entries[3], DELTA);
	assertEquals(0.0f, base.entries[4], DELTA);
	assertEquals(1.0f, base.entries[5], DELTA);
	assertEquals(0.0f, base.entries[6], DELTA);
	assertEquals(0.0f, base.entries[7], DELTA);
	assertEquals(0.0f, base.entries[8], DELTA);
	assertEquals(0.0f, base.entries[9], DELTA);
	assertEquals(1.0f, base.entries[10], DELTA);
	assertEquals(0.0f, base.entries[11], DELTA);
	assertEquals(0.0f, base.entries[12], DELTA);
	assertEquals(0.0f, base.entries[13], DELTA);
	assertEquals(-1.0f, base.entries[14], DELTA);
	assertEquals(1.0f, base.entries[15], DELTA);
    }
    
}
