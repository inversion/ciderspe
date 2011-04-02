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
