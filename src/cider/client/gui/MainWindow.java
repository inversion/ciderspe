package cider.client.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.Time;
import java.util.HashMap;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
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
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.util.StringUtils;

import cider.common.network.Client;
import cider.common.processes.Profile;

public class MainWindow implements Runnable
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
    public static final String GROUPCHAT_TITLE = "Group Chat";
    public JTabbedPane receiveTabs = new JTabbedPane();
    public JPanel receivePanel;
    public JTextArea messageSendBox;

    /**
     * These variable are for the profiles
     * 
     * @author Jon
     */
    public long startTime;
    private Profile myProfile;

    MainWindow(String username, String password, String host, int port,
            String serviceName, Client c) throws XMPPException
    {
        // TODO: Should more stuff be in the constructor rather than the
        // mainArea method? The variables look a bit of a mess
        dirView = new DirectoryViewComponent();
        startTime = System.currentTimeMillis();
        this.username = username;
       
        client = c;      
        client.registerGUIComponents(dirView, tabbedPane, openTabs,
                userListModel, userCount, receiveTabs );
        receivePanel = pnlReceive();
        dirView.setClient(client);
        client.getFileListFromBot();
        profileSetup(); 
    }

    private void profileSetup() 
    {
    	/**
         * This method sets up the profile by asking the Bot
         * if it has the user on its record. If so, the profile
         * updates appropriately. If not, a new profile is created.
         * 
         * @author Jon
         */
        getProfileFromBot();
        
        try 
        {
        	/**
        	 * This is to negate the effect of latency on checking the
        	 * new profile
        	 */
			Thread.sleep(1000);
		} 
        catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
        if (client.profileFound == false)
        {
        	myProfile = new Profile(username, client);
        }
        else
        	myProfile = client.profile;
        
         
        //Inform the bot of the user's current colour
    	botColourChange(myProfile.userColour.getRed(),
    			myProfile.userColour.getGreen(),
    			myProfile.userColour.getBlue());
    	
    	if (client.colours.containsKey(username))
    		client.colours.remove(username);
    	client.colours.put(username, myProfile.userColour);
        System.out.println(myProfile);
        
        retrieveAllUserColours();
		announceColourChange(myProfile.userColour.getRed(),
				myProfile.userColour.getGreen(),
				myProfile.userColour.getBlue());
	}

	private void retrieveAllUserColours() 
	{
		for (int i = 0; i < userListModel.getSize(); i++)
		{
			String focus = (String) userListModel.elementAt(i);
			if (!focus.equals(username))
			{
				getUserColour(focus);
				try {Thread.sleep(2000);} catch (InterruptedException e) {}
				if (client.colours.containsKey(focus))
					client.colours.remove(focus);
				client.colours.put(focus, client.incomingColour);
			}
		}
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
	
	/**
	 * When this method is called, the username and its colour will be added
	 * to the hash table of user colours
	 * @author Jon
	 */
	private void getUserColour(String user)
	{
		try 
		{
			client.botChat.sendMessage(StringUtils.encodeBase64("requestusercolour " + username + " " + user));
			
		} 
		catch (XMPPException e) 
		{
			e.printStackTrace();
		}
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
                else if (action.equals("Change Username"))
                {
                    String s = (String) JOptionPane.showInputDialog(
                            new JPanel(), "Enter new username:",
                            "New username", JOptionPane.PLAIN_MESSAGE);
                    System.out.println("*************************\n"
                            + "USERNAME CHANGED TO: \"" + s + "\"\n"
                            + "*************************\n");
                }
                else if (action.equals("Change Profile Colour"))
                {
                    changeColour();
                }
                else if (action.equals("DEV: Pretend to quit"))
                {
                    myProfile.updateTimeSpent(startTime);
                    startTime = System.currentTimeMillis();
                    myProfile.updateProfileInfo();
                }
                else if (action.equals("DEV: Push profile to server"))
                {
                    sendProfileToBot();
                }
                else if (action.equals("DEV: Get profile from server"))
                {
                	getProfileFromBot();
                }
                else if (action.equals("DEV: Show list of colours stored locally"))
                {
                	System.out.println(client.colours);
                }
                else if (action.equals("Close File"))
                {
                    closeFile(action);
                }
                else if (action.equals("Import"))
                {
                    openFile();
                }
                else if (action.equals("New"))
                {
                    newFile();
                }
                else if (action.equals("Export"))// (action.equals("Save") ||
                                                 // action.equals("Save As"))
                {
                    saveFile(action);
                }
                else if (action.equals("DEV: Terminate Bot Remotely"))
                {
                    client.terminateBotRemotely();
                }
            }
        };
        return AL;
    }
    
    private void changeColour()
    {
        final JColorChooser colorChooser = new JColorChooser(
                myProfile.userColour);
        ActionListener okListener = new ActionListener()
        {
            public void actionPerformed(ActionEvent action)
            {
            	int R = colorChooser.getColor().getRed();
            	int G = colorChooser.getColor().getGreen();
            	int B = colorChooser.getColor().getBlue();
                myProfile.setColour(R, G, B);
                announceColourChange(R, G, B);
            	if (client.colours.containsKey(username))
            		client.colours.remove(username);
            	client.colours.put(username, myProfile.userColour);
            }
        };

        ActionListener cancelListener = new ActionListener()
        {
            public void actionPerformed(ActionEvent action)
            {
                System.out.println("Colour change cancelled");
            }
        };

        final JDialog dialog = JColorChooser.createDialog(null,
                "Change user colour", true, colorChooser, okListener,
                cancelListener);

        URL x = this.getClass().getResource("icon.png");
        ImageIcon image = new ImageIcon(x);
        Image test = image.getImage();
        dialog.setIconImage(test);

        dialog.setVisible(true);

    }

    public void openFile()
    {
    	JFileChooser fc = new JFileChooser();
    	int rVal = fc.showOpenDialog(null);
    	if (rVal == JFileChooser.APPROVE_OPTION)
    	{
    		String temp;
    		currentDir = fc.getSelectedFile().getAbsolutePath();
    		currentFileName = fc.getSelectedFile().getName();
    		
    		//this.liveFolder = new LiveFolder("Bot", "root");
    		//SourceDocument t1 = this.liveFolder.makeDocument("t1.SourceDocument");
    		//client.openTabFor(currentDir);
    		try
    		{
    			//FileInputStream fis = new FileInputStream(currentDir + currentFileName);
    			//BufferedInputStream bis = new BufferedInputStream(fis);
    			BufferedReader br = new BufferedReader(new FileReader(currentDir));
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
        File f = new File(client.getCurrentDocument().name /* + ".java" */);
        fc.setSelectedFile(f);

        if (currentFileName.equals("Unsaved Document 1")
                || action.equals("Export"))
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
            out.write(client.getCurrentDocument().toString()/* currentFileContents */);
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
        // saveFile(action);
        // // closes tab regardless of save or cancel
        // tabbedPane.remove(tabbedPane.getSelectedIndex());
        // tabbedPane.setSelectedIndex(--currentTab);
    }
   
    public void newFile()
    {
    	String s = (String) JOptionPane.showInputDialog(  new JPanel(), "Enter a filename:", "New File", JOptionPane.PLAIN_MESSAGE);
    	if (s == null)
    	{
    		return;
    	}
    	//LiveFolder liveFolder = new LiveFolder(username, this.client.getLiveFolder());
    	//liveFolder.makeDocument(s);
    	//TODO: create directory tree object with 's' then open it
    	JLabel TEMP = new JLabel("blah blah blah");
    	tabbedPane.addTab(s,TEMP);// new SourceEditor(currentFileContents, currentDir)); //new SourceEditor("", "\\."));
    	tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
    }
    
    private void restartProfile()
    {
        int response = JOptionPane.showConfirmDialog(null,
                "Are you sure you wish to reset your profile?");
        if (response == 0)
        {
            File f = new File(username + ".txt");
            try
            {
                if (!f.exists())
                    System.err.println("Error: profile create failed!");
                else
                {
                    f.delete();
                    f.createNewFile();
                    FileWriter fw = new FileWriter(f);
                    BufferedWriter out = new BufferedWriter(fw);
                    System.out
                            .println("Profile has been reset, new credentials are:");
                    System.out
                            .println(username
                                    + "\n"
                                    + "chars: 0\ntimespent: 0\nlastonline: Never!");
                    out.write(username
                            + "\n"
                            + "chars: 0\ntimespent: 0\nlastonline: Never!");
                    out.close();
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            myProfile = new Profile(username, client);
            startTime = System.currentTimeMillis();
        }
        else
            return;
    }
    
	public void announceColourChange(int r, int g, int b) 
	{
        try 
        {
			client.chatroom.sendMessage(StringUtils.encodeBase64(
					"colourchange: " + username + " " + r + " " + g + " " + b));
		} 
        catch (XMPPException e) 
        {
			e.printStackTrace();
		}
	}
    
	private void botColourChange(int r, int g, int b) 
	{
        try 
        {
			client.botChat.sendMessage(StringUtils.encodeBase64(
					"colourchange: " + username + " " + r + " " + g + " " + b));
		} 
        catch (XMPPException e) 
        {
			e.printStackTrace();
		}
	}

    private void showMyProfile()
    {
        System.out.println(myProfile.timeSpent);
        myProfile.updateTimeSpent(startTime);
        System.out.println(myProfile.timeSpent);
        startTime = System.currentTimeMillis();
        
//        int count = this.client.getCurrentDocument().playOutEvents(Long.MAX_VALUE).countCharactersFor(username);
//    	myProfile.adjustCharCount(count);
        
        JFrame profileFrame = new JFrame("My Profile- " + username);
        Container content = profileFrame.getContentPane();
        content.setLayout(new GridLayout(10, 2));

        URL x = this.getClass().getResource("icon.png");
        ImageIcon image = new ImageIcon(x);
        Image test = image.getImage();
        profileFrame.setIconImage(test);

        profileFrame.setBounds(100, 100, 400, 350);
        profileFrame.setResizable(false);
        // profileFrame.pack();
        profileFrame.show();
        profileFrame.setLocationRelativeTo(null);

        JLabel uName = new JLabel("Username: " + username);
        uName.setHorizontalAlignment(JLabel.LEFT);
        uName.setVerticalAlignment(JLabel.TOP);
        content.add(uName);

        // JLabel uPwd = new JLabel("Password: " + client.CLIENT_USERNAME);
        // uPwd.setHorizontalAlignment(JLabel.LEFT);
        // uPwd.setVerticalAlignment(JLabel.TOP);
        // content.add(uPwd);

        JLabel chars = new JLabel("Characters pressed: " + myProfile.typedChars);
        //System.out.println(TypingEventList.countCharactersFor(username));
        chars.setHorizontalAlignment(JLabel.LEFT);
        chars.setVerticalAlignment(JLabel.TOP);
        content.add(chars);

        Time t = new Time(myProfile.timeSpent);
        System.out.println("TS = " + myProfile.timeSpent);
        JLabel time = new JLabel("Total time spent: " + t);
        time.setHorizontalAlignment(JLabel.LEFT);
        time.setVerticalAlignment(JLabel.TOP);
        content.add(time);

        JLabel lastonline = new JLabel("You were last seen: "
                + myProfile.lastOnline);
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

        JLabel chars = new JLabel(
                "CIDEr- Collaborative Integrated Development EnviRonment.");
        content.add(chars);

        frame.setVisible(true);
    }

    public void sendProfileToBot()
    {
    	//FIXME:
    	/*int count = this.client.getCurrentDocument().playOutEvents(Long.MAX_VALUE).countCharactersFor(username);
    	myProfile.adjustCharCount(0);*/
    	
        myProfile.updateTimeSpent(startTime);
        myProfile.updateProfileInfo();
        System.out.println(myProfile.toString());
        try
        {
            String s = myProfile.toString();
            s = "userprofile:  " + s;
            System.out.println(s);
            client.botChat.sendMessage(StringUtils.encodeBase64(s));
        }
        catch (XMPPException e1)
        {
            e1.printStackTrace();
        }
    }
    
    private void getProfileFromBot()
    {
    	try 
    	{
			client.botChat.sendMessage(StringUtils.encodeBase64("requestprofile " + username));
			System.out.println("Requesting profile from server");
		} catch (XMPPException e) 
		{
			e.printStackTrace();
		}
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
                            // closeFile("Close File");
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
        addMenuItem(menu, "Import", KeyEvent.VK_O, aL);
        // addMenuItem(menu, "Save", KeyEvent.VK_S, aL);
        addMenuItem(menu, "Export", KeyEvent.VK_A, aL);
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
        menu = new JMenu("Profile");
        menuBar.add(menu);

        addMenuItem(menu, "My Profile", -1, aL);
        addMenuItem(menu, "Change Profile Colour", -1, aL);
        addMenuItem(menu, "Change Username", -1, aL);
        addMenuItem(menu, "Reset My Profile", -1, aL);

        // menu 4
        menu = new JMenu("Help");
        menuBar.add(menu);

        addMenuItem(menu, "About", -1, aL);

        // the DEV(eloper) menu is for us to test back-end things such as saving
        // and pushing
        // NYI = not yet implemented
        menu = new JMenu("DEV");
        menuBar.add(menu);

        addMenuItem(menu, "DEV: Push profile to server", -1, aL);
        addMenuItem(menu, "DEV: Pretend to quit", -1, aL);
        addMenuItem(menu, "DEV: Get profile from server", -1, aL);
        addMenuItem(menu, "DEV: Terminate Bot Remotely", -1, aL);
        addMenuItem(menu, "DEV: Show list of colours stored locally", -1, aL);

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

    @SuppressWarnings("serial")
	class MyListCellRenderer extends JLabel implements ListCellRenderer
    {
    	boolean selected = false;
    	int index;
    	String name;

        public Component getListCellRendererComponent(JList paramlist, Object value, int index, boolean isSelected, boolean cellHasFocus)
        {
        	this.selected = isSelected;
        	this.index = index;
        	this.name = value.toString();
        	
            return this;
        }
        
        public void paint(Graphics g)
        {
        	Color bg;
        	
            if (selected) {
              bg = Color.LIGHT_GRAY;
            } else {
              bg = Color.WHITE;
            }

            // fill background
            g.setColor(bg);
            g.fillRect(0, 0, getWidth(), getHeight());
            
            // draw coloured rectangle and name
            g.setColor(client.colours.get(name)); //get unique user color here
            g.fillRect(6, 6, 13, 13);
            g.drawString(name, 25, 17);
        }
    }
    
    public JPanel pnlUsers()
    {
        /* panel for the list of online users */
        JPanel panel = new JPanel(new BorderLayout());

        Border emptyBorder = BorderFactory.createEmptyBorder();

        // temporarily added so there were always users online
        /*userListModel.add(0, "Person 1");
        userListModel.add(1, "Person 2");
        userListModel.add(2, "Person 3");
        userListModel.add(3, "Person 4");
        userListModel.add(4, "Person 5");*/
        
        userList = new JList(userListModel);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        userList.setCellRenderer(new MyListCellRenderer());
        userList.setFixedCellWidth(25);
        userList.setFixedCellHeight(25);
        
        

        
        
        
        /*
         * for (int i=0; i < userList.getModel().getSize(); i++) { Object item =
         * userList.getModel().getElementAt(i);
         * userList.setForeground(Color.red); //TODO looking at using different
         * colours for each user }
         */

        /* this can be used to initiate a private chat- Alex */
        MouseListener mouseListener = new MouseAdapter()
        {
            public void mouseClicked(MouseEvent e)
            {
                int i = userList.locationToIndex(e.getPoint());
                if (e.getClickCount() == 2)
                {
                    System.out.println("Double clicked on Item " + i);
                    System.out.println("Double clicked on Item: "
                            + userList.getModel().getElementAt(i));
                    client.initiateChat( (String) userList.getSelectedValue() );
                }
                else if ((e.getButton() == MouseEvent.BUTTON3)
                        && (userList.locationToIndex(e.getPoint()) != -1))
                {
                    /* pop up for viewing users profile/stats etc"); */
                    userList.setSelectedIndex(userList.locationToIndex(e
                            .getPoint()));

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
                                    client.initiateChat( (String) userList.getSelectedValue() );
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
                                    showMyProfile(); // TODO could this be
                                    // parsed the username that
                                    // you want to see the
                                    // profile of?
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
        // userListScroll.setBorder(emptyBorder);

        userCount.setText(" " + userListModel.getSize() + " Users Online");
        panel.add(userCount, BorderLayout.NORTH);
        panel.add(userListScroll);
        panel.setMinimumSize(new Dimension(0, 100));
        return panel;
    }

    public JPanel pnlReceive()
    {
        /*
         * this should only create the panel- initiateAChat) should create the
         * chat's both group and private to fill the tabbed pane- Alex
         */
        /* panel for the chat conversation */
        JPanel panel = new JPanel(new BorderLayout());
        
        
        // messageReceiveBox.addActionListener(); TODO
        /*
         * Format of output:[bold]username[/bold] timestamp: message
         */
        
        // messageReceiveBoxScroll.setBorder(emptyBorder);

        //receiveTabs = new JTabbedPane();

        panel.add(new JLabel(" User Chat" ), BorderLayout.NORTH);
        panel.add(receiveTabs/* messageReceiveBoxScroll */, BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(0, 800));
        
        /**
         * We are not actually intiating the group chat here, so we just need to make
         * the tab for it. The client handles the updating of its contents.
         */
        client.createChatTab( GROUPCHAT_TITLE );
        
        return panel;
    }

    public JPanel pnlSend()
    {
        JPanel panel = new JPanel(new BorderLayout());

        /* Text field for message text */
        messageSendBox = new JTextArea();
        messageSendBox.setLineWrap(true);
        messageSendBox.setWrapStyleWord(true);
        Font sendFont = new Font("Dialog", 1, 12);
        messageSendBox.setFont(sendFont);
        messageSendBox.addKeyListener(new KeyListener()
        {
            @Override
            public void keyTyped(KeyEvent e)
            {
            }

            @Override
            public void keyPressed(KeyEvent e)
            {
            }

            @Override
            public void keyReleased(KeyEvent e)
            {
                int c = e.getKeyCode();
                if (c == KeyEvent.VK_ENTER)
                {
                    sendChatMessage(messageSendBox.getText());
                }
            }
        });

        JScrollPane messageSendBoxScroll = new JScrollPane(messageSendBox);
        panel.add(messageSendBoxScroll, BorderLayout.CENTER);

        JButton btnSend = new JButton("Send");
        btnSend.setMinimumSize(new Dimension(0, 40));
        btnSend.setToolTipText("Click to send message");
        btnSend.addMouseListener(new MouseAdapter()
        {
            public void mouseClicked(MouseEvent e)
            {
                sendChatMessage(messageSendBox.getText());
            }
        });
        
        panel.add(btnSend, BorderLayout.EAST);
        panel.setMaximumSize(new Dimension(10, 40));

        return panel;
    }

    protected void sendChatMessage(String message)
    {
        if (!message.equals("") && !message.equals("\n"))
        {
            client.sendChatMessageFromGUI(message);
            messageSendBox.setText("");
        }
    }

    public JPanel mainArea()
    {
        /* Chat panel stuffs- Alex */
        Border emptyBorder = BorderFactory.createEmptyBorder();

        JSplitPane usersReceive = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                pnlUsers(), receivePanel);
        usersReceive.setBorder(emptyBorder);
        usersReceive.setOneTouchExpandable(true);

        JSplitPane chat = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                usersReceive, pnlSend());
        chat.setBorder(emptyBorder);
        chat.setOneTouchExpandable(true);
        chat.setDividerLocation(800);
        /* End of Chat panel stuffs */

        JLabel test = new JLabel("i have no idea how to call the java compiler");
        JSplitPane EditorDebugSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                this.sourceEditorSection(), test);
        EditorDebugSplit.setBorder(emptyBorder);
        EditorDebugSplit.setOneTouchExpandable(true);
        EditorDebugSplit.setDividerLocation(800);

        JPanel panel = new JPanel(new BorderLayout());
        dirSourceEditorSeletionSplit = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT, dirView, EditorDebugSplit);
        // dirSourceEditorSeletionSplit = new
        // JSplitPane(JSplitPane.HORIZONTAL_SPLIT, dirView,
        // this.sourceEditorSection());

        dirSourceEditorSeletionSplit.setOneTouchExpandable(true);
        dirSourceEditorSeletionSplit.setBorder(emptyBorder);

        editorChatSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                dirSourceEditorSeletionSplit, chat);
        editorChatSplit.setOneTouchExpandable(true);
        this.editorChatSplit.setResizeWeight(1.0);

        // Provide minimum sizes for the two components in the split pane
        panel.add(editorChatSplit);
        return panel;
    }

    // TODO: Update chatlog moved to Client for now

    public void run()
    {
        w = new JFrame("CIDEr - Logged in as " + username);
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
                sendProfileToBot();
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

    public void killWindow()
    {
        w.dispose();
    }
}
