package cider.specialcomponents.editorTypingArea;

import java.util.TimerTask;

public class CaretVisibilityToggler extends TimerTask
{
    private EditorTypingArea eta;
    private boolean skipNextToggle = false;

    public CaretVisibilityToggler(EditorTypingArea eta)
    {
        this.eta = eta;
    }

    public void skipNextToggle()
    {
        this.skipNextToggle = true;
    }

    @Override
    public void run()
    {
        if (this.skipNextToggle)
            this.skipNextToggle = false;
        else
            this.eta.toggleCaretVisibility();
    }

}
