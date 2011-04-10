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

package cider.common.processes;

import java.awt.Color;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

/**
 * Represents a user profile and contains associated methods for getting and putting them
 * to/from the Bot.
 * 
 * @author Jon, Andrew
 *
 */

public class Profile implements Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = 8774619560240198237L;
    
    public String uname;
    public int typedChars;
    public long timeSpent;
    public String lastOnline;
    public Color userColour;
//    public Client client;

    // public static void main (String uname)
    // {
    // new Profile(uname);
    // }

    public Profile(String un)
    {
        uname = un;
        typedChars = 0;
        timeSpent = 0;
        lastOnline = "Never!";
        userColour = new Color(150, 150, 150);
//        client = c;
        // readProfileFileFromServer();
    }

    public void incrementCharCount()
    {
        typedChars++;
        return;
    }

    public void adjustCharCount(int adjustment)
    {
        typedChars += adjustment;
        return;
    }

    public void updateTimeSpent(Long start)
    {
        long end, spent;
        end = System.currentTimeMillis();
        spent = end - start;

        System.out.println("Profile: UPDATING TIME " + spent + timeSpent);
        timeSpent += spent;
    }

    public void updateProfileInfo()
    {
        Date d = new Date();
        DateFormat df = DateFormat.getDateInstance(DateFormat.FULL);
        lastOnline = df.format(d);
    }

    public String toString()
    {
        return uname + "  " + "chars: " + typedChars + "  timespent: "
                + timeSpent + "  lastonline: " + lastOnline + "  colour: "
                + userColour.getRed() + " " + userColour.getGreen() + " "
                + userColour.getBlue();
    }

    public void setColour(int R, int G, int B)
    {
        userColour = new Color(R, G, B);
        System.out.println("Colour updated to: " + R + " " + G + " " + B);
    }

    /**
     * Upload this profile to the bot to be created or updated.
     * 
     * @param botChat The chat session with the bot.
     * @param startTime The start time of this session, used to update the time spent in the profile.
     * 
     * @author Andrew, Jon
     */
    public void uploadProfile( Chat botChat, long startTime )
    {
        Profile myProfile = this;
        myProfile.updateTimeSpent(startTime);
        myProfile.updateProfileInfo();
        System.out.println( "Profile: Uploading profile: " + myProfile.toString());
        try
        {
            Profile profile = myProfile;
            Message message = new Message();
            message.setBody("");
            message.setSubject( "userprofile" );
            message.setProperty( "chars", profile.typedChars );
            message.setProperty( "timeSpent", profile.timeSpent );
            message.setProperty( "lastOnline", profile.lastOnline );
            message.setProperty( "r", profile.userColour.getRed() );
            message.setProperty( "g", profile.userColour.getGreen() );
            message.setProperty( "b", profile.userColour.getBlue() );
            botChat.sendMessage( message );
        }
        catch (XMPPException e1)
        {
            e1.printStackTrace();
        }
    }

    /*
     * Source: http://goo.gl/e4p3x
     */
    public String getTimeString()
    {
        long t = timeSpent;
        String format = String.format("%%0%dd", 2);
        t = timeSpent / 1000;
        String seconds = String.format(format, t % 60);
        String minutes = String.format(format, (t % 3600) / 60);
        String hours = String.format(format, t / 3600);
        String time = hours + ":" + minutes + ":" + seconds;
        return time;
    }
}
