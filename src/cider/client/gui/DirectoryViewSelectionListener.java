package cider.client.gui;

import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import cider.common.network.client.Client;

/**
 * Listens for when a tree node is selected and if one is it gets a file from
 * the server and opens it in a new tab.
 * 
 * TODO: Type checking, error checking TODO: Commenting to explain functionality
 * 
 * @author Andrew
 * 
 */

public class DirectoryViewSelectionListener implements TreeSelectionListener
{

    private JTree tree;
    private Client client;

    DirectoryViewSelectionListener(JTree tree, Client client)
    {
        this.tree = tree;
        this.client = client;
    }

    @Override
    public void valueChanged(TreeSelectionEvent e)
    {
        // TODO Auto-generated method stub
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree
                .getLastSelectedPathComponent();

        // Get the file and let the client message listener make the new tab
        // If the path is null it's a dir (quite sure)
        if (node != null && node.isLeaf())
        {
            client.openTabFor(node.getUserObjectPath());
            // client.directKeyboardInputTo(node.getUserObjectPath());
        }
        // client.getFile(path);
    }
}
