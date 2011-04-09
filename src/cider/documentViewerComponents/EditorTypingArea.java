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

    public EditorTypingArea(SourceDocument doc)
    {
        super(doc);
        this.setupCaretFlashing();
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
    }

    @Override
    public int getCaretPosition()
    {
        return super.getCaretPosition();
    }

    @Override
    public void moveLeft()
    {
        super.moveLeft();
    }

    @Override
    public void moveRight()
    {
        super.moveRight();
    }

    @Override
    public void moveCaret(int spaces)
    {
        super.moveCaret(spaces);
    }

    @Override
    public void moveUp()
    {
        super.moveUp();
    }

    @Override
    public void moveDown()
    {
        super.moveDown();
    }

    @Override
    public void moveHome( boolean select )
    {
        super.moveHome( select );
    }

    @Override
    public void moveEnd( boolean select)
    {
        super.moveEnd( select );
    }

    @Override
    public void moveDocHome()
    {
        super.moveDocEnd();
    }

    @Override
    public void movePageUp()
    {
        super.movePageUp();
    }

    @Override
    public int getCurLine()
    {
        return super.getCurLine();
    }

    @Override
    public void moveDocEnd()
    {
        super.moveDocEnd();
    }

    @Override
    public void movePageDown()
    {
        super.movePageDown();
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
    public int currentPositionLocked(int offset, String user)
    {
        return super.currentPositionLocked(offset, user);
    }

    @Override
    public void toggleCaretVisibility()
    {
        super.toggleCaretVisibility();
    }

    @Override
    public TypingRegion getSelectedRegion()
    {
        return super.getSelectedRegion();
    }

    @Override
    public SDVLine getCurrentLine()
    {
        return super.getCurrentLine();
    }
}
