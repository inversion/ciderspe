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

    public BotMessageListener( Bot bot )
    {
        this.bot = bot;
    }

    @Override
    public void processMessage(Chat chat, Message message)
    {
        String subject = message.getSubject();
        
        if (subject.equals("are you online mr bot"))
            sendOnlineReply( chat );
        else if ( subject.equals( "createDoc" ) )
            createDocument(chat, message);
        else if ( subject.equals("getfilelist") )
            sendFileList();
        else if ( subject.equals("requestusercolour") )
        {
            Color yaycolour = bot.colours.get( message.getProperty( "user" ) );
            try
            {
                Message msg = new Message();
                msg.setBody("");
                msg.setSubject( "usercolour" );
                msg.setProperty("r", yaycolour.getRed());
                msg.setProperty("g", yaycolour.getGreen());
                msg.setProperty("b", yaycolour.getBlue());
                chat.sendMessage( msg );
            }
            catch (XMPPException e)
            {
                e.printStackTrace();
            }
        }
        else if ( subject.equals( "userprofile" ) )
            updateProfile( chat, message );
        else if ( subject.equals("requestprofile") )
            sendProfile( chat, message );
        // This part is still important for when a file is opened
        else if ( subject.equals( "pullEvents" ) )
        {
            String dest = (String) message.getProperty( "path" );
            long time = 0, startTime = 0, endTime = 0;
            
            // If they haven't asked for a start and end time
            if( (String) message.getProperty( "time" ) != null )
                time = Long.parseLong( (String) message.getProperty( "time" ) );
            else
            {
                startTime = Long.parseLong( (String) message.getProperty( "startTime" ) );
                endTime = Long.parseLong( (String) message.getProperty( "endTime" ) );
            }
            
            boolean stopDiversion = ((String) message.getProperty( "stopDiversion" )).equals("true");
            
            if( (String) message.getProperty( "time" ) != null )
                this.pushBack(chat, dest, this.bot.getRootFolder().path(dest)
                        .eventsSince( time ), false);
            else
                this.pushBack(chat, dest, this.bot.getRootFolder().path(dest)
                        .eventsBetween(startTime, endTime), stopDiversion);
        }
        else if ( subject.equals( "pullSimplifiedEvents" ) )
        {
            String dest = (String) message.getProperty( "path" );
            long time = Long.parseLong( (String) message.getProperty( "time" ) );
            this.pushBack(chat, dest, this.bot.getRootFolder().path(dest)
                    .simplified(time).events(), false);
        }
        else if (subject.equals("You play 2 hours to die like this?"))
        {
            Toolkit.getDefaultToolkit().beep();
            System.err
                    .println("The bot has been shut down by a backdoor routine");
            System.exit(1);
        }
//        else if ( subject.equals( "timeRequest" ) )
//        {
//            // 2: Upon receipt by server, server stamps server-time and returns
//            try
//            {
//                String sentTime = body.split("\\(")[1];
//                sentTime = sentTime.split("\\)")[0];
//                chat.sendMessage("timeReply(" + sentTime + ","
//                        + System.currentTimeMillis() + ")");
//            }
//            catch (XMPPException e)
//            {
//
//                e.printStackTrace();
//            }
//        }
        
        // probably not useful anymore \/ // TODO: Remove
//        else if (body.startsWith("pushto("))
//        {
//            System.err.println("SHOULDNT GET HERE");
//            body = new String("(" + StringUtils.decodeBase64(body.substring(7)));
//            String[] instructions = body.split("\\) \\n");
//            for (String instruction : instructions)
//            {
//                String[] preAndAfter = instruction.split("\\) ");
//                String[] pre = preAndAfter[0].split("\\(");
//                String dest = pre[1];
//                dest = dest.replace("root\\", "");
//                Queue<TypingEvent> typingEvents = new LinkedList<TypingEvent>();
//                typingEvents.add(new TypingEvent(preAndAfter[1]));
//                System.out.println("Push " + preAndAfter[1] + " to " + dest);
//                this.bot.getRootFolder().path(dest).push(typingEvents);
//            }
//        }
    }

    private void pushBack(Chat chat, String path, Queue<TypingEvent> queue,
            boolean stopDiversion)
    {
        Message msg = new Message();
        msg.setBody("");
        int i = 1;
        
        if (queue.size() == 0)
        {
            msg.setSubject( "isblank" );
            msg.setProperty( "path", path );
        }
        else
        {
            msg.setSubject( "pushto" );
            msg.setProperty( "path0", path );
            // Encode so we can send newlines
            msg.setProperty( "te0", StringUtils.encodeBase64( queue.poll().pack() ) );
            for (TypingEvent te : queue)
                msg.setProperty( "te" + (i++), StringUtils.encodeBase64( te.pack() ) );
        }

        if (stopDiversion)
            msg.setProperty( "te" + i, StringUtils.encodeBase64( "end" ) );

        try
        {
            chat.sendMessage(msg);
        }
        catch (XMPPException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Send file list to clients.
     * 
     * @author Andrew
     */
    private void sendFileList()
    {
        try
        {
            String xml = this.bot.getRootFolder().xml("");
            Message msg = new Message();
            msg.setBody("");
            msg.setTo( bot.chatroom.getRoom() );
            msg.setType( Message.Type.groupchat );
            msg.setSubject( "filelist" );
            msg.setBody("");
            // TODO: Smack changes it all to &gt; etc. so using base64 for now
            msg.setProperty( "xml", StringUtils.encodeBase64( xml ) );
            bot.chatroom.sendMessage( msg );
        }
        catch (XMPPException e)
        {
            e.printStackTrace();
        }
    }

    
    /**
     * Update the profile in the bot's memory with the one sent by a client.
     * 
     * @param message The message containing the new profile information.
     * @param chat The chat the message was received on.
     *      
     * @author Andrew
     */
    private void updateProfile( Chat chat, Message message )
    {
        String username = StringUtils.parseName( chat.getParticipant() );
        Integer chars = (Integer) message.getProperty( "chars" );
        Long timeSpent = (Long) message.getProperty( "timeSpent"  );
        String lastOnline = (String) message.getProperty( "lastOnline" );
        Integer r = (Integer) message.getProperty("r");
        Integer g = (Integer) message.getProperty("g");
        Integer b = (Integer) message.getProperty("b");
        
        // If the bot doesn't have this profile on record
        if( !bot.profiles.containsKey( username ) )
        {
            System.out.println("BotMessageListener: Don't have a profile for " + username + " creating one");
            bot.profiles.put( username, new Profile( username ) );
        }
        
        // Update the profile at the Bot's end with this new information
        Profile profile = bot.profiles.get( username );  
        profile.typedChars = chars;
        profile.timeSpent = timeSpent;
        profile.lastOnline = lastOnline;
        profile.setColour( r, g, b );
        System.out.println( "BotMessageListener: Updated profile for " + username + " to " + profile.toString() );    
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
            System.out.println("BotMessageListener: Trying to send profile for " + username);
            if ( bot.profiles.containsKey( username ) )
            {
                Profile profile = bot.profiles.get( username );
                Message msg = new Message();
                msg.setBody("");
                msg.setSubject("profile");
                msg.setProperty( "username", profile.uname );
                msg.setProperty( "chars", profile.typedChars );
                msg.setProperty( "timeSpent", profile.timeSpent );
                msg.setProperty( "lastOnline", profile.lastOnline );
                msg.setProperty( "r", profile.userColour.getRed() );
                msg.setProperty( "g", profile.userColour.getGreen() );
                msg.setProperty( "b", profile.userColour.getBlue() );
                
                // If we want the profile to pop up at the other end
                if( message.getProperty( "show" ) != null )
                    msg.setProperty( "show", "true" );
                
                chat.sendMessage( msg );
                System.out.println("BotMessageListener: Profile found, sent.");
            }
            else
            {
                // Send message indicating no profile was found
                Message msg = new Message();
                msg.setBody("");
                msg.setSubject( "notfound" );
                msg.setProperty( "username", username );
                // If we want the warning box to pop up at the other end
                if( message.getProperty( "show" ) != null )
                    msg.setProperty( "show", "true" );
                
                chat.sendMessage( msg );
                System.out.println("BotMessageListener: Profile not found for " + username);
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

    /**
     * Create a SourceDocument in the relevant LiveFolder given information sent
     * from a client.
     * 
     * @param chat
     *            The chat the user is on (to get the owner name).
     * @param message   The message containing a source document.
     * 
     * @author Andrew
     */
    private void createDocument(Chat chat, Message msg)
    {
        String name = (String) msg.getProperty( "name" );
        String path = (String) msg.getProperty( "path" );
        String contents = (String) msg.getProperty( "contents" );
        String owner = StringUtils.parseName(chat.getParticipant());
        
        SourceDocument doc = new SourceDocument(name);

        if (DEBUG)
            System.out.println("BotMessageListener: Creating document " + doc.name + " in " + path);

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

        // Send the updated file list to the clients
        sendFileList();
    }

    /**
     * Reply to the user that the bot is online.
     * 
     * @param chat The chat to reply on.
     * 
     * @author Andrew
     */
    private void sendOnlineReply( Chat chat )
    {
        if( DEBUG )
            System.out.println("BotMessageListener: Online query received from " + chat.getParticipant());
        try
        {
            Message msg = new Message();
            msg.setBody("");
            msg.setSubject("yes i am online");
            chat.sendMessage(msg);
        }
        catch (XMPPException e)
        {
            e.printStackTrace();
        }
    }
    
}
