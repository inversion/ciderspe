/*
 * Some magical password encryption, by adding 128 to the ASCII value of each char...
 * Alex
 */
package cider.client.gui;

public class passwordEncrypt
{
	public static String encrypt(String input)
	{		
		String output = "";
		for (int i = 0; i < input.length(); i++)
		{
			char c = input.charAt(i);
			c = (char) (c + 128);
			output+= c;
		}
		/*System.out.println("Input : " + input);
		System.out.println("Output: " + output);*/
		return output;
	}
	
	public static String decrypt(String input)
	{
		String output = "";
		for (int i = 0; i < input.length(); i++)
		{
			char c = input.charAt(i);
			c = (char) (c - 128);
			output+= c;
		}
		/*System.out.println("Input : " + input);
		System.out.println("Output: " + output);*/
		return output;
	}
}