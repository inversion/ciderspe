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
import java.util.Iterator;

import javax.swing.JScrollPane;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.util.StringUtils;

/**
 * 
 * Listen for newly created private client <-> client chats 
 * and assign them a message listener.
 * 
 * @author Andrew
 *
 */

public class ClientPrivateChatListener implements ChatManagerListener {
    
    private static final boolean DEBUG = true;
    
    protected HashMap<String,Chat> privateChats = new HashMap<String,Chat>();
    private Client client;
    private String chatroomName;
    
    ClientPrivateChatListener( Client caller, String chatroomName )
    {
        client = caller;
        this.chatroomName = chatroomName;
    }
    
    @Override
    public void chatCreated(Chat chat, boolean createdLocally) 
    {
        // The "friendly" name of the participant without the domain etc.
        String name = StringUtils.parseName( chat.getParticipant() );
        
        if( name.equals( chatroomName ) )
            return;
        
        // TODO: Pretty sure below commented out is redundant because initiateChat doesn't let you make duplicate chats.
/*        if( client.receiveTabs.indexOfTab( name ) != -1 )
        {
            System.out.println( "Received private chat from " + name + " but already existed, replacing.");
            destroyChat( name );
        }*/
        
        if( DEBUG )
            System.out.println("Private chat accepted from " + name );
        
        // Add this chat to the hash table of chats
        privateChats.put( name, chat );
        
        // Create a new tab for this chat
        JScrollPane newTab = client.createChatTab( name );
        client.tabsToChats.put( newTab, chat );
        
        // Listen for new messages on this chat
        chat.addMessageListener( new ClientPrivateChatMessageListener( client ) );
        
        if( DEBUG )
            System.out.println("Added to chats by listener: " + name );
        
    }
    
    /**
     * Destroys a chat object and its associated GUI objects.
     * 
     * @author Andrew
     * @param The username associated with the chat to destroy.
     */
    protected void destroyChat( String name )
    {
        // Unregister the chatlistener and remove the chat object itself
        Chat oldChat = privateChats.get( name );
        oldChat.removeMessageListener( (MessageListener) oldChat.getListeners().toArray()[0] );
        privateChats.remove( name );
        
        // Remove this tab from the hash tables and GUI
        Iterator<JScrollPane> itr = client.tabsToChats.keySet().iterator();
        while( itr.hasNext() )
        {
            JScrollPane currentTab = itr.next();
            if( currentTab.getName().equals( name ) )
            {
                client.tabsToChats.remove( currentTab );
                client.shared.receiveTabs.remove( currentTab );
                client.usersToAreas.remove( name );
                break;
            }
        }
    }

}
