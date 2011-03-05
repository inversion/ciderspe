package cider.common.network;

import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JScrollPane;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
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
	
	private static final boolean DEBUG = true;
	
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
		
		// TODO: Pretty sure below commented out is redundant because initiateChat doesn't let you make duplicate chats.
/*		if( client.receiveTabs.indexOfTab( name ) != -1 )
		{
			System.out.println( "Received private chat from " + name + " but already existed, replacing.");
			destroyChat( name );
		}*/
		
		if( DEBUG )
			System.out.println("Private chat accepted from " + name );
		
		// Add this chat to the hash table of chats
		privateChats.put( name, chat );
		
		// Create a new tab for this chat
		JScrollPane newTab = client.createChatTab( name );
		client.tabsToChats.put( newTab, chat );
		
		// Listen for new messages on this chat
		chat.addMessageListener( new ClientPrivateChatMessageListener( client ) );
		
		if( DEBUG )
			System.out.println("Added to chats by listener: " + name );
		
	}
	
	/**
	 * Destroys a chat object and its associated GUI objects.
	 * 
	 * @author Andrew
	 * @param The username associated with the chat to destroy.
	 */
	protected void destroyChat( String name )
	{
		// Unregister the chatlistener and remove the chat object itself
		Chat oldChat = privateChats.get( name );
		oldChat.removeMessageListener( (MessageListener) oldChat.getListeners().toArray()[0] );
		privateChats.remove( name );
		
		// Remove this tab from the hash tables and GUI
		Iterator<JScrollPane> itr = client.tabsToChats.keySet().iterator();
		while( itr.hasNext() )
		{
			JScrollPane currentTab = itr.next();
			if( currentTab.getName().equals( name ) )
			{
				client.tabsToChats.remove( currentTab );
				client.receiveTabs.remove( currentTab );
				client.usersToAreas.remove( name );
				break;
			}
		}
	}

}
