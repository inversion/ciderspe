package cider.common.processes;

import java.awt.Color;
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
import java.text.DateFormat;
import java.util.Date;

public class Profile 
{
	public String uname;
	public static int typedChars;
	public long timeSpent;
	public String lastOnline;
	public Color userColour;
	
	public static void main (String uname)
	{
		new Profile(uname);
	}

	public Profile(String un) 
	{
		uname = un;
		typedChars = 0;
		timeSpent = 0;
		lastOnline = "Never!";
		userColour = new Color(150,150,150);
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
							timeSpent = Integer.parseInt(splitline[1]);
						}
						catch (Exception e)
						{
							System.err.println("Error: Integer parse failed in timespent Profile.java");
						}
					}
					if (line.contains("lastonline:"))
					{
						int index = line.indexOf(" ");
						line = line.substring(index);
						try
						{
							lastOnline = line;
						}
						catch (Exception e)
						{
							System.err.println("Error: Integer parse failed in lastonline Profile.java");
						}
					}

					if (line.contains("colour:"))
					{
						String[] splitline = line.split(" ");
						int r = Integer.parseInt(splitline[1]);
						int g = Integer.parseInt(splitline[2]);
						int b = Integer.parseInt(splitline[3]);
						userColour = new Color(r,g,b);
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
				out.write(uname + "\n" + "chars: 0" + "\n" + "timespent: 0" + "\n" + " lastonline: Never!\n" + "colour: 150 150 150");
				out.close();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
	}
	
	public static void incrementCharCount()
	{
		typedChars++;
		return;
	}
	
	public static void adjustCharCount(int adjustment)
	{
		typedChars+=adjustment;
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
	
	public void updateColour (int R, int G, int B)
	{
		userColour = new Color(R, G, B);
		System.out.println("Colour updated to: " + R + " " + G + " " + B);
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
				Date d = new Date();
				DateFormat df = DateFormat.getDateInstance(DateFormat.FULL);
				lastOnline = df.format(d);
				FileWriter fw = new FileWriter(f);
				BufferedWriter out = new BufferedWriter(fw);
				out.write(uname + "\n" + "chars: " + typedChars + 
						"\ntimespent: " + timeSpent + 
						"\nlastonline: " + lastOnline + 
						"\ncolour: " + 
						userColour.getRed() + " " + 
						userColour.getGreen() + " " + 
						userColour.getBlue());
				out.close();
			}
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
	}
	
	public String toString()
	{
		return uname + "  " + "chars: " + typedChars + 
		"  timespent: " + timeSpent + 
		"  lastonline: " + lastOnline + 
		"  colour: " + 
		userColour.getRed() + " " + 
		userColour.getGreen() + " " + 
		userColour.getBlue();
	}
}
