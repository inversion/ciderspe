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

package cider.common.processes;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JPanel;

import cider.documentViewerComponents.EditorTypingArea;

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
        int offset = (id - 1) * 100;
        w.setLocation(w.getX() + offset, w.getY() + offset);
        SourceDocument sourceDocument = new SourceDocument(
                "test.SourceDocument");
        EditorTypingArea eta = new EditorTypingArea(sourceDocument);
        eta.setWaiting(false);
        SDDemoPanel panel = new SDDemoPanel(w, w.getSize(), this.server,
                this.server.timer, id, eta);
        w.add(panel);
        w.pack();
        w.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        w.setVisible(true);
        PseudoClient client = new PseudoClient(panel, id, sourceDocument);
        this.server.addClient(client);
        panel.setClient(client);
    }

    public class PseudoServer implements ICodeLocation
    {
        private SourceDocument sourceDocument = new SourceDocument(
                "Bot Document");
        private ArrayList<ICodeLocation> clients = new ArrayList<ICodeLocation>();
        private Timer timer = new Timer();
        private long delay = 1;
        private long period = 1000;
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
                        lastPush = currentTime - 1;
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
        SourceDocument sourceDocument;
        long lastUpdateTime;
        SDDemoPanel panel;
        private Timer timer = new Timer();
        private long delay = 0;
        private long period = 100;
        int id;

        public PseudoClient(final SDDemoPanel panel, final int id,
                SourceDocument sourceDocument)
        {
            this.sourceDocument = sourceDocument;
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
            this.id = id;

            this.panel.eta.addActionListener(new ActionListener()
            {

                @Override
                public void actionPerformed(ActionEvent e)
                {
                    Queue<TypingEvent> outgoingEvents = new LinkedList<TypingEvent>();
                    TypingEvent te = (TypingEvent) e.getSource();
                    outgoingEvents.add(te);
                    System.out.println("push to server: " + te);
                    server.push(outgoingEvents);
                }

            });
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
                    this.panel.eta.updateText();
                    // panel.updateText(sourceDocument.toString());
                }// System.out.println(id + " : " + sourceDocument);
                catch (Exception e)
                {
                    e.printStackTrace();
                    System.exit(1);
                }
                /*
                 * for (TypingEvent te : events) {
                 * 
                 * 
                 * switch (te.mode) { case insert: if (te.position >=
                 * panel.eta.getCaretPosition()) panel.eta.moveRight(); break;
                 * case overwrite: if (te.position >=
                 * panel.eta.getCaretPosition()) panel.eta.moveRight(); break;
                 * case backspace: if (te.position >=
                 * panel.eta.getCaretPosition()) panel.eta.moveLeft(); break; }
                 * 
                 * }
                 */
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
        private static final long serialVersionUID = 1L;
        EditorTypingArea eta;
        ICodeLocation server;
        ICodeLocation client;
        int id;

        public SDDemoPanel(JFrame w, Dimension size,
                final ICodeLocation server, final Timer timer, final int id,
                final EditorTypingArea eta)
        {
            this.id = id;
            this.setSize(size);
            this.eta = eta;
            this.eta.setPreferredSize(size);
            this.add(this.eta);
            this.server = server;
            w.addKeyListener(new KeyListener()
            {

                @Override
                public void keyPressed(KeyEvent ke)
                {
                    switch (ke.getKeyCode())
                    {
                    case KeyEvent.VK_LEFT:
                        eta.moveLeft( false );
                        break;
                    case KeyEvent.VK_RIGHT:
                        eta.moveRight( false );
                        break;
                    case KeyEvent.VK_UP:
                        eta.moveUp( false );
                        break;
                    case KeyEvent.VK_DOWN:
                        eta.moveDown( false );
                        break;
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
                            if (eta.getCaretPosition() > 0
                                    && eta.currentPositionLocked(-1,
                                            "Demo User " + id) < 2)
                            {
                                mode = TypingEventMode.backspace;
                            }
                            else
                                return;
                        }
                            break;
                        // case '\u0027':
                        // tryCaretPosition(position + 1);
                        // break;
                        default:
                        {
                            if (eta.currentPositionLocked(0, "Demo User " + id) == 2)
                                return;
                        }
                        }

                        long t = System.currentTimeMillis();

                        TypingEvent te = new TypingEvent(t, mode, eta
                                .getCaretPosition(), 1, String.valueOf(ke
                                .getKeyChar()), "Demo User " + id, null);
                        System.out.println("push to server: " + te);
                        outgoingEvents.add(te);

                        Queue<TypingEvent> internal = new LinkedList<TypingEvent>(
                                outgoingEvents);
                        server.push(outgoingEvents);
                        client.push(internal);

                        switch (mode)
                        {
                        case insert:
                            eta.moveRight( false );
                            break;
                        case overwrite:
                            eta.moveRight( false );
                            break;
                        case backspace:
                            eta.moveLeft( false );
                            break;
                        }
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

        public void setClient(ICodeLocation client)
        {
            this.client = client;
        }
    }
}
