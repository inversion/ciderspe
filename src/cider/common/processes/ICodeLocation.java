package cider.common.processes;

import java.util.Queue;

public interface ICodeLocation
{
    public void push(Queue<TypingEvent> typingEvents);

    public Queue<TypingEvent> events();

    public Queue<TypingEvent> eventsSince(long time);
}
