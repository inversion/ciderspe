package cider.client.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Timer;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import org.jivesoftware.smack.XMPPException;

import cider.common.network.Client;
import cider.common.processes.Profile;

class MainWindow implements Runnable
{
    JTabbedPane tabbedPane = new JTabbedPane();
    JFrame w;
    public String currentDir = System.getProperty("user.dir");
    public String currentFileName = "Unsaved Document 1";
    public String currentFileContents = "";
    public int currentTab = 0;
    
    Client client;
    private JSplitPane dirSourceEditorSeletionSplit;
    private JSplitPane editorChatSplit;
    private Hashtable<String, SourceEditor> openTabs = new Hashtable<String, SourceEditor>();
    private DirectoryViewComponent dirView;
    private String username;
    
    public JList userList;
    public JLabel userCount = new JLabel();
    public DefaultListModel userListModel = new DefaultListModel();
    public JTextArea messageSendBox;
    public JTextArea/*JEditorPane*/ messageReceiveBox = new JTextArea();
    
    /**
     * These variable are for the profiles
     * @author Jon
     */
    public long startTime;
    private Profile myProfile;
    
    
    // Main method and no parameter constructor for running without login box
    public static void main( String[] args )
    {
    	MainWindow main = null;
    	try {
			main = new MainWindow();
		} catch (XMPPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SwingUtilities.invokeLater( main );
    }
     
    MainWindow( ) throws XMPPException
    {
    	myProfile = new Profile (username);
    	startTime = System.currentTimeMillis();
        dirView = new DirectoryViewComponent();
        username = "ciderclient";
        client = new Client( dirView, tabbedPane, openTabs, userListModel, userCount, messageReceiveBox, username, "clientpw", "xmpp.org.uk", 5222, "xmpp.org.uk" );
        dirView.setClient(client);
        client.getFileList();
    }
    
    MainWindow( String username, String password, String host, int port, String serviceName ) throws XMPPException
    {
        // TODO: Should more stuff be in the constructor rather than the mainArea method? The variables look a bit of a mess
        dirView = new DirectoryViewComponent();
    	myProfile = new Profile (username);
        this.username = username;
        client = new Client( dirView, tabbedPane, openTabs, userListModel, userCount, messageReceiveBox, username, password, host, port, serviceName );
        // No need to put this. on tabbedPane and openTabs unless variable in current scope is overriding?
        dirView.setClient(client);
        client.getFileList();
    }

    public static void addMenuItem(JMenu menu, String name, int keyEvent,
            ActionListener a)
    {
        JMenuItem menuItem = new JMenuItem(name);
        menuItem.addActionListener(a);
        if (keyEvent != -1)
            menuItem.setAccelerator(KeyStroke.getKeyStroke(keyEvent,
                    ActionEvent.CTRL_MASK));
        menu.add(menuItem);
    }

    @Deprecated
    public void openFile()
    {
        JFileChooser fc = new JFileChooser();
        int rVal = fc.showOpenDialog(null);
        if (rVal == JFileChooser.APPROVE_OPTION)
        {
            String temp;
            currentDir = fc.getSelectedFile().getAbsolutePath();
            currentFileName = fc.getSelectedFile().getName();
            try
            {
                FileInputStream fis = new FileInputStream(currentDir);
                BufferedInputStream bis = new BufferedInputStream(fis);
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        bis));
                currentFileContents = "";
                while ((temp = br.readLine()) != null)
                {
                    currentFileContents = currentFileContents + temp + "\n";
                }
            }
            catch (IOException e)
            {
                System.err.println("Error: " + e.getMessage());
                System.exit(0);
            }

            // tabbedPane.addTab(currentFileName, new SourceEditor(
            // currentFileContents, currentDir));
            tabbedPane.setSelectedIndex(++currentTab);
        }
    }

    @Deprecated
    public void saveFile(String action)
    {
        JFileChooser fc = new JFileChooser();
        if (currentFileName.equals("Unsaved Document 1")
                || action.equals("Save As"))
        {
            int watdo = fc.showSaveDialog(null);
            if (watdo != JFileChooser.APPROVE_OPTION)
            {
                return;
            }
            currentFileName = fc.getSelectedFile().getName();
            currentDir = fc.getSelectedFile().getAbsolutePath();
        }
        try
        {
            FileWriter fstream = new FileWriter(currentDir);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(currentFileContents);
            out.close();
        }
        catch (IOException e1)
        {
            System.err.println("Error: " + e1.getMessage());
        }
        tabbedPane.setTitleAt(currentTab, currentFileName);
    }

    @Deprecated
    public void closeFile(String action)
    {
        saveFile(action);
        // closes tab regardless of save or cancel
        tabbedPane.remove(tabbedPane.getSelectedIndex());
        tabbedPane.setSelectedIndex(--currentTab);
    }

    @Deprecated
    public void newFile()
    {
        // closes tab regardless of save or cancel
        // tabbedPane.addTab("Unsaved Document 1", new SourceEditor("", "\\."));
        // tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
    }

    public ActionListener newAction()
    {
        ActionListener AL = new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                String action = e.getActionCommand();
                if (action.equals("Quit"))
                {
                    int response;
                    response = JOptionPane.showConfirmDialog(null,
                            "Are you sure you wish to quit without saving?");
                    if (response == 0)
                        System.exit(0);

                }
                else if (action.equals("My Profile"))
                {
                	showMyProfile();
                }
                else if (action.equals("Reset My Profile"))
                {
                	restartProfile();
                }
                else if (action.equals("Close File"))
                {
                    closeFile(action);
                }
                else if (action.equals("Open"))
                {
                    openFile();
                }
                else if (action.equals("New"))
                {
                    newFile();
                }
                else if (action.equals("Save") || action.equals("Save As"))
                {
                    saveFile(action);
                }
                else if (action.equals("Send"))
                {
                	messageSendBox.setText("boogaloo");
                }
                else if (action.equals("Logout"))
                {
                	LoginUI.login.setVisible(true);
                	w.setVisible(false);
                }
                else if (action.equals("About"))
                {
                	showAbout();
                }
                else if (action.equals("Update Profile"))
                {
                	myProfile.updateTimeSpent(startTime);
                	startTime = System.currentTimeMillis();
                	myProfile.updateProfileInfo();
                }

            }

			private void restartProfile() 
			{
                int response = JOptionPane.showConfirmDialog(null,
                        "Are you sure you wish to reset your lovely profile?");
                if (response == 0)
                {
					File f = new File (username + ".txt");
					try 
					{
						if (!f.exists())
							System.out.println("Error: how the hell did that happen"); //TODO: should probably remove ;p
						else
						{
							f.delete();
							f.createNewFile();
							FileWriter fw = new FileWriter(f);
							BufferedWriter out = new BufferedWriter(fw);
							System.out.println("Profile has been reset, new credentials are:");
							System.out.println(username + "\n" + "chars: 0\ntimespent: 0\nlastonline: Never!");
							out.write(username + "\n" + "chars: 0\ntimespent: 0\nlastonline: Never!");
							out.close();
						}
					} 
					catch (IOException e) 
					{
						e.printStackTrace();
					}
					myProfile = new Profile(username);
					startTime = System.currentTimeMillis();
                }
                else
                	return;
			}
        };
        return AL;
    }
    
    private void showMyProfile()
    {
    	System.out.println(myProfile.timeSpent);
    	myProfile.updateTimeSpent(startTime);
    	System.out.println(myProfile.timeSpent);
    	startTime = System.currentTimeMillis();
    	
    	JFrame profileFrame = new JFrame("My Profile- " + username);
    	Container content = profileFrame.getContentPane();
    	content.setLayout(new GridLayout(10, 2));
    	
    	URL x = this.getClass().getResource("icon.png");
        ImageIcon image = new ImageIcon(x);
        Image test = image.getImage();
        profileFrame.setIconImage(test);
    	
    	profileFrame.setBounds(100, 100, 400, 350);
    	profileFrame.setResizable(false);
    	//profileFrame.pack();
    	profileFrame.show();
    	profileFrame.setLocationRelativeTo(null);
    	
    	JLabel uName = new JLabel("Username: " + username);
    	uName.setHorizontalAlignment(JLabel.LEFT);
    	uName.setVerticalAlignment(JLabel.TOP);
    	content.add(uName);
    	
//    	JLabel uPwd = new JLabel("Password: " + client.CLIENT_USERNAME);
//    	uPwd.setHorizontalAlignment(JLabel.LEFT);
//    	uPwd.setVerticalAlignment(JLabel.TOP);
//    	content.add(uPwd);
    	
    	JLabel chars = new JLabel("Characters pressed: " + myProfile.typedChars);
    	chars.setHorizontalAlignment(JLabel.LEFT);
    	chars.setVerticalAlignment(JLabel.TOP);
    	content.add(chars);
    	
    	Time t = new Time(myProfile.timeSpent);
    	System.out.println("TS = " + myProfile.timeSpent);
    	JLabel time = new JLabel("Total time spent: " + t);
    	time.setHorizontalAlignment(JLabel.LEFT);
    	time.setVerticalAlignment(JLabel.TOP);
    	content.add(time);
    	
    	JLabel lastonline = new JLabel("You were last seen: " + myProfile.lastOnline);
    	lastonline.setHorizontalAlignment(JLabel.LEFT);
    	lastonline.setVerticalAlignment(JLabel.TOP);
    	content.add(lastonline);
    	
    	profileFrame.setVisible(true);
    }
    
    private void showAbout()
    {
    	JFrame frame = new JFrame("About CIDEr");
    	Container content = frame.getContentPane();
    	content.setLayout(new GridLayout(10, 2));
    	
    	URL x = this.getClass().getResource("icon.png");
        ImageIcon image = new ImageIcon(x);
        Image test = image.getImage();
        frame.setIconImage(test);
    	
    	frame.setBounds(100, 100, 400, 350);
    	frame.setResizable(false);
    	frame.show();
    	frame.setLocationRelativeTo(null);
    	
    	JLabel chars = new JLabel("CIDEr- Collaborative Integrated Development EnviRonment.");
    	content.add(chars);
    	
    	frame.setVisible(true);
    }

    private void tabClicked(MouseEvent e)
    {
        if (e.getButton() != MouseEvent.BUTTON1 && e.getClickCount() == 1)
        { // if is right-click

            // create popup with Close menuitem
            JPopupMenu popupMenu = new JPopupMenu();
            JMenuItem closeBtn = new JMenuItem("Close");
            closeBtn.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    SwingUtilities.invokeLater(new Runnable()
                    {
                        public void run()
                        {
                            closeFile("Close File");
                        }
                    });
                }
            });
            popupMenu.add(closeBtn);

            // display popup near location of mouse click
            popupMenu.show(e.getComponent(), e.getX(), e.getY() - 10);
        }
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
        addMenuItem(menu, "Save As", KeyEvent.VK_A, aL);
        addMenuItem(menu, "Close File", KeyEvent.VK_F4, aL);
        addMenuItem(menu, "Logout", KeyEvent.VK_L, aL);
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
        
        //menu 4
        menu = new JMenu("Profile");
        menuBar.add(menu);
        
        addMenuItem(menu, "My Profile", -1, aL);
        addMenuItem(menu, "Reset My Profile", -1, aL);
        
        // the DEV(eloper) menu is for us to test back-end things such as saving
        // and pushing
        // NYI = not yet implemented
        menu = new JMenu("DEV");
        menuBar.add(menu);

        addMenuItem(menu, "Push file to server (NYI)", -1, aL);
        addMenuItem(menu, "Update Profile", -1, aL);
        addMenuItem(menu, "Pull item from server (NYI)", -1, aL);

        return menuBar;
    }

    public JPanel sourceEditorSection()
    {
        // tabbedPane.addTab(currentFileName, new SourceEditor(
        // currentFileContents, currentDir));
        // tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(640, 480));
        panel.add(this.tabbedPane);
        return panel;
    }

    public JPanel pnlUsers()
    {
    	/*panel for the list of online users*/
    	JPanel panel = new JPanel(new BorderLayout());

    	Border emptyBorder = BorderFactory.createEmptyBorder();

    	userList = new JList(userListModel);
    	userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    	/*for (int i=0; i < userList.getModel().getSize(); i++) 
    	{
    	    Object item = userList.getModel().getElementAt(i);
    	    userList.setForeground(Color.red); //TODO looking at using different colours for each user    	    
    	}*/
    	
    	/*this can be used to initiate a private chat- Alex*/
    	 MouseListener mouseListener = new MouseAdapter() {
    	     public void mouseClicked(MouseEvent e) {
    	         if (e.getClickCount() == 2) {
    	             int i = userList.locationToIndex(e.getPoint());
    	             System.out.println("Double clicked on Item " + i);
    	             System.out.println("Double clicked on Item: " + userList.getModel().getElementAt(i));  
    	             //initiate.private.chat.with(userList.getModel().getElementAt(i));
    	          }
    	         else if ((e.getButton() == MouseEvent.BUTTON3) && (userList.locationToIndex(e.getPoint()) != -1))
    	         {
    	        	 /*pop up for viewing users profile/stats etc");*/
    	        	 userList.setSelectedIndex(userList.locationToIndex(e.getPoint()));
    	        	 
    	             JPopupMenu popupMenu = new JPopupMenu();
    	             JMenuItem chat = new JMenuItem("Chat with User");
    	             JMenuItem showProfile = new JMenuItem("Show User's Profile");
    	             chat.addActionListener(new ActionListener()
    	             {
    	                 public void actionPerformed(ActionEvent e)
    	                 {
    	                     SwingUtilities.invokeLater(new Runnable()
    	                     {
    	                         public void run()
    	                         {
    	                        	//initiate.private.chat.with(userList.getModel().getElementAt(i));
    	                         }
    	                     });
    	                 }
    	             });
    	             popupMenu.add(chat);
    	             
    	             showProfile.addActionListener(new ActionListener()
    	             {
    	                 public void actionPerformed(ActionEvent e)
    	                 {
    	                     SwingUtilities.invokeLater(new Runnable()
    	                     {
    	                         public void run()
    	                         {
    	                        	 showMyProfile(); //TODO could this be parsed the username that you want to see the profile of?
    	                         }
    	                     });
    	                 }
    	             });
    	             popupMenu.add(showProfile);

    	             // display popup near location of mouse click
    	             popupMenu.show(e.getComponent(), e.getX(), e.getY() - 10);
    	         }
    	     }
    	 };
    	 userList.addMouseListener(mouseListener);
    	
    	
    	
    	JScrollPane userListScroll = new JScrollPane(userList);
    	//userListScroll.setBorder(emptyBorder);
    	
    	userCount.setText(" " + userListModel.getSize() + " Users Online");
    	panel.add(userCount, BorderLayout.NORTH);
    	panel.add(userListScroll);
    	panel.setMinimumSize(new Dimension(0, 100));    	
    	return panel;
    }
    
    public JPanel pnlReceive()
    {
    	/*panel for the chat conversation*/
    	JPanel panel = new JPanel(new BorderLayout());
    	
         messageReceiveBox.setLineWrap(true);
         messageReceiveBox.setWrapStyleWord(true);
         Font receiveFont = new Font("Dialog", 2, 12);
         messageReceiveBox.setFont(receiveFont);
         messageReceiveBox.setEditable(false);
         //messageReceiveBox.addActionListener(); TODO 
         /*Format of output:
          *[bold]username[/bold] timestamp: message*/
         JScrollPane messageReceiveBoxScroll = new JScrollPane(messageReceiveBox);
         //messageReceiveBoxScroll.setBorder(emptyBorder);
         panel.add(new JLabel(" User Chat"), BorderLayout.NORTH);
         panel.add(messageReceiveBoxScroll, BorderLayout.CENTER);
         panel.setPreferredSize(new Dimension(0, 800));
    	
    	return panel;
    }
    
    public JPanel pnlSend()
    {
    	/**/
    	JPanel panel = new JPanel(new BorderLayout());
    	
    	 /*Text field for message text*/
        messageSendBox = new JTextArea();
        messageSendBox.setLineWrap(true);
        messageSendBox.setWrapStyleWord(true);
        Font sendFont = new Font("Dialog", 1, 12);
        messageSendBox.setFont(sendFont);
        //ActionListener aL = newAction(); //TODO - Alex doesn't know what he be doing with action listeners
        //messageSendBox.setActionCommand("Send");
        messageSendBox.addMouseListener(new MouseAdapter()
        {
            /*public void mouseClicked(MouseEvent  e)
            {
            	if (messageSendBox.getText().equals(initialMessage)) //TODO could use an edited flag instead
            	{
            		messageSendBox.setText("boogaloo");
            		messageSendBox.setText("");
            	}
            }*/
        });
        JScrollPane messageSendBoxScroll = new JScrollPane(messageSendBox);
        panel.add(messageSendBoxScroll, BorderLayout.CENTER);
        
        JButton btnSend = new JButton("Send");
        btnSend.setMinimumSize(new Dimension(0,40));

        btnSend.setToolTipText("Click to send message");

        //btnSend.addActionListener(); TODO need an action listener for the enter key
        btnSend.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "doSomething");
        btnSend.getActionMap().put("doSomething", null); //TODO call the mouseclicked code below

        btnSend.addMouseListener(new MouseAdapter()
        {
            public void mouseClicked(MouseEvent  e)
            {
            	String message = messageSendBox.getText();
            	if (!message.equals("")) //TODO disable send button if no meaningful text entered
            	{
            		//DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    //Date date = new Date();

                	//System.out.println(dateFormat.format(date) + " " + message);
                	
                	//client.updateChatLog(username, date, message);
                	client.sendMessageChatroom( message );
                	messageSendBox.setText("");
            	}
            }
        });
        panel.add(btnSend, BorderLayout.EAST);  
        panel.setMaximumSize(new Dimension(10, 40));
    	
    	return panel;
    }

    public JPanel mainArea()
    {        
        /*Chat panel stuffs- Alex*/
    	Border emptyBorder = BorderFactory.createEmptyBorder();
    	
        JSplitPane usersReceive = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pnlUsers(), pnlReceive());
        usersReceive.setBorder(emptyBorder);
        usersReceive.setOneTouchExpandable(true);
        
        JSplitPane chat = new JSplitPane(JSplitPane.VERTICAL_SPLIT, usersReceive, pnlSend());
        chat.setBorder(emptyBorder);
        chat.setOneTouchExpandable(true);
        chat.setDividerLocation(800);
        /*End of Chat panel stuffs*/

        JPanel panel = new JPanel(new BorderLayout());
        dirSourceEditorSeletionSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, dirView, this.sourceEditorSection());
        dirSourceEditorSeletionSplit.setOneTouchExpandable(true);
        dirSourceEditorSeletionSplit.setBorder(emptyBorder);

        editorChatSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, dirSourceEditorSeletionSplit, chat);
        editorChatSplit.setOneTouchExpandable(true);
        this.editorChatSplit.setResizeWeight(1.0);

        // Provide minimum sizes for the two components in the split pane
        panel.add(editorChatSplit);
        return panel;
    }

    // TODO: Update chatlog moved to Client for now    
    
    public void run()
    {
        w = new JFrame("CIDEr");
        w.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        URL x = this.getClass().getResource("icon.png");
        ImageIcon image = new ImageIcon(x);
        Image test = image.getImage();
        w.setIconImage(test);

        JPanel p = new JPanel(new BorderLayout());
        p.add(this.mainMenuBar(), BorderLayout.PAGE_START);
        p.add(this.mainArea());
        w.add(p);
        w.pack();
        this.dirSourceEditorSeletionSplit.setDividerLocation(0.25);
        this.editorChatSplit.setDividerLocation(0.75);
        w.setLocationByPlatform(true);
        w.setExtendedState(JFrame.MAXIMIZED_BOTH);
        w.setVisible(true);
        w.addWindowListener(new WindowListener()
        {

            @Override
            public void windowActivated(WindowEvent arg0)
            {
                // TODO Auto-generated method stub

            }

            @Override
            public void windowClosed(WindowEvent arg0)
            {
            }

            @Override
            public void windowClosing(WindowEvent arg0)
            {
            	myProfile.updateTimeSpent(startTime);
                myProfile.updateProfileInfo();
                System.out.println("disconnecting");
                client.disconnect();
            }

            @Override
            public void windowDeactivated(WindowEvent arg0)
            {
                // TODO Auto-generated method stub

            }

            @Override
            public void windowDeiconified(WindowEvent arg0)
            {
                // TODO Auto-generated method stub

            }

            @Override
            public void windowIconified(WindowEvent arg0)
            {
                // TODO Auto-generated method stub

            }

            @Override
            public void windowOpened(WindowEvent arg0)
            {
                // TODO Auto-generated method stub

            }

        });
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
