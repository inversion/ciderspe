package cider.common.network;

import java.util.HashMap;
import java.util.Iterator;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.util.Base64;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.muc.MultiUserChat;

/**
 * 
 * Listens for new incoming chat sessions to the bot.
 * 
 * @author Andrew
 * 
 */

public class BotChatListener implements ChatManagerListener
{

    public static final boolean DEBUG = true;

    private MultiUserChat chatroom;
    private Bot source;
    HashMap<String, Chat> chats;

    BotChatListener(Bot source)
    {
        this.chatroom = source.chatroom;
        this.source = source;
        chats = new HashMap<String, Chat>();
    }

    @Override
    public void chatCreated(Chat chat, boolean createdLocally)
    {
        // Ignore this chat if it wasn't created by a CIDER client
        if (!StringUtils.parseResource(chat.getParticipant()).startsWith(
                Client.RESOURCE))
            return;

        if (chats.containsKey(StringUtils.parseName(chat.getParticipant())))
        {
            if (DEBUG)
                System.out
                        .println(chat.getParticipant()
                                + " initiated chat, but is already connected from another CIDER client, alerting new instance to quit...");
            try
            {
                chat.sendMessage( Base64.encodeBytes( "quit".getBytes() ) );
            }
            catch (XMPPException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        if (DEBUG)
            System.out.println(chat.getParticipant() + " initiated chat...");

        chats.put(StringUtils.parseName(chat.getParticipant()), chat);
        chat.addMessageListener(new BotMessageListener(this, StringUtils
                .parseName(chat.getParticipant()), this.source));

        // Invite this user to the chatroom
        chatroom.invite(chat.getParticipant(), " ");
    }

    /**
     * Called by message listeners when they receive a quit request from the
     * client. Remove the listener from the chat and remove its entry from the
     * table.
     * 
     * @param user
     */
    protected void endSession(String user)
    {
        if (chats.containsKey(user))
        {
            if (DEBUG)
                System.out.println("Ending session for " + user + "...");
            Chat current = chats.get(user);
            Iterator<MessageListener> itr = current.getListeners().iterator();
            while (itr.hasNext())
                current.removeMessageListener(itr.next());
            chats.remove(user);
        }
    }

}
