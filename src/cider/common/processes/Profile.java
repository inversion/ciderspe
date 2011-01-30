package cider.common.processes;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

public class Profile 
{
	public String uname;
	public int typedChars;
	
	public static void main (String uname)
	{
		new Profile(uname);
	}

	public Profile(String un) 
	{
		uname = un;
	}
	
	public void readProfileFile()
	{
		File f = new File (uname + ".txt");
		if (f.exists())
		{
			try
			{
				FileInputStream fis = new FileInputStream(f);
				DataInputStream dis = new DataInputStream(fis);
				BufferedReader br = new BufferedReader(new InputStreamReader(dis));
				String line;
				while ((line = br.readLine()) != null)
				{
					System.out.println(line);
				}
			}
			catch (FileNotFoundException ex)
			{
				System.err.println("Error: file not found while creating profile");
			}
			catch (IOException ex)
			{
				System.err.println("Error: " + ex.getMessage());
			}
		}
		else
		{
			try 
			{
				f.createNewFile();
				FileWriter fw = new FileWriter(f);
				BufferedWriter out = new BufferedWriter(fw);
				out.write(uname + "\n" + "chars : 0");
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
	}
}
