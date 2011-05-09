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

package cider.client.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import cider.common.network.client.Client;
import cider.common.processes.EventComparer;
import cider.common.processes.SiHistoryFiles;
import cider.common.processes.TypingEvent;
import cider.common.processes.TypingEventMode;
import cider.documentViewerComponents.EditorTypingArea;

/**
 * This class basically means that the TextAreas can constantly update and hold
 * lots of data (which is easier than using an ArrayList in MainWindow.java :3
 * 
 * @author Lawrence + Jon
 * 
 */
@SuppressWarnings("serial")
public class ETASourceEditorPane extends JScrollPane
{
    /**
     * A workaround to a bug in the JTabbedPane component:
     * 
     * @author M�rten Gustafsson
     * 
     */
    public class TabSelectionFocusGainListener implements ComponentListener
    {

        public TabSelectionFocusGainListener()
        {
            super();
        }

        public void componentHidden(ComponentEvent e)
        {
        }

        public void componentMoved(ComponentEvent e)
        {
        }

        public void componentResized(ComponentEvent e)
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
    }

    /**
     *  Keywords for syntax highlighting
     */
    public static HashSet<String> keywords = new HashSet<String>();
    protected EditorTypingArea eta;
    private Component tabHandle = null;
    private Client client;

    private String path;

    /**
     *  Mode of input, default to insert at caret
     */
    private TypingEventMode inputMode = TypingEventMode.insert;

    public ETASourceEditorPane(final EditorTypingArea eta, Client client,
            String path)
    {
        super(eta);
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

        this.setWheelScrollingEnabled(false);
        this.addMouseWheelListener(this.newMouseWheelListener());
        this.client.getParent().w.addWindowListener(this.newWindowListener());
        // FIXME: Caused exception
        SiHistoryFiles.markDocumentOpening(path, System.currentTimeMillis()
                + client.getClockOffset());
    }

    /**
     * Returns the editor typing area
     * @return Editor typing area
     */
    public EditorTypingArea getEditorTypingArea()
    {
        return eta;
    }

    /**
     * Returns the editor area tabs
     * @return The tabs
     */
    public Component getTabHandle()
    {
        return tabHandle;
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

    // ////// End of workaround ////////

    /**
     * Listener for typing into the editor area
     */
    public KeyListener newKeyListener()
    {
        KeyListener k = new KeyListener()
        {
            private void applyToSelection(TypingEventMode mode)
            {
                // Don't do anything if there's no selected region
                if (eta.getSelectedRegion() == null
                        || eta.getSelectedRegion().getLength() == 0)
                    return;

                Queue<TypingEvent> outgoingEvents = new LinkedList<TypingEvent>();
                Queue<TypingEvent> internal = new LinkedList<TypingEvent>();
                TypingEvent te = new TypingEvent(System.currentTimeMillis()
                        + client.getClockOffset(), mode, eta
                        .getSelectedRegion().start, eta.getSelectedRegion()
                        .getLength(), "", client.getUsername(), client
                        .getUsername());

                outgoingEvents.add(te);
                internal.add(te);
                System.out.println("push to server: " + te);
                eta.getSourceDocument().push(internal);
                client.broadcastTypingEvents(outgoingEvents, path);
                eta.updateUI();

                eta.updateText();
                eta.scrollRectToVisible(new Rectangle(0,
                        eta.getCurrentLine().y, eta.getWidth(),
                        EditorTypingArea.lineSpacing));
            }

            @Override
            public void keyPressed(KeyEvent ke)
            {
                client.shared.idleTimer.activityDetected();
                switch (ke.getKeyCode())
                {
                case KeyEvent.VK_LEFT:
                    eta.moveLeft(ke.isShiftDown());
                    break;
                case KeyEvent.VK_RIGHT:
                    eta.moveRight(ke.isShiftDown());
                    break;
                case KeyEvent.VK_UP:
                    eta.moveUp(ke.isShiftDown());
                    break;
                case KeyEvent.VK_DOWN:
                    eta.moveDown(ke.isShiftDown());
                    break;
                case KeyEvent.VK_HOME:
                    if (ke.isControlDown())
                        eta.moveDocHome(ke.isShiftDown());
                    else
                        eta.moveHome(ke.isShiftDown());
                    break;
                case KeyEvent.VK_END:
                    if (ke.isControlDown())
                        eta.moveDocEnd(ke.isShiftDown());
                    else
                        eta.moveEnd(ke.isShiftDown());
                    break;
                case KeyEvent.VK_PAGE_UP:
                    eta.movePageUp(ke.isShiftDown());
                    break;
                case KeyEvent.VK_PAGE_DOWN:
                    eta.movePageDown(ke.isShiftDown());
                    break;
                case KeyEvent.VK_4:
                    if (ke.isControlDown())
                        this.applyToSelection(TypingEventMode.lockRegion);
                    break;
                case KeyEvent.VK_R:
                    if (ke.isControlDown())
                        this.applyToSelection(TypingEventMode.unlockRegion);
                    break;
                case KeyEvent.VK_A:
                    if (ke.isControlDown())
                        eta.selectAll();
                    break;
                case KeyEvent.VK_C:
                    if (ke.isControlDown())
                        eta.copy();
                    break;
                case KeyEvent.VK_X:
                    if (ke.isControlDown())
                    {
                        eta.copy();
                        applyToSelection(TypingEventMode.delete);
                    }
                    break;
                case KeyEvent.VK_V:
                    if (ke.isControlDown())
                    {
                        String text = null;
                        Clipboard clipboard = getToolkit().getSystemClipboard();
                        // Credit help to
                        // http://www.javapractices.com/topic/TopicAction.do?Id=82
                        Transferable contents = clipboard.getContents(null);
                        boolean isText = (contents != null)
                                && contents
                                        .isDataFlavorSupported(DataFlavor.stringFlavor);

                        if (isText)
                            try
                            {
                                text = (String) contents
                                        .getTransferData(DataFlavor.stringFlavor);
                            }
                            catch (UnsupportedFlavorException e)
                            {
                                e.printStackTrace();
                            }
                            catch (IOException e)
                            {
                                e.printStackTrace();
                            }

                        if (text == null)
                            break;

                        TypingEventMode mode;
                        int position, length;

                        // If there's a region selected we need to overwrite
                        if (eta.getSelectedRegion() != null
                                && eta.getSelectedRegion().getLength() > 0)
                        {
                            mode = TypingEventMode.overwrite;
                            position = eta.getSelectedRegion().start;
                            length = eta.getSelectedRegion().getLength();
                        }
                        else
                        { // Otherwise just insert text at caret using default
                          // mode
                            mode = inputMode;
                            position = eta.getCaretPosition();
                            length = text.length();
                        }

                        // TODO: Doesn't handle locking regions
                        TypingEvent te = new TypingEvent(System
                                .currentTimeMillis()
                                + client.getClockOffset(), mode, position,
                                length, text, client.getUsername(), null);

                        Queue<TypingEvent> outgoingEvents = new LinkedList<TypingEvent>();
                        Queue<TypingEvent> internal = new LinkedList<TypingEvent>();
                        outgoingEvents.add(te);
                        internal.add(te);
                        System.out.println("push to server: " + te);
                        eta.getSourceDocument().push(internal);
                        client.broadcastTypingEvents(outgoingEvents, path);

                        eta.updateUI();

                        eta.updateText();
                        eta.scrollRectToVisible(new Rectangle(0, eta
                                .getCurrentLine().y, eta.getWidth(),
                                EditorTypingArea.lineSpacing));

                        eta.moveCaret(length);
                    }
                    break;
                case KeyEvent.VK_INSERT: // Switch default input mode between
                                         // insert and overtype
                    inputMode = (inputMode == TypingEventMode.insert) ? TypingEventMode.overwrite
                            : TypingEventMode.insert;

                    // Provide GUI indication of input mode (possibly block
                    // caret and status bar indicator)
                    MainWindow.statusBar.setInputMode(inputMode.toString()
                            .toUpperCase());

                    System.out.println("Input mode changed to "
                            + inputMode.toString());
                    break;
                }

            }

            @Override
            public void keyReleased(KeyEvent ke)
            {

            }

            @Override
            public void keyTyped(KeyEvent ke)
            {
                if (ke.isControlDown())
                    return;
                else
                {
                    int r = eta.currentPositionLocked(0, client.getUsername());
                    if (ke.isControlDown())
                        System.out.println("Control is down!");
                    else if (r == 2)
                        System.out.println("Current position locked!");
                    else
                    {
                        switch (ke.getKeyCode())
                        {
                        default:
                        {
                            try
                            {
                                // System.out.println(server.lastUpdateTime());
                                TypingEventMode mode = inputMode;
                                String chr;
                                int length = 1, position = eta
                                        .getCaretPosition();

                                switch (ke.getKeyChar())
                                {
                                case '\u007F': // Delete character
                                    mode = TypingEventMode.delete;
                                    chr = " ";
                                    // If there is a region selected change
                                    // start position to the start of this
                                    // region
                                    if (eta.getSelectedRegion() != null
                                            && eta.getSelectedRegion()
                                                    .getLength() > 0)
                                    {
                                        position = eta.getSelectedRegion().start;
                                        length = eta.getSelectedRegion()
                                                .getLength();
                                    }
                                    else if (position < 0) // TODO: Not sure if
                                                           // this ever happens
                                        position = 0;
                                    break;
                                case '\u0008': // Backspace char
                                    mode = TypingEventMode.backspace;
                                    chr = " ";

                                    // If there is a region selected change
                                    // start position to the start of this
                                    // region
                                    // And make it a delete event
                                    if (eta.getSelectedRegion() != null
                                            && eta.getSelectedRegion()
                                                    .getLength() > 0)
                                    {
                                        mode = TypingEventMode.delete;
                                        position = eta.getSelectedRegion().start;
                                        length = eta.getSelectedRegion()
                                                .getLength();
                                    }
                                    else if (position < 1)
                                        return;
                                    break;
                                case '\t':
                                    chr = "    ";
                                    length = 4;
                                    eta.requestFocusInWindow();

                                    client.shared.profile.incrementCharCount();
                                    break;
                                default:
                                    // If there's a region selected we need to
                                    // overwrite
                                    if (eta.getSelectedRegion() != null
                                            && eta.getSelectedRegion()
                                                    .getLength() > 0)
                                    {
                                        mode = TypingEventMode.overwrite;
                                        position = eta.getSelectedRegion().start;
                                        length = eta.getSelectedRegion()
                                                .getLength();
                                    }
                                    chr = String.valueOf(ke.getKeyChar());
                                    client.shared.profile.incrementCharCount();
                                    break;
                                }

                                TypingEvent te = new TypingEvent(System
                                        .currentTimeMillis()
                                        + client.getClockOffset(), mode,
                                        position, length, chr, client
                                                .getUsername(), r == 1 ? client
                                                .getUsername() : null);
                                ArrayList<TypingEvent> particles = te.explode();

                                for (TypingEvent particle : particles)
                                    System.out.println("push to server: "
                                            + particle);

                                Queue<TypingEvent> outgoingEvents = new LinkedList<TypingEvent>();
                                Queue<TypingEvent> internal = new LinkedList<TypingEvent>();
                                PriorityQueue<TypingEvent> toFile = new PriorityQueue<TypingEvent>(
                                        particles.size(), new EventComparer());

                                for (TypingEvent particle : particles)
                                {
                                    outgoingEvents.add(particle);
                                    internal.add(particle);
                                    toFile.add(particle);
                                }

                                SiHistoryFiles.saveEvents(toFile, client
                                        .getCurrentDocumentID().path);

                                eta.getSourceDocument().push(internal);
                                client.broadcastTypingEvents(outgoingEvents,
                                        path);

                                eta.updateText();

                                if (!eta.isEmpty())
                                    eta.scrollRectToVisible(new Rectangle(0,
                                            eta.getCurrentLine().y, eta
                                                    .getWidth(),
                                            EditorTypingArea.lineSpacing));
                                else if (CiderApplication.debugApp)
                                    System.out
                                            .println("Cannot scroll rect to visible because this is an empty document (current line would be null)");

                                switch (mode)
                                {
                                case insert:
                                    eta.moveCaret(particles.size());
                                    break;
                                case overwrite:
                                    eta.moveCaret(particles.size());
                                    break;
                                case backspace:
                                    eta.moveLeft(false);
                                    break;
                                case delete:
                                    break;
                                }
                            }
                            catch (Exception e)
                            {
                                System.err
                                        .println("There was a problem involving keyTyped");
                                e.printStackTrace();
                            }
                        }
                        }
                    }
                }
            }
        };
        return k;
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
                eta.requestFocusInWindow();
            }

            @Override
            public void mouseReleased(MouseEvent arg0)
            {
                // TODO Auto-generated method stub

            }

        };

        return m;
    }

    private MouseWheelListener newMouseWheelListener()
    {
        MouseWheelListener mwl = new MouseWheelListener()
        {

            @Override
            public void mouseWheelMoved(MouseWheelEvent mwe)
            {

                Rectangle rect = eta.getVisibleRect();
                rect.y += EditorTypingArea.lineSpacing
                        * (mwe.getWheelRotation() > 0 ? 1 : -1);
                eta.scrollRectToVisible(rect);
            }

        };
        return mwl;
    }

    /**
     * FIXME: UNUSED METHOD!
     */
    @SuppressWarnings("unused")
    private ComponentListener newTabSelectionFocusGainListener()
    {
        // TODO Auto-generated method stub
        return null;
    }

    private WindowListener newWindowListener()
    {
        return new WindowListener()
        {

            @Override
            public void windowActivated(WindowEvent arg0)
            {
                // TODO Auto-generated method stub

            }

            @Override
            public void windowClosed(WindowEvent arg0)
            {
                SiHistoryFiles.markDocumentClosing(path, System
                        .currentTimeMillis()
                        + client.getClockOffset());
            }

            @Override
            public void windowClosing(WindowEvent arg0)
            {
                SiHistoryFiles.markDocumentClosing(path, System
                        .currentTimeMillis()
                        + client.getClockOffset());
            }

            @Override
            public void windowDeactivated(WindowEvent arg0)
            {
                // TODO Auto-generated method stub

            }

            @Override
            public void windowDeiconified(WindowEvent arg0)
            {
                // TODO Auto-generated method stub

            }

            @Override
            public void windowIconified(WindowEvent arg0)
            {
                // TODO Auto-generated method stub

            }

            @Override
            public void windowOpened(WindowEvent arg0)
            {
                // TODO Auto-generated method stub

            }

        };
    }

    /**
     * Focus for the currently selected document in the editor pane
     */
    @Override
    public void requestFocus()
    {
        System.out.println("Requested focus");
        super.requestFocus();
    }

    /**
     * Set the tabhandle
     * @param tabHandle
     */
    public void setTabHandle(Component tabHandle)
    {
        this.tabHandle = tabHandle;
    }

    public void close()
    {
        this.eta.getSourceDocument().clearAll();
        this.eta = null;
        this.removeAll();
    }
}
