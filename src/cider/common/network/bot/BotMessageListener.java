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

package cider.common.network.bot;

import java.awt.Color;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Queue;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.util.StringUtils;

import cider.common.processes.LiveFolder;
import cider.common.processes.Profile;
import cider.common.processes.SourceDocument;
import cider.common.processes.TypingEvent;
import cider.common.processes.TypingEventMode;
import cider.documentViewerComponents.SourceDocumentViewer;

/**
 * This class waits for a message to be received on a chat session and then
 * responds to messages accordingly.
 * 
 * 
 * @author Andrew
 * 
 */

public class BotMessageListener implements MessageListener
{
    public static boolean DEBUG = true;

    private Bot bot;

    public BotMessageListener(Bot bot)
    {
        this.bot = bot;
    }

    /**
     * Create a SourceDocument in the relevant LiveFolder given information sent
     * from a client.
     * 
     * @param chat
     *            The chat the user is on (to get the owner name).
     * @param message
     *            The message containing a source document.
     * 
     * @author Andrew
     */
    private void createDocument(Chat chat, Message msg)
    {
        String name = (String) msg.getProperty("name");
        String path = (String) msg.getProperty("path");
        String contents = (String) msg.getProperty("contents");
        String owner = StringUtils.parseName(chat.getParticipant());

        SourceDocument doc = new SourceDocument(name);

        if (DEBUG)
            System.out.println("BotMessageListener: Creating document "
                    + doc.name + " in " + path);

        if (contents != null)
        {
            // Generate typing events for current file contents
            TypingEvent te = new TypingEvent(System.currentTimeMillis(),
                    TypingEventMode.insert, 0, contents.length(), contents,
                    owner, null);
            ArrayList<TypingEvent> events = te.explode();
            doc.addEvents(events);
        }

        // Add the new document to the folder
        LiveFolder.findFolder(path, bot.getRootFolder()).addDocument(doc);

        // Send the updated file list to all the clients
        sendFileList(null);
    }

    /**
     * Parses the body of the XMPP message that has been sent to the bot by the client.
     * @param chat The XMPP private chat
     * @param message The XMPP message that has been transmitted over the chat
     * @author Andrew, Jon
     */
    @Override
    public void processMessage(Chat chat, Message message)
    {
        String ciderAction = (String) message.getProperty("ciderAction");

        if (ciderAction.equals("are you online mr bot"))
            sendOnlineReply(chat);
        else if (ciderAction.equals("createDoc"))
            createDocument(chat, message);
        else if (ciderAction.equals("getfilelist"))
            sendFileList(chat.getParticipant());
        else if (ciderAction.equals("requestusercolour"))
        {
            Color yaycolour = bot.colours.get(message.getProperty("user"));
            try
            {
                Message msg = new Message();
                msg.setBody("");
                msg.setProperty("ciderAction", "usercolour");
                msg.setProperty("r", yaycolour.getRed());
                msg.setProperty("g", yaycolour.getGreen());
                msg.setProperty("b", yaycolour.getBlue());
                chat.sendMessage(msg);
            }
            catch (XMPPException e)
            {
                e.printStackTrace();
            }
        }
        else if (ciderAction.equals("userprofile"))
            updateProfile(chat, message);
        else if (ciderAction.equals("requestprofile"))
            sendProfile(chat, message);
        // This part is still important for when a file is opened
        else if (ciderAction.equals("pullEvents"))
        {
            String dest = (String) message.getProperty("path");
            long time = 0, startTime = 0, endTime = 0;

            // If they haven't asked for a start and end time
            if ((String) message.getProperty("time") != null)
                time = Long.parseLong((String) message.getProperty("time"));
            else
            {
                startTime = Long.parseLong((String) message
                        .getProperty("startTime"));
                endTime = Long.parseLong((String) message
                        .getProperty("endTime"));
            }

            boolean stopDiversion = ((String) message
                    .getProperty("stopDiversion")).equals("true");

            if ((String) message.getProperty("time") != null)
                this.pushBack(chat, dest, bot.getRootFolder().path(dest)
                        .eventsSince(time), false, false);
            else
                this.pushBack(chat, dest, bot.getRootFolder().path(dest)
                        .eventsBetween(startTime, endTime), stopDiversion,
                        false);
        }
        else if (ciderAction.equals("pullSimplifiedEvents"))
        {
            String dest = (String) message.getProperty("path");
            long time = Long.parseLong((String) message.getProperty("time"));
            this.pushBack(chat, dest, bot.getRootFolder().path(dest)
                    .simplified(time).events(), false, true);
        }
        else if (ciderAction.equals("You play 2 hours to die like this?"))
        {
            Toolkit.getDefaultToolkit().beep();
            System.err
                    .println("The bot has been shut down by a backdoor routine");
            System.exit(1);
        }
        else if (ciderAction.equals("what my font size?"))
        {
            String username = (String) message.getProperty("username");
            int sizetosend;
            try
            {
                if (bot.profiles.containsKey(username))
                {
                    Profile profile = bot.profiles.get(username);
                    sizetosend = profile.getUserFontSize();
                }
                else
                {
                    sizetosend = SourceDocumentViewer.fontSize;
                }
                Message msg = new Message();
                msg.setBody("");
                msg.setProperty("fontSize", sizetosend);
                chat.sendMessage(msg);
            }
            catch (XMPPException e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * Send events to a client, splitting the queue into chunks
     * 
     * @param chat
     * @param path
     * @param queue
     * @param stopDiversion
     * @param sendPlain
     *            Sends plain text back rather than events.
     */
    private void pushBack(Chat chat, String path, Queue<TypingEvent> queue,
            boolean stopDiversion, boolean sendPlain)
    {
        Message msg = null;
        if (queue.size() == 0)
        {
            msg = new Message();
            msg.setBody("");
            msg.setProperty("ciderAction", "isblank");
            msg.setProperty("path", path);
            try
            {
                chat.sendMessage(msg);
            }
            catch (XMPPException e)
            {
                e.printStackTrace();
            }
            return;
        }
        else
        {
            if (!sendPlain) // If sending events back normally
            {
                int splitInterval = 50;
                Object[] allEvents = queue.toArray();
                ArrayList<TypingEvent[]> splitEvents = new ArrayList<TypingEvent[]>();
                // Split the queue of typing events into blocks of 50 so it can
                // be sent over the network
                for (int split = 0; split < allEvents.length; split++)
                {
                    if (split % splitInterval == 0)
                        splitEvents.add(new TypingEvent[splitInterval]);

                    splitEvents.get(splitEvents.size() - 1)[split
                            % splitInterval] = (TypingEvent) allEvents[split];
                }

                // Send all of the blocks of events we have in turn as separate
                // messages
                for (TypingEvent[] curSplit : splitEvents)
                {
                    msg = new Message();
                    msg.setBody("");
                    msg.setProperty("ciderAction", "pushto");
                    msg.setProperty("path0", path);

                    int i = 0;
                    // Encode so we can send newlines
                    for (TypingEvent te : curSplit)
                    {
                        if (te == null)
                            break;
                        msg.setProperty("te" + (i++), StringUtils
                                .encodeBase64(te.pack()));
                    }

                    if (stopDiversion)
                        msg.setProperty("te" + i, StringUtils
                                .encodeBase64("end"));

                    try
                    {
                        chat.sendMessage(msg);
                    }
                    catch (XMPPException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
            else
            // If sending plain text back rather than events
            {
                msg = new Message();
                msg.setBody("");
                msg.setProperty("ciderAction", "pushtoPlain");
                msg.setProperty("path", path);
                // msg.setProperty( "startTime", queue.peek().time );

                StringBuffer contents = new StringBuffer();
                for (TypingEvent te : queue)
                    contents.append(te.text);

                msg.setProperty("contents", contents.toString());

                try
                {
                    chat.sendMessage(msg);
                }
                catch (XMPPException e)
                {
                    e.printStackTrace();
                }
            }

        }
    }

    /**
     * Send file list to clients.
     * 
     * @param userJID
     *            The FULL JID of the user to send the list to, or null to
     *            broadcast it.
     * @author Andrew
     */
    private void sendFileList(String userJID)
    {
        try
        {
            String xml = bot.getRootFolder().xml("");

            if (DEBUG)
                System.out.println(xml);

            Message msg = new Message();
            msg.setBody("");
            msg.setProperty("ciderAction", "filelist");
            msg.setBody("");
            msg.setProperty("xml", StringUtils.encodeBase64(xml));

            // Send to everyone or just one user.
            if (userJID == null)
            {
                msg.setTo(bot.chatroom.getRoom());
                msg.setType(Message.Type.groupchat);
                bot.chatroom.sendMessage(msg);
            }
            else
            {
                msg.setTo(userJID);
                bot.chatListener.chats.get(StringUtils.parseName(userJID))
                        .sendMessage(msg);
            }
        }
        catch (XMPPException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Reply to the user that the bot is online.
     * 
     * @param chat
     *            The chat to reply on.
     * 
     * @author Andrew
     */
    private void sendOnlineReply(Chat chat)
    {
        if (DEBUG)
            System.out
                    .println("BotMessageListener: Online query received from "
                            + chat.getParticipant());
        try
        {
            Message msg = new Message();
            msg.setBody("");
            msg.setProperty("ciderAction", "yes i am online");
            chat.sendMessage(msg);
        }
        catch (XMPPException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Send a profile to a user.
     * 
     * @param chat
     *            Chat to send it on.
     * @param message
     *            Request message to be parsed.
     * 
     * @author Jon, Andrew
     */
    private void sendProfile(Chat chat, Message message)
    {
        String username = (String) message.getProperty("username");

        try
        {
            System.out
                    .println("BotMessageListener: Trying to send profile for "
                            + username);
            if (bot.profiles.containsKey(username)
                    && !this.sendToClearHistory(username))
            {
                Profile profile = bot.profiles.get(username);
                Message msg = new Message();
                msg.setBody("");
                msg.setProperty("ciderAction", "profile");
                msg.setProperty("username", profile.getUsername());
                msg.setProperty("chars", profile.getTypedChars());
                msg.setProperty("timeSpent", profile.getTimeSpent());
                msg.setProperty("idleTime", profile.getIdleTime());
                msg.setProperty("lastOnline", profile.getLastOnline());
                msg.setProperty("r", profile.getColour().getRed());
                msg.setProperty("g", profile.getColour().getGreen());
                msg.setProperty("b", profile.getColour().getBlue());
                msg.setProperty("fontSize", profile.getUserFontSize());

                // If we want the profile to pop up at the other end
                if (message.getProperty("show") != null)
                    msg.setProperty("show", "true");

                chat.sendMessage(msg);
                System.out.println("BotMessageListener: Profile found, sent.");
            }
            else
            {
                // Send message indicating no profile was found
                Message msg = new Message();
                bot.profiles.put(username, new Profile(username));
                Profile profile = bot.profiles.get(username);
                msg.setBody("");
                msg.setProperty("ciderAction", "profile");
                msg.setProperty("username", profile.getUsername());
                msg.setProperty("chars", profile.getTypedChars());
                msg.setProperty("timeSpent", profile.getTimeSpent());
                msg.setProperty("idleTime", profile.getIdleTime());
                msg.setProperty("lastOnline", profile.getLastOnline());
                msg.setProperty("r", profile.getColour().getRed());
                msg.setProperty("g", profile.getColour().getGreen());
                msg.setProperty("b", profile.getColour().getBlue());
                msg.setProperty("fontSize", profile.getUserFontSize());

                // If we want the profile to pop up at the other end
                if (message.getProperty("show") != null)
                    msg.setProperty("show", "true");

                chat.sendMessage(msg);
                System.out.println("BotMessageListener: Profile CREATED, sent.");
                /*
                msg.setBody("");
                msg.setProperty("ciderAction", "notfound");
                msg.setProperty("username", username);
                // If we want the warning box to pop up at the other end
                if (message.getProperty("show") != null)
                    msg.setProperty("show", "false");

                chat.sendMessage(msg);
                System.out.println("BotMessageListener: Profile not found for "
                        + username);*/
            }
        }
        catch (XMPPException e)
        {
            e.printStackTrace();
            System.err
                    .println("XMPP Exception whilst retrieving profile. Error message: "
                            + e.getMessage());
        }
    }

    public boolean sendToClearHistory(String username)
    {
        if (!bot.isDebugbot())
            return false;

        boolean seen = bot.hasConnectedDuringThisRun(username);

        if (seen)
            System.out.println(username
                    + " has connected during this run before.");
        else
        {
            System.out.println(username
                    + " has not connected during this run before.");
            bot.connectedDuringThisRun(username);
        }

        return !seen;
    }

    /**
     * Update the profile in the bot's memory with the one sent by a client.
     * 
     * @param message The message containing the new profile information.
     * @param chat The chat the message was received on.
     * 
     * @author Andrew
     */
    private void updateProfile(Chat chat, Message message)
    {
        String username = StringUtils.parseName(chat.getParticipant());
        Integer chars = (Integer) message.getProperty("chars");
        Long timeSpent = (Long) message.getProperty("timeSpent");
        Integer idleTime = (Integer) message.getProperty("idleTime");
        String lastOnline = (String) message.getProperty("lastOnline");
        Integer r = (Integer) message.getProperty("r");
        Integer g = (Integer) message.getProperty("g");
        Integer b = (Integer) message.getProperty("b");
        Integer s = (Integer) message.getProperty("userFontSize");

        // If the bot doesn't have this profile on record
        if (!bot.profiles.containsKey(username))
        {
            System.out.println("BotMessageListener: Don't have a profile for "
                    + username + " creating one");
            bot.profiles.put(username, new Profile(username));
        }

        // Update the profile at the Bot's end with this new information
        Profile profile = bot.profiles.get(username);
        profile.setTypedChars(chars);
        profile.setTimeSpent(timeSpent);
        profile.setIdleTime(idleTime);
        profile.setLastOnline(lastOnline);
        profile.setColour(r, g, b);
        profile.setFontSize(s);
        System.out.println("BotMessageListener: Updated profile for "
                + username + " to " + profile.toString());

        // Register this user's profile to be committed to the disk
        bot.updatedProfiles.add(username);
    }

}
