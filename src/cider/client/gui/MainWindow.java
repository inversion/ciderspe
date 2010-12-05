package cider.client.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
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
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

class MainWindow implements Runnable
{
    JFrame w;

    public static void main(String[] args)
    {
        MainWindow program = new MainWindow();
        SwingUtilities.invokeLater(program);
    }

    public static void addMenuItem(JMenu menu, String name, int keyEvent)
    {
        JMenuItem menuItem = new JMenuItem(name);
        if (keyEvent != -1)
            menuItem.setAccelerator(KeyStroke.getKeyStroke(keyEvent,
                    ActionEvent.CTRL_MASK));
        menu.add(menuItem);
    }

    public JMenuBar mainMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu;

        // menu 1
        menu = new JMenu("File");
        menuBar.add(menu);

        addMenuItem(menu, "New", KeyEvent.VK_N);
        addMenuItem(menu, "Open", KeyEvent.VK_O);
        addMenuItem(menu, "Save", KeyEvent.VK_S);
        addMenuItem(menu, "Save As", -1);
        addMenuItem(menu, "Quit", KeyEvent.VK_Q);

        // menu 2
        menu = new JMenu("Edit");
        menuBar.add(menu);

        addMenuItem(menu, "Cut", KeyEvent.VK_X);
        addMenuItem(menu, "Copy", KeyEvent.VK_C);
        addMenuItem(menu, "Paste", KeyEvent.VK_V);

        // menu 3
        menu = new JMenu("Help");
        menuBar.add(menu);

        addMenuItem(menu, "About", -1);

        return menuBar;
    }

    public JPanel sourceEditor()
    {
        JPanel panel = new JPanel(new BorderLayout());
        // text area
        JTextArea textArea;
        textArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane
                .setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setPreferredSize(new Dimension(250, 250));
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
        JPanel panel = new JPanel(new BorderLayout());
        JSplitPane splitPane;
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new DirectoryViewComponent()/* label */,
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
