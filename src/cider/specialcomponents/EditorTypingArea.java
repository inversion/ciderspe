package cider.specialcomponents;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JPanel;

import cider.client.gui.SourceEditor;
import cider.common.network.Client;
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
 * @param fontSize the size of the font in the editortypingarea
 */
public class EditorTypingArea extends JPanel implements MouseListener
{	
    private static final long serialVersionUID = 1L;
    private TypingEventList str = new TypingEventList();
    private int caretPosition = -1;
    private Font font = new Font("Monospaced", Font.PLAIN, fontSize);
    private Font fontbold = new Font("Monospaced", Font.BOLD, fontSize);
    // private ICodeLocation codeLocation = null;
    private SourceDocument doc = null;
    private boolean caretFlashing = true;
    private boolean caretVisible = false;
    public ArrayList<ETALine> lines = new ArrayList<ETALine>();
    private ArrayList<ActionListener> als = new ArrayList<ActionListener>();
    private ArrayList<Integer> KeyWord = new ArrayList<Integer>();
    public ETALine currentLine = null;
    private int currentColNum = 0;
    public static final int LINE_LOCKED = 0;
    public static final int LINE_UNLOCKED = 1;
    private static int fontSize = 14;
    private int leftMargin = fontSize*2;
    private static final int lineSpacing = fontSize+1;
    private static final int characterSpacing = fontSize/2+1;
    private boolean waiting = true;
    private boolean CommentFound = false;
    private boolean CommentedLine = false;
    private boolean isKey = false;
    private int CommentStartLoc = -1;

    private static Client parent;
    public static int Highlighting;

    /**
     * 
     * @param owner
     * @param codeLocation
     */
    public EditorTypingArea(String owner, SourceDocument doc)
    {
        this.doc = doc;
        this.str = this.doc.playOutEvents(Long.MAX_VALUE);
        this.setupCaretFlashing();
        this.addMouseListener(this);
    }

    @Override
    /**
     * calls paint methods on the ETALines
     */
    public void paintComponent(Graphics g2)
    {
        Graphics2D g = (Graphics2D) g2;
        super.paintComponent(g);
        int longestLine = 0;

        if (this.waiting)
        {
            this.paintWaitingSign(g);
        }
        else
        {
            g.setFont(font);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, this.getWidth(), this.getHeight());
            int p = 0;
            int ln = 0;
            boolean caretFound = false;
            try
            {
                for (ETALine line : this.lines)
                {
                    if (longestLine < line.str.length())
                        longestLine = line.str.length();

                    line.paintMargin(g);
                    line.characterColors();
                    String owner;

                    // Paints locking regions
                    for (int i = 0; i < line.str.length(); i++)
                        if ((owner = line.locked(i)) != null
                                || (i > 1 && (owner = line.locked(i - 1)) != null))
                            line.highlight(g, i, parent.colours.get(owner));

                    // If the caret is placed just after a newline
                    // that line might not actually have any text in it
                    if (!caretFound && p == this.caretPosition - ln + 1)
                    {
                        this.currentLine = line;
                        this.currentColNum = 0;
                        caretFound = true;
                        this.currentLine.highlightMargin(g);
                        this.currentLine.paintCaretOnNewline(g);
                    }

                    // Paints the lines of text and the caret if it has not
                    // already been drawn at the start
                    for (int i = 0; i < line.str.length(); i++)
                    {
                        if (!caretFound && p == this.caretPosition - ln)
                        {
                            line.paintCaret(g, i);
                            currentLine = line;
                            currentColNum = i + 1;
                            caretFound = true;
                            line.highlightMargin(g);
                        }
                        p++;
                        line.paintCharacter(g, i);
                    }
                    ln++;
                    CommentedLine = false;
                    KeyWord.clear();
                    KeyWord.add(-1);
                    KeyWord.add(-1);
                }
                p++;
            }
            catch (ConcurrentModificationException e)
            {
                // ignore for now
            }
        }
        int width = longestLine * characterSpacing + leftMargin;
        int height = lineSpacing * lines.size();
        this.setPreferredSize(new Dimension(width, height));
        this.invalidate();
    }

    public void paintWaitingSign(Graphics g)
    {
        g.setFont(new Font("SansSerif", Font.PLAIN, fontSize));
        g.drawString("Retrieving document from server... ", 32, 32);
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
        return (int) (y / (double) lineSpacing) + 1;
    }

    public int xToColumnNumber(int x)
    {
        return (int) ((x - this.leftMargin) / (double) characterSpacing);
    }

    @Override
    /**
     * mouse presses are currently used to lock out lines of text
     */
    public void mousePressed(MouseEvent me)
    {
        int ln = yToLineNumber(me.getY());
        if (ln > this.lines.size())
            ln = this.lines.size();

        if (this.lines.size() > 0)
        {
            ETALine line = this.lines.get(ln - 1);
            int start = line.start + ln - 2;
            int length = line.str.length();

            if (me.getX() < this.leftMargin && length > 1)
            {
                TypingEventMode tem;
                String owner = line.locked(0);

                if (length > 0 && owner != null
                        && owner.equals(parent.getUsername()))
                    tem = TypingEventMode.unlockRegion;
                else if (owner == null)
                    tem = TypingEventMode.lockRegion;
                else
                    tem = null;

                if (tem != null)
                {
                    TypingEvent te = new TypingEvent(System.currentTimeMillis()
                            + parent.getClockOffset(), tem, start, length, "",
                            parent.getUsername(), null);

                    for (ActionListener al : this.als)
                        al.actionPerformed(new ActionEvent(te,
                                tem == TypingEventMode.lockRegion ? LINE_LOCKED
                                        : LINE_UNLOCKED, "Locking event"));
                }
            }
            else
            {
                this.caretPosition = start + this.xToColumnNumber(me.getX());
            }
        }

        this.updateUI();
    }

    /**
     * Represents a line of text on the screen. Lists of these objects are
     * stored and their paint methods called whenever it is time to update the
     * graphics
     * 
     * @author Lawrence
     * 
     */
    public class ETALine
    {
        public TypingEventList str;
        public int y;
        public int lineNum;
        public int start;
        Color[] colors;

        /**
         * 
         * @param tel
         *            the list of events in order that they should be printed on
         *            the screen from lift to right
         * @param y
         *            the number of pixels down the screen
         * @param lineNum
         *            the line number
         * @param start
         *            the caret position of the first character of this line
         */
        public ETALine(TypingEventList tel, int y, int lineNum, int start)
        {
            this.start = start;
            this.lineNum = lineNum;
            this.y = y;
            this.str = tel;
            this.colors = new Color[this.str.length()];
        }

        /**
         * ------------------ >>> LOOK HERE <<< ------------------
         * 
         * Syntax Highlighting: I've provided an example here to help. You might
         * want to store the colours in a hashtable or something.
         */

        public void characterColors()
        {
            LinkedList<TypingEventList> words = this.str
                    .splitWords(new String[] { " ", "(", ")", ";", "\t", ":",
                            "#", "{", "}" });
            int i = 0;
            String str;
            int length;
            Color customColor = null;
            for (TypingEventList word : words)
            {
                str = word.toString();
                str = str.toLowerCase();
                length = str.length();
                if (Highlighting == 0)
                    if ((CommentFound == false)
                            || ((this.lineNum < CommentStartLoc)
                                    && (CommentStartLoc != -1) && (CommentFound == true)))
                    {
                        if (str.startsWith("/*") == true)
                        {
                            CommentFound = true;
                            CommentStartLoc = this.lineNum;
                            wash(this.colors, Color.RED, i, i + length);
                        }
                        if (SourceEditor.keywords.contains(str))
                        {
                            wash(this.colors, Color.BLUE, i, i + length);
                            KeyWord.add(i);
                            KeyWord.add(i + length);
                        }
                        if (isParsableToNum(str) == true)
                            customColor = new Color(0, 100, 0);
                        wash(this.colors, customColor, i, i + length);
                        if (str.startsWith("//") == true)
                        {
                            wash(this.colors, Color.RED, i, i + length);
                            CommentedLine = true;
                        }
                        if (CommentedLine == true)
                            wash(this.colors, Color.RED, i, i + length);
                    }
                    else
                    {
                        wash(this.colors, Color.RED, i, i + length);
                        if (str.endsWith("*/") == true)
                            CommentFound = false;
                    }
                if (Highlighting == 1)
                {
                    userwash(this.colors, this.lineNum, i, i + length);
                }
                i += length + 1;

            }
        }

        /**
         * Draws around the current line number that the user is currently on
         * 
         * @param g
         */
        public void highlightMargin(Graphics g)
        {
            g.setColor(parent.colours.get(parent.getUsername()));
            g.fillRoundRect(3, this.y - lineSpacing+2, leftMargin - 8, lineSpacing+2, 3, 3); //TODO: here Alex
            g.setColor(Color.LIGHT_GRAY);
            g.drawRoundRect(3, this.y - lineSpacing+2, leftMargin - 8, lineSpacing+2, 3, 3);
            paintMargin(g);
        }

        public boolean isParsableToNum(String str)
        {
            try
            {
                Float.parseFloat(str);
                return true;
            }
            catch (NumberFormatException nfe)
            {
                return false;
            }
        }

        /**
         * paints the line number to the left of this line
         * 
         * @param g
         */
        public void paintMargin(Graphics g)
        {
            g.setColor(Color.GRAY);
            g.drawString("" + this.lineNum, 5, this.y);
        }

        /**
         * paints a character of this line, loop through i to draw the whole
         * string
         * 
         * @param g
         * @param i
         *            the character number of this line to be painted
         */
        public void paintCharacter(Graphics g, int i)
        {
            int x = (i * characterSpacing) + leftMargin;
            int y = this.y;

            isKey = false;

            g.setColor(this.colors[i] != null ? this.colors[i] : Color.BLACK);

            for (int j = 0; j < KeyWord.size(); j = j + 2)
            {
                if ((i >= KeyWord.get(j)) && (i <= KeyWord.get(j + 1)))
                    isKey = true;
            }
            if (isKey == true)
            {
                g.setFont(fontbold);
                g.drawString("" + str.get(i).text, x, y);
                g.setFont(font);
            }
            else
            {
                g.setFont(font);
                g.drawString("" + str.get(i).text, x, y);
            }
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
            int x = (i * characterSpacing) + leftMargin;
            int y = this.y+5;
            g.fillRect(x, y - lineSpacing, characterSpacing, lineSpacing); //TODO here Alex
        }

        /**
         * paints a caret on this line at local character number i
         * 
         * @param g
         * @param i
         */
        public void paintCaret(Graphics g, int i)
        {
            g.setColor(parent.colours.get(parent.getUsername())/*Color.BLUE*/);
            int x = ((i + 1) * characterSpacing) + leftMargin;
            int y = this.y+5;
            g.drawLine(x, y - lineSpacing, x, y); //TODO here Alex
        }

        public void paintCaretOnNewline(Graphics g)
        {
            g.setColor(parent.colours.get(parent.getUsername())/*Color.BLUE*/);
            int y = this.y+5;
            g.drawLine(leftMargin, y - lineSpacing, leftMargin, y);  //TODO here Alex
        }

        /**
         * tests whether this character is part of a locked region, which is one
         * way of testing if it should be highlighted
         * 
         * @param i
         * @return
         */
        public String locked(int i)
        {
            return this.str.get(i).lockingGroup;
        }
    }

    public static void wash(Color[] target, Color color, int start, int end)
    {
        start = start < 0 ? 0 : start;
        end = end < target.length ? end : target.length;
        for (int i = start; i < end; i++)
            target[i] = color;
    }

    public void userwash(Color[] target, int LineNo, int start, int end)
    {
        String uname;
        Color usercolor;
        start = start < 0 ? 0 : start;
        end = end < target.length ? end : target.length;

        for (int i = start; i < end; i++)
        {
            /*
             * TODO text owned by offline users who have not had their profile
             * downloaded from the bot will be black/blank
             */
            uname = lines.get(LineNo - 1).str.get(i).owner;
            usercolor = Client.colours.get(uname);
            target[i] = usercolor;
        }
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

    public void setWaiting(boolean waiting)
    {
        this.waiting = waiting;
    }

    /**
     * Frequently called whenever it is time for the text to be updated. Takes
     * from the code location recent events that need to be pushed to doc and
     * then refreshes the lines and updates the UI.
     */
    public void updateText()
    {
        if (this.doc != null)
        {
            this.str = this.doc.playOutEvents(Long.MAX_VALUE);
            this.refreshLines();
            this.updateUI();
        }
    }

    /**
     * called by updateText() to split up text from doc and turn it into a list
     * of TypingEventLists
     */
    private void refreshLines()
    {
        // System.out.println(this.lines.size());
        // Profile.adjustCharCount(-this.lines.size());
        this.lines.clear();
        LinkedList<TypingEventList> split = this.str.split("\n");
        int j = 1;
        int i = 0;
        for (TypingEventList tel : split)
        {
            ETALine line = new ETALine(tel, j * lineSpacing, j++, i);
            this.lines.add(line);
            i += line.str.length();
            // System.out.println(line.str.length());
            // Profile.adjustCharCount(line.str.length());
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
        if (this.caretPosition >= 0)
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

    // TODO:
    // clean up, reuse code between these two methods
    public void moveUp()
    {
        if (this.currentLine.str.newline())
            this.moveLeft();
        else
        {
            int lineNum = this.currentLine.lineNum - 1;
            if (lineNum < 1)
                lineNum = 1;
            ETALine line = this.lines.get(lineNum - 1);
            int start = line.start + line.lineNum - 2;
            int length = line.str.length();
            if (this.currentColNum >= length)
                this.currentColNum = length;
            this.caretPosition = start + this.currentColNum;
            this.updateUI();
        }
    }

    public void moveDown()
    {
        if (this.currentLine.str.newline())
            this.moveRight();
        else
        {
            int lineNum = this.currentLine.lineNum + 1;
            if (lineNum > this.lines.size())
                lineNum = this.lines.size();
            ETALine line = this.lines.get(lineNum - 1);
            int start = line.start + line.lineNum - 2;
            int length = line.str.length();
            if (this.currentColNum >= length)
                this.currentColNum = length;
            this.caretPosition = start + this.currentColNum;
            this.updateUI();
        }
    }

    /**
     * Move the caret to the beginning of the current line and update the UI.
     * 
     * @author Andrew, Lawrence
     */
    public void moveHome()
    {
        int start = this.currentLine.start + this.currentLine.lineNum - 2;
        this.caretPosition = start;
        this.updateUI();
    }

    /**
     * Move the caret to the end of the current line and update the UI.
     * 
     * @author Andrew, Lawrence
     */
    public void moveEnd()
    {
        int start = this.currentLine.start + this.currentLine.lineNum - 2;
        int length = this.currentLine.str.length();
        this.caretPosition = start + length;
        this.updateUI();
    }

    /**
     * Move the caret to the start of the file and update the UI.
     * 
     */
    public void moveDocHome()
    {
        int start = -1;
        this.caretPosition = start;
        this.updateUI();
    }

    /**
     * Move the caret to the end of the file and update the UI.
     * 
     */
    public void moveDocEnd()
    {
        int numLines = this.lines.size();
        ETALine line = this.lines.get(numLines - 1);
        int startLastLine = line.start + numLines - 2;
        int length = line.str.length();
        this.caretPosition = startLastLine + length;
        this.updateUI();
    }

    /**
     * Move the caret up a page and update the UI.
     * 
     * @author Andrew
     */
    public void movePageUp()
    {
        // TODO: Not implemented yet
    }

    /**
     * Move the caret down a page and update the UI.
     * 
     * @author Andrew
     */

    public int GetCurLine()
    {
        try
        {
            return this.currentLine.lineNum;
        }
        catch (NumberFormatException nfe)
        {
            return 0;
        }
    }

    public int GetCurxLen()
    {
        return 0;
        // below doesn't work
        // return (i * characterSpacing) + leftMargin;
    }

    public void movePageDown()
    {
        // TODO: Not implemented yet
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
    public int currentPositionLocked(int offset, String user)
    {
        int pos = this.caretPosition + offset;
        return pos >= 0 && pos < this.str.length() ? this.str.locked(pos, user)
                : 0;
    }

    public void moveCaret(int spaces)
    {
        this.caretPosition += spaces;
        constrain();
        this.updateUI();
    }

    public void setCaretPosition(int position)
    {
        this.caretPosition = position;
        constrain();

        this.updateUI();
    }

    private void constrain()
    {
        if (this.caretPosition > this.str.length())
            this.caretPosition = this.str.length() - 1;
        if (this.caretPosition < -1)
            this.caretPosition = -1;
    }

    public TypingEventList getTypingEventList()
    {
        return this.str;
    }

    public static void addParent(Client p)
    {
        parent = p;
    }

    public SourceDocument getSourceDocument()
    {
        return this.doc;
    }
}
