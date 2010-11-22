package cider.client.gui;

import javax.swing.JFrame;

@SuppressWarnings("serial")
public class Launch extends JFrame
{
	//just throwing this together because nobody else has
	public static void main(String[] args) {
		new Launch();
	}
	
	Launch()
	{
		JFrame f = new JFrame("CIDEr - A Collaborative Coding Experience :)");
		BasicGUI g = new BasicGUI();
		g.setOpaque(true);
		f.add(g);
        f.setContentPane(g);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
        f.setResizable(false);       
        f.pack();		
	}
}