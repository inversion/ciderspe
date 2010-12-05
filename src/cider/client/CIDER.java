package cider.client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
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
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;

class CIDER implements Runnable
{
    JFrame w;

    public static void main(String[] args)
    {
        CIDER program = new CIDER();
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

    public void run()
    {

        // http://java.sun.com/products/jlf/ed1/dg/higk.htm

        // text area
        JTextArea textArea;
        textArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane
                .setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setPreferredSize(new Dimension(250, 250));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        // tabs
        // http://download.oracle.com/javase/tutorial/uiswing/examples/components/TabbedPaneDemoProject/src/components/TabbedPaneDemo.java
        JTabbedPane tabbedPane = new JTabbedPane();
        // ImageIcon icon = createImageIcon("icon.png");

        // JComponent panel1 = makeTextPanel("Panel #1");
        tabbedPane.addTab("Tab 1", textArea);
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
        /*
         * //JComponent panel2 = makeTextPanel("Panel #2");
         * tabbedPane.addTab("Tab 2", textArea); tabbedPane.setMnemonicAt(1,
         * KeyEvent.VK_2);
         * 
         * //JComponent panel3 = makeTextPanel("Panel #3");
         * tabbedPane.addTab("Tab 3", textArea); tabbedPane.setMnemonicAt(2,
         * KeyEvent.VK_3);
         * 
         * //JComponent panel4 = makeTextPanel(
         * "Panel #4 (has a preferred size of 410 x 50).");
         * //panel4.setPreferredSize(new Dimension(410, 50));
         * tabbedPane.addTab("Tab 4", textArea); tabbedPane.setMnemonicAt(3,
         * KeyEvent.VK_4);
         */

        // file tree
        // http://www.java2s.com/Code/Java/File-Input-Output/FileTreeDemo.htm
        /*
         * File root = new File("C"); FileTreeModel model= new
         * FileTreeModel(root);
         */

        DefaultMutableTreeNode top = new DefaultMutableTreeNode(
                "Some root folder");
        createNodes(top);

        JTree tree = new JTree(top);
        // tree.setModel(model);

        JScrollPane scrollpane = new JScrollPane(tree);

        // split pane
        JLabel label;
        label = new JLabel("directory tree");

        JLabel label2;
        label2 = new JLabel("code");

        JLabel label3;
        label3 = new JLabel("chat, oh hai");

        JSplitPane splitPane;
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                scrollpane/* label */, tabbedPane);// textArea/*label2*/);
        JSplitPane splitPane2;
        splitPane2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, splitPane,
                label3);

        splitPane.setOneTouchExpandable(true);
        // splitPane.setDividerLocation(150);
        splitPane2.setOneTouchExpandable(true);
        // splitPane2.setDividerLocation(150);

        // Provide minimum sizes for the two components in the split pane
        Dimension minimumSize = new Dimension(100, 50);
        label.setMinimumSize(minimumSize);
        label2.setMinimumSize(minimumSize);

        JButton button = new JButton("Click Me");
        // button.addActionListener(new MyAction());

        w = new JFrame("CIDEr");
        w.setDefaultCloseOperation(w.EXIT_ON_CLOSE);

        URL x = this.getClass().getResource("icon.png");
        ImageIcon image3 = new ImageIcon(x);
        Image test3 = image3.getImage();
        w.setIconImage(test3);

        JPanel p = new JPanel(new BorderLayout());
        p.add(this.mainMenuBar(), BorderLayout.PAGE_START);
        p.add(splitPane2);

        // w.add(new JLabel("Hello World"));
        // w.add(menuBar);
        // w.add(splitPane2);
        // w.add(textArea);
        // w.add(button);

        w.add(p);

        w.pack();
        w.setLocationByPlatform(true);
        w.setVisible(true);
    }

    public void createNodes(DefaultMutableTreeNode top)
    {
        /* Creates some objects to simulate files and folders etc */
        DefaultMutableTreeNode category = null;
        DefaultMutableTreeNode book = null;

        category = new DefaultMutableTreeNode("Folder 1");
        top.add(category);

        // book = new DefaultMutableTreeNode(new BookInfo
        // ("The Java Tutorial: A Short Course on the Basics",
        // "tutorial.html"));
        for (int i = 0; i < 50; i++)
        {
            book = new DefaultMutableTreeNode("File 1");
            category.add(book);
        }
        /*
         * DefaultMutableTreeNode sub = null; sub = new
         * DefaultMutableTreeNode("SubFile 1"); book.add(sub);
         */

        category = new DefaultMutableTreeNode("Folder 2");
        top.add(category);

        book = new DefaultMutableTreeNode("File 2");
        category.add(book);
    }

    /*
     * public class MyAction implements ActionListener{ public void
     * actionPerformed(ActionEvent e){
     */
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
