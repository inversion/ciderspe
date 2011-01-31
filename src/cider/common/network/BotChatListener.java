package cider.common.network;

import java.util.ArrayList;

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
	private Bot source;
	private ArrayList<Chat> chats;
	
	
	BotChatListener( Bot source )
	{
		this.chatroom = source.chatroom;
		this.source = source;
		chats = new ArrayList<Chat>();
	}

    @Override
    public void chatCreated(Chat chat, boolean createdLocally)
    {
        if( DEBUG )
            System.out.println(chat.getParticipant() + " initiated chat...");

        // Add this chat to the list of chats and give it a new message listener
        chats.add( chat );
        chat.addMessageListener( new BotMessageListener( ) );
        
        // Invite this user to the chatroom
        System.out.println( "Inviting user " + chat.getParticipant() );
        chatroom.invite( chat.getParticipant(), " " );
    }

}
