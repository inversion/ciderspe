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
        Message msg = (Message) packet;
        String body = new String(StringUtils.decodeBase64(msg.getBody()));
        if (body.startsWith("pushto("))
        {
            String[] instructions = body.split("%%");
            Hashtable<String, SourceDocument> changedDocs = new Hashtable<String, SourceDocument>();
            for (String instruction : instructions)
            {
                String[] preAndAfter = instruction.split("\\) ");
                String[] pre = preAndAfter[0].split("\\(");
                String dest = pre[1];
                dest = dest.replace("root\\", "");
                Queue<TypingEvent> typingEvents = new LinkedList<TypingEvent>();
                typingEvents.add(new TypingEvent(preAndAfter[1]));
                // System.out.println("Push " + preAndAfter[1] + " to " + dest);
                if (instructions.length > 1)
                    System.out.println("Bot received " + instructions.length
                            + " events at the same time");
                SourceDocument doc = this.bot.getRootFolder().path(dest);
                doc.push(typingEvents);
                changedDocs.put(dest, doc);
            }

            for (Entry<String, SourceDocument> entry : changedDocs.entrySet())
            {
            	//FIXME: UNUSED VARIABLE!
                @SuppressWarnings("unused")
				Hashtable<String, Integer> characterCountsForUsersEditingThisDocument = entry
                        .getValue().playOutEvents(Long.MAX_VALUE)
                        .countCharactersAll();

            }
        }
        else if (body.startsWith("colourchange:"))
        {
            String[] split = body.split(" ");
            int R = Integer.parseInt(split[2]);
            int G = Integer.parseInt(split[3]);
            int B = Integer.parseInt(split[4]);
            System.out.println("Colour change received from " + split[1] + ": "
                    + R + ", " + G + ", " + B);
            if (bot.colours.containsKey(split[1]))
            {
                bot.colours.remove(split[1]);
            }
            bot.colours.put(split[1], new Color(R, G, B));
        }
    }
}
