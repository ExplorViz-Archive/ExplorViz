/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package explorviz.visualization.layout.datastructures.graph.view;

import java.awt.*;

import javax.swing.*;

import explorviz.visualization.layout.datastructures.graph.model.AdjMatrixGraph;

/**
 * 
 * @author Erich
 */
public class CalculatorWindow extends JFrame {

	static AdjMatrixGraph adj;
	JPanel south;
	private static JTextArea outputArea;
	private JScrollPane sbar;
	static double[][] array = null;

	public CalculatorWindow(final AdjMatrixGraph mod) throws HeadlessException {

		adj = mod;
		array = mod.adjMatrix;
		initComponents();

	}

	public void initComponents() {
		setBounds(1000, 0, 0, 0);
		setSize(300, 1024);
		setVisible(true);
		setTitle("Output pagerank");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		south = new JPanel();
		outputArea = new JTextArea();
		outputArea.setBackground(Color.WHITE);
		sbar = new JScrollPane(outputArea);
		sbar.setPreferredSize(new Dimension(280, 950));
		south.add(sbar);
		getContentPane().add(south);
	}
	//

}