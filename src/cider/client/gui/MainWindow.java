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
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;

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
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.util.StringUtils;

import cider.common.network.client.Client;
import cider.common.processes.Profile;
import cider.shared.ClientSharedComponents;
import cider.specialcomponents.editorTypingArea.EditorTypingArea;

public class MainWindow
{
    JFrame w;
    public String currentDir = System.getProperty("user.dir");
    public String currentFileName = "Unsaved Document 1";
    public String currentFileContents = "";
    public int currentTab = 0;

    public LoginUI login;

    public ClientSharedComponents shared;

    Client client;
    private JSplitPane dirSourceEditorSeletionSplit;
    private JSplitPane editorChatSplit;
    private String username;
    private ArrayList<String> savedFiles = new ArrayList<String>();

    public static final String GROUPCHAT_TITLE = "Group Chat";

    public JPanel receivePanel;
    public JTextArea messageSendBox;
    public static boolean LockingEnabled = true;

    private DebugWindow debugwindow;
    private OutputStream baos;

    tabFlash tabbing = new tabFlash();

    /**
     * These variable are for the profiles
     * 
     * @author Jon
     */
    public long startTime;
    private Profile myProfile;

    MainWindow(String username, String password, String host, int port,
            String serviceName, Client c, LoginUI loginUI,
            ClientSharedComponents shared) throws XMPPException
    {
        // Register GUI components shared with client
        this.shared = shared;

        // TODO: Should more stuff be in the constructor rather than the
        // mainArea method? The variables look a bit of a mess
        startTime = System.currentTimeMillis();
        this.username = username;
        login = loginUI;

        client = c;
        receivePanel = pnlReceive();
        shared.dirView.setClient(client);
        client.addParent(this);
        profileSetup();

        this.baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(new BufferedOutputStream(this.baos));
        // System.setOut(ps);
        System.setErr(ps);

        // tabFlash.flash(0);
    }

    private void profileSetup()
    {
        /**
         * This method sets up the profile by asking the Bot if it has the user
         * on its record. If so, the profile updates appropriately. If not, a
         * new profile is created.
         * 
         * @author Jon
         */
        getProfileFromBot();

        try
        {
            /**
             * This is to negate the effect of latency on checking the new
             * profile
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

        // Inform the bot of the user's current colour
        botColourChange(myProfile.userColour.getRed(),
                myProfile.userColour.getGreen(), myProfile.userColour.getBlue());

        if (client.colours.containsKey(username))
            client.colours.remove(username);
        client.colours.put(username, myProfile.userColour);
        System.out.println(myProfile);

        retrieveAllUserColours();
        announceColourChange(myProfile.userColour.getRed(),
                myProfile.userColour.getGreen(), myProfile.userColour.getBlue());
    }

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
     * When this method is called, the username and its colour will be added to
     * the hash table of user colours
     * 
     * @author Jon
     */
    private void getUserColour(String user)
    {
        try
        {
            client.botChat
                    .sendMessage(StringUtils.encodeBase64("requestusercolour "
                            + username + " " + user));

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
                    showMyProfile(myProfile);
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
                else if (action.equals("Export"))
                {
                    exportFile(action);
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
                    // eta.moveHome();
                }
                else if (action.equals("Line End"))
                {
                    // eta.moveEnd();
                }
                else if (action.equals("Document Home"))
                {
                    // eta.moveDocHome();
                }
                else if (action.equals("Document End"))
                {
                    // client.openTabs.get(this.getPathToSourceDocument(client.getCurrentDocument().name),
                    // 1));
                    // EditorTypingArea.moveDocEnd();
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

            // this.liveFolder = new LiveFolder("Bot", "root");
            // SourceDocument t1 =
            // this.liveFolder.makeDocument("t1.SourceDocument");
            // client.openTabFor(currentDir);
            try
            {
                // FileInputStream fis = new FileInputStream(currentDir +
                // currentFileName);
                // BufferedInputStream bis = new BufferedInputStream(fis);
                BufferedReader br = new BufferedReader(new FileReader(
                        currentDir));
                currentFileContents = "";
                while ((temp = br.readLine()) != null)
                {
                    currentFileContents = currentFileContents + temp + "\n";
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
                JOptionPane.showMessageDialog(new JPanel(),
                        "Error: " + e.getMessage());
                return;
            }

            // tabbedPane.addTab(currentFileName, new SourceEditor(
            // currentFileContents, currentDir));
            shared.tabbedPane.setSelectedIndex(++currentTab);
        }
    }

    public void exportFile(String action)
    {
        try
        {
            JFileChooser fc = new JFileChooser();
            File f = new File(client.getCurrentDocument().name /* + ".java" */);
            fc.setSelectedFile(f);

            if (currentFileName.equals("Unsaved Document 1")
                    || savedFiles.contains(client.getCurrentDocument().name) == false)
            {
                int watdo = fc.showSaveDialog(null);
                if (watdo != JFileChooser.APPROVE_OPTION)
                {
                    return;
                }
                savedFiles.add(client.getCurrentDocument().name);
                currentFileName = fc.getSelectedFile().getName();
                currentDir = fc.getSelectedFile().getAbsolutePath();
            }
            FileWriter fstream = new FileWriter(currentDir);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(client.getCurrentDocument().toString()/* currentFileContents */);
            out.close();
        }
        catch (IOException e1)
        {
            e1.printStackTrace();
            JOptionPane.showMessageDialog(new JPanel(),
                    ("Error: " + e1.getMessage()));
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
        String s = (String) JOptionPane.showInputDialog(new JPanel(),
                "Enter a filename:", "New File", JOptionPane.PLAIN_MESSAGE);
        if (s == null)
        {
            return;
        }
        // LiveFolder liveFolder = new LiveFolder(username,
        // this.client.getLiveFolder());
        // liveFolder.makeDocument(s);
        // TODO: create directory tree object with 's' then open it
        JLabel TEMP = new JLabel("blah blah blah");
        shared.tabbedPane.addTab(s, TEMP);// new
                                          // SourceEditor(currentFileContents,
        // currentDir)); //new SourceEditor("",
        // "\\."));
        shared.tabbedPane.setSelectedIndex(shared.tabbedPane.getTabCount() - 1);
    }

    // http://www.java2s.com/Code/Java/JDK-6/CompileaJavacode.htm
    /*
     * skank way of testing for now, saves file then attempts to run the created
     * .java file- Alex
     */
    void compileFile()
    {
        JFileChooser fc = new JFileChooser();
        File f = new File(client.getCurrentDocument().name + ".java");
        fc.setSelectedFile(f);

        int watdo = fc.showSaveDialog(null);
        if (watdo != JFileChooser.APPROVE_OPTION)
        {
            return;
        }

        currentFileName = fc.getSelectedFile().getName();
        currentDir = fc.getSelectedFile().getAbsolutePath();

        try
        {
            FileWriter fstream = new FileWriter(currentDir);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(client.getCurrentDocument().toString());
            out.close();
        }
        catch (IOException e1)
        {
            e1.printStackTrace();
        }

        /*
         * String sourceFile = currentDir; JavaCompiler compiler =
         * ToolProvider.getSystemJavaCompiler(); StandardJavaFileManager
         * fileManager = compiler.getStandardFileManager(null, null, null);
         * List<File> sourceFileList = new ArrayList<File>();
         * sourceFileList.add(new File(sourceFile)); Iterable<? extends
         * JavaFileObject> compilationUnits =
         * fileManager.getJavaFileObjectsFromFiles(sourceFileList);
         * CompilationTask task = compiler.getTask(null, fileManager, null,
         * null, null, compilationUnits); boolean result = task.call(); if
         * (result) { System.out.println("Compilation was successful"); } else {
         * System.out.println("Compilation failed"); } //fileManager.close();
         */

        JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
        // System.out.println(currentDir);
        int results = javac.run(System.in, System.out, System.err, currentDir);
        if (results == 0)
        {
            this.debugwindow.println("Compilation Successful");
        }
        else
        {
            this.debugwindow.println("Compilation failed");
        }
        updateOutput();
    }

    public void updateOutput()
    {
        this.debugwindow.println(this.baos.toString());
        /*
         * try { this.baos.flush(); } catch (IOException e) {
         * e.printStackTrace(); }
         */
    }

    void runFile()
    {
        try
        {
            String line;
            // int i = 0;
            Process p = Runtime.getRuntime().exec(this.getCommands());
            BufferedReader input = new BufferedReader(new InputStreamReader(
                    p.getInputStream()));
            while ((line = input.readLine()) != null)
            {
                this.debugwindow.println(line);
            }
            input.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public String[] getCommands()
    {
        if (isWindows())
        {
            return new String[] { "cmd.exe", "/C",
                    "start " + currentDir + /*
                                             * "java" +
                                             * "\\test"this.sourceFile.getParentFile
                                             * ().getPath() +
                                             */"\\runHello.bat" };
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

    public static boolean isWindows()
    {
        String os = System.getProperty("os.name").toLowerCase();
        // windows
        return (os.indexOf("win") >= 0);
    }

    public static boolean isMac()
    {
        String os = System.getProperty("os.name").toLowerCase();
        // Mac
        return (os.indexOf("mac") >= 0);
    }

    public static boolean isUnix()
    {

        String os = System.getProperty("os.name").toLowerCase();
        // linux or unix
        return (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0);
    }

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

    private void restartProfile()
    {
        int response = JOptionPane.showConfirmDialog(null,
                "Are you sure you wish to reset your profile?");
        if (response == 0)
        {
            myProfile = new Profile(username, client);
            myProfile.setColour(150, 150, 150);
            announceColourChange(150, 150, 150);
            startTime = System.currentTimeMillis();
        }
        else
            return;
    }

    public void announceColourChange(int r, int g, int b)
    {
        try
        {
            client.chatroom.sendMessage(StringUtils
                    .encodeBase64("colourchange: " + username + " " + r + " "
                            + g + " " + b));
        }
        catch (XMPPException e)
        {
            e.printStackTrace();
            JOptionPane.showMessageDialog(new JPanel(),
                    "Error: " + e.getMessage());
            return;
        }
    }

    private void botColourChange(int r, int g, int b)
    {
        try
        {
            client.botChat.sendMessage(StringUtils
                    .encodeBase64("colourchange: " + username + " " + r + " "
                            + g + " " + b));
        }
        catch (XMPPException e)
        {
            e.printStackTrace();
            JOptionPane.showMessageDialog(new JPanel(),
                    "Error: " + e.getMessage());
            return;
        }
    }

    private void showMyProfile(Profile myProfile)
    {
        if (username.equals(myProfile.uname))
        {
            System.out.println(myProfile.timeSpent);
            myProfile.updateTimeSpent(startTime);
            System.out.println(myProfile.timeSpent);
            startTime = System.currentTimeMillis();
        }

        // int count =
        // this.client.getCurrentDocument().playOutEvents(Long.MAX_VALUE).countCharactersFor(username);
        // myProfile.adjustCharCount(count);

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
        ImageIcon photo;

        try
        {
            // load custom user photo here
            urlImage = this.getClass().getResource(myProfile.uname + ".png");
            photo = new ImageIcon(urlImage);
        }
        catch (NullPointerException npe)
        {
            npe.printStackTrace();
            // load default user photo if custom user doesn't exist
            urlImage = this.getClass().getResource("defaultuser.png");
            photo = new ImageIcon(urlImage);
        }

        JLabel userPhoto = new JLabel(photo);
        userPhoto.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));
        hbox.add(userPhoto);

        JLabel userName = new JLabel("<html><u>Username: " + myProfile.uname
                + "</u></html>");
        Font curFont = userName.getFont();
        userName.setFont(new Font(curFont.getFontName(), curFont.getStyle(),
                curFont.getSize() + 2));

        JLabel userChars = new JLabel("Characters Typed: "
                + myProfile.typedChars);
        String t = myProfile.getTimeString();
        JLabel userTime = new JLabel("Total Time: " + t);
        JLabel userLastOnline = new JLabel("Last Seen: " + myProfile.lastOnline);

        // vertical box with user statistics in
        Box vbox = Box.createVerticalBox();

        vbox.add(userName);
        vbox.add(userChars);
        vbox.add(userTime);
        vbox.add(userLastOnline);

        hbox.add(vbox);
        content.add(hbox);

        profileFrame.pack();
        profileFrame.setResizable(false);
        profileFrame.isDisplayable();
        profileFrame.setLocationRelativeTo(null);

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
        frame.isDisplayable();
        frame.setLocationRelativeTo(null);

        JLabel chars = new JLabel(
                "CIDEr- Collaborative Integrated Development EnviRonment.");
        content.add(chars);

        frame.setVisible(true);
    }

    public void sendProfileToBot()
    {
        // FIXME:
        /*
         * int count =
         * this.client.getCurrentDocument().playOutEvents(Long.MAX_VALUE
         * ).countCharactersFor(username); myProfile.adjustCharCount(0);
         */

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
            client.botChat.sendMessage(StringUtils
                    .encodeBase64("requestprofile " + username));
            System.out.println("Requesting profile from server");
        }
        catch (XMPPException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * FIXME: UNUSED METHOD!
     * 
     */
    /*
     * private void tabClicked(MouseEvent e) { if (e.getButton() !=
     * MouseEvent.BUTTON1 && e.getClickCount() == 1) { // if is right-click
     * 
     * // create popup with Close menuitem JPopupMenu popupMenu = new
     * JPopupMenu(); JMenuItem closeBtn = new JMenuItem("Close");
     * closeBtn.addActionListener(new ActionListener() { public void
     * actionPerformed(ActionEvent e) { SwingUtilities.invokeLater(new
     * Runnable() { public void run() { // closeFile("Close File"); } }); } });
     * popupMenu.add(closeBtn);
     * 
     * // display popup near location of mouse click
     * popupMenu.show(e.getComponent(), e.getX(), e.getY() - 10); } }
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
        addMenuItem(menu, "Close File", KeyEvent.VK_F4, aL);
        addMenuItem(menu, "Logout", KeyEvent.VK_L, aL);
        addMenuItem(menu, "Quit", KeyEvent.VK_Q, aL);

        // menu 2
        menu = new JMenu("Edit");
        menuBar.add(menu);

        addMenuItem(menu, "Cut", KeyEvent.VK_X, aL);
        addMenuItem(menu, "Copy", KeyEvent.VK_C, aL);
        addMenuItem(menu, "Paste", KeyEvent.VK_V, aL);
        addMenuItem(menu, "Line Home", KeyEvent.VK_HOME, aL);
        addMenuItem(menu, "Line End", KeyEvent.VK_END, aL);
        addMenuItem(menu, "Document Home", KeyEvent.VK_HOME, aL);
        addMenuItem(menu, "Document End", KeyEvent.VK_END, aL);
        addMenuItem(menu, "Syntax Highlighting", -1, aL);
        addMenuItem(menu, "User Highlighting", -1, aL);
        addMenuItem(menu, "Line Locking", -1, aL);

        // menu x
        menu = new JMenu("Run");
        menuBar.add(menu);

        addMenuItem(menu, "Compile", KeyEvent.VK_F9, aL);
        addMenuItem(menu, "Run", KeyEvent.VK_F10, aL);

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
        panel.add(shared.tabbedPane);
        return panel;
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
            this.selected = isSelected;
            this.index = index;
            this.name = value.toString();

            return this;
        }

        public void paint(Graphics g)
        {
            Color bg;

            if (selected)
            {
                bg = Color.LIGHT_GRAY;
            }
            else
            {
                bg = Color.WHITE;
            }

            // fill background
            g.setColor(bg);
            g.fillRect(0, 0, getWidth(), getHeight());

            // draw coloured rectangle and name
            g.setColor(client.colours.get(name)); // get unique user color here
            g.fillRect(6, 6, 13, 13);
            g.drawString(name, 25, 17);
        }
    }

    @SuppressWarnings("serial")
    public class tabFlash extends JTabbedPane
    {
        public TabFlash tabflash(int requiredTabIndex)
        {
            TabFlash tabflash = new TabFlash(requiredTabIndex);
            tabflash.start();
            return tabflash;
        }

        public void tabflashstop(int requiredTabIndex)
        {
            // tabflash.stopflash();
        }

        public class TabFlash implements ActionListener
        {
            private Color background;
            private Color foreground;
            private Color oldBackground;
            private Color oldForeground;
            private int requiredTabIndex;
            private boolean flashon = false;
            private Timer timer = new Timer(1000, this);

            public TabFlash(int requiredTabIndex)
            {
                this.requiredTabIndex = requiredTabIndex;
                this.oldForeground = shared.receiveTabs.getForeground();
                this.oldBackground = shared.receiveTabs.getBackground();
                this.foreground = Color.BLACK;
                this.background = Color.ORANGE;
            }

            public void start()
            {
                timer.start();
            }

            public void actionPerformed(ActionEvent e)
            {
                flash(flashon);
                flashon = !flashon;
            }

            public void flash(boolean flashon)
            {
                if (flashon)
                {
                    if (foreground != null)
                    {
                        setForegroundAt(requiredTabIndex, foreground);
                    }
                    if (background != null)
                    {
                        setBackgroundAt(requiredTabIndex, background);
                    }
                }
                else
                {
                    if (oldForeground != null)
                    {
                        setForegroundAt(requiredTabIndex, oldForeground);
                    }
                    if (oldBackground != null)
                    {
                        setBackgroundAt(requiredTabIndex, oldBackground);
                    }
                }
                repaint();
            }

            public void stopflash()
            {
                timer.stop();
                setForegroundAt(requiredTabIndex, oldForeground);
                setBackgroundAt(requiredTabIndex, oldBackground);
            }
        }
    }

    public JPanel pnlUsers()
    {
        /* panel for the list of online users */
        JPanel panel = new JPanel(new BorderLayout());

        /**
         * FIXME: this variable is never used!
         */
        @SuppressWarnings("unused")
        Border emptyBorder = BorderFactory.createEmptyBorder();

        // temporarily added so there were always users online
        /*
         * userListModel.add(0, "Person 1"); userListModel.add(1, "Person 2");
         * userListModel.add(2, "Person 3"); userListModel.add(3, "Person 4");
         * userListModel.add(4, "Person 5");
         */

        shared.userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        shared.userList.setCellRenderer(new MyListCellRenderer());
        shared.userList.setFixedCellWidth(25);
        shared.userList.setFixedCellHeight(25);

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
                int i = shared.userList.locationToIndex(e.getPoint());
                if (e.getClickCount() == 2)
                {
                    System.out.println("Double clicked on Item " + i);
                    System.out.println("Double clicked on Item: "
                            + shared.userList.getModel().getElementAt(i));
                    client.initiateChat((String) shared.userList
                            .getSelectedValue());
                }
                else if ((e.getButton() == MouseEvent.BUTTON3)
                        && (shared.userList.locationToIndex(e.getPoint()) != -1))
                {
                    /* pop up for viewing users profile/stats etc"); */
                    shared.userList.setSelectedIndex(shared.userList
                            .locationToIndex(e.getPoint()));

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
                                    client.initiateChat((String) shared.userList
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
                                    /**
                                     * TODO: - Get the name from the thing the
                                     * user right clicked - Send
                                     * "requestprofile USERNAME" to the bot -
                                     * Wait for 500ms - Check in Client to see
                                     * the new Profile that has appeared :)
                                     */
                                    try
                                    {
                                        System.out.println(client.profileFound);
                                        client.botChat.sendMessage(StringUtils.encodeBase64("requestprofile "
                                                + shared.userList
                                                        .getSelectedValue()
                                                + " notme"));
                                        Thread.sleep(1000);
                                        System.out.println(client.profileFound);
                                        if (!client.profileFound)
                                            JOptionPane
                                                    .showMessageDialog(
                                                            new JPanel(),
                                                            "Error: user profile not found on server");
                                        else
                                        {
                                            showMyProfile(client.notMyProfile);
                                            client.profileFound = false;
                                            client.notMyProfile = null;
                                        }
                                    }
                                    catch (XMPPException e)
                                    {
                                        e.printStackTrace();
                                        JOptionPane.showMessageDialog(
                                                new JPanel(),
                                                "Error: " + e.getMessage());
                                    }
                                    catch (InterruptedException e)
                                    {
                                        e.printStackTrace();
                                    }
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
        shared.userList.addMouseListener(mouseListener);

        JScrollPane userListScroll = new JScrollPane(shared.userList);
        // userListScroll.setBorder(emptyBorder);

        shared.userCount.setText(" " + shared.userListModel.getSize()
                + " Users Online");
        panel.add(shared.userCount, BorderLayout.NORTH);
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

        // receiveTabs = new JTabbedPane();

        panel.add(new JLabel(" User Chat"), BorderLayout.NORTH);
        panel.add(shared.receiveTabs, BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(0, 800));

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
                tabbing.tabflashstop(i);
                System.out.println("Stop flashing " + tp.getTitleAt(i));
            }
        };
        tabbing.addChangeListener(changeListener);

        return panel;
    }

    private void FlipHighlighting(int i)
    {
        EditorTypingArea.Highlighting = i;
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
            public void keyTyped(KeyEvent e)
            {
            }

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
        this.debugwindow = new DebugWindow();
        this.debugwindow.setAutoscrolls(true);

        JSplitPane EditorDebugSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                this.sourceEditorSection(), this.debugwindow/* test */);
        EditorDebugSplit.setBorder(emptyBorder);
        EditorDebugSplit.setOneTouchExpandable(true);
        EditorDebugSplit.setDividerLocation(800);

        JPanel panel = new JPanel(new BorderLayout());
        dirSourceEditorSeletionSplit = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT, shared.dirView, EditorDebugSplit);
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
    public void startApplication(JFrame loginWindow)
    {
        w = new JFrame("CIDEr - Logged in as " + username);

        // FIXME:
        // client.startClockSynchronisation(w);

        // Comment this out if clock synchronisation is being used
        client.getFileListFromBot();

        w.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        URL x = this.getClass().getResource("icon.png");
        ImageIcon image = new ImageIcon(x);
        Image test = image.getImage();
        w.setIconImage(test);

        JPanel p = new JPanel(new BorderLayout());
        p.add(this.mainMenuBar(), BorderLayout.PAGE_START);
        p.add(this.mainArea());
        w.add(p);

        tabbing.addTab("Tab 1", new JLabel("oh"));
        tabbing.addTab("Tab 2", new JLabel("hai"));
        tabbing.addTab("Tab 3", new JLabel("guise"));
        tabbing.tabflash(0);
        tabbing.tabflash(1);
        // w.add(tabbing);

        w.pack();
        this.dirSourceEditorSeletionSplit.setDividerLocation(0.25);
        this.editorChatSplit.setDividerLocation(0.75);
        int left = loginWindow.getX();
        int top = loginWindow.getY();
        w.setLocation(left > 0 ? left : 0, top > 0 ? top : 0);
        w.setVisible(true);
        w.setExtendedState(JFrame.MAXIMIZED_BOTH);
        w.addWindowListener(new WindowListener()
        {
            @Override
            public void windowClosing(WindowEvent arg0)
            {
                myProfile.updateTimeSpent(startTime);
                myProfile.updateProfileInfo();
                sendProfileToBot();
                System.out.println("disconnecting");
                client.disconnect();
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

            public void windowClosed(WindowEvent arg0)
            {
            }

            public void windowActivated(WindowEvent arg0)
            {
            }

        });
    }

    public class Error
    {
        public void errorMessage(String message, String title)
        {
            JOptionPane.showMessageDialog(w, message, title,
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void killWindow()
    {
        w.dispose();
    }
}
