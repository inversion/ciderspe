package cider.common.processes;

import java.util.PriorityQueue;

/**
 * A time border contains a priority queue of some of the typing events in the
 * region leading up to the border's time. If the queue of typing events has all
 * the events needed to represent the history of the document just before the
 * start of and during the region leading up to the berder's time then this
 * TimeBorder has a fullSet. TimeBorders are used to mark the beginning and end
 * of TimeRegions and are one of the objects used for browsing through document
 * history. Each TimeBorder is associated with a specific SourceDocument, the
 * name and path of which is stored in a DocumentID
 * 
 * @author Lawrence
 * 
 */
public class TimeBorder
{
    public final long time;
    public String path;
    public PriorityQueue<TypingEvent> typingEvents = new PriorityQueue<TypingEvent>();
    public boolean fullSet = false;
    public DocumentID documentID;

    public TimeBorder(DocumentID documentID, long time)
    {
        this.time = time;
        this.documentID = documentID;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (TimeBorder.class.equals(obj.getClass()))
        {
            TimeBorder timeBorder = (TimeBorder) obj;
            return this.time == timeBorder.time;
        }
        else if (Long.class.equals(obj.getClass()))
        {
            Long time = (Long) obj;
            return time.equals(this.time);
        }
        else
            return super.equals(obj);
    }
}
