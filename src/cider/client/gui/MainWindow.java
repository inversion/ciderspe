/**
 *  CIDER - Collaborative Integrated Development EnviRonment
    Copyright (C) 2011  Andrew Moss
                        Lawrence Watkiss
                        Jonathan Bannister
                        Alex Sheppard
                        Miles Taylor
                        Ashley Woodman

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package cider.client.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.Box;
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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

import cider.common.network.client.Client;
import cider.common.processes.DocumentProperties;
import cider.common.processes.ImportFiles;
import cider.common.processes.Profile;
import cider.common.processes.SourceDocument;
import cider.common.processes.TimeBorder;
import cider.common.processes.TimeBorderList;
import cider.documentViewerComponents.DocumentHistoryViewer;
import cider.documentViewerComponents.EditorTypingArea;
import cider.documentViewerComponents.SourceDocumentViewer;
import cider.shared.ClientSharedComponents;

/**
 * Class containing the main methods of the main window of the application
 */
public class MainWindow
{
    JFrame w;
    /**
     * Current directory to work in
     */
    public String currentDir;
    /**
     * Name of a new file
     */
    public String currentFileName = "Unsaved Document 1";
    /**
     * Contents of an empty file
     */
    public String currentFileContents = "";

    /**
     * Currently visible tab
     */
    public int currentTab = 0;

    public LoginUI login;
    /**
     * 
     */
    public ClientSharedComponents shared;
    /**
     * 
     */
    public AWTEventListener activityListener;

    Client client;

    public JSplitPane dirSourceEditorSelectionSplit;

    private JSplitPane editorChatSplit;

    private String username;
    private ArrayList<String> savedFiles = new ArrayList<String>();
    public static final String GROUPCHAT_TITLE = "Group Chat";
    /**
     * Chat panel receive area
     */
    public JPanel receivePanel;

    /**
     * Chat panel send box
     */
    public JTextArea messageSendBox;

    /**
     * Whether line locking is enabled
     */
    public static boolean LockingEnabled = true;

    private DebugWindow debugwindow;

    boolean offlineMode = false;

    /**
     * The status bar object for the bottom of the window
     */
    public static StatusBar statusBar;

    /**
     * These variable are for the profiles
     * 
     * @author Jon
     */
    public long startTime = System.currentTimeMillis();

    /**
     * User Profile object
     */
    public Profile myProfile;

    private String profilePictureDir;

    /**
     * Sets up a new ClientSharedComponent
     */
    MainWindow()
    {
        shared = new ClientSharedComponents();
    }

    /**
     * The constructor that assigns a new MainWindow its main components. Sets
     * up a new profile for the user.
     * 
     * @param username
     *            The username of the user.
     * @param c
     *            The Client object that is being used by the client.
     * @param loginUI
     *            The LoginUI object that this method is called from.
     * @param shared
     *            The shared component between client and bot
     * @throws XMPPException
     */
    MainWindow(String username, Client c, LoginUI loginUI,
            ClientSharedComponents shared) throws XMPPException
    {
        // Register GUI components shared with client
        this.shared = shared;
        myProfile = shared.profile;
        this.username = username;
        login = loginUI;

        client = c;
        try
        {
            currentDir = new File( "." ).getCanonicalPath();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        receivePanel = pnlReceive();
        shared.dirView.setClient(client);
        client.addParent(this);
        profileSetup();
        tabListener();

        if (CiderApplication.debugApp)
        {
            System.out.println("Standard output stream working");
            System.err.println("Error output stream working");
        }
    }
    /**
     * Initialises the tab listener
     */
    private void tabListener() 
    {
    	ChangeListener l = new ChangeListener()
    	{
			@Override
			public void stateChanged(ChangeEvent e)
			{
				JTabbedPane p = (JTabbedPane)e.getSource();
				int selection = p.getSelectedIndex();
				String namedoe = shared.tabbedPane.getTitleAt(selection);
				if (shared.openTabs != null && namedoe != null)
				{
					if (shared.openTabs.containsKey(namedoe))
					{
						client.openTab(namedoe);
					}
				}					
			}    		
    	};
        shared.tabbedPane.addChangeListener(l);
	}

	/**
     * Announce you have changed your colour to all connected parties.
     * 
     * @param r
     *            Red component
     * @param g
     *            Green component
     * @param b
     *            Blue component
     * 
     * @author Andrew, Jon
     */
    public void announceColourChange(int r, int g, int b)
    {
        try
        {
            Message msg = new Message();
            msg.setBody("");
            msg.setProperty("ciderAction", "colourchange");
            msg.setProperty("r", r);
            msg.setProperty("g", g);
            msg.setProperty("b", b);
            msg.setProperty("username", username);
            msg.setType(Message.Type.groupchat);
            msg.setTo(client.chatroom.getRoom());
            client.chatroom.sendMessage(msg);
        }
        catch (XMPPException e)
        {
            e.printStackTrace();
            JOptionPane.showMessageDialog(new JPanel(), "Error: "
                    + e.getMessage());
            return;
        }
    }

    /**
     * Allows the user to change their profile's colour. If the colour is
     * changed, it is announced to the XMPP chatroom by the ActionListener.
     * 
     * @author Jon
     */
    private void changeColour()
    {
        final JColorChooser colorChooser = new JColorChooser(myProfile
                .getColour());
        ActionListener okListener = new ActionListener()
        {
            @SuppressWarnings("static-access")
            public void actionPerformed(ActionEvent action)
            {
                int R = colorChooser.getColor().getRed();
                int G = colorChooser.getColor().getGreen();
                int B = colorChooser.getColor().getBlue();
                myProfile.setColour(R, G, B);
                announceColourChange(R, G, B);
                if (client.colours.containsKey(username))
                    client.colours.remove(username);
                client.colours.put(username, myProfile.getColour());
            }
        };

        ActionListener cancelListener = new ActionListener()
        {
            public void actionPerformed(ActionEvent action)
            {
                if (DEBUG)
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

    /**
     * Toggles line-locking.
     */
    private void ChangeLocking()
    {
        if (LockingEnabled == true)
        {
            int response = JOptionPane.showConfirmDialog(null,
                    "Are you sure you wish to disable line locking");
            if (response == 0)
            {
                LockingEnabled = false;
            }
            else
            {
                return;
            }
        }
        else
        {
            int response = JOptionPane.showConfirmDialog(null,
                    "Are you sure you wish to enable line locking?");
            if (response == 0)
            {
                LockingEnabled = true;
            }
            else
            {
                return;
            }
        }
    }

    // Credit: http://www.java2s.com/Code/Java/JDK-6/CompileaJavacode.htm
    void compileFile()
    {
        if( client.getCurrentDocument() == null )
            return;
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory( new File( currentDir ) );
        File f = new File(client.getCurrentDocument().name);
        fc.setSelectedFile(f);

        int watdo = fc.showSaveDialog(null);
        if (watdo != JFileChooser.APPROVE_OPTION)
        {
            return;
        }

        try
        {
            currentFileName = f.getAbsolutePath();
            f.createNewFile();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
//        currentDir = fc.getSelectedFile().getAbsolutePath();

        try
        {
            FileWriter fstream = new FileWriter( currentFileName );
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(client.getCurrentDocument().toString());
            out.close();
        }
        catch (IOException e1)
        {
            e1.printStackTrace();
        }

        JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
        OutputStream baos = new ByteArrayOutputStream();
        int results = javac.run(System.in, System.out, baos, currentFileName);

        if (results == 0)
        {
            debugwindow.println("Compilation Successful");
        }
        else
        {
            debugwindow.println("Compilation failed");
        }
        debugwindow.println(baos.toString());
    }

    /**
     * Converts a SourceDocument into a text file that is saved where the user
     * specifies.
     */
    public void exportFile()
    {
        try
        {
            JFileChooser fc = new JFileChooser();
            File f = new File(client.getCurrentDocument().shortName() + ".java");
            fc.setSelectedFile(f);

            if (currentFileName.equals("Unsaved Document 1")
                    || savedFiles.contains(client.getCurrentDocument()
                            .shortName()
                            + ".java") == false)
            {
                int watdo = fc.showSaveDialog(null);
                if (watdo != JFileChooser.APPROVE_OPTION)
                {
                    return;
                }
                savedFiles.add(client.getCurrentDocument().shortName());
                currentFileName = fc.getSelectedFile().getName() + ".java";
                currentDir = fc.getSelectedFile().getAbsolutePath();
            }
            FileWriter fstream = new FileWriter(currentDir);
            BufferedWriter out = new BufferedWriter(fstream);
            out
                    .write(client.getCurrentDocument().toString()/* currentFileContents */);
            out.close();
        }
        catch (IOException e1)
        {
            e1.printStackTrace();
            JOptionPane.showMessageDialog(new JPanel(), ("Error: " + e1
                    .getMessage()));
            return;
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
            JOptionPane.showMessageDialog(new JPanel(),
                    "Error: There is no document open!");
            return;
        }
        shared.tabbedPane.setTitleAt(currentTab, currentFileName);
    }

    private void FlipHighlighting(int i)
    {
        EditorTypingArea.Highlighting = i;
    }

    // FIXME!
    public String[] getCommands( String name )
    {
        if (isWindows())
        {
            return new String[] { "cmd /C start cmd /K cd \"" + currentDir +
                    "\\", "&", "java " + name };
        }
        else if (isUnix())
        {
            return new String[] { currentDir /* "./runHello.sh" */};
        }
        else
        {
            return null;
        }
    }

    /**
     * When this method is called, the username and its colour will be added to
     * the hash table of user colours
     * 
     * @author Jon
     */
    private void getUserColour(String user)
    {
        try
        {
            Message msg = new Message();
            msg.setBody("");
            msg.setProperty("ciderAction", "requestusercolour");
            msg.setProperty("user", user);
            client.botChat.sendMessage(msg);
        }
        catch (XMPPException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Imports a .java file that the user selects and converts it into a
     * SourceDocument.
     */
    public void importFile()
    {
        JFileChooser fc = new JFileChooser();
        fc.setAcceptAllFileFilterUsed(false);
        FileFilter filter = new FileNameExtensionFilter("Java file", "java");
        fc.addChoosableFileFilter(filter);
        int rVal = fc.showOpenDialog(null);
        if (rVal == JFileChooser.APPROVE_OPTION)
        {
            String file = fc.getSelectedFile().getAbsolutePath();
            ImportFiles imp = null;
            try
            {
                imp = new ImportFiles(file);
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            HashMap<String, String> files = imp.getFiles();
            Iterator<Entry<String, String>> itr = files.entrySet().iterator();

            while (itr.hasNext())
            {
                Entry<String, String> i = itr.next();
                File fullPath = new File(i.getKey());
                client.createDocument(fullPath.getName(), fullPath.getParent(),
                        i.getValue());
            }

            // tabbedPane.addTab(currentFileName, new SourceEditor(
            // currentFileContents, currentDir));
            // shared.tabbedPane.setSelectedIndex(++currentTab);
        }
    }

    /**
     * Disposes of the MainWindow GUI.
     * 
     * @author Jon
     */
    public void killWindow()
    {
        w.dispose();
    }

    /**
     * All the elements added for the window
     * @return
     */
    public JPanel mainArea()
    {
        /* Chat panel stuffs*/
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

        debugwindow = new DebugWindow();
        debugwindow.setAutoscrolls(true);

        JSplitPane EditorDebugSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                this.sourceEditorSection(), debugwindow);

        EditorDebugSplit.setBorder(emptyBorder);
        EditorDebugSplit.setOneTouchExpandable(true);
        EditorDebugSplit.setDividerLocation(800);

        JPanel panel = new JPanel(new BorderLayout());
        dirSourceEditorSelectionSplit = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT, shared.dirView, EditorDebugSplit);

        dirSourceEditorSelectionSplit.setOneTouchExpandable(true);
        dirSourceEditorSelectionSplit.setBorder(emptyBorder);

        editorChatSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                dirSourceEditorSelectionSplit, chat);
        editorChatSplit.setOneTouchExpandable(true);
        editorChatSplit.setResizeWeight(1.0);

        // Provide minimum sizes for the two components in the split pane
        panel.add(editorChatSplit);

        return panel;
    }

    /**
     * Sets up the Toolbar running along the top of the screen.
     * 
     * @return The fully populated toolbar.
     */
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
        addMenuItem(menu, "Export", KeyEvent.VK_S, aL);
        addMenuItem(menu, "Logout", KeyEvent.VK_L, aL);
        addMenuItem(menu, "Quit", KeyEvent.VK_Q, aL);

        // menu 2
        menu = new JMenu("Edit");
        menuBar.add(menu);

        addMenuItem(menu, "Cut", KeyEvent.VK_X, aL);
        addMenuItem(menu, "Copy", KeyEvent.VK_C, aL);
        addMenuItem(menu, "Paste", KeyEvent.VK_V, aL);
        addMenuItem(menu, "Line Home", KeyEvent.VK_LEFT, aL);
        addMenuItem(menu, "Line End", KeyEvent.VK_RIGHT, aL);
        addMenuItem(menu, "Document Home", KeyEvent.VK_HOME, aL);
        addMenuItem(menu, "Document End", KeyEvent.VK_END, aL);
        addMenuItem(menu, "Syntax Highlighting", -1, aL);
        addMenuItem(menu, "User Highlighting", -1, aL);
        addMenuItem(menu, "Line Locking", -1, aL);

        // menu 3
        menu = new JMenu("Run");
        menuBar.add(menu);

        addMenuItem(menu, "Compile", KeyEvent.VK_F9, aL);
        addMenuItem(menu, "Run", KeyEvent.VK_F10, aL);

        // menu 4
        menu = new JMenu("Source");
        menuBar.add(menu);

        addMenuItem(menu, "History", -1, aL);

        // menu 5
        menu = new JMenu("Profile");
        menuBar.add(menu);

        addMenuItem(menu, "My Profile", -1, aL);
        addMenuItem(menu, "Change Profile Colour", -1, aL);
        addMenuItem(menu, "Change Profile Picture", -1, aL);
        //addMenuItem(menu, "Change Font Size NYI FIXME", -1, aL);
        // addMenuItem(menu, "Change Username", -1, aL);
        addMenuItem(menu, "Reset My Profile", -1, aL);

        // menu 6
        menu = new JMenu("Help");
        menuBar.add(menu);

        addMenuItem(menu, "CIDEr Homepage", -1, aL);
        addMenuItem(menu, "User Guide", -1, aL);        
        addMenuItem(menu, "About", -1, aL);

        // the DEV(eloper) menu is for us to test back-end things such as saving
        // and pushing
        // NYI = not yet implemented
        if (DEBUG)
        {
            menu = new JMenu("DEV");
            menuBar.add(menu);

            addMenuItem(menu, "DEV: Push profile to server", -1, aL);
            addMenuItem(menu, "DEV: Pretend to quit", -1, aL);
            addMenuItem(menu, "DEV: Get profile from server", -1, aL);
            addMenuItem(menu, "DEV: Terminate Bot Remotely", -1, aL);
            addMenuItem(menu, "DEV: Show list of colours stored locally", -1,
                    aL);
        }

        return menuBar;
    }

    /**
     * The ActionListener that listens to the clicks on the dropdown menus.
     * 
     * @return The ActionListener.
     */
    public ActionListener newAction()
    {
        ActionListener AL = new ActionListener()
        {

            @SuppressWarnings("static-access")
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
                    myProfile.uploadProfile(client.botChat, startTime,
                            shared.idleTimer.getTotalIdleTime());
                    shared.idleTimer.resetTotal();
                    resetStartTime();
                    showProfile(myProfile);
                }
                else if (action.equals("Reset My Profile"))
                {
                    resetProfile();
                }
                else if (action.equals("Send"))
                {
                    messageSendBox.setText("boogaloo");
                }
                else if (action.equals("Logout"))
                {
                    int response;
                    response = JOptionPane.showConfirmDialog(new JPanel(),
                            "Are you sure you wish to log out?", "Logout",
                            JOptionPane.YES_NO_OPTION);
                    if (response == 0)
                    {
                        login.logout();
                    }
                }
                else if (action.equals("About"))
                {
                    showAbout();
                }
                else if (action.equals("Change Profile Colour"))
                {
                    changeColour();
                }
                else if (action.equals("Import"))
                {
                    importFile();
                }
                else if (action.equals("New"))
                {
                    newFile();
                }
                else if (action.equals("Change Profile Picture"))
                {
                    selectPicture();
                }
                else if (action.equals("Export"))
                {
                    exportFile();
                }
                /**
                 * Developer menu options
                 * 
                 * @author Jon
                 */
                else if (action.equals("DEV: Terminate Bot Remotely"))
                {
                    client.terminateBotRemotely();
                }
                else if (action.equals("DEV: Pretend to quit"))
                {
                    myProfile.updateTimeSpent(startTime);
                    resetStartTime();
                    myProfile.updateLastOnline();
                }
                else if (action.equals("DEV: Push profile to server"))
                {
                    myProfile.uploadProfile(client.botChat, startTime,
                            shared.idleTimer.getTotalIdleTime() / 10);
                    resetStartTime();
                }
                else if (action.equals("DEV: Get profile from server"))
                {
                    Profile.requestProfile(username, false, client.botChat);
                }
                else if (action
                        .equals("DEV: Show list of colours stored locally"))
                {
                    System.out.println(client.colours);
                }
                else if (action.equals("Compile"))
                {
                    compileFile();
                }
                else if (action.equals("Run"))
                {
                    compileFile();
                    runFile();
                }
                else if (action.equals("Syntax Highlighting"))
                {
                    FlipHighlighting(0);
                }
                else if (action.equals("User Highlighting"))
                {
                    FlipHighlighting(1);
                }
                else if (action.equals("Line Locking"))
                {
                    ChangeLocking();
                }
                else if (action.equals("Line Home"))
                {
                    shared.openTabs.get(client.getCurrentDocument().name).eta
                            .moveHome(false);
                }
                else if (action.equals("Line End"))
                {
                    shared.openTabs.get(client.getCurrentDocument().name).eta
                            .moveEnd(false);
                }
                else if (action.equals("Document Home"))
                {
                    shared.openTabs.get(client.getCurrentDocument().name).eta
                            .moveDocHome(false);
                }
                else if (action.equals("Document End"))
                {
                    shared.openTabs.get(client.getCurrentDocument().name).eta
                            .moveDocEnd(false);
                }
                else if (action.equals("History"))
                {
                    startSourceHistory();
                }
                else if (action.equals("Change Font Size"))
                {
                    String s = JOptionPane.showInputDialog(new JPanel(),
                            "Enter font size (default is 14):",
                            "Editor Font Size", JOptionPane.PLAIN_MESSAGE);
                    if (DEBUG)
                        System.out.println("*************************\n"
                                + "fontsize: \"" + s + "\"\n"
                                + "*************************\n");

                    myProfile.setFontSize(Integer.parseInt(s));
                    SourceDocumentViewer.fontSize = Integer.parseInt(s);
                    // SourceDocumentViewer.REFRESH ALL PLEASE
                }
                else if (action.equals("CIDEr Homepage"))
                {
                	try 
                	{
                		URI uri = new URI("http://www.ciderspe.com");
                		java.awt.Desktop.getDesktop().browse(uri);
                	} 
                	catch (URISyntaxException e1) 
                	{
                		e1.printStackTrace();
                	} 
                	catch (IOException e1) 
                	{
                		e1.printStackTrace();
                	}
                }
                else if (action.equals("User Guide"))
                {
                	try 
                	{
                		URI uri = new URI("http://www.ciderspe.com/downloads/CIDEr_USER_GUIDE.pdf");
                		java.awt.Desktop.getDesktop().browse(uri);
                	} 
                	catch (URISyntaxException e1) 
                	{
                		e1.printStackTrace();
                	} 
                	catch (IOException e1) 
                	{
                		e1.printStackTrace();
                	}
                }
            }
        };
        return AL;
    }

    /**
     * Creates a new SoruceDocument on the Bot and opens it
     * 
     * @author Ashley
     */
    public void newFile()
    {
        String s = (String) JOptionPane.showInputDialog(new JPanel(),
                "Enter a filepath (/ as separator):", "New File",
                JOptionPane.PLAIN_MESSAGE);
        if (s == null)
        {
            return;
        }
//
//        String full[] = s.split("/");
//        int n = full.length;
//        String name = full[n - 1];
//        String path = "";
//
//        if (n > 1)
//            path = full[0];
//
//        for (int i = 1; i < (n - 1); i++)
//        {
//            path += "\\";
//            path += full[i];
//        }

        String path, name;
        if( s.indexOf( "/" ) == -1 )
        {
        	path = "";
        	name = s;
        }
        else
        {
	        name = s.substring( s.lastIndexOf("/") + 1 );
	        path = s.substring(0, s.lastIndexOf("/") );
        }
        client.createDocument(name, path, "");

        try
        {
            Thread.sleep(1500);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        client.openTab(s);
    }

    /**
     * Initialises the JPanel that holds the group chat and userlist
     * 
     * @return The JPanel that holds the group chat and userlist
     */
    public JPanel pnlReceive()
    {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel usersHeader = new JPanel(new FlowLayout(FlowLayout.LEFT));
        Box box = Box.createHorizontalBox();

        URL u = this.getClass().getResource("iconchat.png");
        ImageIcon image = new ImageIcon(u);
        JLabel lblImage = new JLabel(image);
        box.add(lblImage);
        box.add(new JLabel(" User Chat"));
        usersHeader.add(box);

        panel.add(usersHeader, BorderLayout.NORTH);
        panel.add(shared.receiveTabs, BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(0, 800));
        panel.setBorder(BorderFactory
                .createMatteBorder(1, 0, 0, 0, Color.BLACK));

        /**
         * We are not actually intiating the group chat here, so we just need to
         * make the tab for it. The client handles the updating of its contents.
         */
        client.createChatTab(GROUPCHAT_TITLE);

        ChangeListener changeListener = new ChangeListener()
        {
            public void stateChanged(ChangeEvent changeEvent)
            {
                JTabbedPane tp = (JTabbedPane) changeEvent.getSource();
                int i = tp.getSelectedIndex();
                shared.receiveTabs.tabflashstop(tp.getTitleAt(i));
                if (DEBUG)
                    System.out.println("Stop flashing " + tp.getTitleAt(i));
            }
        };
        shared.receiveTabs.addChangeListener(changeListener);

        return panel;
    }

    /**
     * The JPanel containing the area where the user provides input to
     *         the group chat
     * @return 
     */
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

            public void keyTyped(KeyEvent e)
            {
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

    /**
     * A Panel containing the userlist
     * 
     * @return The JPanel containing the Userlist and its ActionListeners.
     */
    public JPanel pnlUsers()
    {
        JPanel panel = new JPanel(new BorderLayout());

        if (!offlineMode)
        {
            shared.userList
                    .setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            shared.userList.setCellRenderer(new MyListCellRenderer());
            shared.userList.setFixedCellWidth(25);
            shared.userList.setFixedCellHeight(25);

            MouseListener mouseListener = new MouseAdapter()
            {
                public void mouseClicked(MouseEvent e)
                {
                    int i = shared.userList.locationToIndex(e.getPoint());
                    if (e.getClickCount() == 2)
                    {
                        if (DEBUG)
                        {
                            System.out.println("Double clicked on Item " + i);
                            System.out.println("Double clicked on Item: "
                                    + shared.userList.getModel()
                                            .getElementAt(i));
                        }
                        client.initiateChat((String) shared.userList
                                .getSelectedValue());
                        shared.receiveTabs.setSelectedIndex(shared.receiveTabs
                                .indexOfTab((String) shared.userList
                                        .getSelectedValue()));
                    }
                    else if ((e.getButton() == MouseEvent.BUTTON3)
                            && (shared.userList.locationToIndex(e.getPoint()) != -1))
                    {
                        /* pop up for viewing users profile/stats etc"); */
                        shared.userList.setSelectedIndex(shared.userList
                                .locationToIndex(e.getPoint()));

                        JPopupMenu popupMenu = new JPopupMenu();
                        JMenuItem chat = new JMenuItem("Chat with User");
                        JMenuItem showProfile = new JMenuItem(
                                "Show User's Profile");

                        chat.addActionListener(new ActionListener()
                        {
                            public void actionPerformed(ActionEvent e)
                            {
                                SwingUtilities.invokeLater(new Runnable()
                                {
                                    public void run()
                                    {
                                        client
                                                .initiateChat((String) shared.userList
                                                        .getSelectedValue());
                                    }
                                });
                            }
                        });
                        popupMenu.add(chat);

                        showProfile.addActionListener(new ActionListener()
                        {
                            public void actionPerformed(final ActionEvent e)
                            {
                                SwingUtilities.invokeLater(new Runnable()
                                {
                                    public void run()
                                    {
                                        // Changed by Andrew to remove need for
                                        // thread.sleep()
                                        // See client message listener for new
                                        // way of displaying profile pane
                                        Profile.requestProfile(
                                                (String) shared.userList
                                                        .getSelectedValue(),
                                                true, client.botChat);
                                    }
                                });
                            }
                        });
                        popupMenu.add(showProfile);

                        // display popup near location of mouse click
                        popupMenu.show(e.getComponent(), e.getX(),
                                e.getY() - 10);
                    }
                }
            };
            shared.userList.addMouseListener(mouseListener);

            JScrollPane userListScroll = new JScrollPane(shared.userList);
            // userListScroll.setBorder(emptyBorder);

            shared.userCount.setText(" " + shared.userListModel.getSize()
                    + " Users Online");

            JPanel usersHeader = new JPanel(new FlowLayout(FlowLayout.LEFT));
            Box box = Box.createHorizontalBox();

            URL u = this.getClass().getResource("iconusers.png");
            ImageIcon image = new ImageIcon(u);
            JLabel lblImage = new JLabel(image);
            box.add(lblImage);
            box.add(shared.userCount);
            usersHeader.add(box);

            panel.add(usersHeader, BorderLayout.NORTH);
            panel.add(userListScroll);
            panel.setMinimumSize(new Dimension(0, 100));

            panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0,
                    Color.BLACK));
        }
        return panel;
    }

    /**
     * This method sets up the profile by asking the Bot if it has the user on
     * its record. If so, the profile updates appropriately. If not, a new
     * profile is created.
     * 
     * @author Jon, Andrew
     */
    @SuppressWarnings("static-access")
    private void profileSetup()
    {
        Profile.requestProfile(username, false, client.botChat);

        if (client.colours.containsKey(username))
            client.colours.remove(username);
        client.colours.put(username, myProfile.getColour());

        retrieveAllUserColours();
    }

    /**
     * Reset your own profile to the default state.
     * 
     * @author Jon
     */
    private void resetProfile()
    {
        int response = JOptionPane.showConfirmDialog(null,
                "Are you sure you wish to reset your profile?");
        if (response == 0)
        {
            myProfile = new Profile(username);
            myProfile.setColour(150, 150, 150);
            announceColourChange(150, 150, 150);
            resetStartTime();
        }
        else
            return;
    }

    /**
     * To be used whenever the profile is updated. This sets the startTime
     * variable to be when the profile was last updated.
     * 
     * @author Jon
     */
    public void resetStartTime()
    {
        startTime = System.currentTimeMillis();
    }

    /**
     * Retrieves a list of the most up-to-date profile colours from the Bot.
     * Colours are stored in a HashMap in client.colours
     * 
     * @author Jon
     */
    @SuppressWarnings("static-access")
    private void retrieveAllUserColours()
    {
        for (int i = 0; i < shared.userListModel.getSize(); i++)
        {
            String focus = (String) shared.userListModel.elementAt(i);
            if (!focus.equals(username))
            {
                getUserColour(focus);
                try
                {
                    Thread.sleep(2000);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                if (client.colours.containsKey(focus))
                    client.colours.remove(focus);
                client.colours.put(focus, client.incomingColour);
            }
        }
    }

    /**
     * Runs a file using the command javac
     */
    void runFile()
    {
        try
        {
            @SuppressWarnings("unused")
            Process p;
            // int i = 0;
            String name = client.getCurrentDocument().name.replace( ".java", "" );
            if( isWindows() )
             p = Runtime.getRuntime().exec( "cmd /C start cmd /K \"cd \"" + currentDir +
                    "\" && java " + name + "\"" );
            else if( isUnix() )
            {
            	System.out.println("xterm -hold -e cd \"" + currentDir + "\" && \"java " + name + "\"" );
            	p = Runtime.getRuntime().exec( "xterm -hold -e cd \"" + currentDir + "\" && \"java " + name + "\"" );
            }
            	
//            BufferedReader input = new BufferedReader(new InputStreamReader(p
//                    .getInputStream()));
//            System.out.println( p.getInputStream().available() );
//            while ((line = input.readLine()) != null)
//            {
//                debugwindow.println(line);
//            }
//            input.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Using this, users can select a picture for their profile.
     * 
     * @author Jon
     */
    protected void selectPicture()
    {
        JFileChooser fc = new JFileChooser();
        ImagePreview p = new ImagePreview();

        FileFilter filter = new FileNameExtensionFilter(
                "Image file - .jpg, .png, .bmp, .gif", "jpg", "jpeg", ".png",
                ".bmp", ".gif");
        fc.addChoosableFileFilter(filter);
        fc.setAccessory(p);
        fc.addPropertyChangeListener(p);
        fc.setAcceptAllFileFilterUsed(false);

        int rVal = fc.showOpenDialog(null);
        if (rVal == JFileChooser.APPROVE_OPTION)
        {
            profilePictureDir = fc.getSelectedFile().getAbsolutePath();
        }
    }

    /**
     * Sends a chat message to the chatroom
     * 
     * @param message
     *            The String that is being sent to the XMPP chatroom.
     */
    protected void sendChatMessage(String message)
    {
        if (!message.equals("") && !message.equals("\n"))
        {
            client.sendChatMessageFromGUI(message);
            messageSendBox.setText("");
        }
    }

    /**
     * Opens an "About CIDEr" window.
     */
    private void showAbout()
    {
        JFrame frame = new JFrame("About CIDEr");
        Container content = frame.getContentPane();
        // content.setLayout(new GridLayout(10, 2));

        URL x1 = this.getClass().getResource("icon.png");
        ImageIcon image = new ImageIcon(x1);
        Image test = image.getImage();
        frame.setIconImage(test);

        // frame.setBounds(100, 100, 400, 350);
        frame.setResizable(false);
        frame.isDisplayable();
        frame.setLocationRelativeTo(null);

        /*
         * JLabel chars = new
         * JLabel("CIDEr- Collaborative Integrated Development EnviRonment.");
         * content.add(chars);
         */

        URL u = this.getClass().getResource("ciderabout.png");
        ImageIcon image1 = new ImageIcon(u);
        JLabel lblImage = new JLabel(image1);
        content.add(lblImage);

        frame.pack();
        int x = (int) (w.getX() + w.getWidth() / 2);
        int y = (int) (w.getY() + w.getHeight() / 2);
        frame.setLocation(x - frame.getWidth() / 2, y - frame.getHeight() / 3);
        frame.setVisible(true);
    }

    /**
     * Show a popup window of the requested profile.
     * 
     * @author Jon, Andrew
     * @return false if profile didn't exist.
     */
    public boolean showProfile(Profile profile)
    {
        if (profile == null)
            return false;
        System.out.println("Showing profile " + profile.toString());
        JFrame profileFrame = new JFrame("View Profile");
        Container content = profileFrame.getContentPane();

        // set frame icon to cider logo
        URL x = this.getClass().getResource("icon.png");
        ImageIcon image = new ImageIcon(x);
        Image test = image.getImage();
        profileFrame.setIconImage(test);

        // horizontal box with user image in left cell, info in right cell
        Box hbox = Box.createHorizontalBox();
        hbox.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        URL urlImage;
        Image img;
        ImageIcon photo;

        try
        {
            // load custom user photo here
            if (profile.getUsername().equals(username)
                    && profilePictureDir != null)
            {
                img = Toolkit.getDefaultToolkit().getImage(profilePictureDir);
                photo = new ImageIcon(img);
            }
            else
            {
                urlImage = this.getClass().getResource(
                        profile.getUsername() + ".jpg");
                photo = new ImageIcon(urlImage);
            }
        }
        catch (NullPointerException npe)
        {
            // load default user photo if custom user doesn't exist
            if (CiderApplication.debugApp)
                System.out.println("No profile image for "
                        + profile.getUsername() + ", using default.");
            urlImage = this.getClass().getResource("defaultuser.png");
            photo = new ImageIcon(urlImage);
        }
        img = photo.getImage();
        img = img.getScaledInstance(150, 200, Image.SCALE_SMOOTH);
        photo = new ImageIcon(img);

        JLabel userPhoto = new JLabel(photo);
        userPhoto.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));
        hbox.add(userPhoto);

        JLabel userName = new JLabel("<html><u>Username: "
                + profile.getUsername() + "</u></html>");
        Font curFont = userName.getFont();
        userName.setFont(new Font(curFont.getFontName(), curFont.getStyle(),
                curFont.getSize() + 2));

        JLabel userChars = new JLabel("Characters Typed: "
                + profile.getTypedChars());
        JLabel userTime = new JLabel("Total Time: "
                + Profile.getTimeString(profile.getTimeSpent()));
        JLabel idleTime = new JLabel("Idle Time: "
                + Profile.getTimeString(profile.getIdleTime() * 1000) + " ("
                + profile.idlePercentString() + "%)");
        JLabel userLastOnline = new JLabel("Last Seen: "
                + profile.getLastOnline());
        // JLabel userFontSize = new JLabel("Font Size: " +
        // profile.userFontSize);

        // vertical box with user statistics in
        Box vbox = Box.createVerticalBox();

        vbox.add(userName);
        vbox.add(userChars);
        vbox.add(userTime);
        vbox.add(idleTime);
        vbox.add(userLastOnline);
        // vbox.add(userFontSize);

        hbox.add(vbox);
        content.add(hbox);

        profileFrame.pack();
        profileFrame.setResizable(false);
        profileFrame.isDisplayable();
        profileFrame.setLocationRelativeTo(null);

        profileFrame.setVisible(true);

        return true;
    }

    /**
     * Panel for the editor area
     * @return The editor panel
     */
    public JPanel sourceEditorSection()
    {
        // tabbedPane.addTab(currentFileName, new SourceEditor(
        // currentFileContents, currentDir));
        // tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(640, 480));
        panel.add(shared.tabbedPane);
        return panel;
    }

    /**
     * Creates the JFrame for the main application
     * @param loginWindow
     * @param debugApp Whether in debug mode
     */
    public void startApplication(JFrame loginWindow, boolean debugApp)
    {
        w = new JFrame("CIDEr - Logged in as " + username);
        shared.idleTimer = new IdleTimer(client, true);

        // Detect mouse events across whole window
        // Filter only motion events to set not idle
        long eventMask = AWTEvent.MOUSE_MOTION_EVENT_MASK;

        activityListener = new AWTEventListener()
        {
            public void eventDispatched(AWTEvent e)
            {
                if (shared.idleTimer.userIsIdle())
                {
                    myProfile.uploadProfile(client.botChat, startTime,
                            shared.idleTimer.getTotalIdleTime());
                    resetStartTime();
                    shared.idleTimer.resetTotal();
                }
                shared.idleTimer.activityDetected();
            }
        };

        Toolkit.getDefaultToolkit().addAWTEventListener(activityListener,
                eventMask);

        if (!offlineMode)
        {
            client.getFileListFromBot();
        }
        w.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        URL x = this.getClass().getResource("icon.png");
        ImageIcon image = new ImageIcon(x);
        Image test = image.getImage();
        w.setIconImage(test);

        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        p.add(this.mainMenuBar(), BorderLayout.PAGE_START);
        p.add(this.mainArea(), BorderLayout.CENTER);

        statusBar = new StatusBar();
        statusBar.setUsername(username);
        statusBar.setInputMode("INSERT");
        p.add(statusBar, BorderLayout.SOUTH);

        w.add(p);
        w.pack();

        dirSourceEditorSelectionSplit.setDividerLocation(0.25);
        editorChatSplit.setDividerLocation(0.75);
        int left = loginWindow.getX();
        int top = loginWindow.getY();
        w.setLocation(left > 0 ? left : 0, top > 0 ? top : 0);
        w.setVisible(true);
        if (!offlineMode)
            w.setExtendedState(JFrame.MAXIMIZED_BOTH);
        w.addWindowListener(new WindowListener()
        {
            public void windowActivated(WindowEvent arg0)
            {
            }

            public void windowClosed(WindowEvent arg0)
            {
                // try
                // {
                // if (!offlineMode)
                // {
                // myProfile.uploadProfile(client.botChat, startTime,
                // idleTimer.getTotalIdleTime() );
                // if (DEBUG)
                // System.out.println("disconnecting");
                // client.disconnect();
                // }
                // idleTimer.stop();
                // }
                // catch(Exception e)
                // {
                // e.printStackTrace();
                // }
            }

            @Override
            public void windowClosing(WindowEvent arg0)
            {
                try
                {
                    if (!offlineMode)
                    {
                        myProfile.uploadProfile(client.botChat, startTime,
                                shared.idleTimer.getTotalIdleTime());
                        shared.idleTimer.resetTotal();
                        login.logout();
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            public void windowDeactivated(WindowEvent arg0)
            {
            }

            public void windowDeiconified(WindowEvent arg0)
            {
            }

            public void windowIconified(WindowEvent arg0)
            {
            }

            public void windowOpened(WindowEvent arg0)
            {
            }

        });
    }

    /**
     * Starts the Source History.
     */
    private void startSourceHistory()
    {
        // DHVSourceHistoryPane shp = new DHVSourceHistoryPane();
        // JDialog sourceHistoryDialog = new JDialog(this.w,
        // this.currentFileName
        // + " Source History");
        // sourceHistoryDialog.add(shp);
        // shp.setSize(this.w.getSize());
        // sourceHistoryDialog.setPreferredSize(this.w.getSize());
        // sourceHistoryDialog.setVisible(true);
        try
        {
            DocumentProperties docProperties;
            docProperties = client.currentDocumentProperties;

            DocumentHistoryViewer dhv = new DocumentHistoryViewer(
                    new SourceDocument(docProperties.name), client);

            dhv.setDefaultColor(Color.WHITE);

            dhv.updateText();
            dhv.setWaiting(false);

            TimeBorderList tbl = new TimeBorderList(docProperties);

            ArrayList<Long> borderTimes = new ArrayList<Long>();
            System.out.println(docProperties.creationTime);
            borderTimes.add(docProperties.creationTime);
            tbl.loadLocalBorderTimes(borderTimes);
            long endTime = System.currentTimeMillis() + client.getClockOffset();
            tbl.addTimeBorder(new TimeBorder(client.currentDocumentProperties, endTime, true));
            tbl.loadLocalEvents();
            tbl.createRegions();
            TimeRegionBrowser trb = new TimeRegionBrowser(tbl, 128, 600);

            DHVSourceHistoryPane shp = new DHVSourceHistoryPane(128);
            shp.setDocumentHistoryViewer(dhv);
            shp.setTimeRegionBrowser(trb);

            JDialog w = new JDialog(this.w, false);
            w.setTitle(docProperties.path + " History");
            w.setPreferredSize(new Dimension(600, 600));
            w.setLayout(new BorderLayout());
            w.add(shp);
            w.pack();

            trb.setScale(TimeRegionBrowser.defaultScale);

            w.setVisible(true);
            w.setAlwaysOnTop(true);
            
            trb.setEyePosition(System.currentTimeMillis() + client.getClockOffset() - 1);
            shp.scrollRegionBrowserToEnd();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }
    public class Error
    {
        public void errorMessage(String message, String title)
        {
            JOptionPane.showMessageDialog(w, message, title,
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    @SuppressWarnings("serial")
    class MyListCellRenderer extends JLabel implements ListCellRenderer
    {
        boolean selected = false;
        int index;
        String name;

        public Component getListCellRendererComponent(JList paramlist,
                Object value, int index, boolean isSelected,
                boolean cellHasFocus)
        {
            selected = isSelected;
            this.index = index;
            name = value.toString();

            return this;
        }

        @SuppressWarnings("static-access")
        public void paint(Graphics g)
        {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            Color bg;

            if (selected)
            {
                bg = Color.GRAY;
            }
            else
            {
                bg = Color.DARK_GRAY;
            }

            // fill background
            g2d.setColor(bg);
            g2d.fillRect(0, 0, getWidth(), getHeight());

            // draw coloured rectangle and name
            g2d.setColor(Color.WHITE);
            g2d.fillRoundRect(6, 6, 13, 13, 6, 6);
            // g.fillRect(6, 6, 13, 13);
            g2d.setColor(client.colours.get(name)); // get unique user color
                                                    // here
            g2d.fillRoundRect(7, 7, 11, 11, 6, 6);

            String idleString = "";
            if (Client.usersIdle.contains(name))
            {
                g2d.setColor(Color.WHITE);
                g.fillOval(12, 12, 9, 9);
                g.setColor(new Color(220, 50, 50));
                g.fillOval(13, 13, 7, 7);
                g.setColor(Color.WHITE);
                g.drawLine(16, 13, 16, 17);
                g.drawLine(16, 17, 18, 16);
                idleString = " (idle)";
            }

            g.setColor(Color.WHITE);
            g.drawString(name + idleString, 26, 17);
        }
    }

    static class ProgExit extends TimerTask
    {
        public void run()
        {
            System.exit(0);
        }
    }

    private static boolean DEBUG = false;

    /**
     * Adds an item to the dropdown menu specified in the parameters.
     * 
     * @param menu
     *            The menu that the dropdown item is being added to.
     * @param name
     *            The String that will appear on the new menu item.
     * @param keyEvent
     *            The keystroke that will act as a shortcut for this menu item.
     *            -1 for no keystroke.
     * @param a
     *            The ActionListener that listens to the menu items' clicks.
     */
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
     * 
     * @return True if operating system is Mac, False if not.
     */
    public static boolean isMac()
    {
        String os = System.getProperty("os.name").toLowerCase();
        // Mac
        return (os.indexOf("mac") >= 0);
    }

    /**
     * 
     * @return True if operating system is Unix, False if not.
     */
    public static boolean isUnix()
    {

        String os = System.getProperty("os.name").toLowerCase();
        // linux or unix
        return (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0);
    }

    /**
     * 
     * @return True if operating system is Windows, False if not.
     */
    public static boolean isWindows()
    {
        String os = System.getProperty("os.name").toLowerCase();
        // windows
        return (os.indexOf("win") >= 0);
    }

    /**
     * The MainWindow offline entry point - online features are not available in
     * this GUI debugging version.
     * 
     * @param args
     */
    public static void main(String[] args)
    {
        DEBUG = true;
        MainWindow app = new MainWindow();
        app.offlineMode = true;
        app.startApplication(new JFrame(), true);
        app.w.setTitle("MainWindow offline entry point");
    }

    /**
     * Exits the program after 15000ms.
     */
    public static void TimedExit()
    {
        Timer timer;
        timer = new Timer();
        timer.schedule(new ProgExit(), 15000);
    }
}
