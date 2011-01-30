package cider.common.network;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.Timer;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;

import cider.client.gui.DirectoryViewComponent;
import cider.common.processes.TypingEvent;

/**
 * This class waits for a message to be received by the client on its chat
 * session with the server.
 * 
 * @author Andrew + Lawrence
 * 
 */

public class ClientMessageListener implements MessageListener, ActionListener
{
    // TODO: These probably shouldn't be public
    public DirectoryViewComponent dirView;
    private Client client;
    private Timer timer;

    public ClientMessageListener(DirectoryViewComponent dirView, Client client)
    {
        this.client = client;
        this.dirView = dirView;
        timer = new Timer(100, this);
    }

    @Override
    public void processMessage(Chat chat, Message message)
    {
        String body = message.getBody();
        if (body.startsWith("filelist="))
        {
            String xml = body.split("filelist=")[1];
            this.dirView.constructTree(xml);
            this.client.setLiveFolder(this.dirView.getLiveFolder());
            this.client.setUpdatesAutomatically(true);
        }
        else if (body.startsWith("pushto("))
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
                this.client.push(typingEvents, dest);
            }
            if (!timer.isRunning() && client.updatesAutomatically())
                timer.start();

        }
    }

    @Override
    public void actionPerformed(ActionEvent ae)
    {
        this.client.pullEventsSince(this.client.getLastUpdate());
    }
}
