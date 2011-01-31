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
	public long timeSpent;
	
	public static void main (String uname)
	{
		new Profile(uname);
	}

	public Profile(String un) 
	{
		uname = un;
		typedChars = 0;
		timeSpent = 0;
		readProfileFile();
	}
	
	public void readProfileFile()
	{
		File f = new File (uname + ".txt");
		if (f.exists())
		{
			System.out.println("Profile file exists, reading " + uname + ".txt");
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
					if (line.contains("timespent:"))
					{
						String[] splitline = line.split(" ");
						try
						{
							System.out.println(timeSpent);
							timeSpent = Integer.parseInt(splitline[1]);
							System.out.println(timeSpent);
						}
						catch (Exception e)
						{
							System.err.println("Error: Integer parse failed in Profile.java");
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
				out.write(uname + "\n" + "chars: 0" + "\n" + "timespent: 0");
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

	public void updateTimeSpent(Long start)
	{
		long end, spent;
		end = System.currentTimeMillis();
		spent = end-start;

		System.out.println("UPDATING TIME " + spent + timeSpent);
		timeSpent += spent;
	}
	
	public void updateProfileInfo() 
	{
		File f = new File (uname + ".txt");
		try 
		{
			if (!f.exists())
				System.out.println("Error: User Profile file was not generated");
			else
			{
				f.delete();
				f.createNewFile();
				FileWriter fw = new FileWriter(f);
				BufferedWriter out = new BufferedWriter(fw);
				out.write(uname + "\n" + "chars: " + typedChars + "\ntimespent: " + timeSpent);
				out.close();
			}
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
	}
}
