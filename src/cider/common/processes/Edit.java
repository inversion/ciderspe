package cider.common.processes;

/**
 * Class representing an edit of a source file.
 * 
 * There are two types of edit: insert & replace
 * 
 * Inserts work intuitively, replaces also act intuitively
 * if the length parameter is the same as that of the patch provided.
 * 
 * If the length parameter is longer than the patch length then all the
 * characters after the patch length will be deleted.
 * 
 * TODO: Check if the length parameter is longer than the length of the file to be edited.
 * TODO: Think if there are any other cases which would break the applyEdit method.
 * TODO: Change applyEdit method to take no parameters and act on the file at the path specified.
 * TODO: Testing for the object when file input is implemented
 * TODO: Test serialization
 * TODO: length parameter is irrelevant when inserting, compensate for this? overloading?
 * 
 * TODO: Enhancements:
 * The class is designed to work with the whole source file being changed at once
 * as one big string variable. When we actually implement the client/multi-user editing etc.
 * I imagine it will be better to represent files as perhaps an array of strings with each line
 * being an array entry. When this situation arises the class will of course need to be changed.
 * 
 * @author Andrew
 *
 */

public class Edit implements java.io.Serializable {
	
	/**
	 * Eclipse wanted me to add this, I've looked it up and
	 * it's not terribly relevant to us. It's intended as a form of 
	 * version control. So if we were to update the class whilst we
	 * had serialized versions of it on the file system somewhere
	 * it would break due to the update.
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String path; // Full path of the file to perform the edit on
	private int offset; // Byte offset from start of file to work at
	private int length; // Length of the plaintext patch
	private boolean replace; // true = replace, false = insert
	private String patch; // Patch to insert or replace with
	
	public static void main( String[] args ) throws Exception
	{
		// Testing section
		Edit test1 = new Edit( null, 4, 3, false, "and" );
		if( !test1.applyEdit("bbbbbbb").equals("bbbbandbbb") )
			System.err.println("Test 1, insert, failed");
		
		Edit test2 = new Edit( null, 4, 3, true, "and" );
		if( !test2.applyEdit("bbbbbbbz").equals("bbbbandz") )
			System.err.println("Test 2, simple replace, failed");
		
		Edit test3 = new Edit( null, 5, 3, true, "ie" );
		if( !test3.applyEdit("The lazy brown").equals("The lie brown") )
			System.err.println("Test 3, replace and delete, failed");
		
		Edit test4 = new Edit( null, 0, 4, true, null );
		if( !test4.applyEdit("hello").equals("lo") )
			System.err.println("Test 4, delete only, failed");
	}
	
	Edit( String path, int offset, int length, boolean replace, String patch ) throws Exception
	{
		if( patch == null )
			this.patch = new String( "1" );
		else
			this.patch = patch;
		
		if( length < 1 )
			throw new Exception("Length of edit is less than 1.");
		else if( length < this.patch.length() )
			throw new Exception("Length parameter less than length of data.");
		
		this.path = path;
		this.offset = offset;
		this.length = length;
		this.replace = replace;
	}
	
	public String applyEdit( String input )
	{		
		if( replace == true )
		{
			if( length == patch.length() )
			{
				// If performing a simple replace
				input = input.substring( 0, offset ) +  patch + input.substring( offset+length, input.length() );
			}	
			else
			{
				// If we need to delete a range of the string
				input = input.substring( 0, offset ) + patch + input.substring( offset+length, input.length() );
			}
		}
		else
		{
			// If we are performing an insert
			input = input.substring( 0, offset ) + patch + input.substring( offset, input.length() );
		}
		return input;
	}
}
