package cider.common.network.bot;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class CommitTimer
{
    private Timer timer = new Timer();
    private Bot bot;
    
    public CommitTimer( Bot bot )
    {
        this.bot = bot;
        timer.scheduleAtFixedRate( commitToDisk, 60000, 60000 );
    }
    
    protected void stopTimer()
    {
        timer.cancel();
    }
    
    TimerTask commitToDisk = new TimerTask()
    {
        @Override
        public void run()
        {
            if(!bot.isDebugbot())
                bot.writeUpdatedDocs();

            bot.writeUpdatedProfiles();
            try
            {
                bot.writeChatHistory();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    };

}
