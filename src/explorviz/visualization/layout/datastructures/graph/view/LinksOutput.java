/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package explorviz.visualization.layout.datastructures.graph.view;

import java.awt.*;

import javax.swing.JTextArea;

/**
 * 
 * @author Erich
 */
public class LinksOutput extends JTextArea {

	public LinksOutput() {
		setMargin(new Insets(30, 50, 50, 0));
		setBackground(new Color(255, 255, 210));
		setFont(new Font("Serif", Font.BOLD, 14));
		setText("OUTPUT LINKS : \n");

	}
}
