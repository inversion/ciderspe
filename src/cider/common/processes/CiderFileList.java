package cider.common.processes;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Hashtable;

/**
 * Holds a list of all the source files available to CIDER
 * as CiderFile objects.
 * 
 * The objects are stored in a hashtable to allow quick access
 * mapping paths (acting as unique IDs) to CiderFile objects.
 * 
 * ALPHA: For now just serializing the whole list rather than using XML
 * less efficient but will improve in future releases to be more efficient.
 * 
 * TODO: Handling synchronization for example if a file is created at the client end
 * while CIDER is offline especially and needs to be synched to the server when the
 * client program is next loaded.
 * TODO: Hash table shouldn't really be public, add interface methods to it 
 * 
 * @author Andrew
 *
 */

public class CiderFileList implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final String root;
	public final Hashtable<String,CiderFile> table;
	
	public CiderFileList( String path )
	{
		table = new Hashtable<String,CiderFile>();
		root = path;
		constructTable( root );
	}

	private void constructTable( String path )
	{
		// TODO: Make this throw the exceptions instead
		File f = new File( path );
		File[] list = f.listFiles();
		
		for( int i = 0; i < list.length; i++ )
		{
			if( list[i].isFile() )
				try {
					table.put( list[i].getPath(), new CiderFile( list[i].getPath() ) );
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			else
			{
				try {
					table.put( list[i].getPath(), new CiderFile( list[i].getPath() ) );
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				constructTable( list[i].getPath() );
			}
		}
	}	
}
