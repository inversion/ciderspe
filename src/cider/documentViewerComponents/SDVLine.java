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
import java.awt.Graphics;
import java.util.LinkedList;
import java.util.regex.Pattern;

import cider.client.gui.ETASourceEditorPane;
import cider.common.network.client.Client;
import cider.common.processes.TypingEventList;

/**
 * Represents a line of text on the screen. Lists of these objects are stored
 * and their paint methods called whenever it is time to update the graphics
 * 
 * @author Lawrence, Miles and Alex
 * 
 */
public class SDVLine
{
    /**
     * convenience method to find out whether a string is representing a double
     * 
     * @param string
     * @return
     * @author Lawrence
     */
    public static boolean isDouble(String string)
    {
        return doublePattern.matcher(string).matches();
    }

    /**
     * Replaces in an area of a color array
     * 
     * @param target
     *            color array
     * @param color
     *            to paint
     * @param start
     *            index
     * @param end
     *            index
     * @author Lawrence
     */
    public static void wash(Color[] target, Color color, int start, int end)
    {
        start = start < 0 ? 0 : start;
        end = end < target.length ? end : target.length;
        for (int i = start; i < end; i++)
            target[i] = color;
    }

    public TypingEventList str;
    public int y;
    public int lineNum;
    private int start;
    Color[] colors;

    SourceDocumentViewer sdv;

    private static Pattern doublePattern = Pattern.compile("-?\\d+(\\.\\d*)?");

    /**
     * 
     * @param tel
     *            the list of events in order that they should be printed on the
     *            screen from lift to right
     * @param y
     *            the number of pixels down the screen
     * @param lineNum
     *            the line number
     * @param start
     *            the caret position of the first character of this line
     * @param editorTypingArea
     */
    public SDVLine(TypingEventList tel, int y, int lineNum, int start,
            SourceDocumentViewer sdv)
    {
        this.sdv = sdv;
        this.start = start;
        this.lineNum = lineNum;
        this.y = y;
        str = tel;
        colors = new Color[str.length()];
    }

    /**
     * Syntax highlighting - fills in the colors array. Null entries will be
     * coloured default.
     */
    public void characterColors()
    {
        LinkedList<TypingEventList> words = str.splitWords(new String[] { " ",
                "(", ")", ";", "\t", ":", "#", "{", "}" });
        int i = 0;
        String str;
        int length;
        Color customColor = null;
        for (TypingEventList word : words)
        {
            str = word.toString();
            str = str.toLowerCase();
            length = str.length();
            if (EditorTypingArea.Highlighting == 0)
                if ((sdv.isCommentFound() == false)
                        || ((lineNum < sdv.getCommentStartLoc())
                                && (sdv.getCommentStartLoc() != -1) && (sdv
                                .isCommentFound() == true)))
                {
                    if (str.startsWith("/*") == true)
                    {
                        sdv.setCommentFound(true);
                        sdv.setCommentStartLoc(lineNum);
                        wash(colors, Color.RED, i, i + length);
                    }
                    if (ETASourceEditorPane.keywords.contains(str))
                    {
                        wash(colors, Color.BLUE, i, i + length);
                        sdv.getKeyWord().add(i);
                        sdv.getKeyWord().add(i + length);
                    }
                    if (isDouble(str))
                        customColor = new Color(0, 100, 0);
                    wash(colors, customColor, i, i + length);
                    if (str.startsWith("//") == true)
                    {
                        wash(colors, Color.RED, i, i + length);
                        sdv.setCommentedLine(true);
                    }
                    if (sdv.isCommentedLine() == true)
                        wash(colors, Color.RED, i, i + length);
                }
                else
                {
                    wash(colors, Color.RED, i, i + length);
                    if (str.endsWith("*/") == true)
                        sdv.setCommentFound(false);
                }
            if (sdv.Highlighting == 1)
            {
                userwash(colors, lineNum, i, i + length);
            }
            i += length + 1;

        }
    }

    public int getLineNumber()
    {
        return lineNum;
    }

    public int getPositionAtStart()
    {
        return start + lineNum - 1;
    }

    /**
     * draws a coloured box on this line (should be called before paint(g, i)) -
     * one use of this method is to indicate that this character is locked.
     * Different colours could represent different users
     * 
     * @param g
     * @param i
     * @param c
     * @author Lawrence
     */
    public void highlight(Graphics g, int i, Color c)
    {
        g.setColor(c);
        int x = (i * EditorTypingArea.characterSpacing)
                + EditorTypingArea.leftMargin;
        int y = this.y + 5;
        g
                .fillRect(x, y - EditorTypingArea.lineSpacing,
                        EditorTypingArea.characterSpacing,
                        EditorTypingArea.lineSpacing); // TODO
        // here
        // Alex
    }

    /**
     * Draws around the current line number that the user is currently on
     * 
     * @param g
     * @param caretVisible
     */
    public void highlightMargin(Graphics g)
    {
        g.setColor(Client.colours.get(EditorTypingArea.parent.getUsername()));
        g.fillRoundRect(3, y - EditorTypingArea.lineSpacing + 2,
                EditorTypingArea.leftMargin - 8,
                EditorTypingArea.lineSpacing + 2, 3, 3); // TODO:
        // here
        // Alex
        g.setColor(Color.LIGHT_GRAY);
        g.drawRoundRect(3, y - EditorTypingArea.lineSpacing + 2,
                EditorTypingArea.leftMargin - 8,
                EditorTypingArea.lineSpacing + 2, 3, 3);
        paintMargin(g);
    }

    /**
     * tests whether this character is part of a locked region, which is one way
     * of testing if it should be highlighted
     * 
     * @param i
     * @author Lawrence
     * @return
     */
    public String locked(int i)
    {
        return str.get(i).lockingGroup;
    }

    /**
     * paints a character of this line, loop through i to draw the whole string
     * 
     * @param g
     * @param i
     *            the character number of this line to be painted
     * @author Lawrence
     */
    public void paintCharacter(Graphics g, int i)
    {
        int x = (i * EditorTypingArea.characterSpacing)
                + EditorTypingArea.leftMargin;
        int y = this.y;

        sdv.setIsKey(false);

        g.setColor(colors[i] != null ? colors[i] : sdv.getDefaultColor());

        for (int j = 0; j < sdv.getKeyWord().size(); j = j + 2)
        {
            if ((i >= sdv.getKeyWord().get(j))
                    && (i <= sdv.getKeyWord().get(j + 1)))
                sdv.setIsKey(true);
        }
        if (sdv.isKey() == true)
        {
            g.setFont(EditorTypingArea.fontbold);
            g.drawString("" + str.get(i).text, x, y);
            g.setFont(EditorTypingArea.font);
        }
        else
        {
            g.setFont(EditorTypingArea.font);
            g.drawString("" + str.get(i).text, x, y);
        }
    }

    /**
     * paints the line number to the left of this line
     * 
     * @param g
     * @author Lawrence
     */
    public void paintMargin(Graphics g)
    {
        g.setColor(Color.GRAY);
        g.drawString("" + lineNum, 5, y);
    }

    /**
     * 
     * @param i
     * @return true is the text at i has been selected
     * @author Lawrence
     */
    public boolean selected(int i)
    {
        TypingRegion selectedText = sdv.getSelectedRegion();
        return selectedText != null && selectedText.list.contains(str.get(i));
    }

    /**
     * 
     * @param target
     * @param LineNo
     * @param start
     * @param end
     * @author Miles
     */
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
            uname = sdv.getLine(LineNo - 1).str.get(i).owner;
            usercolor = Client.colours.get(uname);
            target[i] = usercolor;
        }
    }
}