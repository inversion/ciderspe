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

import java.util.Timer;
import java.util.TimerTask;

import cider.common.network.client.Client;

/**
 * Idle timer for MainWindow GUI. If user doesn't move the mouse in the window
 * for 5 minutes it sends an idle presence.
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
            if (idleTime == 10)
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
        if (enabled)
            timer.scheduleAtFixedRate(increment, 0, 1000);
    }

    public void activityDetected()
    {
        if (isIdle)
        {
            client.sendHerePresence();
            System.out.println("IdleTimer: Back...");
            isIdle = false;
            totalIdleTime += idleTime;
            idleTime = 0;
        }
    }

    protected int getTotalIdleTime()
    {
        return totalIdleTime;
    }
    
    public void resetTotal()
    {
        totalIdleTime = 0;
    }

    public void stop()
    {
        timer.cancel();
        timer.purge();
    }

    public boolean userIsIdle()
    {
        if (isIdle)
            return true;
        else
            return false;
    }

}
