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
    public int caretPosition; // only set if caret position changed

    public TypingEvent(long time, TypingEventMode mode, int caretPosition,
            char chr)
    {
        this.time = time;
        this.mode = mode;
        this.caretPosition = caretPosition;
        this.chr = chr;
    }

    @Override
    public String toString()
    {
        return this.time + "\t" + this.caretPosition + "\t" + mode.toString()
                + " " + chr;
    }
}
