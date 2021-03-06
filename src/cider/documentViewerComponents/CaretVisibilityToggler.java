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

package cider.documentViewerComponents;

import java.util.TimerTask;

/**
 * Toggles the caret visibility whenever it is run
 * 
 * @author Lawrence
 * 
 */
public class CaretVisibilityToggler extends TimerTask
{
    private EditorTypingArea eta;
    private boolean skipNextToggle = false;

    public CaretVisibilityToggler(EditorTypingArea eta)
    {
        this.eta = eta;
    }

    @Override
    public void run()
    {
        if (skipNextToggle)
            skipNextToggle = false;
        else
            eta.toggleCaretVisibility();
    }

    /**
     * The next run will have no effect. This can be useful is you want to keep
     * the caret visible while you type
     */
    public void skipNextToggle()
    {
        skipNextToggle = true;
    }

}
