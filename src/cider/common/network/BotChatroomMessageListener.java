package cider.common.network;

/**
 * Listen for new messages to the chatroom
 * 
 * @author Andrew
 * 
 */

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
        String body = new String( StringUtils.decodeBase64( msg.getBody() ) );
        if (body.startsWith("pushto("))
        {
            String[] instructions = body.split(" -> ");
            Hashtable<String, SourceDocument> changedDocs = new Hashtable<String, SourceDocument>();
            for (String instruction : instructions)
            {
                String[] preAndAfter = instruction.split("\\) ");
                String[] pre = preAndAfter[0].split("\\(");
                String dest = pre[1];
                dest = dest.replace("root\\", "");
                Queue<TypingEvent> typingEvents = new LinkedList<TypingEvent>();
                typingEvents.add(new TypingEvent(preAndAfter[1]));
                System.out.println("Push " + preAndAfter[1] + " to " + dest);
                SourceDocument doc = this.bot.getRootFolder().path(dest);
                doc.push(typingEvents);
                changedDocs.put(dest, doc);
            }
            
            for(Entry<String, SourceDocument> entry : changedDocs.entrySet())
            {
            	Hashtable<String, Integer> characterCountsForUsersEditingThisDocument = entry.getValue().playOutEvents(Long.MAX_VALUE).countCharactersAll();
            	
            }
        }
    }
}
