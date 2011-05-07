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
    private HashMap<String,TabFlashListener> usersToTabs = new HashMap<String,TabFlashListener>();
    
    public void tabflash( String name )
    {
        // Don't flash the tab if it's the currently selected one
        if( this.getSelectedIndex() == this.indexOfTab( name ) )
            return;
               
        TabFlashListener tabflash = new TabFlashListener( this, this.indexOfTab( name ) );
        usersToTabs.put( name, tabflash );
        tabflash.start();
    }

    public void removeTab( String name )
    {
        if( !usersToTabs.containsKey( name ) )
            return;
        tabflashstop( name );
        this.remove( this.indexOfTab( name ) );
        this.repaint();
        this.updateUI();
        usersToTabs.remove( name );
    }
    
    public void tabflashstop( String name )
    {
        if( usersToTabs.containsKey( name ) )
            usersToTabs.get( name ).stopflash();
    }

    public class TabFlashListener implements ActionListener
    {
        private Color background;
        private Color foreground;
        private Color oldBackground;
        private Color oldForeground;
        private int requiredTabIndex;
        private boolean flashon = false;
        private Timer timer = new Timer(700, this);

        public TabFlashListener( ChatTabs receiveTabs, int requiredTabIndex )
        {
            this.requiredTabIndex = requiredTabIndex;
            this.oldForeground = receiveTabs.getForeground();
            this.oldBackground = receiveTabs.getBackground();
            this.foreground = Color.BLACK;
            this.background = Color.ORANGE;
        }

        public void start()
        {
            timer.start();
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

        public void stopflash()
        {
            timer.stop();
            setForegroundAt(requiredTabIndex, oldForeground);
            setBackgroundAt(requiredTabIndex, oldBackground);
        }
    }
}
