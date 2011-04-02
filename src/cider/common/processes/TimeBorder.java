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

import java.util.PriorityQueue;

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
