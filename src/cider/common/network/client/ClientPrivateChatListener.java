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

import java.util.HashMap;

import javax.swing.JScrollPane;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.util.StringUtils;

/**
 * 
 * Listen for newly created private client <-> client chats and assign them a
 * message listener.
 * 
 * @author Andrew
 * 
 */

public class ClientPrivateChatListener implements ChatManagerListener
{

    private static final boolean DEBUG = true;

    protected HashMap<String, Chat> usersToChats = new HashMap<String, Chat>();
    protected HashMap<String, JScrollPane> usersToTabs = new HashMap<String, JScrollPane>();
    private Client client;
    private String chatroomName, botName;

    ClientPrivateChatListener(Client caller, String chatroomName, String botName)
    {
        client = caller;
        this.chatroomName = chatroomName;
        this.botName = botName;
    }

    @Override
    public void chatCreated(Chat chat, boolean createdLocally)
    {
        // The "friendly" name of the participant without the domain etc.
        String name = StringUtils.parseName(chat.getParticipant());

        if (name.equals(chatroomName) || name.equals(botName))
            return;

        // TODO: Pretty sure below commented out is redundant because
        // initiateChat doesn't let you make duplicate chats.
        /*
         * if( client.receiveTabs.indexOfTab( name ) != -1 ) {
         * System.out.println( "Received private chat from " + name +
         * " but already existed, replacing."); destroyChat( name ); }
         */

        if (DEBUG)
            System.out.println("Private chat accepted from " + name);

        // If this chat is already open
        if (usersToChats.containsKey(name))
        {
            // Add this chat to the hash table of chats
            usersToChats.put(name, chat);

            // Update the tab to reference the new chat
            client.tabsToChats.put(usersToTabs.get(name), chat);

            // Switch tab to the already opened one
            client.shared.receiveTabs
                    .setSelectedIndex(client.shared.receiveTabs
                            .indexOfTab(name));
        }
        else
        {
            // Create a new tab for this chat
            JScrollPane newTab = client.createChatTab(name);

            client.tabsToChats.put(newTab, chat);
            usersToTabs.put(name, newTab);
            usersToChats.put(name, chat);
        }

        // Listen for new messages on this chat
        chat.addMessageListener(new ClientPrivateChatMessageListener(client));

        if (DEBUG)
            System.out.println("Added to chats by listener: " + name);

    }

    /**
     * Destroys a chat object and its associated GUI objects.
     * 
     * @author Andrew
     * @param The
     *            username associated with the chat to destroy.
     */
    public void closeChat(String name)
    {
        // Unregister the chatlistener and remove the chat object itself
        Chat oldChat = usersToChats.get(name);
        oldChat.removeMessageListener((MessageListener) oldChat.getListeners()
                .toArray()[0]);
        usersToChats.remove(name);

        JScrollPane tab = usersToTabs.get(name);
        client.tabsToChats.remove(tab);
        client.shared.receiveTabs.removeTab(name);
        client.usersToAreas.remove(name);
    }

}
