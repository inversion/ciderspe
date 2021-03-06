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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;

import cider.common.processes.Profile;
import cider.common.processes.SiHistoryFiles;

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
    protected Thread clientThread;

    public ClientMessageListener(Client client)
    {
        this.client = client;
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

    @SuppressWarnings("static-access")
    @Override
    public void processMessage(Chat chat, Message message)
    {
        String body = message.getBody();
        String ciderAction = (String) message.getProperty("ciderAction");
        if (ciderAction.equals("quit"))
        {
            JOptionPane
                    .showMessageDialog(
                            new JPanel(),
                            "Error: Someone is already running a CIDER client with your username, disconnecting and quitting.");
            client.disconnect();
            System.exit(1);
        }
        else if (ciderAction.equals("yes i am online"))
        {
            // Wake up the client thread
            client.botIsOnline = true;
            clientThread.interrupt();
        }
        else if (ciderAction.equals("usercolour"))
        {
            Integer r = (Integer) message.getProperty("r");
            Integer g = (Integer) message.getProperty("g");
            Integer b = (Integer) message.getProperty("b");
            client.incomingColour = new Color(r, g, b);
        }
        else if (ciderAction.equals("clearhistory"))
        {
            if (message.getProperty("show") != null)
                JOptionPane.showMessageDialog(null,
                        "Clear history for "
                                + message.getProperty("username"));

            if (client.getUsername().equals(message.getProperty("username")))
            {
                SiHistoryFiles.clearAllHistory();
            }
        }
        else if (ciderAction.equals("profile"))
        {
            String username = (String) message.getProperty("username");
            Integer chars = (Integer) message.getProperty("chars");
            Long timeSpent = (Long) message.getProperty("timeSpent");
            Integer idleTime = (Integer) message.getProperty("idleTime");
            String lastOnline = (String) message.getProperty("lastOnline");
            Integer r = (Integer) message.getProperty("r");
            Integer g = (Integer) message.getProperty("g");
            Integer b = (Integer) message.getProperty("b");

            // If setting our own profile's values
            if (username.equals(client.getUsername()))
            {
                System.out
                        .println("ClientMessageListener: Updating own profile...");
                client.profile.setUsername(username);
                client.profile.setTypedChars(chars);
                client.profile.setTimeSpent(timeSpent);
                client.profile.setIdleTime(idleTime);
                client.profile.setLastOnline(lastOnline);
                client.profile.setColour(r, g, b);
                client.colours.put(username, client.profile.getColour());
                client.shared.userList.repaint();

                // If we want the profile to pop up
                if (message.getProperty("show") != null)
                    client.getParent().showProfile(client.profile);
            }
            else
            {
                Profile profile = new Profile(username);
                profile.setTypedChars(chars);
                profile.setTimeSpent(timeSpent);
                profile.setIdleTime(idleTime);
                profile.setLastOnline(lastOnline);
                profile.setColour(r, g, b);

                // If we want the profile to pop up
                if (message.getProperty("show") != null)
                    client.getParent().showProfile(profile);
            }
        }
        else if (body.length() > 0 && body.startsWith("timeReply("))
        {
            String str = body.split("\\(")[1];
            str = str.split("\\)")[0];
            String[] args = str.split(",");
            long sentTime = Long.parseLong(args[0]);
            long currentTime = System.currentTimeMillis()
                    + client.getClockOffset();
            long halfLatency = (currentTime - sentTime) / 2;
            long delta = currentTime - Long.parseLong(args[1]) + halfLatency;

            if (client.getClockOffset() == 0)
                client.setTimeDelta(delta);

            client.addTimeDeltaSample(delta);
        }
        else
        {
            client.processDocumentMessages(message);
        }

    }
}
