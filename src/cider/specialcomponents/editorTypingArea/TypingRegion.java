package cider.specialcomponents.editorTypingArea;

import java.util.List;

import cider.common.processes.TypingEvent;

public class TypingRegion
{
    public final int start;
    public final int end;
    public final List<TypingEvent> list;

    public TypingRegion(int start, int end, final List<TypingEvent> list)
    {
        this.start = start;
        this.end = end;
        this.list = list;
    }

    public boolean inside(int position)
    {
        return position >= this.start && position <= this.end;
    }
}
