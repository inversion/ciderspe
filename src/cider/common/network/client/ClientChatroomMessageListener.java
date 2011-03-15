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
        // TODO Auto-generated method stub
        Message msg = (Message) packet;
        if (msg.getType() == Message.Type.groupchat)
        {
            String body = new String( StringUtils.decodeBase64( msg.getBody() ) );
            if (body.startsWith("filelist=") 
            		|| body.startsWith("pushto(")
                    || body.startsWith("empty")
                    || body.startsWith("colourchange:"))
                client.processDocumentMessages(body);
            else
                client.updateChatroomLog(msg.getFrom(), msg.getSubject(), body);
        }
    }

}
