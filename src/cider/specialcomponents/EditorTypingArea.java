package cider.specialcomponents;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JPanel;

import cider.common.processes.ICodeLocation;
import cider.common.processes.SourceDocument;
import cider.common.processes.TypingEvent;

public class EditorTypingArea extends JPanel
{
    private String str = "";
    private int caretPosition = -1;
    private Font font = new Font("monospaced", Font.PLAIN, 11);
    private ICodeLocation codeLocation = null;
    private SourceDocument doc = null;
    private long lastUpdateTime = 0;
    private boolean caretFlashing = true;
    private boolean caretVisible = false;

    public EditorTypingArea()
    {
        this.setupCaretFlashing();
    }

    public EditorTypingArea(ICodeLocation codeLocation)
    {
        this.codeLocation = codeLocation;
        this.doc = new SourceDocument();
        this.doc.push(this.codeLocation.events());
        this.str = this.doc.toString();
        this.setupCaretFlashing();
    }

    private void setupCaretFlashing()
    {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask()
        {

            @Override
            public void run()
            {
                caretVisible = caretFlashing && !caretVisible;
                if (caretFlashing)
                    updateUI();
            }

        }, 0, 500);

        this.addFocusListener(new FocusListener()
        {

            @Override
            public void focusGained(FocusEvent e)
            {
                System.out.println("focus!");
                caretFlashing = true;
            }

            @Override
            public void focusLost(FocusEvent e)
            {
                caretFlashing = false;
            }

        });
    }

    public void updateText()
    {
        if (this.codeLocation != null)
        {
            Queue<TypingEvent> events = new LinkedList<TypingEvent>();
            long latest = this.lastUpdateTime;
            for (TypingEvent te : this.codeLocation
                    .eventsSince(this.lastUpdateTime))
            {
                events.add(te);
                if (latest <= te.time)
                    latest = te.time + 1;
            }
            this.doc.push(events);
            this.str = this.doc.toString();
            this.updateUI();
            this.lastUpdateTime = latest;
        }
    }

    public void setText(String text) throws Throwable
    {
        if (this.codeLocation != null)
            throw new Exception(
                    "Cannot set text directly as this EditorTypingArea is tied to an ICodeLocation");
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

        if (this.caretVisible)
        {
            int localCP = this.caretPosition - caretLineStart;
            // g.drawString("" + localCP, 400, 400);
            int x = (localCP + 2) * 7;
            int y = (caretLine == 0 ? 0 : caretLine - 1) * 10;
            g.drawLine(x + 10, y, x + 10, y + 11);
        }
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

    public ICodeLocation getCodeLocation()
    {
        return this.codeLocation;
    }

    public long getLastUpdate()
    {
        return this.lastUpdateTime;
    }
}
