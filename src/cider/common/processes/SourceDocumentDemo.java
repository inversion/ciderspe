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

import cider.specialcomponents.EditorTypingArea;

/**
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
        SDDemoPanel panel = new SDDemoPanel(w, w.getSize(), this.server,
                this.server.timer);
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
        private long delay = 1;
        private long period = 50;
        private long currentTime = System.currentTimeMillis();
        private long lastPush = 0;

        public PseudoServer()
        {
            this.timer.scheduleAtFixedRate(new TimerTask()
            {
                public void run()
                {
                    try
                    {
                        currentTime = System.currentTimeMillis();
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
                        lastPush = currentTime;
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        System.exit(1);
                    }
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

        @Override
        public void clearAll()
        {
            this.sourceDocument.clearAll();
        }
    }

    public class PseudoClient implements ICodeLocation
    {
        SourceDocument sourceDocument = new SourceDocument();
        long lastUpdateTime;
        SDDemoPanel panel;
        private Timer timer = new Timer();
        private long delay = 0;
        private long period = 100;

        public PseudoClient(final SDDemoPanel panel, final int id)
        {
            this.panel = panel;
            this.timer.scheduleAtFixedRate(new TimerTask()
            {
                public void run()
                {
                    /*
                     * try { panel.updateText(sourceDocument.toString()); }//
                     * System.out.println(id + " : " + sourceDocument); catch
                     * (Exception e) { e.printStackTrace(); System.exit(1); }
                     */

                }
            }, this.delay, this.period);
        }

        @Override
        public void push(Queue<TypingEvent> typingEvents)
        {
            if (typingEvents.size() > 0)
            {
                TypingEvent[] events = new TypingEvent[typingEvents.size()];
                typingEvents.toArray(events);
                this.sourceDocument.push(typingEvents);

                try
                {
                    panel.updateText(sourceDocument.toString());
                }// System.out.println(id + " : " + sourceDocument);
                catch (Exception e)
                {
                    e.printStackTrace();
                    System.exit(1);
                }

                for (TypingEvent te : events)
                {
                    switch (te.mode)
                    {
                    case insert:
                        if (te.position >= panel.eta.getCaretPosition())
                            panel.eta.moveRight();
                        break;
                    case overwrite:
                        if (te.position >= panel.eta.getCaretPosition())
                            panel.eta.moveRight();
                        break;
                    case backspace:
                        if (te.position >= panel.eta.getCaretPosition())
                            panel.eta.moveLeft();
                        break;
                    }
                }
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

        @Override
        public void clearAll()
        {
            this.sourceDocument.clearAll();
        }
    }

    public class SDDemoPanel extends JPanel
    {
        EditorTypingArea eta = new EditorTypingArea();
        ICodeLocation server;

        public SDDemoPanel(JFrame w, Dimension size,
                final ICodeLocation server, final Timer timer)
        {
            this.setSize(size);
            this.eta.setPreferredSize(size);
            this.add(this.eta);
            this.server = server;
            w.addKeyListener(new KeyListener()
            {
                @Override
                public void keyPressed(KeyEvent ke)
                {
                    if (ke.getKeyCode() == KeyEvent.VK_LEFT)
                    {
                        eta.moveLeft();
                    }
                    else if (ke.getKeyCode() == KeyEvent.VK_RIGHT)
                    {
                        eta.moveRight();
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
                    try
                    {
                        Queue<TypingEvent> outgoingEvents = new LinkedList<TypingEvent>();
                        // System.out.println(server.lastUpdateTime());
                        TypingEventMode mode = TypingEventMode.insert;
                        switch (ke.getKeyChar())
                        {
                        case '\u0008':
                        {
                            mode = TypingEventMode.backspace;
                            // eta.moveLeft();
                        }
                            break;
                        // case '\u0027':
                        // tryCaretPosition(position + 1);
                        // break;
                        default:
                        {
                            // eta.moveRight();
                        }
                        }

                        TypingEvent te = new TypingEvent(System
                                .currentTimeMillis(), mode, eta
                                .getCaretPosition(), String.valueOf(ke
                                .getKeyChar()));
                        System.out.println("push to server: " + te);
                        outgoingEvents.add(te);
                        server.push(outgoingEvents);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        timer.cancel();
                        System.exit(1);
                    }
                }
            });
        }

        public void updateText(String string)
        {
            try
            {
                this.eta.setText(string);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                System.exit(1);
            }
            catch (Throwable e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
