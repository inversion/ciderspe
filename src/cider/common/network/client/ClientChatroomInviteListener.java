package cider.common.network.client;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

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

	private static final boolean DEBUG = true;
	
	private MultiUserChat chatroom;
	private String nickname;
	private Client parent;
	
	public ClientChatroomInviteListener( MultiUserChat source, String nickname, Client parent )
	{
		this.nickname = nickname;
		chatroom = source;
		this.parent = parent;
	}
	
	@Override
	public void invitationReceived(Connection conn, String room,
			String inviter, String reason, String password, Message message) {
		try {
			if( DEBUG )
				System.out.println("Invited to chatroom " + room + " by " + inviter + "...");
			chatroom.join( this.nickname, password );
		} 
		catch (XMPPException e)
		{
			JOptionPane.showMessageDialog(new JPanel(), "Error: " + e.getMessage());
			parent.getLogin().logout();
		}
	}
}
