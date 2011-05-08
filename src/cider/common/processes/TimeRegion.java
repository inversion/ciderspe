/**
 *  CIDER - Collaborative Integrated Development EnviRonment
    Copyright (C) 2011  Andrew Moss
                        Lawrence Watkiss
                        Jonathan Bannister
                        Alex Sheppard
                        Miles Taylor
                        Ashley Woodman

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package cider.common.processes;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import cider.common.network.client.Client;

/**
 * TimeRegions have two TimeBorders, the start and end. TimeBorders are used to
 * mark the beginning and end of TimeRegions. Each TimeRegion is associated with
 * a specific SourceDocument, the name and path of which is stored in a
 * DocumentID. TimeRegions can be used to retrieve typing events between a
 * certain area of time and use them for browsing a document's history.
 * 
 * @author Lawrence
 */
public class TimeRegion
{
    public TimeBorder start;
    public TimeBorder end;
    public DocumentProperties documentProperties;
    public static final int FINISHED_UPDATE = 0;
    private ArrayList<ActionListener> actionListeners = new ArrayList<ActionListener>();

    public TimeRegion(TimeBorder start, TimeBorder end) throws Exception
    {
        if (this.start != null
                && this.start.documentProperties != this.end.documentProperties)
            throw new Exception("time borders must belong to the same document");

        this.start = start;
        this.end = end;
        documentProperties = end.documentProperties;
    }

    public void addActionListener(ActionListener actionListener)
    {
        actionListeners.add(actionListener);
    }

    /**
     * This method is called by the Client to indicate that no more typing
     * events will be received. It triggers the action listeners.
     * 
     * @author Lawrence
     */
    public void finishedUpdate()
    {
        end.fullSet = true;

        for (ActionListener al : actionListeners)
            al.actionPerformed(new ActionEvent(this, 0, "finished update"));
    }

    public long getEndTime()
    {
        return end.time;
    }

    public long getStartTime()
    {
        return start == null ? 0 : start.time;
    }

    /**
     * The difference between the start and end time
     * 
     * @return
     */
    public long getTimespan()
    {
        return end.time - start.time;
    }

    @Override
    public String toString()
    {
        return "" + end.time;
    }

    /**
     * Loads the required typing events from the client. The Client may use the
     * TimeRegion to work out which typing events it receives need to be
     * diverted.
     * 
     * @param client
     */
    public void updateWhereRequired(Client client)
    {
        if (!end.fullSet)
        {
            client.setDiversion(this);

            if (start != null)
            {
                if (start.fullSet)
                {
                    SourceDocument startDoc = new SourceDocument(
                            documentProperties.name, documentProperties.path,
                            start.typingEvents);
                    startDoc.simplify(end.time);
                    end.typingEvents.addAll(startDoc.events());
                }
                else
                {
                    client.pullEventsFromBot(documentProperties.path,
                            start.time, true);
                }
            }
            client.pullEventsFromBot(documentProperties.path, this
                    .getStartTime(), this.getEndTime(), true);
        }
    }
}
