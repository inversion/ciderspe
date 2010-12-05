package cider.common.network;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

/**
 * This is the class that implements the bot that connects to the XMPP server
 * and listens for new chats.
 * 
 * @author Andrew
 */

public class Server implements ChatManagerListener
{

    private XMPPConnection connection;
    private ChatManager chatmanager;

    /*
     * Server(String username, String password) { this.connection = new
     * XMPPConnection(config); this.connection.connect();
     * this.connection.login(username, password) }
     */

    public Server()
    {
        try
        {
            // Connect and login to the XMPP server
            ConnectionConfiguration config = new ConnectionConfiguration(
                    Common.HOST, Common.PORT, Common.SERVICE_NAME);
            connection = new XMPPConnection(config);
            connection.connect();
            connection.login(Common.BOT_USERNAME, Common.BOT_PASSWORD);

            if (Common.DEBUG)
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

    @Override
    public void chatCreated(Chat chat, boolean createdLocally)
    {

        if (Common.DEBUG)
            System.out.println(chat.getParticipant() + " connected...");

        chat.addMessageListener(new ServerMessageListener());
    }

}
