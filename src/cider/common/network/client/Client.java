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
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
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
import org.jivesoftware.smackx.muc.MultiUserChat;

import cider.client.gui.CiderApplication;
import cider.client.gui.CloseButton;
import cider.client.gui.ETASourceEditorPane;
import cider.client.gui.LoginUI;
import cider.client.gui.MainWindow;
import cider.common.network.ConfigurationReader;
import cider.common.processes.DocumentProperties;
import cider.common.processes.EventComparer;
import cider.common.processes.ImportFiles;
import cider.common.processes.LiveFolder;
import cider.common.processes.Profile;
import cider.common.processes.SiHistoryFiles;
import cider.common.processes.SourceDocument;
import cider.common.processes.TimeRegion;
import cider.common.processes.TypingEvent;
import cider.common.processes.TypingEventMode;
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
    // private JingleManager jm;
    // private JingleSession incoming = null;
    // private JingleSession outgoing = null;

    // The current user's profile
    public Profile profile;
    public static HashMap<String, Color> colours = new HashMap<String, Color>();
    public Color incomingColour;

    public static ArrayList<String> usersIdle = new ArrayList<String>();

    public DocumentProperties currentDocumentProperties;

    protected HashMap<JScrollPane, Object> tabsToChats = new HashMap<JScrollPane, Object>();

    // GUI components shared with MainWindow
    public ClientSharedComponents shared;

    // Lawrence's source document stuff
    private boolean autoUpdate = false;
    private LiveFolder liveFolder = null;
    private long lastBroadcast = 0;
    private static final long minimumBroadcastDelay = 400;
    private Message outgoingTypingEvents;
    private Timer broadcastTimer = new Timer();
    private boolean isWaitingToBroadcast = false;
    private SourceDocument currentDoc = null;
    private long clockOffset = 0;
    private PriorityQueue<Long> timeDeltaList = new PriorityQueue<Long>();
    // FIXME: synchronised is never read!
    @SuppressWarnings("unused")
    private boolean synchronised = false;
    private TimeRegion typingEventDiversion;

    public Client(String username, String password, String host, int port,
            String serviceName, LoginUI log, ClientSharedComponents shared)
    {
        clientConfig = new ConfigurationReader("Client.conf", log);

        // Assign objects from parameters
        this.username = username;
        chatroomName = clientConfig.getChatroomName() + "@conference."
                + serviceName;
        this.host = host;
        this.port = port;
        this.serviceName = serviceName;
        this.password = password;
        login = log;

        // Set blank outgoingTypingEvents
        resetOutgoingEvents();

        // GUI Components shared with MainWindow
        this.shared = shared;
        profile = shared.profile;

        EditorTypingArea.addParent(this);
    }

    /**
     * Initiate handlers for VoIP chat.
     * 
     * @author Andrew
     */
    // private void initJingle()
    // {
    // ICETransportManager icetm0 = new ICETransportManager(connection,
    // clientConfig.getStunServer(), clientConfig.getStunPort());
    // List<JingleMediaManager> mediaManagers = new
    // ArrayList<JingleMediaManager>();
    // // mediaManagers.add(new JmfMediaManager(icetm0));
    // mediaManagers.add(new SpeexMediaManager(icetm0));
    // mediaManagers.add(new ScreenShareMediaManager(icetm0));
    // jm = new JingleManager(connection, mediaManagers);
    // jm.addCreationListener(icetm0);
    //
    // jm.addJingleSessionRequestListener(new JingleSessionRequestListener()
    // {
    // public void sessionRequested(JingleSessionRequest request)
    // {
    //
    // // if (incoming != null)
    // // return;
    //
    // try
    // {
    // // Accept the call
    // incoming = request.accept();
    //
    // // Start the call
    // incoming.startIncoming();
    // }
    // catch (XMPPException e)
    // {
    // e.printStackTrace();
    // }
    //
    // }
    // });
    // }

    public void addParent(MainWindow p)
    {
        parent = p;
    }

    public void addTimeDeltaSample(long latency)
    {
        timeDeltaList.add(latency);
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
        // Prints out every packet received by the client, used when you want
        // very verbose debugging
        // connection.addPacketListener(new DebugPacketListener(),
        // new DebugPacketFilter());
        // connection.addPacketInterceptor(new DebugPacketInterceptor(),
        // new DebugPacketFilter());

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

        chatmanager = connection.getChatManager();

        // Establish chat session with the bot
        botChatListener = new ClientMessageListener(this);
        botChat = chatmanager.createChat(clientConfig.getBotUsername() + "@"
                + serviceName, botChatListener);

        // Listen for invitation to chatroom and set up message listener for it
        chatroom = new MultiUserChat(connection, chatroomName);
        MultiUserChat.addInvitationListener(connection,
                new ClientChatroomInviteListener(chatroom, username, this));

        chatroom.addParticipantListener(new ClientChatroomPresenceListener(
                shared.userListModel, shared.userCount, this, clientConfig
                        .getBotUsername(), clientConfig.getCheckerUsername()));

        chatroom.addMessageListener(new ClientChatroomMessageListener(this));

        // Add listener for new user chats
        userChatListener = new ClientPrivateChatListener(this, clientConfig
                .getChatroomName(), clientConfig.getBotUsername());
        chatmanager.addChatListener(userChatListener);

        // Initiate voice chat stuff
        // initJingle();

        // Check the bot is online
        Message msg = new Message();
        msg.setBody("");
        msg.setProperty("ciderAction", "are you online mr bot");
        botChat.sendMessage(msg);
        botChatListener.clientThread = Thread.currentThread();
        try
        {
            Thread.sleep(5000);
        }
        catch (InterruptedException e)
        {
            if (CiderApplication.debugApp)
                System.out
                        .println("Bot replied within time out, thread sleep interrupted.");
        }
        return botIsOnline;
    }

    public void broadcastTypingEvents(Queue<TypingEvent> typingEvents,
            String path)
    {
        int i = 0;

        // Find first position to append typing events
        while (outgoingTypingEvents.getProperty("te" + i) != null)
            i++;

        for (TypingEvent te : typingEvents)
        {
            outgoingTypingEvents.setProperty("path" + i, path);
            // So we can send newlines properly, encode it
            outgoingTypingEvents.setProperty("te" + i, StringUtils
                    .encodeBase64(te.pack()));
            i++;
        }
        try
        {
            long currentTime = System.currentTimeMillis()
                    + this.getClockOffset();
            long interval = currentTime - lastBroadcast;
            if (!isWaitingToBroadcast)
            {
                if (interval < minimumBroadcastDelay)
                {
                    isWaitingToBroadcast = true;
                    broadcastTimer.schedule(new TimerTask()
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

                                chatroom.sendMessage(outgoingTypingEvents);
                                resetOutgoingEvents();
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
                    chatroom.sendMessage(outgoingTypingEvents);
                    resetOutgoingEvents();
                    lastBroadcast = System.currentTimeMillis();
                }
            }
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

    /**
     * Close a chat with the specified user.
     * 
     * @author Andrew
     * @param user
     */
    public void closeChat(String user)
    {
        userChatListener.closeChat(user);
    }

    /**
     * Add a chat tab with the specified username as its name to the GUI.
     * 
     * @param user
     * @author Andrew
     * @return The new tab created, labelled with the username it corresponds
     *         to.
     */
    public JScrollPane createChatTab(final String user)
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

        // /* START OF CLOSE TAB BUTTON EDIT
        // ----------------------------------------
        CloseButton tabCloseButton;
        if (user.equals("Group Chat"))
            tabCloseButton = new CloseButton(user, false);
        else
            tabCloseButton = new CloseButton(user, true);
        tabCloseButton.setName(user);
        ActionListener al;
        al = new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                JButton btn = (JButton) ae.getSource();
                closeChat(btn.getName());
            }
        };
        tabCloseButton.addActionListener(al);

        JPanel pnl = new JPanel();
        pnl.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));
        pnl.setOpaque(false);
        JLabel lblUser = new JLabel(user);
        lblUser.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 6));
        pnl.add(lblUser);
        pnl.add(tabCloseButton);
        shared.receiveTabs.add(messageReceiveBoxScroll);
        shared.receiveTabs.tabflash(user);
        shared.receiveTabs.setTabComponentAt(
                shared.receiveTabs.getTabCount() - 1, pnl);
        // END OF CLOSE TAB BUTTON EDIT
        // ----------------------------------------- */

        // If creating a tab for the chatroom, register the chatroom message
        // receive box
        if (user.equals(MainWindow.GROUPCHAT_TITLE))
        {
            chatroomMessageReceiveBox = messageReceiveBox;
            tabsToChats.put(messageReceiveBoxScroll, chatroom);
        }

        return messageReceiveBoxScroll;
    }

    /**
     * Create a new document on the Bot's side.
     * 
     * @param name
     *            The name of the document to create.
     * @param path
     *            The path of the folder to create the document in relative to
     *            the root, use dorward slash to separate dirs.
     * @param contents
     *            Contents of the file, if null a blank file is created.
     * 
     * @author Andrew
     */
    public void createDocument(String name, String path, String contents)
    {
        if (DEBUG)
            System.out.println("Trying to create document with name " + name
                    + " in " + path);

        try
        {
        	if( path.contains( "\\" ) )
        		path = path.replaceAll( "\\\\", "/" );
            Message msg = new Message();
            msg.setBody("");
            msg.setProperty("ciderAction", "createDoc");
            msg.setProperty("name", name);
            msg.setProperty("path", path);
            msg.setProperty("contents", contents);
            botChat.sendMessage(msg);
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

    public long getClockOffset()
    {
        // FIXME
        // return this.clockOffset;
        return 0;

    }

    // TODO: Below is all Lawrence's stuff so he needs to comment it!

    public SourceDocument getCurrentDocument()
    {
        return currentDoc;// .playOutEvents(Long.MAX_VALUE).countCharactersFor("user1");
    }

    public DocumentProperties getCurrentDocumentID()
    {
        return currentDocumentProperties;
    }

    public void getFileListFromBot()
    {
        try
        {
            Message msg = new Message();
            msg.setBody("");
            msg.setProperty("ciderAction", "getfilelist");
            botChat.sendMessage(msg);
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

    public LiveFolder getLiveFolder()
    {
        return liveFolder;
    }

    public LoginUI getLogin()
    {
        return login;
    }

    public MainWindow getParent()
    {
        return parent;
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

    public String getUsername()
    {
        return username;
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
        if (userChatListener.usersToChats.containsKey(user)
                || user.equals(username))
        {
            if (DEBUG)
                System.out
                        .println("Chat with "
                                + user
                                + " already exists or trying to chat to self, not creating one...");
            return;
        }

        chatmanager.createChat(user + "@" + serviceName, null);

        if (DEBUG)
            System.out.println("Chat initiated with " + user);
    }

    public void openTab(String strPath)
    {
        SourceDocument doc = liveFolder.path(strPath);
        currentDoc = doc;
        if (!shared.openTabs.containsKey(strPath))
        {
            EditorTypingArea eta = new EditorTypingArea(doc);
            ETASourceEditorPane sourceEditor = new ETASourceEditorPane(eta,
                    this, strPath);

            sourceEditor.setName(strPath);

            // START OF CLOSE TABS
            CloseButton tabCloseButton = new CloseButton(strPath, true);

            ActionListener al;
            al = new ActionListener()
            {
                public void actionPerformed(ActionEvent ae)
                {
                    JButton btn = (JButton) ae.getSource();
                    String s1 = btn.getActionCommand();

                    System.out.println("Close " + s1);

                    shared.openTabs.get(s1).close();
                    shared.openTabs.remove(s1);
                    currentDoc = null;
                    // shared.tabbedPane.setSelectedIndex( --parent.currentTab
                    // );
                    shared.tabbedPane.remove(shared.tabbedPane.indexOfTab(s1));
                }
            };
            tabCloseButton.addActionListener(al);

            JPanel pnl = new JPanel();
            pnl.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));
            pnl.setOpaque(false);
            JLabel lblUser = new JLabel(strPath);
            lblUser.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 6));
            pnl.add(lblUser);
            pnl.add(tabCloseButton);

            // END

            sourceEditor.setTabHandle(shared.tabbedPane.add(sourceEditor));
            shared.tabbedPane.setTabComponentAt(
                    shared.tabbedPane.getTabCount() - 1, pnl);
            shared.tabbedPane.setSelectedIndex(shared.tabbedPane.getTabCount() - 1);
            
            shared.openTabs.put(strPath, sourceEditor);
            this.pullEventsFromBot(strPath, System.currentTimeMillis()
                    + this.getClockOffset(), true);
            currentDocumentProperties = new DocumentProperties(doc.name,
                    strPath);
            currentDocumentProperties.creationTime = doc.getCreationTime();
        }
        else
        {
        	shared.tabbedPane.setSelectedIndex(shared.tabbedPane.indexOfTab(strPath));
        }
    }

    public void openTabFor(Object[] path)
    {
        String strPath = this.getPathToSourceDocument(path, 1);
        System.out.println(strPath);
        openTab(strPath);

    }

    /**
     * Process a colour change message.
     * 
     * @author Jon, Andrew
     * 
     */
    public void processColourchange(Message msg)
    {
        String changedUser = (String) msg.getProperty("username");
        Integer r = (Integer) msg.getProperty("r");
        Integer g = (Integer) msg.getProperty("g");
        Integer b = (Integer) msg.getProperty("b");
        Color newColour = new Color(r, g, b);
        System.out.println("Client: Changing colour for " + username + " to "
                + r + " " + g + " " + b);

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
     * 
     * @param msg
     * @return True if this was a document related message
     */
    public boolean processDocumentMessages(Message msg)
    {
        String ciderAction = (String) msg.getProperty("ciderAction");
        if (ciderAction.equals("pushto"))
        {
            processPushto(msg);
            return true;
        }
        else if (ciderAction.equals("pushtoPlain"))
        {
            processPushtoPlain(msg);
            return true;
        }
        else if (ciderAction.equals("filelist"))
        {
            processFilelist(msg);
            return true;
        }
        else if (ciderAction.equals("isblank"))
        {
            processIsblank(msg);
            return true;
        }
        else if (ciderAction.equals("colourchange"))
        {
            processColourchange(msg);
            return true;
        }
        return false;
    }

    /**
     * Process incoming filelist
     * 
     * @param msg
     * 
     * @author Andrew
     */
    public void processFilelist(Message msg)
    {
        String xml = new String(StringUtils.decodeBase64((String) msg
                .getProperty("xml")));
        shared.dirView.constructTree(xml);
        this.setLiveFolder(shared.dirView.getLiveFolder());

        // TODO: Redundant
        this.setUpdatesAutomatically(true);

        shared.dirView.refresh();

        // Below is possibly unnecessary
        shared.dirView.repaint();
        shared.dirView.updateUI();
        parent.dirSourceEditorSelectionSplit.repaint();
        parent.dirSourceEditorSelectionSplit.updateUI();
    }

    public void processIsblank(Message msg)
    {
        String dest = (String) msg.getProperty("path");
        EditorTypingArea eta = shared.openTabs.get(dest).getEditorTypingArea();
        eta.setWaiting(false);
    }

    /**
     * Process pushto
     * 
     * @param msg
     * 
     * @author Lawrence, Andrew
     */
    public void processPushto(Message msg)
    {
        Hashtable<String, Queue<TypingEvent>> queues = new Hashtable<String, Queue<TypingEvent>>();
        boolean stopDiversion = false;
        int eventNum = 0;
        String dest = "", te;
        // Loop until we've processed all events in the message
        for (; msg.getProperty("te" + eventNum) != null; eventNum++)
        {
            // If destination for this event isn't null change it
            if (msg.getProperty("path" + eventNum) != null)
            {
                dest = (String) msg.getProperty("path" + eventNum);
                dest = dest.replace("root\\", "");
            }

            te = new String(StringUtils.decodeBase64((String) msg
                    .getProperty("te" + eventNum)));

            if (te.equals("end"))
                stopDiversion = true;
            else
            {
                Queue<TypingEvent> queue = queues.get(dest);
                if (queue == null)
                {
                    queue = new LinkedList<TypingEvent>();
                    queues.put(dest, queue);
                }
                queue.add(new TypingEvent(te));
            }
        }

        for (Entry<String, Queue<TypingEvent>> entry : queues.entrySet())
            this.push(entry.getValue(), entry.getKey());

        if (stopDiversion)
        {
            typingEventDiversion.finishedUpdate();
            typingEventDiversion = null;
        }
    }

    /**
     * Process a 'plain' push to, which is simply a document in plain text.
     * Convert it to a queue of homogenized events.
     * 
     * @param msg
     */
    private void processPushtoPlain(Message msg)
    {
        Queue<TypingEvent> events = new LinkedList<TypingEvent>();
        String contents = (String) msg.getProperty("contents");
        long time = System.currentTimeMillis() - contents.length();

        TypingEvent whole = new TypingEvent(time, TypingEventMode.homogenized,
                0, contents.length(), contents, null, null);
        events.addAll(whole.explode());
        push(events, (String) msg.getProperty("path"));
    }

    public void pullEventsFromBot(String strPath, long time, boolean simplified)
    {
        try
        {
            // System.out.println("pull since " + time);
            Message msg = new Message();
            msg.setBody("");
            if (simplified)
                msg.setProperty("ciderAction", "pullSimplifiedEvents");
            else
                msg.setProperty("ciderAction", "pullEvents");
            msg.setProperty("path", strPath);
            msg.setProperty("time", String.valueOf(time));
            botChat.sendMessage(msg);
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
            msg.setProperty("ciderAction", "pullEvents");
            msg.setProperty("path", strPath);
            msg.setProperty("startTime", String.valueOf(startTime));
            msg.setProperty("endTime", String.valueOf(endTime));
            msg
                    .setProperty("stopDiversion", (stopDiversion ? "true"
                            : "false"));
            botChat.sendMessage(msg);
        }
        catch (XMPPException e)
        {
            e.printStackTrace();
        }
    }

    public void push(Queue<TypingEvent> typingEvents, String dest)
    {
        if (currentDocumentProperties == null)
        {
            if (CiderApplication.debugApp)
                System.out
                        .println("Ignoring typing events when current document id is null");
        }
        else
        {
            PriorityQueue<TypingEvent> remainingEvents;

            remainingEvents = new PriorityQueue<TypingEvent>(typingEvents
                    .size(), new EventComparer());
            for (TypingEvent typingEvent : typingEvents)
            {
                if (typingEventDiversion != null
                        && typingEventDiversion.end.time > typingEvent.time)
                    typingEventDiversion.end.typingEvents.add(typingEvent);
                else
                    remainingEvents.add(typingEvent);
                System.out.println("Push " + typingEvent + " to " + dest);
            }

            EditorTypingArea eta = shared.openTabs.get(dest)
                    .getEditorTypingArea();
            int position = eta.getCaretPosition();
            TypingEvent anchor;
            if (position >= 0 && position < eta.getTypingEventList().length())
                anchor = eta.getTypingEventList().get(position);
            else
                anchor = null;

            SiHistoryFiles.saveEvents(new PriorityQueue<TypingEvent>(
                    remainingEvents), currentDocumentProperties.path);
            eta.getSourceDocument().push(remainingEvents);
            eta.setWaiting(false);
            eta.updateText();
            if (anchor != null)
                eta.setCaretPosition(eta.getTypingEventList()
                        .getLastPositionOf(anchor));
        }
    }

    /**
     * Reset the outgoing typing events to blank
     * 
     * @author Andrew
     */
    private void resetOutgoingEvents()
    {
        outgoingTypingEvents = new Message();
        outgoingTypingEvents.setBody("");
        outgoingTypingEvents.setProperty("ciderAction", "pushto");
        outgoingTypingEvents.setType(Message.Type.groupchat);
        outgoingTypingEvents.setTo(chatroomName);
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

            if (shared.receiveTabs.getSelectedComponent().getName().equals(
                    MainWindow.GROUPCHAT_TITLE))
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
                msg.setType(Message.Type.groupchat);
                chatroom.sendMessage(msg);
                // TODO: need to escape?
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
                // TODO: need to escape?

                // Update the log with the message before it was encoded
                updatePrivateChatLog(username, msg.getSubject(), message);
            }
        }
        catch (XMPPException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Send back presence.
     * 
     * @author Andrew
     */
    public void sendHerePresence()
    {
        Presence here = new Presence(Presence.Type.available);
        here.setMode(Presence.Mode.available);
        here.setTo(chatroom.getRoom());
        connection.sendPacket(here);
    }

    /**
     * Send idle presence.
     * 
     * @author Andrew
     */
    public void sendIdlePresence()
    {
        Presence idle = new Presence(Presence.Type.available);
        idle.setMode(Presence.Mode.away);
        idle.setTo(chatroom.getRoom());
        connection.sendPacket(idle);
    }

    public void setDiversion(TimeRegion timeRegion)
    {
        typingEventDiversion = timeRegion;
    }

    public void setLiveFolder(LiveFolder liveFolder)
    {
        this.liveFolder = liveFolder;
    }

    public void setTimeDelta(long delta)
    {
        clockOffset = -delta;
    }

    public void setUpdatesAutomatically(boolean autoUpdate)
    {
        this.autoUpdate = autoUpdate;
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
                if (requestsMade < 5)
                {
                    timeRequest();
                    requestsMade++;
                    progressMonitor.setProgress(requestsMade);
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
            msg
                    .setProperty("ciderAction",
                            "You play 2 hours to die like this?");
            botChat.sendMessage(msg);
        }
        catch (XMPPException e)
        {

            e.printStackTrace();
        }
    }

    /**
     * Test importing files and creating them on the Bot, use the CIDER
     * directory as source. Demonstrates how to parse path etc.
     * 
     * @author Andrew
     */
    public void testCreation()
    {
        ImportFiles imp = null;
        try
        {
            imp = new ImportFiles("src\\cider\\common\\network");
        }
        catch (FileNotFoundException e1)
        {
            e1.printStackTrace();
        }
        catch (IOException e1)
        {
            e1.printStackTrace();
        }
        HashMap<String, String> files = imp.getFiles();
        Iterator<Entry<String, String>> itr = files.entrySet().iterator();

        while (itr.hasNext())
        {
            Entry<String, String> i = itr.next();
            File fullPath = new File(i.getKey());
            createDocument(fullPath.getName(), fullPath.getParent(), i
                    .getValue());
        }

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

    public void updateClockOffset()
    {
        int mid = timeDeltaList.size() / 2;
        int i = mid;
        while (i-- > 0)
            timeDeltaList.poll();

        clockOffset = timeDeltaList.peek();
        System.out.println("Clock offset set to " + clockOffset);
        timeDeltaList.clear();
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

    public boolean updatesAutomatically()
    {
        return autoUpdate;
    }
}
