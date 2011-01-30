package cider.common.processes;

import java.util.LinkedList;
import java.util.Queue;

public class LocalisedTypingEvents
{
    public String path;
    public Queue<TypingEvent> typingEvents = new LinkedList<TypingEvent>();

    public LocalisedTypingEvents(String path)
    {
        this.path = path;
    }
}
