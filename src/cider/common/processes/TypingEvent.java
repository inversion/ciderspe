package cider.common.processes;

import java.util.ArrayList;

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

    public TypingEvent(TypingEvent typingEvent, long time, String text)
    {
        this.time = time;
        this.position = typingEvent.position;
        this.mode = typingEvent.mode;
        this.locked = typingEvent.locked;
        this.text = text;
        this.length = text.length();
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
            this.mode = TypingEventMode.values()[Integer.parseInt(split[0])];
            this.text = split[1];
            this.position = Integer.parseInt(split[2], 35);
            this.length = Integer.parseInt(split[3]);
            this.time = Long.parseLong(split[4], 35);
            this.owner = split[5];
            if (split.length == 7)
                this.locked = true;
        }
        catch (Exception e)
        {
            throw new Error("Failed to parse " + str + ". " + e.getMessage());
        }
    }

    public String pack()
    {
        return this.mode.ordinal() + "~" + this.text + "~"
                + Integer.toString(this.position, 35) + "~" + this.length + "~"
                + Long.toString(this.time, 35) + "~" + this.owner
                + (this.locked ? "~1" : "");
    }

    public ArrayList<TypingEvent> explode()
    {
        ArrayList<TypingEvent> particles = new ArrayList<TypingEvent>();
        if (this.mode == TypingEventMode.lockRegion
                || this.mode == TypingEventMode.unlockRegion)
            particles.add(this);
        else
        {
            char[] chrs = this.text.toCharArray();
            long t = this.time;

            for (char chr : chrs)
                particles.add(new TypingEvent(this, t++, "" + chr));
        }
        return particles;
    }

    public void setLocked(boolean locked)
    {
        this.locked = locked;
    }

    @Override
    public String toString()
    {
        return "time " + this.time + "\t" + this.position + "\t" + this.length
                + "\t" + this.mode.toString() + "\t" + this.text + "\t"
                + this.locked;
    }

    public boolean existsIn(TypingEvent[] tes)
    {
        for (TypingEvent te : tes)
            if (this.time == te.time)
                return true;
        return false;
    }
}
