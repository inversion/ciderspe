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

import java.io.Serializable;
import java.util.ArrayList;

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
    public static final int radix = 35;

    public String lockingGroup = null;
    public final TypingEventMode mode;
    public final long time;
    public final int position;
    public final int length;
    public final String text;
    public final String owner;

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
            mode = TypingEventMode.values()[Integer.parseInt(split[0])];
            text = split[1];
            position = Integer.parseInt(split[2], radix);
            length = Integer.parseInt(split[3]);
            time = Long.parseLong(split[4], radix);
            owner = split[5];
            if (split.length == 7)
                lockingGroup = split[6];
            else
                lockingGroup = null;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new Error("Failed to parse " + str + ". " + e.getMessage());
        }
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
        mode = typingEvent.mode;
        lockingGroup = typingEvent.lockingGroup;
        this.text = text;
        length = text.length();
        owner = typingEvent.owner;
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
        lockingGroup = typingEvent.lockingGroup;
        length = typingEvent.length;
        text = typingEvent.text;
        owner = typingEvent.owner;

    }

    /**
     * @param tes
     * @return true if this typing event exists in the array tes TODO: look into
     *         finding a more efficient alternative.
     */
    public boolean existsIn(TypingEvent[] tes)
    {
        for (TypingEvent te : tes)
            if (time == te.time)
                return true;
        return false;
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
        if (mode == TypingEventMode.overwrite && text.length() > 1)
        {
            /*
             * If doing an overwrite with more than 1 character to input, split
             * it up
             */
            char[] chrs = text.toCharArray();
            long t = time;
            int pos = position;
            int length = this.length;

            for (int charIndex = 0; charIndex < chrs.length; charIndex++)
            {
                if (charIndex == chrs.length - 1 && length > 1)
                {
                    /*
                     * If this is the last event to be added, tack the length of
                     * remaining deletions needed on the end, if there are any
                     */
                    particles.add(new TypingEvent(t, mode, pos, length, ""
                            + chrs[charIndex], owner, lockingGroup));
                }
                else
                {
                    if (length < 1) // If we've exhausted the length we want to
                        // overwrite, insert the remaining
                        // characters
                        particles
                                .add(new TypingEvent(t++,
                                        TypingEventMode.insert, (pos++) - 1, 1,
                                        "" + chrs[charIndex], owner,
                                        lockingGroup));
                    else
                        particles.add(new TypingEvent(this, t++, pos++, ""
                                + chrs[charIndex]));
                }
                length--;
            }
        }
        else if (mode == TypingEventMode.lockRegion
                || mode == TypingEventMode.unlockRegion
                || mode == TypingEventMode.delete
                || mode == TypingEventMode.overwrite)
            particles.add(this);
        else
        {
            char[] chrs = text.toCharArray();
            long t = time;
            int i = position;

            for (char chr : chrs)
                particles.add(new TypingEvent(this, t++, i++, "" + chr));
        }
        return particles;
    }

    /**
     * creates a string which represents this typing event and can be unpacked
     * by the contructor which takes a string as its artgument.
     * 
     * @return
     */
    public String pack()
    {
        return mode.ordinal() + "~" + text + "~"
                + Integer.toString(position, radix) + "~" + length + "~"
                + Long.toString(time, radix) + "~" + owner
                + (lockingGroup == null ? "" : "~" + lockingGroup);
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
        return time + "\t" + mode.toString() + "\t" + position + "\t" + length
                + "\t" + text + "\t" + lockingGroup;
    }
}
