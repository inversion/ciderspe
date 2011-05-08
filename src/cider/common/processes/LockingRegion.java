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

@Deprecated
public class LockingRegion implements Comparable<LockingRegion>
{
    public String owner;
    public int start;
    public int end;

    @Deprecated
    public LockingRegion(int start)
    {
        this.start = start;
    }

    @Deprecated
    public LockingRegion(String owner, int start, int end)
    {
        this.owner = owner;
        this.start = start;
        this.end = end;
    }

    @Override
    @Deprecated
    public int compareTo(LockingRegion lr)
    {
        if (start < lr.start)
            return -1;
        else if (start > lr.start)
            return 1;
        else
            return 0;
    }

    @Deprecated
    public boolean coversOver(String self, int position)
    {
        return position >= start && position <= end;// &&
        // !this.owner.equals(self);
    }

    @Deprecated
    public void move(int amount)
    {
        start += amount;
        end += amount;
    }
}
