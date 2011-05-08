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

package cider.client.gui;

import java.awt.BorderLayout;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
* Class containing the elements for the status bar at the bottom of the main window
*/
@SuppressWarnings("serial")
public class StatusBar extends JPanel
{

    private JLabel lblUsername;
    private JLabel lblCurrentPos;
    private JLabel lblInputMode;
    private boolean overwrite = false;
    private int lineNo = 0;
    private int colNo = 0;

    /**
     *Creates the status bar
     */
    public StatusBar()
    {
        // GridLayout grid = new GridLayout(1,3);
        // this.setLayout(grid);

        this.setLayout(new BorderLayout());

        this.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

        Box userBox = Box.createHorizontalBox();
        URL u = this.getClass().getResource("iconuser.png");
        ImageIcon image = new ImageIcon(u);
        JLabel lblImage = new JLabel(image);
        userBox.add(lblImage);
        lblUsername = new JLabel(" Logged in as: ");
        userBox.add(lblUsername);

        lblCurrentPos = new JLabel("Line: - | Col: - ");
        lblCurrentPos.setHorizontalAlignment(SwingConstants.CENTER);

        Box inputBox = Box.createHorizontalBox();
        inputBox.setAlignmentX(RIGHT_ALIGNMENT);
        lblInputMode = new JLabel("Input Mode: ");
        lblInputMode.setHorizontalAlignment(SwingConstants.RIGHT);
        inputBox.add(lblInputMode);
        URL u2 = this.getClass().getResource("iconinput.png");
        ImageIcon image2 = new ImageIcon(u2);
        JLabel lblImage2 = new JLabel(image2);
        lblImage2.setAlignmentX(RIGHT_ALIGNMENT);
        inputBox.add(lblImage2);

        this.add(userBox, BorderLayout.WEST);
        this.add(lblCurrentPos);
        this.add(inputBox, BorderLayout.EAST);
    }

    /**
     * Returns whether in overwrite mode
     * @return True if in overwrite mode
     */
    public boolean isOverwrite()
    {
        return overwrite;
    }

    /**
    * Sets the current column number text in the status bar
     * @param col the column number
     */
    public void setColNo(int col)
    {
        colNo = col;
        setCurrentPos(lineNo, colNo);
    }

    /**
     * Sets the line/column text in the status bar
     * @param line the line number
     * @param col the column number
     */
    public void setCurrentPos(int line, int col)
    {
        lblCurrentPos.setText("Line: " + line + " | Col: " + col);
    }

    /**
     * Toggles input mode
     * @param mode Insert/Overwrite
     */
    public void setInputMode(String mode)
    {

        if (mode.equals("OVERWRITE"))
            overwrite = true;
        else
            overwrite = false;
        lblInputMode.setText("Input Mode: " + mode + " ");
    }

    /**
     * Sets the current line number text in the status bar
     * @param line the line number
     */
    public void setLineNo(int line)
    {
        lineNo = line;
        setCurrentPos(lineNo, colNo);
    }

    /**
     * Sets the username text in the status bar
     * @param name the username text
     */
    public void setUsername(String name)
    {
        lblUsername.setText(" Logged in as: " + name);
    }
}
