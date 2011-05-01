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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.Map.Entry;

import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.MultiUserChat;

import cider.common.network.ConfigurationReader;
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

    public static final DateFormat dateFormat = new SimpleDateFormat(
    "yyyy-MM-dd HH:mm:ss");
    
    // XMPP Server Configuration
    private final String HOST;
    protected final String SERVICE_NAME;
    private final int PORT;
    protected final String BOT_USERNAME;
    private final String BOT_PASSWORD;
    private final String CHATROOM_NAME;
    private final String CHECKER_USERNAME;
    private final String CHECKER_PASSWORD;
    private final File SOURCE_DIR;
    private final File PROFILE_DIR;
    private final File CHAT_HISTORY_DIR;
    private ConnectionConfiguration conConfig;

    protected MultiUserChat chatroom;

    private XMPPConnection connection;
    private ChatManager chatmanager;
    protected BotChatListener chatListener;
    private LiveFolder sourceFolder;
    
    private boolean debugbot = true;

    // Documents that have been changed or created during this execution
    protected HashMap<String,SourceDocument> updatedDocs;
    
    // Holds the colours for each user
    public HashMap<String, Color> colours;
    
    protected HashMap<String,Profile> profiles;
    
    // Profiles that have been updated in this execution
    protected HashSet<String> updatedProfiles;

    protected Queue<String> history;
    
    private CommitTimer commitTimer;

    // TODO: Temporary method of running the bot from the command line.
    public static void main(String[] args)
    {
        boolean debugbot = true;
        
        for(String arg : args)
            if(arg.equals("-debugbot=true"))
            {
                debugbot = true;
                System.out.println("debugbot");
            }
        
        Bot bot = new Bot();
        try
        {
            System.in.read();
            bot.commitTimer.stopTimer();
            
            if(!debugbot)
                bot.writeUpdatedDocs();
            
            bot.writeUpdatedProfiles();
            bot.writeChatHistory();
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
        updatedProfiles = new HashSet<String>();
        updatedDocs = new HashMap<String,SourceDocument>();

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
        CHAT_HISTORY_DIR = config.getChatHistoryDir();
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
            
            history = new LinkedList<String>();

            // Verbose debugging to print out every packet leaving or entering
            // the bot
//            connection.addPacketListener(new DebugPacketListener(),
//                    new DebugPacketFilter());
//            connection.addPacketInterceptor(new DebugPacketInterceptor(),
//                    new DebugPacketFilter());

            // Listen for new chats being initiated by clients
            chatmanager = connection.getChatManager();
            chatListener = new BotChatListener(this);
            chatmanager.addChatListener(chatListener);

            if(!this.debugbot)
            {
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
            }
            else
                this.testTree();
            
            // Make profiles directory if it doesn't exist
            if( !PROFILE_DIR.exists() )
                PROFILE_DIR.mkdir();
            
            if(this.debugbot)
                readProfiles();         
            
            // Make chat history directory if it doesn't exist
            if( !CHAT_HISTORY_DIR.exists())
                CHAT_HISTORY_DIR.mkdir();
          
            commitTimer = new CommitTimer( this );
        }
        catch (XMPPException e)
        {
            e.printStackTrace();
            System.err.println("Error:" + e.getMessage());
        }
    }
    
    /**
     * Flush chat history buffer to disk
     * 
     * @author Andrew
     * @throws IOException 
     */
    protected void writeChatHistory() throws IOException
    {
        File log = null;
        FileWriter fw = null;
        BufferedWriter bw = null;
        String item, dateTime, date;
        while( !history.isEmpty() )
        {
            item = history.poll();
            dateTime = item.substring( 1, 20 );
            date = dateTime.substring( 0, 10 );
            if( log == null ) // If there's no log file for this day
            {
                log = new File( CHAT_HISTORY_DIR, date + ".log" );
                log.createNewFile();
                fw = new FileWriter( log, true );
                bw = new BufferedWriter( fw );
            }
            else if( date != log.getName().substring( 0, 10 ) ) // If we've got to a new day
            {
                bw.close();
                fw.close();
                log = new File( CHAT_HISTORY_DIR, date + ".log" );
                log.createNewFile();
                fw = new FileWriter( log, true );
                bw = new BufferedWriter( fw );
            }
            bw.append( item + "\n" );
        }
        if( bw != null )
        {
            bw.close();
            fw.close();
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
     * Writes updated source documents to disk and flushes the list of updated docs.
     * 
     * @author Andrew
     */
    protected void writeUpdatedDocs()
    {
        this.debugbot = false;
        
        Set<Entry<String,SourceDocument>> entries = updatedDocs.entrySet();

        for ( Entry<String,SourceDocument> entry : entries )
        {
            String path = entry.getKey();
            SourceDocument doc = entry.getValue();
            // Append this file to the pathname
            File file = new File(SOURCE_DIR, path);
            
            // Make parent dirs if they don't exist
            file.getParentFile().mkdirs();
            
            try
            {   
                file.delete();
                file.createNewFile();

                // Write the simplified source document to the file
                FileOutputStream fos = new FileOutputStream(file);
                ObjectOutputStream out = new ObjectOutputStream(fos);
                out.writeObject(doc.simplified(Long.MAX_VALUE));
                out.close();
                fos.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        
        updatedDocs.clear();
    }
    
    /**
     * Write profiles that have been modified to disk
     * 
     * @author Andrew
     */
    protected void writeUpdatedProfiles()
    {
        Iterator<String> itr = updatedProfiles.iterator();
        while( itr.hasNext() )
        {
            try
            {
                String username = itr.next();
                File file = new File( PROFILE_DIR, username + ".dat" );
                file.delete();
                file.createNewFile();
                
                FileOutputStream fos = new FileOutputStream(file);
                ObjectOutputStream out = new ObjectOutputStream(fos);
                out.writeObject( profiles.get( username ) );
                out.close();
                fos.close();
                updatedProfiles.remove( username );
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
    
    @Deprecated
    /**
     * Write all profiles back to disk
     * 
     * @author Andrew
     */
    private void writeAllProfiles()
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

    public boolean isDebugbot()
    {
        return this.debugbot;
    }
}
