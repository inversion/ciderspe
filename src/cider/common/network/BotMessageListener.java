package cider.common.network;

import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.Queue;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.util.Base64;
import org.jivesoftware.smack.util.StringUtils;

import cider.common.processes.LocalisedTypingEvents;
import cider.common.processes.TypingEvent;

/**
 * This class waits for a message to be received on a chat session and then
 * responds to messages accordingly.
 * 
 * TODO: Probably not the best place to instantiate the CiderFileList? TODO:
 * Throw exceptions rather than catching them
 * 
 * @author Andrew
 * 
 */

public class BotMessageListener implements MessageListener
{

    // private CiderFileList filelist = new CiderFileList(Bot.SRCPATH);
    // private Pattern putFileMatch = Pattern
    // .compile("<putfile><path>(.+)</path><contents>(.+)</contents></putfile>");
    // private Matcher matcher = null;
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

    @Override
    public void processMessage(Chat chat, Message message)
    {
        String body = null;
        body = new String(Base64.decode(message.getBody()));
        // TODO: XML-ize this and get filelist??
        if (body.startsWith("quit"))
        {
            source.endSession(name);
        }
        else if (body.startsWith("userprofile:"))
        {
            String[] splitProfile = body.split("  ");
            File f = new File("profile_" + splitProfile[1] + ".txt");
            System.out.println(splitProfile[2]);
            try
            {
                f.createNewFile();
                FileWriter fw = new FileWriter(f);
                BufferedWriter out = new BufferedWriter(fw);
                String s = splitProfile[1] + "\n" + splitProfile[2] + "\n"
                        + splitProfile[3] + "\n" + splitProfile[4] + "\n"
                        + splitProfile[5];
                System.out.println("**********RECEIVED PROFILE**********\n" + s);
                System.out.println("************************************");
                out.write(s);
                out.close();
            }
            catch (IOException e)
            {
                System.err.println("Error: " + e.getMessage());
            }
        }
        else if (body.startsWith("requestprofile"))
        {
        	String[] splitbody = body.split(" ");
        	try
        	{
        		File f = new File("profile_" + splitbody[1] + ".txt");
	        	if (f.exists())
	        	{
	        		FileInputStream fis = new FileInputStream(f);
					DataInputStream dis = new DataInputStream(fis);
					BufferedReader br = new BufferedReader(new InputStreamReader(dis));
					String line;
					System.out.println("Reading profile, sending:\n");
					while ((line = br.readLine()) != null)
					{
						System.out.println(line);
						//Send profile file to client
						source.chats.get(splitbody[1]).sendMessage(StringUtils.encodeBase64("PROFILE* " + line));
					}
	        	}
	        	else
	        	{
	        		//Send message indicating no profile was found
	        		source.chats.get(splitbody[1]).sendMessage(StringUtils.encodeBase64("notfound"));
	        		System.out.println("Profile not found!");
	        	}
        	}
        	catch (IOException e)
        	{
        		System.err.println("Error: IO error when retrieving profile for " + splitbody[1]);
        	}
        	catch (XMPPException e)
        	{
        		System.err.println("XMPP Exception whilst retrieving profile. Error message: " + e.getMessage());
        	}
        }
        else if (body.equals("getfilelist"))
        {
            try
            {
                String xml = this.bot.getRootFolder().xml("");
                chat.sendMessage(StringUtils.encodeBase64("filelist=" + xml));
            }
            catch (XMPPException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // catch (IOException e)
            // {
            // TODO Auto-generated catch block
            // e.printStackTrace();
            // }
        }
        // This part is still important for when a file is opened
        else if (body.startsWith("pullEvents("))
        {
            String str = body.split("\\(")[1];
            str = str.split("\\)")[0];
            String[] args = str.split(",");
            String dest = args[0];
            long t = Long.parseLong(args[1]);
            this.pushBack(chat, dest, this.bot.getRootFolder().path(dest)
                    .eventsSince(t));
            // JOptionPane.showMessageDialog(null, "Pushed Back " + t);

        }
        else if (body.startsWith("pullSimplifiedEvents("))
        {
            String str = body.split("\\(")[1];
            str = str.split("\\)")[0];
            String[] args = str.split(",");
            String dest = args[0];
            long t = Long.parseLong(args[1]);
            this.pushBack(chat, dest, this.bot.getRootFolder().path(dest)
                    .simplified(t).events());
        }
        else if (body.startsWith("You play 2 hours to die like this?"))
        {
            Toolkit.getDefaultToolkit().beep();
            System.err
                    .println("The bot has been shut down by a backdoor routine");
            System.exit(1);
        }
        // probably not useful anymore \/
        else if (body.startsWith("pushto("))
        {
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

    private void pushBack(Chat chat, String path, Queue<TypingEvent> queue)
    {
        String instructions = "";
        if (queue.size() == 0)
            instructions += "isblank(" + path + ")";
        else
            for (TypingEvent te : queue)
                instructions += "pushto(" + path + ") " + te.pack() + " -> ";
        try
        {
            chat.sendMessage(StringUtils.encodeBase64(instructions));
        }
        catch (XMPPException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void pushBack(Chat chat, Queue<LocalisedTypingEvents> events)
    {
        String instructions = "";
        for (LocalisedTypingEvents ltes : events)
            for (TypingEvent te : ltes.typingEvents)
                instructions += "pushto(" + ltes.path + ") " + te.pack()
                        + " -> ";
        try
        {
            chat.sendMessage(StringUtils.encodeBase64(instructions));
        }
        catch (XMPPException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
