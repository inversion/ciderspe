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

package cider.specialcomponents.editorTypingArea;

import java.util.List;

import cider.common.processes.TypingEvent;

public class TypingRegion
{
    public final int start;
    public final int end;
    public final List<TypingEvent> list;

    public TypingRegion(int start, int end, final List<TypingEvent> list)
    {
        this.start = start;
        this.end = end;
        this.list = list;
    }

    public boolean inside(int position)
    {
        return position >= this.start && position <= this.end;
    }

    public int getLength()
    {
        return this.end - start;
    }
}
