package cider.common.processes;

import java.util.TreeMap;

/**
 * 
 * @author Lawrence
 * 
 */
public class TypingEvent
{
    public boolean locked = false;
    public final TypingEventMode mode;
    public final long time;
    public final int position;
    public final int length;
    public final String text;
    public final String owner;

    public TypingEvent(long time, final TypingEventMode mode, int position,
            int length, String text, String owner)
    {
        this.time = time;
        this.mode = mode;
        this.position = position;
        this.text = text;
        this.length = length;
        this.owner = owner;
    }

    public TypingEvent(TypingEvent typingEvent, long time, int position,
            TypingEventMode mode)
    {
        this.time = time;
        this.position = position;
        this.mode = mode;
        this.locked = typingEvent.locked;
        this.length = typingEvent.length;
        this.text = typingEvent.text;
        this.owner = typingEvent.owner;

    }

    public TypingEvent(String str)
    {
        String[] split = str.split("~");
        // it's possible the user will want
        // to use ~ so we need some way of
        // telling the difference.
        try
        {
            this.mode = TypingEventMode.valueOf(split[0]);
            this.text = split[1];
            this.position = Integer.parseInt(split[2]);
            this.length = Integer.parseInt(split[3]);
            this.time = Long.parseLong(split[4]);
            this.owner = split[5];
        }
        catch (Exception e)
        {
            throw new Error("Failed to parse " + str + ". " + e.getMessage());
        }
    }

    public String pack()
    {
        return this.mode + "~" + this.text + "~" + this.position + "~"
                + this.length + "~" + this.time + "~" + this.owner;
    }

    public TreeMap<Double, TypingEvent> explode()
    {
        int len = this.text.length();
        return this.explode(len > 0 ? 1 : len);
    }

    public TreeMap<Double, TypingEvent> explode(final double amountOfTime)
    {
        TreeMap<Double, TypingEvent> result = new TreeMap<Double, TypingEvent>();
        int len = this.text.length();
        if (len == 0)
            result.put((double) this.time, this);
        else
        {
            final double inc = amountOfTime / this.text.length();
            int pos = this.position;
            double t;
            for (char c : this.text.toCharArray())
            {
                t = this.time + inc * pos;
                result.put(t, new TypingEvent(this.time, this.mode, pos,
                        length, "" + c, owner));
                pos++;
            }
        }
        return result;
    }

    public void setLocked(boolean locked)
    {
        this.locked = locked;
    }

    @Override
    public String toString()
    {
        return "time " + this.time + "\t" + this.position + "\t"
                + mode.toString() + "\t" + text;
    }

    public boolean existsIn(TypingEvent[] tes)
    {
        for (TypingEvent te : tes)
            if (this.time == te.time)
                return true;
        return false;
    }
}
