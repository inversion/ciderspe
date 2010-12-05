package cider.client.gui;

import java.util.Hashtable;

import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import cider.common.network.Client;

/**
 * Listens for when a tree node is selected and if one is it gets a file from the server
 * and opens it in a new tab.
 * 
 * TODO: Type checking, error checking
 * TODO: Commenting to explain functionality
 * 
 * @author Andrew
 *
 */

public class DirectoryViewSelectionListener implements TreeSelectionListener {

	private JTree tree;
	private Hashtable nodePaths;
	private Client client;
	
	DirectoryViewSelectionListener( JTree tree, Hashtable nodePaths, Client client )
	{
		this.tree = tree;
		this.nodePaths = nodePaths;
		this.client = client;
	}
	
	@Override
	public void valueChanged(TreeSelectionEvent e) {
		// TODO Auto-generated method stub
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
		
		if( node == null )
			return;
		String path = (String) nodePaths.get( node );
		
		// Get the file and let the client message listener make the new tab
		// If the path is null it's a dir (quite sure)
		if( path != null)
			client.getFile(path);
	}

}
