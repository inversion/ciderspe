package cider.common.processes;

/**
 * 
 * @author Lawrence
 * 
 */
public class TypingEvent
{
    public char chr;
    public long time;
    public TypingEventMode mode;
    public int position;

    public TypingEvent(long time, TypingEventMode mode, int position, char chr)
    {
        this.time = time;
        this.mode = mode;
        this.position = position;
        this.chr = chr;
    }

    @Override
    public String toString()
    {
        return this.time + "\t" + this.position + "\t" + mode.toString() + " "
                + chr;
    }
}
