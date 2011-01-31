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
import java.sql.Date;
import java.util.Calendar;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Profile 
{
	public String uname;
	public int typedChars;
	public String lastOnline;
	
	public static void main (String uname)
	{
		new Profile(uname);
	}

	public Profile(String un) 
	{
		uname = un;
		typedChars = 0;
		lastOnline = "never";
		readProfileFile();
	}
	
	public void readProfileFile()
	{
		File f = new File (uname + ".txt");
		if (f.exists())
		{
			System.out.println("Profile file exists, reading");
			try
			{
				FileInputStream fis = new FileInputStream(f);
				DataInputStream dis = new DataInputStream(fis);
				BufferedReader br = new BufferedReader(new InputStreamReader(dis));
				String line;
				while ((line = br.readLine()) != null)
				{
					if (line.contains("chars:"))
					{
						String[] splitline = line.split(" ");
						try
						{
							typedChars = Integer.parseInt(splitline[1]);
						}
						catch (Exception e)
						{
							System.err.println("Error: Integer parse failed in Profile.java");
						}
					}
					if (line.contains("lasttime:"))
					{
						String[] splitline = line.split(" ");
						try
						{
							lastOnline = splitline[3];
						}
						catch (Exception e)
						{
							System.err.println("Error: Last Online parse failed in Profile.java");
						}
					      
					}
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
			System.out.println("Profile file not found, constructing");
			try 
			{
				f.createNewFile();
				FileWriter fw = new FileWriter(f);
				BufferedWriter out = new BufferedWriter(fw);
				out.write(uname + "\n" + "chars: 0");
				out.close();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
	}
	
	public void incrementCharCount()
	{
		typedChars++;
		return;
	}

	public void updateProfileInfo() 
	{
		File f = new File (uname + ".txt");
		try 
		{
			if (!f.exists())
				System.out.println("Error: how the hell did that happen"); //TODO: should probably remove ;p
			else
			{
				lastOnline = Date.toGMTString();
				f.delete();
				f.createNewFile();
				FileWriter fw = new FileWriter(f);
				BufferedWriter out = new BufferedWriter(fw);
				out.write(uname + "\n" + "chars: " + typedChars + "\n" + "lastime: " + lastOnline);
				out.close();
			}
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
	}
}
