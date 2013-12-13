package java.awt.geom;

public abstract class Rectangle2D {
    
    public static class Double extends Rectangle2D {
	
	public double y;
	public double x;
	public double width;
	public double height;
	
	public Double() {
	    x = 0d;
	    y = 0d;
	    width = 0d;
	    height = 0d;
	}
	
	public Double(double x, double y, double w, double h) {
	    this.x = x;
	    this.y = y;
	    width = w;
	    height = h;
	}
	
	public double getMinX() {
	    return x;
	}
	
	public double getMinY() {
	    return y;
	}
	
	public double getMaxX() {
	    return x + width;
	}
	
	public double getMaxY() {
	    return y + height;
	}
	
	public void setFrameFromDiagonal(double x1, double y1, double x2, double y2) {
	    if (x2 < x1) {
		final double t = x1;
		x1 = x2;
		x2 = t;
	    }
	    
	    if (y2 < y1) {
		final double t = y1;
		y1 = y2;
		y2 = t;
	    }
	    setFrame(x1, y1, x2 - x1, y2 - y1);
	}
	
	private void setFrame(double x, double y, double w, double h) {
	    this.x = x;
	    this.y = y;
	    width = w;
	    height = h;
	}
    }
    
    public abstract double getMinX();
    
    public abstract double getMinY();
    
    public abstract double getMaxX();
    
    public abstract double getMaxY();
    
    public abstract void setFrameFromDiagonal(double x1, double y1, double x2, double y2);
    
    public static void union(Rectangle2D src1, Rectangle2D src2, Rectangle2D dest) {
	final double x1 = Math.min(src1.getMinX(), src2.getMinX());
	final double y1 = Math.min(src1.getMinY(), src2.getMinY());
	final double x2 = Math.max(src1.getMaxX(), src2.getMaxX());
	final double y2 = Math.max(src1.getMaxY(), src2.getMaxY());
	dest.setFrameFromDiagonal(x1, y1, x2, y2);
    }
}
