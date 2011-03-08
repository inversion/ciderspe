package cider.specialcomponents.editorTypingArea;

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
 * @author Lawrence, Miles and Alex
 * @param fontSize
 *            the size of the font in the editortypingarea
 */
public class EditorTypingArea extends JPanel implements MouseListener
{
    private static final long serialVersionUID = 1L;
    private TypingEventList str = new TypingEventList();
    private int caretPosition = 0;
    // private ICodeLocation codeLocation = null;
    private SourceDocument doc = null;
    private boolean caretFlashing = true;
    private boolean caretVisible = false;
    private ArrayList<ETALine> lines = new ArrayList<ETALine>();
    private ArrayList<ActionListener> als = new ArrayList<ActionListener>();
    private ArrayList<Integer> KeyWord = new ArrayList<Integer>();
    private ETALine currentLine = null;
    private int currentColNum = 0;
    public static final int LINE_LOCKED = 0;
    public static final int LINE_UNLOCKED = 1;
    public static final int fontSize = 14;
    public static final Font fontbold = new Font("Monospaced", Font.BOLD,
            fontSize);
    public static final Font font = new Font("Monospaced", Font.PLAIN, fontSize);
    public static final int leftMargin = fontSize * 2;
    public static final int lineSpacing = fontSize + 1;
    public static final int characterSpacing = fontSize / 2 + 1;
    private boolean waiting = true;
    private boolean commentFound = false;
    private boolean commentedLine = false;
    private boolean isKey = false;
    private int commentStartLoc = -1;

    static Client parent;
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
            // g.fillRect(0, 0, this.getWidth(), this.getHeight());
            int p = 0;
            int ln = 0;
            boolean caretFound = false;
            try
            {
                if (this.lines.size() == 0)
                    this.paintCaret(g, -1, lineSpacing);
                else
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
                                line.highlight(g, i,
                                        glass(parent.colours.get(owner)));

                        // If the caret is placed just after a newline
                        // that line might not actually have any text in it
                        if (!caretFound && p == this.caretPosition - ln + 1)
                        {
                            this.setCurrentLine(line);
                            this.currentColNum = 0;
                            caretFound = true;
                            this.getCurrentLine().highlightMargin(g);
                            this.paintCaret(g, -1, line.y);
                        }

                        // Paints the lines of text and the caret if it has not
                        // already been drawn at the start
                        for (int i = 0; i < line.str.length(); i++)
                        {
                            if (!caretFound && p == this.caretPosition - ln)
                            {
                                this.paintCaret(g, i, line.y);
                                setCurrentLine(line);
                                currentColNum = i + 1;
                                caretFound = true;
                                line.highlightMargin(g);
                            }
                            p++;
                            line.paintCharacter(g, i);
                        }
                        ln++;
                        setCommentedLine(false);
                        getKeyWord().clear();
                        getKeyWord().add(-1);
                        getKeyWord().add(-1);
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

    private void paintCaret(Graphics g, int i, int lineY)
    {
        if (this.caretVisible)
        {
            g.setColor(EditorTypingArea.parent.colours
                    .get(EditorTypingArea.parent.getUsername())/* Color.BLUE */);
            int x = ((i + 1) * EditorTypingArea.characterSpacing)
                    + EditorTypingArea.leftMargin;
            int y = lineY + 5;
            g.fillRect(x, y - EditorTypingArea.lineSpacing, 3,
                    EditorTypingArea.lineSpacing);
        }
    }

    private static Color glass(Color color)
    {
        return color == null ? null : new Color(color.getRed(),
                color.getGreen(), color.getBlue(), color.getAlpha() / 3);
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
        return (int) ((x - leftMargin) / (double) characterSpacing);
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

            if (me.getX() < leftMargin && length > 1)
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
                int colNumber = this.xToColumnNumber(me.getX());
                if (colNumber > length)
                    colNumber = length;
                this.caretPosition = start + colNumber;
            }
        }

        this.updateUI();
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
                caretVisible = hasFocus() && caretFlashing && !caretVisible;
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
            ETALine line = new ETALine(tel, j * lineSpacing, j++, i, this);
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

        this.caretVisible = true;
        this.updateUI();
    }

    /**
     * moves the caret right one position and updates the UI
     */
    public void moveRight()
    {
        if (this.caretPosition < this.str.length() - 1)
            this.caretPosition++;

        this.caretVisible = true;
        this.updateUI();
    }

    // TODO:
    // clean up, reuse code between these two methods
    public void moveUp()
    {
        if (this.getCurrentLine().str.newline())
            this.moveLeft();
        else
        {
            this.caretVisible = true;
            int lineNum = this.getCurrentLine().lineNum - 1;
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
        if (this.getCurrentLine().str.newline())
            this.moveRight();
        else
        {
            this.caretVisible = true;
            int lineNum = this.getCurrentLine().lineNum + 1;
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
        int start = this.getCurrentLine().start + this.getCurrentLine().lineNum
                - 2;
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
        int start = this.getCurrentLine().start + this.getCurrentLine().lineNum
                - 2;
        int length = this.getCurrentLine().str.length();
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
            return this.getCurrentLine().lineNum;
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
        if (this.caretPosition > this.str.length() + 1)
            this.caretPosition = this.str.length();
        if (this.caretPosition < 0)
            this.caretPosition = 0;
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

    public void setCommentedLine(boolean commentedLine)
    {
        this.commentedLine = commentedLine;
    }

    public boolean isCommentedLine()
    {
        return commentedLine;
    }

    public void setKeyWord(ArrayList<Integer> keyWord)
    {
        KeyWord = keyWord;
    }

    public ArrayList<Integer> getKeyWord()
    {
        return KeyWord;
    }

    public void setIsKey(boolean isKey)
    {
        this.isKey = isKey;
    }

    public boolean isKey()
    {
        return isKey;
    }

    public ETALine getLine(int i)
    {
        return this.lines.get(i);
    }

    public void setCommentFound(boolean commentFound)
    {
        this.commentFound = commentFound;
    }

    public boolean isCommentFound()
    {
        return commentFound;
    }

    public void setCommentStartLoc(int commentStartLoc)
    {
        this.commentStartLoc = commentStartLoc;
    }

    public int getCommentStartLoc()
    {
        return commentStartLoc;
    }

    private void setCurrentLine(ETALine currentLine)
    {
        this.currentLine = currentLine;
    }

    public ETALine getCurrentLine()
    {
        return currentLine;
    }
}
