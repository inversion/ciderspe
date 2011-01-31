package cider.common.network;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.muc.MultiUserChat;

import cider.common.processes.LiveFolder;
import cider.common.processes.SourceDocument;
import cider.common.processes.TypingEvent;
import cider.common.processes.TypingEventMode;

/**
 * This is the class that implements the bot that connects to the XMPP server.
 * 
 * @author Andrew
 */

public class Bot
{
    public static final boolean DEBUG = true;
    public static final String SRCPATH = "src";

    // Google apps configuration
    public static final String HOST = "talk.google.com";
    public static final int PORT = 5222;
    public static final String SERVICE_NAME = "mossage.co.uk";
    public static final String BOT_USERNAME = "ciderbot@mossage.co.uk";
    public static final String BOT_PASSWORD = "botpassword";
    
    // Chatroom
    protected MultiUserChat chatroom;
    private final String chatroomName = "private-chat-d70eec50-2cbf-11e0-91fa-0800200c9a70" + "@" + "groupchat.google.com";

    private XMPPConnection connection;
    private ChatManager chatmanager;
    private BotChatListener chatListener;
    
    private LiveFolder liveFolder;
    
    public static void main(String[] args)
    {
        @SuppressWarnings("unused")
        Bot bot = new Bot();
        try
        {
            System.in.read();
        }
        catch (IOException e)
        {
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
            ConnectionConfiguration config = new ConnectionConfiguration(HOST, PORT, SERVICE_NAME);
            connection = new XMPPConnection(config);
            connection.connect();
            connection.login(BOT_USERNAME, BOT_PASSWORD);

            // Set up and join chatroom
            chatroom = new MultiUserChat( connection, chatroomName );
            chatroom.join( "ciderbot" );
            
            // Listen for new chats being initiated by clients
            chatmanager = connection.getChatManager();
            chatListener = new BotChatListener( this );
            chatmanager.addChatListener( chatListener );
        }
        catch (XMPPException e)
        {
            e.printStackTrace();
        }
    }
    
    public LiveFolder getRootFolder()
    {
        return this.liveFolder;
    }
    
    public void testTree()
    {
        this.liveFolder = new LiveFolder("root");
        SourceDocument t1 = this.liveFolder.makeDocument("t1.SourceDocument");
        Queue<TypingEvent> tes = new LinkedList<TypingEvent>();
        tes.addAll(SourceDocument.generateEvents(0, 1000, 0, "Created at "
                + System.currentTimeMillis(), TypingEventMode.insert, "bot"));
        t1.push(tes);
        this.liveFolder.makeFolder("testFolder").makeFolder("test2")
                .makeDocument("test2Doc.SourceDocument");
    }

    public void disconnect()
    {
        this.connection.disconnect();
    }

}
