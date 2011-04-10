/**
 *  CIDER - Collaborative Integrated Development EnviRonment
    Copyright (C) 2011  Andrew Moss
                        Lawrence Watkiss
                        Jonathan Bannister
                        Alex Sheppard
                        Miles Taylor
                        Ashley Woodman

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package cider.common.network.client;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ProgressMonitor;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.jingle.JingleManager;
import org.jivesoftware.smackx.jingle.JingleSession;
import org.jivesoftware.smackx.jingle.JingleSessionRequest;
import org.jivesoftware.smackx.jingle.listeners.JingleSessionRequestListener;
import org.jivesoftware.smackx.jingle.media.JingleMediaManager;
import org.jivesoftware.smackx.jingle.mediaimpl.jspeex.SpeexMediaManager;
import org.jivesoftware.smackx.jingle.mediaimpl.sshare.ScreenShareMediaManager;
import org.jivesoftware.smackx.jingle.nat.ICETransportManager;
import org.jivesoftware.smackx.muc.MultiUserChat;

import cider.client.gui.ETASourceEditorPane;
import cider.client.gui.LoginUI;
import cider.client.gui.MainWindow;
import cider.common.network.ConfigurationReader;
import cider.common.network.DebugPacketFilter;
import cider.common.network.DebugPacketInterceptor;
import cider.common.network.DebugPacketListener;
import cider.common.processes.ImportFiles;
import cider.common.processes.LiveFolder;
import cider.common.processes.Profile;
import cider.common.processes.SourceDocument;
import cider.common.processes.TimeRegion;
import cider.common.processes.TypingEvent;
import cider.documentViewerComponents.EditorTypingArea;
import cider.shared.ClientSharedComponents;

/**
 * 
 * This implements the client side of the XMPP layer.
 * 
 * It also handles user chats and chatrooms.
 * 
 * @author Andrew, Lawrence
 */

public class Client
{
    // TODO: Need to make sure usernames are all alpha numeric or at least don't
    // mess up XML

    private static final boolean DEBUG = true;
    public static final String RESOURCE = "CIDER";
    public static final DateFormat dateFormat = new SimpleDateFormat(
            "dd/MM/yyyy HH:mm:ss");

    private LoginUI login;
    private MainWindow parent;

    // External configuration for the Client
    ConfigurationReader clientConfig;
    
    // XMPP Basics
    private XMPPConnection connection;
    private ChatManager chatmanager;
    private String username;
    private String host;
    private int port;
    private String serviceName;
    private String password;

    // Chat session with the Bot
    public Chat botChat;
    private ClientMessageListener botChatListener;
    public boolean botIsOnline = false;

    // Multi user chatroom
    private String chatroomName;
    public MultiUserChat chatroom;
    private JTextArea chatroomMessageReceiveBox;

    // Private chat sessions with other users
    private ClientPrivateChatListener userChatListener;
    protected HashMap<String, JTextArea> usersToAreas = new HashMap<String, JTextArea>();
    
    // Jingle (voice chat) stuff    
    private JingleManager jm;
    private JingleSession incoming = null;
    private JingleSession outgoing = null;
    
    // The current user's profile
    public Profile profile;
    public static HashMap<String, Color> colours = new HashMap<String, Color>();
    public Color incomingColour;

    // FIXME: UNUSED VARIABLE
    // private ArrayList<ActionListener> als = new ArrayList<ActionListener>();

    protected HashMap<JScrollPane, Object> tabsToChats = new HashMap<JScrollPane, Object>();

    // GUI components shared with MainWindow
    public ClientSharedComponents shared;

    // Lawrence's source document stuff
    private boolean autoUpdate = false;
    private LiveFolder liveFolder = null;
    private long lastBroadcast = 0;
    private static final long minimumBroadcastDelay = 400;
    private Message outgoingTypingEvents = new Message();
    private Timer broadcastTimer = new Timer();
    private boolean isWaitingToBroadcast = false;
    private SourceDocument currentDoc = null;
    private long clockOffset = 0;
    private PriorityQueue<Long> timeDeltaList = new PriorityQueue<Long>();
    // FIXME: synchronised is never read!
    @SuppressWarnings("unused")
    private boolean synchronised = false;
    private TimeRegion diversion;

    public Client(String username, String password, String host, int port,
            String serviceName, LoginUI log, ClientSharedComponents shared)
    {
        clientConfig = new ConfigurationReader( "Client.conf" , log );
        
        // Assign objects from parameters
        this.username = username;
        this.chatroomName = clientConfig.getChatroomName() + "@conference." + serviceName;
        this.host = host;
        this.port = port;
        this.serviceName = serviceName;
        this.password = password;
        this.login = log;

        // GUI Components shared with MainWindow
        this.shared = shared;
        this.profile = shared.profile;

        EditorTypingArea.addParent(this);
    }
    
    /**
     * Initiate handlers for VoIP chat.
     * 
     * @author Andrew
     */
    private void initJingle()
    {
        ICETransportManager icetm0 = new ICETransportManager(connection, clientConfig.getStunServer(), clientConfig.getStunPort());
        List<JingleMediaManager> mediaManagers = new ArrayList<JingleMediaManager>();
        //mediaManagers.add(new JmfMediaManager(icetm0));
        mediaManagers.add(new SpeexMediaManager(icetm0));
        mediaManagers.add(new ScreenShareMediaManager(icetm0));
        jm = new JingleManager(connection, mediaManagers);
        jm.addCreationListener(icetm0);

        jm.addJingleSessionRequestListener(new JingleSessionRequestListener() {
            public void sessionRequested(JingleSessionRequest request) {

//                if (incoming != null)
//                    return;

                try {
                    // Accept the call
                    incoming = request.accept();

                    // Start the call
                    incoming.startIncoming();
                }
                catch (XMPPException e) {
                    e.printStackTrace();
                }

            }
        });
    }
    
    /**
     * Tries to connect to the XMPP server, throwing an exception if it fails in
     * any way.
     * 
     * @author Jon, Andrew
     */
    public boolean attemptConnection() throws XMPPException
    {
        // Connect and login to the XMPP server
        ConnectionConfiguration config = new ConnectionConfiguration(host,
                port, serviceName);
        connection = new XMPPConnection(config);
        connection.connect();

        /*
         * Append a random string to the resource to prevent conflicts with
         * existing instances of the CIDER client from the same user.
         * 
         * Later the Bot will alert the user if there is an existing instance of
         * the CIDER client.
         */
        String rand = StringUtils.randomString(5);
        connection.login(username, password, RESOURCE + rand);
        if (DEBUG)
            System.out.println("Logged into XMPP server, username=" + username
                    + "/" + rand);

        // Prints out every packet received by the client, used when you want
        // very verbose debugging
         connection.addPacketListener(new DebugPacketListener(), new
         DebugPacketFilter());
         connection.addPacketInterceptor(new DebugPacketInterceptor(),
                 new DebugPacketFilter());


        chatmanager = this.connection.getChatManager();

        // Establish chat session with the bot
        botChatListener = new ClientMessageListener(this);
        botChat = chatmanager.createChat( clientConfig.getBotUsername() + "@" + serviceName,
                botChatListener);

        // Listen for invitation to chatroom and set up message listener for it
        chatroom = new MultiUserChat(connection, chatroomName);
        MultiUserChat.addInvitationListener(connection,
                new ClientChatroomInviteListener(chatroom, username, this));
        chatroom.addParticipantListener(new ClientChatroomPresenceListener(
                shared.userListModel, shared.userCount, this, clientConfig.getBotUsername(), clientConfig.getCheckerUsername()));
        chatroom.addMessageListener(new ClientChatroomMessageListener(this));

        // Add listener for new user chats
        userChatListener = new ClientPrivateChatListener( this, clientConfig.getChatroomName() );
        chatmanager.addChatListener(userChatListener);
        
        // Initiate voice chat stuff
        //initJingle();

        // Check the bot is online
        Message msg = new Message();
        msg.setBody("");
        msg.setSubject( "are you online mr bot" );
        botChat.sendMessage( msg );
        try
        {
            Thread.sleep(1000);
            return botIsOnline;
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Allows the bot to be killed remotely by clients.
     * 
     * Used in development for example when someone leaves it running by
     * accident.
     * 
     * @author Lawrence
     * 
     */
    public void terminateBotRemotely()
    {
        try
        {
            Message msg = new Message();
            msg.setBody("");
            msg.setSubject( "You play 2 hours to die like this?" );
            botChat.sendMessage( msg );
        }
        catch (XMPPException e)
        {
            
            e.printStackTrace();
        }
    }

    /**
     * Disconnect the client from the XMPP server.
     * 
     * @author Andrew, Jon
     * 
     */
    public void disconnect()
    {
        chatroom.leave();
        connection.disconnect();
        if (DEBUG)
            System.out.println("Disconnected from XMPP server...");
    }

    /**
     * Update the chatroom message log with a new message.
     * 
     * @param The
     *            username of the user who sent the message
     * @param The
     *            date the message was originally sent (as Dateformat)
     * @param The
     *            message body.
     * 
     * @author Andrew
     */
    protected void updateChatroomLog(String username, String date,
            String message)
    {
        // messageReceiveBox.setContentType("text/html");
        // String oldText = messageReceiveBox.getText();
        // messageReceiveBox.setText("<html>" + "<b>" + username + "</b>" + " ("
        // + dateFormat.format(date) + "):<br>" + message + "<br></html>");

        // messageReceiveBox.append(username + " (" + dateFormat.format(date) +
        // "):\n");

        if (DEBUG)
            System.out.println(StringUtils.parseResource(username) + "\n"
                    + date + "\n" + message);

        chatroomMessageReceiveBox.append(StringUtils.parseResource(username)
                + " (" + date + "):\n" + message + "\n");

        chatroomMessageReceiveBox.setCaretPosition(chatroomMessageReceiveBox
                .getDocument().getLength());
    }

    /**
     * Update a private chat message log with a new message.
     * 
     * @param The
     *            username of the user who sent the message
     * @param The
     *            date the message was originally sent (as Dateformat)
     * @param The
     *            message body.
     * 
     * @author Andrew
     */
    protected void updatePrivateChatLog(String username, String date,
            String message)
    {
        JTextArea current;
        // HTML Stuff by alex that's not currently in use
        // messageReceiveBox.setContentType("text/html");
        // String oldText = messageReceiveBox.getText();
        // messageReceiveBox.setText("<html>" + "<b>" + username + "</b>" + " ("
        // + dateFormat.format(date) + "):<br>" + message + "<br></html>");

        // messageReceiveBox.append(username + " (" + dateFormat.format(date) +
        // "):\n");

        if (DEBUG)
            System.out.println(username + "\n" + date + "\n" + message);

        /*
         * If this has been called for a locally sent message, use the username
         * we are sending to in selecting the right text area to update.
         */
        if (username.equals(this.username))
            current = usersToAreas.get(shared.receiveTabs
                    .getSelectedComponent().getName());
        else
            current = usersToAreas.get(username);

        current.append(username + " (" + date + "):\n" + message + "\n");
        current.setCaretPosition(current.getDocument().getLength());
    }

    /**
     * Initiate a chat session with someone, essentially has no effect if the
     * chat already exists, or if you try to chat with yourself.
     * 
     * @param The
     *            user to initiate a chat with.
     * @author Andrew
     */
    public void initiateChat(String user)
    {
        if (userChatListener.privateChats.containsKey(user)
                || user.equals(username))
        {
            if (DEBUG)
                System.out
                        .println("Chat with "
                                + user
                                + " already exists or trying to chat to self, not creating one...");
            return;
        }

        /*
         * When you create a chat session with someone it gets bounced back and
         * picked up by the userChatListener, which creates the tab etc.
         */
        chatmanager.createChat(user + "@" + serviceName, null);

        if (DEBUG)
            System.out.println("Chat initiated with " + user);
    }

    /**
     * Add a chat tab with the specified username as title to the GUI.
     * 
     * @param user
     * @author Andrew
     * @return The new tab created, labelled with the username it corresponds
     *         to.
     */
    public JScrollPane createChatTab(String user)
    {
        // Create the new message box and new tab
        JTextArea messageReceiveBox = new JTextArea();
        messageReceiveBox.setLineWrap(true);
        messageReceiveBox.setWrapStyleWord(true);
        Font receiveFont = new Font("Dialog", 2, 12);
        messageReceiveBox.setFont(receiveFont);
        messageReceiveBox.setEditable(false);
        usersToAreas.put(user, messageReceiveBox);

        JScrollPane messageReceiveBoxScroll = new JScrollPane(messageReceiveBox);
        messageReceiveBoxScroll.setName(user);
        shared.receiveTabs.add(messageReceiveBoxScroll);
        shared.receiveTabs.setTitleAt(shared.receiveTabs.getTabCount() - 1,
                user);

        // If creating a tab for the chatroom, register the chatroom message
        // receive box
        if (user.equals(MainWindow.GROUPCHAT_TITLE))
        {
            this.chatroomMessageReceiveBox = messageReceiveBox;
            tabsToChats.put(messageReceiveBoxScroll, chatroom);
        }

        return messageReceiveBoxScroll;
        // TODO: Else switch to the already open private conversation?
    }

    /**
     * Close a chat with the specified user.
     * 
     * @author Andrew
     * @param user
     */
    public void closeChat(String user)
    {
        userChatListener.destroyChat(user);
    }
    
    /**
     * Send a message on the chat session corresponding to the currently
     * selected receive tab in the Client GUI.
     * 
     * First encode it to be sent over XMPP.
     * 
     * @author Andrew
     * @param The
     *            message to be sent, as a string.
     */
    public void sendChatMessageFromGUI(String message)
    {
        
        try
        {
            Date date = new Date();
            Message msg = chatroom.createMessage();
            msg.setBody(message);
            msg.setSubject(dateFormat.format(date));

            if (shared.receiveTabs.getSelectedComponent().getName()
                    .equals(MainWindow.GROUPCHAT_TITLE))
            {
                /*
                 * If the message is being sent on a chatroom there is no need
                 * to display it here because all messages are reflected by XMPP
                 * chatrooms back to all members of the chatroom even if they
                 * sent the message in the first place.
                 * 
                 * Therefore the chatroom message listener will pick it up in
                 * this case.
                 */
                if (DEBUG)
                    System.out
                            .println("Client: Sending message on group chat: "
                                    + message);
                msg.setType( Message.Type.groupchat );
                chatroom.sendMessage(msg);
                //TODO: need to escape?
            }
            else
            {
                if (DEBUG)
                    System.out
                            .println("Client: Sending message on private chat to "
                                    + shared.receiveTabs.getSelectedComponent()
                                            .getName()
                                    + " contents: "
                                    + message);
                ((Chat) tabsToChats.get(shared.receiveTabs
                        .getSelectedComponent())).sendMessage(msg);
              //TODO: need to escape?

                // Update the log with the message before it was encoded
                updatePrivateChatLog(this.username, msg.getSubject(), message);
            }
        }
        catch (XMPPException e)
        {
            e.printStackTrace();
        }
    }

    // TODO: Below is all Lawrence's stuff so he needs to comment it!

    public String getUsername()
    {
        return this.username;
    }

    public SourceDocument getCurrentDocument()
    {
        return this.currentDoc;// .playOutEvents(Long.MAX_VALUE).countCharactersFor("user1");
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
        this.currentDoc = doc;
        if (!shared.openTabs.containsKey(strPath))
        {
            EditorTypingArea eta = new EditorTypingArea(doc);
            ETASourceEditorPane sourceEditor = new ETASourceEditorPane(eta,
                    this, strPath);
            sourceEditor.setTabHandle(shared.tabbedPane.add(strPath,
                    sourceEditor));
            shared.openTabs.put(strPath, sourceEditor);
            this.pullEventsFromBot(strPath,
                    System.currentTimeMillis() + this.getClockOffset(), true);
        }
    }

    public void pullEventsFromBot(String strPath, long time, boolean simplified)
    {
        try
        {
            // System.out.println("pull since " + time);
            Message msg = new Message();
            msg.setBody("");
            if( simplified )
                msg.setSubject( "pullSimplifiedEvents" );
            else
                msg.setSubject( "pullEvents" );
            msg.setProperty( "path", strPath );
            msg.setProperty( "time", String.valueOf(time) );
            botChat.sendMessage( msg );
        }
        catch (XMPPException e)
        {
            e.printStackTrace();
        }
    }

    public void pullEventsFromBot(String strPath, long startTime, long endTime,
            boolean stopDiversion)
    {
        try
        {
            Message msg = new Message();
            msg.setBody("");
            msg.setSubject( "pullEvents" );
            msg.setProperty( "path", strPath );
            msg.setProperty( "startTime", String.valueOf(startTime) );
            msg.setProperty( "endTime", String.valueOf(endTime) );
            msg.setProperty( "stopDiversion", (stopDiversion ? "true" : "false") );
            botChat.sendMessage( msg );
        }
        catch (XMPPException e)
        {
            e.printStackTrace();
        }
    }

    public void broadcastTypingEvents(Queue<TypingEvent> typingEvents,
            String path)
    {
        outgoingTypingEvents = new Message();
        outgoingTypingEvents.setBody("");
        outgoingTypingEvents.setSubject( "pushto" );
        outgoingTypingEvents.setType( Message.Type.groupchat );
        outgoingTypingEvents.setTo( chatroom.getRoom() );
        int i = 0;
        for (TypingEvent te : typingEvents)
        {
            outgoingTypingEvents.setProperty( "path" + i, path );
            // So we can send newlines, encode it
            outgoingTypingEvents.setProperty( "te" + i, StringUtils.encodeBase64( te.pack() ) );
            i++;
        }
        try
        {
            long currentTime = System.currentTimeMillis()
                    + this.getClockOffset();
            long interval = currentTime - this.lastBroadcast;
            if (!this.isWaitingToBroadcast)
            {
                if (interval < minimumBroadcastDelay)
                {
                    this.isWaitingToBroadcast = true;
                    this.broadcastTimer.schedule(new TimerTask()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                isWaitingToBroadcast = false;
                                long currentTime = System.currentTimeMillis()
                                        + getClockOffset();
                                if (currentTime - lastBroadcast < minimumBroadcastDelay)
                                    throw new Error(
                                            "Bug detected: broadcasting too soon");

                                chatroom.sendMessage( outgoingTypingEvents );
                                lastBroadcast = System.currentTimeMillis()
                                        + getClockOffset();
                            }
                            catch (XMPPException e)
                            {
                                e.printStackTrace();
                                JOptionPane.showMessageDialog(null,
                                        "Client failed to send message across bot chat: "
                                                + e.getMessage());
                                System.exit(1);
                            }

                        }
                    }, minimumBroadcastDelay - interval);
                }
                else
                {
                    chatroom.sendMessage( this.outgoingTypingEvents );
                    this.lastBroadcast = System.currentTimeMillis();
                }
            }
        }
        catch (XMPPException e)
        {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    null,
                    "Client failed to send message across bot chat: "
                            + e.getMessage());
            System.exit(1);
        }
    }

    public void getFileListFromBot()
    {
        try
        {
            Message msg = new Message();
            msg.setBody("");
            msg.setSubject( "getfilelist" );
            botChat.sendMessage( msg );
        }
        catch (XMPPException e)
        {
            e.printStackTrace();
            JOptionPane.showMessageDialog(new JPanel(),
                    "Error retrieving file list: " + e.getMessage());
            e.printStackTrace();
            return;
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
        Queue<TypingEvent> remainingEvents;
        if (this.diversion != null)
        {
            remainingEvents = new LinkedList<TypingEvent>();
            for (TypingEvent typingEvent : typingEvents)
                if (this.diversion.end.time > typingEvent.time)
                    this.diversion.end.typingEvents.add(typingEvent);
                else
                    remainingEvents.add(typingEvent);
        }
        else
            remainingEvents = typingEvents;

        EditorTypingArea eta = shared.openTabs.get(dest).getEditorTypingArea();
        int position = eta.getCaretPosition();
        TypingEvent anchor;
        if (position >= 0 && position < eta.getTypingEventList().length())
            anchor = eta.getTypingEventList().get(position);
        else
            anchor = null;

        eta.getSourceDocument().push(remainingEvents);
        eta.setWaiting(false);
        eta.updateText();
        if (anchor != null)
            eta.setCaretPosition(eta.getTypingEventList().getLastPositionOf(
                    anchor));
    }

    /**
     * Process incoming filelist
     * 
     * @param msg
     * 
     * @author Andrew
     */
    public void processFilelist( Message msg )
    {
        String xml = new String( StringUtils.decodeBase64( (String) msg.getProperty( "xml" ) ) );
        shared.dirView.constructTree(xml);
        this.setLiveFolder(shared.dirView.getLiveFolder());
        this.setUpdatesAutomatically(true);
    }
    
    /**
     * Process pushto
     * 
     * @param msg
     * 
     * @author Lawrence, Andrew
     */
    public void processPushto( Message msg )
    {
        Hashtable<String, Queue<TypingEvent>> queues = new Hashtable<String, Queue<TypingEvent>>();
        boolean stopDiversion = false;
        int eventNum = 0;
        String dest = "", te;
        // Loop until we've processed all events in the message
        while( true )
        {
            // If destination for this event isn't null change it
            if( msg.getProperty( "path" + eventNum ) != null )
                dest = (String) msg.getProperty( "path" + eventNum );
            
            // Processed all events in message
            if( msg.getProperty( "te" + eventNum ) == null )
                break;
            
            // So we can send newlines in the message
            te = new String( StringUtils.decodeBase64( (String) msg.getProperty( "te" + eventNum ) ) );
            
            dest = dest.replace("root\\", "");

            if( te.equals("end") )
                stopDiversion = true;

            Queue<TypingEvent> queue = queues.get(dest);
            if (queue == null)
            {
                queue = new LinkedList<TypingEvent>();
                queues.put(dest, queue);
            }
            queue.add(new TypingEvent( te ));
            System.out.println("Push " + te + " to " + dest);
            eventNum++;
        }

        for (Entry<String, Queue<TypingEvent>> entry : queues.entrySet())
            this.push(entry.getValue(), entry.getKey());

        if (stopDiversion)
        {
            this.diversion.finishedUpdate();
            this.diversion = null;
        }
    }
    
    public void processIsblank( Message msg )
    {
        String dest = (String) msg.getProperty("path");
        EditorTypingArea eta = shared.openTabs.get(dest)
                .getEditorTypingArea();
        eta.setWaiting(false);
    }
    
    /**
     * Process a colour change message.
     * 
     * @author Jon, Andrew
     *
     */
    public void processColourchange( Message msg )
    {
        String changedUser = (String) msg.getProperty("username");
        Integer r = (Integer) msg.getProperty("r");
        Integer g = (Integer) msg.getProperty("g");
        Integer b = (Integer) msg.getProperty("b");
        Color newColour = new Color(r, g, b);
        System.out.println( "Client: Changing colour for " + username + " to " + r + " " + g + " " + b);
        
        if (colours.containsKey(changedUser))
            colours.remove(changedUser);
        colours.put(changedUser, newColour);
        shared.userList.repaint();
        // EditorTypingArea.highlightMargin(); //FIXME update current line
        // colour when user changes profile colour
    }
    
    /**
     * 
     * Process document related messages.
     * @param msg
     * @return True if this was a document related message
     */
    public boolean processDocumentMessages( Message msg )
    {
        String subject = msg.getSubject();
        if ( subject.equals("pushto") )
        {
            processPushto(msg);
            return true;
        }
        else if( subject.equals("filelist") )
        {
            processFilelist(msg);
            return true;
        }
        else if( subject.equals("isblank") )
        {    
            processIsblank( msg );
            return true;
        }
        else if( subject.equals("colourchange") )
        {
            processColourchange(msg);
            return true;
        }
        return false;
    }

    public long getClockOffset()
    {
        // FIXME
        // return this.clockOffset;
        return 0;

    }

    /**
     * 
     * @param parentComponent
     */
    public void startClockSynchronisation(Component parentComponent)
    {
        final int requiredSamples = 5;
        final ProgressMonitor progressMonitor = new ProgressMonitor(
                parentComponent, "Synchronising clocks...", "", 0,
                requiredSamples + 1);
        progressMonitor.setMillisToDecideToPopup(0);

        final Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask()
        {
            int requestsMade = 0;

            @Override
            public void run()
            {
                if (this.requestsMade < 5)
                {
                    timeRequest();
                    this.requestsMade++;
                    progressMonitor.setProgress(this.requestsMade);
                }
                else
                {
                    updateClockOffset();
                    synchronised = true;
                    progressMonitor.setProgress(requiredSamples);
                    progressMonitor.setNote("Getting file list...");
                    getFileListFromBot();
                    progressMonitor.setProgress(requiredSamples + 1);
                    progressMonitor.close();
                    timer.cancel();
                }
            }

        }, 0, 2000);
    }

    private void timeRequest()
    {
        // Synchronise clock

        // 1: Client stamps current local time on a "time request" packet and
        // sends to server
        long currentLocalTime = System.currentTimeMillis();
        try
        {
            botChat.sendMessage("timeRequest(" + currentLocalTime + ")");
        }
        catch (XMPPException e)
        {
            e.printStackTrace();
            JOptionPane.showMessageDialog(new JPanel(),
                    "Error retrieving file list: " + e.getMessage());
            return;
        }
    }

    public void addTimeDeltaSample(long latency)
    {
        this.timeDeltaList.add(latency);
    }

    public void updateClockOffset()
    {
        int mid = this.timeDeltaList.size() / 2;
        int i = mid;
        while (i-- > 0)
            this.timeDeltaList.poll();

        this.clockOffset = this.timeDeltaList.peek();
        System.out.println("Clock offset set to " + this.clockOffset);
        this.timeDeltaList.clear();
    }

    public void setTimeDelta(long delta)
    {
        this.clockOffset = -delta;
    }

    public MainWindow getParent()
    {
        return parent;
    }

    public LoginUI getLogin()
    {
        return login;
    }

    public void addParent(MainWindow p)
    {
        parent = p;
    }

    public void setDiversion(TimeRegion timeRegion)
    {
        this.diversion = timeRegion;
    }

    /**
     * Create a new document on the Bot's side.
     * 
     * @param name The name of the document to create.
     * @param path The path of the folder to create the document in relative to the root, use backslash to separate dirs.
     * @param contents Contents of the file, if null a blank file is created.
     * 
     * @author Andrew
     */
    public void createDocument( String name, String path, String contents )
    {
        if( DEBUG )
            System.out.println("Trying to create document with name " + name + " in " + path);
        
        try
        {
            Message msg = new Message();
            msg.setBody("");
            msg.setProperty( "name", name );
            msg.setProperty( "path", path );
            msg.setProperty( "contents", contents );
            botChat.sendMessage( msg );
        }
        catch (XMPPException e)
        {
            
            e.printStackTrace();
        }
    }
    
    /**
     * Test importing files and creating them on the Bot, use the CIDER directory as source.
     * Demonstrates how to parse path etc.
     * 
     * @author Andrew
     */
    public void testCreation()
    {
        ImportFiles imp = null;
        try
        {
            imp = new ImportFiles( "src\\cider\\common\\network" );
        }
        catch (FileNotFoundException e1)
        {
            e1.printStackTrace();
        }
        catch (IOException e1)
        {
            e1.printStackTrace();
        }
        HashMap<String,String> files = imp.getFiles();
        Iterator<Entry<String,String>> itr = files.entrySet().iterator();
        
        while( itr.hasNext() )
        {
            Entry<String,String> i = itr.next();
            File fullPath = new File( i.getKey() );
            createDocument( fullPath.getName(), fullPath.getParent(), i.getValue() );
        }
        
    }
    
    /**
     * Send idle presence.
     * 
     * @author Andrew
     */
    public void sendIdlePresence()
    {
        Presence idle = new Presence( Presence.Type.available );
        idle.setMode( Presence.Mode.away );
        idle.setTo( chatroom.getRoom() );
        connection.sendPacket( idle );
    }

    /**
     * Send back presence.
     * 
     * @author Andrew
     */
    public void sendHerePresence()
    {
        Presence here = new Presence( Presence.Type.available );
        here.setMode( Presence.Mode.available );
        here.setTo( chatroom.getRoom() );
        connection.sendPacket( here );
        
    }
}
