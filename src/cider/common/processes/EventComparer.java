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

import java.io.Serializable;
import java.util.Comparator;

/**
 * Compares event priority based on their time of creation, used by the priority
 * queue
 * 
 * @author Lawrence
 * 
 */
public class EventComparer implements Comparator<TypingEvent>, Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public int compare(TypingEvent e1, TypingEvent e2)
    {
        if (e1.time > e2.time)
            return 1;
        else if (e1.time < e2.time)
            return -1;
        else
            return 0;
    }
}
