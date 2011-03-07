package cider.common.network;

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

	private static final boolean DEBUG = true;
	
	public DefaultListModel list;
	private JLabel userCount;
	private Client parent;
	
	public ClientChatroomParticipantListener( DefaultListModel userListModel, JLabel userTotal , Client parent) 
	{
		this.list = userListModel;
		this.userCount = userTotal;
		this.parent = parent;
	}

	@Override
	public void processPacket( Packet packet ) 
	{
		// TODO: Implement away/idle etc.
		Presence pres = (Presence) packet;
		String nickname = StringUtils.parseResource( pres.getFrom() );
		if( pres.getType() == Presence.Type.available )
		{
			if( DEBUG )
				System.out.println( "Presence from: " + nickname + " AVAILABLE" );
			if( !list.contains( nickname ) && !nickname.equals(Bot.BOT_USERNAME) && !nickname.equals( "ciderchecker" ) )
			{
				if( DEBUG )
					System.out.println( "Adding " + nickname + " to list...");
				list.addElement( nickname );
				userCount.setText(" " + list.getSize() + " Users Online");
			}
			
			// Send packet to let new users know about us
			Presence presence = new Presence( Presence.Type.available );
			parent.connection.sendPacket( presence );
			
		}
		else if( pres.getType() == Presence.Type.unavailable )
		{
			if( DEBUG )
				System.out.println( "Presence from: " + nickname + " NOT AVAILABLE" );
			
			if( nickname.equals( Bot.BOT_USERNAME ) )
			{
				JOptionPane.showMessageDialog(new JPanel(), "Bot has gone offline, CIDER will now log out.");
				parent.getParent().login.logout();
			}
			
			if( list.contains( nickname ) )
			{
				list.removeElement( nickname );
				userCount.setText(" " + list.getSize() + " Users Online");
			}
		}
			
		else
			if( DEBUG ) 
				System.out.println( "Presence type unknown" );
	}

}
