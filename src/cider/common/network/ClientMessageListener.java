package cider.common.network;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JOptionPane;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;

import cider.specialcomponents.Base64;

/**
 * This class waits for a message to be received by the client on its chat
 * session with the server.
 * 
 * @author Andrew + Lawrence
 * 
 */

public class ClientMessageListener implements MessageListener, ActionListener
{
    // TODO: These probably shouldn't be public
    private Client client;

    public ClientMessageListener(Client client)
    {
        this.client = client;
    }

    @Override
    public void processMessage(Chat chat, Message message)
    {
        String body = null;
		try {
			body = new String( Base64.decode( message.getBody() ) );
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        if (body.startsWith("quit"))
        {
            client.disconnect();
            System.err
                    .println("Someone is already running a CIDER client with your username, disconnecting and quitting.");
            System.exit(1);
        }
        else
            this.client.processDocumentMessages(body);
    }

    @Override
    public void actionPerformed(ActionEvent ae)
    {
        try
        {
            this.client.pullEventsSinceFromBot(this.client.getLastUpdate());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Cannot pull events: "
                    + e.getMessage());
            System.exit(1);
        }
    }
}
