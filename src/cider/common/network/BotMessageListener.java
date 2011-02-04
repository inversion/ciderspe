package cider.common.network;

import java.util.LinkedList;
import java.util.Queue;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

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
        String body = message.getBody();
        // TODO: XML-ize this and get filelist??
        if (body.startsWith("quit"))
        {
            source.endSession(name);
        }
        else if (body.equals("getfilelist"))
        {
            try
            {
                // chat.sendMessage("filelist=" +
                // Base64.encodeObject(filelist));
                String xml = this.bot.getRootFolder().xml("");
                chat.sendMessage("filelist=" + xml);

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
        else if (body.startsWith("pullEventsSince("))
        {
            String arg = body.split("\\(")[1];
            arg = arg.split("\\)")[0];
            long t = Long.parseLong(arg);

            Queue<LocalisedTypingEvents> events = this.bot.getRootFolder()
                    .eventsSince(t, "");
            String instructions = "";
            for (LocalisedTypingEvents ltes : events)
                for (TypingEvent te : ltes.typingEvents)
                    instructions += "pushto(" + ltes.path + ") " + te.pack()
                            + "\n";
            try
            {
                chat.sendMessage(instructions);
            }
            catch (XMPPException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        else if (body.startsWith("pushto("))
        {
            String[] instructions = body.split("\\n");
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

            /*
             * String[] preAndAfter = body.split(") "); String[] pre =
             * preAndAfter[0].split("("); String dest = pre[1];
             * Queue<TypingEvent> typingEvents = new LinkedList<TypingEvent>();
             * typingEvents.add(new TypingEvent(preAndAfter[1]));
             * this.source.getRootFolder().path(dest).push(typingEvents);
             */
        }
        /*
         * else if (body.startsWith("<putfile>")) { if (matcher != null) matcher
         * = matcher.reset(body); else matcher = putFileMatch.matcher(body);
         * 
         * if (matcher.matches()) { try { // Write the file String path = new
         * String(Base64.decode(matcher.group(1))); FileWriter fwriter = new
         * FileWriter(path); fwriter.write(new
         * String(Base64.decode(matcher.group(2)))); fwriter.flush();
         * fwriter.close();
         * 
         * // Replace the CiderFile object in the CiderFileList
         * filelist.table.remove(path); filelist.table.put(path, new
         * CiderFile(path));
         * 
         * // TODO: Send updated file list to clients now? } catch (IOException
         * e) { // TODO Auto-generated catch block e.printStackTrace(); } }
         * 
         * }
         */
    }

}
