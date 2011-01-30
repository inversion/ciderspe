package cider.common.network;

import java.util.ArrayList;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;

/**
 * 
 * Listen for newly created user chats and assign them a message listener.
 * 
 * @author Andrew
 *
 */

public class ClientUserChatListener implements ChatManagerListener {
	
	private ArrayList<Chat> chats;
	private ClientUserChatMessageListener userChatMessageListener;

	ClientUserChatListener()
	{
		chats = new ArrayList<Chat>();
		userChatMessageListener = new ClientUserChatMessageListener();
	}
	
	@Override
	public void chatCreated(Chat chat, boolean createdLocally) {
		// TODO Auto-generated method stub
		System.out.println("Chat created...");
		chats.add( chat );
		chat.addMessageListener( userChatMessageListener );
	}

}
