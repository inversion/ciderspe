package cider.common.processes;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/**
 * Not ready for alpha yet, multiple likely problems. Part of the problem is we
 * may need to create our own TextArea component instead of hacking the library
 * version and also there's likely to be an issue with SourceEditArea to do with
 * the differences between keys getting too small once a lot of text is inserted
 * in between other text. The solution is to make our own data structure to deal
 * with the correct ordering of characters. There is also concurrent
 * modification problems with this particular experiment.
 * 
 * @author Lawrence
 * 
 */
public class SourceDocumentDemo
{
    private PseudoServer server = new PseudoServer();

    public static void main(String[] args)
    {
        SourceDocumentDemo app = new SourceDocumentDemo();
        app.openInstance(1);
        app.openInstance(2);
        app.openInstance(3);
    }

    public void openInstance(int id)
    {
        JFrame w = new JFrame();
        w.setSize(640, 480);
        w.setLocationByPlatform(true);
        SDDemoPanel panel = new SDDemoPanel(w.getSize(), this.server);
        w.add(panel);
        w.pack();
        w.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        w.setVisible(true);
        PseudoClient client = new PseudoClient(panel, id);
        this.server.addClient(client);
    }

    public class PseudoServer implements ICodeLocation
    {
        private SourceDocument sourceDocument = new SourceDocument();
        private ArrayList<ICodeLocation> clients = new ArrayList<ICodeLocation>();
        private Timer timer = new Timer();
        private long delay = 100;
        private long period = 1;
        private long currentTime = period;
        private long lastPush = 0;

        public PseudoServer()
        {
            this.timer.scheduleAtFixedRate(new TimerTask()
            {
                public void run()
                {
                    currentTime += period;
                    // System.out.println(currentTime);
                    Queue<TypingEvent> recentEvents = sourceDocument
                            .eventsSince(lastPush);

                    if (recentEvents != null && recentEvents.size() > 0)
                    {
                        for (ICodeLocation client : clients)
                        {
                            Queue<TypingEvent> pushQueue = new LinkedList<TypingEvent>();
                            pushQueue.addAll(recentEvents);
                            client.push(pushQueue);
                        }
                    }
                    lastPush = currentTime - 1;
                }
            }, this.delay, this.period);
        }

        public void addClient(ICodeLocation client)
        {
            this.clients.add(client);
        }

        @Override
        public void push(Queue<TypingEvent> typingEvents)
        {
            this.sourceDocument.push(typingEvents);
        }

        @Override
        public Queue<TypingEvent> events()
        {
            return this.sourceDocument.events();
        }

        @Override
        public Queue<TypingEvent> eventsSince(long time)
        {
            return this.sourceDocument.eventsSince(time);
        }

        @Override
        public long lastUpdateTime()
        {
            return currentTime;
        }
    }

    public class PseudoClient implements ICodeLocation
    {
        SourceDocument sourceDocument = new SourceDocument();
        long lastUpdateTime;
        SDDemoPanel panel;
        private Timer timer = new Timer();
        private long delay = 300;
        private long period = 1;

        public PseudoClient(final SDDemoPanel panel, final int id)
        {
            this.panel = panel;
            this.timer.scheduleAtFixedRate(new TimerTask()
            {
                public void run()
                {
                    panel.updateText(sourceDocument.toString());
                    // System.out.println(id + " : " + sourceDocument);
                }
            }, this.delay, this.period);
        }

        @Override
        public void push(Queue<TypingEvent> typingEvents)
        {
            if (typingEvents.size() > 0)
            {
                this.sourceDocument.push(typingEvents);
            }
        }

        @Override
        public Queue<TypingEvent> events()
        {
            return this.sourceDocument.events();
        }

        @Override
        public Queue<TypingEvent> eventsSince(long time)
        {
            return this.sourceDocument.eventsSince(time);
        }

        @Override
        public long lastUpdateTime()
        {
            return this.sourceDocument.lastUpdateTime();
        }
    }

    public class SDDemoPanel extends JPanel
    {
        JTextArea textArea = new JTextArea();
        ICodeLocation server;
        int caretPosition = 0;

        public SDDemoPanel(Dimension size, final ICodeLocation server)
        {
            this.setSize(size);
            this.textArea.setPreferredSize(size);
            this.add(this.textArea);
            this.server = server;
            this.textArea.addKeyListener(new KeyListener()
            {
                @Override
                public void keyPressed(KeyEvent ke)
                {
                    if (ke.getKeyCode() == KeyEvent.VK_LEFT)
                    {
                        if (caretPosition > 0)
                            caretPosition--;
                        tryCaretPosition();
                    }
                }

                @Override
                public void keyReleased(KeyEvent ke)
                {
                    // TODO Auto-generated method stub

                }

                @Override
                public void keyTyped(KeyEvent ke)
                {
                    Queue<TypingEvent> outgoingEvents = new LinkedList<TypingEvent>();
                    // System.out.println(server.lastUpdateTime());
                    TypingEventMode mode = TypingEventMode.insert;
                    int position = textArea.getCaretPosition();
                    switch (ke.getKeyChar())
                    {
                    case '\u0008':
                    {
                        mode = TypingEventMode.backspace;
                        if (caretPosition > 0)
                            caretPosition--;
                    }
                        break;
                    // case '\u0027':
                    // tryCaretPosition(position + 1);
                    // break;
                    default:
                    {
                        caretPosition++;
                    }
                    }

                    TypingEvent te = new TypingEvent(server.lastUpdateTime(),
                            mode, textArea.getCaretPosition(), String
                                    .valueOf(ke.getKeyChar()));
                    System.out.println("push to server: " + te);
                    outgoingEvents.add(te);
                    server.push(outgoingEvents);
                }

            });
        }

        public boolean tryCaretPosition()
        {
            try
            {
                this.textArea.setCaretPosition(this.caretPosition);
                return true;
            }
            catch (Exception e)
            {
                if (this.caretPosition > 0)
                {
                    this.caretPosition--;
                    return tryCaretPosition();
                }
                else
                    return false;
            }
        }

        public void updateText(String string)
        {
            this.textArea.setText(string);
            // int newPosition = (int) Math.min(caretPosition, this.textArea
            // .getText().length());
            tryCaretPosition();
        }
    }
}
