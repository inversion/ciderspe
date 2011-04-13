package cider.client.gui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.Timer;
import java.util.TimerTask;

import cider.common.network.client.Client;

/**
 * Idle timer for MainWindow GUI. 
 * If user doesn't move the mouse in the window for 5 minutes it sends an idle presence.
 * 
 * @author Andrew
 *
 */

public class IdleTimer implements MouseMotionListener
{
    private int idleTime = 0;
    public boolean isIdle = false;
    private Timer timer = new Timer();
    private Client client;
    
    TimerTask increment = new TimerTask()
    {
        @Override
        public void run()
        {
            idleTime++;
            if( idleTime == 10 )
            {
                isIdle = true;
                client.sendIdlePresence();
                System.out.println("IdleTimer: Idle...");
            }
                
        }
    };
    
    public IdleTimer(Client client)
    {
        idleTime = 0;
        this.client = client;
        timer.scheduleAtFixedRate( increment, 0, 1000 );
    }
    
    @Override
    public void mouseDragged(MouseEvent arg0)
    {
    }

    @Override
    public void mouseMoved(MouseEvent arg0)
    {
        idleTime = 0;
        if( isIdle )
        {
            client.sendHerePresence();
            System.out.println("IdleTimer: Back...");
            isIdle = false;
        }
    }

}
