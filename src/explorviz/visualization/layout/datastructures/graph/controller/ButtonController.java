/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package explorviz.visualization.layout.datastructures.graph.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import explorviz.visualization.layout.datastructures.graph.model.AdjMatrixGraph;
import explorviz.visualization.layout.datastructures.graph.view.GraphGui;
import explorviz.visualization.layout.datastructures.graph.view.StartPanel;

/**
 * 
 * @author Erich
 */
public class ButtonController implements ActionListener {

	GraphGui gui;
	StartPanel panel;
	AdjMatrixGraph mod;
	int number;

	public ButtonController(final GraphGui gui) {
		this.gui = gui;

	}

	public ButtonController(final StartPanel aThis, final GraphGui gui) {
		panel = aThis;
		this.gui = gui;
	}

	@Override
	public void actionPerformed(final ActionEvent ae) {
		final int n;
		switch (ae.getActionCommand()) {
			case "Create random graph":
				final String s = JOptionPane.showInputDialog("Give in a number");
				final int num = Integer.parseInt(s);
				mod = new AdjMatrixGraph(num);
				gui.changePanelView(mod);
				break;
			case "Pagerank berechnen":
				break;
		}

	}
}
