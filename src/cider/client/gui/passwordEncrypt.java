/*
 * Some magical password encryption, by adding 128 to the ASCII value of each char...
 * Alex
 */
/**
 *  CIDER - Collaborative Integrated Development EnviRonment
    Copyright (C) 2011  Andrew Moss
                        Lawrence Watkiss
                        Jonathan Bannister
                        Alex Sheppard
                        Miles Taylor
                        Ashley Woodman

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
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