package cider.client.gui;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import cider.common.processes.CiderFile;
import cider.common.processes.CiderFileList;

public class DirectoryViewComponent extends JPanel
{
	private DefaultMutableTreeNode top;
	
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

	public void constructTree( CiderFileList list )
	{
		// TODO: Dirs without children are displayed with the same icon as files
		// TODO: This is pretty incomprehensible, I need to add comments (Andrew)
		Object[] files = list.table.keySet().toArray();
		Arrays.sort( files );
		
		// Table of directories and their nodes, keyed by path
		Hashtable<String,DefaultMutableTreeNode> dirs = new Hashtable<String,DefaultMutableTreeNode>();
		DefaultMutableTreeNode cur = null;
		Pattern pattern = Pattern.compile( "(.+)\\\\(.+)$" );
		Matcher matcher = null;
		
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
				
				if( dirs.containsKey( matcher.group(1) ) )
					dirs.get( matcher.group(1) ).add( cur );
				else
					top.add( cur );
				dirs.put( (String)files[i], cur );
			}
			else
			{
				if( matcher != null )
					matcher = matcher.reset( (String)files[i] );
				else
					matcher = pattern.matcher( (String)files[i] );
				
				if( matcher.matches() )
					dirs.get( matcher.group(1) ).add( new DefaultMutableTreeNode( matcher.group(2) ) );
			}
				
		}
	}
}
