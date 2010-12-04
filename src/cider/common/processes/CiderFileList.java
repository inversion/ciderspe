package cider.common.processes;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;

import cider.specialcomponents.Base64;

/**
 * Holds a list of all the source files available to CIDER
 * as CiderFile objects.
 * 
 * The objects are stored in a hashtable to allow quick access
 * mapping paths (acting as unique IDs) to CiderFile objects.
 * 
 * TODO: Handling synchronization for example if a file is created at the client end
 * while CIDER is offline especially and needs to be synched to the server when the
 * client program is next loaded.
 * 
 * TODO: Read file list from XML string and construct a cider file list
 * TODO: Create JTree from CiderFileList
 * 
 * @author Andrew
 *
 */

public class CiderFileList {

	private final String root;
	private final Hashtable<String,CiderFile> table;
	
	CiderFileList( String path )
	{
		table = new Hashtable<String,CiderFile>();
		root = path;
		constructTable( root );
	}
	
	public static void main( String[] args )
	{
		CiderFileList test = new CiderFileList( "src" );
		try {
			System.out.println(test.getDirListXML());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// Get directory list as XML
	public String getDirListXML() throws IOException
	{
		// TODO: Add filter possiblility (*.java) or more with other langs
		// TODO: Byte array vs buffered?
		// TODO: Compression, built into xmpp?
		int depth = 0;
		Object[] s = table.keySet().toArray();
		Arrays.sort( s ); 
		ByteArrayOutputStream xmlout = new ByteArrayOutputStream();
		
		for( int i = 0; i < s.length; i++ )
		{
			if( table.get( s[i] ).isDir() == true )
			{
				if( depth > 0 )
				{
					xmlout.write( "</dir>\n".getBytes() );
					depth--;
				}
				
				xmlout.write( "<dir obj=\"".getBytes() );
				xmlout.write( Base64.encodeObject( table.get( s[i] ) ).getBytes() );
				xmlout.write( "\">\n".getBytes() );
				depth++;
			}
			else
			{				
				xmlout.write( "<file obj=\"".getBytes() );
				xmlout.write( Base64.encodeObject( table.get( s[i] ) ).getBytes() );
				xmlout.write( "\">\n".getBytes() );
			}
		}
		xmlout.write( "</dir>".getBytes() );
		return xmlout.toString();
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
