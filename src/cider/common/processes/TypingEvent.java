package cider.common.processes;

/**
 * 
 * @author Lawrence
 * 
 */
public class TypingEvent
{
    public char chr;
    public double time;
    public TypingEventMode mode;

    public TypingEvent(char chr, double time, TypingEventMode mode)
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
