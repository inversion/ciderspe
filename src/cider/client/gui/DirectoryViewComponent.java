package cider.client.gui;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

public class DirectoryViewComponent extends JPanel
{
    public static void main(String[] args)
    {
        JFrame w = new JFrame();
        w.add(new DirectoryViewComponent());
        w.setLocationByPlatform(true);
        w.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        w.setPreferredSize(new Dimension(400, 800));
        w.setVisible(true);
    }

    public DirectoryViewComponent()
    {
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

        this.add(scrollpane);
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

    public void parseXML(String xml)
    {
        // todo
    }
}
