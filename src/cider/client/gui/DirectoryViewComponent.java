package cider.client.gui;

import java.awt.BorderLayout;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import cider.common.network.Client;
import cider.common.processes.CiderFileList;

public class DirectoryViewComponent extends JPanel
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private DefaultMutableTreeNode top;
	//TODO type checking?
	private Hashtable nodePaths;
	private Client client;
	private JTree tree;
	
	
/*    public static void main(String[] args)
    {
        JFrame w = new JFrame();
        w.add(new DirectoryViewComponent());
        w.setLocationByPlatform(true);
        w.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        w.setPreferredSize(new Dimension(400, 800));
        w.setVisible(true);
    }*/

    public DirectoryViewComponent()
    {
    	top = new DefaultMutableTreeNode(
                "root");
        //createNodes(top);

        tree = new JTree(top);
        // tree.setModel(model);

        JScrollPane scrollpane = new JScrollPane(tree);

        // split pane
        JLabel label;
        label = new JLabel("directory tree");

        JLabel label2;
        label2 = new JLabel("code");

        JLabel label3;
        label3 = new JLabel("chat, oh hai");

        this.setLayout(new BorderLayout());
        this.add(scrollpane, BorderLayout.CENTER);
    }
    
    public void setClient( Client client )
    {
    	this.client = client;
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

	public void constructTree( CiderFileList list )
	{
		// TODO: Dirs without children are displayed with the same icon as files
		// TODO: This is pretty incomprehensible, I need to add comments (Andrew)
		
		Object[] files = list.table.keySet().toArray();
		Arrays.sort( files );
		
		// Table of files/dirs and their nodes, keyed by path
		Hashtable<String,DefaultMutableTreeNode> nodes = new Hashtable<String,DefaultMutableTreeNode>();
		DefaultMutableTreeNode cur = null;
		Pattern pattern = Pattern.compile( "(.+)\\\\(.+)$" );
		Matcher matcher = null;
		
		// Purge current directory tree to prevent duplicates
		top.removeAllChildren();
		
		for( int i = 0; i < files.length; i++ )
		{
			if( list.table.get( files[i] ).isDir() )
			{	
				if( matcher != null )
					matcher = matcher.reset( (String)files[i] );
				else
					matcher = pattern.matcher( (String)files[i] );
				
				if( matcher.matches() )
					cur = new DefaultMutableTreeNode( matcher.group(2) );
				
				if( nodes.containsKey( matcher.group(1) ) )
					nodes.get( matcher.group(1) ).add( cur );
				else
					top.add( cur );
				nodes.put( (String)files[i], cur );
			}
			else
			{
				if( matcher != null )
					matcher = matcher.reset( (String)files[i] );
				else
					matcher = pattern.matcher( (String)files[i] );
				
				if( matcher.matches() )
				{
					cur = new DefaultMutableTreeNode( matcher.group(2) );
					nodes.get( matcher.group(1) ).add( cur );
					nodes.put( (String)files[i], cur );					
				}
			}		
		}
		// TODO: Can see a potential problem with this method if people click on nodes before this method finishes, change approach to make both hashes on the fly?
		Object[] keys = nodes.keySet().toArray();
		nodePaths = new Hashtable( keys.length );
		
		for( int i = 0; i < keys.length; i++ )
			nodePaths.put( nodes.get( keys[i] ), keys[i] );
		
        // Listen for changes in the selected node
		//TODO : Bit messy just removing current listeners atm
		TreeSelectionListener[] listeners = tree.getTreeSelectionListeners();
		for( int i = 0; i < listeners.length; i++ )
			tree.removeTreeSelectionListener(listeners[i]);
		
        tree.addTreeSelectionListener( new DirectoryViewSelectionListener( tree, nodePaths, client ) );
	}
	
/*	// Flip the keys and values of a hash and return a new one
	private Hashtable flipHash( Hashtable h )
	{
		Object[] keys = h.keySet().toArray();
		Hashtable newHash = new Hashtable( keys.length );
		
		for( int i = 0; i < keys.length; i++ )
			newHash.put( h.get( keys[i] ), keys[i] );
		
		return newHash;
	}*/
}
