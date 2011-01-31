package cider.specialcomponents;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import javax.swing.JPanel;

import cider.common.processes.ICodeLocation;
import cider.common.processes.SourceDocument;
import cider.common.processes.TypingEvent;
import cider.common.processes.TypingEventMode;

public class EditorTypingArea extends JPanel implements MouseListener
{
    private String str = "";
    private int caretPosition = -1;
    private Font font = new Font("monospaced", Font.PLAIN, 11);
    private ICodeLocation codeLocation = null;
    private SourceDocument doc = null;
    private long lastUpdateTime = 0;
    private boolean caretFlashing = true;
    private boolean caretVisible = false;
    private int leftMargin = 16;
    private TreeSet<Integer> lineStarts = new TreeSet<Integer>();

    public EditorTypingArea()
    {
        this.setupCaretFlashing();
        this.addMouseListener(this);
    }

    public EditorTypingArea(ICodeLocation codeLocation)
    {
        this.codeLocation = codeLocation;
        this.doc = new SourceDocument();
        this.doc.push(this.codeLocation.events());
        this.str = this.doc.toString();
        this.setupCaretFlashing();
        this.addMouseListener(this);
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

    private void paintLine(Graphics g, String line, int lineNumber,
            boolean highlighted)
    {
        g.setFont(font);
        g.setColor(Color.black);
        int sy = lineNumber * 10;
        int ry = lineNumber * 5 - 2;
        if (highlighted)
            g.drawRect(0, ry, this.getWidth(), 3);
        g.drawString(line, 10 + this.leftMargin, sy);
        g.setColor(new Color(0.9f, 0.9f, 0.9f));
        g.setColor(Color.LIGHT_GRAY);
        g.drawString("" + lineNumber, 3, sy);
    }

    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        g.clearRect(0, 0, this.getWidth(), this.getHeight());
        String[] lines = this.str.split("\n");
        int i = 0;
        int ln = 0;
        this.lineStarts.clear();
        for (String line : lines)
        {
            line = line.replace("\n", "");
            this.lineStarts.add(i);
            g.setColor(Color.LIGHT_GRAY);
            boolean lockedOut = this.doc != null && this.doc.lockedOut(i);
            this.paintLine(g, line, ++ln, lockedOut);
            i += line.length();
        }

        if (this.caretVisible)
        {

            int cln = this.getLineNumberOf(this.caretPosition);
            if (cln < 1)
                cln++;
            Integer lineStart = (Integer) this.lineStarts.toArray()[cln - 1];
            int localCP = this.caretPosition - lineStart - cln + 1;
            int x = (localCP * 7) + this.leftMargin + 16;
            // System.out.println(cln);
            int y = ((cln - 1) * 10);
            g.setColor(Color.BLUE);
            g.drawLine(x, y, x, y + 10);

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

    @Override
    public void mouseClicked(MouseEvent arg0)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseEntered(MouseEvent arg0)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseExited(MouseEvent arg0)
    {
        // TODO Auto-generated method stub

    }

    public int getStartOfLineContaining(int position)
    {
        Integer ln = this.lineStarts.lower(position);
        return ln != null ? ln : this.lineStarts.size() - 1;
    }

    public int getEndOfLineContaining(int position)
    {
        Integer ln = this.lineStarts.higher(position - 1);
        return ln != null ? ln : this.lineStarts.size() - 1;
    }

    private int getLineNumberOf(int localCP)
    {
        Integer ln = this.lineStarts.headSet(localCP).size();
        return ln != null ? ln : this.lineStarts.size() - 1;
    }

    public int yToLineNumber(int y)
    {
        return (int) (y / 10.0) + 1;
    }

    @Override
    public void mousePressed(MouseEvent me)
    {
        int ln = yToLineNumber(me.getY());
        int start = (Integer) this.lineStarts.toArray()[Math.min(
                this.lineStarts.size() - 1, ln - 1)];
        int end = this.getEndOfLineContaining(start + 1) - 1;
        this.caretPosition = start;
        this.updateUI();
        System.out.println("locking " + start + ", " + end + ", ln " + ln);

        if (this.codeLocation != null)
        {
            Queue<TypingEvent> tes = new LinkedList<TypingEvent>();
            tes.add(new TypingEvent(lastUpdateTime, TypingEventMode.lockRegion,
                    start, end, "", doc.getOwner()));
            this.codeLocation.push(tes);
        }
        else
            System.out.println("no code location to send locking event to");
    }

    @Override
    public void mouseReleased(MouseEvent arg0)
    {
        // TODO Auto-generated method stub

    }
}
