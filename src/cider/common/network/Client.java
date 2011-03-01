package cider.common.network;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.muc.MultiUserChat;

import cider.client.gui.DirectoryViewComponent;
import cider.client.gui.SourceEditor;
import cider.common.processes.LiveFolder;
import cider.common.processes.SourceDocument;
import cider.common.processes.TypingEvent;
import cider.specialcomponents.Base64;
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
    public static final String RESOURCE = "CIDER";
    public final DateFormat dateFormat = new SimpleDateFormat(
            "dd/MM/yyyy HH:mm:ss");

    private XMPPConnection connection;
    private ChatManager chatmanager;
    private boolean autoUpdate = false;
    private LiveFolder liveFolder = null;
    private JTabbedPane tabbedPane;
    private long lastUpdate = 0;
    private Hashtable<String, SourceEditor> openTabs;
    private long lastBroardcast = 0;
    private static final long minimumBroadcastDelay = 0;
    private String outgoingTypingEvents = "";
    private Timer broardcastTimer = new Timer();

    private DirectoryViewComponent dirView;

    // Chat session with the Bot
    public Chat botChat;
    private ClientMessageListener botChatListener;
    private String username;
    private String host;
    private int port;
    private String serviceName;
    private String password;

    // Listen for private chat sessions with other users
    // TODO: Not yet implemented
    // private ClientPrivateChatListener userChatListener;

    // Chatroom
    private String chatroomName;
    private MultiUserChat chatroom;

    private JTextArea messageReceiveBox;
    private boolean isWaitingToBroadcast = false;
    private SourceDocument currentDoc = null;

    public Client(String username, String password, String host, int port,
            String serviceName)
    {

        // Assign objects from parameters
        this.username = username;
        this.chatroomName = "ciderchat" + "@conference." + serviceName;
        this.host = host;
        this.port = port;
        this.serviceName = serviceName;
        this.password = password;
    }

    public SourceDocument getCurrentDocument()
    {
        return this.currentDoc;
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
            JTextArea messageReceiveBox)
    {
        this.dirView = dirView;
        this.tabbedPane = tabbedPane;
        this.openTabs = openTabs;
        this.messageReceiveBox = messageReceiveBox;

        chatroom.addMessageListener(new ClientChatroomMessageListener(this));
        chatroom.addParticipantListener(new ClientChatroomParticipantListener(
                userListModel, userCount));
    }

    /**
     * Returns TRUE on successful connection, FALSE on failure
     * 
     * Edited by Andrew to throw the exception.
     * 
     * @author Jon
     */
    public void attemptConnection() throws XMPPException
    {
        // Connect and login to the XMPP server
        ConnectionConfiguration config = new ConnectionConfiguration(host,
                port, serviceName);
        connection = new XMPPConnection(config);
        connection.connect();
        /**
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
        connection.addPacketListener(new DebugPacketListener(),
                new DebugPacketFilter());

        // Add listener for new user chats
        chatmanager = this.connection.getChatManager();
        // userChatListener = new ClientPrivateChatListener( userListModel );
        // chatmanager.addChatListener(userChatListener);

        // Establish chat session with the bot
        botChatListener = new ClientMessageListener(this);
        botChat = chatmanager.createChat(Bot.BOT_USERNAME + "@" + serviceName,
                botChatListener);

        // Listen for invitation to chatroom and set up message listener for it
        chatroom = new MultiUserChat(connection, chatroomName);
        MultiUserChat.addInvitationListener(connection,
                new ClientChatroomInviteListener(chatroom, username));

    }

    public void updateChatLog(String username, String date, String message)
    {

        // messageReceiveBox.setContentType("text/html");
        // String oldText = messageReceiveBox.getText();
        // messageReceiveBox.setText("<html>" + "<b>" + username + "</b>" + " ("
        // + dateFormat.format(date) + "):<br>" + message + "<br></html>");

        // messageReceiveBox.append(username + " (" + dateFormat.format(date) +
        // "):\n");
        System.out.println(StringUtils.parseResource(username) + "\n" + date
                + "\n" + message);
        messageReceiveBox.append(StringUtils.parseResource(username) + " ("
                + date + "):\n" + message + "\n");
    }

    public void sendMessageChatroom(String message)
    {
        try
        {
            Date date = new Date();
            Message msg = chatroom.createMessage();
            msg.setBody(Base64.encodeBytes(message.getBytes()));
            msg.setSubject(dateFormat.format(date));
            chatroom.sendMessage(msg);
        }
        catch (XMPPException e)
        {
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
        try
        {
            botChat.sendMessage(Base64.encodeBytes("quit".getBytes()));
        }
        catch (XMPPException e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        chatroom.leave();
        connection.disconnect();
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
            botChat.sendMessage(Base64.encodeBytes(("pullSimplifiedEvents("
                    + strPath + "," + String.valueOf(time) + ")").getBytes()));
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
            botChat.sendMessage(Base64.encodeBytes(("pullEvents(" + strPath
                    + "," + String.valueOf(time) + ")").getBytes()));
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

                                chatroom.sendMessage(Base64
                                        .encodeBytes(outgoingTypingEvents
                                                .getBytes()));
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
                    this.chatroom.sendMessage(Base64
                            .encodeBytes(this.outgoingTypingEvents.getBytes()));
                    this.outgoingTypingEvents = "";
                    this.lastBroardcast = System.currentTimeMillis();
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
            botChat.sendMessage(Base64.encodeBytes("getfilelist".getBytes()));
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
        TypingEvent anchor = eta.getTypingEventList().get(
                eta.getCaretPosition());
        eta.getCodeLocation().push(typingEvents);
        eta.setWaiting(false);
        eta.updateText();
        eta.setCaretPosition(eta.getTypingEventList().getLastPositionOf(anchor));
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
            // Bad horrible terrible dumb hack!!!!
            // TODO:
            // Need to make separate queues for different destinations
            // Make sure the document is only repainted once.
            String[] instructions = body.split(" -> ");
            for (String instruction : instructions)
            {
                String[] preAndAfter = instruction.split("\\) ");
                String[] pre = preAndAfter[0].split("\\(");
                String dest = pre[1];
                dest = dest.replace("root\\", "");
                Queue<TypingEvent> typingEvents = new LinkedList<TypingEvent>();
                typingEvents.add(new TypingEvent(preAndAfter[1]));
                System.out.println("Push " + preAndAfter[1] + " to " + dest);
                this.push(typingEvents, dest);
            }

        }
        else if (body.startsWith("isblank("))
        {
            String dest = body.split("\\(")[1].split("\\)")[0];
            EditorTypingArea eta = this.openTabs.get(dest)
                    .getEditorTypingArea();
            eta.setWaiting(false);
        }
    }

    public void terminateBotRemotely()
    {
        try
        {
            this.botChat.sendMessage(Base64
                    .encodeBytes("Sir, blame it on your ISP".getBytes()));
        }
        catch (XMPPException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
