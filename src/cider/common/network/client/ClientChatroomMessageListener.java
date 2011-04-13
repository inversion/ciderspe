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

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.util.StringUtils;

import cider.client.gui.MainWindow;


/**
 * Listens for new messages in chatrooms.
 * 
 * (Currently there is only one room, created by the bot, for all clients).
 * 
 * @author Andrew
 * 
 */

public class ClientChatroomMessageListener implements PacketListener
{

    private Client client;

    public ClientChatroomMessageListener(Client source)
    {
        client = source;
    }

    @Override
    public void processPacket(Packet packet)
    {
        
        boolean docMessage = false;
        Message msg = (Message) packet;
        String body = msg.getBody();
        
        // Don't parse your own messages
        if( !StringUtils.parseResource( packet.getFrom() ).equals( client.getUsername() ) )
            docMessage = client.processDocumentMessages( msg );
        else if( msg.getProperty( "ciderAction" ) != null ) // If it's our action don't print to gui
            return;
        
        // If this isn't a document message print it to the chatlog
        if( !docMessage )
            client.updateChatroomLog(msg.getFrom(), msg.getSubject(), body);
        
        client.shared.receiveTabs.tabflash( MainWindow.GROUPCHAT_TITLE );
    }

}
