package cider.common.processes;

import java.util.TreeMap;

/**
 * 
 * @author Lawrence
 * 
 */
public class TypingEvent
{
    public final String text;
    public final long time;
    public final TypingEventMode mode;
    public final int position;

    public TypingEvent(long time, final TypingEventMode mode, int position,
            String text)
    {
        this.time = time;
        this.mode = mode;
        this.position = position;
        this.text = text;
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
            this.time = Long.parseLong(split[3]);
        }
        catch (Exception e)
        {
            throw new Error("Failed to parse " + str + ". " + e.getMessage());
        }
    }

    public String pack()
    {
        return this.mode + "~" + this.text + "~" + this.position + "~"
                + this.time;
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
                pos++;
                t = this.time + inc * pos;
                result.put(t,
                        new TypingEvent(this.time, this.mode, pos, "" + c));
            }
        }
        return result;
    }

    @Override
    public String toString()
    {
        return "time " + this.time + "\t" + this.position + "\t"
                + mode.toString() + "\t" + text;
    }
}
