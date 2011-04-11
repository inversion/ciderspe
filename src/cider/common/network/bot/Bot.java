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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;

import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.MultiUserChat;

import cider.common.network.ConfigurationReader;
import cider.common.network.DebugPacketFilter;
import cider.common.network.DebugPacketInterceptor;
import cider.common.network.DebugPacketListener;
import cider.common.processes.LiveFolder;
import cider.common.processes.Profile;
import cider.common.processes.SourceDocument;

/**
 * This is the class that implements the bot that connects to the XMPP server.
 * 
 * @author Andrew
 */

public class Bot
{
    private static final boolean DEBUG = true;

    // XMPP Server Configuration
    private final String HOST;
    private final String SERVICE_NAME;
    private final int PORT;
    protected final String BOT_USERNAME;
    private final String BOT_PASSWORD;
    private final String CHATROOM_NAME;
    private final String CHECKER_USERNAME;
    private final String CHECKER_PASSWORD;
    private final File SOURCE_DIR;
    private final File PROFILE_DIR;
    private ConnectionConfiguration conConfig;

    protected MultiUserChat chatroom;

    private XMPPConnection connection;
    private ChatManager chatmanager;
    protected BotChatListener chatListener;
    private LiveFolder sourceFolder;

    // Holds the colours for each user
    public HashMap<String, Color> colours;
    
    protected HashMap<String,Profile> profiles;

    // TODO: Temporary method of running the bot from the command line.
    public static void main(String[] args)
    {
        Bot bot = new Bot();
        try
        {
            System.in.read();
            bot.sourceFolder.writeToDisk(bot.SOURCE_DIR);
            bot.writeProfiles();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public Bot()
    {
        colours = new HashMap<String, Color>();
        profiles = new HashMap<String,Profile>();

        // Set up the bot configuration
        ConfigurationReader config = new ConfigurationReader("Bot.conf", null);
        HOST = config.getHost();
        SERVICE_NAME = config.getServiceName();
        PORT = config.getPort();
        BOT_USERNAME = config.getBotUsername();
        BOT_PASSWORD = config.getBotPassword();
        CHATROOM_NAME = config.getChatroomName();
        CHECKER_USERNAME = config.getCheckerUsername();
        CHECKER_PASSWORD = config.getCheckerPassword();
        SOURCE_DIR = config.getSourceDir();
        PROFILE_DIR = config.getProfileDir();
        conConfig = new ConnectionConfiguration(HOST, PORT, SERVICE_NAME);

        try
        {
            checkForBot();

            // Connect and login to the XMPP server
            connection = new XMPPConnection(conConfig);
            connection.connect();
            connection.login(BOT_USERNAME, BOT_PASSWORD);

            // Set up and join chatroom
            chatroom = new MultiUserChat(connection, CHATROOM_NAME
                    + "@conference." + SERVICE_NAME);
            chatroom.create(BOT_USERNAME);
            chatroom.addMessageListener(new BotChatroomMessageListener(this));
            BotChatroomPresenceListener participantListener = new BotChatroomPresenceListener(this);
            chatroom.addParticipantListener( participantListener );

            // Verbose debugging to print out every packet leaving or entering
            // the bot
            connection.addPacketListener(new DebugPacketListener(),
                    new DebugPacketFilter());
            connection.addPacketInterceptor(new DebugPacketInterceptor(),
                    new DebugPacketFilter());

            // Listen for new chats being initiated by clients
            chatmanager = connection.getChatManager();
            chatListener = new BotChatListener(this);
            chatmanager.addChatListener(chatListener);

            if (DEBUG)
                System.out.println("Using source path: "
                        + config.getSourceDir().getPath());

            sourceFolder = new LiveFolder("root", "Bot");
            readFromDisk(SOURCE_DIR, sourceFolder);

            // If source dir doesn't exist create it
            if (!SOURCE_DIR.exists())
                SOURCE_DIR.mkdir();

            // If source dir is empty make some test files
            if (SOURCE_DIR.list().length == 0)
                this.testTree();
            
            // Make profiles directory if it doesn't exist
            if( !PROFILE_DIR.exists() )
                PROFILE_DIR.mkdir();
            
            readProfiles();            
        }
        catch (XMPPException e)
        {
            e.printStackTrace();
            System.err.println("Error:" + e.getMessage());
        }
    }
    
    /**
     * Read serialized profiles into the profiles table.
     * 
     * @author Andrew
     * 
     */
    private void readProfiles()
    {
        File[] list = PROFILE_DIR.listFiles();
        for (File file : list)
        {
            if (file.isFile())
            {
                try
                {
                    FileInputStream fis = new FileInputStream(file);
                    ObjectInputStream input = new ObjectInputStream(fis);
                    Profile profile = (Profile) input.readObject();
                    profiles.put( profile.uname, profile );
                    input.close();
                    fis.close();
                }
                catch (InvalidClassException e)
                {
                    System.err.println( "You need to delete the " + PROFILE_DIR.getName() + " folder, you have out of date serialized classes.");
                }
                catch (FileNotFoundException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                catch (IOException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                catch (ClassNotFoundException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Write profiles back to disk on close
     * 
     * @author Andrew
     */
    private void writeProfiles()
    {
        Iterator<String> itr = profiles.keySet().iterator();
        while( itr.hasNext() )
        {
            try
            {
                String username = itr.next();
                File file = new File( PROFILE_DIR, username + ".dat" );
                file.createNewFile();
                
                FileOutputStream fos = new FileOutputStream(file);
                ObjectOutputStream out = new ObjectOutputStream(fos);
                out.writeObject( profiles.get( username ) );
                out.close();
                fos.close();
            }
            catch (FileNotFoundException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    /**
     * Connect to the server as a reserved user (CHECKER_USERNAME) to check if
     * the bot is already logged on from another location. Alert the user on
     * stderr if this is the case
     * 
     * @author Andrew
     * @throws XMPPException
     * 
     */
    private void checkForBot() throws XMPPException
    {
        XMPPConnection conn = new XMPPConnection(conConfig);
        conn.connect();
        conn.login(CHECKER_USERNAME, CHECKER_PASSWORD);
        chatroom = new MultiUserChat(conn, CHATROOM_NAME + "@conference."
                + SERVICE_NAME);
        try
        {
            chatroom.create(CHECKER_USERNAME);
        }
        catch (XMPPException e)
        {
            e.printStackTrace();
            System.err
                    .println("Error: Chatroom already exists, this means the bot is already online, or someone else has created the room.");
            System.err.println("Disconnecting, exiting...");
            conn.disconnect();
            System.exit(1);
        }
        chatroom.leave();
        conn.disconnect();
    }

    /**
     * Leave the chatroom and disconnect from the server.
     * 
     * @author Andrew
     */
    public void disconnect()
    {
        chatroom.leave();
        connection.disconnect();
    }

    /**
     * Read source document tree into live folder.
     * 
     * @author Andrew
     */
    private void readFromDisk(File path, LiveFolder folder)
    {
        if (!path.exists())
            return;
        File[] list = path.listFiles();
        for (File file : list)
        {
            if (file.isFile())
            {
                try
                {
                    FileInputStream fis = new FileInputStream(file);
                    ObjectInputStream input = new ObjectInputStream(fis);
                    folder.addDocument((SourceDocument) input.readObject());
                    input.close();
                    fis.close();
                }
                catch (InvalidClassException e)
                {
                    System.err.println( "You need to delete the " + path.getName() + " folder, you have out of date serialized classes.");
                    e.printStackTrace();
                }
                catch (IOException e)
                {
                    System.err.println( "You need to delete the " + path.getName() + " folder, you have corrupted files probably caused by unclean termination of the bot.");
                    e.printStackTrace();
                }
                catch (ClassNotFoundException e)
                {

                    e.printStackTrace();
                }
            }
            else if (file.isDirectory())
                readFromDisk(file.getAbsoluteFile(),
                        folder.makeFolder(file.getName()));
        }
    }

    /**
     * Create a test live folder tree
     * 
     * @author Lawrence
     */
    @SuppressWarnings("unused")
    private void testTree()
    {
        this.sourceFolder = new LiveFolder("root", "Bot");
        // FIXME: t1 is unused
        @SuppressWarnings("unused")
        SourceDocument t1 = this.sourceFolder.makeDocument("t1.SourceDocument");
        // Queue<TypingEvent> tes = new LinkedList<TypingEvent>();
        // tes.addAll(SourceDocument.generateEvents(0, 1000, 0, "Created at "
        // + System.currentTimeMillis(), TypingEventMode.insert, "bot"));
        // t1.push(tes);
        this.sourceFolder.makeFolder("testFolder").makeFolder("test2")
                .makeDocument("test2Doc.SourceDocument");
    }

    public LiveFolder getRootFolder()
    {
        return this.sourceFolder;
    }
}
