package cider.common.network;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

/**
 * 
 * This implements the client side of the XMPP layer, it has methods
 * to get a file from the server and return its contents as a String.
 * 
 * 
 * @author Andrew
 *
 */

public class Client {

	private XMPPConnection connection;
	private ChatManager chatmanager;
	private ClientMessageListener listener;
	private Chat chat;
	
	Client()
	{
		try
		{
			// Connect and login to the XMPP server
			connection = new XMPPConnection( Common.DOMAIN );
			connection.connect();
			connection.login( Common.CLIENT_USERNAME, Common.CLIENT_PASSWORD );
			
			chatmanager = connection.getChatManager();
			listener = new ClientMessageListener( );
			chat = chatmanager.createChat( Common.BOT_USERNAME + "@" + Common.DOMAIN, listener );
		}
		catch( XMPPException e )
		{
			e.printStackTrace();
		}
	}
	
	public void getFile( String filename )
	{
		try {
			chat.sendMessage( "getfile " + filename );
		} catch (XMPPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public void getFileList()
	{
		try {
			chat.sendMessage( "getfilelist" );
		} catch (XMPPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
