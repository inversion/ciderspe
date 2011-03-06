package cider.common.network;

import java.util.HashMap;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;

/**
 * 
 * Listen for Presence packets of users in the chatroom.
 * 
 * Update the GUI list of users accordingly.
 *
 * @author Andrew
 *
 */

public class ClientChatroomParticipantListener implements PacketListener {

	public DefaultListModel list;
	private JLabel userCount;
	private Client parentClient;
	
	// Maintain table of users online/offline
	// TODO: Implement some sort of greyed out status in the GUI for users that have been 'seen' but are offline
	private HashMap<String,Boolean> users;
	
	public ClientChatroomParticipantListener( DefaultListModel userListModel, JLabel userTotal , Client parent) 
	{
		list = userListModel;
		users = new HashMap<String,Boolean>();
		userCount = userTotal;
		parentClient = parent;
	}

	@Override
	public void processPacket( Packet packet ) 
	{
		// TODO: Implement away/idle etc.
		Presence pres = (Presence) packet;
		String nickname = StringUtils.parseResource( pres.getFrom() );
		if( pres.getType() == Presence.Type.available )
		{
			System.out.println( "Presence from: " + nickname + " AVAILABLE" );
			if( !users.containsKey( nickname ) && !nickname.equals(Bot.BOT_USERNAME) )
			{
				users.put( nickname, true );
				list.addElement( nickname );
				userCount.setText(" " + list.getSize() + " Users Online");
			}
		}
		else if( pres.getType() == Presence.Type.unavailable )
		{
			System.out.println( "Presence from: " + nickname + " NOT AVAILABLE" );
			// TODO: GUI people display error box for this?
			if( nickname.equals( Bot.BOT_USERNAME ) )
			{
				JOptionPane.showMessageDialog(new JPanel(), "Bot has gone offline, CIDER will now log out.");
				parentClient.getParent().login.logout();
			}
			
			if( users.containsKey( nickname ) )
			{
				users.remove( nickname );
				list.removeElement( nickname );
				userCount.setText(" " + list.getSize() + " Users Online");
			}
		}
			
		else
			System.out.println( "Presence type unknown" );
	}

}
