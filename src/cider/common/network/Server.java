package cider.common.network;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

/**
 * 	This is the class that implements the bot that connects
	to the XMPP server and listens for new chats.
	
	@author Andrew
*/

public class Server implements ChatManagerListener {
	
	private XMPPConnection connection;
	private ChatManager chatmanager;
	
	Server()
	{
			try
			{
				// Connect and login to the XMPP server
				connection = new XMPPConnection( Common.DOMAIN );
				connection.connect();
				connection.login( Common.BOT_USERNAME, Common.BOT_PASSWORD );
				
				// Listen for new chats being initiated
				chatmanager = connection.getChatManager();
				chatmanager.addChatListener(this);
			}
			catch( XMPPException e )
			{
				e.printStackTrace();
			}
	}
	
	@Override
	public void chatCreated(Chat chat, boolean createdLocally) {
		
		if( Common.DEBUG )
			System.out.println(chat.getParticipant() + " connected...");
		
		chat.addMessageListener( new ServerMessageListener() );
	}  
	 

}
