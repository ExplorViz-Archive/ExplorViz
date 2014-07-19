package explorviz.visualization.layout.datastructures.quadtree;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.*;

public class Zeichnen extends Frame {
	private final ArrayList<Rectangle2D> objects;

	public Zeichnen(final QuadTree tree) {
		setSize((int) tree.getBounds().getWidth(), (int) tree.getBounds().getHeight());
		setVisible(true);
		objects = tree.getObjectsBuh(tree);
		paintMist(getGraphics());
	}

	private ArrayList<Rectangle2D> getobjects() {
		return objects;
	}

	public void paintMist(final Graphics g) {
		final Graphics2D graphics2 = (Graphics2D) g;
		for (final Rectangle2D rec : objects) {
			graphics2.draw(rec);
		}
	}

	public static void main(final String[] args) {
		final Helper helper = new Helper();
		final ArrayList<Rectangle2D> liste = new ArrayList<Rectangle2D>();

		liste.add(new Rectangle(new Dimension(150, 150)));
		liste.add(new Rectangle(new Dimension(300, 300)));
		liste.add(new Rectangle(new Dimension(100, 100)));
		liste.add(new Rectangle(new Dimension(250, 250)));
		liste.add(new Rectangle(new Dimension(100, 100)));
		liste.add(new Rectangle(new Dimension(10, 10)));
		liste.add(new Rectangle(new Dimension(10, 10)));
		liste.add(new Rectangle(new Dimension(30, 30)));

		Collections.sort(liste, new Comparator<Rectangle2D>() {
			Helper help = new Helper();

			@Override
			public int compare(final Rectangle2D o1, final Rectangle2D o2) {
				return Double.compare(help.flaechenInhalt(o1), help.flaechenInhalt(o2));
			}
		});
		System.out.println(liste);
		double width = 0;
		double height = 0;
		for (final Rectangle2D calcRect : liste) {
			height += calcRect.getHeight();
			width += calcRect.getWidth();
		}

		double size = width;

		if (height > width) {
			size = height;
		}

		if (helper.biggest(liste).getWidth() >= helper.biggest(liste).getHeight()) {
			if (size < (2 * helper.biggest(liste).getWidth())) {
				size = 2 * helper.biggest(liste).getWidth();
			}
		} else {
			if (size < (2 * helper.biggest(liste).getHeight())) {
				size = 2 * helper.biggest(liste).getHeight();
			}
		}

		final QuadTree quad = new QuadTree(0, new Rectangle(0, 0, (int) size, (int) size));
		for (final Rectangle2D rect : liste) {
			quad.insert(quad, rect);
		}

		final Zeichnen brett = new Zeichnen(quad);
	}

}
