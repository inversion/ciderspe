package cider.common.network;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.ProgressMonitor;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.muc.MultiUserChat;

import cider.client.gui.DirectoryViewComponent;
import cider.client.gui.LoginUI;
import cider.client.gui.MainWindow;
import cider.client.gui.SourceEditor;
import cider.common.processes.LiveFolder;
import cider.common.processes.Profile;
import cider.common.processes.SourceDocument;
import cider.common.processes.TypingEvent;
import cider.specialcomponents.EditorTypingArea;

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
	
	private MainWindow parent;
	private LoginUI login;

    private static final boolean DEBUG = true;
    public static final String RESOURCE = "CIDER";
    public final DateFormat dateFormat = new SimpleDateFormat(
            "dd/MM/yyyy HH:mm:ss");

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

    // Multi user chatroom
    private String chatroomName;
    public MultiUserChat chatroom;
    private JTextArea chatroomMessageReceiveBox;

    // Private chat sessions with other users
    private ClientPrivateChatListener userChatListener;
    protected HashMap<String, JTextArea> usersToAreas = new HashMap<String, JTextArea>();

    // The current user's profile
    public Profile profile = null;
    public boolean profileFound;
    public HashMap<String, Color> colours = new HashMap<String, Color>();
    public Color incomingColour;
    
    //FIXME: UNUSED VARIABLE
    //private ArrayList<ActionListener> als = new ArrayList<ActionListener>();

    /*
     * Abstract because it can be a private chat or multi user chat (chatroom)
     * and smack represents them as different types
     */
    protected HashMap<JScrollPane, Object> tabsToChats = new HashMap<JScrollPane, Object>();

    // GUI components
    public JTabbedPane receiveTabs;
    private DirectoryViewComponent dirView;

    // Lawrence's source document stuff
    private boolean autoUpdate = false;
    private LiveFolder liveFolder = null;
    private JTabbedPane tabbedPane;
    private long lastUpdate = 0;
    private Hashtable<String, SourceEditor> openTabs;
    private long lastBroardcast = 0;
    private static final long minimumBroadcastDelay = 400;
    private String outgoingTypingEvents = "";
    private Timer broardcastTimer = new Timer();
    private boolean isWaitingToBroadcast = false;
    private SourceDocument currentDoc = null;
    private PriorityQueue<Long> timeDeltaList = new PriorityQueue<Long>();
    private long clockOffset = 0;
    //FIXME: synchronised is never read!
    @SuppressWarnings("unused")
	private boolean synchronised = false;

    public Client(String username, String password, String host, int port,
            String serviceName, LoginUI log)
    {
        // Assign objects from parameters
        this.username = username;
        this.chatroomName = Bot.CHATROOM_NAME + "@conference." + serviceName;
        this.host = host;
        this.port = port;
        this.serviceName = serviceName;
        this.password = password;
        this.login = log;
        
        EditorTypingArea.addParent(this);
    }

    /**
     * Tries to connect to the XMPP server, throwing an exception if it fails in
     * any way.
     * 
     * @author Jon, Andrew
     */
    public void attemptConnection() throws XMPPException
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
        // connection.addPacketListener(new DebugPacketListener(), new
        // DebugPacketFilter());

        chatmanager = this.connection.getChatManager();

        // Establish chat session with the bot
        botChatListener = new ClientMessageListener(this);
        botChat = chatmanager.createChat(Bot.BOT_USERNAME + "@" + serviceName,
                botChatListener);

        // Listen for invitation to chatroom and set up message listener for it
        chatroom = new MultiUserChat(connection, chatroomName);
        MultiUserChat.addInvitationListener(connection,
                new ClientChatroomInviteListener(chatroom, username, this));

        // Add listener for new user chats
        userChatListener = new ClientPrivateChatListener(this);
        chatmanager.addChatListener(userChatListener);
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
            this.sendBotMessage("timeRequest(" + currentLocalTime + ")");
        }
        catch (XMPPException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public void addParent(MainWindow p)
    {
    	parent = p;
    }
    
    public MainWindow getParent()
    {
    	return parent;
    }
    
    public LoginUI getLogin()
    {
    	return login;
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
            sendBotMessage("You play 2 hours to die like this?");
        }
        catch (XMPPException e)
        {
            // TODO Auto-generated catch block
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
        try
        {
            // Tell the bot that this client is quitting
            sendBotMessage("quit");
        }
        catch (XMPPException e1)
        {
            e1.printStackTrace();
        }
        chatroom.leave();
        connection.disconnect();

        // TODO: Is below stuff necessary?
        while (this.connection.isConnected())
            System.out.printf(".");
        try
        {
            Thread.sleep(1000);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        // TODO: Is above stuff necessary?

        if (DEBUG)
            System.out.println("Disconnected from XMPP server...");
    }

    /**
     * Register GUI components from the Main Window
     * 
     * @author Andrew
     * 
     */
    public void registerGUIComponents(DirectoryViewComponent dirView,
            JTabbedPane tabbedPane, Hashtable<String, SourceEditor> openTabs,
            DefaultListModel userListModel, JLabel userCount,
            JTabbedPane receiveTabs)
    {
        this.dirView = dirView;
        this.tabbedPane = tabbedPane;
        this.openTabs = openTabs;
        this.receiveTabs = receiveTabs;

        chatroom.addMessageListener(new ClientChatroomMessageListener(this));
        chatroom.addParticipantListener(new ClientChatroomParticipantListener(
                userListModel, userCount));
    }

    /**
     * Update the chatroom message log with a new message.
     * 
     * @param The username of the user who sent the message
     * @param The date the message was originally sent (as Dateformat)
     * @param The message body.
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
     * @param The username of the user who sent the message
     * @param The date the message was originally sent (as Dateformat)
     * @param The message body.
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
            current = usersToAreas.get(receiveTabs.getSelectedComponent()
                    .getName());
        else
            current = usersToAreas.get(username);

        current.append(username + " (" + date + "):\n" + message + "\n");
        current.setCaretPosition(current.getDocument().getLength());
    }

    /**
     * Initiate a chat session with someone, essentially has no effect if the
     * chat already exists, or if you try to chat with yourself.
     * 
     * @param The user to initiate a chat with.
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
        receiveTabs.add(messageReceiveBoxScroll);
        receiveTabs.setTitleAt(receiveTabs.getTabCount() - 1, user);

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
     * Send a message to the bot, first encoding it to be sent over XMPP.
     * 
     * @author Andrew
     * @throws XMPPException
     */
    public void sendBotMessage(String message) throws XMPPException
    {
        botChat.sendMessage(StringUtils.encodeBase64(message));
    }

    /**
     * Send a message to the chatroom, first encoding it to be sent over XMPP.
     * 
     * @author Andrew
     * @throws XMPPException
     */
    public void sendChatroomMessage(String message) throws XMPPException
    {
        chatroom.sendMessage(StringUtils.encodeBase64(message));
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
            // TODO: Potentially a problem that this is creating a MUC message
            // then maybe sending it privately
            Message msg = chatroom.createMessage();
            msg.setBody(StringUtils.encodeBase64(message));
            msg.setSubject(dateFormat.format(date));

            if (receiveTabs.getSelectedComponent().getName()
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
                chatroom.sendMessage(msg);
            }
            else
            {
                if (DEBUG)
                    System.out
                            .println("Client: Sending message on private chat to "
                                    + receiveTabs.getSelectedComponent()
                                            .getName()
                                    + " contents: "
                                    + message);
                ((Chat) tabsToChats.get(receiveTabs.getSelectedComponent()))
                        .sendMessage(msg);

                // Update the log with the message before it was encoded
                updatePrivateChatLog(this.username, msg.getSubject(), message);
            }
        }
        catch (XMPPException e)
        {
            // TODO Auto-generated catch block
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
        if (!this.openTabs.containsKey(strPath))
        {
            EditorTypingArea eta = new EditorTypingArea(this.username, doc);
            SourceEditor sourceEditor = new SourceEditor(eta, this, strPath);
            sourceEditor.setTabHandle(this.tabbedPane.add(strPath, eta));
            this.openTabs.put(strPath, sourceEditor);
            this.pullSimplifiedEventsFromBot(strPath,
                    System.currentTimeMillis());
        }
    }

    public void pullSimplifiedEventsFromBot(String strPath, long time)
    {
        try
        {
            // System.out.println("pull since " + time);
            sendBotMessage("pullSimplifiedEvents(" + strPath + ","
                    + String.valueOf(time) + ")");
        }
        catch (XMPPException e)
        {
            // TODO:
            e.printStackTrace();
        }
    }

    public void pullEventsFromBot(String strPath, long time)
    {
        try
        {
            // System.out.println("pull since " + time);
            sendBotMessage("pullEvents(" + strPath + "," + String.valueOf(time)
                    + ")");
        }
        catch (XMPPException e)
        {
            // TODO:
            e.printStackTrace();
        }
    }

    public void broadcastTypingEvents(Queue<TypingEvent> typingEvents,
            String path)
    {
        for (TypingEvent te : typingEvents)
        {
            this.outgoingTypingEvents += "pushto(" + path + ") " + te.pack()
                    + " -> ";
        }
        try
        {
            long currentTime = System.currentTimeMillis();
            long interval = currentTime - this.lastBroardcast;
            if (!this.isWaitingToBroadcast)
            {
                if (interval < minimumBroadcastDelay)
                {
                    this.isWaitingToBroadcast = true;
                    this.broardcastTimer.schedule(new TimerTask()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                isWaitingToBroadcast = false;
                                long currentTime = System.currentTimeMillis();
                                if (currentTime - lastBroardcast < minimumBroadcastDelay)
                                    throw new Error(
                                            "Bug detected: broadcasting too soon");

                                sendChatroomMessage(outgoingTypingEvents);
                                outgoingTypingEvents = "";
                                lastBroardcast = System.currentTimeMillis();
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
                    sendChatroomMessage(this.outgoingTypingEvents);
                    this.outgoingTypingEvents = "";
                    this.lastBroardcast = System.currentTimeMillis();
                }
            }
        }
        catch (XMPPException e)
        {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    null, "Client failed to send message across bot chat: " + e.getMessage());
            System.exit(1);
        }
    }

    public void getFileListFromBot()
    {
        try
        {
            sendBotMessage("getfilelist");
        }
        catch (XMPPException e)
        {
			JOptionPane.showMessageDialog(
					new JPanel(), "Error retrieving file list: " + e.getMessage());
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
        EditorTypingArea eta = this.openTabs.get(dest).getEditorTypingArea();
        int position = eta.getCaretPosition();
        TypingEvent anchor;
        if (position >= 0 && position < eta.getTypingEventList().length())
            anchor = eta.getTypingEventList().get(position);
        else
            anchor = null;
        eta.getCodeLocation().push(typingEvents);
        eta.setWaiting(false);
        eta.updateText();
        if (anchor != null)
            eta.setCaretPosition(eta.getTypingEventList().getLastPositionOf(
                    anchor));

        if (eta.getLastUpdate() >= this.lastUpdate)
            this.lastUpdate = eta.getLastUpdate();
    }

    public long getLastUpdate()
    {
        return this.lastUpdate;
    }

    public void processDocumentMessages(String body)
    {
        if (body.startsWith("filelist="))
        {
            String xml = body.split("filelist=")[1];
            this.dirView.constructTree(xml);
            this.setLiveFolder(this.dirView.getLiveFolder());
            this.setUpdatesAutomatically(true);
        }
        else if (body.startsWith("pushto("))
        {
            String[] instructions = body.split(" -> ");
            Hashtable<String, Queue<TypingEvent>> queues = new Hashtable<String, Queue<TypingEvent>>();
            for (String instruction : instructions)
            {
                String[] preAndAfter = instruction.split("\\) ");
                String[] pre = preAndAfter[0].split("\\(");
                String dest = pre[1];
                dest = dest.replace("root\\", "");
                Queue<TypingEvent> queue = queues.get(dest);
                if (queue == null)
                {
                    queue = new LinkedList<TypingEvent>();
                    queues.put(dest, queue);
                }
                queue.add(new TypingEvent(preAndAfter[1]));
                System.out.println("Push " + preAndAfter[1] + " to " + dest);
            }

            for (Entry<String, Queue<TypingEvent>> entry : queues.entrySet())
                this.push(entry.getValue(), entry.getKey());

        }
        else if (body.startsWith("isblank("))
        {
            String dest = body.split("\\(")[1].split("\\)")[0];
            EditorTypingArea eta = this.openTabs.get(dest)
                    .getEditorTypingArea();
            eta.setWaiting(false);
        }
        else if (body.startsWith("colourchange:"))
        {
            String[] split = body.split(" ");
            String changedUser = split[1];
            Color newColour = new Color(Integer.parseInt(split[2]),
                    Integer.parseInt(split[3]), Integer.parseInt(split[4]));
            if (colours.containsKey(changedUser))
                colours.remove(changedUser);
            colours.put(changedUser, newColour);
            parent.userList.repaint();
            //EditorTypingArea.highlightMargin(); //FIXME update current line colour when user changes profile colour
        }
    }

    public void updateProfile(Profile p)
    {
        profile = p;
    }

    public Profile getProfile()
    {
        return profile;
    }
    
    public long getClockOffset()
    {
        return this.clockOffset;
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

        this.clockOffset = System.currentTimeMillis()
                - this.timeDeltaList.peek();
        System.out.println("Clock offset set to " + this.clockOffset);
        this.timeDeltaList.clear();
    }

    public void setTimeDelta(long delta)
    {
        this.clockOffset = System.currentTimeMillis() - delta;
    }
}

