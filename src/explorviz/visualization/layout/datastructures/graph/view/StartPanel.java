/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package explorviz.visualization.layout.datastructures.graph.view;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.JTextField;

import explorviz.visualization.layout.datastructures.graph.model.AdjMatrixGraph;

/**
 * 
 * @author Erich
 */
public class StartPanel extends JPanel {

	private BufferedImage img;
	private BufferedImage img2;
	private BufferedImage img3;
	public String text;
	Thread runner;
	int Pos = 360;
	GraphGui gui;
	JTextField input;
	static int n;
	AdjMatrixGraph adj;

	public StartPanel() {

		final Dimension d = new Dimension(800, 100);
		setPreferredSize(d);
		setBackground(Color.WHITE);

	}

	@Override
	public void paint(final Graphics g) {
		super.paint(g);
		try {
			img2 = ImageIO.read(getClass().getResource("Graph2.png"));
			img3 = ImageIO.read(getClass().getResource("matrix.png"));

		} catch (final IOException ex) {
			Logger.getLogger(StartPanel.class.getName()).log(Level.SEVERE, null, ex);
		}
		g.drawImage(img, 0, 70, this);
		g.drawImage(img3, 0, 390, this);
		g.drawImage(img2, 360, 70, this);

	}

}
