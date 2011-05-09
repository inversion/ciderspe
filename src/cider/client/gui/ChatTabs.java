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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.JTabbedPane;
import javax.swing.Timer;

/**
 * Extension of JTabbedPane to include flashing functionality
 * 
 * @author Alex, Andrew
 * 
 */

@SuppressWarnings("serial")
public class ChatTabs extends JTabbedPane
{
    public class TabFlashListener implements ActionListener
    {
        private Color background;
        private Color foreground;
        private Color oldBackground;
        private Color oldForeground;
        private int requiredTabIndex;
        private boolean flashon = false;
        private Timer timer = new Timer(700, this);

        public TabFlashListener(ChatTabs receiveTabs, int requiredTabIndex)
        {
            this.requiredTabIndex = requiredTabIndex;
            oldForeground = receiveTabs.getForeground();
            oldBackground = receiveTabs.getBackground();
            foreground = Color.BLACK;
            background = Color.ORANGE;
        }

        public void actionPerformed(ActionEvent e)
        {
            flash(flashon);
            flashon = !flashon;
        }

        public void flash(boolean flashon)
        {
            if (flashon)
            {
                if (foreground != null)
                {
                    setForegroundAt(requiredTabIndex, foreground);
                }
                if (background != null)
                {
                    setBackgroundAt(requiredTabIndex, background);
                }
            }
            else
            {
                if (oldForeground != null)
                {
                    setForegroundAt(requiredTabIndex, oldForeground);
                }
                if (oldBackground != null)
                {
                    setBackgroundAt(requiredTabIndex, oldBackground);
                }
            }
            repaint();
        }

        public void start()
        {
            timer.start();
        }

        public void stopflash()
        {
            timer.stop();
            setForegroundAt(requiredTabIndex, oldForeground);
            setBackgroundAt(requiredTabIndex, oldBackground);
        }
    }

    private HashMap<String, TabFlashListener> usersToTabs = new HashMap<String, TabFlashListener>();

    /**
     * Removes a tab from the editor pane
     * @param name Name of the tab to remove
     */
    public void removeTab(String name)
    {
        if (!usersToTabs.containsKey(name))
            return;
        tabflashstop(name);
        this.remove(this.indexOfTab(name));
        this.repaint();
        this.updateUI();
        usersToTabs.remove(name);
    }

    /**
     * Makes a tab flash
     * @param name Name of the tab to flash
     */
    public void tabflash(String name)
    {
        TabFlashListener tabflash;
        // Don't flash the tab if it's the currently selected one
        if (this.getSelectedIndex() == this.indexOfTab(name))
            return;

        if (!usersToTabs.containsKey(name))
        {
            tabflash = new TabFlashListener(this, this.indexOfTab(name));
            usersToTabs.put(name, tabflash);
        }
        else
            tabflash = usersToTabs.get(name);

        tabflash.start();
    }

    /**
     * Makes a tab stop flashing
     * @param name Name of the tab to stop
     */
    public void tabflashstop(String name)
    {
        if (usersToTabs.containsKey(name))
            usersToTabs.get(name).stopflash();
    }
}
