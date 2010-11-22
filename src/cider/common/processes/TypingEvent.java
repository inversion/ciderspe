package src.cider.common.processes;

/**
 * 
 * @author Lawrence
 * 
 */
public class TypingEvent
{
    public final char chr;
    public final long time;
    public final TypingEventMode mode;
    public final int position;

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
