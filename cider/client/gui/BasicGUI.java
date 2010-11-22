package cider.client.gui;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;

@SuppressWarnings("serial")
public class BasicGUI extends JPanel implements ActionListener, KeyListener
{
	public static String fileName = "NewFile.java";
	public static String fileContents;
	
    public BasicGUI()
    {
    	super(new GridLayout(1,2));
    	
    	JTextArea text = new JTextArea("class Hello{\n\tpublic static void main(String[] args) \n\t{\n\t\tSystem.out.println(\"hello\");\n\t}\n}");
        text.setRows(20);
        text.setBounds(0, 0, 10, 50);
        text.setBorder(BorderFactory.createLineBorder(new Color(2), 2));
        text.addKeyListener(this);
        
        this.add(text);

        JPanel p = new JPanel();
        
        JTextField filename = new JTextField(10);
        filename.addKeyListener(this);
        filename.setBounds(1, 0, 20, 10);
        
        JButton quickPush = new JButton("DEV: quickPush");
        quickPush.addActionListener(this);
        quickPush.setBounds(1, 1, 50, 50);
                
        p.add(quickPush);
        p.add(filename);
        
        add(p);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (e.getActionCommand() == "DEV: quickPush")
        {
            File f = new File (fileName);
            if (!f.exists())
            {
				try 
				{
					f.createNewFile();
		            FileWriter fstream = new FileWriter(fileName);
		            BufferedWriter out = new BufferedWriter(fstream);
		            out.write(fileContents);
		            out.close();
				} 
				catch (IOException e1) 
				{
					e1.printStackTrace();
				}
            }
            
        }
    }

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		if (key == KeyEvent.VK_ENTER)
		{
			try
			{
				JTextField temp = (JTextField) e.getSource();
	
				fileName = temp.getText();
				System.out.println(fileName);
			}
			catch (Exception ex)
			{
				JTextArea temp = (JTextArea) e.getSource();
				fileContents = temp.getText();
				System.out.println(fileContents);
			}
		}		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
    
    

}