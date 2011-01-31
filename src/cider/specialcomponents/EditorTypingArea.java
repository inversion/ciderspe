package cider.specialcomponents;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JPanel;

import cider.common.processes.ICodeLocation;
import cider.common.processes.SourceDocument;
import cider.common.processes.TypingEvent;

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
    private ArrayList<ETALine> lines = new ArrayList<ETALine>();

    class ETALine
    {
        char[] str;
        int y;

        public ETALine(String str, int y, int startCP)
        {
            this.y = y;
            this.str = str.toCharArray();
        }

        public void paint(Graphics g, int i)
        {
            g.setColor(Color.BLACK);
            g.drawString("" + str[i], i * 7, this.y);
        }

        public void paintCaret(Graphics g, int i)
        {
            g.setColor(Color.BLUE);
            int x = (i + 1) * 7;
            g.drawLine(x, this.y - 10, x, this.y);
        }
    }

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
            this.refreshLines();
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
        this.refreshLines();
        this.updateUI();
    }

    private void refreshLines()
    {
        this.lines.clear();
        String[] split = this.str.split("\\n");
        int j = 1;
        for (String lineStr : split)
        {
            this.lines.add(new ETALine(lineStr, j * 10, j++));
        }
    }

    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        g.setFont(font);
        g.clearRect(0, 0, this.getWidth(), this.getHeight());
        int p = 0;
        for (ETALine line : this.lines)
        {
            for (int i = 0; i < line.str.length; i++)
            {
                if (p == this.caretPosition)
                    line.paintCaret(g, i);
                line.paint(g, i);
                p++;
            }
            p++;
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

    }

    @Override
    public void mouseReleased(MouseEvent arg0)
    {
        // TODO Auto-generated method stub

    }
}
