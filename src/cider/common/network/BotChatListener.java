package cider.common.network;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;

/**
 * 
 * Listens for new incoming chat sessions to the bot.
 * 
 * @author Andrew
 *
 */

public class BotChatListener implements ChatManagerListener {
	
	public static final boolean DEBUG = true;
	
	private BotMessageListener botMessageListener;
	
	BotChatListener( Bot source )
	{
		botMessageListener = new BotMessageListener( source );
	}

    @Override
    public void chatCreated(Chat chat, boolean createdLocally)
    {
        if( DEBUG )
            System.out.println(chat.getParticipant() + " initiated chat...");

        chat.addMessageListener( botMessageListener );
    }

}
