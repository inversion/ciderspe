package cider.client.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import cider.common.network.Client;
import cider.common.network.Server;

class MainWindow implements Runnable
{
    JFrame w;
    public String currentFileName = "newfile.java";
    public String currentFileContents;
    Client client;
    Server server;
    
    public static void main(String[] args)
    {
        MainWindow program = new MainWindow();
        SwingUtilities.invokeLater(program);
    }

    public static void addMenuItem(JMenu menu, String name, int keyEvent, ActionListener a)
    {
        JMenuItem menuItem = new JMenuItem(name);
        menuItem.addActionListener(a);
        if (keyEvent != -1)
            menuItem.setAccelerator(KeyStroke.getKeyStroke(keyEvent,
                    ActionEvent.CTRL_MASK));
        menu.add(menuItem);
    }
    
    public ActionListener newAction()
    {
    	ActionListener AL = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String action = e.getActionCommand();
				if (action.equals("Quit"))
				{
					int response;
		            response = JOptionPane.showConfirmDialog(null, "Are you sure you wish to quit without saving?");
		            if (response == 0)
		            	System.exit(0);
					
				}
				else if (action.equals("Save file locally") || action.equals("Save") || action.equals("Save As"))
				{
					if (currentFileName.equals("newfile.java") || action.equals("Save As"))
					{
						String response = JOptionPane.showInputDialog(null,
							  "Enter new file name",
							  currentFileName,
							  JOptionPane.QUESTION_MESSAGE);
						currentFileName = response;
					}
		            File f = new File (currentFileName);
		            if (!f.exists())
		            {
						try 
						{
							f.createNewFile();
				            FileWriter fstream = new FileWriter(currentFileName);
				            BufferedWriter out = new BufferedWriter(fstream);
				            out.write(currentFileContents);
				            out.close();
						} 
						catch (IOException e1) 
						{
							e1.printStackTrace();
						}
		            }
				}

			}
    	};
    	return AL;
    }
    
    public KeyListener newKeyListener()
    {
    	KeyListener k;
    	k = new KeyListener(){

			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void keyPressed(KeyEvent e) {
				int key = e.getKeyCode();
				if (key == KeyEvent.VK_ENTER)
				{
					JTextArea temp = (JTextArea) e.getSource();
					currentFileContents = temp.getText();
					System.out.println(currentFileContents);
				}		
			}

			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
    		
    	};
    	return k;
    }

    public JMenuBar mainMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu;

        // menu 1
        menu = new JMenu("File");
        menuBar.add(menu);
        
        ActionListener aL = newAction();

        addMenuItem(menu, "New", KeyEvent.VK_N, aL);
        addMenuItem(menu, "Open", KeyEvent.VK_O, aL);
        addMenuItem(menu, "Save", KeyEvent.VK_S, aL);
        addMenuItem(menu, "Save As", -1, aL);
        addMenuItem(menu, "Quit", KeyEvent.VK_Q, aL);

        // menu 2
        menu = new JMenu("Edit");
        menuBar.add(menu);

        addMenuItem(menu, "Cut", KeyEvent.VK_X, aL);
        addMenuItem(menu, "Copy", KeyEvent.VK_C, aL);
        addMenuItem(menu, "Paste", KeyEvent.VK_V, aL);

        // menu 3
        menu = new JMenu("Help");
        menuBar.add(menu);

        addMenuItem(menu, "About", -1, aL);
        
        //the DEV(eloper) menu is for us to test back-end things such as saving and pushing
        //NYI = not yet implemented
        menu = new JMenu("DEV");
        menuBar.add(menu);
        
        addMenuItem(menu, "Save file locally", -1, aL);
        addMenuItem(menu, "Push file to server (NYI)", -1, aL);
        addMenuItem(menu, "Get file list from server (NYI)", -1, aL);
        addMenuItem(menu, "Pull item from server (NYI)", -1, aL);
        
        

        return menuBar;
    }

    public JPanel sourceEditor()
    {
        JPanel panel = new JPanel(new BorderLayout());
        // text area
        JTextArea textArea = new JTextArea(
        		"class Hello{\n\tpublic static void main(String[] args) \n\t{\n\t\tSystem.out.println(\"hello\");\n\t}\n}");
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setPreferredSize(new Dimension(250, 250));
        textArea.addKeyListener(newKeyListener());
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        panel.add(scrollPane);
        return panel;
    }

    public JPanel sourceEditorSection()
    {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Tab 1", sourceEditor());
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(640, 480));
        panel.add(tabbedPane);
        return panel;
    }

    public JPanel mainArea()
    {
    	DirectoryViewComponent dirView = new DirectoryViewComponent();
		server = new Server();
		client = new Client( dirView );
		client.getFileList();
    	
        JPanel panel = new JPanel(new BorderLayout());
        JSplitPane splitPane;
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                dirView/* label */,
                this.sourceEditorSection());// textArea/*label2*/);
        JSplitPane splitPane2;
        splitPane2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, splitPane,
                new JLabel("chat"));

        splitPane.setOneTouchExpandable(true);
        // splitPane.setDividerLocation(150);
        splitPane2.setOneTouchExpandable(true);
        // splitPane2.setDividerLocation(150);

        // Provide minimum sizes for the two components in the split pane
        Dimension minimumSize = new Dimension(100, 50);
        panel.add(splitPane2);
        return panel;
    }

    public void run()
    {
        w = new JFrame("CIDEr");
        w.setDefaultCloseOperation(w.EXIT_ON_CLOSE);

        URL x = this.getClass().getResource("icon.png");
        ImageIcon image3 = new ImageIcon(x);
        Image test3 = image3.getImage();
        w.setIconImage(test3);

        JPanel p = new JPanel(new BorderLayout());
        p.add(this.mainMenuBar(), BorderLayout.PAGE_START);
        p.add(this.mainArea());

        w.add(p);

        w.pack();
        w.setLocationByPlatform(true);
        w.setVisible(true);
    }

    public class Error
    {
        public void errorMessage(String message, String title)
        {
            JOptionPane.showMessageDialog(w, message, title,
                    JOptionPane.ERROR_MESSAGE);
            // JOptionPane.showMessageDialog(w, "OMG you broken somethings.",
            // "You fail", JOptionPane.ERROR_MESSAGE);
        }
    }
}
