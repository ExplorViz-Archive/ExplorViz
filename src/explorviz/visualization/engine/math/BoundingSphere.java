package explorviz.visualization.engine.math;

public class BoundingSphere {
    public Vector3f center;
    public float    radius;
    
    public BoundingSphere(Vector3f center, float radius) {
	this.center = center;
	this.radius = radius;
    }
    
    public boolean isVisible(Plane[] frustum) {
	for (final Plane p : frustum) {
	    if (p.dot(center) < -radius) {
		return false;
	    }
	}
	return true;
    }
    
    public double rayCast(Ray ray) {
	final Vector3f o = shiftSphereToCenter(ray);
	
	// compute coefficients
	final double a = ray.dir.length() * ray.dir.length();
	final double b = 2 * o.dot(ray.dir);
	final double c = (o.length() * o.length()) - (radius * radius);
	
	// solve quadratic equation, if possible
	final double disc = (b * b) - (4 * a * c);
	if (disc < 0) {
	    return Double.POSITIVE_INFINITY; // no intersection
	}
	// avoid poor numeric precision when b is similar to sqrt(disc)
	final double q = (-b + ((b < 0 ? 1 : -1) * Math.sqrt(disc))) * 0.5;
	final double t0 = q / a;
	final double t1 = c / q;
	
	return returnSmallestPositiveSolution(t0, t1);
    }
    
    private Vector3f shiftSphereToCenter(Ray ray) {
	final Vector3f o = ray.origin.sub(center);
	return o;
    }
    
    private double returnSmallestPositiveSolution(double t0, double t1) {
	if (t0 > t1) {
	    final double tmp = t0;
	    t0 = t1;
	    t1 = tmp;
	}
	if (t1 < 0) {
	    return Double.POSITIVE_INFINITY; // no intersection
	} else if (t0 < 0) {
	    return t1;
	} else {
	    return t0;
	}
    }
}