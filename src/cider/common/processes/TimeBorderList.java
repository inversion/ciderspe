package cider.common.processes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;

import cider.common.network.client.Client;

public class TimeBorderList
{
    public static TimeBorderComparer comparer = new TimeBorderComparer();
    private ArrayList<TimeBorder> timeBorders = new ArrayList<TimeBorder>();
    private Hashtable<Long, TimeBorder> timeBorderLookup = new Hashtable<Long, TimeBorder>();
    private Hashtable<Long, TimeRegion> timeRegionLookup = new Hashtable<Long, TimeRegion>();
    private String path;
    private Client client;

    public TimeBorderList(String path, Client client)
    {
        this.path = path;
        this.client = client;
    }

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
            region = new TimeRegion(previousBorder, endBorder);
        }

        return region;
    }

    static class TimeBorderComparer implements Comparator<TimeBorder>
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
