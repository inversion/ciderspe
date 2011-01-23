package cider.specialcomponents;

import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.JPanel;

import cider.common.processes.ICodeLocation;
import cider.common.processes.SourceDocument;

public class EditorTypingArea extends JPanel
{
    private String str = "";
    private int caretPosition = -1;
    private Font font = new Font("monospaced", Font.PLAIN, 11);
    private ICodeLocation codeLocation = null;
    private SourceDocument doc = null;
    private Component tabHandle = null;

    public EditorTypingArea()
    {
    }

    public EditorTypingArea(ICodeLocation codeLocation)
    {
        this.codeLocation = codeLocation;
        this.doc = new SourceDocument();
        this.doc.push(this.codeLocation.events());
        this.str = this.doc.toString();
    }

    public void setText(String text)
    {
        if (this.codeLocation != null)
        {
            this.doc = new SourceDocument();
            this.codeLocation.clearAll();
        }

        this.str = text;
        this.updateUI();
    }

    private void paintLine(Graphics g, String line, int lineNumber)
    {
        g.setFont(font);
        g.drawString(line, 10, lineNumber * 10);
    }

    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        g.clearRect(0, 0, this.getWidth(), this.getHeight());
        String[] lines = this.str.split("\n");
        int i = 0;
        int ln = 0;
        int caretLine = 0;
        int caretLineStart = 0;
        for (String line : lines)
        {
            this.paintLine(g, line, ++ln);
            if (this.caretPosition >= i)
            {
                caretLine++;
                caretLineStart = i + ln;
            }
            i += line.replaceAll("\n", "").length();
        }

        int localCP = this.caretPosition - caretLineStart;
        // g.drawString("" + localCP, 400, 400);
        int x = (localCP + 2) * 7;
        int y = (caretLine - 1) * 10;
        g.drawLine(x + 10, y, x + 10, y + 11);
    }

    public int getCaretPosition()
    {
        return this.caretPosition;
    }

    public void moveLeft()
    {
        if (this.caretPosition > 0)
            this.caretPosition--;
        this.updateUI();
    }

    public void moveRight()
    {
        if (this.caretPosition < this.str.length())
            this.caretPosition++;
        this.updateUI();
    }

    public void setTabHandle(Component tabHandle)
    {
        this.tabHandle = tabHandle;
    }

    public Component getTabHandle()
    {
        return this.tabHandle;
    }
}
