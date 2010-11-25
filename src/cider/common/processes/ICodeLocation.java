package cider.common.processes;

import java.util.Queue;

public interface ICodeLocation
{
    public void setOpen(boolean open);

    public boolean isOpen();

    public void push(Queue<TypingEvent> typingEvents);

    public Queue<TypingEvent> update();
}
