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

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

@SuppressWarnings("serial")
public class DebugWindow extends JPanel
{
    JTextArea textArea = new JTextArea();
    JScrollPane scrollTextArea = new JScrollPane(textArea);

    public DebugWindow()
    {
        this.setLayout(new BorderLayout());
        textArea.setEditable(false);
        // this.scrolltxt.setBounds(3, 3, 300, 200);
        this.add(scrollTextArea);
    }

    public String getText()
    {
        return textArea.getText();
    }

    public void println(String text)
    {
        textArea.setText(textArea.getText() + text + "\n");
    }

    public void setText(String text)
    {
        textArea.setText(text);
    }
}
