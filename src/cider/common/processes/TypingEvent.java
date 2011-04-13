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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * TypingEvents are what make up the history of SourceDocuments. TypingEvents
 * can represent insertions, overwrites backspaces and locking or unlocking.
 * Each typing event happens at a certain time, and has an owner. They may also
 * belong to certain locking groups, which means that only users are a member of
 * that group are allowed to interfere with what happens at this position.
 * 
 * @author Lawrence
 * 
 */
public class TypingEvent implements Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = 2L;

    public String lockingGroup = null;
    public final TypingEventMode mode;
    public final long time;
    public final int position;
    public final int length;
    public final String text;
    public final String owner;
    public static final String folderpath = System.getenv("APPDATA")
            + "\\cider\\localhistory\\";

    /**
     * 
     * @param time
     * @param mode
     * @param position
     * @param length
     * @param text
     * @param owner
     * @param lockingGroup
     * 
     * @author Lawrence
     */
    public TypingEvent(long time, final TypingEventMode mode, int position,
            int length, String text, String owner, String lockingGroup)
    {
        this.time = time;
        this.mode = mode;
        this.position = position;
        this.text = text;
        this.length = length;
        this.owner = owner;
        this.lockingGroup = lockingGroup;
    }

    /**
     * This typing event will be the same except for the time, position and
     * mode.
     * 
     * @param typingEvent
     * @param time
     * @param position
     * @param mode
     * 
     * @author Lawrence
     */
    public TypingEvent(TypingEvent typingEvent, long time, int position,
            TypingEventMode mode)
    {
        this.time = time;
        this.position = position;
        this.mode = mode;
        this.lockingGroup = typingEvent.lockingGroup;
        this.length = typingEvent.length;
        this.text = typingEvent.text;
        this.owner = typingEvent.owner;

    }

    /**
     * This typing event will be the same except for the time and text.
     * 
     * @param typingEvent
     * @param time
     * @param text
     * 
     * @author Lawrence
     */
    public TypingEvent(TypingEvent typingEvent, long time, int position,
            String text)
    {
        this.time = time;
        this.position = position;
        this.mode = typingEvent.mode;
        this.lockingGroup = typingEvent.lockingGroup;
        this.text = text;
        this.length = text.length();
        this.owner = typingEvent.owner;
    }

    /**
     * This typing event is specified by a string that was created by the pack
     * method
     * 
     * @param str
     * @author Lawrence
     */
    public TypingEvent(String str)
    {
        String[] split = str.split("~");
        // it's possible the user will want
        // to use ~ so we need some way of
        // telling the difference.
        try
        {
            this.mode = TypingEventMode.values()[Integer.parseInt(split[0])];
            this.text = split[1];
            this.position = Integer.parseInt(split[2], 35);
            this.length = Integer.parseInt(split[3]);
            this.time = Long.parseLong(split[4], 35);
            this.owner = split[5];
            if (split.length == 7)
                this.lockingGroup = split[6];
            else
                this.lockingGroup = null;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new Error("Failed to parse " + str + ". " + e.getMessage());
        }
    }

    /**
     * creates a string which represents this typing event and can be unpacked
     * by the contructor which takes a string as its artgument.
     * 
     * @return
     */
    public String pack()
    {
        return this.mode.ordinal() + "~" + this.text + "~"
                + Integer.toString(this.position, 35) + "~" + this.length + "~"
                + Long.toString(this.time, 35) + "~" + this.owner
                + (this.lockingGroup == null ? "" : "~" + this.lockingGroup);
    }

    /**
     * if this typing event represents text of more than one character, the
     * explode method can be used to split it up into a list of typing events,
     * one for each character. Each typing event will have a different time, the
     * first event has the same time as this event. The time increments by one
     * long integer for every event in the list.
     * 
     * @return the array list of typing events
     * @author Lawrence
     */
    public ArrayList<TypingEvent> explode()
    {
        ArrayList<TypingEvent> particles = new ArrayList<TypingEvent>();
        if (this.mode == TypingEventMode.overwrite && this.text.length() > 1)
        {
            /*
             * If doing an overwrite with more than 1 character to input, split
             * it up
             */
            char[] chrs = this.text.toCharArray();
            long t = this.time;
            int pos = this.position;
            int length = this.length;

            for (int charIndex = 0; charIndex < chrs.length; charIndex++)
            {
                if (charIndex == chrs.length - 1 && length > 1)
                {
                    /*
                     * If this is the last event to be added, tack the length of
                     * remaining deletions needed on the end, if there are any
                     */
                    particles.add(new TypingEvent(t, this.mode, pos, length, ""
                            + chrs[charIndex], this.owner, this.lockingGroup));
                }
                else
                {
                    if (length < 1) // If we've exhausted the length we want to
                                    // overwrite, insert the remaining
                                    // characters
                        particles.add(new TypingEvent(t++,
                                TypingEventMode.insert, (pos++) - 1, 1, ""
                                        + chrs[charIndex], this.owner,
                                this.lockingGroup));
                    else
                        particles.add(new TypingEvent(this, t++, pos++, ""
                                + chrs[charIndex]));
                }
                length--;
            }
        }
        else if (this.mode == TypingEventMode.lockRegion
                || this.mode == TypingEventMode.unlockRegion
                || this.mode == TypingEventMode.delete
                || this.mode == TypingEventMode.overwrite)
            particles.add(this);
        else
        {
            char[] chrs = this.text.toCharArray();
            long t = this.time;
            int i = this.position;

            for (char chr : chrs)
                particles.add(new TypingEvent(this, t++, i++, "" + chr));
        }
        return particles;
    }

    /**
     * @param lockingGroup
     * @author Lawrence
     */
    public void setLockingGroup(String lockingGroup)
    {
        this.lockingGroup = lockingGroup;
    }

    @Override
    /**
     * Useful for debugging, but use pack() to compress the event into something that needs to be sent over the network.
     * @author Lawrence
     */
    public String toString()
    {
        return this.time + "\t" + this.mode.toString() + "\t" + this.position
                + "\t" + this.length + "\t" + this.text + "\t" + this.lockingGroup;
    }

    /**
     * @param tes
     * @return true if this typing event exists in the array tes TODO: look into
     *         finding a more efficient alternative.
     */
    public boolean existsIn(TypingEvent[] tes)
    {
        for (TypingEvent te : tes)
            if (this.time == te.time)
                return true;
        return false;
    }

    public static void main(String[] args)
    {
        String originalMessage = "The quick brown jox jumped over the lazy dog";
        TypingEvent te = new TypingEvent(0, TypingEventMode.insert, 0,
                originalMessage.length(), originalMessage, "owner", null);
        SourceDocument doc = new SourceDocument("test");
        doc.addEvents(te.explode());
        String resultingMessage = doc.toString();
        if (resultingMessage.equals(originalMessage))
            System.out.println("pass");
        else
            System.out.println("fail, should of been " + originalMessage
                    + " but got " + resultingMessage);

    }
    
    public static Set<String> times(Collection<TypingEvent> typingEvents)
    {
        Set<String> results = new LinkedHashSet<String>();
        for(TypingEvent te : typingEvents)
            results.add("" + te.time);
        return results;
    }

    public static Set<String> eventTimesExistsInFile(String documentPath, Set<String> times)
            throws IOException
    {
        FileInputStream fstream = new FileInputStream(folderpath + documentPath);
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String strLine;
        Set<String> results = new LinkedHashSet<String>();
        boolean foundMatch;
        int i;
        int start;
        while ((strLine = br.readLine()) != null)
        {
            foundMatch = false;
            i = 0;
            for(String time : times)
            {
                if (strLine.startsWith(time))
                {
                    start = time.length();
                    if(strLine.substring(start, strLine.indexOf(' ', start + 1))
                            .equals(TypingEventMode.homogenized.toString()))
                    {
                        foundMatch = true;
                        results.add(time);
                        break;
                    }
                }
                i++;
            }
            
            if(foundMatch)
                times.remove(i);
        }
        in.close();
        return results;
    }

    public static void saveEvents(Collection<TypingEvent> typingEvents,
            String documentPath)
    {
        try
        {
            File f = new File(folderpath + documentPath);
            System.out.println(f.getPath());
            new File(f.getParent()).mkdirs();
            f.createNewFile();
            
            Set<String> matches = eventTimesExistsInFile(documentPath, times(typingEvents));

            FileWriter fstream = new FileWriter(f, true);
            BufferedWriter out = new BufferedWriter(fstream);
            
            for (TypingEvent typingEvent : typingEvents)
                if(!matches.contains("" + typingEvent.time))
                    out.write(typingEvent.toString() + "\n");
            
            out.close();
        }
        catch (IOException e1)
        {
            e1.printStackTrace();
            JOptionPane.showMessageDialog(new JPanel(),
                    ("Error: " + e1.getMessage()));
            return;
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
            JOptionPane.showMessageDialog(new JPanel(),
                    "Error: There is no document open!");
            return;
        }
    }
}
