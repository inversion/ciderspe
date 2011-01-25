package cider.client.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Hashtable;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import cider.common.network.Client;

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

    public static void main(String[] args)
    {
        MainWindow program = new MainWindow();
        SwingUtilities.invokeLater(program);
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

            }
        };
        return AL;
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
        addMenuItem(menu, "Save As", -1, aL);
        addMenuItem(menu, "Close File", KeyEvent.VK_F4, aL);
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

        // the DEV(eloper) menu is for us to test back-end things such as saving
        // and pushing
        // NYI = not yet implemented
        menu = new JMenu("DEV");
        menuBar.add(menu);

        addMenuItem(menu, "Push file to server (NYI)", -1, aL);
        addMenuItem(menu, "Get file list from server (NYI)", -1, aL);
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

    public JPanel mainArea()
    {
        DirectoryViewComponent dirView = new DirectoryViewComponent();
        client = new Client(dirView, this.tabbedPane, this.openTabs);
        dirView.setClient(client);
        client.getFileList();

        JPanel panel = new JPanel(new BorderLayout());
        dirSourceEditorSeletionSplit = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT, dirView/* label */, this
                        .sourceEditorSection());// textArea/*label2*/);
        editorChatSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                dirSourceEditorSeletionSplit, new JLabel("chat"));

        dirSourceEditorSeletionSplit.setOneTouchExpandable(true);
        // splitPane.setDividerLocation(150);
        editorChatSplit.setOneTouchExpandable(true);
        this.editorChatSplit.setResizeWeight(1.0);
        // splitPane2.setDividerLocation(150);

        // Provide minimum sizes for the two components in the split pane
        panel.add(editorChatSplit);
        return panel;
    }

    public void run()
    {
        w = new JFrame("CIDEr");
        w.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        URL x = this.getClass().getResource("icon.png");
        ImageIcon image3 = new ImageIcon(x);
        Image test3 = image3.getImage();
        w.setIconImage(test3);

        JPanel p = new JPanel(new BorderLayout());
        p.add(this.mainMenuBar(), BorderLayout.PAGE_START);
        p.add(this.mainArea());
        w.add(p);
        w.pack();
        this.dirSourceEditorSeletionSplit.setDividerLocation(0.25);
        this.editorChatSplit.setDividerLocation(0.8);
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
                // TODO Auto-generated method stub

            }

            @Override
            public void windowClosing(WindowEvent arg0)
            {
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
