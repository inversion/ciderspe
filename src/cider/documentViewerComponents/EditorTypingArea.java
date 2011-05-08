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

import cider.common.processes.SourceDocument;

/**
 * The EditorTypingArea is a type of SourceDocumentViewer which has the caret
 * enabled and makes caret related methods publicly available
 * 
 * @author Lawrence
 * 
 */
public class EditorTypingArea extends SourceDocumentViewer
{

    /**
     * 
     */
    private static final long serialVersionUID = 7119052848681284989L;

    public EditorTypingArea(SourceDocument doc)
    {
        super(doc);
        this.setupCaretFlashing();
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
    }

    @Override
    public void copy()
    {
        super.copy();
    }

    @Override
    public int currentPositionLocked(int offset, String user)
    {
        return super.currentPositionLocked(offset, user);
    }

    @Override
    public int getCaretPosition()
    {
        return super.getCaretPosition();
    }

    @Override
    public SDVLine getCurrentLine()
    {
        return super.getCurrentLine();
    }

    @Override
    public int getCurrentLineNumber()
    {
        return super.getCurrentLineNumber();
    }

    @Override
    public TypingRegion getSelectedRegion()
    {
        return super.getSelectedRegion();
    }

    @Override
    public boolean isEmpty()
    {
        return super.isEmpty();
    }

    @Override
    public void moveCaret(int spaces)
    {
        super.moveCaret(spaces);
    }

    @Override
    public void moveDocEnd(boolean select)
    {
        super.moveDocEnd(select);
    }

    @Override
    public void moveDocHome(boolean select)
    {
        super.moveDocHome(select);
    }

    @Override
    public void moveDown(boolean select)
    {
        super.moveDown(select);
    }

    @Override
    public void moveEnd(boolean select)
    {
        super.moveEnd(select);
    }

    @Override
    public void moveHome(boolean select)
    {
        super.moveHome(select);
    }

    @Override
    public void moveLeft(boolean select)
    {
        super.moveLeft(select);
    }

    @Override
    public void movePageDown(boolean select)
    {
        super.movePageDown(select);
    }

    @Override
    public void movePageUp(boolean select)
    {
        super.movePageUp(select);
    }

    @Override
    public void moveRight(boolean select)
    {
        super.moveRight(select);
    }

    @Override
    public void moveUp(boolean select)
    {
        super.moveUp(select);
    }

    @Override
    public void selectAll()
    {
        super.selectAll();
    }

    @Override
    public void setCaretPosition(int position)
    {
        super.setCaretPosition(position);
    }

    @Override
    public void toggleCaretVisibility()
    {
        super.toggleCaretVisibility();
    }

}
