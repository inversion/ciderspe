package cider.common.network;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.regex.Matcher;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

import cider.common.processes.CiderFileList;
import cider.common.processes.LocalisedTypingEvents;
import cider.common.processes.TypingEvent;
import cider.specialcomponents.Base64;

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

public class ServerMessageListener implements MessageListener
{

    private CiderFileList filelist = new CiderFileList(Common.SRCPATH);
    // private Pattern putFileMatch = Pattern
    // .compile("<putfile><path>(.+)</path><contents>(.+)</contents></putfile>");
    private Matcher matcher = null;
    private Server source;

    public ServerMessageListener(Server source)
    {
        this.source = source;
    }

    @Override
    public void processMessage(Chat chat, Message message)
    {
        String body = message.getBody();
        // TODO: XML-ize this and get filelist??
        if (body.startsWith("getfile="))
        {
            try
            {
                // TODO: Error checking if file doesn't exist??
                String encPath = body.substring(8, body.length());
                String path = new String(Base64.decode(encPath.getBytes()));
                String contents = filelist.table.get(path).getFileContents();
                String encContents = Base64.encodeBytes(contents.getBytes());

                chat.sendMessage("<file><path>" + encPath + "</path><contents>"
                        + encContents + "</contents></file>");

            }
            catch (XMPPException e)
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
        else if (body.equals("getfilelist"))
        {
            try
            {
                // chat.sendMessage("filelist=" +
                // Base64.encodeObject(filelist));
                String xml = this.source.getRootFolder().xml("");
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

            Queue<LocalisedTypingEvents> events = this.source.getRootFolder()
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
            String[] preAndAfter = body.split(") ");
            String[] pre = preAndAfter[0].split("(");
            String dest = pre[1];
            Queue<TypingEvent> typingEvents = new LinkedList<TypingEvent>();
            typingEvents.add(new TypingEvent(preAndAfter[1]));
            this.source.getRootFolder().path(dest).push(typingEvents);
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
