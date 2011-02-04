package cider.common.network;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
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

    // XMPP Server Configuration
    public static final String HOST = "xmpp.org.uk";
    public static final int PORT = 5222;
    public static final String SERVICE_NAME = "xmpp.org.uk";
    public static final String BOT_USERNAME = "ciderbot";
    public static final String BOT_PASSWORD = "botpassword";
    private ConnectionConfiguration config = new ConnectionConfiguration(HOST,
            PORT, SERVICE_NAME);

    // Chatroom
    protected MultiUserChat chatroom;
    private final String CHATROOM_NAME = "ciderchat";

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
            checkForBot();

            // Connect and login to the XMPP server
            connection = new XMPPConnection(config);
            connection.connect();
            connection.login(BOT_USERNAME, BOT_PASSWORD);

            // Set up and join chatroom
            chatroom = new MultiUserChat(connection, CHATROOM_NAME
                    + "@conference." + SERVICE_NAME);
            chatroom.create(BOT_USERNAME);

            // Listen for new chats being initiated by clients
            chatmanager = connection.getChatManager();
            chatListener = new BotChatListener(this);
            chatmanager.addChatListener(chatListener);

            this.testTree();
        }
        catch (XMPPException e)
        {
            System.err.println("Error:" + e.getMessage());
        }
    }

    // Connect to the server as a reserved user to check if the bot is already
    // online from another location
    private void checkForBot() throws XMPPException
    {
        XMPPConnection conn = new XMPPConnection(config);
        conn.connect();
        conn.login("ciderchecker", "checkerpw");
        chatroom = new MultiUserChat(conn, CHATROOM_NAME + "@conference."
                + SERVICE_NAME);
        try
        {
            chatroom.create("ciderchecker");
        }
        catch (XMPPException e)
        {
            System.err
                    .println("Error: Chatroom already exists, this means the bot is already online, or someone else has created the room.");
            System.err.println("Disconnecting, exiting...");
            conn.disconnect();
            System.exit(1);
        }
        chatroom.leave();
        conn.disconnect();
    }

    // Leave the chatroom and disconnect from the server
    public void disconnect()
    {
        chatroom.leave();
        connection.disconnect();
    }

    public void testTree()
    {
        this.liveFolder = new LiveFolder("Bot", "root");
        SourceDocument t1 = this.liveFolder.makeDocument("t1.SourceDocument");
        Queue<TypingEvent> tes = new LinkedList<TypingEvent>();
        tes.addAll(SourceDocument.generateEvents(0, 1000, 0, "Created at "
                + System.currentTimeMillis(), TypingEventMode.insert, "bot"));
        t1.push(tes);
        this.liveFolder.makeFolder("testFolder").makeFolder("test2")
                .makeDocument("test2Doc.SourceDocument");
    }

    public LiveFolder getRootFolder()
    {
        return this.liveFolder;
    }

}
