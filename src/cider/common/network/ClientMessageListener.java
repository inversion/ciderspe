package cider.common.network;

import java.io.IOException;
import java.util.Arrays;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;

import cider.client.gui.DirectoryViewComponent;
import cider.common.processes.CiderFile;
import cider.common.processes.CiderFileList;
import cider.specialcomponents.Base64;

/**
 * This class waits for a message to be received by the client
 * on its chat session with the server.
 * 
 * 
 * @author Andrew
 *
 */

public class ClientMessageListener implements MessageListener {	
	
	public DirectoryViewComponent dirView;
	
	public ClientMessageListener( DirectoryViewComponent dirView ) {
		this.dirView = dirView;
	}

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
			//TODO: getting filelist more than once will add loads of duplicates!
			try {
				dirView.constructTree( (CiderFileList) Base64.decodeToObject( message.getBody().substring( 9, message.getBody().length() ) ) );
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
