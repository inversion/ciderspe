package cider.common.network;

import java.io.IOException;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

import cider.specialcomponents.Base64;

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
            String body = null;
            try
            {
                body = new String(Base64.decode(msg.getBody()));
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (body.startsWith("filelist=") || body.startsWith("pushto(")
                    || body.startsWith("empty"))
            {
                if (body.contains("true"))
                {
                    System.out.println("\t[locked]");
                }
                client.processDocumentMessages(body);
            }
            else
                client.updateChatLog(msg.getFrom(), msg.getSubject(), body);
        }
    }

}
