package cider.specialcomponents.editorTypingArea;

import cider.common.processes.SourceDocument;

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
    public void moveHome()
    {
        super.moveHome();
    }

    @Override
    public void moveEnd()
    {
        super.moveEnd();
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
    public int GetCurLine()
    {
        return super.GetCurLine();
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
