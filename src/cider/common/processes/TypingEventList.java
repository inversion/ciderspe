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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Random;

import cider.documentViewerComponents.TypingRegion;

/**
 * Think of this as being like a string of characters, except they're actually
 * typing events. This object is generally used by the document viewers. It will
 * often contain only the events that are to be painted (such as inserts and
 * overwrites).
 * 
 * @author Lawrence
 * 
 */
public class TypingEventList
{
    private ArrayList<TypingEvent> tel = new ArrayList<TypingEvent>();

    public TypingEventList()
    {

    }

    /**
     * Adds a typing event to the end of the list if its position is greater
     * than the list size, otherwise it is inserted to the list at position + 1.
     * 
     * @param te
     * @author Lawrence
     */
    public void insert(TypingEvent te)
    {
        if (te.position >= tel.size())
            this.tel.add(te);
        else
            this.tel.add(te.position + 1, te);
    }

    /**
     * Adds a typing event to the end of the list if its position is greater
     * than the list size, otherwise it overwrites the event at that position.
     * 
     * @param te
     */
    public void overwrite(TypingEvent te)
    {
        if (te.position >= tel.size())
            this.tel.add(te);
        else
            this.tel.set(te.position, te);

        
        // If overwriting length > 1 delete the rest of the stuff to be overwritten
        if( te.length > 1 )
            delete( te.position + 1, te.length - 1 );
    }

    /**
     * Removes the nearest typing event at position i.
     * 
     * @param position
     * @author Lawrence and Miles
     */
    public void backspace(int position)
    {

        if (position >= tel.size())
            position = tel.size() - 1;
        if (position < 0)
            return;
//            position = 0;
        
        this.tel.remove(position);
    }
    
    /**
     * Delete behaviour, reusing backspace method
     * 
     * @param position The position to delete at.
     * @param length The length to delete for, usually 1.
     * 
     * @author Andrew
     */
    public void delete( int position, int length )
    {
        if( position == tel.size() - 1 )
            return;
        
        if( position == -1 )
            position = 0;
        
        // Delete for length
        for( int i = 1; i <= length; i++ )
            backspace( position );          
    }

    /**
     * Checks whether an event exists at position i.
     * 
     * @param i
     * @return
     */
    public boolean exists(int i)
    {
        return i > 0 && i < this.tel.size();
    }

    /**
     * Gets a typing event at position i.
     * 
     * @param i
     * @author Lawrence
     * @return
     */
    public TypingEvent get(int i)
    {
        return this.tel.get(i);
    }

    /**
     * Removes all the typing events in this list.
     * 
     * @author Lawrence
     */
    public void clear()
    {
        this.tel.clear();
    }

    public static void main(String[] args)
    {
        test();
    }

    /**
     * Auto-test routine: Tests the insert, overwrite and backspace methods.
     */
    public static void test()
    {
        Random rand = new Random();
        TypingEventList tel = new TypingEventList();

        for (int test = 0; test < 100; test++)
        {
            TypingEvent te = new TypingEvent(0, TypingEventMode.deleteAll, 0,
                    1, "x", "test user", null);
            switch (rand.nextInt(3))
            {
            case 0:
                tel.insert(te);
                break;
            case 1:
                tel.overwrite(te);
                break;
            case 2:
                tel.backspace(rand.nextInt(200));
                break;
            }
        }

        System.out.println("reached end");
    }

    @Override
    public String toString()
    {
        String string = "";
        for (TypingEvent te : this.tel)
            string += te.text;
        return string;
    }

    /**
     * Splits this TypingEventList into smaller TypingEventLists
     * 
     * @param string
     *            the text on which to make the splits
     * @return
     */
    public LinkedList<TypingEventList> split(String string)
    {
        LinkedList<TypingEventList> ll = new LinkedList<TypingEventList>();
        TypingEventList current = new TypingEventList();
        for (TypingEvent te : this.tel)
            if (te.text.equals(string))
            {
                ll.add(current);
                current = new TypingEventList();
            }
            else
                current.tel.add(te);
        ll.add(current);
        return ll;
    }

    /**
     * Convenience method to find out if a string is a member of another set of
     * strings.
     * 
     * @param string
     * @param strings
     * @author Lawrence
     * @return
     */
    static boolean belongsTo(String string, String[] strings)
    {
        for (String str : strings)
            if (str.equals(string))
                return true;
        return false;
    }

    /**
     * Splits the typingEventList into smaller TypingEventLists using a set of
     * strings to identify which events to split on.
     * 
     * @param dividers
     * @return
     */
    public LinkedList<TypingEventList> splitWords(String[] dividers)
    {
        LinkedList<TypingEventList> ll = new LinkedList<TypingEventList>();
        TypingEventList current;
        current = new TypingEventList();
        for (TypingEvent te : this.tel)
            if (belongsTo(te.text, dividers))
            {
                ll.add(current);
                current = new TypingEventList();
            }
            else
                current.tel.add(te);
        ll.add(current);
        return ll;
    }

    /**
     * Finds out to what degree this position is locked.
     * 
     * @param position
     * @param user
     * @return 0 if this position is not locked, 1 if it's locked by this user
     *         and 2 if it's locked by somebody else.
     * 
     * @author Lawrence
     */
    public int locked(int position, String user)
    {
        TypingEvent te = this.tel.get(position);
        return te.lockingGroup == null ? 0 : (te.lockingGroup.equals(user) ? 1
                : 2);
    }

    /**
     * The sise of the typing event list.
     * 
     * @return
     * @author Lawrence
     */
    public int length()
    {
        return this.tel.size();
    }

    /**
     * @return true if this TypingEventList is empty or contains nothing but a
     *         newline.
     * 
     * @author Lawrence
     */
    public boolean newline()
    {
        return this.tel.size() == 0 || this.tel.size() == 1
                && this.tel.get(0).equals("\n");
    }

    /**
     * 
     * @return all of the typing events
     * 
     * @author Lawrence
     */
    public Collection<? extends TypingEvent> events()
    {
        return this.tel;
    }

    /**
     * Each typing event will have a time that is only 1 greater than the
     * previous time. The greatest time in the list will be end.
     * 
     * @param end
     *            the last time
     * @author Lawrence
     */
    public void homogenize(long end)
    {
        int size = this.tel.size();
        long start = end - size;
        long t = start;
        for (int i = 0; i < size; i++)
            this.tel.set(i, new TypingEvent(this.tel.get(i), t++, i,
                    TypingEventMode.insert));
    }

    /**
     * @param te
     * @return the last index of te
     */
    public int getLastPositionOf(TypingEvent te)
    {
        return this.tel.lastIndexOf(te);
    }

    /**
     * Adds up all the characters belonging to this user
     * 
     * @param user
     * @return
     * @author Lawrence
     */
    public int countCharactersFor(String user)
    {
        int count = 0;
        for (TypingEvent te : this.tel)
        {
            if (te.owner.equals(user) && !te.text.equals("\n"))
            {
                count++;
            }
        }
        return count;
    }

    /**
     * Adds up all the characters for all the users who have characters in this
     * TypingEventList
     * 
     * @return a hashtable containing the number of characters with user names
     *         for keys
     * @author Lawrence
     */
    public Hashtable<String, Integer> countCharactersAll()
    {
        Hashtable<String, Integer> results = new Hashtable<String, Integer>();
        for (TypingEvent te : this.tel)
        {
            if (!te.text.equals("\n"))
            {
                if (results.containsKey(te.owner))
                {
                    results.put(te.owner, results.get(te.owner) + 1);
                }
                else
                {
                    results.put(te.owner, 0);
                }
            }
        }
        return results;
    }

    /**
     * 
     * @param start
     * @param end
     * @return a TypingRegion starting from start and ending at end
     * 
     * @author Lawrence
     */
    public TypingRegion region(int start, int end)
    {
        TypingRegion region = new TypingRegion(start, end, this.tel.subList(
                start, end));
        return region;
    }
}
