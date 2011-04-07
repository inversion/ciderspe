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

import java.util.Comparator;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.TreeMap;

/**
 * The TimeBorderList stores an array of time borders which can be sorted with
 * the sort method. You can retrieve a list of border times and create/retrieve
 * TimeBorders with the regionLeadingUpTo method.
 * 
 * @author Lawrence
 * 
 */
public class TimeBorderList
{
    public static final TimeBorderComparer comparer = new TimeBorderComparer();
    private TreeMap<Long, TimeBorder> timeBorders = new TreeMap<Long, TimeBorder>();
    private TreeMap<Long, TimeRegion> timeRegions = new TreeMap<Long, TimeRegion>();

    public void addTimeBorder(TimeBorder timeBorder)
    {
        this.timeBorders.put(timeBorder.time, timeBorder);
    }

    public TimeBorder getBorder(long time)
    {
        return this.timeBorders.get(time);
    }

    public void createRegions()
    {
        TimeBorder lastBorder = null;

        try
        {
            for (Entry<Long, TimeBorder> entry : this.timeBorders.entrySet())
            {
                this.timeRegions.put(entry.getValue().time, new TimeRegion(
                        lastBorder, entry.getValue()));
                lastBorder = entry.getValue();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * an array of border times
     * 
     * @return
     */
    public PriorityQueue<Long> borderTimes()
    {
        PriorityQueue<Long> borderTimes = new PriorityQueue<Long>();
        for (Long t : this.timeBorders.keySet())
            borderTimes.add(t);
        return borderTimes;
    }

    public static class TimeBorderComparer implements Comparator<TimeBorder>
    {
        public int compare(TimeBorder border1, TimeBorder border2)
        {
            if (border1.time > border2.time)
                return 1;
            else if (border1.time < border2.time)
                return -1;
            else
                return 0;
        }
    }

    public TimeRegion regionThatCovers(long t)
    {
        Entry<Long, TimeRegion> entry = this.timeRegions.ceilingEntry(t);
        if (entry == null)
            return null;
        else
            return entry.getValue();
    }
}
