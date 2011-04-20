package cider.client.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

@SuppressWarnings("serial")
public class StatusBar extends JPanel {

	private JLabel lblUsername;
	private JLabel lblCurrentPos;
	private JLabel lblInputMode;
	
	public StatusBar()
	{
		GridLayout grid = new GridLayout(1,3);
		this.setLayout(grid);
		
		//this.setLayout(new BorderLayout());
		
		this.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));

		lblUsername = new JLabel("Logged in as: ");
		lblCurrentPos = new JLabel("Line: TODO | Col: TODO");
		lblCurrentPos.setHorizontalAlignment(SwingConstants.CENTER);
		lblInputMode = new JLabel("Input Mode: ");
		lblInputMode.setHorizontalAlignment(SwingConstants.RIGHT);
		
		this.add(lblUsername, BorderLayout.WEST);
		this.add(lblCurrentPos, BorderLayout.CENTER);
		this.add(lblInputMode, BorderLayout.EAST);
	}
	
	public void setInputMode(String mode)
	{
		lblInputMode.setText("Input Mode: " + mode);
	}
	
	public void setCurrentPos(int line, int col)
	{
		lblCurrentPos.setText("Line: "+ line + " | Col: " + col);
	}
	
	public void setUsername(String name)
	{
		lblUsername.setText("Logged in as: " + name);
	}	
}
