package cider.common.network;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import cider.common.processes.LiveFolder;
import cider.common.processes.SourceDocument;
import cider.common.processes.TypingEvent;
import cider.common.processes.TypingEventMode;

/**
 * This is the class that implements the bot that connects to the XMPP server
 * and listens for new chats.
 * 
 * @author Andrew
 */

public class Bot implements ChatManagerListener
{
	public static final boolean DEBUG = true;
	public static final String SRCPATH = "src";
	
    // Google apps configuration
    public static final String HOST = "talk.google.com";
    public static final int PORT = 5222;
    public static final String SERVICE_NAME = "mossage.co.uk";
    public static final String BOT_USERNAME = "ciderbot@mossage.co.uk";
    public static final String BOT_PASSWORD = "botpassword";
    
    private XMPPConnection connection;
    private ChatManager chatmanager;
    private LiveFolder liveFolder;
    private BotMessageListener messageListener;

    public void testTree()
    {
        this.liveFolder = new LiveFolder("root");
        SourceDocument t1 = this.liveFolder.makeDocument("t1.SourceDocument");
        Queue<TypingEvent> tes = new LinkedList<TypingEvent>();
        tes.addAll(SourceDocument.generateEvents(0, 1000, 0,
                "This was a triumph!", TypingEventMode.insert));
        t1.push(tes);
        this.liveFolder.makeFolder("testFolder").makeFolder("test2")
                .makeDocument("test2Doc.SourceDocument");
    }

    public static void main( String[] args )
    {
    	Bot bot = new Bot();
    	try {
			System.in.read();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public Bot()
    {
        try
        {
            this.testTree();
            System.out.println(this.liveFolder.xml(""));

            // Connect and login to the XMPP server
            ConnectionConfiguration config = new ConnectionConfiguration(
                    HOST, PORT, SERVICE_NAME);
            this.messageListener = new BotMessageListener( this );
            connection = new XMPPConnection( config );
            connection.connect();
            connection.login( BOT_USERNAME, BOT_PASSWORD );

            if ( DEBUG )
            {
                System.out.println("Server connected="
                        + connection.isConnected());
                System.out.println("Server username=" + connection.getUser());
            }

            // Listen for new chats being initiated
            chatmanager = connection.getChatManager();
            chatmanager.addChatListener(this);
        }
        catch (XMPPException e)
        {
            e.printStackTrace();
        }
    }

    public void disconnect()
    {
        this.connection.disconnect();
    }

    public LiveFolder getRootFolder()
    {
        return this.liveFolder;
    }

    @Override
    public void chatCreated(Chat chat, boolean createdLocally)
    {
        if (DEBUG)
            System.out.println(chat.getParticipant() + " connected...");

        chat.addMessageListener(this.messageListener);
    }

}
