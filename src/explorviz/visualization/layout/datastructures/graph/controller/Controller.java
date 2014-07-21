/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package explorviz.visualization.layout.datastructures.graph.controller;

import java.awt.EventQueue;

import explorviz.visualization.layout.datastructures.graph.model.AdjMatrixGraph;
import explorviz.visualization.layout.datastructures.graph.view.GraphGui;

/**
 * 
 * @author Erich
 */
public class Controller {

	private final AdjMatrixGraph<?> mod;
	@SuppressWarnings("unused")
	private final GraphGui gui;
	String n;

	@SuppressWarnings("rawtypes")
	public Controller() {

		mod = new AdjMatrixGraph();
		gui = new GraphGui(mod);

	}

	public static void main(final String[] args) {
		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				new Controller();

			}
		});
	}
}
