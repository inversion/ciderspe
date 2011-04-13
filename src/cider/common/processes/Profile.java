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
    private static final long serialVersionUID = -5594341971483531519L;
    
    private static boolean DEBUG = true;
    
    public String uname;
    public int typedChars;
    public long timeSpent;
    public int idleTime;
    public String lastOnline;
    public Color userColour;

    

    public Profile(String un)
    {
        uname = un;
        typedChars = 0;
        timeSpent = 0;
        lastOnline = "Never!";
        userColour = new Color(150, 150, 150);
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
    
    /**
     * Update the amount of time spent idle
     * 
     * @param time The amount of time to be added.
     * 
     * @author Andrew
     */
    public void updateIdleTime( int time )
    {
        idleTime += time;
    }

    public void updateLastOnline()
    {
        Date d = new Date();
        DateFormat df = DateFormat.getDateInstance(DateFormat.FULL);
        lastOnline = df.format(d);
    }

    public String toString()
    {
        return uname + "  " + "chars: " + typedChars + "  timespent: "
                + timeSpent + "  idletime: " + idleTime + "  lastonline: " + lastOnline + "  colour: "
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
     * @param idleTime The amount of time spent idle in this session.
     * 
     * @author Andrew, Jon
     */
    public void uploadProfile( Chat botChat, long startTime, int idleTime )
    {        
        updateTimeSpent(startTime);
        updateLastOnline();
        updateIdleTime( idleTime );
        System.out.println( "Profile: Uploading profile: " + this.toString());
        try
        {
            Message message = new Message();
            message.setBody("");
            message.setProperty( "ciderAction", "userprofile" );
            message.setProperty( "chars", typedChars );
            message.setProperty( "timeSpent", timeSpent );
            message.setProperty( "idleTime", this.idleTime );
            message.setProperty( "lastOnline", lastOnline );
            message.setProperty( "r", userColour.getRed() );
            message.setProperty( "g", userColour.getGreen() );
            message.setProperty( "b", userColour.getBlue() );
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
    
    /**
     * Request profile for specified user from the Bot.
     * 
     * @param username
     *            The username of the profile we are requesting.
     * @param show Show the pop up dialog when this profile is returned or not.
     * @param botChat The chat session with the bot.
     * 
     * @author Jon, Andrew
     */
    public static void requestProfile(String username, boolean show, Chat botChat)
    {
        try
        {
            Message msg = new Message();
            msg.setBody("");
            msg.setProperty("ciderAction", "requestprofile");
            msg.setProperty("username", username);
            if (show)
                msg.setProperty("show", "true");
            botChat.sendMessage(msg);
            if (DEBUG )
                System.out.println("Profile: Requesting profile from server for " + username);
        }
        catch (XMPPException e)
        {
            e.printStackTrace();
        }
    }
}
