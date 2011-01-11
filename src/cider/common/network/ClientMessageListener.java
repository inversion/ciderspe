package cider.common.network;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JTabbedPane;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;

import cider.client.gui.DirectoryViewComponent;
import cider.client.gui.SourceEditor;
import cider.specialcomponents.Base64;

/**
 * This class waits for a message to be received by the client on its chat
 * session with the server.
 * 
 * 
 * @author Andrew
 * 
 */

public class ClientMessageListener implements MessageListener
{

    // TODO: These probably shouldn't be public
    public DirectoryViewComponent dirView;
    public JTabbedPane tabbedPane;

    private Pattern fileMatch = Pattern
            .compile("<file><path>(.+)</path><contents>(.+)</contents></file>");
    private Matcher matcher = null;

    public ClientMessageListener(DirectoryViewComponent dirView,
            JTabbedPane tabbedPane)
    {
        this.dirView = dirView;
        this.tabbedPane = tabbedPane;
    }

    @Override
    public void processMessage(Chat chat, Message message)
    {
        String body = message.getBody();
        if (body.startsWith("<file>"))
        {
            try
            {
                if (matcher != null)
                    matcher = matcher.reset(body);
                else
                    matcher = fileMatch.matcher(body);

                if (matcher.matches())
                {
                    String path = new String(Base64.decode(matcher.group(1)));
                    String contents = new String(Base64
                            .decode(matcher.group(2)));
                    System.out.println("got " + contents);
                    tabbedPane.addTab(path, new SourceEditor(contents, path));
                    // tabbedPane.setSelectedIndex(++currentTab);
                }

            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        else if (body.startsWith("filelist="))
        {
            /*
             * try { dirView.constructTree( (CiderFileList)
             * Base64.decodeToObject( body.substring( 9, body.length() ) ) ); }
             * catch (IOException e) { // TODO Auto-generated catch block
             * e.printStackTrace(); } catch (ClassNotFoundException e) { // TODO
             * Auto-generated catch block e.printStackTrace(); }
             */

            String xml = body.split("filelist=")[1];
            this.dirView.constructTree(xml);
        }
    }
}
