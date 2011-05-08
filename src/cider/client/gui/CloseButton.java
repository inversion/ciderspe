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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
* Deals with the buttons used on editor pane and chat tabs
*/
@SuppressWarnings("serial")
public class CloseButton extends JButton
{

    private boolean rollover = false;
    private boolean pressed = false;

    public CloseButton(String name, boolean enabled)
    {
        super.setActionCommand(name);
        super.setEnabled(enabled);

        super.setPreferredSize(new Dimension(15, 15));
        super.setToolTipText("Close Tab");
        super.setFocusable(false);
        super.setBorderPainted(false);

        super.addChangeListener(new ChangeListener()
        {

            @Override
            public void stateChanged(ChangeEvent e)
            {

                ButtonModel model = getModel();

                if (model.isRollover() && !rollover)
                {
                    rollover = true;
                }
                else if (rollover && !model.isRollover())
                {
                    rollover = false;
                }

                if (model.isPressed() && !pressed)
                {
                    pressed = true;
                }
                else if (pressed && !model.isPressed())
                {
                    pressed = false;
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g)
    {

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw disabled button
        if (!super.isEnabled())
        {
            g2d.setColor(Color.DARK_GRAY);
            g2d.drawLine(4, 4, 10, 10);
            g2d.drawLine(4, 10, 10, 4);
        }
        else
        {
            g2d.setColor(Color.DARK_GRAY);

            if (pressed)
            {
                g2d.setColor(new Color(150, 0, 0));
                g2d.fillOval(0, 0, 15, 15);
                g2d.setColor(Color.WHITE);
            }
            else if (rollover)
            {
                g2d.setColor(new Color(220, 75, 75));
                g2d.fillOval(0, 0, 15, 15);
                g2d.setColor(Color.WHITE);
            }

            g2d.drawLine(4, 4, 10, 10);
            g2d.drawLine(4, 10, 10, 4);

        }
    }
}
