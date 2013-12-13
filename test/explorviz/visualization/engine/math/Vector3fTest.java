package explorviz.visualization.engine.math;

import static org.junit.Assert.*;

import org.junit.Test;

import explorviz.visualization.engine.math.Vector3f;

public class Vector3fTest {
    
    private static final float DELTA = 0.001f;
    
    @Test
    public void testVector3f() {
	final Vector3f Vector3f = new Vector3f();
	
	assertEquals(0, Vector3f.x, DELTA);
	assertEquals(0, Vector3f.y, DELTA);
	assertEquals(0, Vector3f.z, DELTA);
    }
    
    @Test
    public void testVector3fVector3f() {
	final Vector3f src = new Vector3f();
	final Vector3f newvector = new Vector3f(src);
	
	assertEquals(src.x, newvector.x, DELTA);
	assertEquals(src.y, newvector.y, DELTA);
	assertEquals(src.z, newvector.z, DELTA);
    }
    
    @Test
    public void testVector3fDoubleDoubleDouble() {
	final Vector3f Vector3f = new Vector3f(1.1f, 0f, -3.0f);
	
	assertEquals(1.1, Vector3f.x, DELTA);
	assertEquals(0, Vector3f.y, DELTA);
	assertEquals(-3.0, Vector3f.z, DELTA);
    }
    
    @Test
    public void testAdd() {
	final Vector3f base = new Vector3f(2.0f, -5.0f, 0.0f);
	final Vector3f summand = new Vector3f(1.0f, 2.0f, -3.0f);
	
	final Vector3f sum = base.add(summand);
	
	assertEquals(3.0, sum.x, DELTA);
	assertEquals(-3.0, sum.y, DELTA);
	assertEquals(-3.0, sum.z, DELTA);
    }
    
    @Test
    public void testSub() {
	final Vector3f base = new Vector3f(2.0f, -5.0f, 0.0f);
	final Vector3f submand = new Vector3f(1.0f, 2.0f, -3.0f);
	
	final Vector3f result = base.sub(submand);
	
	assertEquals(1.0, result.x, DELTA);
	assertEquals(-7.0, result.y, DELTA);
	assertEquals(3.0, result.z, DELTA);
    }
    
    @Test
    public void testNegate() {
	final Vector3f negated = new Vector3f(1.1f, 0f, -3.0f).negate();
	
	assertEquals(-1.1, negated.x, DELTA);
	assertEquals(0, negated.y, DELTA);
	assertEquals(3.0, negated.z, DELTA);
    }
    
    @Test
    public void testScale() {
	final Vector3f scaled3 = new Vector3f(1.1f, 0f, -3.0f).scale(3);
	
	assertEquals(3.3, scaled3.x, DELTA);
	assertEquals(0, scaled3.y, DELTA);
	assertEquals(-9.0, scaled3.z, DELTA);
	
	final Vector3f scaled1 = new Vector3f(1.1f, 0f, -3.0f).scale(1);
	
	assertEquals(1.1, scaled1.x, DELTA);
	assertEquals(0, scaled1.y, DELTA);
	assertEquals(-3.0, scaled1.z, DELTA);
	
	final Vector3f scaled0 = new Vector3f(1.1f, 0f, -3.0f).scale(0);
	
	assertEquals(0, scaled0.x, DELTA);
	assertEquals(0, scaled0.y, DELTA);
	assertEquals(-0, scaled0.z, DELTA);
	
	final Vector3f scaledMinus3 = new Vector3f(1.1f, 0f, -3.0f).scale(-3.0f);
	
	assertEquals(-3.3, scaledMinus3.x, DELTA);
	assertEquals(0, scaledMinus3.y, DELTA);
	assertEquals(9.0, scaledMinus3.z, DELTA);
    }
    
    @Test
    public void testDiv() {
	final Vector3f divided3 = new Vector3f(1.2f, 0f, -3.0f).div(3);
	
	assertEquals(0.4, divided3.x, DELTA);
	assertEquals(0, divided3.y, DELTA);
	assertEquals(-1.0, divided3.z, DELTA);
	
	final Vector3f divided1 = new Vector3f(1.1f, 0f, -3.0f).div(1);
	
	assertEquals(1.1, divided1.x, DELTA);
	assertEquals(0, divided1.y, DELTA);
	assertEquals(-3.0, divided1.z, DELTA);
	
	final Vector3f scaledMinus3 = new Vector3f(1.1f, 0f, -3.0f).div(-3.0f);
	
	assertEquals(-0.36666, scaledMinus3.x, DELTA);
	assertEquals(0, scaledMinus3.y, DELTA);
	assertEquals(1.0, scaledMinus3.z, DELTA);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testDivBy0() {
	final Vector3f divided0 = new Vector3f(1.1f, 0f, -3.0f).div(0);
	
	assertEquals(0, divided0.x, DELTA);
	assertEquals(0, divided0.y, DELTA);
	assertEquals(-0, divided0.z, DELTA);
    }
    
    @Test
    public void testDot() {
	final double dotProd = new Vector3f(1.1f, 0f, -3.0f).dot(new Vector3f());
	
	assertEquals(0, dotProd, DELTA);
	
	final double dotProd2 = new Vector3f(1.1f, 0f, -3.0f).dot(new Vector3f(1.1f, 0f, -3.0f));
	
	assertEquals(10.21, dotProd2, DELTA);
    }
    
    @Test
    public void testCross() {
	final Vector3f crossProd = new Vector3f(1.1f, 0f, -3.0f).cross(new Vector3f());
	
	assertEquals(0, crossProd.x, DELTA);
	assertEquals(0, crossProd.y, DELTA);
	assertEquals(-0, crossProd.z, DELTA);
	
	final Vector3f crossProd2 = new Vector3f(1.1f, 0f, -3.0f).cross(new Vector3f(1.1f, 0f,
		-3.0f));
	
	assertEquals(0, crossProd2.x, DELTA);
	assertEquals(0, crossProd2.y, DELTA);
	assertEquals(-0, crossProd2.z, DELTA);
	
	final Vector3f crossProd3 = new Vector3f(1.1f, 0f, -3.0f)
		.cross(new Vector3f(1.1f, 0f, 3.0f));
	
	assertEquals(0, crossProd3.x, DELTA);
	assertEquals(-6.6, crossProd3.y, DELTA);
	assertEquals(0, crossProd3.z, DELTA);
	
    }
    
    @Test
    public void testLength() {
	final Vector3f vec0 = new Vector3f();
	assertEquals(0, vec0.length(), DELTA);
	
	final Vector3f vec1 = new Vector3f(1, 1, 1);
	assertEquals(1.732, vec1.length(), DELTA);
    }
    
    @Test
    public void testNormalize() {
	final Vector3f vec = new Vector3f(1.2f, 3f, -3.0f);
	assertEquals(4.409, vec.length(), DELTA);
	
	final Vector3f vecNormalized = vec.normalize();
	
	assertEquals(1.0, vecNormalized.length(), DELTA);
	
	assertEquals(0.272, vecNormalized.x, DELTA);
	assertEquals(0.68, vecNormalized.y, DELTA);
	assertEquals(-0.68, vecNormalized.z, DELTA);
    }
    
    @Test
    public void testToString() {
	final Vector3f Vector3f = new Vector3f(1.1f, 0f, -3.0f);
	
	assertEquals("(1.1, 0.0, -3.0)", Vector3f.toString());
    }
    
}
