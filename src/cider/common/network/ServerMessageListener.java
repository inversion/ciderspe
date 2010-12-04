package cider.common.network;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;


/**
 * This class waits for a message to be received on a chat session
 * and then responds to messages accordingly.
 * 
 * @author Andrew
 *
 */

public class ServerMessageListener implements MessageListener {
	
	@Override
	public void processMessage(Chat chat, Message message) {
		String body;
		body = message.getBody();
		if( body.startsWith( "getfile=" ) )
		{
			//chat.sendMessage( "file=" + fh.getFileContents( body.substring( 8, body.length() ) ) );
		}
		else if( body.equals( "getfilelist" ) )
		{
			//chat.sendMessage( "filelist=" + fh.getDirListXML( Common.SRCPATH ) );
		}
	}
	
}
