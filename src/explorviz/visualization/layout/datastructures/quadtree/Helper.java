package explorviz.visualization.layout.datastructures.quadtree;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

public class Helper {

	public double flaechenInhalt(final Rectangle2D rect) {
		return rect.getHeight() * rect.getWidth();
	}
	
	public double calcLength(double l1, double l2) {
		return l1+l2;
	}

	public Rectangle2D biggest(final ArrayList<Rectangle2D> objects) {
		Rectangle2D biggy = objects.get(0);
		for (int i = 0; i < (objects.size() - 1); i++) {
			if (flaechenInhalt(objects.get(i)) < flaechenInhalt(objects.get(i + 1))) {
				if (flaechenInhalt(biggy) < flaechenInhalt(objects.get(i + 1))) {
					biggy = objects.get(i + 1);
				}
			} else if (flaechenInhalt(objects.get(i)) > flaechenInhalt(biggy)) {
				biggy = objects.get(i);
			}
		}
		return biggy;
	}
	

    public int compare(Rectangle2D o1, Rectangle2D o2) {
        return (int) Double.compare(flaechenInhalt(o1), flaechenInhalt(o2));
    }

	public Rectangle2D biggestLooser(final ArrayList<Rectangle2D> objects) {
		Rectangle2D looser = objects.get(0);
		for (int i = 0; i < (objects.size() - 1); i++) {
			if (flaechenInhalt(objects.get(i)) < flaechenInhalt(objects.get(i + 1))) {
				if (flaechenInhalt(looser) > flaechenInhalt(objects.get(i))) {
					looser = objects.get(i);
				}
			} else if (flaechenInhalt(objects.get(i + 1)) < flaechenInhalt(looser)) {
				looser = objects.get(i + 1);
			}
		}
		return looser;
	}
}
