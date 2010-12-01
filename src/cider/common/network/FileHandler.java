package cider.common.network;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import cider.specialcomponents.Base64;

public class FileHandler {

	/**
	 * File handling functions on the server side.
	 * This should probably be in the "serverside" package?
	 * 
	 * TODO: Error handling
	 * escaping (just base64 everything?)
	 * 
	 * @author Andrew
	 */
	
	// Get recursive directory listing as XML
	public String getDirListXML( String path  )
	{
		// TODO: Add filter possiblility (*.java) or more with other langs
		// handle escaping gt and lt signs etc. (look up what needs to be escaped and make a method to do this)
		File f = new File( path );
		File[] list = f.listFiles();
		String xmlout = "";
		
		for( int i = 0; i < list.length; i++ )
		{
			if( list[i].isFile() )
				xmlout = xmlout + getFileXML(list[i]);
			else
			{
				xmlout = xmlout + "<directory name=\"" + list[i].getName() + "\">\n";
				xmlout = xmlout + getDirListXML( list[i].getAbsolutePath() );
				xmlout = xmlout + "</directory>\n";
			}
		}
		return xmlout;
	}
	
	// Gets information on a file as XML
	private String getFileXML( File f )
	{
		String xmlout = "<file>\n";
		xmlout = xmlout + "\t<name>" + f.getName() + "</name>\n";
		xmlout = xmlout + "\t<modified>" + f.lastModified() + "</modified>\n";
		xmlout = xmlout + "</file>\n";
		return xmlout;		
	}
	
	public String getFileContents( String s )
	{
		return getFileContents( new File(s) );
	}
	
	// Get contents of a file and return it Base64 encoded
	public String getFileContents( File f )
	{
		String file = "";
		// TODO: not sure if this is the best way to read in the file
		FileReader freader;
		try {
			freader = new FileReader( f );
			BufferedReader breader = new BufferedReader( freader );
			
			while( breader.ready() )
				file = file + breader.readLine();
			
			breader.close();
			freader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return Base64.encodeBytes(file.getBytes());
	}
	
	
}
