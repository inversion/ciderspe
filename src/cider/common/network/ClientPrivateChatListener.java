package cider.common.network;

import java.util.HashMap;

import javax.swing.DefaultListModel;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;

/**
 * 
 * Listen for newly created private client <-> client chats 
 * and assign them a message listener.
 * 
 * @author Andrew
 *
 */

public class ClientPrivateChatListener implements ChatManagerListener {
	
	private HashMap<String,Chat> chats;
	private ClientPrivateChatMessageListener userChatMessageListener;

	ClientPrivateChatListener(DefaultListModel userListModel)
	{
		chats = new HashMap<String,Chat>();
		userChatMessageListener = new ClientPrivateChatMessageListener();
	}
	
	@Override
	public void chatCreated(Chat chat, boolean createdLocally) {
		// TODO Auto-generated method stub
		chats.put( chat.getParticipant(), chat );
		System.out.println("Private chat accepted from " + chat.getParticipant() );
		chat.addMessageListener( userChatMessageListener );
	}

}
