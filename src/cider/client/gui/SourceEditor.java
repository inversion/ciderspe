package cider.client.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * This class basically means that the TextAreas can constantly update 
 * and hold lots of data (which is easier than using an ArrayList in
 * MainWindow.java :3
 * @author Jon
 *
 */
@SuppressWarnings("serial")
public class SourceEditor extends JPanel {
	String fileContents;
	String fileDirectory;
	
    public KeyListener newKeyListener()
    {
    	KeyListener k;
    	k = new KeyListener(){

			@Override
			public void keyTyped(KeyEvent e) {
				JTextArea temp = (JTextArea) e.getSource();
				fileContents = temp.getText();
				System.out.println(fileContents);				
			}

			@Override
			//on every keypress, the string containing the document in its entirety updates
			public void keyPressed(KeyEvent e) {
	
			}

			@Override
			public void keyReleased(KeyEvent e) {
				
			}
    		
    	};
    	return k;
    }
	
	public SourceEditor (String input){
		super (new BorderLayout());
		// text area
	    final JTextArea textArea = new JTextArea(input);
	    JScrollPane scrollPane = new JScrollPane(textArea);
	    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	    scrollPane.setPreferredSize(new Dimension(250, 250));
	    textArea.addKeyListener(newKeyListener());
	    textArea.setLineWrap(true);
	    textArea.setWrapStyleWord(true);
	    this.add(scrollPane);
	    this.addComponentListener(new ComponentListener(){
	
			@Override
			public void componentResized(ComponentEvent e) {
				
			}
	
			@Override
			public void componentMoved(ComponentEvent e) {
				
			}
	
			@Override
			public void componentShown(ComponentEvent e) {
				fileContents = textArea.getText();
			}
	
			@Override
			public void componentHidden(ComponentEvent e) {
			}
	    	
	    });
	}
}
