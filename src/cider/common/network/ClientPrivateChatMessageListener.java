package cider.common.network;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.util.StringUtils;



/**
 * 
 * Handle incoming messages on user chats.
 * 
 * @author Andrew
 *
 */

public class ClientPrivateChatMessageListener implements MessageListener {

	private Client client;
	
	public ClientPrivateChatMessageListener( Client caller ) 
	{
		this.client = caller;
	}

	@Override
	public void processMessage(Chat chat, Message message) 
	{
		// TODO: Bit dodgy about null etc.
        String body = new String( StringUtils.decodeBase64( message.getBody() ) );
        
        if( Client.DEBUG )
        	System.out.println("ClientPrivateChatMessageListener: Received message on private chat from " + chat.getParticipant() + ", " + body);
        
		client.updatePrivateChatLog( StringUtils.parseName( chat.getParticipant() ), message.getSubject(), body );
	}
}
