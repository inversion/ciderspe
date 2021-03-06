/**
 *  CIDER - Collaborative Integrated Development EnviRonment
    Copyright (C) 2011  Andrew Moss
                        Lawrence Watkiss
                        Jonathan Bannister
                        Alex Sheppard
                        Miles Taylor
                        Ashley Woodman

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package cider.documentViewerComponents;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.Timer;

import javax.swing.JPanel;

import cider.client.gui.MainWindow;
import cider.common.network.client.Client;
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
 * @author Lawrence, Miles, Alex, Andrew
 * @param fontSize
 *            the size of the font in the editortypingarea (14 default)
 */
public class SourceDocumentViewer extends JPanel implements MouseListener,
        MouseMotionListener
{
    private static final long serialVersionUID = 1L;

    /**
     * 
     * @param p
     * @author Alex
     */
    public static void addParent(Client p)
    {
        parent = p;
    }

    /**
     * 
     * @param color
     * @return the same color but with reduced alpha, or null if a null if the
     *         input color was null
     */
    private static Color glass(Color color)
    {
        return color == null ? null : new Color(color.getRed(), color
                .getGreen(), color.getBlue(), color.getAlpha() / 3);
    }

    private TypingEventList str = new TypingEventList();
    private int caretPosition = 0;
    // private ICodeLocation codeLocation = null;
    protected SourceDocument doc = null;
    private boolean caretFlashing = true;
    private boolean caretVisible = false;
    private ArrayList<SDVLine> lines = new ArrayList<SDVLine>();
    private ArrayList<ActionListener> als = new ArrayList<ActionListener>();
    private ArrayList<Integer> KeyWord = new ArrayList<Integer>();
    private SDVLine currentLine = null;
    private int currentColNum = 0;
    public static final int LINE_LOCKED = 0;
    public static final int LINE_UNLOCKED = 1;
    public static int fontSize = 14;
    public static final Font fontbold = new Font("Monospaced", Font.BOLD,
            fontSize);
    public static final Font font = new Font("Monospaced", Font.PLAIN, fontSize);
    public static final int leftMargin = fontSize * 2;
    public static final int lineSpacing = fontSize + 1;
    public static final int characterSpacing = fontSize / 2 + 1;
    public static final Color selectedRegionColor = new Color(255, 255, 255,
            122);
    private Color defaultColor = Color.WHITE;
    private boolean waiting = true;
    private boolean commentFound = false;
    private boolean commentedLine = false;
    private boolean isKey = false;
    private int commentStartLoc = -1;
    private TypingRegion selectedRegion = null;
    private CaretVisibilityToggler toggleCaretVisibility;

    private Timer caretTimer;
    private long flashDelay = 500;

    static Client parent;

    public static int Highlighting;

    // public static void main( String[] args )
    // {
    // SourceDocument doc = new SourceDocument( "testdoc" );
    // SourceDocumentViewer sdv = new SourceDocumentViewer( doc );
    // // TODO: Complete this testing for cut, paste, etc.
    // }
    //    
    /**
     * 
     * @param owner
     * @param codeLocation
     */
    public SourceDocumentViewer(SourceDocument doc)
    {
        this.doc = doc;
        str = this.doc.playOutEvents(Long.MAX_VALUE);
    }

    public void addActionListener(ActionListener al)
    {
        als.add(al);
    }

    /**
     * 
     * @param position
     * @return the nearest valid position
     */
    private int constrain(int position)
    {
        if (position > str.length())
            return str.length() - 1;
        if (position < 0)
            return 0;
        else
            return position;
    }

    /**
     * Copy current selection to the clipboard
     * 
     * @author Andrew
     */
    protected void copy()
    {
        if (selectedRegion.getLength() == 0)
            return;

        StringBuffer sb = new StringBuffer();
        for (TypingEvent te : selectedRegion.list)
            sb.append(te.text);

        Clipboard clipboard = getToolkit().getSystemClipboard();
        StringSelection stringSelection = new StringSelection(sb.toString());
        clipboard.setContents(stringSelection, stringSelection);
    }

    /**
     * used to find out if the position which the caret is currently at is part
     * of a locked out region. Used to find out whether the doc should accept a
     * particular event when it is being typed out by the user.
     * 
     * @param offset
     * @return
     */
    protected int currentPositionLocked(int offset, String user)
    {
        int pos = this.getCaretPosition() - 1 + offset;
        return pos >= 0 && pos < str.length() ? str.locked(pos, user) : 0;
    }

    /**
     * the caret indicates which part of the document text typing events will be
     * applied to. This method returns the position of the caret starting from
     * the very beginning of the document in the "friendly" format.
     * 
     * @return The caret position (NB: 0 is before event 0, 1 is before event 1,
     *         etc.)
     */
    protected int getCaretPosition()
    {
        return caretPosition;
    }

    /**
     * 
     * @return
     * @author Miles or Alex
     */
    public int getCommentStartLoc()
    {
        return commentStartLoc;
    }

    /**
     * 
     * @return the line on which the caret currently rests
     * @author Lawrence
     */
    protected SDVLine getCurrentLine()
    {
        return currentLine;
    }

    protected int getCurrentLineNumber()
    {
        try
        {
            return this.getCurrentLine().getLineNumber();
        }
        catch (NumberFormatException nfe)
        {
            nfe.printStackTrace();
            return 0;
        }
    }

    // FIXME: This is pointless?
    protected int GetCurxLen()
    {
        return 0;
        // below doesn't work
        // return (i * characterSpacing) + leftMargin;
    }

    /**
     * 
     * @return color of text that hasn't been re-colored by syntax highlighting
     */
    public Color getDefaultColor()
    {
        return defaultColor;
    }

    /**
     * 
     * @return
     * @author Miles or Alex
     */
    public ArrayList<Integer> getKeyWord()
    {
        return KeyWord;
    }

    /**
     * 
     * @param lineNumber
     * @return a line of text
     */
    public SDVLine getLine(int lineNumber)
    {
        try
        {
            return lines.get(lineNumber);
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            return null;
        }
    }

    /**
     * 
     * @return a TypingRegion that represents the currently selected region of
     *         text
     */
    protected TypingRegion getSelectedRegion()
    {
        return selectedRegion;
    }

    public SourceDocument getSourceDocument()
    {
        return doc;
    }

    public TypingEventList getTypingEventList()
    {
        return str;
    }

    /**
     * hold off toggling the caret visibility for the next timer event
     * 
     * @param visible
     */
    protected void holdCaretVisibility(boolean visible)
    {
        caretVisible = visible;
        toggleCaretVisibility.skipNextToggle();
    }

    /**
     * 
     * @return
     * @author Miles or Alex
     */
    public boolean isCommentedLine()
    {
        return commentedLine;
    }

    /**
     * 
     * @return Miles or Alex
     */
    public boolean isCommentFound()
    {
        return commentFound;
    }

    public boolean isEmpty()
    {
        return this.getCurrentLine() == null;
    }

    /**
     * 
     * @return
     * @author Miles or Alex
     */
    public boolean isKey()
    {
        return isKey;
    }

    @Override
    public void mouseClicked(MouseEvent arg0)
    {
        if (selectedRegion != null)
        {
            selectedRegion = null;
            this.updateUI();
        }
    }

    @Override
    public void mouseDragged(MouseEvent me)
    {
        try
        {
            int col = xToColumnNumber(me.getX());
            int ln = yToLineNumber(me.getY());

            if (lines.size() > 0)
            {
                if (ln < 1)
                    ln = 0;
                if (ln > lines.size())
                    ln = lines.size();

                SDVLine line = this.getLine(ln - 1);

                // Bail out if the line doesn't exist
                if (line == null)
                    return;

                if (col > line.str.length())
                    col = line.str.length();
                int end = line.getPositionAtStart() + col - 1;
                end = this.constrain(end);

                int start = this.getCaretPosition();

                if (start > end)
                    selectedRegion = str.region(end + 1, start);
                else
                    selectedRegion = str.region(start, end + 1);
                this.updateUI();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
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
    public void mouseMoved(MouseEvent me)
    {

    }

    @Override
    /**
     * mouse presses are currently used to lock out lines of text
     */
    public void mousePressed(MouseEvent me)
    {
        int ln = yToLineNumber(me.getY());
        if (ln > lines.size())
            ln = lines.size();

        if (lines.size() > 0)
        {
            SDVLine line = lines.get(ln - 1);
            int start = line.getPositionAtStart();
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
                            + parent.getClockOffset(), tem, start - 1, length,
                            "", parent.getUsername(), null);

                    for (ActionListener al : als)
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
                caretPosition = start + colNumber;
            }
        }

        this.updateUI();
    }

    @Override
    public void mouseReleased(MouseEvent arg0)
    {

    }

    /**
     * moves the caret from its current position by a number of spaces
     * 
     * @param spaces
     */
    protected void moveCaret(int spaces)
    {
        caretPosition += spaces;
        caretPosition = constrain(this.getCaretPosition());
        selectedRegion = null;
        this.holdCaretVisibility(true);
        this.updateUI();
    }

    /**
     * Move the caret to the end of the file and update the UI.
     * 
     * @param select
     *            If true, transform selected area to include that traversed.
     * 
     */
    protected void moveDocEnd(boolean select)
    {
        // int numLines = this.lines.size();
        // SDVLine line = this.lines.get(numLines - 1);
        // int startLastLine = line.start + numLines - 2;
        // int length = line.str.length();
        // this.caretPosition = startLastLine + length;
        setCaretPosition(str.length());
        selectedRegion = null;
        this.updateUI();
    }

    /**
     * Move the caret to the start of the file and update the UI.
     * 
     * @param select
     *            If true, transform selected area to include that traversed.
     * 
     */
    protected void moveDocHome(boolean select)
    {
        setCaretPosition(0);
        selectedRegion = null;
        this.updateUI();
    }

    /**
     * Moves the caret down a line
     */
    protected void moveDown(boolean select)
    {
        if (currentColNum == 0)
            this.moveRight(select);
        else
        {
            int lineNum = this.getCurrentLineNumber();
            if (lineNum >= lines.size())
                return;

            SDVLine line = lines.get(lineNum);
            int start = line.getPositionAtStart();
            int length = line.str.length();
            if (currentColNum > length)
                currentColNum = length;

            if (select)
            {
                if (selectedRegion == null)
                    selectedRegion = str.region(getCaretPosition(), start
                            + currentColNum);
                else if (start + currentColNum > selectedRegion.start)
                    selectedRegion = str.region(selectedRegion.end, start
                            + currentColNum);
                else
                    selectedRegion = str.region(start + currentColNum,
                            selectedRegion.start);
            }

            setCaretPosition(start + currentColNum);
            this.holdCaretVisibility(true);
            this.updateUI();
        }
    }

    /**
     * Move the caret to the end of the current line and update the UI.
     * 
     * @param select
     *            True if selecting the area traversed.
     * @author Andrew, Lawrence
     */
    protected void moveEnd(boolean select)
    {
        int start = currentLine.getPositionAtStart();
        int length = this.getCurrentLine().str.length();

        if (select)
        {
            System.out.println("Selecting from " + getCaretPosition() + " to "
                    + (start + length));
            selectedRegion = str.region(this.getCaretPosition(),
                    (start + length));
        }

        caretPosition = start + length;

        this.updateUI();
    }

    /**
     * Move the caret to the beginning of the current line and update the UI.
     * 
     * @param select
     *            True if selecting the area traversed.
     * @author Andrew, Lawrence
     */
    protected void moveHome(boolean select)
    {
        int start = currentLine.getPositionAtStart();
        if (select)
        {
            System.out.println("Selecting from " + (start) + " to "
                    + getCaretPosition());
            selectedRegion = str.region(start, this.getCaretPosition() - 1);
        }

        caretPosition = start;

        this.updateUI();
    }

    /**
     * moves the caret left one position and updates the UI
     */
    protected void moveLeft(boolean select)
    {
        if (getCaretPosition() == 0)
            return;

        if (select)
        {
            if (selectedRegion == null)
                selectedRegion = str.region(getCaretPosition() - 1,
                        getCaretPosition());
            else if (getCaretPosition() == selectedRegion.start)
                selectedRegion = str.region(selectedRegion.start - 1,
                        selectedRegion.end);
            else
                selectedRegion = str.region(selectedRegion.start,
                        selectedRegion.end - 1);
        }

        caretPosition--;

        this.holdCaretVisibility(true);
        // this.caretFlashing = fa
        this.updateUI();
    }

    /**
     * Move the caret down a page and update the UI.
     * 
     * @param select
     *            If true, transform selected area to include that traversed.
     * 
     * @author Andrew
     */
    protected void movePageDown(boolean select)
    {
        this.holdCaretVisibility(true);

        // Move down 26 lines
        int lineNum = getCurrentLineNumber() + 26;

        // If trying to move below the bottom of document
        if (lineNum > lines.size())
            lineNum = lines.size();

        // TODO: Unexplained minuses
        SDVLine line = lines.get(lineNum - 1);
        int start = line.getPositionAtStart();
        int lineLength = line.str.length();

        // If trying to move beyond end of line
        if (currentColNum >= lineLength)
            currentColNum = lineLength - 1;

        caretPosition = start + currentColNum - 1;
        this.updateUI();
    }

    /**
     * Move the caret up a page and update the UI.
     * 
     * @param select
     *            If true, transform selected area to include that traversed.
     * 
     * @author Andrew
     */
    protected void movePageUp(boolean select)
    {
        this.holdCaretVisibility(true);

        // Move up 26 lines
        int lineNum = getCurrentLineNumber() - 26;

        // If trying to move above top of document
        if (lineNum < 1)
            lineNum = 1;

        // TODO: Unexplained minuses
        SDVLine line = lines.get(lineNum - 1);
        int start = line.getPositionAtStart();
        int length = line.str.length();

        // If trying to move beyond end of line
        if (currentColNum >= length)
            currentColNum = length;

        caretPosition = start + currentColNum;
        this.updateUI();
    }

    /**
     * moves the caret right one position and updates the UI
     */
    protected void moveRight(boolean select)
    {
        if (getCaretPosition() == str.length())
            return;

        if (select)
        {
            if (selectedRegion == null)
                selectedRegion = str.region(getCaretPosition(),
                        getCaretPosition() + 1);
            else if (getCaretPosition() == selectedRegion.end)
                selectedRegion = str.region(selectedRegion.start,
                        selectedRegion.end + 1);
            else
                selectedRegion = str.region(selectedRegion.start + 1,
                        selectedRegion.end);
        }

        caretPosition++;

        this.holdCaretVisibility(true);
        this.updateUI();
    }

    /**
     * Move up
     * 
     * @param select
     *            Selecting or not.
     * 
     * @author Lawrence, Andrew
     */
    protected void moveUp(boolean select)
    {
        if (currentColNum == 0)
            this.moveLeft(select);
        else
        {
            int lineNum = this.getCurrentLineNumber() - 2;
            if (lineNum < 0)
                return;

            SDVLine line = lines.get(lineNum);
            int start = line.getPositionAtStart();
            int length = line.str.length();
            if (currentColNum > length)
                currentColNum = length;

            if (select)
            {
                if (selectedRegion == null)
                    selectedRegion = str.region(start + currentColNum,
                            getCaretPosition());
                else if (start + currentColNum < selectedRegion.start)
                    selectedRegion = str.region(start + currentColNum,
                            selectedRegion.end);
                else
                    selectedRegion = str.region(selectedRegion.start, start
                            + currentColNum);
            }

            setCaretPosition(start + currentColNum);

            this.holdCaretVisibility(true);
            this.updateUI();
        }
    }

    /**
     * paints a caret given the column number and yPixelCoordinate
     * 
     * @param g
     * @param columnNumber
     * @param yPixelCoordinate
     */
    @SuppressWarnings("static-access")
	private void paintCaret(Graphics g, int columnNumber, int yPixelCoordinate)
    {
        g.setColor(EditorTypingArea.parent.colours.get(EditorTypingArea.parent
                .getUsername())/* Color.BLUE */);
        int x = ((columnNumber + 1) * EditorTypingArea.characterSpacing)
                + EditorTypingArea.leftMargin;
        int y = yPixelCoordinate + 5;

        // Draw Caret for OVERWRITE and INSERT mode
        if (MainWindow.statusBar.isOverwrite() == true)
        {
            g.drawRect(x, y - EditorTypingArea.lineSpacing - 1,
                    characterSpacing, EditorTypingArea.lineSpacing);
            g.drawRect(x + 1, y - EditorTypingArea.lineSpacing,
                    characterSpacing - 2, EditorTypingArea.lineSpacing - 2);
        }
        else
        {
            g.fillRect(x, y - EditorTypingArea.lineSpacing - 1, 3,
                    EditorTypingArea.lineSpacing);
        }

        // TEMP StatusBar update
        MainWindow.statusBar.setColNo(columnNumber + 1);
        MainWindow.statusBar.setLineNo((yPixelCoordinate / lineSpacing));
    }

    @SuppressWarnings("static-access")
	@Override
    /**
     * calls paint methods on the ETALines
     */
    public void paintComponent(Graphics g2)
    {
        Graphics2D g = (Graphics2D) g2;
        super.paintComponent(g);
        int longestLine = 0;

        if (waiting)
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
            int numLines = lines.size();
            boolean caretFound = false;
            try
            {
                if (numLines == 0)
                {
                    this.paintCaret(g, -1, lineSpacing);
                }
                else
                    for (SDVLine line : lines)
                    {
                        if (longestLine < line.str.length())
                            longestLine = line.str.length();

                        line.paintMargin(g);
                        line.characterColors();
                        String owner;

                        // Paints locked and selected regions
                        for (int i = 0; i < line.str.length(); i++)
                        {
                            if ((owner = line.locked(i)) != null
                                    || (i > 1 && (owner = line.locked(i - 1)) != null))
                                line.highlight(g, i, glass(parent.colours
                                        .get(owner)));

                            if (line.selected(i))
                                line.highlight(g, i, selectedRegionColor);
                        }

                        // If the caret is placed just after a newline
                        // that line might not actually have any text in it
                        if (!caretFound && p == this.getCaretPosition() - ln)
                        {
                            currentLine = line;
                            currentColNum = 0;
                            caretFound = true;

                            if (caretVisible)
                                this.paintCaret(g, -1, line.y);
                            if (this.hasFocus())
                                line.highlightMargin(g);
                        }

                        // Paints the lines of text and the caret if it has not
                        // already been drawn at the start
                        for (int i = 0; i < line.str.length(); i++)
                        {
                            if (!caretFound)
                            {
                                if (p == this.getCaretPosition() - ln - 2)
                                    currentLine = line;
                                else if (p == this.getCaretPosition() - 1 - ln)
                                {
                                    if (caretVisible)
                                        this.paintCaret(g, i, line.y);

                                    currentColNum = i + 1;
                                    caretFound = true;
                                    if (this.hasFocus())
                                        line.highlightMargin(g);
                                }
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
                e.printStackTrace();
                // ignore for now
            }
        }
        int width = longestLine * characterSpacing + leftMargin;
        int height = lineSpacing * lines.size();
        this.setPreferredSize(new Dimension(width, height));
        this.invalidate();
    }

    /**
     * Useful for telling the user that the document is being fetched from the
     * server
     * 
     * @param g
     */
    public void paintWaitingSign(Graphics g)
    {
        g.setFont(new Font("SansSerif", Font.PLAIN, fontSize));
        g.drawString("Retrieving document from server... ", 32, 32);
    }

    /**
     * called by updateText() to split up text from doc and turn it into a list
     * of TypingEventLists
     */
    private void refreshLines()
    {
        // System.out.println(this.lines.size());
        // Profile.adjustCharCount(-this.lines.size());
        lines.clear();
        LinkedList<TypingEventList> split = str.split("\n");
        int j = 1;
        int i = 0;
        for (TypingEventList tel : split)
        {
            SDVLine line = new SDVLine(tel, j * lineSpacing, j++, i, this);
            lines.add(line);
            i += line.str.length();
            // System.out.println(line.str.length());
            // Profile.adjustCharCount(line.str.length());
        }
    }

    public void removeActionListeners()
    {
        als.clear();
    }

    /**
     * Select all of the text in the document
     * 
     * @author Andrew
     */
    protected void selectAll()
    {
        moveDocEnd(false);
        selectedRegion = str.region(0, str.length());
        this.updateUI();
    }

    /**
     * sets the caret position amongst the text
     * 
     * @param position
     *            is the number of characters into the document that you want
     *            the caret to be located
     */
    protected void setCaretPosition(int position)
    {
        if (position == 0)
            caretPosition = 0;
        else
            caretPosition = constrain(position);
        this.updateUI();
    }

    /**
     * 
     * @param commentedLine
     * @author Miles or Alex
     */
    public void setCommentedLine(boolean commentedLine)
    {
        this.commentedLine = commentedLine;
    }

    /**
     * 
     * @param commentFound
     * @author Miles or Alex
     */
    public void setCommentFound(boolean commentFound)
    {
        this.commentFound = commentFound;
    }

    /**
     * 
     * @param commentStartLoc
     * @author Miles or Alex
     */
    public void setCommentStartLoc(int commentStartLoc)
    {
        this.commentStartLoc = commentStartLoc;
    }

    /**
     * Sets the default text color (text that hasn't been re-colored by syntax
     * highlighting)
     * 
     * @param defaultColor
     */
    public void setDefaultColor(Color defaultColor)
    {
        this.defaultColor = defaultColor;
    }

    /**
     * 
     * @param isKey
     * @author Miles or Alex
     */
    public void setIsKey(boolean isKey)
    {
        this.isKey = isKey;
    }

    /**
     * 
     * @param keyWord
     * @author Miles or Alex
     */
    public void setKeyWord(ArrayList<Integer> keyWord)
    {
        KeyWord = keyWord;
    }

    /**
     * The caret flashes to help draw attention to its location and indicate
     * that this component is in focus. This method schedules a timer task to
     * toggle between the caret being visible or invisible. Should only need to
     * be called once.
     */
    protected void setupCaretFlashing()
    {
        caretTimer = new Timer();
        toggleCaretVisibility = new CaretVisibilityToggler(
                (EditorTypingArea) this);

        caretTimer.scheduleAtFixedRate(toggleCaretVisibility, 0, flashDelay);

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
                caretVisible = false;
                repaint();
            }

        });
    }

    /**
     * Waiting sign will disapear if you set waiting to false
     * 
     * @param waiting
     */
    public void setWaiting(boolean waiting)
    {
        this.waiting = waiting;
    }

    /**
     * toggles the caret visibility
     */
    protected void toggleCaretVisibility()
    {
        caretVisible = this.hasFocus() && caretFlashing && !caretVisible;
        if (caretFlashing)
            updateUI();
    }

    /**
     * Frequently called whenever it is time for the text to be updated. Takes
     * from the code location recent events that need to be pushed to doc and
     * then refreshes the lines and updates the UI.
     */
    public void updateText()
    {
        this.updateText(Long.MAX_VALUE);
    }

    /**
     * Frequently called whenever it is time for the text to be updated. Takes
     * from the code location recent events that need to be pushed to doc and
     * then refreshes the lines and updates the UI.
     */
    public void updateText(long endTime)
    {
        if (doc != null)
        {
            str = doc.playOutEvents(endTime);
            this.refreshLines();
            this.updateUI();
        }
    }

    /**
     * 
     * @param x
     *            coordinate
     * @return column number
     */
    protected int xToColumnNumber(int x)
    {
        int col = (int) ((x - leftMargin + characterSpacing / 2) / (double) characterSpacing);
        return col;
    }

    /**
     * converts the number of pixels from the top of the document to a line
     * number, useful for picking out a particular line for selection or locking
     * 
     * @param y
     *            pixels
     * @return
     */
    protected int yToLineNumber(int y)
    {
        int line = (int) (y / (double) lineSpacing) + 1;
        return line;
    }
}
