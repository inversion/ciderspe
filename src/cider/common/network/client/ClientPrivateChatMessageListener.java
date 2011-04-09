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

import java.util.Date;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.util.StringUtils;



/**
 * 
 * Handle incoming messages on user chats.
 * 
 * @author Andrew
 *
 */

public class ClientPrivateChatMessageListener implements MessageListener {

    private static final boolean DEBUG = true;
    
    private Client client;
    
    public ClientPrivateChatMessageListener( Client caller ) 
    {
        this.client = caller;
    }

    @Override
    public void processMessage(Chat chat, Message message) 
    {
        // TODO: Bit dodgy about null etc.
        String body = message.getBody();
        if( body == null )
            return;
        
        if( DEBUG )
            System.out.println("ClientPrivateChatMessageListener: Received message on private chat from " + chat.getParticipant() + ", " + body);
        
        if( message.getSubject() != null )
            client.updatePrivateChatLog( StringUtils.parseName( chat.getParticipant() ), message.getSubject(), body );
        else
        {
            Date date = new Date();
            client.updatePrivateChatLog( StringUtils.parseName( chat.getParticipant() ), Client.dateFormat.format(date), body );
        }
            
    }
}
