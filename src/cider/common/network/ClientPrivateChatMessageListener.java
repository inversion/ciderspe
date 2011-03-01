package cider.common.network;

import java.io.IOException;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;

import cider.specialcomponents.Base64;

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
		String body = null;
		try
        {
            body = new String(Base64.decode( message.getBody()));
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block=
            e.printStackTrace();
        }
        System.out.println("Received message on private chat from " + chat.getParticipant() + ", " + body);
		client.updatePrivateChatLog( chat.getParticipant(), message.getSubject(), body );
	}
}
