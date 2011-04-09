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

/**
 * Listen for new messages to the chatroom
 * 
 * @author Andrew
 * 
 */

import java.awt.Color;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Queue;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.util.StringUtils;

import cider.common.processes.SourceDocument;
import cider.common.processes.TypingEvent;

public class BotChatroomMessageListener implements PacketListener
{

    private Bot bot;

    public BotChatroomMessageListener(Bot bot)
    {
        this.bot = bot;
    }

    @Override
    public void processPacket(Packet packet)
    {
        Message message = (Message) packet;

        String subject = message.getSubject();
        if (subject.equals("pushto"))
            pushto( message );
        else if ( subject.equals("colourchange") )
        {
            Integer r = (Integer) message.getProperty("r");
            Integer g = (Integer) message.getProperty("g");
            Integer b = (Integer) message.getProperty("b");
            String username = StringUtils.parseResource( packet.getFrom() );

            System.out.println("Colour change received from " + username + ": "
                    + r + ", " + g + ", " + b);
            
            if (bot.colours.containsKey(username))
            {
                bot.colours.remove(username);
            }
            bot.colours.put(username, new Color(r, g, b));
        }
    }
    
    private void pushto( Message msg )
    {
        int i = 0;
        Hashtable<String, SourceDocument> changedDocs = new Hashtable<String, SourceDocument>();
        // Loop until we've processed all events in the message
        while( true )
        {
            String dest = (String) msg.getProperty( "path" + i );
            String te = (String) msg.getProperty( "te" + i );
            
            // If all events have been processed
            if( dest == null && te == null )
                break;

            dest = dest.replace("root\\", "");
            Queue<TypingEvent> typingEvents = new LinkedList<TypingEvent>();
            typingEvents.add(new TypingEvent(te));
            // System.out.println("Push " + preAndAfter[1] + " to " + dest);
            SourceDocument doc = this.bot.getRootFolder().path(dest);
            doc.push(typingEvents);
            changedDocs.put(dest, doc);
            i++;
        }
        
        if (i > 0)
            System.out.println("Bot received " + i
                    + " events at the same time");

        for (Entry<String, SourceDocument> entry : changedDocs.entrySet())
        {
            //FIXME: UNUSED VARIABLE!
            @SuppressWarnings("unused")
            Hashtable<String, Integer> characterCountsForUsersEditingThisDocument = entry
                    .getValue().playOutEvents(Long.MAX_VALUE)
                    .countCharactersAll();

        }
    }
}
