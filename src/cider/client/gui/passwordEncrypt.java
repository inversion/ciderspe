/*
 * Some magical password encryption, by adding 128 to the ASCII value of each char...
 * Alex
 */
package cider.client.gui;

public class passwordEncrypt
{
	public static String encrypt(String input)
	{
		System.out.println(input);
		
		String encryptedText = "";
		for (int i = 0; i < input.length(); i++)
		{
			char c = input.charAt(i);
			c = (char) (c + 128);
			encryptedText+= c;
		}
		System.out.println(encryptedText);
		return encryptedText;
	}
	
	public static String decrypt(String input)
	{
		String encryptedText = "";
		for (int i = 0; i < input.length(); i++)
		{
			char c = input.charAt(i);
			c = (char) (c - 128);
			encryptedText+= c;
		}
		System.out.println("Input : " + input);
		System.out.println("Output: " + encryptedText);
		return encryptedText;
	}
}