package cider.common.network;

/**
 * Listen for new messages to the chatroom
 * 
 * @author Andrew
 * 
 */

import java.util.LinkedList;
import java.util.Queue;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

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
        String body = msg.getBody();
        if (body.startsWith("pushto("))
        {
            String[] instructions = body.split("\\n");
            for (String instruction : instructions)
            {
                String[] preAndAfter = instruction.split("\\) ");
                String[] pre = preAndAfter[0].split("\\(");
                String dest = pre[1];
                dest = dest.replace("root\\", "");
                Queue<TypingEvent> typingEvents = new LinkedList<TypingEvent>();
                typingEvents.add(new TypingEvent(preAndAfter[1]));
                System.out.println("Push " + preAndAfter[1] + " to " + dest);
                this.bot.getRootFolder().path(dest).push(typingEvents);
            }
        }
    }
}
