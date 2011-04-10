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

package cider.common.network.bot;

import java.util.HashMap;
import java.util.Iterator;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.muc.MultiUserChat;

import cider.common.network.client.Client;



/**
 * 
 * Listens for new incoming chat sessions to the bot.
 * 
 * @author Andrew
 * 
 */

public class BotChatListener implements ChatManagerListener
{

    public static final boolean DEBUG = true;

    private MultiUserChat chatroom;
    private Bot source;
    HashMap<String, Chat> chats;

    BotChatListener(Bot source)
    {
        this.chatroom = source.chatroom;
        this.source = source;
        chats = new HashMap<String, Chat>();
    }

    @Override
    public void chatCreated(Chat chat, boolean createdLocally)
    {
        // Ignore this chat if it wasn't created by a CIDER client
        if (!StringUtils.parseResource(chat.getParticipant()).startsWith(
                Client.RESOURCE))
            return;

        if (chats.containsKey(StringUtils.parseName(chat.getParticipant())))
        {
            if (DEBUG)
                System.out
                        .println(chat.getParticipant()
                                + " initiated chat, but is already connected from another CIDER client, alerting new instance to quit...");
            try
            {
                Message msg = new Message();
                msg.setBody("");
                msg.setSubject( "quit" );
                chat.sendMessage( msg );
            }
            catch (XMPPException e)
            {
                
                e.printStackTrace();
            }
        }

        if (DEBUG)
            System.out.println(chat.getParticipant() + " initiated chat...");

        chats.put(StringUtils.parseName(chat.getParticipant()), chat);
        chat.addMessageListener(new BotMessageListener( this.source ));

        // Invite this user to the chatroom
        chatroom.invite(chat.getParticipant(), " ");
    }

    /**
     * Called by message listeners when they receive a quit request from the
     * client. Remove the listener from the chat and remove its entry from the
     * table.
     * 
     * @param user
     * @author Andrew
     */
    protected void endSession(String user)
    {
        if (chats.containsKey(user))
        {
            if (DEBUG)
                System.out.println("Ending session for " + user + "...");
            Chat current = chats.get(user);
            Iterator<MessageListener> itr = current.getListeners().iterator();
            while (itr.hasNext())
                current.removeMessageListener(itr.next());
            chats.remove(user);
        }
    }

}
