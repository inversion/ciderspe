package cider.common.network;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.util.StringUtils;

/**
 * 
 * Listen for newly created private client <-> client chats 
 * and assign them a message listener.
 * 
 * @author Andrew
 *
 */

public class ClientPrivateChatListener implements ChatManagerListener {
	
	private ClientPrivateChatMessageListener privateChatMessageListener;
	private Client client;

	ClientPrivateChatListener( Client caller )
	{
		client = caller;
		privateChatMessageListener = new ClientPrivateChatMessageListener( client );
	}
	
	@Override
	public void chatCreated(Chat chat, boolean createdLocally) {
		// TODO Auto-generated method stub
		
		if( StringUtils.parseName( chat.getParticipant() ).equals( Bot.CHATROOM_NAME ) )
			return;
		
		if( client.chats.containsKey( StringUtils.parseName( chat.getParticipant() ) ) )
		{
			System.out.println( "Received private chat from " + StringUtils.parseName( chat.getParticipant() ) + " but already existed, replacing.");
		}
		
		System.out.println("Private chat accepted from " + StringUtils.parseName( chat.getParticipant() ) );
		client.createChatTab( StringUtils.parseName( chat.getParticipant() ) );
		chat.addMessageListener( privateChatMessageListener );
		client.chats.put( StringUtils.parseName( chat.getParticipant() ), chat );
	}

}
