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

package cider.specialcomponents.editorTypingArea;

import java.awt.Color;
import java.awt.Graphics;
import java.util.LinkedList;

import cider.client.gui.ETASourceEditorPanel;
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
    public TypingEventList str;
    public int y;
    public int lineNum;
    public int start;
    Color[] colors;
    SourceDocumentViewer sdv;

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
        this.str = tel;
        this.colors = new Color[this.str.length()];
    }

    /*
     * Syntax Highlighting
     */

    public void characterColors()
    {
        LinkedList<TypingEventList> words = this.str.splitWords(new String[] {
                " ", "(", ")", ";", "\t", ":", "#", "{", "}" });
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
                if ((this.sdv.isCommentFound() == false)
                        || ((this.lineNum < this.sdv.getCommentStartLoc())
                                && (this.sdv.getCommentStartLoc() != -1) && (this.sdv
                                .isCommentFound() == true)))
                {
                    if (str.startsWith("/*") == true)
                    {
                        this.sdv.setCommentFound(true);
                        this.sdv.setCommentStartLoc(this.lineNum);
                        wash(this.colors, Color.RED, i, i + length);
                    }
                    if (ETASourceEditorPanel.keywords.contains(str))
                    {
                        wash(this.colors, Color.BLUE, i, i + length);
                        this.sdv.getKeyWord().add(i);
                        this.sdv.getKeyWord().add(i + length);
                    }
                    if (isParsableToNum(str) == true)
                        customColor = new Color(0, 100, 0);
                    wash(this.colors, customColor, i, i + length);
                    if (str.startsWith("//") == true)
                    {
                        wash(this.colors, Color.RED, i, i + length);
                        this.sdv.setCommentedLine(true);
                    }
                    if (this.sdv.isCommentedLine() == true)
                        wash(this.colors, Color.RED, i, i + length);
                }
                else
                {
                    wash(this.colors, Color.RED, i, i + length);
                    if (str.endsWith("*/") == true)
                        this.sdv.setCommentFound(false);
                }
            if (this.sdv.Highlighting == 1)
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
        g.setColor(Client.colours.get(EditorTypingArea.parent.getUsername()));
        g.fillRoundRect(3, this.y - EditorTypingArea.lineSpacing + 2,
                EditorTypingArea.leftMargin - 8,
                EditorTypingArea.lineSpacing + 2, 3, 3); // TODO:
        // here
        // Alex
        g.setColor(Color.LIGHT_GRAY);
        g.drawRoundRect(3, this.y - EditorTypingArea.lineSpacing + 2,
                EditorTypingArea.leftMargin - 8,
                EditorTypingArea.lineSpacing + 2, 3, 3);
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
            nfe.printStackTrace();
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
     * paints a character of this line, loop through i to draw the whole string
     * 
     * @param g
     * @param i
     *            the character number of this line to be painted
     */
    public void paintCharacter(Graphics g, int i)
    {
        int x = (i * EditorTypingArea.characterSpacing)
                + EditorTypingArea.leftMargin;
        int y = this.y;

        this.sdv.setIsKey(false);

        g.setColor(this.colors[i] != null ? this.colors[i] : Color.WHITE);

        for (int j = 0; j < this.sdv.getKeyWord().size(); j = j + 2)
        {
            if ((i >= this.sdv.getKeyWord().get(j))
                    && (i <= this.sdv.getKeyWord().get(j + 1)))
                this.sdv.setIsKey(true);
        }
        if (this.sdv.isKey() == true)
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
     * draws a coloured box on this line (should be called before paint(g, i)) -
     * one use of this method is to indicate that this character is locked.
     * Different colours could represent different users
     * 
     * @param g
     * @param i
     * @param c
     */
    public void highlight(Graphics g, int i, Color c)
    {
        g.setColor(c);
        int x = (i * EditorTypingArea.characterSpacing)
                + EditorTypingArea.leftMargin;
        int y = this.y + 5;
        g.fillRect(x, y - EditorTypingArea.lineSpacing,
                EditorTypingArea.characterSpacing, EditorTypingArea.lineSpacing); // TODO
        // here
        // Alex
    }

    /**
     * tests whether this character is part of a locked region, which is one way
     * of testing if it should be highlighted
     * 
     * @param i
     * @return
     */
    public String locked(int i)
    {
        return this.str.get(i).lockingGroup;
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
            uname = sdv.getLine(LineNo - 1).str.get(i).owner;
            usercolor = Client.colours.get(uname);
            target[i] = usercolor;
        }
    }

    public boolean selected(int i)
    {
        TypingRegion tr = this.sdv.getSelectedRegion();
        return tr != null && tr.list.contains(this.str.get(i));
    }
}