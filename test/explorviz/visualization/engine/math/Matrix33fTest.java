package explorviz.visualization.engine.math;

import static org.junit.Assert.*;

import org.junit.Test;

import explorviz.visualization.engine.math.Matrix33f;
import explorviz.visualization.engine.math.Vector3f;

public class Matrix33fTest {
    
    private static final float DELTA = 0.001f;
    
    @Test
    public void testMatrix33d() {
	final Matrix33f ident = new Matrix33f();
	
	assertEquals(1.0f, ident.entries[0], DELTA);
	assertEquals(0.0f, ident.entries[1], DELTA);
	assertEquals(0.0f, ident.entries[2], DELTA);
	assertEquals(0.0f, ident.entries[3], DELTA);
	assertEquals(1.0f, ident.entries[4], DELTA);
	assertEquals(0.0f, ident.entries[5], DELTA);
	assertEquals(0.0f, ident.entries[6], DELTA);
	assertEquals(0.0f, ident.entries[7], DELTA);
	assertEquals(1.0f, ident.entries[8], DELTA);
    }
    
    @Test
    public void testMatrix33dMatrix33d() {
	final Matrix33f src = new Matrix33f(1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f, 8.0f, 9.0f);
	final Matrix33f base = new Matrix33f(src);
	
	assertEquals(1.0f, base.entries[0], DELTA);
	assertEquals(2.0f, base.entries[1], DELTA);
	assertEquals(3.0f, base.entries[2], DELTA);
	assertEquals(4.0f, base.entries[3], DELTA);
	assertEquals(5.0f, base.entries[4], DELTA);
	assertEquals(6.0f, base.entries[5], DELTA);
	assertEquals(7.0f, base.entries[6], DELTA);
	assertEquals(8.0f, base.entries[7], DELTA);
	assertEquals(9.0f, base.entries[8], DELTA);
    }
    
    @Test
    public void testMatrix33dDoubleDoubleDoubleDoubleDoubleDoubleDoubleDoubleDouble() {
	final Matrix33f base = new Matrix33f(1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f, 8.0f, 9.0f);
	
	assertEquals(1.0f, base.entries[0], DELTA);
	assertEquals(2.0f, base.entries[1], DELTA);
	assertEquals(3.0f, base.entries[2], DELTA);
	assertEquals(4.0f, base.entries[3], DELTA);
	assertEquals(5.0f, base.entries[4], DELTA);
	assertEquals(6.0f, base.entries[5], DELTA);
	assertEquals(7.0f, base.entries[6], DELTA);
	assertEquals(8.0f, base.entries[7], DELTA);
	assertEquals(9.0f, base.entries[8], DELTA);
    }
    
    @Test
    public void testMultVector3f() {
	final Matrix33f base = new Matrix33f(1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f, 8.0f, 9.0f);
	final Vector3f multiplied = base.mult(new Vector3f(1.0f, 2.0f, 3.0f));
	
	assertEquals(30.0f, multiplied.x, DELTA);
	assertEquals(36.0f, multiplied.y, DELTA);
	assertEquals(42.0f, multiplied.z, DELTA);
    }
    
    @Test
    public void testMultMatrix33d() {
	final Matrix33f base = new Matrix33f(1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f, 8.0f, 9.0f);
	final Matrix33f multiplied = base.mult(base);
	
	assertEquals(30.0f, multiplied.entries[0], DELTA);
	assertEquals(36.0f, multiplied.entries[1], DELTA);
	assertEquals(42.0f, multiplied.entries[2], DELTA);
	assertEquals(66.0f, multiplied.entries[3], DELTA);
	assertEquals(81.0f, multiplied.entries[4], DELTA);
	assertEquals(96.0f, multiplied.entries[5], DELTA);
	assertEquals(102.0f, multiplied.entries[6], DELTA);
	assertEquals(126.0f, multiplied.entries[7], DELTA);
	assertEquals(150.0f, multiplied.entries[8], DELTA);
    }
    
    @Test
    public void testToString() {
	final Matrix33f matrix33d = new Matrix33f();
	
	assertEquals("(1.0, 0.0, 0.0\n0.0, 1.0, 0.0\n0.0, 0.0, 1.0)", matrix33d.toString());
    }
    
    @Test
    public void testRotationX() {
	final Matrix33f rotationX0 = Matrix33f.rotationX(0);
	
	assertEquals(1.0f, rotationX0.entries[0], DELTA);
	assertEquals(0.0f, rotationX0.entries[1], DELTA);
	assertEquals(0.0f, rotationX0.entries[2], DELTA);
	assertEquals(0.0f, rotationX0.entries[3], DELTA);
	assertEquals(1.0f, rotationX0.entries[4], DELTA);
	assertEquals(0.0f, rotationX0.entries[5], DELTA);
	assertEquals(0.0f, rotationX0.entries[6], DELTA);
	assertEquals(0.0f, rotationX0.entries[7], DELTA);
	assertEquals(1.0f, rotationX0.entries[8], DELTA);
	
	final Matrix33f rotationX90 = Matrix33f.rotationX(90);
	
	assertEquals(1.0f, rotationX90.entries[0], DELTA);
	assertEquals(0.0f, rotationX90.entries[1], DELTA);
	assertEquals(0.0f, rotationX90.entries[2], DELTA);
	assertEquals(0.0f, rotationX90.entries[3], DELTA);
	assertEquals(0.0f, rotationX90.entries[4], DELTA);
	assertEquals(1.0f, rotationX90.entries[5], DELTA);
	assertEquals(0.0f, rotationX90.entries[6], DELTA);
	assertEquals(-1.0f, rotationX90.entries[7], DELTA);
	assertEquals(0, rotationX90.entries[8], DELTA);
	
	final Matrix33f rotationX180 = Matrix33f.rotationX(180);
	
	assertEquals(1.0f, rotationX180.entries[0], DELTA);
	assertEquals(0.0f, rotationX180.entries[1], DELTA);
	assertEquals(0.0f, rotationX180.entries[2], DELTA);
	assertEquals(0.0f, rotationX180.entries[3], DELTA);
	assertEquals(-1.0f, rotationX180.entries[4], DELTA);
	assertEquals(0.0f, rotationX180.entries[5], DELTA);
	assertEquals(0.0f, rotationX180.entries[6], DELTA);
	assertEquals(0.0f, rotationX180.entries[7], DELTA);
	assertEquals(-1.0f, rotationX180.entries[8], DELTA);
    }
    
    @Test
    public void testRotationY() {
	final Matrix33f rotationY0 = Matrix33f.rotationY(0);
	
	assertEquals(1.0f, rotationY0.entries[0], DELTA);
	assertEquals(0.0f, rotationY0.entries[1], DELTA);
	assertEquals(0.0f, rotationY0.entries[2], DELTA);
	assertEquals(0.0f, rotationY0.entries[3], DELTA);
	assertEquals(1.0f, rotationY0.entries[4], DELTA);
	assertEquals(0.0f, rotationY0.entries[5], DELTA);
	assertEquals(0.0f, rotationY0.entries[6], DELTA);
	assertEquals(0.0f, rotationY0.entries[7], DELTA);
	assertEquals(1.0f, rotationY0.entries[8], DELTA);
	
	final Matrix33f rotationY90 = Matrix33f.rotationY(90);
	
	assertEquals(0.0f, rotationY90.entries[0], DELTA);
	assertEquals(0.0f, rotationY90.entries[1], DELTA);
	assertEquals(-1.0f, rotationY90.entries[2], DELTA);
	assertEquals(0.0f, rotationY90.entries[3], DELTA);
	assertEquals(1.0f, rotationY90.entries[4], DELTA);
	assertEquals(0.0f, rotationY90.entries[5], DELTA);
	assertEquals(1.0f, rotationY90.entries[6], DELTA);
	assertEquals(0.0f, rotationY90.entries[7], DELTA);
	assertEquals(0.0f, rotationY90.entries[8], DELTA);
	
	final Matrix33f rotationY180 = Matrix33f.rotationY(180);
	
	assertEquals(-1.0f, rotationY180.entries[0], DELTA);
	assertEquals(0.0f, rotationY180.entries[1], DELTA);
	assertEquals(0.0f, rotationY180.entries[2], DELTA);
	assertEquals(0.0f, rotationY180.entries[3], DELTA);
	assertEquals(1.0f, rotationY180.entries[4], DELTA);
	assertEquals(0.0f, rotationY180.entries[5], DELTA);
	assertEquals(0.0f, rotationY180.entries[6], DELTA);
	assertEquals(0.0f, rotationY180.entries[7], DELTA);
	assertEquals(-1.0f, rotationY180.entries[8], DELTA);
    }
    
    @Test
    public void testRotationZ() {
	final Matrix33f rotationZ0 = Matrix33f.rotationZ(0);
	
	assertEquals(1.0f, rotationZ0.entries[0], DELTA);
	assertEquals(0.0f, rotationZ0.entries[1], DELTA);
	assertEquals(0.0f, rotationZ0.entries[2], DELTA);
	assertEquals(0.0f, rotationZ0.entries[3], DELTA);
	assertEquals(1.0f, rotationZ0.entries[4], DELTA);
	assertEquals(0.0f, rotationZ0.entries[5], DELTA);
	assertEquals(0.0f, rotationZ0.entries[6], DELTA);
	assertEquals(0.0f, rotationZ0.entries[7], DELTA);
	assertEquals(1.0f, rotationZ0.entries[8], DELTA);
	
	final Matrix33f rotationZ90 = Matrix33f.rotationZ(90);
	
	assertEquals(0.0f, rotationZ90.entries[0], DELTA);
	assertEquals(1.0f, rotationZ90.entries[1], DELTA);
	assertEquals(0.0f, rotationZ90.entries[2], DELTA);
	assertEquals(-1.0f, rotationZ90.entries[3], DELTA);
	assertEquals(0.0f, rotationZ90.entries[4], DELTA);
	assertEquals(0.0f, rotationZ90.entries[5], DELTA);
	assertEquals(0.0f, rotationZ90.entries[6], DELTA);
	assertEquals(0.0f, rotationZ90.entries[7], DELTA);
	assertEquals(1.0f, rotationZ90.entries[8], DELTA);
	
	final Matrix33f rotationZ180 = Matrix33f.rotationZ(180);
	
	assertEquals(-1.0f, rotationZ180.entries[0], DELTA);
	assertEquals(0.0f, rotationZ180.entries[1], DELTA);
	assertEquals(0.0f, rotationZ180.entries[2], DELTA);
	assertEquals(0.0f, rotationZ180.entries[3], DELTA);
	assertEquals(-1.0f, rotationZ180.entries[4], DELTA);
	assertEquals(0.0f, rotationZ180.entries[5], DELTA);
	assertEquals(0.0f, rotationZ180.entries[6], DELTA);
	assertEquals(0.0f, rotationZ180.entries[7], DELTA);
	assertEquals(1.0f, rotationZ180.entries[8], DELTA);
    }
    
    @Test
    public void testRotationAxis() {
	final Vector3f axis = new Vector3f(1, 1, 1);
	
	final Matrix33f rotationAxis0 = Matrix33f.rotationAxis(axis, 0);
	
	assertEquals(1.0f, rotationAxis0.entries[0], DELTA);
	assertEquals(0.0f, rotationAxis0.entries[1], DELTA);
	assertEquals(0.0f, rotationAxis0.entries[2], DELTA);
	assertEquals(0.0f, rotationAxis0.entries[3], DELTA);
	assertEquals(1.0f, rotationAxis0.entries[4], DELTA);
	assertEquals(0.0f, rotationAxis0.entries[5], DELTA);
	assertEquals(0.0f, rotationAxis0.entries[6], DELTA);
	assertEquals(0.0f, rotationAxis0.entries[7], DELTA);
	assertEquals(1.0f, rotationAxis0.entries[8], DELTA);
	
	final Matrix33f rotationAxis90 = Matrix33f.rotationAxis(axis, 90);
	
	assertEquals(0.333, rotationAxis90.entries[0], DELTA);
	assertEquals(-0.244, rotationAxis90.entries[1], DELTA);
	assertEquals(0.910, rotationAxis90.entries[2], DELTA);
	assertEquals(0.910, rotationAxis90.entries[3], DELTA);
	assertEquals(0.333, rotationAxis90.entries[4], DELTA);
	assertEquals(-0.244, rotationAxis90.entries[5], DELTA);
	assertEquals(-0.244, rotationAxis90.entries[6], DELTA);
	assertEquals(0.910, rotationAxis90.entries[7], DELTA);
	assertEquals(0.333, rotationAxis90.entries[8], DELTA);
	
	final Matrix33f rotationAxis180 = Matrix33f.rotationAxis(axis, 180);
	
	assertEquals(-0.333, rotationAxis180.entries[0], DELTA);
	assertEquals(0.6666, rotationAxis180.entries[1], DELTA);
	assertEquals(0.6666, rotationAxis180.entries[2], DELTA);
	assertEquals(0.6666, rotationAxis180.entries[3], DELTA);
	assertEquals(-0.333, rotationAxis180.entries[4], DELTA);
	assertEquals(0.6666, rotationAxis180.entries[5], DELTA);
	assertEquals(0.6666, rotationAxis180.entries[6], DELTA);
	assertEquals(0.6666, rotationAxis180.entries[7], DELTA);
	assertEquals(-0.333, rotationAxis180.entries[8], DELTA);
    }
    
    @Test
    public void testScale() {
	final Matrix33f scaled0 = Matrix33f.scale(0);
	
	assertEquals(0.0f, scaled0.entries[0], DELTA);
	assertEquals(0.0f, scaled0.entries[1], DELTA);
	assertEquals(0.0f, scaled0.entries[2], DELTA);
	assertEquals(0.0f, scaled0.entries[3], DELTA);
	assertEquals(0.0f, scaled0.entries[4], DELTA);
	assertEquals(0.0f, scaled0.entries[5], DELTA);
	assertEquals(0.0f, scaled0.entries[6], DELTA);
	assertEquals(0.0f, scaled0.entries[7], DELTA);
	assertEquals(0.0f, scaled0.entries[8], DELTA);
	
	final Matrix33f scaled5 = Matrix33f.scale(5);
	
	assertEquals(5.0f, scaled5.entries[0], DELTA);
	assertEquals(0.0f, scaled5.entries[1], DELTA);
	assertEquals(0.0f, scaled5.entries[2], DELTA);
	assertEquals(0.0f, scaled5.entries[3], DELTA);
	assertEquals(5.0f, scaled5.entries[4], DELTA);
	assertEquals(0.0f, scaled5.entries[5], DELTA);
	assertEquals(0.0f, scaled5.entries[6], DELTA);
	assertEquals(0.0f, scaled5.entries[7], DELTA);
	assertEquals(5.0f, scaled5.entries[8], DELTA);
	
	final Matrix33f scaledM5 = Matrix33f.scale(-5);
	
	assertEquals(-5.0f, scaledM5.entries[0], DELTA);
	assertEquals(0.0f, scaledM5.entries[1], DELTA);
	assertEquals(0.0f, scaledM5.entries[2], DELTA);
	assertEquals(0.0f, scaledM5.entries[3], DELTA);
	assertEquals(-5.0f, scaledM5.entries[4], DELTA);
	assertEquals(0.0f, scaledM5.entries[5], DELTA);
	assertEquals(0.0f, scaledM5.entries[6], DELTA);
	assertEquals(0.0f, scaledM5.entries[7], DELTA);
	assertEquals(-5.0f, scaledM5.entries[8], DELTA);
    }
    
}
