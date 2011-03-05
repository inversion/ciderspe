package cider.client.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashSet;
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
    // Keywords for syntax highlighting
    public static HashSet<String> keywords = new HashSet<String>();
    public static HashSet<String> comments = new HashSet<String>();

    private EditorTypingArea eta;
    private Component tabHandle = null;
    private Client client;
    private String path;

    public SourceEditor(final EditorTypingArea eta, Client client, String path)
    {
        this.eta = eta;
        this.eta.addComponentListener(new TabSelectionFocusGainListener());
        this.eta.addKeyListener(this.newKeyListener());
        this.eta.addMouseListener(this.newMouseListener());
        this.eta.addActionListener(this.lockingActionListener());
        this.eta.setFocusTraversalKeysEnabled(false);
        this.client = client;
        this.path = path;

        String[] keywordArray = "instanceof assert if else switch case default break goto return for while do continue new throw throws try catch finally this super extends implements import true false null package transient strictfp void char short int long double float const static volatile byte boolean class interface native private protected public final abstract synchronized enum"
                .split(" ");
        for (int i = 0; i < keywordArray.length; i++)
            keywords.add(keywordArray[i]);

        String[] CommentArray = "/* */".split(" ");
        for (int i = 0; i < CommentArray.length; i++)
            comments.add(CommentArray[i]);
    }

    private ActionListener lockingActionListener()
    {
        return new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                Queue<TypingEvent> outgoingEvents = new LinkedList<TypingEvent>();
                Queue<TypingEvent> internal = new LinkedList<TypingEvent>();
                TypingEvent te = (TypingEvent) e.getSource();
                outgoingEvents.add(te);
                internal.add(te);
                System.out.println("push to server: " + te);
                // eta.getCodeLocation().push(internal);
                // eta.updateUI();
                client.broadcastTypingEvents(outgoingEvents, path);
            }

        };
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

    private MouseListener newMouseListener()
    {
        MouseListener m = new MouseListener()
        {

            @Override
            public void mouseClicked(MouseEvent arg0)
            {
                // TODO Auto-generated method stub

            }

            @Override
            public void mouseEntered(MouseEvent arg0)
            {
                // TODO Auto-generated method stub

            }

            @Override
            public void mouseExited(MouseEvent arg0)
            {
                // TODO Auto-generated method stub

            }

            @Override
            public void mousePressed(MouseEvent arg0)
            {
                SourceEditor.this.eta.requestFocusInWindow();
            }

            @Override
            public void mouseReleased(MouseEvent arg0)
            {
                // TODO Auto-generated method stub

            }

        };

        return m;
    }

    public KeyListener newKeyListener()
    {
        KeyListener k = new KeyListener()
        {
            @Override
            public void keyPressed(KeyEvent ke)
            {
                switch (ke.getKeyCode())
                {
                case KeyEvent.VK_LEFT:
                    eta.moveLeft();
                    break;
                case KeyEvent.VK_RIGHT:
                    eta.moveRight();
                    break;
                case KeyEvent.VK_UP:
                    eta.moveUp();
                    break;
                case KeyEvent.VK_DOWN:
                    eta.moveDown();
                    break;
                case KeyEvent.VK_HOME:
                	eta.moveHome();
                	break;
                case KeyEvent.VK_END:
                	eta.moveEnd();
                	break;
                case KeyEvent.VK_PAGE_UP:
                	eta.movePageUp();
                	break;
                case KeyEvent.VK_PAGE_DOWN:
                	eta.movePageDown();
                	break;
                case KeyEvent.VK_CONTROL: //&& KeyEvent.VK_HOME:
                	//switch (ke.getKeyCode())
                	{
                	//case  KeyEvent.VK_HOME:
                		eta.moveDocHome();
                		break;
                	//case KeyEvent.VK_SHIFT:
                		//eta.moveDocEnd();
                		//break;
                	}
                case KeyEvent.VK_SHIFT:
                	eta.moveDocEnd();
                	break;
                }
            }

            @Override
            public void keyReleased(KeyEvent e)
            {
                // TODO Auto-generated method stub

            }

            @Override
            public void keyTyped(KeyEvent ke)
            {
                if (eta.currentPositionLocked(0))
                {
                    System.out.println("Current position locked!");
                }
                else
                {
                    switch (ke.getKeyCode())
                    {
                    default:
                    {
                        try
                        {
                            // System.out.println(server.lastUpdateTime());
                            TypingEventMode mode = TypingEventMode.insert;
                            String chr;

                            switch (ke.getKeyChar())
                            {
                            case '\u007F':
                            {
                                if (eta.getCaretPosition() >= 0)
                                {
                                    mode = TypingEventMode.backspace;
                                    chr = " ";
                                }
                                else
                                    return;
                            }
                                break;
                            case '\u0008':
                            {
                                if (eta.getCaretPosition() >= 0)
                                {
                                    mode = TypingEventMode.backspace;
                                    chr = " ";
                                }
                                else
                                    return;
                            }
                                break;
                            case '\t':
                            {
                                chr = "    ";
                                SourceEditor.this.eta.requestFocusInWindow();
                            }
                                break;
                            default:
                                chr = String.valueOf(ke.getKeyChar());
                                break;
                            }

                            TypingEvent te = new TypingEvent(
                                    System.currentTimeMillis(), mode,
                                    eta.getCaretPosition(), chr.length(), chr,
                                    client.getUsername());
                            ArrayList<TypingEvent> particles = te.explode();

                            for (TypingEvent particle : particles)
                                System.out.println("push to server: "
                                        + particle);

                            Queue<TypingEvent> outgoingEvents = new LinkedList<TypingEvent>();
                            Queue<TypingEvent> internal = new LinkedList<TypingEvent>();

                            for (TypingEvent particle : particles)
                            {
                                outgoingEvents.add(particle);
                                internal.add(particle);
                            }

                            eta.getCodeLocation().push(internal);
                            eta.updateText();
                            client.broadcastTypingEvents(outgoingEvents, path);

                            switch (mode)
                            {
                            case insert:
                                eta.moveCaret(particles.size());
                                break;
                            case overwrite:
                                eta.moveCaret(particles.size());
                                break;
                            case backspace:
                                eta.moveLeft();
                                break;
                            }
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                    }
                }
            }
        };
        return k;
    }
}
