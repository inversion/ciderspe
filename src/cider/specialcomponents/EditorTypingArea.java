package cider.specialcomponents;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JPanel;

import cider.common.processes.ICodeLocation;
import cider.common.processes.SourceDocument;
import cider.common.processes.TypingEvent;
import cider.common.processes.TypingEventList;
import cider.common.processes.TypingEventMode;

public class EditorTypingArea extends JPanel implements MouseListener
{
    private TypingEventList str = new TypingEventList();
    private int caretPosition = -1;
    private Font font = new Font("monospaced", Font.PLAIN, 11);
    private ICodeLocation codeLocation = null;
    private SourceDocument doc = null;
    private long lastUpdateTime = 0;
    private boolean caretFlashing = true;
    private boolean caretVisible = false;
    private int leftMargin = 32;
    private ArrayList<ETALine> lines = new ArrayList<ETALine>();
    private ArrayList<ActionListener> als = new ArrayList<ActionListener>();
    public static final int LINE_LOCKED = 0;
    public static final int LINE_UNLOCKED = 1;

    class ETALine
    {
        TypingEventList str;
        int y;
        int ln;
        public int start;

        public ETALine(TypingEventList tel, int y, int ln, int start)
        {
            this.start = start;
            this.ln = ln;
            this.y = y;
            this.str = tel;
        }

        public void paintMargin(Graphics g)
        {
            g.setColor(Color.GRAY);
            g.drawString("" + this.ln, 5, this.y);
        }

        public void paint(Graphics g, int i)
        {
            int x = (i * 7) + leftMargin;
            int y = this.y;
            g.setColor(Color.BLACK);
            g.drawString("" + str.get(i).text, x, y);
        }

        public void highlight(Graphics g, int i, Color c)
        {
            g.setColor(c);
            int x = (i * 7) + leftMargin;
            int y = this.y;
            g.fillRect(x, y - 10, 7, 10);
        }

        public void paintCaret(Graphics g, int i)
        {
            g.setColor(Color.BLUE);
            int x = ((i + 1) * 7) + leftMargin;
            g.drawLine(x, this.y - 10, x, this.y);
        }

        public boolean locked(int i)
        {
            return this.str.get(i).locked;
        }
    }

    public EditorTypingArea(String owner, ICodeLocation codeLocation)
    {
        this.codeLocation = codeLocation;
        this.doc = new SourceDocument(owner);
        this.doc.push(this.codeLocation.events());
        this.str = this.doc.playOutEvents(Long.MAX_VALUE);
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
            this.str = this.doc.playOutEvents(Long.MAX_VALUE);
            this.refreshLines();
            this.updateUI();
            this.lastUpdateTime = latest;
        }
    }

    /*
     * public void setText(String text) throws Throwable { if (this.codeLocation
     * != null) throw new Exception(
     * "Cannot set text directly as this EditorTypingArea is tied to an ICodeLocation"
     * ); this.str = text; this.refreshLines(); this.updateUI(); }
     */

    private void refreshLines()
    {
        this.lines.clear();
        LinkedList<TypingEventList> split = this.str.split("\n");
        int j = 1;
        int i = 0;
        for (TypingEventList tel : split)
        {
            ETALine line = new ETALine(tel, j * 10, j++, i);
            this.lines.add(line);
            i += line.str.length();
        }
    }

    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        g.setFont(font);
        g.clearRect(0, 0, this.getWidth(), this.getHeight());
        int p = 0;
        int ln = 0;
        try
        {
            for (ETALine line : this.lines)
            {
                line.paintMargin(g);

                for (int i = 0; i < line.str.length(); i++)
                    if (line.locked(i))
                        line.highlight(g, i, Color.LIGHT_GRAY);

                for (int i = 0; i < line.str.length(); i++)
                {
                    if (p == this.caretPosition - ln)
                        line.paintCaret(g, i);
                    p++;
                    line.paint(g, i);
                }
                ln++;
            }
            p++;
        }
        catch (ConcurrentModificationException e)
        {
            // ignore
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
        if (this.caretPosition < this.str.length() - 1)
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

    public int yToLineNumber(int y)
    {
        return (int) (y / 10.0) + 1;
    }

    @Override
    public void mousePressed(MouseEvent me)
    {
        int ln = yToLineNumber(me.getY());
        if (ln > this.lines.size())
            ln = this.lines.size();
        ETALine line = this.lines.get(ln - 1);
        int start = line.start + ln - 2;
        int length = line.str.length();

        TypingEvent te = new TypingEvent(System.currentTimeMillis(),
                TypingEventMode.lockRegion, start, length, "", "");

        for (ActionListener al : this.als)
            al
                    .actionPerformed(new ActionEvent(te, LINE_LOCKED,
                            "Lines locked"));

        this.updateUI();
    }

    @Override
    public void mouseReleased(MouseEvent arg0)
    {
        // TODO Auto-generated method stub

    }

    public void addActionListener(ActionListener al)
    {
        this.als.add(al);
    }

    public void removeActionListeners()
    {
        this.als.clear();
    }

    public boolean currentPositionLocked(int offset)
    {
        int pos = this.caretPosition + offset;
        return pos >= 0 && pos < this.str.length() && this.str.locked(pos);
    }
}
