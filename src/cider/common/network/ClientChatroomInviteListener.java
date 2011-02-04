package cider.common.network;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.MultiUserChat;

/**
 * Listens for invitations to chatrooms.
 * 
 * (Currently there is only one room, created by the bot, for all clients).
 * 
 * @author Andrew
 *
 */

public class ClientChatroomInviteListener implements InvitationListener {

	private MultiUserChat chatroom;
	private String nickname;
	
	public ClientChatroomInviteListener( MultiUserChat source, String nickname )
	{
		this.nickname = nickname;
		chatroom = source;
	}
	
	@Override
	public void invitationReceived(Connection conn, String room,
			String inviter, String reason, String password, Message message) {
		// TODO Auto-generated method stub
		try {
			System.out.println("Invited to chatroom " + room + " by " + inviter + "...");
			chatroom.join( this.nickname, password );				
		} catch (XMPPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
