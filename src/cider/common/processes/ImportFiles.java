package cider.common.processes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class ImportFiles
{
    private String owner;
    HashMap<String, String> files = new HashMap<String,String>();

    public ImportFiles( String owner )
    {
        this.owner = owner;
    }
    
    public static void main( String[] args )
    {
        ImportFiles program = new ImportFiles( "andrew" );
        try
        {
            program.importFrom( "src" );
        }
        catch (FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Iterator<Entry<String,String>> itr = program.getFiles().entrySet().iterator();
        
        while( itr.hasNext() )
        {
            System.out.println( "Source Document - " + itr.next().getKey() );
            break;
        }
    }
    
    /**
     * Read file from file system
     * 
     * @throws FileNotFoundException, IOException 
     * @return String of file contents
     */
    private String readFile( File file ) throws FileNotFoundException, IOException
    {
        FileReader fr = new FileReader( file );
        BufferedReader br = new BufferedReader( fr );
        StringBuffer str = new StringBuffer();
        String line = null;

        while( (line = br.readLine()) != null )
            str.append(line + '\n');
        
        br.close();
        return str.toString();
    }
    
    /**
     * Import source files from the client's file system.
     * 
     * @param path If path is a file, a single file will be returned in the list, if it's a directory it will be recursed.
     * 
     * @author Andrew
     * @throws FileNotFoundException 
     */
    public void importFrom( String path ) throws FileNotFoundException, IOException
    {
        File root = new File( path );
        
        if( !root.canRead() )
            throw new FileNotFoundException("Can't read file/dir at path " + path);
        
        if( root.isFile() )
        {
            files.put( path, readFile( root ) );
            return;
        }
        
        File[] list = root.listFiles();
        for (File file : list)
        {
            if( file.isFile() && file.getName().endsWith(".java"))
                files.put( file.getPath(), readFile( file ) );
            else if( file.isDirectory() )
                importFrom( file.getPath() );
        }
        
        return;
    }
    
    /**
     * 
     * @return A mapping of paths -> contents which can be uploaded in turn with createDocument()
     */
    public HashMap<String,String> getFiles()
    {
        return files;
    }
}
