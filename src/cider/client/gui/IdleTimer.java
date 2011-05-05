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

public class IdleTimer
{
    private int idleTime = 0;
    public boolean isIdle = false;
    private Timer timer = new Timer();
    private Client client;
    private int totalIdleTime = 0;
    
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
    
    public IdleTimer(Client client, boolean enabled)
    {
        idleTime = 0;
        this.client = client;
        if(enabled)
            timer.scheduleAtFixedRate( increment, 0, 1000 );
    }
    
    public void stop()
    {
        this.timer.cancel();
        this.timer.purge();
    }
    
    protected int getTotalIdleTime()
    {
        return totalIdleTime;
    }
	
    public void mouseMoved()
    {
	    totalIdleTime += idleTime;
	    idleTime = 0;
	    if( isIdle )
	    {
	        client.sendHerePresence();
	        System.out.println("IdleTimer: Back...");
	        isIdle = false;
	    }
    }

}
