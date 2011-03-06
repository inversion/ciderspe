package cider.common.network;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.util.StringUtils;

import cider.common.processes.Profile;



/**
 * This class waits for a message to be received by the client on its chat
 * session with the bot.
 * 
 * @author Andrew + Lawrence
 * 
 */

public class ClientMessageListener implements MessageListener, ActionListener
{
    private Client client;

    public ClientMessageListener(Client client)
    {
        this.client = client;
    }

    @Override
    public void processMessage(Chat chat, Message message)
    {
    	String body = new String( StringUtils.decodeBase64( message.getBody() ) );
        if (body.startsWith("quit"))
        {
			JOptionPane.showMessageDialog(new JPanel(), 
					"Error: Someone is already running a CIDER client with your username, disconnecting and quitting.");
            client.disconnect();
            System.exit(1);
        }
        else if (body.equals("notfound"))
        {
        	client.profileFound = false;
        }
        else if (body.startsWith("usercolour:"))
        {
        	String[] split = body.split(" ");
        	Color c = new Color(Integer.parseInt(split[1]),
        			Integer.parseInt(split[2]),
        			Integer.parseInt(split[3]));
        	client.incomingColour = c;
        }
        else if (body.startsWith("PROFILE* "))
        {
        	System.out.println(body);
        	if (client.profile == null)
        	{
        		client.profile = new Profile(client.getUsername(), client);
        	}
        	client.profileFound = true;
        	String[] splitLine = body.split(" ");
        	if (splitLine[1].equals("chars:"))
        	{
        		client.profile.setChars(Integer.parseInt(splitLine[2]));
        	}
        	else if (splitLine[1].equals("timespent:"))
        	{
        		client.profile.setTime(Long.parseLong(splitLine[2]));
        	}
        	else if (splitLine[1].equals("lastonline:"))
        	{
        		String newsplit = body.substring(21);
        		client.profile.setLastOnline(newsplit);
        	}
        	else if (splitLine[1].equals("colour:"))
        	{
        		client.profile.setColour(
        				Integer.parseInt(splitLine[2]),
        				Integer.parseInt(splitLine[3]),
        				Integer.parseInt(splitLine[4]));
        	}
        	else if (body.startsWith("timeReply("))
            {
                String str = body.split("\\(")[1];
                str = str.split("\\)")[0];
                String[] args = str.split(",");
                long sentTime = Long.parseLong(args[0]);
                long currentTime = System.currentTimeMillis()
                        + this.client.getClockOffset();
                long halfLatency = (currentTime - sentTime) / 2;
                long delta = currentTime - Long.parseLong(args[1]) + halfLatency;

                if (this.client.getClockOffset() == 0)
                    this.client.setTimeDelta(delta);

                this.client.addTimeDeltaSample(delta);
            }
        	else
        	{
        		client.profile.uname = splitLine[2];
        	}
        }
        else
            this.client.processDocumentMessages(body);
    }

    @Override
    public void actionPerformed(ActionEvent ae)
    {
        /*
         * try { this.client.pullEventsFromBot(this.client.getLastUpdate()); }
         * catch (Exception e) { e.printStackTrace();
         * JOptionPane.showMessageDialog(null, "Cannot pull events: " +
         * e.getMessage()); System.exit(1); }
         */
    }
}
