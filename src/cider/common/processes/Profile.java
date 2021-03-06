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
import java.text.DecimalFormat;
import java.util.Date;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

/**
 * Represents a user profile and contains associated methods for getting and
 * putting them to/from the Bot.
 * 
 * @author Jon, Andrew
 * 
 */

public class Profile implements Serializable
{
    private static final long serialVersionUID = -4978676304815568223L;

    private static boolean DEBUG = true;

    /**
     * Source: http://goo.gl/e4p3x
     * 
     * Time string from milliseconds time.
     * 
     * @param t
     *            The time in milliseconds to be converted
     * 
     * @return String of time in readable format
     */
    public static String getTimeString(long t)
    {
        String format = String.format("%%0%dd", 2);
        t /= 1000;
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
     * @param show
     *            Show the pop up dialog when this profile is returned or not.
     * @param botChat
     *            The chat session with the bot.
     * 
     * @author Jon, Andrew
     */
    public static void requestProfile(String username, boolean show,
            Chat botChat)
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
            if (DEBUG)
                System.out
                        .println("Profile: Requesting profile from server for "
                                + username);
        }
        catch (XMPPException e)
        {
            e.printStackTrace();
        }
    }

    private String uname;
    private int typedChars;
    private long timeSpent;
    private int idleTime;
    private String lastOnline;
    private Color userColour;

    private int userFontSize;

    private int loadedIdleStartTime;

    /**
     * Constructor for the Profile class. All values are set to the default.
     * 
     * @param un
     *            The client's username.
     * @author Jon
     */
    public Profile(String un)
    {
        loadedIdleStartTime = 0;
        uname = un;
        typedChars = 0;
        timeSpent = 0;
        idleTime = 0;
        lastOnline = "Never!";
        userColour = new Color(150, 150, 150);
        userFontSize = 14;
    }

    /**
     * Adjusts the number of characters typed by the amount specified in the
     * parameter
     * 
     * @param adjustment
     *            The number to be added to the total of characters typed
     * @author Jon
     */
    public void adjustCharCount(int adjustment)
    {
        typedChars += adjustment;
        return;
    }

    /**
     * 
     * @return The profile's colour
     */
    public Color getColour()
    {
        return userColour;
    }

    /**
     * 
     * @return The length of time in seconds that the user has been idle.
     */
    public int getIdleTime()
    {
        return idleTime;
    }

    /**
     * 
     * @return The String showing the date the user was last online
     */
    public String getLastOnline()
    {
        return lastOnline;
    }

    /**
     * 
     * @return The time in milliseconds that the user has spent in CIDEr.
     */
    public long getTimeSpent()
    {
        return timeSpent;
    }

    /**
     * 
     * @return The number of characters the user has typed in CIDEr.
     */
    public int getTypedChars()
    {
        return typedChars;
    }

    /**
     * 
     * @return The user's font size in pt.
     */
    public int getUserFontSize()
    {
        return userFontSize;
    }

    /**
     * 
     * @return The profile's username
     */
    public String getUsername()
    {
        return uname;
    }

    /**
     * Percentage of time spent idle.
     * 
     * @return String of rounded percentage.
     * 
     * @author Andrew
     */
    public String idlePercentString()
    {
        DecimalFormat df = new DecimalFormat("#.#");
        int spent = (int) (getTimeSpent() / 1000);
        Float percentage = new Float(((float) idleTime / (float) spent) * 100);
        return df.format(percentage);
    }

    /**
     * Increments the number of characters typed by 1
     * 
     * @author Jon
     */
    public void incrementCharCount()
    {
        typedChars++;
        return;
    }

    /**
     * Sets the Colour of the user's Profile.
     * 
     * @param R
     *            Red component of the colour.
     * @param G
     *            Green component of the colour.
     * @param B
     *            Blue component of the colour.
     */
    public void setColour(int R, int G, int B)
    {
        userColour = new Color(R, G, B);
        System.out.println("Colour updated to: " + R + " " + G + " " + B);
    }

    /**
     * Sets the user's font size.
     * 
     * @param s
     *            The font size in pt.
     */
    public void setFontSize(int s)
    {
        userFontSize = s;
        System.out.println("Font size updated to: " + s);
    }

    /**
     * Sets the amount of time that the user has been idle in the CIDEr client.
     * 
     * @param idleTime2
     *            The amount of time that the user has been idle in the CIDEr
     *            client.
     */
    public void setIdleTime(Integer idleTime2)
    {
        idleTime = idleTime2;
    }

    /**
     * Sets the date that the user was last seen in the CIDEr client.
     * 
     * @param lastOnline2
     *            The date that the user was last seen in the CIDEr client.
     */
    public void setLastOnline(String lastOnline2)
    {
        lastOnline = lastOnline2;
    }

    /**
     * Sets the time the user has spent in the CIDEr client.
     * 
     * @param timeSpent2
     *            The time the user has spent in the CIDEr client.
     */
    public void setTimeSpent(Long timeSpent2)
    {
        timeSpent = timeSpent2;
    }

    /**
     * Sets the number of character that the user has typed.
     * 
     * @param chars
     *            The number of characterts the user has typed.
     */
    public void setTypedChars(Integer chars)
    {
        typedChars = chars;
    }

    /**
     * Sets the user's username.
     * 
     * @param user
     *            The user's username.
     */
    public void setUsername(String user)
    {
        uname = user;
    }

    /**
     * @return The String which holds all of the information in the profile.
     *         Useful for client-bot communication.
     * 
     */
    @Override
    public String toString()
    {
        return uname + "  " + "chars: " + typedChars + "  timespent: "
                + getTimeSpent() + "  idletime: " + idleTime + "  lastonline: "
                + lastOnline + "  colour: " + userColour.getRed() + " "
                + userColour.getGreen() + " " + userColour.getBlue()
                + " fontSize: " + userFontSize;
    }

    /**
     * Update the amount of time spent idle
     * 
     * @param time
     *            The amount of time to be added.
     * 
     * @author Andrew
     */
    public void updateIdleTime(int time)
    {
        idleTime = time + loadedIdleStartTime;
    }

    /**
     * Update the date that the user was last online.
     */
    public void updateLastOnline()
    {
        Date d = new Date();
        DateFormat df = DateFormat.getDateInstance(DateFormat.FULL);
        lastOnline = df.format(d);
    }

    /**
     * Updates the time that the user has spent on CIDEr.
     * 
     * @param start
     *            The time in milliseconds that repesents the last time the
     *            profile was updated.
     */
    public void updateTimeSpent(Long start)
    {
        long end, spent;
        end = System.currentTimeMillis();
        spent = end - start;

        System.out.println("Profile: UPDATING TIME " + spent + getTimeSpent());
        setTimeSpent(getTimeSpent() + spent);
    }

    /**
     * Upload this profile to the bot to be created or updated.
     * 
     * @param botChat
     *            The chat session with the bot.
     * @param startTime
     *            The time in millis when the profile was last updated.
     * @param idleTime
     *            The amount of time spent idle in this session.
     * 
     * @author Andrew, Jon
     */
    public void uploadProfile(Chat botChat, long startTime, int idleTime)
    {
        updateTimeSpent(startTime);
        updateLastOnline();
        updateIdleTime(idleTime);
        System.out.println("Profile: Uploading profile: " + this.toString());
        try
        {
            Message message = new Message();
            message.setBody("");
            message.setProperty("ciderAction", "userprofile");
            message.setProperty("chars", typedChars);
            message.setProperty("timeSpent", getTimeSpent());
            message.setProperty("idleTime", this.idleTime);
            message.setProperty("lastOnline", lastOnline);
            message.setProperty("r", userColour.getRed());
            message.setProperty("g", userColour.getGreen());
            message.setProperty("b", userColour.getBlue());
            message.setProperty("fontSize", userFontSize);
            botChat.sendMessage(message);
        }
        catch (XMPPException e1)
        {
            e1.printStackTrace();
        }
    }

}
