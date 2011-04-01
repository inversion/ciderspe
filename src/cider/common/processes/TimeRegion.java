package cider.common.processes;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import cider.common.network.client.Client;

public class TimeRegion
{
    public TimeBorder start;
    public TimeBorder end;
    public DocumentID documentID;
    private ArrayList<ActionListener> actionListeners = new ArrayList<ActionListener>();

    public TimeRegion(TimeBorder start, TimeBorder end) throws Exception
    {
        if (this.start.documentID != this.end.documentID)
            throw new Exception("time borders must belong to the same document");

        this.start = start;
        this.end = end;
        this.documentID = start.documentID;
    }

    public void updateWhereRequired(Client client)
    {
        if (!this.end.fullSet)
        {
            client.setDiversion(this);

            if (this.start != null)
            {
                if (this.start.fullSet)
                {
                    SourceDocument startDoc = new SourceDocument(
                            this.documentID.name, this.start.typingEvents);
                    startDoc.simplify(this.end.time);
                    this.end.typingEvents.addAll(startDoc.events());
                }
                else
                {
                    client.pullSimplifiedEventsFromBot(this.documentID.path,
                            this.start.time);
                }
            }
            client.pullEventsFromBot(this.documentID.path, this.start.time,
                    this.end.time, true);
        }
    }

    public long getTimespan()
    {
        return this.end.time - this.start.time;
    }

    public void finishedUpdate()
    {
        for (ActionListener al : this.actionListeners)
            al.actionPerformed(new ActionEvent(this, 0, "finished update"));
    }

    public void addActionListener(ActionListener actionListener)
    {
        this.actionListeners.add(actionListener);
    }
}
