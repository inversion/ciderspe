package cider.common.network;

import java.io.IOException;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

import cider.common.processes.CiderFileList;
import cider.specialcomponents.Base64;


/**
 * This class waits for a message to be received on a chat session
 * and then responds to messages accordingly.
 * 
 * TODO: Probably not the best place to instantiate the CiderFileList?
 * 
 * @author Andrew
 *
 */

public class ServerMessageListener implements MessageListener {
	
	private CiderFileList filelist = new CiderFileList( Common.SRCPATH );
	
	@Override
	public void processMessage(Chat chat, Message message) {
		String body;
		body = message.getBody();
		if( body.startsWith( "getfile=" ) ) 
		{
			try {
				chat.sendMessage( "file=" + Base64.encodeBytes( filelist.table.get( body.substring( 8, body.length() ) ).getFileContents().getBytes() ) );
			} catch (XMPPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if( body.equals( "getfilelist" ) )
		{
			try {
				chat.sendMessage( "filelist=" + Base64.encodeObject( filelist ) );
			} catch (XMPPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}
