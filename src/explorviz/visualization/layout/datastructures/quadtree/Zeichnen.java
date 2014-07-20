package explorviz.visualization.layout.datastructures.quadtree;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.*;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Zeichnen extends JPanel {
	private final ArrayList<Rectangle2D> objects;
	JPanel panel;

	public Zeichnen(final QuadTree tree) {
		final JFrame frame = new JFrame();
		frame.setSize((int) tree.getBounds().getWidth(), (int) tree.getBounds().getHeight());
		frame.setVisible(true);
		frame.setContentPane(this);
		objects = tree.getObjectsBuh(tree);
		frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
		this.setSize((int) tree.getBounds().getWidth(), (int) tree.getBounds().getHeight());
	}

	@Override
	public void paintComponent(final Graphics g) {
		super.paintComponent(g);
		paintMist(g);
	}

	private ArrayList<Rectangle2D> getobjects() {
		return objects;
	}

	public void paintMist(final Graphics g) {
		final Graphics2D graphics2 = (Graphics2D) g;
		for (final Rectangle2D rec : objects) {
			System.out.println(rec);
			graphics2.draw(rec);
		}
	}

	public static void main(final String[] args) {
		final Helper helper = new Helper();
		final ArrayList<Rectangle2D> liste = new ArrayList<Rectangle2D>();

		liste.add(new Rectangle(new Dimension(50, 50)));
		liste.add(new Rectangle(new Dimension(75, 75)));
		liste.add(new Rectangle(new Dimension(100, 100)));
		liste.add(new Rectangle(new Dimension(300, 300)));
		liste.add(new Rectangle(new Dimension(100, 100)));
		liste.add(new Rectangle(new Dimension(150, 150)));
		liste.add(new Rectangle(new Dimension(10, 10)));
		liste.add(new Rectangle(new Dimension(10, 10)));
		liste.add(new Rectangle(new Dimension(40, 40)));
		liste.add(new Rectangle(new Dimension(10, 10)));
		liste.add(new Rectangle(new Dimension(10, 10)));
		liste.add(new Rectangle(new Dimension(30, 30)));
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
				System.out.println("Ich bin jetzt");
			}
		} else {
			if (size < (2 * helper.biggest(liste).getHeight())) {
				size = 2 * helper.biggest(liste).getHeight();
				System.out.println("Ich bin hier");
			}
		}
		System.out.println("size " + size);
		final QuadTree quad = new QuadTree(0, new Rectangle(0, 0, (int) size, (int) size));
		for (final Rectangle2D rect : liste) {
			quad.insert(quad, rect);
		}

		final Zeichnen brett = new Zeichnen(quad);
	}

}
