package explorviz.visualization.engine.math;

import static org.junit.Assert.*;

import org.junit.Test;

import explorviz.visualization.engine.math.Vector4f;

public class Vector4fTest {
    
    private static final float DELTA = 0.001f;
    
    @Test
    public void testVector4d() {
	final Vector4f vector4d = new Vector4f();
	
	assertEquals(0, vector4d.x, DELTA);
	assertEquals(0, vector4d.y, DELTA);
	assertEquals(0, vector4d.z, DELTA);
	assertEquals(0, vector4d.w, DELTA);
    }
    
    @Test
    public void testVector4dVector4d() {
	final Vector4f src = new Vector4f();
	final Vector4f newvector = new Vector4f(src);
	
	assertEquals(src.x, newvector.x, DELTA);
	assertEquals(src.y, newvector.y, DELTA);
	assertEquals(src.z, newvector.z, DELTA);
    }
    
    @Test
    public void testVector4dDoubleDoubleDoubleDouble() {
	final Vector4f vector4d = new Vector4f(1.1f, 0f, -3.0f, 8.9f);
	
	assertEquals(1.1, vector4d.x, DELTA);
	assertEquals(0, vector4d.y, DELTA);
	assertEquals(-3.0f, vector4d.z, DELTA);
	assertEquals(8.9, vector4d.w, DELTA);
    }
    
    @Test
    public void testAdd() {
	final Vector4f base = new Vector4f(2.0f, -5.0f, 0.0f, 1.0f);
	final Vector4f summand = new Vector4f(1.0f, 2.0f, -3.0f, 3.0f);
	
	final Vector4f sum = base.add(summand);
	
	assertEquals(3.0f, sum.x, DELTA);
	assertEquals(-3.0f, sum.y, DELTA);
	assertEquals(-3.0f, sum.z, DELTA);
	assertEquals(4.0f, sum.w, DELTA);
    }
    
    @Test
    public void testSub() {
	final Vector4f base = new Vector4f(2.0f, -5.0f, 0.0f, 1.0f);
	final Vector4f submand = new Vector4f(1.0f, 2.0f, -3.0f, 3.0f);
	
	final Vector4f result = base.sub(submand);
	
	assertEquals(1.0f, result.x, DELTA);
	assertEquals(-7.0f, result.y, DELTA);
	assertEquals(3.0f, result.z, DELTA);
	assertEquals(-2.0f, result.w, DELTA);
    }
    
    @Test
    public void testNegate() {
	final Vector4f negated = new Vector4f(1.1f, 0f, -3.0f, 3.0f).negate();
	
	assertEquals(-1.1, negated.x, DELTA);
	assertEquals(0, negated.y, DELTA);
	assertEquals(3.0f, negated.z, DELTA);
	assertEquals(-3.0f, negated.w, DELTA);
    }
    
    @Test
    public void testScale() {
	final Vector4f scaled3 = new Vector4f(1.1f, 0f, -3.0f, 1.0f).scale(3);
	
	assertEquals(3.3, scaled3.x, DELTA);
	assertEquals(0, scaled3.y, DELTA);
	assertEquals(-9.0f, scaled3.z, DELTA);
	assertEquals(3.0f, scaled3.w, DELTA);
	
	final Vector4f scaled1 = new Vector4f(1.1f, 0f, -3.0f, 1.0f).scale(1);
	
	assertEquals(1.1, scaled1.x, DELTA);
	assertEquals(0, scaled1.y, DELTA);
	assertEquals(-3.0f, scaled1.z, DELTA);
	assertEquals(1.0f, scaled1.w, DELTA);
	
	final Vector4f scaled0 = new Vector4f(1.1f, 0f, -3.0f, 1.0f).scale(0);
	
	assertEquals(0, scaled0.x, DELTA);
	assertEquals(0, scaled0.y, DELTA);
	assertEquals(-0, scaled0.z, DELTA);
	assertEquals(0.0f, scaled0.w, DELTA);
	
	final Vector4f scaledMinus3 = new Vector4f(1.1f, 0f, -3.0f, 1.0f).scale(-3.0f);
	
	assertEquals(-3.3, scaledMinus3.x, DELTA);
	assertEquals(0, scaledMinus3.y, DELTA);
	assertEquals(9.0f, scaledMinus3.z, DELTA);
	assertEquals(-3.0f, scaledMinus3.w, DELTA);
    }
    
    @Test
    public void testDiv() {
	final Vector4f divided3 = new Vector4f(1.2f, 0f, -3.0f, 1.0f).div(3);
	
	assertEquals(0.4, divided3.x, DELTA);
	assertEquals(0, divided3.y, DELTA);
	assertEquals(-1.0f, divided3.z, DELTA);
	assertEquals(0.333, divided3.w, DELTA);
	
	final Vector4f divided1 = new Vector4f(1.1f, 0f, -3.0f, 1.0f).div(1);
	
	assertEquals(1.1, divided1.x, DELTA);
	assertEquals(0, divided1.y, DELTA);
	assertEquals(-3.0f, divided1.z, DELTA);
	assertEquals(1.0f, divided1.w, DELTA);
	
	final Vector4f scaledMinus3 = new Vector4f(1.1f, 0f, -3.0f, 1.0f).div(-3.0f);
	
	assertEquals(-0.36666, scaledMinus3.x, DELTA);
	assertEquals(0, scaledMinus3.y, DELTA);
	assertEquals(1.0f, scaledMinus3.z, DELTA);
	assertEquals(-0.333, scaledMinus3.w, DELTA);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testDivBy0() {
	final Vector4f divided0 = new Vector4f(1.1f, 0f, -3.0f, 1.0f).div(0);
	
	assertEquals(0, divided0.x, DELTA);
	assertEquals(0, divided0.y, DELTA);
	assertEquals(-0, divided0.z, DELTA);
	assertEquals(0, divided0.w, DELTA);
    }
    
    @Test
    public void testDot() {
	final double dotProd = new Vector4f(1.1f, 0f, -3.0f, 9.0f).dot(new Vector4f());
	
	assertEquals(0, dotProd, DELTA);
	
	final double dotProd2 = new Vector4f(1.1f, 0f, -3.0f, 9.0f).dot(new Vector4f(1.1f, 0f,
		-3.0f, 9.0f));
	
	assertEquals(91.21, dotProd2, DELTA);
    }
    
    @Test
    public void testLength() {
	final Vector4f vec0 = new Vector4f();
	assertEquals(0, vec0.length(), DELTA);
	
	final Vector4f vec1 = new Vector4f(1, 1, 1, 1);
	assertEquals(2.0f, vec1.length(), DELTA);
    }
    
    @Test
    public void testNormalize() {
	final Vector4f vec = new Vector4f(1.2f, 3f, -3.0f, 3.0f);
	assertEquals(5.3329, vec.length(), DELTA);
	
	final Vector4f vecNormalized = vec.normalize();
	
	assertEquals(1.0f, vecNormalized.length(), DELTA);
	
	assertEquals(0.225, vecNormalized.x, DELTA);
	assertEquals(0.5625, vecNormalized.y, DELTA);
	assertEquals(-0.5625, vecNormalized.z, DELTA);
	assertEquals(0.5625, vecNormalized.w, DELTA);
    }
    
    @Test
    public void testToString() {
	final Vector4f vector4d = new Vector4f(1.1f, 0f, -3.0f, -0.9f);
	
	assertEquals("(1.1, 0.0, -3.0, -0.9)", vector4d.toString());
    }
    
}
