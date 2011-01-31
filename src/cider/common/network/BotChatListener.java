package cider.common.network;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smackx.muc.MultiUserChat;

/**
 * 
 * Listens for new incoming chat sessions to the bot.
 * 
 * @author Andrew
 *
 */

public class BotChatListener implements ChatManagerListener {
	
	public static final boolean DEBUG = true;
	
	private MultiUserChat chatroom;
	
	BotChatListener( MultiUserChat chatroom )
	{
		this.chatroom = chatroom;
	}

    @Override
    public void chatCreated(Chat chat, boolean createdLocally)
    {
        if( DEBUG )
            System.out.println(chat.getParticipant() + " initiated chat...");

        chat.addMessageListener( new BotMessageListener( ) );
        
        // Invite this user to the chatroom
        System.out.println( "Inviting user " + chat.getParticipant() );
        chatroom.invite( chat.getParticipant(), " " );
    }

}
