/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package explorviz.visualization.layout.datastructures.graph.view;

import java.awt.*;

import javax.swing.*;

import explorviz.visualization.layout.datastructures.graph.controller.ButtonController;
import explorviz.visualization.layout.datastructures.graph.model.AdjMatrixGraph;

/**
 * 
 * @author Erich
 */
public class GraphGui extends JFrame {

	private MatrixOutput ta;
	private LinksOutput ta2;
	private JMenuBar bar;
	private JButton rank;
	JPanel cards;
	CardLayout layout;
	StartPanel sp;
	GraphicsPanel drawPanel;

	public GraphGui(final AdjMatrixGraph mod) {

		super("Masterprojekt 2014");
		setSize(1120, 950);
		setResizable(false);
		// setLocation(300, 300);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		initComponent(mod);
	}

	public final void initComponent(final AdjMatrixGraph mod) {

		getContentPane().setLayout(new BorderLayout());
		ta2 = new LinksOutput();
		ta = new MatrixOutput();
		final JScrollPane spane = new JScrollPane(ta);
		final JScrollPane spane2 = new JScrollPane(ta2);
		spane.setPreferredSize(new Dimension(300, 0));
		final JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(360, HEIGHT / 2));
		panel.setBackground(Color.red);
		panel.setLayout(new GridLayout(2, 1));
		panel.add(spane2);
		panel.add(spane);
		getContentPane().add(panel, BorderLayout.EAST);
		rank = new JButton("CALCULATE RANK");
		getContentPane().add(rank, BorderLayout.SOUTH);
		bar = new JMenuBar();
		setJMenuBar(bar);
		bar.setBackground(new Color(193, 205, 193));
		final JPanel buttonPanel = new JPanel();

		final JButton b2 = new JButton("Create random graph");
		b2.setBounds(200, 200, 50, 50);
		b2.addActionListener(new ButtonController(this));
		final JButton b3 = new JButton("Calculator");
		b3.addActionListener(new ButtonController(this));

		buttonPanel.add(b2);
		buttonPanel.add(b3);
		// buttonPanel.add(b2);
		final JTextField input = new JTextField(0);
		input.setColumns(3);
		input.setBounds(200, 200, 50, 50);
		buttonPanel.add(input);
		getContentPane().add(buttonPanel, BorderLayout.NORTH);

		drawPanel = new GraphicsPanel(mod, ta, ta2);
		drawPanel.setVisible(false);
		sp = new StartPanel();
		cards = new JPanel();
		cards.add(sp);
		cards.add(drawPanel);
		cards.setSize(new Dimension(100, 100));
		getContentPane().add(cards, BorderLayout.CENTER);
		final String text = "This is a test Programm";
		final JLabel label = new JLabel(text);
		// getContentPane().add(label, BorderLayout.PAGE_START);
		layout = new CardLayout();
		cards.setLayout(layout);

	}

	public void changePanelView(final AdjMatrixGraph mod) {
		cards.remove(drawPanel);
		drawPanel = new GraphicsPanel(mod, ta, ta2);
		cards.add(drawPanel);
		layout.show(cards, "");
		// pw.setVisible(true);
		drawPanel.setVisible(true);
		layout.last(cards);
	}

}
