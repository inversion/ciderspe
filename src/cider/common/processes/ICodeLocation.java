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
