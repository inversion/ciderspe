package cider.client.gui;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;

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

    TreeModel model;
    DefaultMutableTreeNode top = new DefaultMutableTreeNode("Root");
    JTree tree = new JTree(this.top);

    public DirectoryViewComponent()
    {
        this.add(this.tree);
        this.model = this.tree.getModel();
    }

    public void parseXML(String xml)
    {
        // todo
    }
}
