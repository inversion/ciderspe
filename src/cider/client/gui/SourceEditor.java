package cider.client.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import cider.common.network.Client;
import cider.common.processes.TypingEvent;
import cider.common.processes.TypingEventMode;
import cider.specialcomponents.EditorTypingArea;

/**
 * This class basically means that the TextAreas can constantly update and hold
 * lots of data (which is easier than using an ArrayList in MainWindow.java :3
 * 
 * @author Lawrence + Jon
 * 
 */
@SuppressWarnings("serial")
public class SourceEditor extends JPanel
{
    private EditorTypingArea eta;
    private Component tabHandle = null;
    private Client client;

    public SourceEditor(final EditorTypingArea eta, Client client)
    {
        this.eta = eta;
        this.eta.addComponentListener(new TabSelectionFocusGainListener());
        this.eta.addKeyListener(this.newKeyListener());
        this.client = client;
    }

    /**
     * A workaround to a bug in the JTabbedPane component:
     * 
     * @author Mårten Gustafsson
     * 
     */
    public class TabSelectionFocusGainListener implements ComponentListener
    {

        public TabSelectionFocusGainListener()
        {
            super();
        }

        public void componentResized(ComponentEvent e)
        {
        }

        public void componentMoved(ComponentEvent e)
        {
        }

        public void componentShown(ComponentEvent e)
        {
            Component component = e.getComponent();
            Container parent = component.getParent();
            if (parent instanceof JTabbedPane)
            {
                JTabbedPane tabbed = (JTabbedPane) parent;
                if (tabbed.getSelectedComponent() == component)
                    component.requestFocusInWindow();
            }
        }

        public void componentHidden(ComponentEvent e)
        {
        }
    }

    // ////// End of workaround ////////

    private ComponentListener newTabSelectionFocusGainListener()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void requestFocus()
    {
        Thread.dumpStack();
        super.requestFocus();
    }

    public EditorTypingArea getEditorTypingArea()
    {
        return this.eta;
    }

    public void setTabHandle(Component tabHandle)
    {
        this.tabHandle = tabHandle;
    }

    public Component getTabHandle()
    {
        return this.tabHandle;
    }

    public KeyListener newKeyListener()
    {
        KeyListener k = new KeyListener()
        {
            @Override
            public void keyPressed(KeyEvent ke)
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

                    TypingEvent te = new TypingEvent(
                            System.currentTimeMillis(), mode, eta
                                    .getCaretPosition(), String.valueOf(ke
                                    .getKeyChar()));
                    System.out.println("push to server: " + te);
                    outgoingEvents.add(te);
                    client.pushToServer(outgoingEvents);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            @Override
            public void keyReleased(KeyEvent e)
            {
                // TODO Auto-generated method stub

            }

            @Override
            public void keyTyped(KeyEvent e)
            {

            }
        };
        return k;
    }
}
