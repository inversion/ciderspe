package cider.common.network;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Queue;

import javax.swing.JTabbedPane;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import cider.client.gui.DirectoryViewComponent;
import cider.client.gui.SourceEditor;
import cider.common.processes.LiveFolder;
import cider.common.processes.SourceDocument;
import cider.common.processes.TypingEvent;
import cider.specialcomponents.EditorTypingArea;

/**
 * 
 * This implements the client side of the XMPP layer.
 * 
 * It also handles the networking side of user chats
 * 
 * @author Andrew + Lawrence
 */

public class Client
{
    public static final boolean DEBUG = true;

    // TODO: Move this out of this class
    // Google apps configuration
    public static final String BOT_USERNAME = "ciderbot@mossage.co.uk";

    private XMPPConnection connection;
    private ChatManager chatmanager;
    private boolean autoUpdate = false;
    private LiveFolder liveFolder = null;
    private JTabbedPane tabbedPane;
    private long lastUpdate = 0;
    private Hashtable<String, SourceEditor> openTabs;
    private long lastPush = 0;
    
    // Chat session with the Bot
    private Chat botChat;
    private ClientMessageListener botChatlistener;
    
    // Listen for chat sessions with other users
    private ClientUserChatListener userChatListener;

    public Client(DirectoryViewComponent dirView, JTabbedPane tabbedPane,
            Hashtable<String, SourceEditor> openTabs, String username, String password, String host, int port, String serviceName) throws XMPPException
    {
        // Connect and login to the XMPP server
        ConnectionConfiguration config = new ConnectionConfiguration(host,
                port, serviceName);
        connection = new XMPPConnection(config);
        connection.connect();
        connection.login( username, password );

        if (DEBUG)
        {
            System.out.println("Client connected="
                    + this.connection.isConnected());
            System.out.println("Client username="
                    + this.connection.getUser());
        }

        // Add listener for new user chats
        chatmanager = this.connection.getChatManager();
        userChatListener = new ClientUserChatListener();
        chatmanager.addChatListener( userChatListener );
        
        // Establish chat session with the bot
        botChatlistener = new ClientMessageListener(dirView, this);
        botChat = chatmanager.createChat(BOT_USERNAME, botChatlistener);

        this.tabbedPane = tabbedPane;
        this.openTabs = openTabs;
        printRoster();
    }
    
    // Print users currently connected to the XMPP server
    public void printRoster()
    {
    	Roster roster = connection.getRoster();
    	Collection<RosterEntry> users = roster.getEntries();
    	Iterator<RosterEntry> itr = users.iterator();
    	System.out.println( "Roster:" );
    	while( itr.hasNext() )
    	{
    		System.out.println( itr.next().getName() );
    	}
    }

    public void disconnect()
    {
        this.connection.disconnect();
        while (this.connection.isConnected())
            System.out.printf(".");
        try
        {
            Thread.sleep(1000);
        }
        catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("Client disconnected");
    }

    public boolean updatesAutomatically()
    {
        return this.autoUpdate;
    }

    public String getPathToSourceDocument(Object[] path, int skip)
    {
        String strPath = "";
        for (Object obj : path)
        {
            if (skip > 0)
                skip--;
            else
                strPath += (String) obj + "\\";
        }
        strPath = strPath.substring(0, strPath.length() - 1);
        return strPath;
    }

    public void openTabFor(Object[] path)
    {
        String strPath = this.getPathToSourceDocument(path, 1);
        System.out.println(strPath);
        SourceDocument doc = this.liveFolder.path(strPath);
        if (!this.openTabs.containsKey(strPath))
        {
            EditorTypingArea eta = new EditorTypingArea(doc);
            SourceEditor sourceEditor = new SourceEditor(eta, this, strPath);
            sourceEditor.setTabHandle(this.tabbedPane.add(strPath, eta));
            this.openTabs.put(strPath, sourceEditor);
            System.out.println("Pull since 0 since a new tab is being opened");
            this.pullEventsSince(0);
        }
    }

    public void pullEventsSince(long time)
    {
        try
        {
            // System.out.println("pull since " + time);
            botChat.sendMessage("pullEventsSince(" + String.valueOf(time) + ")");
        }
        catch (XMPPException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void pushToServer(Queue<TypingEvent> typingEvents, String path)
    {
        // TODO
        // System.out.println("TODO: Send those outgoing events to the server");

        // FIXME
        // HACK!
        // Need code reuse between Server
        String instructions = "";
        for (TypingEvent te : typingEvents)
            instructions += "pushto(" + path + ") " + te.pack() + "\n";
        try
        {
            botChat.sendMessage(instructions);
        }
        catch (XMPPException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        this.lastPush = System.currentTimeMillis();
        this.pullEventsSince(lastPush - 1);
    }

    public void getFileList()
    {
        try
        {
            botChat.sendMessage("getfilelist");
        }
        catch (XMPPException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void setUpdatesAutomatically(boolean autoUpdate)
    {
        this.autoUpdate = autoUpdate;
    }

    public LiveFolder getLiveFolder()
    {
        return this.liveFolder;
    }

    public void setLiveFolder(LiveFolder liveFolder)
    {
        this.liveFolder = liveFolder;
    }

    public void push(Queue<TypingEvent> typingEvents, String dest)
    {
        EditorTypingArea eta = this.openTabs.get(dest).getEditorTypingArea();
        eta.getCodeLocation().push(typingEvents);
        eta.updateText();
        if (eta.getLastUpdate() >= this.lastUpdate)
            this.lastUpdate = eta.getLastUpdate();
    }

    public long getLastUpdate()
    {
        return this.lastUpdate;
    }
}
