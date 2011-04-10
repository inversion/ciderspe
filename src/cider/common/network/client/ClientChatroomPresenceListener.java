/**
 *  CIDER - Collaborative Integrated Development EnviRonment
    Copyright (C) 2011  Andrew Moss
                        Lawrence Watkiss
                        Jonathan Bannister
                        Alex Sheppard
                        Miles Taylor
                        Ashley Woodman

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package cider.common.network.client;

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

public class ClientChatroomPresenceListener implements PacketListener {

    private static final boolean DEBUG = true;
    
    public DefaultListModel list;
    private JLabel userCount;
    private Client parent;
    private String botUsername;
    private String checkerUsername;
    
    public ClientChatroomPresenceListener( DefaultListModel userListModel, JLabel userTotal , Client parent, String botUsername, String checkerUsername ) 
    {
        this.list = userListModel;
        this.userCount = userTotal;
        this.parent = parent;
        this.botUsername = botUsername;
        this.checkerUsername = checkerUsername;
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
            
            if( !nickname.equals( botUsername ) && !nickname.equals( checkerUsername ) )
            {
                if( pres.getMode() == null || pres.getMode() == Presence.Mode.available )
                {
                    if( !list.contains( nickname )   )
                    {
                        if( DEBUG )
                            System.out.println( "Adding " + nickname + " to list...");
                        list.addElement( nickname );
                        userCount.setText(" " + list.getSize() + " Users Online");
                    }
                    else
                    {
                        // TODO: Add GUI code for coming back from idle here
                        System.out.println( nickname + " is no longer idle...");
                    }
                }
                else if( pres.getMode() == Presence.Mode.away )
                {
                 // TODO: Add GUI code for going idle here
                    System.out.println( nickname + " went idle...");
                }
                    
            }
       
        }
        else if( pres.getType() == Presence.Type.unavailable )
        {
            if( DEBUG )
                System.out.println( "Presence from: " + nickname + " NOT AVAILABLE" );
            
            if( nickname.equals( botUsername ) )
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
