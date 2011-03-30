package cider.common.processes;

import cider.common.network.client.Client;

public class TimeRegion
{
    public TimeBorder start;
    public TimeBorder end;

    public TimeRegion(TimeBorder start, TimeBorder end)
    {
        this.start = start;
        this.end = end;
    }

    public boolean couldInclude(long time)
    {
        return time >= this.start.time && time < this.end.time;
    }

    public void updateWhereRequired(Client client)
    {
        if (!this.end.fullSet)
        {
            if (this.start != null)
            {
                if (this.start.fullSet)
                {
                    SourceDocument startDoc = new SourceDocument(
                            this.start.documentName, this.start.typingEvents);
                    startDoc.simplify(this.end.time);
                    this.end.typingEvents.addAll(startDoc.events());
                }
                else
                {
                    // get the simplified typing events from the bot
                }
            }

            // get all the events that happened in this period of time
        }
    }
}
