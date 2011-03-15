package cider.common.processes;

import java.util.Queue;

public interface ICodeLocation
{
    /**
     * Takes typing events out of the queue and puts them in this code location
     * 
     * @author Lawrence
     * @param typingEvents
     */
    public void push(Queue<TypingEvent> typingEvents);

    /**
     * Returns all of the events at this code location
     * 
     * @author Lawrence
     * @return
     */
    public Queue<TypingEvent> events();

    /**
     * Returns all events since time specified
     * 
     * @author Lawrence
     * @param time
     * @return
     */
    public Queue<TypingEvent> eventsSince(long time);

    /**
     * Returns the last time this code location was updated
     * 
     * @author Lawrence
     * @return
     */
    public long lastUpdateTime();

    /**
     * Clears all events from this location
     * 
     * @author Lawrence
     */
    public void clearAll();
}
