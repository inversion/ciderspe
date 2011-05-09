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
import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.TreeMap;
import java.util.Map.Entry;

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

    public static final TimeBorderComparer comparer = new TimeBorderComparer();
    private TreeMap<Long, TimeBorder> timeBorders = new TreeMap<Long, TimeBorder>();
    private TreeMap<Long, TimeRegion> timeRegions = new TreeMap<Long, TimeRegion>();
    private DocumentProperties documentProperties;
    private Long firstTime;

    private long endTime;

    public TimeBorderList(DocumentProperties documentProperties)
    {
        this.documentProperties = documentProperties;
    }

    public void addRegion(TimeRegion timeRegion)
    {
        timeRegions.put(timeRegion.getEndTime(), timeRegion);
    }

    public void addTimeBorder(TimeBorder timeBorder)
    {
        if (firstTime == null)
            firstTime = timeBorder.time;

        if (!timeBorder.documentProperties.equals(documentProperties))
            throw new Error("Time Border belongs to a different document");
        else
        {
            if (timeBorder.time > endTime)
                endTime = timeBorder.time;

            timeBorders.put(timeBorder.time, timeBorder);
        }
    }

    public LinkedList<Entry<Long, TimeBorder>> borderList()
    {
        return new LinkedList<Entry<Long, TimeBorder>>(timeBorders.entrySet());
    }

    /**
     * an array of border times
     * 
     * @return
     */
    public PriorityQueue<Long> borderTimes()
    {
        PriorityQueue<Long> borderTimes = new PriorityQueue<Long>();
        for (Long t : timeBorders.keySet())
            borderTimes.add(t);
        return borderTimes;
    }

    public void createRegions()
    {
        TimeBorder lastBorder = null;

        try
        {
            for (Entry<Long, TimeBorder> entry : timeBorders.entrySet())
            {
                timeRegions.put(entry.getValue().time, new TimeRegion(
                        lastBorder, entry.getValue()));
                lastBorder = entry.getValue();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public TimeBorder getBorder(long time)
    {
        return timeBorders.get(time);
    }

    public DocumentProperties getDocumentProperties()
    {
        return documentProperties;
    }

    public long getEndTime()
    {
        return endTime;
    }

    public long getFirstTime()
    {
        return firstTime;
    }

    public boolean hasNoBorders()
    {
        return timeBorders.isEmpty();
    }

    public void loadLocalBorderTimes(ArrayList<Long> borderTimes)
    {
        try
        {
            borderTimes.addAll(SiHistoryFiles
                    .getBorderTimes(documentProperties.path));

            if (borderTimes != null && borderTimes.size() > 1)
                for (long t : borderTimes)
                    this.addTimeBorder(new TimeBorder(documentProperties, t,
                            false));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void loadLocalEvents()
    {
        firstTime = timeBorders.firstKey();
        this.createRegions();
        SiHistoryFiles.getEvents(documentProperties.path, this);
    }

    public TimeRegion regionThatCovers(long t)
    {
        if (timeBorders.size() != 0)
        {
            Long last = timeBorders.lastKey();
            if (t > last)
                t = last;
        }

        Entry<Long, TimeRegion> entry = timeRegions.ceilingEntry(t);
        if (entry == null)
            return null;
        else
            return entry.getValue();
    }

    public void replaceEndBorder(TimeRegion region, TimeBorder newBorder)
    {
        timeRegions.remove(region.end.time);
        region.end = newBorder;
        timeRegions.put(region.end.time, region);
    }

    @Override
    public String toString()
    {
        String out = "";
        for (Entry<Long, TimeRegion> entry : timeRegions.entrySet())
        {
            out += entry.getValue().toString() + "\n";
        }
        return out;
    }
}
