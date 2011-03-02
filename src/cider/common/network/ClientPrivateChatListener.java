package cider.common.network;

import java.util.HashMap;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

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
	
	protected HashMap<String,Chat> privateChats = new HashMap<String,Chat>();
	private Client client;

	ClientPrivateChatListener( Client caller )
	{
		client = caller;
	}
	
	@Override
	public void chatCreated(Chat chat, boolean createdLocally) 
	{
		// The "friendly" name of the participant without the domain etc.
		String name = StringUtils.parseName( chat.getParticipant() );
		
		if( name.equals( Bot.CHATROOM_NAME ) )
			return;
		
		if( client.receiveTabs.indexOfTab( name ) != -1 )
		{
			System.out.println( "Received private chat from " + name + " but already existed, replacing.");
		}
		
		if( Client.DEBUG )
			System.out.println("Private chat accepted from " + name );
		
		// Add this chat to the hash table of chats
		// TODO: handle dupes
		privateChats.put( name, chat );
		
		// Create a new tab for this chat
		JScrollPane newTab = client.createChatTab( name );
		client.tabsToChats.put( newTab, chat );
		
		chat.addMessageListener( new ClientPrivateChatMessageListener( client ) );
		
		if( Client.DEBUG )
			System.out.println("Added to chats by listener: " + name );
		
	}

}
