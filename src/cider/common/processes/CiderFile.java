package cider.common.processes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Class to represent a source file in CIDER
 * 
 * When a file is created by a client a serialized version
 * of it can be sent to the server to be propagated to other clients.
 * 
 * TODO: Add handling of opened by list
 * TODO: Think how to handle deletion of a file
 * 
 * @author Andrew
 *
 */

public class CiderFile implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String path;
	private boolean isDir;
	private long modified;
	private boolean open;
	
	// TODO: Eventually this arraylist will be of type 'User'
	@SuppressWarnings({ "rawtypes", "unused" })
	private ArrayList opened_by;
	
	public CiderFile( String path ) throws IOException
	{
		File f = new File( path );
		
		if( !f.exists() )
			throw new IOException("File " + path + " doesn't exist.");
		
		this.path = path;
		this.modified = f.lastModified();
		this.open = false;
		if( f.isDirectory() )
			this.isDir = true;
		else
			this.isDir = false;
	}
	
	public String getFileContents()
	{
		// TODO: not sure if this is the best way to read in the file
		// TODO: Throw error when trying to read a directory
		// TODO: Send files in binary instead (handling images etc.)
		
		File f = new File( path );
		String contents = "";
		FileReader freader;
		try {
			freader = new FileReader( f );
			BufferedReader breader = new BufferedReader( freader );
			
			while( breader.ready() )
				contents = contents + breader.readLine();
			
			breader.close();
			freader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return contents;
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
	
	public String getPath()
	{
		return path;
	}
	
}
