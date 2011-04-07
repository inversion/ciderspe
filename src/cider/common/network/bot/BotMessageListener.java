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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.util.Base64;
import org.jivesoftware.smack.util.StringUtils;

import cider.common.processes.LiveFolder;
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
    
    private BotChatListener source;
    private String name;
    private Bot bot;


    public BotMessageListener(BotChatListener source, String name, Bot bot)
    {
        this.source = source;
        this.name = name;
        this.bot = bot;
        // System.out.println(this.liveFolder.xml(""));
    }
    
    /**
     * Goes through a path and finds the corresponding live folder object.
     * 
     * It will be created if it doesn't exist.
     * 
     * @param path The remainder of the path we are finding.
     * @param current The current LiveFolder we are in.
     * @return The LiveFolder.
     * 
     * @author Andrew
     */
    private LiveFolder findFolder( String path, LiveFolder current )
    {
        String part;
        // Trim trailing slash
        if( path.endsWith( "\\" ) )
            path = path.substring( 0, path.length()-1 );
        
        while( path.length() > 0 )
        {
            if( path.indexOf("\\") == -1 )
            {
                System.out.println("Returning " + path);
                if( current.getFolder( path ) == null )
                    current.makeFolder( path );
                return current.getFolder( path );
            }
            else
            {
                part = path.substring( 0, path.indexOf( "\\" ) );
                if( current.getFolder( part ) == null )
                    current = current.makeFolder( part );
                else
                    current = current.getFolder( part );
                    
                path = path.substring( part.length()+1 );
            }
        }
        
        return current;
    }

    @Override
    public void processMessage(Chat chat, Message message)
    {
        String body = null;
        body = message.getBody();

        if (body.startsWith("quit"))
            source.endSession(name);
        else if ( body.startsWith( "createDocument=" ) )
        {
            String[] components = body.split( ":" );
            String name = new String( StringUtils.decodeBase64( components[1] ) );
            String path = new String( StringUtils.decodeBase64( components[3] ) );
            String contents = new String( StringUtils.decodeBase64( components[5] ) );
            String owner = StringUtils.parseName( chat.getParticipant() );
            SourceDocument doc = new SourceDocument( name, owner );
            
            if( DEBUG )
                System.out.println( "Creating document " + doc.name + " in " + path );
            
            if( contents != null )
            {
                // Generate typing events for current file contents
                TypingEvent te = new TypingEvent(System.currentTimeMillis(), TypingEventMode.insert, 
                        0, contents.length(), contents, owner, null);
                ArrayList<TypingEvent> events = te.explode();
                doc.addEvents( events );
            }
                
            findFolder( path, bot.getRootFolder() ).addDocument( doc );             
            
            // TODO: Duplicate code from below
            String xml = this.bot.getRootFolder().xml("");
            try
            {
                bot.chatroom.sendMessage("filelist=" + StringUtils.encodeBase64(xml));
            }
            catch (XMPPException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }   
        }
        else if (body.startsWith("requestusercolour"))
        {
            String[] split = body.split(" ");
            Color yaycolour = bot.colours.get(split[2]);
            try
            {
                source.chats.get(split[1]).sendMessage(
                        "usercolour: "
                                + yaycolour.getRed() + " "
                                + yaycolour.getGreen() + " "
                                + yaycolour.getBlue());
            }
            catch (XMPPException e)
            {
                e.printStackTrace();
            }
        }
        else if (body.startsWith("userprofile:"))
        {
            String[] splitProfile = body.split("  ");
            File f = new File("profile_" + splitProfile[1] + ".txt");
            try
            {
                f.createNewFile();
                FileWriter fw = new FileWriter(f);
                BufferedWriter out = new BufferedWriter(fw);
                String s = splitProfile[1] + "\n" + splitProfile[2] + "\n"
                        + splitProfile[3] + "\n" + splitProfile[4] + "\n"
                        + splitProfile[5];
                System.out
                        .println("**********RECEIVED PROFILE**********\n" + s);
                System.out.println("************************************");
                out.write(s);
                out.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
                System.err.println("Error: " + e.getMessage());
            }
        }
        else if (body.equals("are you online mr bot"))
        {
            System.out.println("Online query received");
            try
            {
                chat.sendMessage("yes i am online");
            }
            catch (XMPPException e)
            {
                e.printStackTrace();
            }
        }
        else if (body.startsWith("requestprofile"))
        {
            String[] splitbody = body.split(" ");
            System.out.println(body);
            boolean notme = false;
            for (int i = 0; i < splitbody.length; i++)
            {
                if (splitbody[i].equals("notme"))
                    notme = true;
            }
            try
            {
                File f = new File("profile_" + splitbody[1] + ".txt");
                if (f.exists())
                {
                    FileInputStream fis = new FileInputStream(f);
                    DataInputStream dis = new DataInputStream(fis);
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(dis));
                    String line;
                    System.out.println("Reading profile, sending:\n");
                    while ((line = br.readLine()) != null)
                    {
                        System.out.println(line);
                        // Send profile file to client
                        if (notme)
                            chat.sendMessage("PROFILE$ " + line);
                        else
                            source.chats.get(splitbody[1]).sendMessage(
                                    ("PROFILE* " + line) );
                    }
                }
                else
                {
                    // Send message indicating no profile was found
                    source.chats.get(splitbody[1]).sendMessage(
                            "notfound");
                    System.out.println("Profile not found!");
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
                System.err
                        .println("Error: IO error when retrieving profile for "
                                + splitbody[1]);
            }
            catch (XMPPException e)
            {
                e.printStackTrace();
                System.err
                        .println("XMPP Exception whilst retrieving profile. Error message: "
                                + e.getMessage());
            }
        }
        else if (body.equals("getfilelist"))
        {
            try
            {
                String xml = this.bot.getRootFolder().xml("");
                chat.sendMessage( "filelist=" + StringUtils.encodeBase64(xml));
            }
            catch (XMPPException e)
            {
                e.printStackTrace();
            }
        }
        // This part is still important for when a file is opened
        else if (body.startsWith("pullEvents("))
        {
            body = "(" + new String( StringUtils.decodeBase64( body.substring( 11 )  ) );

            String str = body.split("\\(")[1];
            str = str.split("\\)")[0];
            String[] args = str.split(",");
            String dest = args[0];
            long startTime = Long.parseLong(args[1]);

            if (args.length == 2)
                this.pushBack(chat, dest, this.bot.getRootFolder().path(dest)
                        .eventsSince(startTime), false);
            else
            {
                long endTime = Long.parseLong(args[2]);
                this.pushBack(chat, dest, this.bot.getRootFolder().path(dest)
                        .eventsBetween(startTime, endTime), args[3].equals("t"));
            }

        }
        else if (body.startsWith("pullSimplifiedEvents("))
        {
            body = "(" + new String( StringUtils.decodeBase64( body.substring( 21 ) ) );

            String str = body.split("\\(")[1];
            str = str.split("\\)")[0];
            String[] args = str.split(",");
            String dest = args[0];
            long t = Long.parseLong(args[1]);
            this.pushBack(chat, dest, this.bot.getRootFolder().path(dest)
                    .simplified(t).events(), false);
        }
        else if (body.startsWith("You play 2 hours to die like this?"))
        {
            Toolkit.getDefaultToolkit().beep();
            System.err
                    .println("The bot has been shut down by a backdoor routine");
            System.exit(1);
        }
        else if (body.startsWith("timeRequest("))
        {
            // 2: Upon receipt by server, server stamps server-time and returns
            try
            {
                String sentTime = body.split("\\(")[1];
                sentTime = sentTime.split("\\)")[0];
                chat.sendMessage("timeReply("
                        + sentTime + "," + System.currentTimeMillis() + ")");
            }
            catch (XMPPException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        // probably not useful anymore \/
        else if (body.startsWith("pushto("))
        {
            body = new String( "(" + StringUtils.decodeBase64( body.substring( 7 ) ) );
            String[] instructions = body.split("\\) \\n");
            for (String instruction : instructions)
            {
                String[] preAndAfter = instruction.split("\\) ");
                String[] pre = preAndAfter[0].split("\\(");
                String dest = pre[1];
                dest = dest.replace("root\\", "");
                Queue<TypingEvent> typingEvents = new LinkedList<TypingEvent>();
                typingEvents.add(new TypingEvent(preAndAfter[1]));
                System.out.println("Push " + preAndAfter[1] + " to " + dest);
                this.bot.getRootFolder().path(dest).push(typingEvents);
            }
        }
    }

    private void pushBack(Chat chat, String path, Queue<TypingEvent> queue,
            boolean stopDiversion)
    {
        String instructions = "";
        if (queue.size() == 0)
            instructions += "isblank(" + path + ")";
        else
        {
            instructions += "pushto(" + path + ") ";
            for (TypingEvent te : queue)
                instructions +=  te.pack() + "%%";
        }

        if (stopDiversion)
            instructions += "stopdiversion";

        try
        {
            if( instructions.startsWith( "isblank" ) )
                instructions = "isblank(" + StringUtils.encodeBase64( instructions.substring( 8 ) );
            else if( instructions.startsWith( "pushto" ))
                instructions = "pushto(" + StringUtils.encodeBase64( instructions.substring( 7 ) );
            chat.sendMessage(instructions);
        }
        catch (XMPPException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * FIXME: Unused method!
     * 
     */
    /*
     * private void pushBack(Chat chat, Queue<LocalisedTypingEvents> events) {
     * String instructions = ""; for (LocalisedTypingEvents ltes : events) for
     * (TypingEvent te : ltes.typingEvents) instructions += "pushto(" +
     * ltes.path + ") " + te.pack() + ">"; try {
     * chat.sendMessage(StringUtils.encodeBase64(instructions)); } catch
     * (XMPPException e) { // TODO Auto-generated catch block
     * e.printStackTrace(); } }
     */
}
