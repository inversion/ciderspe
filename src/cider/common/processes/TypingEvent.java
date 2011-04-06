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
    private static final long serialVersionUID = 1L;
    
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
    public TypingEvent(TypingEvent typingEvent, long time, String text)
    {
        this.time = time;
        this.position = typingEvent.position;
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
     * by the contructor which takes a string as its argument.
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
        if (this.mode == TypingEventMode.lockRegion
                || this.mode == TypingEventMode.unlockRegion)
            particles.add(this);
        else
        {
            char[] chrs = this.text.toCharArray();
            long t = this.time;

            for (char chr : chrs)
                particles.add(new TypingEvent(this, t++, "" + chr));
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
        return "time " + this.time + "\t" + this.position + "\t" + this.length
                + "\t" + this.mode.toString() + "\t" + this.text + "\t"
                + this.lockingGroup;
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
}
