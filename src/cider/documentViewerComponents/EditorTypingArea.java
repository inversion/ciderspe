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
    public void moveLeft( boolean select )
    {
        super.moveLeft( select );
    }

    @Override
    public void moveRight( boolean select )
    {
        super.moveRight( select );
    }

    @Override
    public void moveCaret(int spaces)
    {
        super.moveCaret(spaces);
    }

    @Override
    public void moveUp( boolean select )
    {
        super.moveUp( select );
    }

    @Override
    public void moveDown( boolean select )
    {
        super.moveDown( select );
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
    public void moveDocHome( boolean select )
    {
        super.moveDocHome( select );
    }
    
    @Override
    public void moveDocEnd( boolean select )
    {
        super.moveDocEnd( select );
    }

    @Override
    public void movePageUp( boolean select )
    {
        super.movePageUp( select );
    }
    

    @Override
    public void movePageDown( boolean select )
    {
        super.movePageDown( select );
    }

    @Override
    public int getCurLine()
    {
        return super.getCurLine();
    }
    
    @Override
    public void selectAll()
    {
        super.selectAll();
    }
    
    @Override
    public void copy()
    {
        super.copy();        
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
