package cider.common.processes;

import java.util.Comparator;
import java.util.PriorityQueue;

public class TimeBorderList
{
    public final PriorityQueue<TimeBorder> timeBorders;

    public TimeBorderList(int initialCapacity)
    {
        this.timeBorders = new PriorityQueue<TimeBorder>(initialCapacity,
                new TimeBorderComparer());
    }

    class TimeBorderComparer implements Comparator<TimeBorder>
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
