package cider.common.network;

import java.util.ArrayList;

import javax.swing.DefaultListModel;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;

/**
 * 
 * Listen for newly created user chats and assign them a message listener.
 * 
 * @author Andrew
 *
 */

public class ClientPrivateChatListener implements ChatManagerListener {
	
	// TODO: Not sure if there's any point in this chats array
	private ArrayList<Chat> chats;
	private ClientPrivateChatMessageListener userChatMessageListener;

	ClientPrivateChatListener(DefaultListModel userListModel)
	{
		chats = new ArrayList<Chat>();
		userChatMessageListener = new ClientPrivateChatMessageListener();
	}
	
	@Override
	public void chatCreated(Chat chat, boolean createdLocally) {
		// TODO Auto-generated method stub
		System.out.println("Chat created...");
		chats.add( chat );
		chat.addMessageListener( userChatMessageListener );
	}

}
