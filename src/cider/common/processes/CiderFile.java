package cider.common.processes;

import java.io.File;
import java.util.ArrayList;

/**
 * Preliminary class for a file in CIDER. Serialized versions
 * of these will be sent through the XMPP server in dir listings.
 * 
 * TODO: Add handling of case when file doesn't exist
 * 
 * @author Andrew
 *
 */

public class CiderFile {

	private String path;
	private long modified;
	// TODO: Eventually this arraylist will be of type 'User'
	private ArrayList opened_by;
	private boolean open;
	
/*	Define whether this file is a directory 
	and give a list of its children
	
	TODO: Is this the best way to store children? I think it would probably be better to use some external data structure.
*/
	private boolean isDir;
	public ArrayList<CiderFile> children;
	
	public CiderFile( File f )
	{
		File[] list = f.listFiles();
		modified = f.lastModified();
		open = false;
		if( f.isDirectory() )
		{
			isDir = true;
			children = new ArrayList<CiderFile>();
		}
		else
			isDir = false;
	}
	
	public CiderFile( String path )
	{
		this( new File( path ) );
	}
	
	public boolean isOpen()
	{
		return open;
	}
	
	public boolean isDir()
	{
		return isDir;
	}
	
	public long getModified()
	{
		return modified;
	}
	
	
	
}
