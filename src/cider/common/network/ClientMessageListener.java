package cider.common.network;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;

import cider.client.gui.DirectoryViewComponent;
import cider.common.processes.TypingEvent;
import cider.specialcomponents.Base64;

/**
 * This class waits for a message to be received by the client on its chat
 * session with the server.
 * 
 * 
 * @author Andrew + Lawrence
 * 
 */

public class ClientMessageListener implements MessageListener
{

    // TODO: These probably shouldn't be public
    public DirectoryViewComponent dirView;

    private Pattern fileMatch = Pattern
            .compile("<file><path>(.+)</path><contents>(.+)</contents></file>");
    private Matcher matcher = null;
    private long lastUpdate = 0;
    private Client client;

    public ClientMessageListener(DirectoryViewComponent dirView, Client client)
    {
        this.client = client;
        this.dirView = dirView;
    }

    @Deprecated
    private void mossFile(String body)
    {
        try
        {
            if (matcher != null)
                matcher = matcher.reset(body);
            else
                matcher = fileMatch.matcher(body);

            if (matcher.matches())
            {
                String path = new String(Base64.decode(matcher.group(1)));
                String contents = new String(Base64.decode(matcher.group(2)));
                System.out.println("got " + contents);
                // tabbedPane.addTab(path, new SourceEditor(contents, path));
                // tabbedPane.setSelectedIndex(++currentTab);
            }

        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void processMessage(Chat chat, Message message)
    {
        String body = message.getBody();
        if (body.startsWith("<file>"))
        {
            this.mossFile(body);
        }
        else if (body.startsWith("filelist="))
        {
            String xml = body.split("filelist=")[1];
            this.dirView.constructTree(xml);
            this.client.setLiveFolder(this.dirView.getLiveFolder());
            this.client.setUpdatesAutomatically(true);
        }
        else if (body.startsWith("pushto("))
        {
            String[] preAndAfter = body.split(") ");
            String[] pre = preAndAfter[0].split("(");
            String dest = pre[1];
            Queue<TypingEvent> typingEvents = new LinkedList<TypingEvent>();
            typingEvents.add(new TypingEvent(preAndAfter[1]));
            this.client.getLiveFolder().path(dest).push(typingEvents);
            if (client.updatesAutomatically())
                this.client.pullEventsSince(System.currentTimeMillis());
        }
    }
}
