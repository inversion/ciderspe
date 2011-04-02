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
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;

public class TimeBorderList
{
    public static final TimeBorderComparer comparer = new TimeBorderComparer();
    private ArrayList<TimeBorder> timeBorders = new ArrayList<TimeBorder>();
    private Hashtable<Long, TimeBorder> timeBorderLookup = new Hashtable<Long, TimeBorder>();
    private Hashtable<Long, TimeRegion> timeRegionLookup = new Hashtable<Long, TimeRegion>();

    public void addTimeBorder(TimeBorder timeBorder)
    {
        this.timeBorders.add(timeBorder);
        this.timeBorderLookup.put(timeBorder.time, timeBorder);
    }

    public void sort()
    {
        Collections.sort(this.timeBorders, comparer);
    }

    public TimeBorder getBorder(long time)
    {
        return this.timeBorderLookup.get(time);
    }

    public TimeRegion regionLeadingUpTo(long time)
    {
        TimeRegion region = this.timeRegionLookup.get(time);

        if (region == null)
        {
            TimeBorder endBorder = this.getBorder(time);
            int i = this.timeBorders.indexOf(endBorder);
            TimeBorder previousBorder = null;
            if (i != -1)
                previousBorder = this.timeBorders.get(i - 1);
            try
            {
                region = new TimeRegion(previousBorder, endBorder);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        return region;
    }

    public long[] borderTimes()
    {
        int size = this.timeBorders.size();
        long[] result = new long[size];
        for (int i = 0; i < size; i++)
            result[i] = this.timeBorders.get(i).time;
        return result;
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
}
