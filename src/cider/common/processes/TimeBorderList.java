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
import java.util.NavigableMap;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import cider.common.network.client.Client;

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
    private DocumentID documentID;
    private Long firstTime;
    private long endTime;

    public TimeBorderList(DocumentID documentID)
    {
        this.documentID = documentID;
    }
    
    public void addTimeBorder(TimeBorder timeBorder)
    {
        if(this.firstTime == null)
            this.firstTime = timeBorder.time;
        
        if(!timeBorder.documentID.equals(this.documentID))
            throw new Error("Time Border belongs to a different document");
        else
        {
            if(timeBorder.time > this.endTime)
                this.endTime = timeBorder.time;
            
            this.timeBorders.put(timeBorder.time, timeBorder);
        }
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
        if(this.timeBorders.size() != 0)
        {
            Long last = this.timeBorders.lastKey();
            if(t > last)
                t = last;
        }
        
        Entry<Long, TimeRegion> entry = this.timeRegions.ceilingEntry(t);
        if (entry == null)
            return null;
        else
            return entry.getValue();
    }

    public void useTimeBordersFrom(String currentFileName, Client client)
    {
        // TODO Auto-generated method stub

    }
    
    public LinkedList<Entry<Long,TimeBorder>> borderList()
    {
        return new LinkedList<Entry<Long, TimeBorder>>(this.timeBorders.entrySet());
    }
    
    public DocumentID getDocumentID()
    {
        return this.documentID;
    }

    public void addRegion(TimeRegion timeRegion)
    {
        this.timeRegions.put(timeRegion.getEndTime(), timeRegion);
    }
    
    @Override
    public String toString()
    {
        String out = "";
        for(Entry<Long, TimeRegion> entry : this.timeRegions.entrySet())
        {
            out += entry.getValue().toString() + "\n";
        }
        return out;
    }

    public void replaceEndBorder(TimeRegion region, TimeBorder newBorder)
    {
        this.timeRegions.remove(region.end.time);
        region.end = newBorder;
        this.timeRegions.put(region.end.time, region);     
    }

    public void loadLocalHistory()
    {
        try
        {
            ArrayList<Long> borderTimes = SiHistoryFiles.getBorderTimes(this.documentID.path);
            boolean fullSet = false;
            for(long t : borderTimes)
            {
                this.addTimeBorder(new TimeBorder(this.documentID, t, fullSet));
                fullSet = !fullSet;
            }
            
            this.firstTime = borderTimes.get(0) - 1;
            this.createRegions();
            SiHistoryFiles.getEvents(this.documentID.path, this);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public long getFirstTime()
    {
        return this.firstTime;
    }

    public long getEndTime()
    {
        return this.endTime;
    }
}
