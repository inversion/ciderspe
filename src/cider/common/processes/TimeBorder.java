package cider.common.processes;

import java.util.PriorityQueue;

public class TimeBorder
{
    public final long time;
    public String message;
    public PriorityQueue<TypingEvent> typingEvents = new PriorityQueue<TypingEvent>();

    public TimeBorder(long time, String message)
    {
        this.time = time;
        this.message = message;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (TimeBorder.class.equals(obj.getClass()))
        {
            TimeBorder timeBorder = (TimeBorder) obj;
            return this.time == timeBorder.time;
        }
        else if (Long.class.equals(obj.getClass()))
        {
            Long time = (Long) obj;
            return time.equals(this.time);
        }
        else
            return super.equals(obj);
    }

}
