package cider.common.network;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Queue;

import javax.swing.DefaultListModel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketInterceptor;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.muc.MultiUserChat;

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
    private String username;

    // Listen for private chat sessions with other users
    private ClientPrivateChatListener userChatListener;
    
    // Chatroom
    private MultiUserChat chatroom;
    private final String chatroomName = "private-chat-d70eec50-2cbf-11e0-91fa-0800200c9a70" + "@" + "groupchat.google.com";
    
    private JTextArea messageReceiveBox;

    public Client(DirectoryViewComponent dirView, JTabbedPane tabbedPane,
            Hashtable<String, SourceEditor> openTabs, DefaultListModel userListModel, JTextArea messageReceiveBox, String username,
            String password, String host, int port, String serviceName)
            
    {
    	// Assign objects from parameters
        this.tabbedPane = tabbedPane;
        this.openTabs = openTabs;
        this.username = username;
        this.messageReceiveBox = messageReceiveBox;
    	
        // Connect and login to the XMPP server
        ConnectionConfiguration config = new ConnectionConfiguration(host,
                port, serviceName);
        connection = new XMPPConnection( config );
        try {
			connection.connect();
		} catch (XMPPException e1) {
			// TODO Auto-generated catch block
			System.err.println( "Error Connecting: " + e1.getMessage() );
		}
		
		if( DEBUG )
			System.out.println("Connected to XMPP server, using TLS=" + connection.isSecureConnection() + ", using compression=" + connection.isUsingCompression() );
		
        try {
			connection.login(username + "@" + serviceName, password);
		} catch (XMPPException e1) {
			// TODO Auto-generated catch block
			System.err.println( "Error logging in: " + e1.getMessage() );
		}
		
		if( DEBUG )
			System.out.println("Logged into XMPP server, username=" + username + "@" + serviceName);

        // Subscribe to bot
        Presence sub = new Presence( Presence.Type.subscribe );
        Packet pkt = (Packet)sub;
        pkt.setTo( BOT_USERNAME );
        connection.sendPacket( pkt );
        
        // TODO: TEMP SOLUTION: Reconnect to make sure friendship is established with bot
        connection.disconnect();
        connection = new XMPPConnection( config );
        try {
			connection.connect();
		} catch (XMPPException e1) {
			// TODO Auto-generated catch block
			System.err.println( "Error Connecting: " + e1.getMessage() );
		}
		
		if( DEBUG )
			System.out.println("Connected to XMPP server, using TLS=" + connection.isSecureConnection() + ", using compression=" + connection.isUsingCompression() );
		
        try {
			connection.login(username + "@" + serviceName, password);
		} catch (XMPPException e1) {
			// TODO Auto-generated catch block
			System.err.println( "Error logging in: " + e1.getMessage() );
		}
		
		if( DEBUG )
			System.out.println("Logged into XMPP server, username=" + username + "@" + serviceName);

		
        // Add listener for new user chats
        chatmanager = this.connection.getChatManager();
//        userChatListener = new ClientPrivateChatListener( userListModel );
//        chatmanager.addChatListener(userChatListener);
        
        // Establish chat session with the bot
        botChatlistener = new ClientMessageListener(dirView, this);
        botChat = chatmanager.createChat(BOT_USERNAME, botChatlistener);
        
        // Listen for invitation to chatroom and set up message listener for it
        chatroom = new MultiUserChat( connection, chatroomName );
        MultiUserChat.addInvitationListener( connection, new ClientChatroomInviteListener( chatroom, username ) );
        chatroom.addMessageListener( new ClientChatroomMessageListener( this ) );
    }
    
    public void updateChatLog(String username, Date date, String message)
    {
    	//TODO format the text nicely i.e. bold and not bold
    	DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    	//messageReceiveBox.setContentType("text/html");
    	String oldText = messageReceiveBox.getText();
    	//messageReceiveBox.setText("<html>" + "<b>" + username + "</b>" + " (" + dateFormat.format(date) + "):<br>" + message + "<br></html>");
    	
    	//messageReceiveBox.append(username + " (" + dateFormat.format(date) + "):\n");
    	System.out.println(username + "\n" + date + "\n" + message);
    	messageReceiveBox.append(username + " (" + (date) + "):\n" + message + "\n");
    }
    
    public void sendMessageChatroom( String message )
    {
    	try {
    		Message msg = chatroom.createMessage();
    		msg.setBody( message );
			chatroom.sendMessage( msg );
		} catch (XMPPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public String getUsername()
    {
        return this.username;
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
        System.out.println("Disconnected from XMPP server...");
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
            botChat
                    .sendMessage("pullEventsSince(" + String.valueOf(time)
                            + ")");
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
