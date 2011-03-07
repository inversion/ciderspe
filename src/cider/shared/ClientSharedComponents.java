package cider.shared;

import java.util.Hashtable;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTabbedPane;

import cider.client.gui.DirectoryViewComponent;
import cider.client.gui.SourceEditor;

/**
 * (Mostly) GUI Components that the Client needs to share with MainWindow.
 * 
 * @author Andrew
 *
 */

public class ClientSharedComponents {
	
	// The list and count of users online
	public DefaultListModel userListModel = new DefaultListModel();
	public JList userList = new JList( userListModel );
	public JLabel userCount = new JLabel();
	
	// Tabs for source editors and table to keep track of them
	public JTabbedPane tabbedPane = new JTabbedPane();
	public Hashtable<String, SourceEditor> openTabs = new Hashtable<String, SourceEditor>();
	
	// The directory tree
	public DirectoryViewComponent dirView = new DirectoryViewComponent();
	
	// Tabs for chat sessions
	public JTabbedPane receiveTabs = new JTabbedPane();

}
