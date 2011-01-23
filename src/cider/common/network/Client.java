package cider.common.network;

import java.util.Hashtable;

import javax.swing.JTabbedPane;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import cider.client.gui.DirectoryViewComponent;
import cider.common.processes.LiveFolder;
import cider.common.processes.SourceDocument;
import cider.specialcomponents.Base64;
import cider.specialcomponents.EditorTypingArea;

/**
 * 
 * This implements the client side of the XMPP layer, it has methods to get a
 * file from the server and return its contents as a String.
 * 
 * 
 * @author Andrew + Lawrence
 */

public class Client
{

    private XMPPConnection connection;
    private ChatManager chatmanager;
    private ClientMessageListener listener;
    private Chat chat;
    private boolean autoUpdate = false;
    private LiveFolder liveFolder = null;
    private JTabbedPane tabbedPane;
    private Hashtable<String, EditorTypingArea> openTabs = new Hashtable<String, EditorTypingArea>();

    public Client(DirectoryViewComponent dirView, JTabbedPane tabbedPane)
    {
        try
        {
            // Connect and login to the XMPP server
            ConnectionConfiguration config = new ConnectionConfiguration(
                    Common.HOST, Common.PORT, Common.SERVICE_NAME);
            this.connection = new XMPPConnection(config);
            this.connection.connect();
            this.connection.login(Common.CLIENT_USERNAME,
                    Common.CLIENT_PASSWORD);

            if (Common.DEBUG)
            {
                System.out.println("Client connected="
                        + this.connection.isConnected());
                System.out.println("Client username="
                        + this.connection.getUser());
            }

            this.chatmanager = this.connection.getChatManager();
            this.listener = new ClientMessageListener(dirView, this);
            this.chat = this.chatmanager.createChat(Common.BOT_USERNAME,
                    listener);
            this.tabbedPane = tabbedPane;
        }
        catch (XMPPException e)
        {
            e.printStackTrace();
        }
    }

    public boolean updatesAutomatically()
    {
        return this.autoUpdate;
    }

    @Deprecated
    public void getFile(String path)
    {
        try
        {
            chat.sendMessage("getfile=" + Base64.encodeBytes(path.getBytes()));
        }
        catch (XMPPException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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
        if (!this.openTabs.containsKey(path))
        {
            EditorTypingArea eta = new EditorTypingArea(doc);
            eta.setTabHandle(this.tabbedPane.add(strPath, eta));
            this.openTabs.put(strPath, eta);
        }
    }

    public void pullEventsSince(long time)
    {
        try
        {
            chat.sendMessage("pullEventsSince(" + String.valueOf(time) + ")");
        }
        catch (XMPPException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void getFileList()
    {
        try
        {
            chat.sendMessage("getfilelist");
        }
        catch (XMPPException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Deprecated
    public void putFile(String path, String contents)
    {
        try
        {
            chat.sendMessage("<putfile><path>"
                    + Base64.encodeBytes(path.getBytes()) + "</path><contents>"
                    + Base64.encodeBytes(contents.getBytes())
                    + "</contents></putfile>");
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
}
