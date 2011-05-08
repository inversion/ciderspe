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

package cider.common.network.bot;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
* Writes data to disk on the bot machine
*/
public class CommitTimer
{
    private Timer timer = new Timer();
    private Bot bot;

    TimerTask commitToDisk = new TimerTask()
    {
        @Override
        public void run()
        {
            if (!bot.isDebugbot())
                bot.writeUpdatedDocs();

            bot.writeUpdatedProfiles();
            try
            {
                bot.writeChatHistory();
            }
            catch (IOException e)
            {

                e.printStackTrace();
            }
        }
    };

    public CommitTimer(Bot bot)
    {
        this.bot = bot;
        timer.scheduleAtFixedRate(commitToDisk, 60000, 60000);
    }
    
    protected void stopTimer()
    {
        timer.cancel();
    }

}
