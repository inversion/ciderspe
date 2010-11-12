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

    /**
     * backspace
     * 
     * @param time
     */
    public TypingEvent(long time)
    {
        this.time = time;
        this.mode = TypingEventMode.backspace;
    }

    public TypingEvent(char chr, long time, TypingEventMode mode)
    {
        this.chr = chr;
        this.time = time;
        this.mode = mode;
    }

    @Override
    public String toString()
    {
        return "" + chr;
    }
}
