package cider.common.network;

import java.io.IOException;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;
import cider.specialcomponents.Base64;

/**
 * This class waits for a message to be received by the client
 * on its chat session with the server.
 * 
 * 
 * 
 *
 * @author Andrew
 *
 */

public class ClientMessageListener implements MessageListener {
	
	@Override
	public void processMessage(Chat chat, Message message) {

		if( message.getBody().startsWith( "file=" ) )
		{
			// For now just print out the decoded file
			try {
				System.out.println( new String( Base64.decode( message.getBody().substring( 5, message.getBody().length() ).getBytes() ) ) );
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if( message.getBody().startsWith( "filelist=" ) )
		{
			// For now just print out the file list XML
			System.out.println( message.getBody().substring( 10, message.getBody().length() ) );
		}
		
	}

}
