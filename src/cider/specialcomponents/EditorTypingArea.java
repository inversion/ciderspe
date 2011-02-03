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

/**
 * A panel in which text is painted on the screen designed to fit in with our
 * concurrent editing objects. The purpose of this object is to offer an
 * alternative to Swing text objects which do not offer enough flexibility (in
 * particular with the way the caret is controlled and text formatting)
 * 
 * @author Lawrence
 * 
 */
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

    /**
     * Represents a line of text on the screen. Lists of these objects are
     * stored and their paint methods called whenever it is time to update the
     * graphics
     * 
     * @author Lawrence
     * 
     */
    class ETALine
    {
        TypingEventList str;
        int y;
        int ln;
        public int start;

        /**
         * 
         * @param tel
         *            the list of events in order that they should be printed on
         *            the screen from lift to right
         * @param y
         *            the number of pixels down the screen
         * @param ln
         *            the line number
         * @param start
         *            the caret position of the first character of this line
         */
        public ETALine(TypingEventList tel, int y, int ln, int start)
        {
            this.start = start;
            this.ln = ln;
            this.y = y;
            this.str = tel;
        }

        /**
         * paints the line number to the left of this line
         * 
         * @param g
         */
        public void paintMargin(Graphics g)
        {
            g.setColor(Color.GRAY);
            g.drawString("" + this.ln, 5, this.y);
        }

        /**
         * paints a character of this line, loop through i to draw the whole
         * string
         * 
         * @param g
         * @param i
         *            the character number of this line to be painted
         */
        public void paint(Graphics g, int i)
        {
            int x = (i * 7) + leftMargin;
            int y = this.y;
            g.setColor(Color.BLACK);
            g.drawString("" + str.get(i).text, x, y);
        }

        /**
         * draws a coloured box on this line (should be called before paint(g,
         * i)) - one use of this method is to indicate that this character is
         * locked. Different colours could represent different users
         * 
         * @param g
         * @param i
         * @param c
         */
        public void highlight(Graphics g, int i, Color c)
        {
            g.setColor(c);
            int x = (i * 7) + leftMargin;
            int y = this.y;
            g.fillRect(x, y - 10, 7, 10);
        }

        /**
         * paints a caret on this line at local character number i
         * 
         * @param g
         * @param i
         */
        public void paintCaret(Graphics g, int i)
        {
            g.setColor(Color.BLUE);
            int x = ((i + 1) * 7) + leftMargin;
            g.drawLine(x, this.y - 10, x, this.y);
        }

        /**
         * tests whether this character is part of a locked region, which is one
         * way of testing if it should be highlighted
         * 
         * @param i
         * @return
         */
        public boolean locked(int i)
        {
            return this.str.get(i).locked;
        }
    }

    /**
     * 
     * @param owner
     * @param codeLocation
     */
    public EditorTypingArea(String owner, ICodeLocation codeLocation)
    {
        this.codeLocation = codeLocation;
        this.doc = new SourceDocument(owner);
        this.doc.push(this.codeLocation.events());
        this.str = this.doc.playOutEvents(Long.MAX_VALUE);
        this.setupCaretFlashing();
        this.addMouseListener(this);
    }

    /**
     * The caret flashes to help draw attention to its location and indicate
     * that this component is in focus. This method schedules a timer task to
     * toggle between the caret being visible or invisible. Should only need to
     * be called once.
     */
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

    /**
     * Frequently called whenever it is time for the text to be updated. Takes
     * from the code location recent events that need to be pushed to doc and
     * then refreshes the lines and updates the UI.
     */
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

    /**
     * called by updateText() to split up text from doc and turn it into a list
     * of TypingEventLists
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

    /**
     * the caret indicates which part of the document text typing events will be
     * applied to. This method returns the position of the caret starting from
     * the very beginning of the document.
     * 
     * @return
     */
    public int getCaretPosition()
    {
        return this.caretPosition;
    }

    /**
     * moves the caret left one position and updates the UI
     */
    public void moveLeft()
    {
        if (this.caretPosition > 0)
            this.caretPosition--;

        this.updateUI();
    }

    /**
     * moves the caret right one position and updates the UI
     */
    public void moveRight()
    {
        if (this.caretPosition < this.str.length() - 1)
            this.caretPosition++;

        this.updateUI();
    }

    /**
     * used to retrieve the object from which typing events stored in this
     * EditorTypingArea's SourceDocument are drawn from
     * 
     * @return
     */
    public ICodeLocation getCodeLocation()
    {
        return this.codeLocation;
    }

    /**
     * the last time that this document was updated
     * 
     * @return
     */
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

    /**
     * converts the number of pixels from the top of the document to a line
     * number, useful for picking out a particular line for selection or locking
     * 
     * @param y
     *            pixels
     * @return
     */
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

    /**
     * used to find out if the position which the caret is currently at is part
     * of a locked out region. Used to find out whether the doc should accept a
     * particular event when it is being typed out by the user.
     * 
     * @param offset
     * @return
     */
    public boolean currentPositionLocked(int offset)
    {
        int pos = this.caretPosition + offset;
        return pos >= 0 && pos < this.str.length() && this.str.locked(pos);
    }
}
